# Amino Acids & Extended Nutrients Integration Plan

Full integration of essential amino acids (Histidine, Isoleucine, Leucine, Lysine, Methionine, Phenylalanine, Threonine, Tryptophan, Valine) and extended micronutrients (Vitamins B1, B2, B3, B5, B6, Biotin, Choline, Phosphorus, Selenium, Copper, Manganese) across the entire application data stack, AI extraction pipeline, UI logs, Home cards, Progress charts, and Settings.

## User Review & Critical Decisions

> [!IMPORTANT]
> All key design and feature scope decisions were confirmed during Phase 1 clarification:
> - **Nutrient Scope**: Full profile including essential amino acids (EAAs/BCAAs), extended vitamins, and minerals.
> - **UI Integration**: Breakdown sheets on food click, customizable Home/Progress cards & rings, and expandable log details.
> - **Goals & AI Extraction**: Auto-calculation of daily target recommendations based on user body metrics with AI food photo/text extraction.

---

## 1. Overview & Core Concept

This update turns Chompass into a comprehensive protein quality and micronutrient tracker:
- **Essential Amino Acid Profile**: Tracks all 9 EAAs (Leucine, Isoleucine, Valine, Lysine, Methionine, Phenylalanine, Threonine, Tryptophan, Histidine) in grams/milligrams to measure complete protein intake.
- **Extended Micronutrient Suite**: Tracks B-complex vitamins (B1, B2, B3, B5, B6, Biotin), Choline, and trace minerals (Phosphorus, Selenium, Copper, Manganese).
- **Automated Target Derivation**: Calculates RDA/DRI targets for amino acids and micronutrients derived from user gender, weight, and activity level.
- **AI Extraction**: Prompt engineers AI vision & text models to automatically estimate amino acid and micronutrient quantities from meals.

---

## 2. User Experience & Visual Design

- **Home Screen**:
  - Customizable nutrient rings and progress mini-cards for key EAAs (e.g. Leucine, Lysine) alongside existing macros.
  - Quick-view bottom sheet on clicking any logged food entry showing full amino acid & micronutrient composition.
- **Progress & Analytics Screen**:
  - Dedicated "Amino Acids & Protein Quality" tab/card showing daily complete protein breakdown.
  - Expanded "Vitamins & Minerals" breakdown with RDA fulfillment percentage bars and trends.
- **Food Logging & Editing**:
  - Categorized "Amino Acids" and "Micronutrients" accordion sections in food entry editor sheets.
  - Interactive sliders/fields for fine-tuning individual amino acid grams and mg of vitamins/minerals.
- **Settings & Targets**:
  - Auto-calculated RDA targets with optional manual overrides in Optional Nutrient Goals screen.

---

## 3. Key Product Decisions & Trade-Offs

- **EAA Units**: Standardized on grams (`g`) for amino acids in food entry models and daily totals for readability (e.g., Leucine 2.4g), with `mg` support in input fields where appropriate.
- **Backward Compatibility**: All new fields are nullable with default `null` values to ensure existing JSON serialization, database backups, and saved meals load smoothly without migration errors.
- **Performance**: Derived total calculations use memoized computations in ViewModels to keep scrolling in `LazyColumn` at 60 FPS.

---

## 4. Technical Architecture & Data Strategy

```
┌─────────────────────────────────────────────────────────┐
│                    UI Presentation                      │
│ ┌──────────────────┐ ┌────────────────┐ ┌─────────────┐ │
│ │ Home Nutrient    │ │ Progress EAA   │ │ Food Detail │ │
│ │ Cards & Rings    │ │ & Micro Charts │ │ Sheet       │ │
│ └─────────┬────────┘ └───────┬────────┘ └──────┬──────┘ │
└───────────┼──────────────────┼─────────────────┼────────┘
            │                  │                 │
┌───────────▼──────────────────▼─────────────────▼────────┐
│                   ViewModel & Domain                    │
│ ┌─────────────────────────────────────────────────────┐ │
│ │ HomeViewModel / ProgressViewModel / FoodEditViewModel │ │
│ │ Auto-calculates RDA goals & aggregates daily totals   │ │
│ └──────────────────────────┬──────────────────────────┘ │
└────────────────────────────┼────────────────────────────┘
                             │
┌────────────────────────────▼────────────────────────────┐
│                    Data & AI Engine                     │
│ ┌──────────────────┐ ┌────────────────┐ ┌─────────────┐ │
│ │ Micronutrient    │ │ AI Extraction  │ │ FoodEntry   │ │
│ │ Catalog & Goals  │ │ Prompt & Parser│ │ & Database  │ │
│ └──────────────────┘ └────────────────┘ └─────────────┘ │
└─────────────────────────────────────────────────────────┘
```

### Key Data Entities & Changes
1. `MicronutrientCatalog.kt`: Add enum entries for EAAs (`HISTIDINE`, `ISOLEUCINE`, `LEUCINE`, `LYSINE`, `METHIONINE`, `PHENYLALANINE`, `THREONINE`, `TRYPTOPHAN`, `VALINE`) and extended vitamins/minerals (`VITAMIN_B1`, `VITAMIN_B2`, `VITAMIN_B3`, `VITAMIN_B5`, `VITAMIN_B6`, `BIOTIN`, `CHOLINE`, `PHOSPHORUS`, `SELENIUM`, `COPPER`, `MANGANESE`).
2. `FoodEntry.kt` & `FoodConstituent.kt`: Expand fields for all new nutrients.
3. `NutrientGoals.kt`: Add RDA goal calculators for amino acids based on body weight (e.g. Leucine ~42mg/kg/day) and standard DRI rules.
4. `FoodAnalysisService.kt`: Update AI prompt instructions and JSON schema to extract amino acid breakdowns and extended micronutrients.
5. `NutritionDetailSheet.kt`, `AddFoodSheet.kt`, `EditFoodEntrySheet.kt`: Add Amino Acid input and view sections.
6. `HomeScreen.kt` & `ProgressScreen.kt`: Add visual UI indicators, progress rings, and charts for EAAs and new nutrients.
