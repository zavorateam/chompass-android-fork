package app.chompass.ui.settings

import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.outlined.Delete
import androidx.compose.material.icons.outlined.Edit
import androidx.compose.material.icons.outlined.FolderOpen
import androidx.compose.material.icons.outlined.Link
import androidx.compose.material.icons.outlined.Refresh
import androidx.compose.material.icons.outlined.Restaurant
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import app.chompass.AppContainer
import app.chompass.R
import app.chompass.models.CustomFoodDatabase
import app.chompass.models.CustomFoodExporter
import app.chompass.models.CustomFoodParser
import app.chompass.models.FoodEntry
import app.chompass.models.FoodGroundingProvenance
import app.chompass.models.FoodSource
import app.chompass.models.NutrientBasis
import app.chompass.models.NutrientSourceKind
import app.chompass.services.ai.FoodAnalysisService
import app.chompass.ui.components.ChompassTextField
import app.chompass.ui.theme.AppColors
import app.chompass.ui.theme.AppTextOpacity
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.util.UUID
import kotlin.math.roundToInt

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CustomFoodDatabaseDetailScreen(
    databaseId: String,
    container: AppContainer,
    onBack: () -> Unit,
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val repo = container.customFoodDatabaseRepository
    val databases by repo.databases.collectAsState()
    val db = databases.firstOrNull { it.id == databaseId }

    var foods by remember { mutableStateOf<List<FoodEntry>>(emptyList()) }
    var searchQuery by remember { mutableStateOf("") }
    var editingFood by remember { mutableStateOf<FoodEntry?>(null) }
    var showAddFoodDialog by remember { mutableStateOf(false) }
    var foodToDelete by remember { mutableStateOf<FoodEntry?>(null) }
    var showMenu by remember { mutableStateOf(false) }
    var isProcessing by remember { mutableStateOf(false) }

    val reloadFoods = {
        scope.launch {
            foods = repo.getFoods(databaseId)
        }
    }

    LaunchedEffect(databaseId) {
        reloadFoods()
    }

    val fileAppendPicker = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri ->
        if (uri != null && db != null) {
            scope.launch {
                isProcessing = true
                try {
                    val content = withContext(Dispatchers.IO) {
                        context.contentResolver.openInputStream(uri)?.use { input ->
                            input.bufferedReader().readText()
                        } ?: ""
                    }
                    val (_, newFoods) = CustomFoodParser.parseContent(content, db.name)
                    val combined = foods + newFoods
                    repo.saveDatabase(db, combined)
                    foods = combined
                    Toast.makeText(context, "Added ${newFoods.size} items", Toast.LENGTH_SHORT).show()
                } catch (e: Exception) {
                    Toast.makeText(context, "Error: ${e.localizedMessage}", Toast.LENGTH_LONG).show()
                } finally {
                    isProcessing = false
                }
            }
        }
    }

    val filteredFoods = remember(foods, searchQuery) {
        if (searchQuery.isBlank()) foods
        else {
            val q = searchQuery.trim().lowercase()
            foods.filter {
                it.name.lowercase().contains(q) ||
                    it.customNote?.lowercase()?.contains(q) == true
            }
        }
    }

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(
                            db?.name ?: stringResource(R.string.custom_food_dbs_title),
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        Text(
                            stringResource(R.string.custom_food_db_items_count_format, foods.size),
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = stringResource(R.string.action_close))
                    }
                },
                actions = {
                    if (db?.sourceUrl != null) {
                        IconButton(
                            onClick = {
                                scope.launch {
                                    isProcessing = true
                                    try {
                                        repo.updateDatabaseFromUrl(databaseId, FoodAnalysisService.defaultClient)
                                        reloadFoods()
                                        Toast.makeText(context, "Refreshed from URL", Toast.LENGTH_SHORT).show()
                                    } catch (e: Exception) {
                                        Toast.makeText(context, "Failed: ${e.localizedMessage}", Toast.LENGTH_LONG).show()
                                    } finally {
                                        isProcessing = false
                                    }
                                }
                            }
                        ) {
                            Icon(Icons.Outlined.Refresh, contentDescription = stringResource(R.string.custom_food_db_action_refresh_url))
                        }
                    }

                    Box {
                        IconButton(onClick = { showMenu = true }) {
                            Icon(Icons.Default.MoreVert, contentDescription = null)
                        }
                        DropdownMenu(
                            expanded = showMenu,
                            onDismissRequest = { showMenu = false }
                        ) {
                            DropdownMenuItem(
                                text = { Text(stringResource(R.string.custom_food_db_action_import_file)) },
                                leadingIcon = { Icon(Icons.Outlined.FolderOpen, contentDescription = null) },
                                onClick = {
                                    showMenu = false
                                    fileAppendPicker.launch("*/*")
                                }
                            )
                            db?.let { d ->
                                DropdownMenuItem(
                                    text = { Text(stringResource(R.string.custom_food_db_action_export_csv)) },
                                    leadingIcon = { Icon(Icons.Outlined.FolderOpen, contentDescription = null) },
                                    onClick = {
                                        showMenu = false
                                        scope.launch {
                                            val csv = CustomFoodExporter.exportToCsv(d, foods)
                                            copyToClipboard(context, "${d.name}.csv", csv)
                                            Toast.makeText(context, "CSV copied to clipboard", Toast.LENGTH_SHORT).show()
                                        }
                                    }
                                )
                            }
                        }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.background)
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = { showAddFoodDialog = true },
                containerColor = AppColors.Calorie,
                contentColor = MaterialTheme.colorScheme.onPrimary
            ) {
                Icon(Icons.Default.Add, contentDescription = stringResource(R.string.custom_food_db_action_add_food))
            }
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            ChompassTextField(
                value = searchQuery,
                onValueChange = { searchQuery = it },
                placeholder = stringResource(R.string.saved_meals_search_placeholder),
                leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
                trailingIcon = if (searchQuery.isNotEmpty()) {
                    {
                        IconButton(onClick = { searchQuery = "" }) {
                            Icon(Icons.Default.Close, contentDescription = null)
                        }
                    }
                } else null,
                modifier = Modifier.fillMaxWidth()
            )

            if (filteredFoods.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .weight(1f),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(
                            Icons.Outlined.Restaurant,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f),
                            modifier = Modifier.size(48.dp)
                        )
                        Text(
                            if (searchQuery.isBlank()) "База пока пуста. Нажмите + чтобы добавить продукт."
                            else "Ничего не найдено по запросу",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            } else {
                LazyColumn(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(filteredFoods, key = { it.id.toString() }) { food ->
                        FoodItemRow(
                            food = food,
                            onClick = { editingFood = food },
                            onDelete = { foodToDelete = food }
                        )
                    }
                    item {
                        Spacer(Modifier.height(80.dp))
                    }
                }
            }
        }
    }

    // Add / Edit Food Dialog
    if (showAddFoodDialog || editingFood != null) {
        val targetFood = editingFood
        AddEditFoodItemDialog(
            food = targetFood,
            dbName = db?.name ?: "Custom DB",
            onDismiss = {
                showAddFoodDialog = false
                editingFood = null
            },
            onSave = { updatedFood ->
                showAddFoodDialog = false
                editingFood = null
                scope.launch {
                    if (targetFood == null) {
                        repo.addFoodItem(databaseId, updatedFood)
                    } else {
                        repo.updateFoodItem(databaseId, updatedFood)
                    }
                    reloadFoods()
                }
            }
        )
    }

    foodToDelete?.let { food ->
        AlertDialog(
            onDismissRequest = { foodToDelete = null },
            title = { Text(stringResource(R.string.custom_food_db_confirm_delete_food_title)) },
            text = { Text(stringResource(R.string.custom_food_db_confirm_delete_food_desc, food.name)) },
            confirmButton = {
                TextButton(
                    onClick = {
                        val id = food.id.toString()
                        foodToDelete = null
                        scope.launch {
                            repo.deleteFoodItem(databaseId, id)
                            reloadFoods()
                        }
                    }
                ) {
                    Text(stringResource(R.string.action_delete), color = MaterialTheme.colorScheme.error)
                }
            },
            dismissButton = {
                TextButton(onClick = { foodToDelete = null }) {
                    Text(stringResource(R.string.action_cancel))
                }
            }
        )
    }
}

