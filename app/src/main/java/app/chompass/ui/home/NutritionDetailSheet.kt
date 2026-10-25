package app.chompass.ui.home

import app.chompass.ui.components.rememberChompassSheetState
import app.chompass.ui.components.ChompassSheetLazyColumn
import app.chompass.ui.components.ChompassBottomSheet
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Bolt
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.Coffee
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.LocalFireDepartment
import androidx.compose.material.icons.filled.Spa
import androidx.compose.material.icons.filled.WaterDrop
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.PlatformTextStyle
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.LineHeightStyle
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import app.chompass.R
import app.chompass.models.FoodEntry
import app.chompass.models.EnergyFormat
import app.chompass.models.HomeTopNutrient
import app.chompass.models.MacroValueFormatter
import app.chompass.models.OptionalNutrientGoals
import app.chompass.models.ResolvedDayTargets
import app.chompass.ui.components.energyUnitLabel
import app.chompass.ui.navigation.LocalEnergyUnit
import app.chompass.models.UserProfile
import app.chompass.ui.components.ChompassSurface
import app.chompass.ui.components.isDarkTheme
import app.chompass.ui.theme.AppColors
import app.chompass.ui.theme.AppTextOpacity
import app.chompass.ui.theme.AppRadii
import java.util.Locale
import kotlin.math.roundToInt

