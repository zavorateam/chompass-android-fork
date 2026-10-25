package app.chompass.models

import app.chompass.services.ai.FoodAnalysis
import app.chompass.services.grounding.DatabaseSearchResult
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonArray
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.booleanOrNull
import kotlinx.serialization.json.contentOrNull
import kotlinx.serialization.json.doubleOrNull
import kotlinx.serialization.json.intOrNull
import kotlinx.serialization.json.jsonArray
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import java.net.InetAddress
import java.net.URI
import java.util.UUID
import kotlin.math.roundToInt

@Serializable
data class CustomFoodDatabase(
    val id: String = UUID.randomUUID().toString(),
    val name: String,
    val description: String = "",
    val enabled: Boolean = true,
    val sourceUrl: String? = null,
    val lastSyncEpochMs: Long? = null,
    val createdAtEpochMs: Long = System.currentTimeMillis(),
    val updatedAtEpochMs: Long = System.currentTimeMillis(),
    val itemCount: Int = 0,
)

object FoodDatabaseSecurity {
    const val MAX_CONTENT_BYTES: Long = 10 * 1024 * 1024L // 10 MB limit
    const val MAX_FOOD_ITEMS: Int = 5_000

    private val BLOCKED_HOSTNAMES = setOf(
        "localhost",
        "0.0.0.0",
        "127.0.0.1",
        "::1",
    )

    /**
     * Validates that a user-entered URL is a secure public web address:
     * - Only http and https schemes allowed
     * - Reject loopback, link-local, private LAN ranges, and localhost to prevent SSRF
     */
    fun validateUrl(rawUrl: String): Result<String> {
        val trimmed = rawUrl.trim()
        if (trimmed.isEmpty()) {
            return Result.failure(IllegalArgumentException("URL cannot be empty"))
        }

        val uri = try {
            URI(trimmed)
        } catch (e: Exception) {
            return Result.failure(IllegalArgumentException("Invalid URL syntax: ${e.message}"))
        }

        val scheme = uri.scheme?.lowercase() ?: ""
        if (scheme != "http" && scheme != "https") {
            return Result.failure(IllegalArgumentException("Only HTTP and HTTPS URLs are supported"))
        }

        val host = uri.host?.lowercase()
        if (host.isNullOrBlank()) {
            return Result.failure(IllegalArgumentException("URL host is missing"))
        }

        if (host in BLOCKED_HOSTNAMES ||
            host.endsWith(".localhost") ||
            host.endsWith(".local") ||
            host.endsWith(".internal") ||
            host.endsWith(".lan")
        ) {
            return Result.failure(IllegalArgumentException("Access to local/private network addresses is blocked for security"))
        }

        // Check if host is a numeric IP address
        try {
            val addr = InetAddress.getByName(host)
            if (addr.isLoopbackAddress || addr.isAnyLocalAddress || addr.isLinkLocalAddress || addr.isSiteLocalAddress) {
                return Result.failure(IllegalArgumentException("Access to private/local IP addresses is blocked for security"))
            }
        } catch (_: Exception) {
            // Unresolvable at validation time is fine (will be resolved by OkHttp during fetch)
        }

        return Result.success(trimmed)
    }

    fun sanitizeText(raw: String?, maxLength: Int = 200): String {
        if (raw == null) return ""
        val cleaned = raw.replace(Regex("[\\p{Cntrl}&&[^\r\n\t]]"), "").trim()
        return if (cleaned.length <= maxLength) cleaned else cleaned.take(maxLength)
    }

    fun sanitizeNumber(value: Double?, min: Double = 0.0, max: Double = 10_000.0, default: Double = 0.0): Double {
        if (value == null || value.isNaN() || value.isInfinite()) return default
        return value.coerceIn(min, max)
    }
}

object CustomFoodParser {

