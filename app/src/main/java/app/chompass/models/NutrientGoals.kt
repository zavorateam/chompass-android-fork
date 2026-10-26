package app.chompass.models

import app.chompass.R

import kotlinx.serialization.Serializable
import kotlin.math.roundToInt

enum class HomeTopNutrient(
    val storageKey: String,
    val displayName: String,
    val unit: String,
    val displayNameRes: Int,
    val unitRes: Int
) {
    CALORIES("calories", "Calories", "kcal", R.string.nutrition_label_calories, R.string.unit_kcal),
    PROTEIN("protein", "Protein", "g", R.string.nutrition_label_protein, R.string.unit_g),
    CARBS("carbs", "Carbs", "g", R.string.nutrition_label_carbs, R.string.unit_g),
    FAT("fat", "Fat", "g", R.string.nutrition_label_fat, R.string.unit_g),
    FIBER("fiber", "Fiber", "g", R.string.nutrition_label_fiber, R.string.unit_g),
    SUGAR("sugar", "Sugar", "g", R.string.nutrition_label_sugar, R.string.unit_g),
    ADDED_SUGAR("addedSugar", "Added Sugar", "g", R.string.nutrition_label_added_sugar, R.string.unit_g),
    SATURATED_FAT("saturatedFat", "Sat Fat", "g", R.string.nutrient_short_sat_fat, R.string.unit_g),
    CHOLESTEROL("cholesterol", "Cholesterol", "mg", R.string.nutrition_label_cholesterol, R.string.unit_mg),
    SODIUM("sodium", "Sodium", "mg", R.string.nutrition_label_sodium, R.string.unit_mg),
    POTASSIUM("potassium", "Potassium", "mg", R.string.nutrition_label_potassium, R.string.unit_mg),
    TRANS_FAT("transFat", "Trans Fat", "g", R.string.nutrition_label_trans_fat, R.string.unit_g),
    CALCIUM("calcium", "Calcium", "mg", R.string.nutrition_label_calcium, R.string.unit_mg),
    IRON("iron", "Iron", "mg", R.string.nutrition_label_iron, R.string.unit_mg),
    MAGNESIUM("magnesium", "Magnesium", "mg", R.string.nutrition_label_magnesium, R.string.unit_mg),
    ZINC("zinc", "Zinc", "mg", R.string.nutrition_label_zinc, R.string.unit_mg),
    VITAMIN_A("vitaminA", "Vit A", "mcg", R.string.nutrient_short_vit_a, R.string.unit_mcg),
    VITAMIN_C("vitaminC", "Vit C", "mg", R.string.nutrient_short_vit_c, R.string.unit_mg),
    VITAMIN_D("vitaminD", "Vit D", "mcg", R.string.nutrient_short_vit_d, R.string.unit_mcg),
    VITAMIN_B12("vitaminB12", "B12", "mcg", R.string.nutrient_short_b12, R.string.unit_mcg),
    VITAMIN_E("vitaminE", "Vit E", "mg", R.string.nutrient_short_vit_e, R.string.unit_mg),
    VITAMIN_K("vitaminK", "Vit K", "mcg", R.string.nutrient_short_vit_k, R.string.unit_mcg),
    FOLATE("folate", "Folate", "mcg", R.string.nutrition_label_folate, R.string.unit_mcg),
    OMEGA3("omega3", "Omega-3", "g", R.string.nutrient_short_omega, R.string.unit_g),
    CAFFEINE("caffeine", "Caffeine", "mg", R.string.nutrition_label_caffeine, R.string.unit_mg),
    HISTIDINE("histidine", "Histidine", "g", R.string.nutrition_label_histidine, R.string.unit_g),
    ISOLEUCINE("isoleucine", "Isoleucine", "g", R.string.nutrition_label_isoleucine, R.string.unit_g),
    LEUCINE("leucine", "Leucine", "g", R.string.nutrition_label_leucine, R.string.unit_g),
    LYSINE("lysine", "Lysine", "g", R.string.nutrition_label_lysine, R.string.unit_g),
    METHIONINE("methionine", "Methionine", "g", R.string.nutrition_label_methionine, R.string.unit_g),
    PHENYLALANINE("phenylalanine", "Phenylalanine", "g", R.string.nutrition_label_phenylalanine, R.string.unit_g),
    THREONINE("threonine", "Threonine", "g", R.string.nutrition_label_threonine, R.string.unit_g),
    TRYPTOPHAN("tryptophan", "Tryptophan", "g", R.string.nutrition_label_tryptophan, R.string.unit_g),
    VALINE("valine", "Valine", "g", R.string.nutrition_label_valine, R.string.unit_g),
    VITAMIN_B1("vitaminB1", "Vit B1", "mg", R.string.nutrient_short_b1, R.string.unit_mg),
    VITAMIN_B2("vitaminB2", "Vit B2", "mg", R.string.nutrient_short_b2, R.string.unit_mg),
    VITAMIN_B3("vitaminB3", "Vit B3", "mg", R.string.nutrient_short_b3, R.string.unit_mg),
    VITAMIN_B5("vitaminB5", "Vit B5", "mg", R.string.nutrient_short_b5, R.string.unit_mg),
    VITAMIN_B6("vitaminB6", "Vit B6", "mg", R.string.nutrient_short_b6, R.string.unit_mg),
    BIOTIN("biotin", "Biotin", "mcg", R.string.nutrition_label_biotin, R.string.unit_mcg),
    CHOLINE("choline", "Choline", "mg", R.string.nutrition_label_choline, R.string.unit_mg),
    PHOSPHORUS("phosphorus", "Phosphorus", "mg", R.string.nutrition_label_phosphorus, R.string.unit_mg),
    SELENIUM("selenium", "Selenium", "mcg", R.string.nutrition_label_selenium, R.string.unit_mcg),
    COPPER("copper", "Copper", "mg", R.string.nutrition_label_copper, R.string.unit_mg),
    MANGANESE("manganese", "Manganese", "mg", R.string.nutrition_label_manganese, R.string.unit_mg);

    /** Band-card label: the localized macro glyph for core nutrients, else the localized short name. */
    fun cardShortLabelRes(): Int = when (this) {
        CALORIES -> R.string.nutrient_short_calories
        PROTEIN -> R.string.macro_glyph_protein
        CARBS -> R.string.macro_glyph_carbs
        FAT -> R.string.macro_glyph_fat
        FIBER -> R.string.macro_glyph_fiber
        SUGAR -> R.string.macro_glyph_sugar
        ADDED_SUGAR -> R.string.nutrient_short_added_sugar
        SATURATED_FAT -> R.string.nutrient_short_sat_fat
        CHOLESTEROL -> R.string.nutrient_short_cholesterol
        SODIUM -> R.string.nutrient_short_sodium
        POTASSIUM -> R.string.nutrient_short_potassium
        TRANS_FAT -> R.string.nutrient_short_trans_fat
        CALCIUM -> R.string.nutrient_short_calcium
        IRON -> R.string.nutrient_short_iron
        MAGNESIUM -> R.string.nutrient_short_magnesium
        ZINC -> R.string.nutrient_short_zinc
        VITAMIN_A -> R.string.nutrient_short_vit_a
        VITAMIN_C -> R.string.nutrient_short_vit_c
        VITAMIN_D -> R.string.nutrient_short_vit_d
        VITAMIN_B12 -> R.string.nutrient_short_b12
        VITAMIN_E -> R.string.nutrient_short_vit_e
        VITAMIN_K -> R.string.nutrient_short_vit_k
        FOLATE -> R.string.nutrient_short_folate
        OMEGA3 -> R.string.nutrient_short_omega
        CAFFEINE -> R.string.nutrient_short_caffeine
        else -> displayNameRes
    }

    fun current(entries: List<FoodEntry>): Double = when (this) {
        CALORIES -> entries.sumOf { it.calories }.toDouble()
        PROTEIN -> entries.sumOf { it.protein }
        CARBS -> entries.sumOf { it.carbs }
        FAT -> entries.sumOf { it.fat }
        FIBER -> entries.sumOf { it.fiber ?: 0.0 }
        SUGAR -> entries.sumOf { it.sugar ?: 0.0 }
        ADDED_SUGAR -> entries.sumOf { it.addedSugar ?: 0.0 }
        SATURATED_FAT -> entries.sumOf { it.saturatedFat ?: 0.0 }
        CHOLESTEROL -> entries.sumOf { it.cholesterol ?: 0.0 }
        SODIUM -> entries.sumOf { it.sodium ?: 0.0 }
        POTASSIUM -> entries.sumOf { it.potassium ?: 0.0 }
        TRANS_FAT -> entries.sumOf { it.transFat ?: 0.0 }
        CALCIUM -> entries.sumOf { it.calcium ?: 0.0 }
        IRON -> entries.sumOf { it.iron ?: 0.0 }
        MAGNESIUM -> entries.sumOf { it.magnesium ?: 0.0 }
        ZINC -> entries.sumOf { it.zinc ?: 0.0 }
        VITAMIN_A -> entries.sumOf { it.vitaminA ?: 0.0 }
        VITAMIN_C -> entries.sumOf { it.vitaminC ?: 0.0 }
        VITAMIN_D -> entries.sumOf { it.vitaminD ?: 0.0 }
        VITAMIN_B12 -> entries.sumOf { it.vitaminB12 ?: 0.0 }
        VITAMIN_E -> entries.sumOf { it.vitaminE ?: 0.0 }
        VITAMIN_K -> entries.sumOf { it.vitaminK ?: 0.0 }
        FOLATE -> entries.sumOf { it.folate ?: 0.0 }
        OMEGA3 -> entries.sumOf { it.omega3 ?: 0.0 }
        CAFFEINE -> entries.sumOf { it.caffeine ?: 0.0 }
        HISTIDINE -> entries.sumOf { it.histidine ?: 0.0 }
        ISOLEUCINE -> entries.sumOf { it.isoleucine ?: 0.0 }
        LEUCINE -> entries.sumOf { it.leucine ?: 0.0 }
        LYSINE -> entries.sumOf { it.lysine ?: 0.0 }
        METHIONINE -> entries.sumOf { it.methionine ?: 0.0 }
        PHENYLALANINE -> entries.sumOf { it.phenylalanine ?: 0.0 }
        THREONINE -> entries.sumOf { it.threonine ?: 0.0 }
        TRYPTOPHAN -> entries.sumOf { it.tryptophan ?: 0.0 }
        VALINE -> entries.sumOf { it.valine ?: 0.0 }
        VITAMIN_B1 -> entries.sumOf { it.vitaminB1 ?: 0.0 }
        VITAMIN_B2 -> entries.sumOf { it.vitaminB2 ?: 0.0 }
        VITAMIN_B3 -> entries.sumOf { it.vitaminB3 ?: 0.0 }
        VITAMIN_B5 -> entries.sumOf { it.vitaminB5 ?: 0.0 }
        VITAMIN_B6 -> entries.sumOf { it.vitaminB6 ?: 0.0 }
        BIOTIN -> entries.sumOf { it.biotin ?: 0.0 }
        CHOLINE -> entries.sumOf { it.choline ?: 0.0 }
        PHOSPHORUS -> entries.sumOf { it.phosphorus ?: 0.0 }
        SELENIUM -> entries.sumOf { it.selenium ?: 0.0 }
        COPPER -> entries.sumOf { it.copper ?: 0.0 }
        MANGANESE -> entries.sumOf { it.manganese ?: 0.0 }
    }

    /**
     * P/C/F goals scale with the hero ring's projected day when ADD_ACTIVE
     * pushes the displayed calorie target above the stored base (#38); the
     * optional micronutrient goals are fixed daily targets and never scale.
     */
    fun goal(profile: UserProfile?, optionalGoals: OptionalNutrientGoals, macroScale: Float = 1f): Int = when (this) {
        CALORIES -> profile?.effectiveCalories ?: 2_000
        PROTEIN -> scaleMacro(profile?.effectiveProtein ?: 150, macroScale)
        CARBS -> scaleMacro(profile?.effectiveCarbs ?: 220, macroScale)
        FAT -> scaleMacro(profile?.effectiveFat ?: 70, macroScale)
        FIBER -> optionalGoals.fiber
        SUGAR -> optionalGoals.sugar
        ADDED_SUGAR -> optionalGoals.addedSugar
        SATURATED_FAT -> optionalGoals.saturatedFat
        CHOLESTEROL -> optionalGoals.cholesterol
        SODIUM -> optionalGoals.sodium
        POTASSIUM -> optionalGoals.potassium
        TRANS_FAT -> optionalGoals.transFat
        CALCIUM -> optionalGoals.calcium
        IRON -> optionalGoals.iron
        MAGNESIUM -> optionalGoals.magnesium
        ZINC -> optionalGoals.zinc
        VITAMIN_A -> optionalGoals.vitaminA
        VITAMIN_C -> optionalGoals.vitaminC
        VITAMIN_D -> optionalGoals.vitaminD
        VITAMIN_B12 -> optionalGoals.vitaminB12
        VITAMIN_E -> optionalGoals.vitaminE
        VITAMIN_K -> optionalGoals.vitaminK
        FOLATE -> optionalGoals.folate
        OMEGA3 -> optionalGoals.omega3
        CAFFEINE -> optionalGoals.caffeine
        HISTIDINE -> optionalGoals.histidine
        ISOLEUCINE -> optionalGoals.isoleucine
        LEUCINE -> optionalGoals.leucine
        LYSINE -> optionalGoals.lysine
        METHIONINE -> optionalGoals.methionine
        PHENYLALANINE -> optionalGoals.phenylalanine
        THREONINE -> optionalGoals.threonine
        TRYPTOPHAN -> optionalGoals.tryptophan
        VALINE -> optionalGoals.valine
        VITAMIN_B1 -> optionalGoals.vitaminB1
        VITAMIN_B2 -> optionalGoals.vitaminB2
        VITAMIN_B3 -> optionalGoals.vitaminB3
        VITAMIN_B5 -> optionalGoals.vitaminB5
        VITAMIN_B6 -> optionalGoals.vitaminB6
        BIOTIN -> optionalGoals.biotin
        CHOLINE -> optionalGoals.choline
        PHOSPHORUS -> optionalGoals.phosphorus
        SELENIUM -> optionalGoals.selenium
        COPPER -> optionalGoals.copper
        MANGANESE -> optionalGoals.manganese
    }

    /**
     * Macro day types (#60 phase 1): P/C/F goals come from the day-type plan's
     * resolved targets for the viewed day ([MacroPlanResolver] output); a null
     * [resolved] or a base fallback keeps the profile set. Optional
     * micronutrient goals are unchanged fixed globals.
     */
    fun goal(
        resolved: ResolvedDayTargets?,
        profile: UserProfile?,
        optionalGoals: OptionalNutrientGoals,
        macroScale: Float = 1f,
    ): Int = when (this) {
        CALORIES -> resolved?.targets?.calories ?: profile?.effectiveCalories ?: 2_000
        PROTEIN -> scaleMacro(resolved?.targets?.proteinG ?: profile?.effectiveProtein ?: 150, macroScale)
        CARBS -> scaleMacro(resolved?.targets?.carbsG ?: profile?.effectiveCarbs ?: 220, macroScale)
        FAT -> scaleMacro(resolved?.targets?.fatG ?: profile?.effectiveFat ?: 70, macroScale)
        else -> goal(profile, optionalGoals, macroScale)
    }

    private fun scaleMacro(grams: Int, scale: Float): Int =
        if (scale > 1f && grams > 0) (grams * scale).roundToInt() else grams

    companion object {
        /** #75: P/C/F — never part of the Progress averages selection (the macro card covers them). */
        val Macros = setOf(CALORIES, PROTEIN, CARBS, FAT)

        /**
         * #75: nutrients the Progress averages card can show, in canonical
         * declaration order — every goal-bearing non-macro nutrient.
         */
        val AveragesCandidates: List<HomeTopNutrient> = entries.filterNot { it in Macros }

        /** The original trio: pref unset renders exactly the pre-#75 rows. */
        val DefaultAverages: Set<HomeTopNutrient> = setOf(FIBER, SUGAR, SODIUM)
        val DefaultAveragesStorage: Set<String> = DefaultAverages.map { it.storageKey }.toSet()

        /**
         * Normalizes a stored/raw key set for the averages selection: known
         * non-macro storage keys (or enum names) only. The empty set is kept —
         * master toggle on with nothing selected hides the card.
         */
        fun normalizeAveragesSelectionStorage(raw: Iterable<String>): Set<String> =
            raw.mapNotNull { key ->
                values().firstOrNull { it.storageKey == key || it.name == key }
            }
                .filter { it !in Macros }
                .map { it.storageKey }
                .toSet()

        /** Raw set → canonical-order non-macro selection. */
        fun averagesSelectionFromStorage(raw: Collection<String>): List<HomeTopNutrient> {
            val keys = normalizeAveragesSelectionStorage(raw)
            return AveragesCandidates.filter { it.storageKey in keys }
        }

        val DefaultSelection = listOf(PROTEIN, CARBS, FAT, FIBER)
        val DefaultStorageValue = DefaultSelection.joinToString(",") { it.storageKey }

        fun fromStorage(raw: String?): List<HomeTopNutrient> {
            if (raw.isNullOrBlank()) return DefaultSelection
            val selected = raw.split(",").mapNotNull { part ->
                val key = part.trim()
                values().firstOrNull { it.storageKey == key || it.name == key }
            }
            return normalized(selected)
        }

        fun toStorage(selection: List<HomeTopNutrient>): String =
            normalized(selection).joinToString(",") { it.storageKey }

        /** Distinct picks, 1 to [HomeDisplayPreferences.MAX_NUTRIENT_CARD_COUNT]. Empty falls back to the default row. */
        fun normalized(selection: List<HomeTopNutrient>): List<HomeTopNutrient> {
            val picked = selection.distinct().take(HomeDisplayPreferences.MAX_NUTRIENT_CARD_COUNT)
            return picked.ifEmpty { DefaultSelection }
        }
    }
}