@Composable
private fun FoodItemRow(
    food: FoodEntry,
    onClick: () -> Unit,
    onDelete: () -> Unit,
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .clickable(onClick = onClick),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    food.name,
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.Medium,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    food.customNote?.takeIf { it.isNotBlank() }?.let { brand ->
                        Text(
                            brand,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.primary,
                            fontWeight = FontWeight.SemiBold
                        )
                        Text("•", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                    val servingText = "${food.servingSizeGrams?.roundToInt() ?: 100} ${food.selectedServingUnit ?: "г"}"
                    Text(
                        servingText,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            Column(horizontalAlignment = Alignment.End) {
                Text(
                    "${food.calories} ккал",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = AppColors.Calorie
                )
                Text(
                    "Б:${food.protein.roundToInt()}g  У:${food.carbs.roundToInt()}g  Ж:${food.fat.roundToInt()}g",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            IconButton(onClick = onDelete, modifier = Modifier.size(36.dp)) {
                Icon(
                    Icons.Outlined.Delete,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f),
                    modifier = Modifier.size(18.dp)
                )
            }
        }
    }
}

@Composable
private fun AddEditFoodItemDialog(
    food: FoodEntry?,
    dbName: String,
    onDismiss: () -> Unit,
    onSave: (FoodEntry) -> Unit,
) {
    var name by remember { mutableStateOf(food?.name ?: "") }
    var brand by remember { mutableStateOf(food?.customNote ?: "") }
    var caloriesStr by remember { mutableStateOf(food?.calories?.toString() ?: "") }
    var proteinStr by remember { mutableStateOf(food?.protein?.toString() ?: "") }
    var carbsStr by remember { mutableStateOf(food?.carbs?.toString() ?: "") }
    var fatStr by remember { mutableStateOf(food?.fat?.toString() ?: "") }
    var servingGramsStr by remember { mutableStateOf(food?.servingSizeGrams?.roundToInt()?.toString() ?: "100") }
    var servingUnit by remember { mutableStateOf(food?.selectedServingUnit ?: "g") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(if (food == null) stringResource(R.string.custom_food_db_action_add_food) else stringResource(R.string.custom_food_db_action_edit_food))
        },
        text = {
            Column(
                modifier = Modifier.verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text(stringResource(R.string.custom_food_db_food_name)) },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = brand,
                    onValueChange = { brand = it },
                    label = { Text(stringResource(R.string.custom_food_db_brand)) },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(
                        value = caloriesStr,
                        onValueChange = { caloriesStr = it },
                        label = { Text("Калории (ккал)") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        singleLine = true,
                        modifier = Modifier.weight(1f)
                    )
                    OutlinedTextField(
                        value = servingGramsStr,
                        onValueChange = { servingGramsStr = it },
                        label = { Text("Вес (г)") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        singleLine = true,
                        modifier = Modifier.weight(1f)
                    )
                }
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(
                        value = proteinStr,
                        onValueChange = { proteinStr = it },
                        label = { Text("Белки (г)") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                        singleLine = true,
                        modifier = Modifier.weight(1f)
                    )
                    OutlinedTextField(
                        value = carbsStr,
                        onValueChange = { carbsStr = it },
                        label = { Text("Углеводы (г)") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                        singleLine = true,
                        modifier = Modifier.weight(1f)
                    )
                    OutlinedTextField(
                        value = fatStr,
                        onValueChange = { fatStr = it },
                        label = { Text("Жиры (г)") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                        singleLine = true,
                        modifier = Modifier.weight(1f)
                    )
                }
                OutlinedTextField(
                    value = servingUnit,
                    onValueChange = { servingUnit = it },
                    label = { Text("Единица измерения (г, порция)") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            TextButton(
                enabled = name.isNotBlank(),
                onClick = {
                    val cal = caloriesStr.toIntOrNull() ?: 0
                    val p = proteinStr.replace(",", ".").toDoubleOrNull() ?: 0.0
                    val c = carbsStr.replace(",", ".").toDoubleOrNull() ?: 0.0
                    val f = fatStr.replace(",", ".").toDoubleOrNull() ?: 0.0
                    val grams = servingGramsStr.toDoubleOrNull() ?: 100.0

                    val resultFood = FoodEntry(
                        id = food?.id ?: UUID.randomUUID(),
                        name = name.trim(),
                        calories = cal,
                        protein = p,
                        carbs = c,
                        fat = f,
                        servingSizeGrams = grams,
                        selectedServingUnit = servingUnit.ifBlank { "g" },
                        selectedServingQuantity = 1.0,
                        customNote = brand.trim().ifBlank { null },
                        source = FoodSource.SEARCH,
                        grounding = FoodGroundingProvenance(
                            sourceKind = NutrientSourceKind.CUSTOM,
                            sourceName = dbName,
                            nutrientBasis = NutrientBasis.PER_SERVING,
                            datasetVersion = dbName,
                            retrievedAtEpochMs = System.currentTimeMillis(),
                        )
                    )
                    onSave(resultFood)
                }
            ) {
                Text(stringResource(R.string.action_save))
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(stringResource(R.string.action_cancel))
            }
        }
    )
}

private fun copyToClipboard(context: android.content.Context, label: String, text: String) {
    val clipboard = context.getSystemService(android.content.Context.CLIPBOARD_SERVICE) as android.content.ClipboardManager
    val clip = android.content.ClipData.newPlainText(label, text)
    clipboard.setPrimaryClip(clip)
}
