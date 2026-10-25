package app.chompass.models

import java.util.Locale
import java.util.UUID
import kotlin.math.roundToInt

/**
 * Unified calculation layer for dietary hydration (water intake from food and beverages).
 *
 * Rule:
 * 1. Exact measurement: When [FoodEntry.waterPer100g] or [FoodConstituent.waterPer100g]
 *    is provided by the database/source:
 *      waterMl = consumedGrams * waterPer100g / 100.0
 * 2. Composite meals: When an entry has non-empty constituents, water is computed
 *    from each constituent and summed up, avoiding double-counting with the parent meal.
 * 3. Centralized estimation profiles: For known beverage categories and high-water items
 *    lacking exact database numbers, realistic water ratios are applied and explicitly
 *    flagged as estimates (distinguished from measured values).
 * 4. Solid food without water data does NOT assume 100% or guess chemical composition;
 *    unmatched solid food contributes 0 ml.
 * 5. Alcoholic drinks: Water fraction is accounted for (e.g. ~60% in spirits, ~86% in wine,
 *    ~92% in beer) without arbitrary negative dehydrating penalties.
 */
object FoodHydrationCalculator {

    data class ItemHydration(
        val foodId: UUID,
        val foodName: String,
        val portionGrams: Double,
        val waterMl: Double,
        val waterPercentage: Double,
        val isExact: Boolean,
        val categoryLabel: String? = null,
        val constituents: List<ConstituentHydration> = emptyList(),
    )

    data class ConstituentHydration(
        val name: String,
        val portionGrams: Double,
        val waterMl: Double,
        val waterPercentage: Double,
        val isExact: Boolean,
        val categoryLabel: String? = null,
    )

    data class FoodWaterSummary(
        val totalMl: Int,
        val measuredMl: Int,
        val estimatedMl: Int,
        val items: List<ItemHydration> = emptyList(),
    ) {
        val hasWater: Boolean get() = totalMl > 0
    }

    val EmptySummary = FoodWaterSummary(
        totalMl = 0,
        measuredMl = 0,
        estimatedMl = 0,
        items = emptyList(),
    )

    /** Category profile with estimated water ratio (0.0 .. 1.0) and display name. */
    private data class CategoryProfile(
        val ratio: Double,
        val label: String,
        val keywords: List<String>,
    )