/**
 * Nutrition totals for the entries passed in (day or one meal slot).
 *
 * Two sections:
 *   Macros: Calories / Protein / Carbs / Fat — icon + label + value + unit
 *     + '/ goal (percent)'.
 *   Detailed Nutrition: same row, percent only when the goal is > 0.
 *
 * Home Cards stay on the day sheet and hide for a meal slot.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NutritionDetailSheet(
    entries: List<FoodEntry>,
    profile: UserProfile?,
    /** #60: the viewed day's resolved targets; null falls back to the base set. */
    resolved: ResolvedDayTargets? = null,
    homeTopNutrients: List<HomeTopNutrient>,
    optionalGoals: OptionalNutrientGoals,
    macroScale: Float = 1f,
    title: String? = null,
    showHomeCards: Boolean = true,
    trackerCaffeineMg: Double = 0.0,
    onHomeTopNutrientsChange: (List<HomeTopNutrient>) -> Unit,
    onDismiss: () -> Unit,
) {
    val state = rememberChompassSheetState()
    val listState = rememberLazyListState()
    var showHomeCardsPicker by remember { mutableStateOf(false) }
    val calories = entries.sumOf { it.calories }
    val protein = entries.sumOf { it.protein }
    val carbs = entries.sumOf { it.carbs }
    val fat = entries.sumOf { it.fat }
    val sugar = entries.sumOf { it.sugar ?: 0.0 }
    val addedSugar = entries.sumOf { it.addedSugar ?: 0.0 }
    val fiber = entries.sumOf { it.fiber ?: 0.0 }
    val satFat = entries.sumOf { it.saturatedFat ?: 0.0 }
    val monoFat = entries.sumOf { it.monounsaturatedFat ?: 0.0 }
    val polyFat = entries.sumOf { it.polyunsaturatedFat ?: 0.0 }
    val cholesterol = entries.sumOf { it.cholesterol ?: 0.0 }
    val sodium = entries.sumOf { it.sodium ?: 0.0 }
    val potassium = entries.sumOf { it.potassium ?: 0.0 }
    val transFat = entries.sumOf { it.transFat ?: 0.0 }
    val calcium = entries.sumOf { it.calcium ?: 0.0 }
    val iron = entries.sumOf { it.iron ?: 0.0 }
    val magnesium = entries.sumOf { it.magnesium ?: 0.0 }
    val zinc = entries.sumOf { it.zinc ?: 0.0 }
    val vitaminA = entries.sumOf { it.vitaminA ?: 0.0 }
    val vitaminC = entries.sumOf { it.vitaminC ?: 0.0 }
    val vitaminD = entries.sumOf { it.vitaminD ?: 0.0 }
    val vitaminB12 = entries.sumOf { it.vitaminB12 ?: 0.0 }
    val vitaminE = entries.sumOf { it.vitaminE ?: 0.0 }
    val vitaminK = entries.sumOf { it.vitaminK ?: 0.0 }
    val folate = entries.sumOf { it.folate ?: 0.0 }
    val omega3 = entries.sumOf { it.omega3 ?: 0.0 }
    val caffeine = entries.sumOf { it.caffeine ?: 0.0 } + trackerCaffeineMg
    val histidine = entries.sumOf { it.histidine ?: 0.0 }
    val isoleucine = entries.sumOf { it.isoleucine ?: 0.0 }
    val leucine = entries.sumOf { it.leucine ?: 0.0 }
    val lysine = entries.sumOf { it.lysine ?: 0.0 }
    val methionine = entries.sumOf { it.methionine ?: 0.0 }
    val phenylalanine = entries.sumOf { it.phenylalanine ?: 0.0 }
    val threonine = entries.sumOf { it.threonine ?: 0.0 }
    val tryptophan = entries.sumOf { it.tryptophan ?: 0.0 }
    val valine = entries.sumOf { it.valine ?: 0.0 }
    val vitaminB1 = entries.sumOf { it.vitaminB1 ?: 0.0 }
    val vitaminB2 = entries.sumOf { it.vitaminB2 ?: 0.0 }
    val vitaminB3 = entries.sumOf { it.vitaminB3 ?: 0.0 }
    val vitaminB5 = entries.sumOf { it.vitaminB5 ?: 0.0 }
    val vitaminB6 = entries.sumOf { it.vitaminB6 ?: 0.0 }
    val biotin = entries.sumOf { it.biotin ?: 0.0 }
    val choline = entries.sumOf { it.choline ?: 0.0 }
    val phosphorus = entries.sumOf { it.phosphorus ?: 0.0 }
    val selenium = entries.sumOf { it.selenium ?: 0.0 }
    val copper = entries.sumOf { it.copper ?: 0.0 }
    val manganese = entries.sumOf { it.manganese ?: 0.0 }
    val isDark = isDarkTheme()
    val sheetSurface = MaterialTheme.colorScheme.surfaceContainerLow

    fun fmt(v: Double): String = if (v == 0.0) "—" else String.format("%.1f", v)

    ChompassBottomSheet(
        onDismiss = onDismiss,
        sheetState = state,
        containerColor = sheetSurface,
    ) {
        SheetReviewToolbar(
            title = title ?: stringResource(R.string.nutrition_details_title),
            onCancel = onDismiss,
        )
        ChompassSheetLazyColumn(
            listState = listState,
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp)
                .padding(bottom = 16.dp),
            // Codeberg #30 (maintainer decision 2026-08-18): this read-only
            // micros sheet used to block content drags entirely (no swipe to
            // dismiss). Keep content drags enabled so it dismisses like the
            // other sheets — deliberately, via the raised sheet thresholds.
            blockTopEdge = false,
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            if (showHomeCards) {
                item { NutritionSheetSectionHeader(stringResource(R.string.nutrition_section_home_cards)) }
                item {
                    Card {
                        HomeCardsRow(
                            selected = homeTopNutrients,
                            onClick = { showHomeCardsPicker = true }
                        )
                    }
                }
            }

            item { NutritionSheetSectionHeader(stringResource(R.string.nutrition_section_macros)) }
            item {
                Card {
                    val baseCalorieGoal = resolved?.targets?.calories ?: profile?.effectiveCalories ?: 2000
                    val calorieGoal = if (macroScale > 1f && (resolved != null || profile != null)) {
                        (baseCalorieGoal * macroScale).roundToInt()
                    } else {
                        baseCalorieGoal
                    }
                    val proteinGoal = HomeTopNutrient.PROTEIN.goal(resolved, profile, optionalGoals, macroScale)
                    val carbsGoal = HomeTopNutrient.CARBS.goal(resolved, profile, optionalGoals, macroScale)
                    val fatGoal = HomeTopNutrient.FAT.goal(resolved, profile, optionalGoals, macroScale)
                    val proteinGlyph = stringResource(R.string.macro_glyph_protein)
                    val carbsGlyph = stringResource(R.string.macro_glyph_carbs)
                    val fatGlyph = stringResource(R.string.macro_glyph_fat)
                    DetailRow(Icons.Filled.LocalFireDepartment, stringResource(R.string.nutrition_label_calories), "${EnergyFormat.quantity(calories, LocalEnergyUnit.current)}", energyUnitLabel(), goal = "${EnergyFormat.quantity(calorieGoal, LocalEnergyUnit.current)}", percent = nutritionGoalPercent(calories.toDouble(), calorieGoal.toDouble()), accentColor = AppColors.Calorie)
                    Hairline()
                    DetailRow(null, stringResource(R.string.nutrition_label_protein), MacroValueFormatter.string(protein), stringResource(R.string.unit_g), goal = "$proteinGoal", percent = nutritionGoalPercent(protein, proteinGoal.toDouble()), labelGlyph = proteinGlyph, accentColor = AppColors.Protein)
                    Hairline()
                    DetailRow(null, stringResource(R.string.nutrition_label_carbs), MacroValueFormatter.string(carbs), stringResource(R.string.unit_g), goal = "$carbsGoal", percent = nutritionGoalPercent(carbs, carbsGoal.toDouble()), labelGlyph = carbsGlyph, accentColor = AppColors.Carbs)
                    Hairline()
                    DetailRow(null, stringResource(R.string.nutrition_label_fat), MacroValueFormatter.string(fat), stringResource(R.string.unit_g), goal = "$fatGoal", percent = nutritionGoalPercent(fat, fatGoal.toDouble()), labelGlyph = fatGlyph, accentColor = AppColors.Fat)
                }
            }

            item { NutritionSheetSectionHeader(stringResource(R.string.nutrition_section_detailed)) }
            item {
                Card {
                    DetailRow(null, stringResource(R.string.nutrition_label_sugar), fmt(sugar), stringResource(R.string.unit_g), goal = "${optionalGoals.sugar}", percent = nutritionGoalPercent(sugar, optionalGoals.sugar.toDouble()), labelGlyph = "S")
                    Hairline()
                    DetailRow(null, stringResource(R.string.nutrition_label_added_sugar), fmt(addedSugar), stringResource(R.string.unit_g), goal = "${optionalGoals.addedSugar}", percent = nutritionGoalPercent(addedSugar, optionalGoals.addedSugar.toDouble()), labelGlyph = "+")
                    Hairline()
                    DetailRow(Icons.Filled.Spa, stringResource(R.string.nutrition_label_fiber), fmt(fiber), stringResource(R.string.unit_g), goal = "${optionalGoals.fiber}", percent = nutritionGoalPercent(fiber, optionalGoals.fiber.toDouble()), accentColor = AppColors.Fiber)
                    Hairline()
                    DetailRow(Icons.Filled.WaterDrop, stringResource(R.string.nutrition_label_saturated_fat), fmt(satFat), stringResource(R.string.unit_g), goal = "${optionalGoals.saturatedFat}", percent = nutritionGoalPercent(satFat, optionalGoals.saturatedFat.toDouble()))
                    Hairline()
                    DetailRow(Icons.Filled.WaterDrop, stringResource(R.string.nutrition_label_mono_fat), fmt(monoFat), stringResource(R.string.unit_g))
                    Hairline()
                    DetailRow(Icons.Filled.WaterDrop, stringResource(R.string.nutrition_label_poly_fat), fmt(polyFat), stringResource(R.string.unit_g))
                    Hairline()
                    DetailRow(Icons.Filled.Favorite, stringResource(R.string.nutrition_label_cholesterol), fmt(cholesterol), stringResource(R.string.unit_mg), goal = "${optionalGoals.cholesterol}", percent = nutritionGoalPercent(cholesterol, optionalGoals.cholesterol.toDouble()))
                    Hairline()
                    DetailRow(Icons.Filled.Bolt, stringResource(R.string.nutrition_label_sodium), fmt(sodium), stringResource(R.string.unit_mg), goal = "${optionalGoals.sodium}", percent = nutritionGoalPercent(sodium, optionalGoals.sodium.toDouble()))
                    Hairline()
                    DetailRow(Icons.Filled.Bolt, stringResource(R.string.nutrition_label_potassium), fmt(potassium), stringResource(R.string.unit_mg), goal = "${optionalGoals.potassium}", percent = nutritionGoalPercent(potassium, optionalGoals.potassium.toDouble()))
                    Hairline()
                    DetailRow(Icons.Filled.WaterDrop, stringResource(R.string.nutrition_label_trans_fat), fmt(transFat), stringResource(R.string.unit_g), goal = "${optionalGoals.transFat}", percent = nutritionGoalPercent(transFat, optionalGoals.transFat.toDouble()))
                    Hairline()
                    DetailRow(Icons.Filled.Bolt, stringResource(R.string.nutrition_label_calcium), fmt(calcium), stringResource(R.string.unit_mg), goal = "${optionalGoals.calcium}", percent = nutritionGoalPercent(calcium, optionalGoals.calcium.toDouble()))
                    Hairline()
                    DetailRow(Icons.Filled.Bolt, stringResource(R.string.nutrition_label_iron), fmt(iron), stringResource(R.string.unit_mg), goal = "${optionalGoals.iron}", percent = nutritionGoalPercent(iron, optionalGoals.iron.toDouble()))
                    Hairline()
                    DetailRow(Icons.Filled.Bolt, stringResource(R.string.nutrition_label_magnesium), fmt(magnesium), stringResource(R.string.unit_mg), goal = "${optionalGoals.magnesium}", percent = nutritionGoalPercent(magnesium, optionalGoals.magnesium.toDouble()))
                    Hairline()
                    DetailRow(Icons.Filled.Bolt, stringResource(R.string.nutrition_label_zinc), fmt(zinc), stringResource(R.string.unit_mg), goal = "${optionalGoals.zinc}", percent = nutritionGoalPercent(zinc, optionalGoals.zinc.toDouble()))
                    Hairline()
                    DetailRow(null, stringResource(R.string.nutrition_label_vitamin_a), fmt(vitaminA), stringResource(R.string.unit_mcg), goal = "${optionalGoals.vitaminA}", percent = nutritionGoalPercent(vitaminA, optionalGoals.vitaminA.toDouble()), labelGlyph = "A")
                    Hairline()
                    DetailRow(null, stringResource(R.string.nutrition_label_vitamin_c), fmt(vitaminC), stringResource(R.string.unit_mg), goal = "${optionalGoals.vitaminC}", percent = nutritionGoalPercent(vitaminC, optionalGoals.vitaminC.toDouble()), labelGlyph = "C")
                    Hairline()
                    DetailRow(null, stringResource(R.string.nutrition_label_vitamin_d), fmt(vitaminD), stringResource(R.string.unit_mcg), goal = "${optionalGoals.vitaminD}", percent = nutritionGoalPercent(vitaminD, optionalGoals.vitaminD.toDouble()), labelGlyph = "D")
                    Hairline()
                    DetailRow(null, stringResource(R.string.nutrition_label_vitamin_b12), fmt(vitaminB12), stringResource(R.string.unit_mcg), goal = "${optionalGoals.vitaminB12}", percent = nutritionGoalPercent(vitaminB12, optionalGoals.vitaminB12.toDouble()), labelGlyph = "B")
                    Hairline()
                    DetailRow(null, stringResource(R.string.nutrition_label_vitamin_e), fmt(vitaminE), stringResource(R.string.unit_mg), goal = "${optionalGoals.vitaminE}", percent = nutritionGoalPercent(vitaminE, optionalGoals.vitaminE.toDouble()), labelGlyph = "E")
                    Hairline()
                    DetailRow(null, stringResource(R.string.nutrition_label_vitamin_k), fmt(vitaminK), stringResource(R.string.unit_mcg), goal = "${optionalGoals.vitaminK}", percent = nutritionGoalPercent(vitaminK, optionalGoals.vitaminK.toDouble()), labelGlyph = "K")
                    Hairline()
                    DetailRow(Icons.Filled.Spa, stringResource(R.string.nutrition_label_folate), fmt(folate), stringResource(R.string.unit_mcg), goal = "${optionalGoals.folate}", percent = nutritionGoalPercent(folate, optionalGoals.folate.toDouble()))
                    Hairline()
                    DetailRow(Icons.Filled.WaterDrop, stringResource(R.string.nutrition_label_omega3), fmt(omega3), stringResource(R.string.unit_g), goal = "${optionalGoals.omega3}", percent = nutritionGoalPercent(omega3, optionalGoals.omega3.toDouble()))
                    Hairline()
                    DetailRow(Icons.Filled.Coffee, stringResource(R.string.nutrition_label_caffeine), fmt(caffeine), stringResource(R.string.unit_mg), goal = "${optionalGoals.caffeine}", percent = nutritionGoalPercent(caffeine, optionalGoals.caffeine.toDouble()))
                    Hairline()
                    DetailRow(null, stringResource(R.string.nutrition_label_vitamin_b1), fmt(vitaminB1), stringResource(R.string.unit_mg), goal = "${optionalGoals.vitaminB1}", percent = nutritionGoalPercent(vitaminB1, optionalGoals.vitaminB1.toDouble()), labelGlyph = "B1")
                    Hairline()
                    DetailRow(null, stringResource(R.string.nutrition_label_vitamin_b2), fmt(vitaminB2), stringResource(R.string.unit_mg), goal = "${optionalGoals.vitaminB2}", percent = nutritionGoalPercent(vitaminB2, optionalGoals.vitaminB2.toDouble()), labelGlyph = "B2")
                    Hairline()
                    DetailRow(null, stringResource(R.string.nutrition_label_vitamin_b3), fmt(vitaminB3), stringResource(R.string.unit_mg), goal = "${optionalGoals.vitaminB3}", percent = nutritionGoalPercent(vitaminB3, optionalGoals.vitaminB3.toDouble()), labelGlyph = "B3")
                    Hairline()
                    DetailRow(null, stringResource(R.string.nutrition_label_vitamin_b5), fmt(vitaminB5), stringResource(R.string.unit_mg), goal = "${optionalGoals.vitaminB5}", percent = nutritionGoalPercent(vitaminB5, optionalGoals.vitaminB5.toDouble()), labelGlyph = "B5")
                    Hairline()
                    DetailRow(null, stringResource(R.string.nutrition_label_vitamin_b6), fmt(vitaminB6), stringResource(R.string.unit_mg), goal = "${optionalGoals.vitaminB6}", percent = nutritionGoalPercent(vitaminB6, optionalGoals.vitaminB6.toDouble()), labelGlyph = "B6")
                    Hairline()
                    DetailRow(null, stringResource(R.string.nutrition_label_biotin), fmt(biotin), stringResource(R.string.unit_mcg), goal = "${optionalGoals.biotin}", percent = nutritionGoalPercent(biotin, optionalGoals.biotin.toDouble()), labelGlyph = "B7")
                    Hairline()
                    DetailRow(null, stringResource(R.string.nutrition_label_choline), fmt(choline), stringResource(R.string.unit_mg), goal = "${optionalGoals.choline}", percent = nutritionGoalPercent(choline, optionalGoals.choline.toDouble()), labelGlyph = "Ch")
                    Hairline()
                    DetailRow(Icons.Filled.Bolt, stringResource(R.string.nutrition_label_phosphorus), fmt(phosphorus), stringResource(R.string.unit_mg), goal = "${optionalGoals.phosphorus}", percent = nutritionGoalPercent(phosphorus, optionalGoals.phosphorus.toDouble()))
                    Hairline()
                    DetailRow(Icons.Filled.Bolt, stringResource(R.string.nutrition_label_selenium), fmt(selenium), stringResource(R.string.unit_mcg), goal = "${optionalGoals.selenium}", percent = nutritionGoalPercent(selenium, optionalGoals.selenium.toDouble()))
                    Hairline()
                    DetailRow(Icons.Filled.Bolt, stringResource(R.string.nutrition_label_copper), fmt(copper), stringResource(R.string.unit_mg), goal = "${optionalGoals.copper}", percent = nutritionGoalPercent(copper, optionalGoals.copper.toDouble()))
                    Hairline()
                    DetailRow(Icons.Filled.Bolt, stringResource(R.string.nutrition_label_manganese), fmt(manganese), stringResource(R.string.unit_mg), goal = "${optionalGoals.manganese}", percent = nutritionGoalPercent(manganese, optionalGoals.manganese.toDouble()))
                }
            }

            item { NutritionSheetSectionHeader(stringResource(R.string.nutrition_section_amino_acids)) }
            item {
                Card {
                    DetailRow(null, stringResource(R.string.nutrition_label_histidine), fmt(histidine), stringResource(R.string.unit_g), goal = "${optionalGoals.histidine}", percent = nutritionGoalPercent(histidine, optionalGoals.histidine.toDouble()), labelGlyph = "H")
                    Hairline()
                    DetailRow(null, stringResource(R.string.nutrition_label_isoleucine), fmt(isoleucine), stringResource(R.string.unit_g), goal = "${optionalGoals.isoleucine}", percent = nutritionGoalPercent(isoleucine, optionalGoals.isoleucine.toDouble()), labelGlyph = "I")
                    Hairline()
                    DetailRow(null, stringResource(R.string.nutrition_label_leucine), fmt(leucine), stringResource(R.string.unit_g), goal = "${optionalGoals.leucine}", percent = nutritionGoalPercent(leucine, optionalGoals.leucine.toDouble()), labelGlyph = "L", accentColor = AppColors.Protein)
                    Hairline()
                    DetailRow(null, stringResource(R.string.nutrition_label_lysine), fmt(lysine), stringResource(R.string.unit_g), goal = "${optionalGoals.lysine}", percent = nutritionGoalPercent(lysine, optionalGoals.lysine.toDouble()), labelGlyph = "K")
                    Hairline()
                    DetailRow(null, stringResource(R.string.nutrition_label_methionine), fmt(methionine), stringResource(R.string.unit_g), goal = "${optionalGoals.methionine}", percent = nutritionGoalPercent(methionine, optionalGoals.methionine.toDouble()), labelGlyph = "M")
                    Hairline()
                    DetailRow(null, stringResource(R.string.nutrition_label_phenylalanine), fmt(phenylalanine), stringResource(R.string.unit_g), goal = "${optionalGoals.phenylalanine}", percent = nutritionGoalPercent(phenylalanine, optionalGoals.phenylalanine.toDouble()), labelGlyph = "F")
                    Hairline()
                    DetailRow(null, stringResource(R.string.nutrition_label_threonine), fmt(threonine), stringResource(R.string.unit_g), goal = "${optionalGoals.threonine}", percent = nutritionGoalPercent(threonine, optionalGoals.threonine.toDouble()), labelGlyph = "T")
                    Hairline()
                    DetailRow(null, stringResource(R.string.nutrition_label_tryptophan), fmt(tryptophan), stringResource(R.string.unit_g), goal = "${optionalGoals.tryptophan}", percent = nutritionGoalPercent(tryptophan, optionalGoals.tryptophan.toDouble()), labelGlyph = "W")
                    Hairline()
                    DetailRow(null, stringResource(R.string.nutrition_label_valine), fmt(valine), stringResource(R.string.unit_g), goal = "${optionalGoals.valine}", percent = nutritionGoalPercent(valine, optionalGoals.valine.toDouble()), labelGlyph = "V")
                }
            }
            // #86: per-ingredient breakdown with the same micros +% block as
            // the review sheets. Read-only; only when entries carry rows.
            val constituentRows = entries.flatMap { it.constituents }
            if (constituentRows.isNotEmpty()) {
                item { NutritionSheetSectionHeader(stringResource(R.string.sheet_constituents)) }
                item {
                    Text(
                        text = stringResource(R.string.sheet_constituents_estimates_note),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(start = 14.dp),
                    )
                }
                item {
                    Card {
                        constituentRows.forEachIndexed { index, row ->
                            if (index > 0) Hairline()
                            ConstituentSummaryRow(row, optionalGoals)
                        }
                    }
                }
            }
        }
    }

    if (showHomeCardsPicker) {
        HomeTopNutrientPickerDialog(
            selected = homeTopNutrients,
            onSave = onHomeTopNutrientsChange,
            onDismiss = { showHomeCardsPicker = false }
        )
    }
}