    /**
     * Parses raw file/URL content as multi-database JSON or single database JSON/CSV.
     * Returns a list of database metadata and parsed food item pairs.
     */
    fun parseMultiOrSingle(rawText: String, defaultName: String = "Imported Database"): List<Pair<CustomFoodDatabase, List<FoodEntry>>> {
        val trimmed = rawText.trim()
        if (trimmed.isEmpty()) {
            throw IllegalArgumentException("File content is empty")
        }

        if (trimmed.startsWith("{")) {
            val json = Json { ignoreUnknownKeys = true; isLenient = true }
            val root = try { json.parseToJsonElement(trimmed) } catch (_: Exception) { null }
            if (root is JsonObject && root.containsKey("databases")) {
                val dbsArray = root["databases"]?.jsonArray
                    ?: throw IllegalArgumentException("Databases element is not an array")
                val results = mutableListOf<Pair<CustomFoodDatabase, List<FoodEntry>>>()
                for (dbElement in dbsArray) {
                    if (dbElement is JsonObject) {
                        val dbObj = dbElement.jsonObject
                        val name = findJsonString(dbObj, listOf("name", "title", "db_name", "database_name", "название")) ?: defaultName
                        val desc = findJsonString(dbObj, listOf("description", "desc", "описание")) ?: ""
                        val sourceUrl = findJsonString(dbObj, listOf("sourceUrl", "source_url", "url"))
                        val foodsArray = dbObj["foods"]?.jsonArray
                            ?: dbObj["items"]?.jsonArray
                            ?: dbObj["products"]?.jsonArray
                            ?: dbObj["data"]?.jsonArray

                        if (foodsArray != null) {
                            val fakeJsonStr = JsonObject(mapOf(
                                "name" to kotlinx.serialization.json.JsonPrimitive(name),
                                "description" to kotlinx.serialization.json.JsonPrimitive(desc),
                                "foods" to foodsArray
                            )).toString()
                            val pair = parseJson(fakeJsonStr, name, sourceUrl)
                            results.add(pair)
                        }
                    }
                }
                if (results.isNotEmpty()) return results
            }
        }

        return listOf(parseContent(rawText, defaultName))
    }

    /**
     * Parses raw file/URL content as either JSON or CSV, auto-detecting the format
     * and auto-mapping column names. Returns a pair of database metadata and parsed food items.
     */
    fun parseContent(rawText: String, defaultName: String, sourceUrl: String? = null): Pair<CustomFoodDatabase, List<FoodEntry>> {
        val trimmed = rawText.trim()
        if (trimmed.isEmpty()) {
            throw IllegalArgumentException("File content is empty")
        }

        return if (trimmed.startsWith("[") || trimmed.startsWith("{")) {
            parseJson(trimmed, defaultName, sourceUrl)
        } else {
            parseCsv(trimmed, defaultName, sourceUrl)
        }
    }

