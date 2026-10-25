package app.chompass.services.grounding

import android.app.Application
import app.chompass.data.CustomFoodDatabaseRepository
import app.chompass.data.PreferencesStore
import app.chompass.models.CustomFoodParser
import app.chompass.models.NutrientSourceKind
import app.chompass.services.ai.FoodAnalysis
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.RuntimeEnvironment
import org.robolectric.annotation.Config
import java.io.File

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [33], application = Application::class)
class CustomFoodDatabaseSearchTest {
    private val context = RuntimeEnvironment.getApplication()

    private fun prepareOfflineFoodIndexes() {
        val usdaAsset = listOf(
            File("app/src/main/assets/usda/usda_foods.sqlite"),
            File("src/main/assets/usda/usda_foods.sqlite"),
        ).firstOrNull { it.exists() }

        val swissAsset = listOf(
            File("app/src/main/assets/swiss/swiss_foods.sqlite"),
            File("src/main/assets/swiss/swiss_foods.sqlite"),
        ).firstOrNull { it.exists() }

        if (usdaAsset != null) {
            val target = File(context.filesDir, "usda_foods.sqlite")
            usdaAsset.copyTo(target, overwrite = true)
            File(context.filesDir, "usda_foods.sqlite.sha256").writeText("test")
        }

        if (swissAsset != null) {
            val target = File(context.filesDir, "swiss_foods.sqlite")
            swissAsset.copyTo(target, overwrite = true)
            File(context.filesDir, "swiss_foods.sqlite.sha256").writeText("test")
        }
    }

    private fun loadVkusnoJson(): String {
        val candidates = listOf(
            File("public/assets/aistudio/vkusno_i_tochka_b5d56b1e.json"),
            File("/public/assets/aistudio/vkusno_i_tochka_b5d56b1e.json"),
            File("../public/assets/aistudio/vkusno_i_tochka_b5d56b1e.json"),
        )
        for (f in candidates) {
            if (f.exists()) {
                return f.readText()
            }
        }
        return """
        {
          "name": "Вкусно — и точка",
          "description": "Продукты из ресторана",
          "foods": [
            {
              "name": "Hello Kitty Бургер",
              "brand": "Вкусно — и точка",
              "calories": 538.0,
              "protein": 21.0,
              "carbs": 53.0,
              "fat": 27.0,
              "serving_size": 235.0,
              "serving_unit": "g"
            },
            {
              "name": "Айс Де Люкс Ананас",
              "brand": "Вкусно — и точка",
              "calories": 290.0,
              "protein": 5.5,
              "carbs": 54.0,
              "fat": 5.6,
              "serving_size": 200.0,
              "serving_unit": "g"
            },
            {
              "name": "Наггетсы",
              "brand": "Вкусно — и точка",
              "calories": 179.0,
              "protein": 11.0,
              "carbs": 13.0,
              "fat": 9.0,
              "serving_size": 72.0,
              "serving_unit": "g"
            }
          ]
        }
        """.trimIndent()
    }

    @Test
    fun parseAndSearchVkusnoCustomDatabase() = runBlocking {
        val repo = CustomFoodDatabaseRepository(context)
        val json = loadVkusnoJson()

        val (db, foods) = CustomFoodParser.parseContent(json, defaultName = "Вкусно — и точка")
        assertEquals("Вкусно — и точка", db.name)
        assertTrue("Foods list should not be empty", foods.isNotEmpty())
        repo.saveDatabase(db, foods)

        // 1. Direct search by Cyrillic food name
        val burgerHits = repo.searchActiveDatabases("бургер", limit = 10)
        assertTrue("Expected burger hits from custom database, got ${burgerHits.size}", burgerHits.isNotEmpty())
        assertTrue(burgerHits.all { it.sourceKind == NutrientSourceKind.CUSTOM })
        assertTrue(burgerHits.any { it.name.contains("бургер", ignoreCase = true) })

        // 2. Direct search by brand
        val brandHits = repo.searchActiveDatabases("Вкусно", limit = 10)
        assertTrue("Expected hits when searching by brand 'Вкусно', got ${brandHits.size}", brandHits.isNotEmpty())

        // 3. Search multi-word Cyrillic term
        val iceHits = repo.searchActiveDatabases("Айс Де Люкс", limit = 10)
        assertTrue("Expected hits for 'Айс Де Люкс', got ${iceHits.size}", iceHits.isNotEmpty())
    }

