package app.chompass.models

import app.chompass.R

/**
 * Single catalog for optional micronutrients shared by food review/edit sheets,
 * analysis ↔ entry mapping, and JSON parse keys.
 *
 * Keeps typed fields on the models (no Map-based diary wire format).
 */
enum class MicronutrientField(
    val labelRes: Int,
    val unitRes: Int,
    /** Snake_case key in food-analysis JSON. */
    val jsonKey: String,
    /** Snake_case key in nutrition-label (per-100g) JSON. */
    val jsonKeyPer100g: String,
) {
    SUGAR(R.string.sheet_micro_sugar, R.string.unit_g, "sugar", "sugar_per_100g"),
    ADDED_SUGAR(R.string.sheet_micro_added_sugar, R.string.unit_g, "added_sugar", "added_sugar_per_100g"),
    FIBER(R.string.nutrition_label_fiber, R.string.unit_g, "fiber", "fiber_per_100g"),
    SATURATED_FAT(R.string.sheet_micro_saturated_fat, R.string.unit_g, "saturated_fat", "saturated_fat_per_100g"),
    MONOUNSATURATED_FAT(R.string.sheet_micro_mono_fat, R.string.unit_g, "monounsaturated_fat", "monounsaturated_fat_per_100g"),
    POLYUNSATURATED_FAT(R.string.sheet_micro_poly_fat, R.string.unit_g, "polyunsaturated_fat", "polyunsaturated_fat_per_100g"),
    CHOLESTEROL(R.string.sheet_micro_cholesterol, R.string.unit_mg, "cholesterol", "cholesterol_per_100g"),
    SODIUM(R.string.sheet_micro_sodium, R.string.unit_mg, "sodium", "sodium_per_100g"),
    POTASSIUM(R.string.sheet_micro_potassium, R.string.unit_mg, "potassium", "potassium_per_100g"),
    TRANS_FAT(R.string.nutrition_label_trans_fat, R.string.unit_g, "trans_fat", "trans_fat_per_100g"),
    CALCIUM(R.string.nutrition_label_calcium, R.string.unit_mg, "calcium", "calcium_per_100g"),
    IRON(R.string.nutrition_label_iron, R.string.unit_mg, "iron", "iron_per_100g"),
    MAGNESIUM(R.string.nutrition_label_magnesium, R.string.unit_mg, "magnesium", "magnesium_per_100g"),
    ZINC(R.string.nutrition_label_zinc, R.string.unit_mg, "zinc", "zinc_per_100g"),
    VITAMIN_A(R.string.nutrition_label_vitamin_a, R.string.unit_mcg, "vitamin_a", "vitamin_a_per_100g"),
    VITAMIN_C(R.string.nutrition_label_vitamin_c, R.string.unit_mg, "vitamin_c", "vitamin_c_per_100g"),
    VITAMIN_D(R.string.nutrition_label_vitamin_d, R.string.unit_mcg, "vitamin_d", "vitamin_d_per_100g"),
    VITAMIN_B12(R.string.nutrition_label_vitamin_b12, R.string.unit_mcg, "vitamin_b12", "vitamin_b12_per_100g"),
    VITAMIN_E(R.string.nutrition_label_vitamin_e, R.string.unit_mg, "vitamin_e", "vitamin_e_per_100g"),
    VITAMIN_K(R.string.nutrition_label_vitamin_k, R.string.unit_mcg, "vitamin_k", "vitamin_k_per_100g"),
    FOLATE(R.string.nutrition_label_folate, R.string.unit_mcg, "folate", "folate_per_100g"),
    OMEGA3(R.string.nutrition_label_omega3, R.string.unit_g, "omega_3", "omega_3_per_100g"),
    CAFFEINE(R.string.nutrition_label_caffeine, R.string.unit_mg, "caffeine", "caffeine_per_100g"),
    HISTIDINE(R.string.nutrition_label_histidine, R.string.unit_g, "histidine", "histidine_per_100g"),
    ISOLEUCINE(R.string.nutrition_label_isoleucine, R.string.unit_g, "isoleucine", "isoleucine_per_100g"),
    LEUCINE(R.string.nutrition_label_leucine, R.string.unit_g, "leucine", "leucine_per_100g"),
    LYSINE(R.string.nutrition_label_lysine, R.string.unit_g, "lysine", "lysine_per_100g"),
    METHIONINE(R.string.nutrition_label_methionine, R.string.unit_g, "methionine", "methionine_per_100g"),
    PHENYLALANINE(R.string.nutrition_label_phenylalanine, R.string.unit_g, "phenylalanine", "phenylalanine_per_100g"),
    THREONINE(R.string.nutrition_label_threonine, R.string.unit_g, "threonine", "threonine_per_100g"),
    TRYPTOPHAN(R.string.nutrition_label_tryptophan, R.string.unit_g, "tryptophan", "tryptophan_per_100g"),
    VALINE(R.string.nutrition_label_valine, R.string.unit_g, "valine", "valine_per_100g"),
    VITAMIN_B1(R.string.nutrition_label_vitamin_b1, R.string.unit_mg, "vitamin_b1", "vitamin_b1_per_100g"),
    VITAMIN_B2(R.string.nutrition_label_vitamin_b2, R.string.unit_mg, "vitamin_b2", "vitamin_b2_per_100g"),
    VITAMIN_B3(R.string.nutrition_label_vitamin_b3, R.string.unit_mg, "vitamin_b3", "vitamin_b3_per_100g"),
    VITAMIN_B5(R.string.nutrition_label_vitamin_b5, R.string.unit_mg, "vitamin_b5", "vitamin_b5_per_100g"),
    VITAMIN_B6(R.string.nutrition_label_vitamin_b6, R.string.unit_mg, "vitamin_b6", "vitamin_b6_per_100g"),
    BIOTIN(R.string.nutrition_label_biotin, R.string.unit_mcg, "biotin", "biotin_per_100g"),
    CHOLINE(R.string.nutrition_label_choline, R.string.unit_mg, "choline", "choline_per_100g"),
    PHOSPHORUS(R.string.nutrition_label_phosphorus, R.string.unit_mg, "phosphorus", "phosphorus_per_100g"),
    SELENIUM(R.string.nutrition_label_selenium, R.string.unit_mcg, "selenium", "selenium_per_100g"),
    COPPER(R.string.nutrition_label_copper, R.string.unit_mg, "copper", "copper_per_100g"),
    MANGANESE(R.string.nutrition_label_manganese, R.string.unit_mg, "manganese", "manganese_per_100g");

    companion object {
        val AminoAcids: List<MicronutrientField> = listOf(
            HISTIDINE, ISOLEUCINE, LEUCINE, LYSINE, METHIONINE, PHENYLALANINE, THREONINE, TRYPTOPHAN, VALINE
        )
        val Bcaas: List<MicronutrientField> = listOf(LEUCINE, ISOLEUCINE, VALINE)

        /** Expandable "More Nutrition" section (fiber in macros card, amino acids in dedicated card). */
        val MoreNutrition: List<MicronutrientField> = entries.filter { it != FIBER && it !in AminoAcids }
    }
}