    fun parseJson(jsonString: String, defaultName: String, sourceUrl: String? = null): Pair<CustomFoodDatabase, List<FoodEntry>> {
        val json = Json { ignoreUnknownKeys = true; isLenient = true }
        val rootElement: JsonElement = try {
            json.parseToJsonElement(jsonString)
        } catch (e: Exception) {
            throw IllegalArgumentException("Invalid JSON format: ${e.message}")
        }

        var dbName = defaultName
        var dbDescription = ""
        val foodElements: JsonArray

        if (rootElement is JsonArray) {
            foodElements = rootElement
        } else if (rootElement is JsonObject) {
            val obj = rootElement.jsonObject
            dbName = obj["name"]?.jsonPrimitive?.contentOrNull?.takeIf { it.isNotBlank() } ?: defaultName
            dbDescription = obj["description"]?.jsonPrimitive?.contentOrNull ?: ""
            val itemsArray = obj["foods"]?.jsonArray
                ?: obj["items"]?.jsonArray
                ?: obj["products"]?.jsonArray
                ?: obj["data"]?.jsonArray
                ?: throw IllegalArgumentException("JSON object does not contain a 'foods' or 'items' array")
            foodElements = itemsArray
        } else {
            throw IllegalArgumentException("Unsupported JSON root element")
        }

        val db = CustomFoodDatabase(
            name = FoodDatabaseSecurity.sanitizeText(dbName, 80).ifBlank { defaultName },
            description = FoodDatabaseSecurity.sanitizeText(dbDescription, 300),
            sourceUrl = sourceUrl,
            lastSyncEpochMs = if (sourceUrl != null) System.currentTimeMillis() else null,
            itemCount = 0,
        )

        val foods = mutableListOf<FoodEntry>()
        for (i in 0 until minOf(foodElements.size, FoodDatabaseSecurity.MAX_FOOD_ITEMS)) {
            val itemObj = foodElements[i].let { if (it is JsonObject) it else null } ?: continue
            val name = findJsonString(itemObj, listOf("name", "food", "food_name", "title", "product", "item", "название", "продукт", "наименование"))
            val cleanName = FoodDatabaseSecurity.sanitizeText(name)
            if (cleanName.isBlank()) continue

            val brand = findJsonString(itemObj, listOf("brand", "manufacturer", "vendor", "бренд", "производитель"))
            val calories = findJsonDouble(itemObj, listOf("calories", "calorie", "kcal", "energy", "калории", "ккал", "энергия")) ?: 0.0
            val protein = findJsonDouble(itemObj, listOf("protein", "proteins", "белки", "белок")) ?: 0.0
            val carbs = findJsonDouble(itemObj, listOf("carbs", "carbohydrate", "carbohydrates", "углеводы")) ?: 0.0
            val fat = findJsonDouble(itemObj, listOf("fat", "fats", "total_fat", "жиры", "жир")) ?: 0.0
            val servingSizeGrams = findJsonDouble(itemObj, listOf("serving", "serving_size", "serving_grams", "grams", "weight", "порция", "вес")) ?: 100.0
            val servingUnit = findJsonString(itemObj, listOf("unit", "serving_unit", "единица"))?.ifBlank { "g" } ?: "g"
            val fiber = findJsonDouble(itemObj, listOf("fiber", "клетчатка"))
            val sugar = findJsonDouble(itemObj, listOf("sugar", "сахар"))
            val sodium = findJsonDouble(itemObj, listOf("sodium", "натрий", "salt", "соль"))
            val water = findJsonDouble(itemObj, listOf("water", "water_g", "water_ml", "water_per_100g", "вода", "жидкость"))
            val waterPer100g = water?.let { w ->
                if (itemObj.containsKey("water_per_100g")) {
                    FoodDatabaseSecurity.sanitizeNumber(w, 0.0, 100.0)
                } else if (servingSizeGrams > 0) {
                    FoodDatabaseSecurity.sanitizeNumber((w / servingSizeGrams) * 100.0, 0.0, 100.0)
                } else {
                    FoodDatabaseSecurity.sanitizeNumber(w, 0.0, 100.0)
                }
            }

            val entry = FoodEntry(
                id = UUID.randomUUID(),
                name = cleanName,
                calories = FoodDatabaseSecurity.sanitizeNumber(calories, 0.0, 10_000.0).roundToInt(),
                protein = FoodDatabaseSecurity.sanitizeNumber(protein, 0.0, 1_000.0),
                carbs = FoodDatabaseSecurity.sanitizeNumber(carbs, 0.0, 1_000.0),
                fat = FoodDatabaseSecurity.sanitizeNumber(fat, 0.0, 1_000.0),
                servingSizeGrams = FoodDatabaseSecurity.sanitizeNumber(servingSizeGrams, 0.1, 10_000.0, 100.0),
                selectedServingUnit = servingUnit,
                selectedServingQuantity = 1.0,
                fiber = fiber?.let { FoodDatabaseSecurity.sanitizeNumber(it, 0.0, 1_000.0) },
                sugar = sugar?.let { FoodDatabaseSecurity.sanitizeNumber(it, 0.0, 1_000.0) },
                sodium = sodium?.let { FoodDatabaseSecurity.sanitizeNumber(it, 0.0, 50_000.0) },
                waterPer100g = waterPer100g,
                customNote = brand?.let { FoodDatabaseSecurity.sanitizeText(it) }?.takeIf { it.isNotBlank() },
                source = FoodSource.SEARCH,
                grounding = FoodGroundingProvenance(
                    sourceKind = NutrientSourceKind.CUSTOM,
                    sourceName = db.name,
                    nutrientBasis = NutrientBasis.PER_SERVING,
                    datasetVersion = db.name,
                    retrievedAtEpochMs = System.currentTimeMillis(),
                ),
            )
            foods.add(entry)
        }

        if (foods.isEmpty()) {
            throw IllegalArgumentException("No valid food items found in JSON")
        }

        return db.copy(itemCount = foods.size) to foods
    }

