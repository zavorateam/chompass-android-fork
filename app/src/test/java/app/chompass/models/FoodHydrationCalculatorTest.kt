package app.chompass.models

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test
import java.util.UUID

class FoodHydrationCalculatorTest {

    @Test
    fun exactWaterFormulaCalculatesCorrectly() {
        // Example from spec: 250 g portion with 98 g water per 100 g -> 245 ml
        val entry = FoodEntry(
            id = UUID.randomUUID(),
            name = "Cucumber salad",
            calories = 50,
            protein = 1.0,
            carbs = 4.0,
            fat = 0.2,
            servingSizeGrams = 250.0,
            waterPer100g = 98.0,
            source = FoodSource.MANUAL,
        )

        val item = FoodHydrationCalculator.calculateEntry(entry)
        assertNotNull(item)
        assertTrue(item!!.isExact)
        assertEquals(245.0, item.waterMl, 0.01)
        assertEquals(98.0, item.waterPercentage, 0.01)
    }

    @Test
    fun compositeMealCalculatesConstituentsWithoutDoubleCounting() {
        val c1 = FoodConstituent(
            name = "Espresso",
            calories = 5,
            protein = 0.2,
            carbs = 0.8,
            fat = 0.1,
            servingSizeGrams = 50.0,
            waterPer100g = 99.0,
        )
        val c2 = FoodConstituent(
            name = "Steamed milk",
            calories = 120,
            protein = 6.0,
            carbs = 9.0,
            fat = 6.0,
            servingSizeGrams = 200.0,
            waterPer100g = 89.0,
        )

        // Parent has no waterPer100g, but constituents have exact values
        val cappuccino = FoodEntry(
            id = UUID.randomUUID(),
            name = "Cappuccino",
            calories = 125,
            protein = 6.2,
            carbs = 9.8,
            fat = 6.1,
            servingSizeGrams = 250.0,
            constituents = listOf(c1, c2),
            source = FoodSource.MANUAL,
        )

        val item = FoodHydrationCalculator.calculateEntry(cappuccino)
        assertNotNull(item)
        assertEquals(2, item!!.constituents.size)
        // c1: 50 * 99 / 100 = 49.5 ml
        // c2: 200 * 89 / 100 = 178.0 ml
        // Total = 49.5 + 178.0 = 227.5 ml
        assertEquals(227.5, item.waterMl, 0.01)
        assertTrue(item.isExact)
    }

    @Test
    fun beverageEstimationProfilesRecognizeCoffeeTeaMilkJuice() {
        val coffee = FoodEntry(
            id = UUID.randomUUID(),
            name = "Black Coffee",
            calories = 5,
            protein = 0.5,
            carbs = 0.5,
            fat = 0.1,
            servingSizeGrams = 300.0,
            waterPer100g = null,
            source = FoodSource.MANUAL,
        )
        val coffeeItem = FoodHydrationCalculator.calculateEntry(coffee)
        assertNotNull(coffeeItem)
        assertFalse(coffeeItem!!.isExact)
        assertEquals(99.0, coffeeItem.waterPercentage, 0.01)
        assertEquals(297.0, coffeeItem.waterMl, 0.01)

        val milk = FoodEntry(
            id = UUID.randomUUID(),
            name = "Whole Milk",
            calories = 150,
            protein = 8.0,
            carbs = 12.0,
            fat = 8.0,
            servingSizeGrams = 250.0,
            waterPer100g = null,
            source = FoodSource.MANUAL,
        )
        val milkItem = FoodHydrationCalculator.calculateEntry(milk)
        assertNotNull(milkItem)
        assertFalse(milkItem!!.isExact)
        assertEquals(89.0, milkItem.waterPercentage, 0.01)
        assertEquals(222.5, milkItem.waterMl, 0.01)

        val orangeJuice = FoodEntry(
            id = UUID.randomUUID(),
            name = "Orange Juice",
            calories = 110,
            protein = 2.0,
            carbs = 26.0,
            fat = 0.5,
            servingSizeGrams = 250.0,
            waterPer100g = null,
            source = FoodSource.MANUAL,
        )
        val juiceItem = FoodHydrationCalculator.calculateEntry(orangeJuice)
        assertNotNull(juiceItem)
        assertFalse(juiceItem!!.isExact)
        assertEquals(88.0, juiceItem.waterPercentage, 0.01)
        assertEquals(220.0, juiceItem.waterMl, 0.01)
    }

