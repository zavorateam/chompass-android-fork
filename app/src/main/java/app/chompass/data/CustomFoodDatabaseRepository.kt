package app.chompass.data

import android.content.Context
import android.util.Log
import app.chompass.models.CustomFoodDatabase
import app.chompass.models.CustomFoodParser
import app.chompass.models.FoodDatabaseSecurity
import app.chompass.models.FoodEntry
import app.chompass.models.toCustomDatabaseHit
import app.chompass.services.grounding.DatabaseSearchResult
import app.chompass.services.grounding.QueryNormalizer
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withContext
import kotlinx.serialization.builtins.ListSerializer
import kotlinx.serialization.json.Json
import okhttp3.OkHttpClient
import okhttp3.Request
import java.io.File
import java.io.FileOutputStream
import java.io.InputStream
import java.util.Locale
import java.util.UUID

class CustomFoodDatabaseRepository(
    private val context: Context,
) {
    private val json = Json { ignoreUnknownKeys = true; prettyPrint = false }
    private val baseDir: File = File(context.filesDir, "custom_food_dbs").apply { mkdirs() }
    private val registryFile: File = File(baseDir, "databases_registry.json")
    private val mutex = Mutex()

    private val _databases = MutableStateFlow<List<CustomFoodDatabase>>(emptyList())
    val databases: StateFlow<List<CustomFoodDatabase>> = _databases.asStateFlow()

    // In-memory cache of foods per database id
    private val foodsCache = mutableMapOf<String, List<FoodEntry>>()

    init {
        loadRegistrySync()
    }

    private fun loadRegistrySync() {
        try {
            if (registryFile.exists()) {
                val text = registryFile.readText()
                val list = json.decodeFromString(ListSerializer(CustomFoodDatabase.serializer()), text)
                _databases.value = list
            }
        } catch (e: Exception) {
            Log.e("CustomFoodDbRepo", "Failed to load database registry", e)
            _databases.value = emptyList()
        }
    }

    private suspend fun persistRegistry(list: List<CustomFoodDatabase>) = withContext(Dispatchers.IO) {
        val temp = File(baseDir, "registry.tmp")
        val content = json.encodeToString(ListSerializer(CustomFoodDatabase.serializer()), list)
        temp.writeText(content)
        temp.renameTo(registryFile)
        _databases.value = list
    }

    private fun getDbItemsFile(databaseId: String): File = File(baseDir, "db_${databaseId}_items.json")

    suspend fun getFoods(databaseId: String): List<FoodEntry> = mutex.withLock {
        foodsCache[databaseId]?.let { return@withLock it }
        withContext(Dispatchers.IO) {
            val file = getDbItemsFile(databaseId)
            if (!file.exists()) return@withContext emptyList<FoodEntry>()
            try {
                val text = file.readText()
                val items = json.decodeFromString(ListSerializer(FoodEntry.serializer()), text)
                foodsCache[databaseId] = items
                items
            } catch (e: Exception) {
                Log.e("CustomFoodDbRepo", "Error reading items for db $databaseId", e)
                emptyList()
            }
        }
    }

    suspend fun getFoodItem(databaseId: String, foodId: String): FoodEntry? {
        val list = getFoods(databaseId)
        return list.firstOrNull { it.id.toString() == foodId }
    }

    suspend fun hasEnabledDatabases(): Boolean {
        return _databases.value.any { it.enabled }
    }

    suspend fun setDatabaseEnabled(databaseId: String, enabled: Boolean) = mutex.withLock {
        val current = _databases.value
        val updated = current.map {
            if (it.id == databaseId) it.copy(enabled = enabled, updatedAtEpochMs = System.currentTimeMillis())
            else it
        }
        persistRegistry(updated)
    }

    suspend fun saveDatabase(database: CustomFoodDatabase, foods: List<FoodEntry>) = mutex.withLock {
        withContext(Dispatchers.IO) {
            val file = getDbItemsFile(database.id)
            val temp = File(baseDir, "db_${database.id}.tmp")
            val content = json.encodeToString(ListSerializer(FoodEntry.serializer()), foods)
            temp.writeText(content)
            temp.renameTo(file)
            foodsCache[database.id] = foods

            val current = _databases.value.toMutableList()
            val existingIdx = current.indexOfFirst { it.id == database.id }
            val updatedDb = database.copy(
                itemCount = foods.size,
                updatedAtEpochMs = System.currentTimeMillis(),
            )
            if (existingIdx >= 0) {
                current[existingIdx] = updatedDb
            } else {
                current.add(0, updatedDb)
            }
            persistRegistry(current)
        }
    }

    suspend fun updateDatabaseMeta(databaseId: String, name: String, description: String) = mutex.withLock {
        val current = _databases.value.toMutableList()
        val idx = current.indexOfFirst { it.id == databaseId }
        if (idx >= 0) {
            current[idx] = current[idx].copy(
                name = FoodDatabaseSecurity.sanitizeText(name, 80).ifBlank { current[idx].name },
                description = FoodDatabaseSecurity.sanitizeText(description, 300),
                updatedAtEpochMs = System.currentTimeMillis(),
            )
            persistRegistry(current)
        }
    }

    suspend fun deleteDatabase(databaseId: String) = mutex.withLock {
        withContext(Dispatchers.IO) {
            val file = getDbItemsFile(databaseId)
            if (file.exists()) {
                file.delete()
            }
            foodsCache.remove(databaseId)

            val current = _databases.value.filterNot { it.id == databaseId }
            persistRegistry(current)
        }
    }

    suspend fun addFoodItem(databaseId: String, food: FoodEntry) = mutex.withLock {
        val foods = getFoodsInternal(databaseId).toMutableList()
        foods.add(0, food)
        saveFoodsInternal(databaseId, foods)
    }

    suspend fun updateFoodItem(databaseId: String, food: FoodEntry) = mutex.withLock {
        val foods = getFoodsInternal(databaseId).toMutableList()
        val idx = foods.indexOfFirst { it.id == food.id }
        if (idx >= 0) {
            foods[idx] = food
            saveFoodsInternal(databaseId, foods)
        }
    }

    suspend fun deleteFoodItem(databaseId: String, foodId: String) = mutex.withLock {
        val foods = getFoodsInternal(databaseId).toMutableList()
        val filtered = foods.filterNot { it.id.toString() == foodId }
        saveFoodsInternal(databaseId, filtered)
    }

    private suspend fun getFoodsInternal(databaseId: String): List<FoodEntry> {
        foodsCache[databaseId]?.let { return it }
        val file = getDbItemsFile(databaseId)
        if (!file.exists()) return emptyList()
        return try {
            val text = file.readText()
            val items = json.decodeFromString(ListSerializer(FoodEntry.serializer()), text)
            foodsCache[databaseId] = items
            items
        } catch (e: Exception) {
            emptyList()
        }
    }

    private suspend fun saveFoodsInternal(databaseId: String, foods: List<FoodEntry>) = withContext(Dispatchers.IO) {
        val file = getDbItemsFile(databaseId)
        val temp = File(baseDir, "db_${databaseId}.tmp")
        val content = json.encodeToString(ListSerializer(FoodEntry.serializer()), foods)
        temp.writeText(content)
        temp.renameTo(file)
        foodsCache[databaseId] = foods

        val current = _databases.value.toMutableList()
        val idx = current.indexOfFirst { it.id == databaseId }
        if (idx >= 0) {
            current[idx] = current[idx].copy(
                itemCount = foods.size,
                updatedAtEpochMs = System.currentTimeMillis(),
            )
            persistRegistry(current)
        }
    }

    /**
     * Downloads, validates, and parses food database content from a public URL.
     * Enforces SSRF protection, size limits, and timeouts.
     */
    suspend fun downloadFromUrl(
        rawUrl: String,
        okHttpClient: OkHttpClient,
        fallbackName: String = "Imported Database",
    ): Pair<CustomFoodDatabase, List<FoodEntry>> = withContext(Dispatchers.IO) {
        val validUrl = FoodDatabaseSecurity.validateUrl(rawUrl).getOrThrow()

        val request = Request.Builder()
            .url(validUrl)
            .header("User-Agent", "Chompass/Android-FoodDatabaseImporter")
            .header("Accept", "application/json, text/csv, text/plain, */*")
            .build()

        val response = okHttpClient.newCall(request).execute()
        if (!response.isSuccessful) {
            throw IllegalArgumentException("HTTP download failed with code ${response.code}: ${response.message}")
        }

        val body = response.body ?: throw IllegalArgumentException("Response body is empty")
        val contentLength = body.contentLength()
        if (contentLength > FoodDatabaseSecurity.MAX_CONTENT_BYTES) {
            throw IllegalArgumentException("File size exceeds 10 MB limit ($contentLength bytes)")
        }

        // Bounded read: never read more than MAX_CONTENT_BYTES
        val inputStream = body.byteStream()
        val text = readBoundedString(inputStream, FoodDatabaseSecurity.MAX_CONTENT_BYTES)

        if (text.isBlank()) {
            throw IllegalArgumentException("Downloaded file content is empty")
        }

        val inferredName = validUrl.substringAfterLast("/")
            .substringBefore("?")
            .removeSuffix(".json")
            .removeSuffix(".csv")
            .takeIf { it.isNotBlank() && it.length > 2 }
            ?: fallbackName

        val parsedList = CustomFoodParser.parseMultiOrSingle(text, inferredName)
        if (parsedList.isEmpty()) {
            throw IllegalArgumentException("No valid food database or food items found in downloaded content")
        }

        val updatedList = parsedList.map { (db, foods) ->
            db.copy(
                sourceUrl = validUrl,
                lastSyncEpochMs = System.currentTimeMillis(),
                updatedAtEpochMs = System.currentTimeMillis(),
                itemCount = foods.size,
            ) to foods
        }

        for ((db, foods) in updatedList) {
            saveDatabase(db, foods)
        }

        updatedList.first()
    }

    /**
     * Re-fetches the database from its stored [sourceUrl] and updates the food items.
     */
    suspend fun updateDatabaseFromUrl(
        databaseId: String,
        okHttpClient: OkHttpClient,
    ): CustomFoodDatabase = withContext(Dispatchers.IO) {
        val db = _databases.value.firstOrNull { it.id == databaseId }
            ?: throw IllegalArgumentException("Database not found")
        val url = db.sourceUrl
        if (url.isNullOrBlank()) {
            throw IllegalArgumentException("Database does not have a source URL configured")
        }

        val (downloadedDb, foods) = downloadFromUrl(url, okHttpClient, db.name)
        val updated = db.copy(
            name = downloadedDb.name.ifBlank { db.name },
            description = downloadedDb.description.ifBlank { db.description },
            itemCount = foods.size,
            lastSyncEpochMs = System.currentTimeMillis(),
            updatedAtEpochMs = System.currentTimeMillis(),
        )
        saveDatabase(updated, foods)
        updated
    }

    private fun readBoundedString(inputStream: InputStream, maxBytes: Long): String {
        val buffer = ByteArray(8192)
        var totalBytes = 0L
        val out = java.io.ByteArrayOutputStream()
        inputStream.use { input ->
            while (true) {
                val read = input.read(buffer)
                if (read == -1) break
                totalBytes += read
                if (totalBytes > maxBytes) {
                    throw IllegalArgumentException("Downloaded data exceeded maximum allowed size of 10 MB")
                }
                out.write(buffer, 0, read)
            }
        }
        return out.toString("UTF-8")
    }

    /**
     * Search foods across all enabled custom databases.
     * Scores hits using token overlap and prefix matching, capped at [limit].
     */
    suspend fun searchActiveDatabases(query: String, limit: Int = 8): List<DatabaseSearchResult> = withContext(Dispatchers.Default) {
        val qClean = query.trim().lowercase(Locale.ROOT)
        if (qClean.isEmpty()) return@withContext emptyList()

        val tokens = QueryNormalizer.tokenize(qClean).map { it.lowercase(Locale.ROOT) }
            .ifEmpty { listOf(qClean) }

        val enabledDbs = _databases.value.filter { it.enabled }
        if (enabledDbs.isEmpty()) return@withContext emptyList()

        val results = mutableListOf<DatabaseSearchResult>()
        for (db in enabledDbs) {
            val foods = getFoods(db.id)
            for (food in foods) {
                val score = calculateMatchScore(food, tokens, qClean)
                if (score > 0.0) {
                    results.add(food.toCustomDatabaseHit(db, score))
                }
            }
        }

        results.sortedByDescending { it.matchScore }.take(limit)
    }

    private fun calculateMatchScore(food: FoodEntry, queryTokens: List<String>, rawQuery: String): Double {
        val nameLower = food.name.lowercase(Locale.ROOT)
        val brandLower = food.customNote?.lowercase(Locale.ROOT).orEmpty()
        val foodTokens = QueryNormalizer.tokenize(nameLower).map { it.lowercase(Locale.ROOT) }

        val qClean = rawQuery.trim().lowercase(Locale.ROOT)
        val nameCompact = nameLower.replace(Regex("[\\s\\p{Punct}—–-]+"), "")
        val queryCompact = qClean.replace(Regex("[\\s\\p{Punct}—–-]+"), "")

        var matchedTokens = 0
        var score = 0.0

        for (qToken in queryTokens) {
            when {
                foodTokens.any { it == qToken } -> {
                    matchedTokens++
                    score += 6.0
                }
                foodTokens.any { it.startsWith(qToken) || qToken.startsWith(it) } -> {
                    matchedTokens++
                    score += 4.0
                }
                nameLower.contains(qToken) || (nameCompact.isNotEmpty() && nameCompact.contains(qToken)) -> {
                    matchedTokens++
                    score += 3.0
                }
                brandLower.contains(qToken) -> {
                    matchedTokens++
                    score += 2.5
                }
            }
        }

        // Substring & prefix bonus on full raw query
        if (qClean.isNotEmpty()) {
            if (nameLower.startsWith(qClean)) {
                matchedTokens = maxOf(matchedTokens, 1)
                score += 8.0
            } else if (nameLower.contains(qClean)) {
                matchedTokens = maxOf(matchedTokens, 1)
                score += 5.0
            } else if (queryCompact.isNotEmpty() && nameCompact.contains(queryCompact)) {
                matchedTokens = maxOf(matchedTokens, 1)
                score += 4.0
            } else if (brandLower.contains(qClean)) {
                matchedTokens = maxOf(matchedTokens, 1)
                score += 3.0
            }
        }

        if (matchedTokens == 0) return 0.0

        // Token coverage bonus
        val coverage = if (queryTokens.isNotEmpty()) {
            (matchedTokens.toDouble() / queryTokens.size.toDouble()).coerceAtMost(1.0)
        } else 1.0
        score *= (0.5 + 0.5 * coverage)

        return score
    }

    suspend fun exportAllDatabasesJson(): String = mutex.withLock {
        val list = mutableListOf<Pair<CustomFoodDatabase, List<FoodEntry>>>()
        for (db in _databases.value) {
            val foods = getFoodsInternal(db.id)
            list.add(db to foods)
        }
        app.chompass.models.CustomFoodExporter.exportAllToJson(list)
    }

    suspend fun importDatabasesJson(jsonContent: String): Int = mutex.withLock {
        val parsed = CustomFoodParser.parseMultiOrSingle(jsonContent)
        if (parsed.isEmpty()) return@withLock 0
        withContext(Dispatchers.IO) {
            val current = _databases.value.toMutableList()
            for ((db, foods) in parsed) {
                val file = getDbItemsFile(db.id)
                val temp = File(baseDir, "db_${db.id}.tmp")
                val content = json.encodeToString(ListSerializer(FoodEntry.serializer()), foods)
                temp.writeText(content)
                temp.renameTo(file)
                foodsCache[db.id] = foods

                val existingIdx = current.indexOfFirst { it.id == db.id }
                val updatedDb = db.copy(
                    itemCount = foods.size,
                    updatedAtEpochMs = System.currentTimeMillis(),
                )
                if (existingIdx >= 0) {
                    current[existingIdx] = updatedDb
                } else {
                    current.add(0, updatedDb)
                }
            }
            persistRegistry(current)
        }
        parsed.size
    }
}