    fun parseCsv(csvString: String, defaultName: String, sourceUrl: String? = null): Pair<CustomFoodDatabase, List<FoodEntry>> {
        val lines = csvString.lines().map { it.trim() }.filter { it.isNotEmpty() }
        if (lines.isEmpty()) {
            throw IllegalArgumentException("CSV content is empty")
        }

        val headerLine = lines.first()
        val delimiter = detectDelimiter(headerLine)
        val headers = parseCsvLine(headerLine, delimiter).map { normalizeKey(it) }

        val nameIdx = findHeaderIndex(headers, listOf("name", "food", "foodname", "item", "title", "product", "productname", "название", "продукт", "наименование", "блюдо"))
        if (nameIdx == -1) {
            throw IllegalArgumentException("CSV header must contain a column for name (e.g. name, food, product, название)")
        }

        val brandIdx = findHeaderIndex(headers, listOf("brand", "manufacturer", "vendor", "бренд", "производитель", "фирма"))
        val calIdx = findHeaderIndex(headers, listOf("calories", "calorie", "kcal", "energy", "energykcal", "калории", "ккал", "калорийность", "энергия"))
        val proIdx = findHeaderIndex(headers, listOf("protein", "proteins", "белки", "белок", "протеин"))
        val carbIdx = findHeaderIndex(headers, listOf("carbs", "carbohydrate", "carbohydrates", "углеводы", "углевод"))
        val fatIdx = findHeaderIndex(headers, listOf("fat", "fats", "totalfat", "жиры", "жир"))
        val servIdx = findHeaderIndex(headers, listOf("serving", "servingsize", "servinggrams", "grams", "weight", "порция", "вес", "грамм"))
        val unitIdx = findHeaderIndex(headers, listOf("unit", "servingunit", "единица", "ед"))
        val fiberIdx = findHeaderIndex(headers, listOf("fiber", "dietaryfiber", "клетчатка", "волокна"))
        val sugarIdx = findHeaderIndex(headers, listOf("sugar", "sugars", "сахар"))
        val sodiumIdx = findHeaderIndex(headers, listOf("sodium", "salt", "натрий", "соль"))
        val waterIdx = findHeaderIndex(headers, listOf("water", "water_g", "water_ml", "water_per_100g", "вода", "жидкость"))

        val db = CustomFoodDatabase(
            name = defaultName,
            sourceUrl = sourceUrl,
            lastSyncEpochMs = if (sourceUrl != null) System.currentTimeMillis() else null,
            itemCount = 0,
        )

        val foods = mutableListOf<FoodEntry>()
        for (line in lines.drop(1).take(FoodDatabaseSecurity.MAX_FOOD_ITEMS)) {
            val cols = parseCsvLine(line, delimiter)
            if (cols.size <= nameIdx) continue
            val rawName = cols[nameIdx]
            val cleanName = FoodDatabaseSecurity.sanitizeText(rawName)
            if (cleanName.isBlank()) continue

            val brand = brandIdx.takeIf { it != -1 && it < cols.size }?.let { cols[it] }?.let { FoodDatabaseSecurity.sanitizeText(it) }?.takeIf { it.isNotBlank() }
            val calories = calIdx.takeIf { it != -1 && it < cols.size }?.let { parseNumeric(cols[it]) } ?: 0.0
            val protein = proIdx.takeIf { it != -1 && it < cols.size }?.let { parseNumeric(cols[it]) } ?: 0.0
            val carbs = carbIdx.takeIf { it != -1 && it < cols.size }?.let { parseNumeric(cols[it]) } ?: 0.0
            val fat = fatIdx.takeIf { it != -1 && it < cols.size }?.let { parseNumeric(cols[it]) } ?: 0.0
            val servingSizeGrams = servIdx.takeIf { it != -1 && it < cols.size }?.let { parseNumeric(cols[it]) } ?: 100.0
            val servingUnit = unitIdx.takeIf { it != -1 && it < cols.size }?.let { cols[it].trim().ifBlank { "g" } } ?: "g"
            val fiber = fiberIdx.takeIf { it != -1 && it < cols.size }?.let { parseNumeric(cols[it]) }
            val sugar = sugarIdx.takeIf { it != -1 && it < cols.size }?.let { parseNumeric(cols[it]) }
            val sodium = sodiumIdx.takeIf { it != -1 && it < cols.size }?.let { parseNumeric(cols[it]) }
            val waterRaw = waterIdx.takeIf { it != -1 && it < cols.size }?.let { parseNumeric(cols[it]) }
            val waterPer100g = waterRaw?.let { w ->
                val isExplicitPer100g = headers.getOrNull(waterIdx)?.contains("100") == true
                if (isExplicitPer100g || servingSizeGrams <= 0) {
                    FoodDatabaseSecurity.sanitizeNumber(w, 0.0, 100.0)
                } else {
                    FoodDatabaseSecurity.sanitizeNumber((w / servingSizeGrams) * 100.0, 0.0, 100.0)
                }
            }

            val entry = FoodEntry(
                id = UUID.randomUUID(),
                name = cleanName,
                calories = FoodDatabaseSecurity.sanitizeNumber(calories, 0.0, 10_000.0).roundToInt(),
                protein = FoodDatabaseSecurity.sanitizeNumber(protein, 0.0, 1_000.0),
                carbs = FoodDatabaseSecurity.sanitizeNumber(carbs, 0.0, 1_000.0),
                fat = FoodDatabaseSecurity.sanitizeNumber(fat, 0.0, 1_000.0),
                servingSizeGrams = FoodDatabaseSecurity.sanitizeNumber(servingSizeGrams, 0.1, 10_000.0, 100.0),
                selectedServingUnit = servingUnit,
                selectedServingQuantity = 1.0,
                fiber = fiber?.let { FoodDatabaseSecurity.sanitizeNumber(it, 0.0, 1_000.0) },
                sugar = sugar?.let { FoodDatabaseSecurity.sanitizeNumber(it, 0.0, 1_000.0) },
                sodium = sodium?.let { FoodDatabaseSecurity.sanitizeNumber(it, 0.0, 50_000.0) },
                waterPer100g = waterPer100g,
                customNote = brand,
                source = FoodSource.SEARCH,
                grounding = FoodGroundingProvenance(
                    sourceKind = NutrientSourceKind.CUSTOM,
                    sourceName = db.name,
                    nutrientBasis = NutrientBasis.PER_SERVING,
                    datasetVersion = db.name,
                    retrievedAtEpochMs = System.currentTimeMillis(),
                ),
            )
            foods.add(entry)
        }

        if (foods.isEmpty()) {
            throw IllegalArgumentException("No valid food items found in CSV")
        }

        return db.copy(itemCount = foods.size) to foods
    }