enum class OptionalNutrient(
    val displayName: String,
    val unit: String,
    val defaultGoal: Int,
    val displayNameRes: Int,
    val unitRes: Int,
    /** Full settable envelope and increment for the goal wheel picker in
     *  Settings; tap-to-type enters exact values without spinning. */
    val goalRange: IntRange,
    val goalStep: Int,
    /** Saving above this line asks for confirmation — usually a tolerable
     *  upper intake, not a hard stop. */
    val softMax: Int
) {
    SUGAR("Sugar", "g", 50, R.string.nutrition_label_sugar, R.string.unit_g, 0..500, 5, 200),
    ADDED_SUGAR("Added Sugar", "g", 25, R.string.nutrition_label_added_sugar, R.string.unit_g, 0..300, 5, 100),
    FIBER("Fiber", "g", 30, R.string.nutrition_label_fiber, R.string.unit_g, 0..300, 1, 100),
    SATURATED_FAT("Saturated Fat", "g", 20, R.string.nutrition_label_saturated_fat, R.string.unit_g, 0..200, 1, 80),
    CHOLESTEROL("Cholesterol", "mg", 300, R.string.nutrition_label_cholesterol, R.string.unit_mg, 0..2000, 25, 1000),
    SODIUM("Sodium", "mg", 2300, R.string.nutrition_label_sodium, R.string.unit_mg, 0..10000, 50, 5000),
    POTASSIUM("Potassium", "mg", 3500, R.string.nutrition_label_potassium, R.string.unit_mg, 0..15000, 50, 7000),
    TRANS_FAT("Trans Fat", "g", 0, R.string.nutrition_label_trans_fat, R.string.unit_g, 0..50, 1, 10),
    CALCIUM("Calcium", "mg", 1000, R.string.nutrition_label_calcium, R.string.unit_mg, 0..5000, 50, 2000),
    IRON("Iron", "mg", 18, R.string.nutrition_label_iron, R.string.unit_mg, 0..200, 1, 45),
    MAGNESIUM("Magnesium", "mg", 400, R.string.nutrition_label_magnesium, R.string.unit_mg, 0..2000, 25, 800),
    ZINC("Zinc", "mg", 11, R.string.nutrition_label_zinc, R.string.unit_mg, 0..100, 1, 40),
    VITAMIN_A("Vitamin A", "mcg", 900, R.string.nutrition_label_vitamin_a, R.string.unit_mcg, 0..5000, 50, 3000),
    VITAMIN_C("Vitamin C", "mg", 90, R.string.nutrition_label_vitamin_c, R.string.unit_mg, 0..2000, 10, 500),
    VITAMIN_D("Vitamin D", "mcg", 20, R.string.nutrition_label_vitamin_d, R.string.unit_mcg, 0..500, 1, 100),
    VITAMIN_B12("Vitamin B12", "mcg", 3, R.string.nutrition_label_vitamin_b12, R.string.unit_mcg, 0..100, 1, 20),
    VITAMIN_E("Vitamin E", "mg", 15, R.string.nutrition_label_vitamin_e, R.string.unit_mg, 0..1000, 1, 100),
    VITAMIN_K("Vitamin K", "mcg", 120, R.string.nutrition_label_vitamin_k, R.string.unit_mcg, 0..1000, 10, 300),
    FOLATE("Folate", "mcg", 400, R.string.nutrition_label_folate, R.string.unit_mcg, 0..2000, 50, 1000),
    OMEGA3("Omega-3", "g", 2, R.string.nutrition_label_omega3, R.string.unit_g, 0..50, 1, 10),
    CAFFEINE("Caffeine", "mg", 400, R.string.nutrition_label_caffeine, R.string.unit_mg, 0..2000, 25, 1000),
    HISTIDINE("Histidine", "g", 1, R.string.nutrition_label_histidine, R.string.unit_g, 0..20, 1, 10),
    ISOLEUCINE("Isoleucine", "g", 2, R.string.nutrition_label_isoleucine, R.string.unit_g, 0..30, 1, 15),
    LEUCINE("Leucine", "g", 3, R.string.nutrition_label_leucine, R.string.unit_g, 0..40, 1, 20),
    LYSINE("Lysine", "g", 3, R.string.nutrition_label_lysine, R.string.unit_g, 0..30, 1, 15),
    METHIONINE("Methionine", "g", 1, R.string.nutrition_label_methionine, R.string.unit_g, 0..20, 1, 10),
    PHENYLALANINE("Phenylalanine", "g", 2, R.string.nutrition_label_phenylalanine, R.string.unit_g, 0..30, 1, 15),
    THREONINE("Threonine", "g", 1, R.string.nutrition_label_threonine, R.string.unit_g, 0..20, 1, 10),
    TRYPTOPHAN("Tryptophan", "g", 1, R.string.nutrition_label_tryptophan, R.string.unit_g, 0..10, 1, 5),
    VALINE("Valine", "g", 2, R.string.nutrition_label_valine, R.string.unit_g, 0..30, 1, 15),
    VITAMIN_B1("Vitamin B1", "mg", 1, R.string.nutrition_label_vitamin_b1, R.string.unit_mg, 0..100, 1, 50),
    VITAMIN_B2("Vitamin B2", "mg", 1, R.string.nutrition_label_vitamin_b2, R.string.unit_mg, 0..100, 1, 50),
    VITAMIN_B3("Vitamin B3", "mg", 16, R.string.nutrition_label_vitamin_b3, R.string.unit_mg, 0..200, 1, 100),
    VITAMIN_B5("Vitamin B5", "mg", 5, R.string.nutrition_label_vitamin_b5, R.string.unit_mg, 0..100, 1, 50),
    VITAMIN_B6("Vitamin B6", "mg", 2, R.string.nutrition_label_vitamin_b6, R.string.unit_mg, 0..100, 1, 50),
    BIOTIN("Biotin", "mcg", 30, R.string.nutrition_label_biotin, R.string.unit_mcg, 0..500, 5, 200),
    CHOLINE("Choline", "mg", 550, R.string.nutrition_label_choline, R.string.unit_mg, 0..3500, 25, 2000),
    PHOSPHORUS("Phosphorus", "mg", 700, R.string.nutrition_label_phosphorus, R.string.unit_mg, 0..4000, 50, 3000),
    SELENIUM("Selenium", "mcg", 55, R.string.nutrition_label_selenium, R.string.unit_mcg, 0..400, 5, 200),
    COPPER("Copper", "mg", 1, R.string.nutrition_label_copper, R.string.unit_mg, 0..10, 1, 5),
    MANGANESE("Manganese", "mg", 2, R.string.nutrition_label_manganese, R.string.unit_mg, 0..20, 1, 10)
}

