package app.chompass.data

import android.app.Application
import app.chompass.models.CustomFoodDatabase
import app.chompass.models.CustomFoodExporter
import app.chompass.models.CustomFoodParser
import app.chompass.models.FoodDatabaseSecurity
import app.chompass.models.FoodEntry
import app.chompass.models.NutrientSourceKind
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.RuntimeEnvironment
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [33], application = Application::class)
class CustomFoodDatabaseTest {
    private val context = RuntimeEnvironment.getApplication()

    @Test
    fun security_validateUrl_rejectsLocalAndPrivateAddresses() {
        assertTrue(FoodDatabaseSecurity.validateUrl("http://localhost/db.csv").isFailure)
        assertTrue(FoodDatabaseSecurity.validateUrl("http://127.0.0.1/db.json").isFailure)
        assertTrue(FoodDatabaseSecurity.validateUrl("http://192.168.1.1/foods").isFailure)
        assertTrue(FoodDatabaseSecurity.validateUrl("file:///sdcard/foods.csv").isFailure)
        assertTrue(FoodDatabaseSecurity.validateUrl("javascript:alert(1)").isFailure)

        // Public HTTPS URL should succeed
        val valid = FoodDatabaseSecurity.validateUrl("https://example.com/my_foods.csv")
        assertTrue(valid.isSuccess)
        assertEquals("https://example.com/my_foods.csv", valid.getOrNull())
    }

    @Test
    fun parser_parseCsv_autoDetectsEnglishAndRussianColumns() {
        val csv = """
            Название,Бренд,Калории,Белки,Углеводы,Жиры,Вес
            Творог 5%,Простоквашино,120,16.0,3.0,5.0,100
            Куриная грудка,Петруха,165,31.0,0.0,3.6,100
        """.trimIndent()

        val (db, foods) = CustomFoodParser.parseContent(csv, defaultName = "Тестовая база")

        assertEquals("Тестовая база", db.name)
        assertEquals(2, foods.size)

        val tvorog = foods[0]
        assertEquals("Творог 5%", tvorog.name)
        assertEquals("Простоквашино", tvorog.customNote)
        assertEquals(120, tvorog.calories)
        assertEquals(16.0, tvorog.protein, 0.1)
        assertEquals(3.0, tvorog.carbs, 0.1)
        assertEquals(5.0, tvorog.fat, 0.1)
        assertEquals(NutrientSourceKind.CUSTOM, tvorog.grounding?.sourceKind)
    }

    @Test
    fun parser_parseJson_arrayAndObjectFormats() {
        val jsonArray = """
            [
              {
                "name": "Protein Shake",
                "brand": "Optimum Nutrition",
                "calories": 120,
                "protein": 24,
                "carbs": 3,
                "fat": 1.5,
                "serving_size": 30,
                "unit": "scoop"
              }
            ]
        """.trimIndent()

        val (_, foods) = CustomFoodParser.parseContent(jsonArray, defaultName = "Supplements")

        assertEquals(1, foods.size)
        val shake = foods[0]
        assertEquals("Protein Shake", shake.name)
        assertEquals("Optimum Nutrition", shake.customNote)
        assertEquals(120, shake.calories)
        assertEquals(24.0, shake.protein, 0.1)
        assertEquals(30.0, shake.servingSizeGrams ?: 0.0, 0.1)
        assertEquals("scoop", shake.selectedServingUnit)
    }

    @Test
    fun repository_crudAndSearch() = runBlocking {
        val repo = CustomFoodDatabaseRepository(context)

        val db = CustomFoodDatabase(
            id = "test-db-1",
            name = "Keto Special",
            enabled = true,
        )

        val item1 = FoodEntry(
            name = "Avocado Fresh",
            calories = 160,
            protein = 2.0,
            carbs = 8.5,
            fat = 14.7,
            servingSizeGrams = 100.0,
            customNote = "Organic",
            source = app.chompass.models.FoodSource.SEARCH,
        )

        repo.saveDatabase(db, listOf(item1))

        val loadedDbs = repo.databases.value
        assertTrue(loadedDbs.any { it.id == "test-db-1" })

        val loadedFoods = repo.getFoods("test-db-1")
        assertEquals(1, loadedFoods.size)
        assertEquals("Avocado Fresh", loadedFoods[0].name)

        // Search test
        val hits = repo.searchActiveDatabases("Avocado")
        assertEquals(1, hits.size)
        assertEquals("Avocado Fresh", hits[0].name)
        assertEquals(NutrientSourceKind.CUSTOM, hits[0].sourceKind)

        // Export test
        val csv = CustomFoodExporter.exportToCsv(db, loadedFoods)
        assertTrue(csv.contains("Avocado Fresh"))

        // Cleanup
        repo.deleteDatabase("test-db-1")
        assertFalse(repo.databases.value.any { it.id == "test-db-1" })
    }
}