    private fun detectDelimiter(line: String): Char {
        val commaCount = line.count { it == ',' }
        val semiCount = line.count { it == ';' }
        val tabCount = line.count { it == '\t' }
        return when {
            semiCount > commaCount && semiCount > tabCount -> ';'
            tabCount > commaCount && tabCount > semiCount -> '\t'
            else -> ','
        }
    }

    private fun parseCsvLine(line: String, delimiter: Char): List<String> {
        val result = mutableListOf<String>()
        val current = StringBuilder()
        var inQuotes = false
        var i = 0
        while (i < line.length) {
            val c = line[i]
            if (c == '"') {
                if (inQuotes && i + 1 < line.length && line[i + 1] == '"') {
                    current.append('"')
                    i++
                } else {
                    inQuotes = !inQuotes
                }
            } else if (c == delimiter && !inQuotes) {
                result.add(current.toString().trim())
                current.setLength(0)
            } else {
                current.append(c)
            }
            i++
        }
        result.add(current.toString().trim())
        return result
    }

    private fun normalizeKey(raw: String): String {
        return raw.lowercase()
            .replace("_", "")
            .replace("-", "")
            .replace(" ", "")
            .replace("\"", "")
            .replace("'", "")
    }

    private fun findHeaderIndex(headers: List<String>, candidates: List<String>): Int {
        val normalizedCandidates = candidates.map { normalizeKey(it) }
        return headers.indexOfFirst { h -> normalizedCandidates.any { c -> h == c || h.contains(c) } }
    }

    private fun parseNumeric(raw: String): Double? {
        val cleaned = raw.replace(",", ".").replace(Regex("[^0-9.]"), "")
        return cleaned.toDoubleOrNull()
    }

    private fun findJsonString(obj: JsonObject, candidates: List<String>): String? {
        for (key in candidates) {
            val element = obj[key] ?: obj[normalizeKey(key)]
            if (element != null) {
                return element.jsonPrimitive.contentOrNull
            }
        }
        // Fallback: check matching case-insensitive keys
        for ((k, v) in obj) {
            if (normalizeKey(k) in candidates.map { normalizeKey(it) }) {
                return v.jsonPrimitive.contentOrNull
            }
        }
        return null
    }