@Serializable
data class OptionalNutrientGoals(
    val sugar: Int = OptionalNutrient.SUGAR.defaultGoal,
    val addedSugar: Int = OptionalNutrient.ADDED_SUGAR.defaultGoal,
    val fiber: Int = OptionalNutrient.FIBER.defaultGoal,
    val saturatedFat: Int = OptionalNutrient.SATURATED_FAT.defaultGoal,
    val cholesterol: Int = OptionalNutrient.CHOLESTEROL.defaultGoal,
    val sodium: Int = OptionalNutrient.SODIUM.defaultGoal,
    val potassium: Int = OptionalNutrient.POTASSIUM.defaultGoal,
    val transFat: Int = OptionalNutrient.TRANS_FAT.defaultGoal,
    val calcium: Int = OptionalNutrient.CALCIUM.defaultGoal,
    val iron: Int = OptionalNutrient.IRON.defaultGoal,
    val magnesium: Int = OptionalNutrient.MAGNESIUM.defaultGoal,
    val zinc: Int = OptionalNutrient.ZINC.defaultGoal,
    val vitaminA: Int = OptionalNutrient.VITAMIN_A.defaultGoal,
    val vitaminC: Int = OptionalNutrient.VITAMIN_C.defaultGoal,
    val vitaminD: Int = OptionalNutrient.VITAMIN_D.defaultGoal,
    val vitaminB12: Int = OptionalNutrient.VITAMIN_B12.defaultGoal,
    val vitaminE: Int = OptionalNutrient.VITAMIN_E.defaultGoal,
    val vitaminK: Int = OptionalNutrient.VITAMIN_K.defaultGoal,
    val folate: Int = OptionalNutrient.FOLATE.defaultGoal,
    val omega3: Int = OptionalNutrient.OMEGA3.defaultGoal,
    val caffeine: Int = OptionalNutrient.CAFFEINE.defaultGoal,
    val histidine: Int = OptionalNutrient.HISTIDINE.defaultGoal,
    val isoleucine: Int = OptionalNutrient.ISOLEUCINE.defaultGoal,
    val leucine: Int = OptionalNutrient.LEUCINE.defaultGoal,
    val lysine: Int = OptionalNutrient.LYSINE.defaultGoal,
    val methionine: Int = OptionalNutrient.METHIONINE.defaultGoal,
    val phenylalanine: Int = OptionalNutrient.PHENYLALANINE.defaultGoal,
    val threonine: Int = OptionalNutrient.THREONINE.defaultGoal,
    val tryptophan: Int = OptionalNutrient.TRYPTOPHAN.defaultGoal,
    val valine: Int = OptionalNutrient.VALINE.defaultGoal,
    val vitaminB1: Int = OptionalNutrient.VITAMIN_B1.defaultGoal,
    val vitaminB2: Int = OptionalNutrient.VITAMIN_B2.defaultGoal,
    val vitaminB3: Int = OptionalNutrient.VITAMIN_B3.defaultGoal,
    val vitaminB5: Int = OptionalNutrient.VITAMIN_B5.defaultGoal,
    val vitaminB6: Int = OptionalNutrient.VITAMIN_B6.defaultGoal,
    val biotin: Int = OptionalNutrient.BIOTIN.defaultGoal,
    val choline: Int = OptionalNutrient.CHOLINE.defaultGoal,
    val phosphorus: Int = OptionalNutrient.PHOSPHORUS.defaultGoal,
    val selenium: Int = OptionalNutrient.SELENIUM.defaultGoal,
    val copper: Int = OptionalNutrient.COPPER.defaultGoal,
    val manganese: Int = OptionalNutrient.MANGANESE.defaultGoal
) {
    fun valueFor(nutrient: OptionalNutrient): Int = when (nutrient) {
        OptionalNutrient.SUGAR -> sugar
        OptionalNutrient.ADDED_SUGAR -> addedSugar
        OptionalNutrient.FIBER -> fiber
        OptionalNutrient.SATURATED_FAT -> saturatedFat
        OptionalNutrient.CHOLESTEROL -> cholesterol
        OptionalNutrient.SODIUM -> sodium
        OptionalNutrient.POTASSIUM -> potassium
        OptionalNutrient.TRANS_FAT -> transFat
        OptionalNutrient.CALCIUM -> calcium
        OptionalNutrient.IRON -> iron
        OptionalNutrient.MAGNESIUM -> magnesium
        OptionalNutrient.ZINC -> zinc
        OptionalNutrient.VITAMIN_A -> vitaminA
        OptionalNutrient.VITAMIN_C -> vitaminC
        OptionalNutrient.VITAMIN_D -> vitaminD
        OptionalNutrient.VITAMIN_B12 -> vitaminB12
        OptionalNutrient.VITAMIN_E -> vitaminE
        OptionalNutrient.VITAMIN_K -> vitaminK
        OptionalNutrient.FOLATE -> folate
        OptionalNutrient.OMEGA3 -> omega3
        OptionalNutrient.CAFFEINE -> caffeine
        OptionalNutrient.HISTIDINE -> histidine
        OptionalNutrient.ISOLEUCINE -> isoleucine
        OptionalNutrient.LEUCINE -> leucine
        OptionalNutrient.LYSINE -> lysine
        OptionalNutrient.METHIONINE -> methionine
        OptionalNutrient.PHENYLALANINE -> phenylalanine
        OptionalNutrient.THREONINE -> threonine
        OptionalNutrient.TRYPTOPHAN -> tryptophan
        OptionalNutrient.VALINE -> valine
        OptionalNutrient.VITAMIN_B1 -> vitaminB1
        OptionalNutrient.VITAMIN_B2 -> vitaminB2
        OptionalNutrient.VITAMIN_B3 -> vitaminB3
        OptionalNutrient.VITAMIN_B5 -> vitaminB5
        OptionalNutrient.VITAMIN_B6 -> vitaminB6
        OptionalNutrient.BIOTIN -> biotin
        OptionalNutrient.CHOLINE -> choline
        OptionalNutrient.PHOSPHORUS -> phosphorus
        OptionalNutrient.SELENIUM -> selenium
        OptionalNutrient.COPPER -> copper
        OptionalNutrient.MANGANESE -> manganese
    }

    fun withValue(nutrient: OptionalNutrient, value: Int): OptionalNutrientGoals {
        val safe = value.coerceIn(0, nutrient.goalRange.last)
        return when (nutrient) {
            OptionalNutrient.SUGAR -> copy(sugar = safe)
            OptionalNutrient.ADDED_SUGAR -> copy(addedSugar = safe)
            OptionalNutrient.FIBER -> copy(fiber = safe)
            OptionalNutrient.SATURATED_FAT -> copy(saturatedFat = safe)
            OptionalNutrient.CHOLESTEROL -> copy(cholesterol = safe)
            OptionalNutrient.SODIUM -> copy(sodium = safe)
            OptionalNutrient.POTASSIUM -> copy(potassium = safe)
            OptionalNutrient.TRANS_FAT -> copy(transFat = safe)
            OptionalNutrient.CALCIUM -> copy(calcium = safe)
            OptionalNutrient.IRON -> copy(iron = safe)
            OptionalNutrient.MAGNESIUM -> copy(magnesium = safe)
            OptionalNutrient.ZINC -> copy(zinc = safe)
            OptionalNutrient.VITAMIN_A -> copy(vitaminA = safe)
            OptionalNutrient.VITAMIN_C -> copy(vitaminC = safe)
            OptionalNutrient.VITAMIN_D -> copy(vitaminD = safe)
            OptionalNutrient.VITAMIN_B12 -> copy(vitaminB12 = safe)
            OptionalNutrient.VITAMIN_E -> copy(vitaminE = safe)
            OptionalNutrient.VITAMIN_K -> copy(vitaminK = safe)
            OptionalNutrient.FOLATE -> copy(folate = safe)
            OptionalNutrient.OMEGA3 -> copy(omega3 = safe)
            OptionalNutrient.CAFFEINE -> copy(caffeine = safe)
            OptionalNutrient.HISTIDINE -> copy(histidine = safe)
            OptionalNutrient.ISOLEUCINE -> copy(isoleucine = safe)
            OptionalNutrient.LEUCINE -> copy(leucine = safe)
            OptionalNutrient.LYSINE -> copy(lysine = safe)
            OptionalNutrient.METHIONINE -> copy(methionine = safe)
            OptionalNutrient.PHENYLALANINE -> copy(phenylalanine = safe)
            OptionalNutrient.THREONINE -> copy(threonine = safe)
            OptionalNutrient.TRYPTOPHAN -> copy(tryptophan = safe)
            OptionalNutrient.VALINE -> copy(valine = safe)
            OptionalNutrient.VITAMIN_B1 -> copy(vitaminB1 = safe)
            OptionalNutrient.VITAMIN_B2 -> copy(vitaminB2 = safe)
            OptionalNutrient.VITAMIN_B3 -> copy(vitaminB3 = safe)
            OptionalNutrient.VITAMIN_B5 -> copy(vitaminB5 = safe)
            OptionalNutrient.VITAMIN_B6 -> copy(vitaminB6 = safe)
            OptionalNutrient.BIOTIN -> copy(biotin = safe)
            OptionalNutrient.CHOLINE -> copy(choline = safe)
            OptionalNutrient.PHOSPHORUS -> copy(phosphorus = safe)
            OptionalNutrient.SELENIUM -> copy(selenium = safe)
            OptionalNutrient.COPPER -> copy(copper = safe)
            OptionalNutrient.MANGANESE -> copy(manganese = safe)
        }
    }

    companion object {
        val Default = OptionalNutrientGoals()
    }
}