    @Test
    fun searchOffline_includesCustomDatabasesOnMainScreenPath() = runBlocking {
        prepareOfflineFoodIndexes()
        val repo = CustomFoodDatabaseRepository(context)
        val json = loadVkusnoJson()
        val (db, foods) = CustomFoodParser.parseContent(json, defaultName = "Вкусно — и точка")
        repo.saveDatabase(db, foods)

        val prefs = PreferencesStore(context)
        val search = FoodDatabaseSearch(
            prefs = prefs,
            usda = UsdaFoodIndex(context),
            swiss = SwissFoodIndex(context),
            customFoodDbRepository = repo,
        )

        // Home screen calls container.foodDatabaseSearch.searchOffline(query)
        val results = search.searchOffline("бургер", limit = 12)
        assertTrue("Expected results from searchOffline, got ${results.size}", results.isNotEmpty())

        val customHits = results.filter { it.sourceKind == NutrientSourceKind.CUSTOM }
        assertTrue("searchOffline MUST include CUSTOM database hits on the main screen path!", customHits.isNotEmpty())
        assertEquals("Вкусно — и точка", customHits.first().datasetVersion)
    }

    @Test
    fun foodSuggestionRanker_ranksCustomDatabaseHits() = runBlocking {
        val repo = CustomFoodDatabaseRepository(context)
        val json = loadVkusnoJson()
        val (db, foods) = CustomFoodParser.parseContent(json, defaultName = "Вкусно — и точка")
        repo.saveDatabase(db, foods)

        val hits = repo.searchActiveDatabases("бургер", limit = 6)
        assertTrue(hits.isNotEmpty())

        val suggestions = FoodSuggestionRanker.rank(
            query = "бургер",
            saved = emptyList(),
            recipes = emptyList(),
            database = hits,
        )

        assertTrue("Expected suggestions from custom database hits", suggestions.isNotEmpty())
        val firstHit = suggestions.first() as? FoodSuggestion.DatabaseHit
        assertNotNull("Expected DatabaseHit suggestion", firstHit)
        assertEquals(NutrientSourceKind.CUSTOM, firstHit!!.result.sourceKind)
    }

    @Test
    fun toAnalysis_resolvesCustomFoodNutrientsAccurately() = runBlocking {
        prepareOfflineFoodIndexes()
        val repo = CustomFoodDatabaseRepository(context)
        val json = loadVkusnoJson()
        val (db, foods) = CustomFoodParser.parseContent(json, defaultName = "Вкусно — и точка")
        repo.saveDatabase(db, foods)

        val prefs = PreferencesStore(context)
        val search = FoodDatabaseSearch(
            prefs = prefs,
            usda = UsdaFoodIndex(context),
            swiss = SwissFoodIndex(context),
            customFoodDbRepository = repo,
        )

        val hits = search.searchOffline("Hello Kitty Бургер", limit = 5)
        val customHit = hits.firstOrNull { it.sourceKind == NutrientSourceKind.CUSTOM }
        assertNotNull("Expected Hello Kitty Бургер in custom hits", customHit)

        val analysis = search.toAnalysis(customHit!!)
        assertEquals(NutrientSourceKind.CUSTOM, analysis.grounding?.sourceKind)
        assertTrue("Calories should be > 0", analysis.calories > 0)
        assertTrue("Protein should be > 0", analysis.protein > 0)
        assertTrue("Carbs should be > 0", analysis.carbs > 0)
        assertTrue("Fat should be > 0", analysis.fat > 0)
        assertEquals("Вкусно — и точка", analysis.grounding?.sourceName)
    }
}