    private fun findJsonDouble(obj: JsonObject, candidates: List<String>): Double? {
        for (key in candidates) {
            val element = obj[key] ?: obj[normalizeKey(key)]
            if (element != null) {
                element.jsonPrimitive.doubleOrNull?.let { return it }
                element.jsonPrimitive.intOrNull?.let { return it.toDouble() }
                element.jsonPrimitive.contentOrNull?.let { parseNumeric(it) }?.let { return it }
            }
        }
        for ((k, v) in obj) {
            if (normalizeKey(k) in candidates.map { normalizeKey(it) }) {
                v.jsonPrimitive.doubleOrNull?.let { return it }
                v.jsonPrimitive.intOrNull?.let { return it.toDouble() }
                v.jsonPrimitive.contentOrNull?.let { parseNumeric(it) }?.let { return it }
            }
        }
        return null
    }
}

object CustomFoodExporter {
    fun exportToCsv(database: CustomFoodDatabase, foods: List<FoodEntry>): String {
        val sb = StringBuilder()
        sb.append("name,brand,calories,protein,carbs,fat,serving_size,serving_unit,fiber,sugar,sodium\n")
        for (f in foods) {
            fun esc(s: String?): String {
                val str = s ?: ""
                return if (str.contains(",") || str.contains("\"") || str.contains("\n")) {
                    "\"${str.replace("\"", "\"\"")}\""
                } else str
            }
            sb.append(esc(f.name)).append(",")
            sb.append(esc(f.customNote)).append(",")
            sb.append(f.calories).append(",")
            sb.append(f.protein).append(",")
            sb.append(f.carbs).append(",")
            sb.append(f.fat).append(",")
            sb.append(f.servingSizeGrams ?: 100.0).append(",")
            sb.append(esc(f.selectedServingUnit ?: "g")).append(",")
            sb.append(f.fiber ?: "").append(",")
            sb.append(f.sugar ?: "").append(",")
            sb.append(f.sodium ?: "").append("\n")
        }
        return sb.toString()
    }

    fun exportToJson(database: CustomFoodDatabase, foods: List<FoodEntry>): String {
        val json = Json { prettyPrint = true }
        val array = JsonArray(foods.map { f ->
            JsonObject(buildMap {
                put("name", kotlinx.serialization.json.JsonPrimitive(f.name))
                f.customNote?.takeIf { it.isNotBlank() }?.let { put("brand", kotlinx.serialization.json.JsonPrimitive(it)) }
                put("calories", kotlinx.serialization.json.JsonPrimitive(f.calories))
                put("protein", kotlinx.serialization.json.JsonPrimitive(f.protein))
                put("carbs", kotlinx.serialization.json.JsonPrimitive(f.carbs))
                put("fat", kotlinx.serialization.json.JsonPrimitive(f.fat))
                put("serving_size", kotlinx.serialization.json.JsonPrimitive(f.servingSizeGrams ?: 100.0))
                put("serving_unit", kotlinx.serialization.json.JsonPrimitive(f.selectedServingUnit ?: "g"))
                f.fiber?.let { put("fiber", kotlinx.serialization.json.JsonPrimitive(it)) }
                f.sugar?.let { put("sugar", kotlinx.serialization.json.JsonPrimitive(it)) }
                f.sodium?.let { put("sodium", kotlinx.serialization.json.JsonPrimitive(it)) }
            })
        })
        val root = JsonObject(mapOf(
            "name" to kotlinx.serialization.json.JsonPrimitive(database.name),
            "description" to kotlinx.serialization.json.JsonPrimitive(database.description),
            "foods" to array,
        ))
        return json.encodeToString(JsonObject.serializer(), root)
    }

    fun exportAllToJson(allDatabases: List<Pair<CustomFoodDatabase, List<FoodEntry>>>): String {
        val json = Json { prettyPrint = true }
        val dbsArray = JsonArray(allDatabases.map { (db, foods) ->
            val foodsArray = JsonArray(foods.map { f ->
                JsonObject(buildMap {
                    put("name", kotlinx.serialization.json.JsonPrimitive(f.name))
                    f.customNote?.takeIf { it.isNotBlank() }?.let { put("brand", kotlinx.serialization.json.JsonPrimitive(it)) }
                    put("calories", kotlinx.serialization.json.JsonPrimitive(f.calories))
                    put("protein", kotlinx.serialization.json.JsonPrimitive(f.protein))
                    put("carbs", kotlinx.serialization.json.JsonPrimitive(f.carbs))
                    put("fat", kotlinx.serialization.json.JsonPrimitive(f.fat))
                    put("serving_size", kotlinx.serialization.json.JsonPrimitive(f.servingSizeGrams ?: 100.0))
                    put("serving_unit", kotlinx.serialization.json.JsonPrimitive(f.selectedServingUnit ?: "g"))
                    f.fiber?.let { put("fiber", kotlinx.serialization.json.JsonPrimitive(it)) }
                    f.sugar?.let { put("sugar", kotlinx.serialization.json.JsonPrimitive(it)) }
                    f.sodium?.let { put("sodium", kotlinx.serialization.json.JsonPrimitive(it)) }
                })
            })
            JsonObject(buildMap {
                put("id", kotlinx.serialization.json.JsonPrimitive(db.id))
                put("name", kotlinx.serialization.json.JsonPrimitive(db.name))
                put("description", kotlinx.serialization.json.JsonPrimitive(db.description))
                db.sourceUrl?.let { put("sourceUrl", kotlinx.serialization.json.JsonPrimitive(it)) }
                put("foods", foodsArray)
            })
        })
        val root = JsonObject(mapOf(
            "version" to kotlinx.serialization.json.JsonPrimitive(1),
            "exportedAtEpochMs" to kotlinx.serialization.json.JsonPrimitive(System.currentTimeMillis()),
            "databases" to dbsArray,
        ))
        return json.encodeToString(JsonObject.serializer(), root)
    }
}