/** Snapshot of all optional micronutrient fields for edit UI and mapping. */
data class MicronutrientValues(
    val sugar: Double? = null,
    val addedSugar: Double? = null,
    val fiber: Double? = null,
    val saturatedFat: Double? = null,
    val monounsaturatedFat: Double? = null,
    val polyunsaturatedFat: Double? = null,
    val cholesterol: Double? = null,
    val sodium: Double? = null,
    val potassium: Double? = null,
    val transFat: Double? = null,
    val calcium: Double? = null,
    val iron: Double? = null,
    val magnesium: Double? = null,
    val zinc: Double? = null,
    val vitaminA: Double? = null,
    val vitaminC: Double? = null,
    val vitaminD: Double? = null,
    val vitaminB12: Double? = null,
    val vitaminE: Double? = null,
    val vitaminK: Double? = null,
    val folate: Double? = null,
    val omega3: Double? = null,
    val caffeine: Double? = null,
    val histidine: Double? = null,
    val isoleucine: Double? = null,
    val leucine: Double? = null,
    val lysine: Double? = null,
    val methionine: Double? = null,
    val phenylalanine: Double? = null,
    val threonine: Double? = null,
    val tryptophan: Double? = null,
    val valine: Double? = null,
    val vitaminB1: Double? = null,
    val vitaminB2: Double? = null,
    val vitaminB3: Double? = null,
    val vitaminB5: Double? = null,
    val vitaminB6: Double? = null,
    val biotin: Double? = null,
    val choline: Double? = null,
    val phosphorus: Double? = null,
    val selenium: Double? = null,
    val copper: Double? = null,
    val manganese: Double? = null,
) : java.io.Serializable {
    operator fun get(field: MicronutrientField): Double? = when (field) {
        MicronutrientField.SUGAR -> sugar
        MicronutrientField.ADDED_SUGAR -> addedSugar
        MicronutrientField.FIBER -> fiber
        MicronutrientField.SATURATED_FAT -> saturatedFat
        MicronutrientField.MONOUNSATURATED_FAT -> monounsaturatedFat
        MicronutrientField.POLYUNSATURATED_FAT -> polyunsaturatedFat
        MicronutrientField.CHOLESTEROL -> cholesterol
        MicronutrientField.SODIUM -> sodium
        MicronutrientField.POTASSIUM -> potassium
        MicronutrientField.TRANS_FAT -> transFat
        MicronutrientField.CALCIUM -> calcium
        MicronutrientField.IRON -> iron
        MicronutrientField.MAGNESIUM -> magnesium
        MicronutrientField.ZINC -> zinc
        MicronutrientField.VITAMIN_A -> vitaminA
        MicronutrientField.VITAMIN_C -> vitaminC
        MicronutrientField.VITAMIN_D -> vitaminD
        MicronutrientField.VITAMIN_B12 -> vitaminB12
        MicronutrientField.VITAMIN_E -> vitaminE
        MicronutrientField.VITAMIN_K -> vitaminK
        MicronutrientField.FOLATE -> folate
        MicronutrientField.OMEGA3 -> omega3
        MicronutrientField.CAFFEINE -> caffeine
        MicronutrientField.HISTIDINE -> histidine
        MicronutrientField.ISOLEUCINE -> isoleucine
        MicronutrientField.LEUCINE -> leucine
        MicronutrientField.LYSINE -> lysine
        MicronutrientField.METHIONINE -> methionine
        MicronutrientField.PHENYLALANINE -> phenylalanine
        MicronutrientField.THREONINE -> threonine
        MicronutrientField.TRYPTOPHAN -> tryptophan
        MicronutrientField.VALINE -> valine
        MicronutrientField.VITAMIN_B1 -> vitaminB1
        MicronutrientField.VITAMIN_B2 -> vitaminB2
        MicronutrientField.VITAMIN_B3 -> vitaminB3
        MicronutrientField.VITAMIN_B5 -> vitaminB5
        MicronutrientField.VITAMIN_B6 -> vitaminB6
        MicronutrientField.BIOTIN -> biotin
        MicronutrientField.CHOLINE -> choline
        MicronutrientField.PHOSPHORUS -> phosphorus
        MicronutrientField.SELENIUM -> selenium
        MicronutrientField.COPPER -> copper
        MicronutrientField.MANGANESE -> manganese
    }

    fun with(field: MicronutrientField, value: Double?): MicronutrientValues = when (field) {
        MicronutrientField.SUGAR -> copy(sugar = value)
        MicronutrientField.ADDED_SUGAR -> copy(addedSugar = value)
        MicronutrientField.FIBER -> copy(fiber = value)
        MicronutrientField.SATURATED_FAT -> copy(saturatedFat = value)
        MicronutrientField.MONOUNSATURATED_FAT -> copy(monounsaturatedFat = value)
        MicronutrientField.POLYUNSATURATED_FAT -> copy(polyunsaturatedFat = value)
        MicronutrientField.CHOLESTEROL -> copy(cholesterol = value)
        MicronutrientField.SODIUM -> copy(sodium = value)
        MicronutrientField.POTASSIUM -> copy(potassium = value)
        MicronutrientField.TRANS_FAT -> copy(transFat = value)
        MicronutrientField.CALCIUM -> copy(calcium = value)
        MicronutrientField.IRON -> copy(iron = value)
        MicronutrientField.MAGNESIUM -> copy(magnesium = value)
        MicronutrientField.ZINC -> copy(zinc = value)
        MicronutrientField.VITAMIN_A -> copy(vitaminA = value)
        MicronutrientField.VITAMIN_C -> copy(vitaminC = value)
        MicronutrientField.VITAMIN_D -> copy(vitaminD = value)
        MicronutrientField.VITAMIN_B12 -> copy(vitaminB12 = value)
        MicronutrientField.VITAMIN_E -> copy(vitaminE = value)
        MicronutrientField.VITAMIN_K -> copy(vitaminK = value)
        MicronutrientField.FOLATE -> copy(folate = value)
        MicronutrientField.OMEGA3 -> copy(omega3 = value)
        MicronutrientField.CAFFEINE -> copy(caffeine = value)
        MicronutrientField.HISTIDINE -> copy(histidine = value)
        MicronutrientField.ISOLEUCINE -> copy(isoleucine = value)
        MicronutrientField.LEUCINE -> copy(leucine = value)
        MicronutrientField.LYSINE -> copy(lysine = value)
        MicronutrientField.METHIONINE -> copy(methionine = value)
        MicronutrientField.PHENYLALANINE -> copy(phenylalanine = value)
        MicronutrientField.THREONINE -> copy(threonine = value)
        MicronutrientField.TRYPTOPHAN -> copy(tryptophan = value)
        MicronutrientField.VALINE -> copy(valine = value)
        MicronutrientField.VITAMIN_B1 -> copy(vitaminB1 = value)
        MicronutrientField.VITAMIN_B2 -> copy(vitaminB2 = value)
        MicronutrientField.VITAMIN_B3 -> copy(vitaminB3 = value)
        MicronutrientField.VITAMIN_B5 -> copy(vitaminB5 = value)
        MicronutrientField.VITAMIN_B6 -> copy(vitaminB6 = value)
        MicronutrientField.BIOTIN -> copy(biotin = value)
        MicronutrientField.CHOLINE -> copy(choline = value)
        MicronutrientField.PHOSPHORUS -> copy(phosphorus = value)
        MicronutrientField.SELENIUM -> copy(selenium = value)
        MicronutrientField.COPPER -> copy(copper = value)
        MicronutrientField.MANGANESE -> copy(manganese = value)
    }

    /** Scale every present value (1-decimal rounding matches sheet preview). */
    fun scaled(scale: Double, round1: Boolean = true): MicronutrientValues {
        fun s(v: Double?): Double? = v?.let {
            val raw = it * scale
            if (round1) kotlin.math.round(raw * 10) / 10.0 else raw
        }
        return MicronutrientValues(
            sugar = s(sugar),
            addedSugar = s(addedSugar),
            fiber = s(fiber),
            saturatedFat = s(saturatedFat),
            monounsaturatedFat = s(monounsaturatedFat),
            polyunsaturatedFat = s(polyunsaturatedFat),
            cholesterol = s(cholesterol),
            sodium = s(sodium),
            potassium = s(potassium),
            transFat = s(transFat),
            calcium = s(calcium),
            iron = s(iron),
            magnesium = s(magnesium),
            zinc = s(zinc),
            vitaminA = s(vitaminA),
            vitaminC = s(vitaminC),
            vitaminD = s(vitaminD),
            vitaminB12 = s(vitaminB12),
            vitaminE = s(vitaminE),
            vitaminK = s(vitaminK),
            folate = s(folate),
            omega3 = s(omega3),
            caffeine = s(caffeine),
            histidine = s(histidine),
            isoleucine = s(isoleucine),
            leucine = s(leucine),
            lysine = s(lysine),
            methionine = s(methionine),
            phenylalanine = s(phenylalanine),
            threonine = s(threonine),
            tryptophan = s(tryptophan),
            valine = s(valine),
            vitaminB1 = s(vitaminB1),
            vitaminB2 = s(vitaminB2),
            vitaminB3 = s(vitaminB3),
            vitaminB5 = s(vitaminB5),
            vitaminB6 = s(vitaminB6),
            biotin = s(biotin),
            choline = s(choline),
            phosphorus = s(phosphorus),
            selenium = s(selenium),
            copper = s(copper),
            manganese = s(manganese),
        )
    }

    fun applyTo(entry: FoodEntry): FoodEntry = entry.copy(
        sugar = sugar,
        addedSugar = addedSugar,
        fiber = fiber,
        saturatedFat = saturatedFat,
        monounsaturatedFat = monounsaturatedFat,
        polyunsaturatedFat = polyunsaturatedFat,
        cholesterol = cholesterol,
        sodium = sodium,
        potassium = potassium,
        transFat = transFat,
        calcium = calcium,
        iron = iron,
        magnesium = magnesium,
        zinc = zinc,
        vitaminA = vitaminA,
        vitaminC = vitaminC,
        vitaminD = vitaminD,
        vitaminB12 = vitaminB12,
        vitaminE = vitaminE,
        vitaminK = vitaminK,
        folate = folate,
        omega3 = omega3,
        caffeine = caffeine,
        histidine = histidine,
        isoleucine = isoleucine,
        leucine = leucine,
        lysine = lysine,
        methionine = methionine,
        phenylalanine = phenylalanine,
        threonine = threonine,
        tryptophan = tryptophan,
        valine = valine,
        vitaminB1 = vitaminB1,
        vitaminB2 = vitaminB2,
        vitaminB3 = vitaminB3,
        vitaminB5 = vitaminB5,
        vitaminB6 = vitaminB6,
        biotin = biotin,
        choline = choline,
        phosphorus = phosphorus,
        selenium = selenium,
        copper = copper,
        manganese = manganese,
    )

    companion object {
        fun from(entry: FoodEntry) = MicronutrientValues(
            sugar = entry.sugar,
            addedSugar = entry.addedSugar,
            fiber = entry.fiber,
            saturatedFat = entry.saturatedFat,
            monounsaturatedFat = entry.monounsaturatedFat,
            polyunsaturatedFat = entry.polyunsaturatedFat,
            cholesterol = entry.cholesterol,
            sodium = entry.sodium,
            potassium = entry.potassium,
            transFat = entry.transFat,
            calcium = entry.calcium,
            iron = entry.iron,
            magnesium = entry.magnesium,
            zinc = entry.zinc,
            vitaminA = entry.vitaminA,
            vitaminC = entry.vitaminC,
            vitaminD = entry.vitaminD,
            vitaminB12 = entry.vitaminB12,
            vitaminE = entry.vitaminE,
            vitaminK = entry.vitaminK,
            folate = entry.folate,
            omega3 = entry.omega3,
            caffeine = entry.caffeine,
            histidine = entry.histidine,
            isoleucine = entry.isoleucine,
            leucine = entry.leucine,
            lysine = entry.lysine,
            methionine = entry.methionine,
            phenylalanine = entry.phenylalanine,
            threonine = entry.threonine,
            tryptophan = entry.tryptophan,
            valine = entry.valine,
            vitaminB1 = entry.vitaminB1,
            vitaminB2 = entry.vitaminB2,
            vitaminB3 = entry.vitaminB3,
            vitaminB5 = entry.vitaminB5,
            vitaminB6 = entry.vitaminB6,
            biotin = entry.biotin,
            choline = entry.choline,
            phosphorus = entry.phosphorus,
            selenium = entry.selenium,
            copper = entry.copper,
            manganese = entry.manganese,
        )

        fun from(c: FoodConstituent) = MicronutrientValues(
            sugar = c.sugar,
            addedSugar = c.addedSugar,
            fiber = c.fiber,
            saturatedFat = c.saturatedFat,
            monounsaturatedFat = c.monounsaturatedFat,
            polyunsaturatedFat = c.polyunsaturatedFat,
            cholesterol = c.cholesterol,
            sodium = c.sodium,
            potassium = c.potassium,
            transFat = c.transFat,
            calcium = c.calcium,
            iron = c.iron,
            magnesium = c.magnesium,
            zinc = c.zinc,
            vitaminA = c.vitaminA,
            vitaminC = c.vitaminC,
            vitaminD = c.vitaminD,
            vitaminB12 = c.vitaminB12,
            vitaminE = c.vitaminE,
            vitaminK = c.vitaminK,
            folate = c.folate,
            omega3 = c.omega3,
            caffeine = c.caffeine,
            histidine = c.histidine,
            isoleucine = c.isoleucine,
            leucine = c.leucine,
            lysine = c.lysine,
            methionine = c.methionine,
            phenylalanine = c.phenylalanine,
            threonine = c.threonine,
            tryptophan = c.tryptophan,
            valine = c.valine,
            vitaminB1 = c.vitaminB1,
            vitaminB2 = c.vitaminB2,
            vitaminB3 = c.vitaminB3,
            vitaminB5 = c.vitaminB5,
            vitaminB6 = c.vitaminB6,
            biotin = c.biotin,
            choline = c.choline,
            phosphorus = c.phosphorus,
            selenium = c.selenium,
            copper = c.copper,
            manganese = c.manganese,
        )

        fun fromJson(optDouble: (String) -> Double?) = MicronutrientValues(
            sugar = optDouble(MicronutrientField.SUGAR.jsonKey),
            addedSugar = optDouble(MicronutrientField.ADDED_SUGAR.jsonKey),
            fiber = optDouble(MicronutrientField.FIBER.jsonKey),
            saturatedFat = optDouble(MicronutrientField.SATURATED_FAT.jsonKey),
            monounsaturatedFat = optDouble(MicronutrientField.MONOUNSATURATED_FAT.jsonKey),
            polyunsaturatedFat = optDouble(MicronutrientField.POLYUNSATURATED_FAT.jsonKey),
            cholesterol = optDouble(MicronutrientField.CHOLESTEROL.jsonKey),
            sodium = optDouble(MicronutrientField.SODIUM.jsonKey),
            potassium = optDouble(MicronutrientField.POTASSIUM.jsonKey),
            transFat = optDouble(MicronutrientField.TRANS_FAT.jsonKey),
            calcium = optDouble(MicronutrientField.CALCIUM.jsonKey),
            iron = optDouble(MicronutrientField.IRON.jsonKey),
            magnesium = optDouble(MicronutrientField.MAGNESIUM.jsonKey),
            zinc = optDouble(MicronutrientField.ZINC.jsonKey),
            vitaminA = optDouble(MicronutrientField.VITAMIN_A.jsonKey),
            vitaminC = optDouble(MicronutrientField.VITAMIN_C.jsonKey),
            vitaminD = optDouble(MicronutrientField.VITAMIN_D.jsonKey),
            vitaminB12 = optDouble(MicronutrientField.VITAMIN_B12.jsonKey),
            vitaminE = optDouble(MicronutrientField.VITAMIN_E.jsonKey),
            vitaminK = optDouble(MicronutrientField.VITAMIN_K.jsonKey),
            folate = optDouble(MicronutrientField.FOLATE.jsonKey),
            omega3 = optDouble(MicronutrientField.OMEGA3.jsonKey),
            caffeine = optDouble(MicronutrientField.CAFFEINE.jsonKey),
            histidine = optDouble(MicronutrientField.HISTIDINE.jsonKey),
            isoleucine = optDouble(MicronutrientField.ISOLEUCINE.jsonKey),
            leucine = optDouble(MicronutrientField.LEUCINE.jsonKey),
            lysine = optDouble(MicronutrientField.LYSINE.jsonKey),
            methionine = optDouble(MicronutrientField.METHIONINE.jsonKey),
            phenylalanine = optDouble(MicronutrientField.PHENYLALANINE.jsonKey),
            threonine = optDouble(MicronutrientField.THREONINE.jsonKey),
            tryptophan = optDouble(MicronutrientField.TRYPTOPHAN.jsonKey),
            valine = optDouble(MicronutrientField.VALINE.jsonKey),
            vitaminB1 = optDouble(MicronutrientField.VITAMIN_B1.jsonKey),
            vitaminB2 = optDouble(MicronutrientField.VITAMIN_B2.jsonKey),
            vitaminB3 = optDouble(MicronutrientField.VITAMIN_B3.jsonKey),
            vitaminB5 = optDouble(MicronutrientField.VITAMIN_B5.jsonKey),
            vitaminB6 = optDouble(MicronutrientField.VITAMIN_B6.jsonKey),
            biotin = optDouble(MicronutrientField.BIOTIN.jsonKey),
            choline = optDouble(MicronutrientField.CHOLINE.jsonKey),
            phosphorus = optDouble(MicronutrientField.PHOSPHORUS.jsonKey),
            selenium = optDouble(MicronutrientField.SELENIUM.jsonKey),
            copper = optDouble(MicronutrientField.COPPER.jsonKey),
            manganese = optDouble(MicronutrientField.MANGANESE.jsonKey),
        )

        fun fromLabelJson(optDouble: (String) -> Double?) = MicronutrientValues(
            sugar = optDouble(MicronutrientField.SUGAR.jsonKeyPer100g),
            addedSugar = optDouble(MicronutrientField.ADDED_SUGAR.jsonKeyPer100g),
            fiber = optDouble(MicronutrientField.FIBER.jsonKeyPer100g),
            saturatedFat = optDouble(MicronutrientField.SATURATED_FAT.jsonKeyPer100g),
            monounsaturatedFat = optDouble(MicronutrientField.MONOUNSATURATED_FAT.jsonKeyPer100g),
            polyunsaturatedFat = optDouble(MicronutrientField.POLYUNSATURATED_FAT.jsonKeyPer100g),
            cholesterol = optDouble(MicronutrientField.CHOLESTEROL.jsonKeyPer100g),
            sodium = optDouble(MicronutrientField.SODIUM.jsonKeyPer100g),
            potassium = optDouble(MicronutrientField.POTASSIUM.jsonKeyPer100g),
            transFat = optDouble(MicronutrientField.TRANS_FAT.jsonKeyPer100g),
            calcium = optDouble(MicronutrientField.CALCIUM.jsonKeyPer100g),
            iron = optDouble(MicronutrientField.IRON.jsonKeyPer100g),
            magnesium = optDouble(MicronutrientField.MAGNESIUM.jsonKeyPer100g),
            zinc = optDouble(MicronutrientField.ZINC.jsonKeyPer100g),
            vitaminA = optDouble(MicronutrientField.VITAMIN_A.jsonKeyPer100g),
            vitaminC = optDouble(MicronutrientField.VITAMIN_C.jsonKeyPer100g),
            vitaminD = optDouble(MicronutrientField.VITAMIN_D.jsonKeyPer100g),
            vitaminB12 = optDouble(MicronutrientField.VITAMIN_B12.jsonKeyPer100g),
            vitaminE = optDouble(MicronutrientField.VITAMIN_E.jsonKeyPer100g),
            vitaminK = optDouble(MicronutrientField.VITAMIN_K.jsonKeyPer100g),
            folate = optDouble(MicronutrientField.FOLATE.jsonKeyPer100g),
            omega3 = optDouble(MicronutrientField.OMEGA3.jsonKeyPer100g),
            caffeine = optDouble(MicronutrientField.CAFFEINE.jsonKeyPer100g),
            histidine = optDouble(MicronutrientField.HISTIDINE.jsonKeyPer100g),
            isoleucine = optDouble(MicronutrientField.ISOLEUCINE.jsonKeyPer100g),
            leucine = optDouble(MicronutrientField.LEUCINE.jsonKeyPer100g),
            lysine = optDouble(MicronutrientField.LYSINE.jsonKeyPer100g),
            methionine = optDouble(MicronutrientField.METHIONINE.jsonKeyPer100g),
            phenylalanine = optDouble(MicronutrientField.PHENYLALANINE.jsonKeyPer100g),
            threonine = optDouble(MicronutrientField.THREONINE.jsonKeyPer100g),
            tryptophan = optDouble(MicronutrientField.TRYPTOPHAN.jsonKeyPer100g),
            valine = optDouble(MicronutrientField.VALINE.jsonKeyPer100g),
            vitaminB1 = optDouble(MicronutrientField.VITAMIN_B1.jsonKeyPer100g),
            vitaminB2 = optDouble(MicronutrientField.VITAMIN_B2.jsonKeyPer100g),
            vitaminB3 = optDouble(MicronutrientField.VITAMIN_B3.jsonKeyPer100g),
            vitaminB5 = optDouble(MicronutrientField.VITAMIN_B5.jsonKeyPer100g),
            vitaminB6 = optDouble(MicronutrientField.VITAMIN_B6.jsonKeyPer100g),
            biotin = optDouble(MicronutrientField.BIOTIN.jsonKeyPer100g),
            choline = optDouble(MicronutrientField.CHOLINE.jsonKeyPer100g),
            phosphorus = optDouble(MicronutrientField.PHOSPHORUS.jsonKeyPer100g),
            selenium = optDouble(MicronutrientField.SELENIUM.jsonKeyPer100g),
            copper = optDouble(MicronutrientField.COPPER.jsonKeyPer100g),
            manganese = optDouble(MicronutrientField.MANGANESE.jsonKeyPer100g),
        )
    }
}