    private val ESTIMATED_PROFILES = listOf(
        CategoryProfile(
            ratio = 1.00,
            label = "Water",
            keywords = listOf(
                "water", "still water", "mineral water", "sparkling water", "seltzer",
                "club soda", "вода", "минеральная вода", "минералка", "газированная вода"
            ),
        ),
        CategoryProfile(
            ratio = 0.99,
            label = "Black Coffee",
            keywords = listOf(
                "black coffee", "americano", "filter coffee", "drip coffee",
                "cold brew", "brewed coffee", "lungo", "long black",
                "черный кофе", "американо", "фильтр-кофе", "колд брю"
            ),
        ),
        CategoryProfile(
            ratio = 0.96,
            label = "Espresso",
            keywords = listOf("espresso", "ristretto", "эспрессо", "ристретто"),
        ),
        CategoryProfile(
            ratio = 0.99,
            label = "Tea",
            keywords = listOf(
                "tea", "black tea", "green tea", "herbal tea", "earl grey", "chamomile",
                "peppermint", "rooibos", "oolong", "matcha", "mint tea", "hibiscus",
                "чай", "черный чай", "зеленый чай", "травяной чай", "ромашковый", "каркаде", "мятный чай"
            ),
        ),
        CategoryProfile(
            ratio = 0.99,
            label = "Diet Soda",
            keywords = listOf(
                "diet coke", "coke zero", "pepsi max", "diet pepsi", "sugar free soda",
                "zero sugar", "diet soda", "cola zero", "зеро", "диет", "без сахара"
            ),
        ),
        CategoryProfile(
            ratio = 0.89,
            label = "Soda & Lemonade",
            keywords = listOf(
                "coca-cola", "coke", "pepsi", "sprite", "fanta", "lemonade", "soda",
                "tonic water", "energy drink", "red bull", "monster energy",
                "газировка", "лимонад", "кола", "пепси", "энергетик", "тоник"
            ),
        ),
        CategoryProfile(
            ratio = 0.90,
            label = "Coffee with Milk",
            keywords = listOf(
                "cappuccino", "latte", "flat white", "macchiato", "cortado", "cafe au lait",
                "капучино", "латте", "флэт уайт", "кофе с молоком"
            ),
        ),
        CategoryProfile(
            ratio = 0.89,
            label = "Cow Milk",
            keywords = listOf(
                "milk", "whole milk", "skim milk", "low fat milk", "cow milk", "dairy milk",
                "молоко", "коровье молоко", "обезжиренное молоко", "цельное молоко"
            ),
        ),
        CategoryProfile(
            ratio = 0.91,
            label = "Plant Milk",
            keywords = listOf(
                "almond milk", "soy milk", "oat milk", "coconut milk drink", "rice milk", "cashew milk",
                "растительное молоко", "миндальное молоко", "соевое молоко", "овсяное молоко", "кокосовое молоко"
            ),
        ),
        CategoryProfile(
            ratio = 0.88,
            label = "Fruit Juice",
            keywords = listOf(
                "orange juice", "apple juice", "grapefruit juice", "grape juice", "pineapple juice",
                "fruit juice", "smoothie", "сок", "апельсиновый сок", "яблочный сок", "грейпфрутовый", "смузи"
            ),
        ),
        CategoryProfile(
            ratio = 0.93,
            label = "Vegetable Juice",
            keywords = listOf(
                "tomato juice", "vegetable juice", "celery juice", "carrot juice",
                "томатный сок", "овощной сок", "сельдереевый сок"
            ),
        ),
        CategoryProfile(
            ratio = 0.95,
            label = "Broth",
            keywords = listOf(
                "broth", "bouillon", "bone broth", "clear soup", "consomme",
                "бульон", "костный бульон", "мясной бульон", "куриный бульон"
            ),
        ),
        CategoryProfile(
            ratio = 0.85,
            label = "Soup",
            keywords = listOf(
                "soup", "noodle soup", "vegetable soup", "miso soup", "chicken soup", "borscht",
                "суп", "борщ", "щи", "мисо", "рамен", "лапша с бульоном", "окрошка", "солянка"
            ),
        ),
        CategoryProfile(
            ratio = 0.92,
            label = "Beer & Cider",
            keywords = listOf(
                "beer", "lager", "ale", "pilsner", "stout", "ipa", "cider",
                "пиво", "сидр", "лагер", "эль", "светлое пиво", "темное пиво"
            ),
        ),
        CategoryProfile(
            ratio = 0.86,
            label = "Wine",
            keywords = listOf(
                "wine", "red wine", "white wine", "rose wine", "champagne", "prosecco", "sparkling wine",
                "вино", "красное вино", "белое вино", "розовое вино", "шампанское", "просекко", "брют"
            ),
        ),
        CategoryProfile(
            ratio = 0.60,
            label = "Spirits",
            keywords = listOf(
                "vodka", "whiskey", "whisky", "gin", "rum", "tequila", "brandy", "cognac", "bourbon",
                "водка", "виски", "джин", "ром", "текила", "коньяк", "бренди", "бурбон"
            ),
        ),
        CategoryProfile(
            ratio = 0.78,
            label = "Cocktail",
            keywords = listOf(
                "cocktail", "mojito", "margarita", "martini", "spritz", "gin and tonic", "gin & tonic",
                "aperol", "long island", "negroni", "sangria", "коктейль", "мохито", "маргарита", "апероль", "спритц"
            ),
        ),
        CategoryProfile(
            ratio = 0.92,
            label = "Watermelon",
            keywords = listOf("watermelon", "арбуз"),
        ),
        CategoryProfile(
            ratio = 0.95,
            label = "Cucumber",
            keywords = listOf("cucumber", "огурец", "огурцы"),
        ),
        CategoryProfile(
            ratio = 0.90,
            label = "Melon",
            keywords = listOf("melon", "cantaloupe", "дыня"),
        ),
        CategoryProfile(
            ratio = 0.91,
            label = "Berries",
            keywords = listOf("strawberry", "strawberries", "клубника", "земляника"),
        ),
        CategoryProfile(
            ratio = 0.94,
            label = "Tomato",
            keywords = listOf("tomato", "tomatoes", "помидор", "томат", "помидоры"),
        ),
    )

    /**
     * Resolves the consumed mass in grams from a [FoodEntry] or [FoodConstituent].
     */
    fun resolveConsumedGrams(servingSizeGrams: Double?, unit: String?, quantity: Double?): Double {
        if (servingSizeGrams != null && servingSizeGrams > 0.0) {
            return servingSizeGrams
        }
        val q = quantity?.takeIf { it > 0.0 } ?: 1.0
        val u = unit?.trim()?.lowercase(Locale.ROOT) ?: return 100.0
        return when (u) {
            "g", "grams", "г", "грамм", "ml", "мл" -> q
            "l", "л", "литр", "liter" -> q * 1000.0
            "fl oz", "fl. oz", "floz" -> q * 29.5735
            "cup", "cups", "стакан", "чашка" -> q * 240.0
            "can", "банка" -> q * 330.0
            "glass", "бокал" -> q * 250.0
            "pint", "пинта" -> q * 473.0
            "bottle", "бутылка" -> q * 500.0
            "tbsp", "ст. л." -> q * 15.0
            "tsp", "ч. л." -> q * 5.0
            else -> 100.0 * q
        }
    }