// HomeTopNutrientPickerDialog moved to HomeNutrientPicker.kt

@Composable
private fun Card(content: @Composable () -> Unit) {
    ChompassSurface(
        modifier = Modifier.fillMaxWidth(),
        cornerRadius = 20.dp,
        padding = 0.dp
    ) {
        Column { content() }
    }
}

@Composable
private fun NutritionSheetSectionHeader(title: String) {
    Text(
        title.uppercase(Locale.getDefault()),
        fontSize = 12.sp,
        fontWeight = FontWeight.SemiBold,
        color = MaterialTheme.colorScheme.onSurface.copy(alpha = AppTextOpacity.Muted),
        letterSpacing = 0.sp,
        modifier = Modifier.padding(start = 14.dp, top = 6.dp, bottom = 4.dp)
    )
}

@Composable
private fun HomeCardsRow(
    selected: List<HomeTopNutrient>,
    onClick: () -> Unit
) {
    Row(
        Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(horizontal = 14.dp, vertical = 14.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Icon(Icons.Filled.Spa, null, tint = AppColors.Calorie, modifier = Modifier.size(20.dp))
        Column(Modifier.weight(1f)) {
            Text(stringResource(R.string.home_nutrient_cards), fontSize = 17.sp)
            Text(
                selected.map { stringResource(it.displayNameRes) }.joinToString(", "),
                fontSize = 13.sp,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = AppTextOpacity.Muted)
            )
        }
        Icon(
            Icons.AutoMirrored.Filled.KeyboardArrowRight,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.onSurface.copy(alpha = AppTextOpacity.Disabled),
            modifier = Modifier.size(20.dp)
        )
    }
}