/**
 * Maps a [FoodEntry] stored in a custom database to [DatabaseSearchResult] for search integration.
 */
fun FoodEntry.toCustomDatabaseHit(database: CustomFoodDatabase, score: Double): DatabaseSearchResult {
    val grams = servingSizeGrams ?: 100.0
    return DatabaseSearchResult(
        sourceKind = NutrientSourceKind.CUSTOM,
        sourceId = "${database.id}:${id}",
        name = name,
        brand = customNote ?: database.name,
        servingGrams = grams,
        caloriesPerServing = calories.toDouble(),
        proteinPerServing = protein,
        carbsPerServing = carbs,
        fatPerServing = fat,
        incompleteEnergy = false,
        datasetVersion = database.name,
        matchScore = score,
    )
}

/**
 * Converts a custom database [FoodEntry] into a scaled [FoodAnalysis] ready for food log addition.
 */
fun FoodEntry.toFoodAnalysis(targetGrams: Double, dbName: String): FoodAnalysis {
    val baseGrams = servingSizeGrams?.takeIf { it > 0 } ?: 100.0
    val scale = targetGrams / baseGrams
    return FoodAnalysis(
        name = name,
        calories = (calories * scale).roundToInt(),
        protein = protein * scale,
        carbs = carbs * scale,
        fat = fat * scale,
        servingSizeGrams = targetGrams,
        waterPer100g = waterPer100g,
        emoji = emoji ?: "🍽",
        sugar = sugar?.let { it * scale },
        addedSugar = addedSugar?.let { it * scale },
        fiber = fiber?.let { it * scale },
        saturatedFat = saturatedFat?.let { it * scale },
        sodium = sodium?.let { it * scale },
        servingUnitOptions = servingUnitOptions.ifEmpty {
            listOf(ServingUnitOption(unit = selectedServingUnit ?: "g", gramsPerUnit = baseGrams))
        },
        selectedServingUnit = selectedServingUnit ?: "g",
        selectedServingQuantity = if (baseGrams > 0) targetGrams / baseGrams else 1.0,
        customNote = customNote,
        grounding = FoodGroundingProvenance(
            sourceKind = NutrientSourceKind.CUSTOM,
            sourceId = id.toString(),
            sourceName = dbName,
            nutrientBasis = if (servingSizeGrams != null) NutrientBasis.PER_SERVING else NutrientBasis.PER_100G,
            datasetVersion = dbName,
            retrievedAtEpochMs = System.currentTimeMillis(),
            identityEvidence = "custom-food-database:$name",
        ),
    )
}