    private fun matchCategory(name: String): CategoryProfile? {
        val lower = name.trim().lowercase(Locale.ROOT)
        return ESTIMATED_PROFILES.firstOrNull { profile ->
            profile.keywords.any { kw ->
                if (lower == kw) true
                else {
                    val regex = Regex("(?:^|[^\\p{L}\\p{N}])${Regex.escape(kw)}(?:$|[^\\p{L}\\p{N}])")
                    regex.containsMatchIn(lower)
                }
            }
        }
    }

    /**
     * Calculates hydration contribution for a single constituent.
     */
    fun calculateConstituent(c: FoodConstituent): ConstituentHydration? {
        val grams = c.servingSizeGrams.takeIf { it > 0.0 }
            ?: resolveConsumedGrams(c.servingSizeGrams, c.selectedServingUnit, c.selectedServingQuantity)

        if (c.waterPer100g != null) {
            val percentage = c.waterPer100g.coerceIn(0.0, 100.0)
            val waterMl = (grams * percentage) / 100.0
            return ConstituentHydration(
                name = c.name,
                portionGrams = grams,
                waterMl = waterMl,
                waterPercentage = percentage,
                isExact = true,
                categoryLabel = null,
            )
        }

        val category = matchCategory(c.name) ?: return null
        val percentage = category.ratio * 100.0
        val waterMl = grams * category.ratio
        return ConstituentHydration(
            name = c.name,
            portionGrams = grams,
            waterMl = waterMl,
            waterPercentage = percentage,
            isExact = false,
            categoryLabel = category.label,
        )
    }

    /**
     * Calculates hydration contribution for a [FoodEntry].
     *
     * If the entry has constituents, constituent water is calculated and summed,
     * ensuring no double-counting with the parent record.
     */
    fun calculateEntry(entry: FoodEntry): ItemHydration? {
        // Case 1: Composite entry with constituents
        if (entry.constituents.isNotEmpty()) {
            val constituentResults = entry.constituents.mapNotNull { calculateConstituent(it) }
            if (constituentResults.isEmpty()) return null
            val totalWaterMl = constituentResults.sumOf { it.waterMl }
            if (totalWaterMl <= 0.0) return null
            val totalGrams = constituentResults.sumOf { it.portionGrams }
            val avgPercentage = if (totalGrams > 0.0) (totalWaterMl / totalGrams) * 100.0 else 0.0
            val allExact = constituentResults.all { it.isExact }
            return ItemHydration(
                foodId = entry.id,
                foodName = entry.name,
                portionGrams = totalGrams,
                waterMl = totalWaterMl,
                waterPercentage = avgPercentage,
                isExact = allExact,
                categoryLabel = if (allExact) null else "Composite",
                constituents = constituentResults,
            )
        }

        // Case 2: Indivisible food with explicit waterPer100g from database/label
        val grams = resolveConsumedGrams(entry.servingSizeGrams, entry.selectedServingUnit, entry.selectedServingQuantity)
        if (entry.waterPer100g != null) {
            val percentage = entry.waterPer100g.coerceIn(0.0, 100.0)
            val waterMl = (grams * percentage) / 100.0
            return ItemHydration(
                foodId = entry.id,
                foodName = entry.name,
                portionGrams = grams,
                waterMl = waterMl,
                waterPercentage = percentage,
                isExact = true,
                categoryLabel = null,
            )
        }

        // Case 3: Beverage or hydrating food category estimation
        val category = matchCategory(entry.name) ?: return null
        val percentage = category.ratio * 100.0
        val waterMl = grams * category.ratio
        return ItemHydration(
            foodId = entry.id,
            foodName = entry.name,
            portionGrams = grams,
            waterMl = waterMl,
            waterPercentage = percentage,
            isExact = false,
            categoryLabel = category.label,
        )
    }

    /**
     * Aggregates hydration from all food and beverage entries for a given day.
     */
    fun calculate(entries: List<FoodEntry>): FoodWaterSummary {
        if (entries.isEmpty()) return EmptySummary

        val items = mutableListOf<ItemHydration>()
        var totalMeasuredMl = 0.0
        var totalEstimatedMl = 0.0

        for (entry in entries) {
            val item = calculateEntry(entry) ?: continue
            if (item.waterMl <= 0.0) continue
            items.add(item)
            if (item.isExact) {
                totalMeasuredMl += item.waterMl
            } else {
                totalEstimatedMl += item.waterMl
            }
        }

        val totalMl = (totalMeasuredMl + totalEstimatedMl).roundToInt()
        return FoodWaterSummary(
            totalMl = totalMl,
            measuredMl = totalMeasuredMl.roundToInt(),
            estimatedMl = totalEstimatedMl.roundToInt(),
            items = items,
        )
    }
}