    @Test
    fun alcoholicBeveragesAccountForWaterWithoutArbitraryNegativePenalties() {
        val beer = FoodEntry(
            id = UUID.randomUUID(),
            name = "Lager Beer",
            calories = 150,
            protein = 1.0,
            carbs = 13.0,
            fat = 0.0,
            servingSizeGrams = 500.0,
            waterPer100g = null,
            source = FoodSource.MANUAL,
        )
        val beerItem = FoodHydrationCalculator.calculateEntry(beer)
        assertNotNull(beerItem)
        assertFalse(beerItem!!.isExact)
        assertEquals(92.0, beerItem.waterPercentage, 0.01)
        assertEquals(460.0, beerItem.waterMl, 0.01)

        val whiskey = FoodEntry(
            id = UUID.randomUUID(),
            name = "Whiskey",
            calories = 100,
            protein = 0.0,
            carbs = 0.0,
            fat = 0.0,
            servingSizeGrams = 50.0,
            waterPer100g = null,
            source = FoodSource.MANUAL,
        )
        val whiskeyItem = FoodHydrationCalculator.calculateEntry(whiskey)
        assertNotNull(whiskeyItem)
        assertFalse(whiskeyItem!!.isExact)
        assertEquals(60.0, whiskeyItem.waterPercentage, 0.01)
        assertEquals(30.0, whiskeyItem.waterMl, 0.01)
    }

    @Test
    fun solidFoodWithoutWaterDataContributesZeroAndDoesNotGuess() {
        val steak = FoodEntry(
            id = UUID.randomUUID(),
            name = "Grilled Ribeye Steak",
            calories = 650,
            protein = 50.0,
            carbs = 0.0,
            fat = 45.0,
            servingSizeGrams = 300.0,
            waterPer100g = null,
            source = FoodSource.MANUAL,
        )
        val steakItem = FoodHydrationCalculator.calculateEntry(steak)
        assertNull(steakItem)
    }

    @Test
    fun calculateSummaryAggregatesMeasuredAndEstimatedProperly() {
        val watermelon = FoodEntry(
            id = UUID.randomUUID(),
            name = "Watermelon",
            calories = 30,
            protein = 0.6,
            carbs = 7.6,
            fat = 0.2,
            servingSizeGrams = 200.0,
            waterPer100g = 91.5, // Measured
            source = FoodSource.MANUAL,
        )
        val tea = FoodEntry(
            id = UUID.randomUUID(),
            name = "Green Tea",
            calories = 2,
            protein = 0.0,
            carbs = 0.0,
            fat = 0.0,
            servingSizeGrams = 300.0,
            waterPer100g = null, // Estimated 99% = 297 ml
            source = FoodSource.MANUAL,
        )
        val bread = FoodEntry(
            id = UUID.randomUUID(),
            name = "Rye Bread",
            calories = 160,
            protein = 5.0,
            carbs = 30.0,
            fat = 1.0,
            servingSizeGrams = 60.0,
            waterPer100g = null, // 0 ml
            source = FoodSource.MANUAL,
        )

        val summary = FoodHydrationCalculator.calculate(listOf(watermelon, tea, bread))
        // watermelon: 200 * 91.5 / 100 = 183 ml (measured)
        // tea: 300 * 99 / 100 = 297 ml (estimated)
        // total: 183 + 297 = 480 ml
        assertEquals(480, summary.totalMl)
        assertEquals(183, summary.measuredMl)
        assertEquals(297, summary.estimatedMl)
        assertEquals(2, summary.items.size)
    }
}