/**
 * Percent of a daily goal. Null when the goal is missing or not positive.
 */
internal fun nutritionGoalPercent(value: Double, goal: Double): Int? {
    if (!value.isFinite() || !goal.isFinite() || goal <= 0.0) return null
    return ((value / goal) * 100.0).roundToInt()
}

/**
 * Row layout: icon (24dp pink, optional) + label (17sp) + value (17sp pink semibold)
 * + unit (13sp secondary) + optional '/ goal (percent)' (12sp tertiary).
 *
 * iOS uses LinearGradient on the SF Symbol; Compose uses a flat tint
 * since Material icons aren't text-paintable.
 */
@Composable
private fun DetailRow(
    icon: ImageVector?,
    label: String,
    value: String,
    unit: String,
    goal: String? = null,
    percent: Int? = null,
    labelGlyph: String? = null,
    accentColor: Color = AppColors.Calorie,
    ) {
    Row(
        Modifier.fillMaxWidth().padding(horizontal = 14.dp, vertical = 14.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        if (icon != null) {
            Icon(icon, null, tint = accentColor, modifier = Modifier.size(20.dp))
        } else if (labelGlyph != null) {
            Box(
                Modifier
                    .size(20.dp)
                    .clip(RoundedCornerShape(AppRadii.Track))
                    .background(accentColor),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    labelGlyph,
                    style = TextStyle(
                        color = Color.White,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        textAlign = TextAlign.Center,
                        lineHeight = 11.sp,
                        platformStyle = PlatformTextStyle(includeFontPadding = false),
                        lineHeightStyle = LineHeightStyle(
                            alignment = LineHeightStyle.Alignment.Center,
                            trim = LineHeightStyle.Trim.Both,
                        ),
                    ),
                )
            }
        } else {
            Spacer(Modifier.width(20.dp))
        }
        Text(label, fontSize = 17.sp, modifier = Modifier.weight(1f))
        Row(verticalAlignment = Alignment.Bottom, horizontalArrangement = Arrangement.spacedBy(3.dp)) {
            Text(value, fontSize = 17.sp, fontWeight = FontWeight.SemiBold, color = accentColor)
            Text(unit, fontSize = 13.sp, color = MaterialTheme.colorScheme.onSurface.copy(alpha = AppTextOpacity.Muted))
        }
        goal?.let {
            val suffix = if (percent != null) "/ $it ($percent%)" else "/ $it"
            Text(
                suffix,
                fontSize = 12.sp,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = AppTextOpacity.Disabled),
                modifier = Modifier.padding(start = 6.dp)
            )
        }
    }
}

@Composable
private fun Hairline() {
    Box(
        Modifier
            .padding(start = 14.dp)
            .fillMaxWidth()
            .height(0.5.dp)
            .background(MaterialTheme.colorScheme.onSurface.copy(alpha = 0.1f))
    )
}
