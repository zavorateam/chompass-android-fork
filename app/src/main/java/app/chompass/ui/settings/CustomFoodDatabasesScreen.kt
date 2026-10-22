package app.chompass.ui.settings

import android.net.Uri
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.outlined.CloudDownload
import androidx.compose.material.icons.outlined.Code
import androidx.compose.material.icons.outlined.ContentPaste
import androidx.compose.material.icons.outlined.Delete
import androidx.compose.material.icons.outlined.Edit
import androidx.compose.material.icons.outlined.FileDownload
import androidx.compose.material.icons.outlined.FolderOpen
import androidx.compose.material.icons.outlined.Link
import androidx.compose.material.icons.outlined.PostAdd
import androidx.compose.material.icons.outlined.Refresh
import androidx.compose.material.icons.outlined.Storage
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
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
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import app.chompass.AppContainer
import app.chompass.R
import app.chompass.models.CustomFoodDatabase
import app.chompass.models.CustomFoodExporter
import app.chompass.models.CustomFoodParser
import app.chompass.services.ai.FoodAnalysisService
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import app.chompass.ui.theme.AppColors
import app.chompass.ui.theme.AppTextOpacity
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CustomFoodDatabasesScreen(
    container: AppContainer,
    onBack: () -> Unit,
    onOpenDetail: (dbId: String) -> Unit,
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val repo = container.customFoodDatabaseRepository
    val databases by repo.databases.collectAsState()

    var showAddMenu by remember { mutableStateOf(false) }
    var showUrlDialog by remember { mutableStateOf(false) }
    var showTextDialog by remember { mutableStateOf(false) }
    var showCreateEmptyDialog by remember { mutableStateOf(false) }
    var editingDbMeta by remember { mutableStateOf<CustomFoodDatabase?>(null) }
    var dbToDelete by remember { mutableStateOf<CustomFoodDatabase?>(null) }
    var isProcessing by remember { mutableStateOf(false) }

    // File picker launcher
    val filePicker = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        if (uri != null) {
            scope.launch {
                isProcessing = true
                try {
                    val content = withContext(Dispatchers.IO) {
                        context.contentResolver.openInputStream(uri)?.use { input ->
                            input.bufferedReader().readText()
                        } ?: ""
                    }
                    val fileName = uri.lastPathSegment?.substringAfterLast("/")?.substringBefore(".") ?: "Imported DB"
                    val (db, foods) = CustomFoodParser.parseContent(content, fileName)
                    repo.saveDatabase(db, foods)
                    Toast.makeText(context, context.getString(R.string.custom_food_db_items_count_format, foods.size), Toast.LENGTH_SHORT).show()
                } catch (e: Exception) {
                    Toast.makeText(context, "Error importing file: ${e.localizedMessage}", Toast.LENGTH_LONG).show()
                } finally {
                    isProcessing = false
                }
            }
        }
    }

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.custom_food_dbs_title)) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(
                            Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = stringResource(R.string.action_close)
                        )
                    }
                },
                actions = {
                    Box {
                        IconButton(onClick = { showAddMenu = true }) {
                            Icon(Icons.Default.Add, contentDescription = stringResource(R.string.custom_food_db_action_add))
                        }
                        DropdownMenu(
                            expanded = showAddMenu,
                            onDismissRequest = { showAddMenu = false }
                        ) {
                            DropdownMenuItem(
                                text = { Text(stringResource(R.string.custom_food_db_action_import_file)) },
                                leadingIcon = { Icon(Icons.Outlined.FolderOpen, contentDescription = null) },
                                onClick = {
                                    showAddMenu = false
                                    filePicker.launch("*/*")
                                }
                            )
                            DropdownMenuItem(
                                text = { Text(stringResource(R.string.custom_food_db_action_import_url)) },
                                leadingIcon = { Icon(Icons.Outlined.Link, contentDescription = null) },
                                onClick = {
                                    showAddMenu = false
                                    showUrlDialog = true
                                }
                            )
                            DropdownMenuItem(
                                text = { Text(stringResource(R.string.custom_food_db_action_import_text)) },
                                leadingIcon = { Icon(Icons.Outlined.ContentPaste, contentDescription = null) },
                                onClick = {
                                    showAddMenu = false
                                    showTextDialog = true
                                }
                            )
                            DropdownMenuItem(
                                text = { Text(stringResource(R.string.custom_food_db_action_create_empty)) },
                                leadingIcon = { Icon(Icons.Outlined.PostAdd, contentDescription = null) },
                                onClick = {
                                    showAddMenu = false
                                    showCreateEmptyDialog = true
                                }
                            )
                        }
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 16.dp, vertical = 12.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            Text(
                stringResource(R.string.custom_food_dbs_subtitle),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = AppTextOpacity.Muted),
            )

            if (isProcessing) {
                Card(
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier.padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        CircularProgressIndicator(modifier = Modifier.size(24.dp))
                        Text(stringResource(R.string.action_logging), style = MaterialTheme.typography.bodyMedium)
                    }
                }
            }

            if (databases.isEmpty()) {
                EmptyDatabasesCard(
                    onImportFile = { filePicker.launch("*/*") },
                    onImportUrl = { showUrlDialog = true },
                    onPasteText = { showTextDialog = true },
                    onCreateEmpty = { showCreateEmptyDialog = true }
                )
            } else {
                databases.forEach { db ->
                    DatabaseCard(
                        database = db,
                        onOpen = { onOpenDetail(db.id) },
                        onToggle = { enabled ->
                            scope.launch { repo.setDatabaseEnabled(db.id, enabled) }
                        },
                        onRefreshUrl = if (!db.sourceUrl.isNullOrBlank()) {
                            {
                                scope.launch {
                                    isProcessing = true
                                    try {
                                        repo.updateDatabaseFromUrl(db.id, FoodAnalysisService.defaultClient)
                                        Toast.makeText(context, context.getString(R.string.custom_food_db_last_synced_format, "Just now"), Toast.LENGTH_SHORT).show()
                                    } catch (e: Exception) {
                                        Toast.makeText(context, "Failed to refresh: ${e.localizedMessage}", Toast.LENGTH_LONG).show()
                                    } finally {
                                        isProcessing = false
                                    }
                                }
                            }
                        } else null,
                        onEditMeta = { editingDbMeta = db },
                        onExportCsv = {
                            scope.launch {
                                val foods = repo.getFoods(db.id)
                                val csv = CustomFoodExporter.exportToCsv(db, foods)
                                copyToClipboard(context, "${db.name}.csv", csv)
                                Toast.makeText(context, "CSV copied to clipboard", Toast.LENGTH_SHORT).show()
                            }
                        },
                        onExportJson = {
                            scope.launch {
                                val foods = repo.getFoods(db.id)
                                val jsonStr = CustomFoodExporter.exportToJson(db, foods)
                                copyToClipboard(context, "${db.name}.json", jsonStr)
                                Toast.makeText(context, "JSON copied to clipboard", Toast.LENGTH_SHORT).show()
                            }
                        },
                        onDelete = { dbToDelete = db }
                    )
                }
            }

            Spacer(Modifier.height(32.dp))
        }
    }

    // Dialogs
    if (showUrlDialog) {
        ImportUrlDialog(
            onDismiss = { showUrlDialog = false },
            onConfirm = { url, name ->
                showUrlDialog = false
                scope.launch {
                    isProcessing = true
                    try {
                        val (db, foods) = repo.downloadFromUrl(url, FoodAnalysisService.defaultClient, name.ifBlank { "URL Database" })
                        repo.saveDatabase(db, foods)
                        Toast.makeText(context, context.getString(R.string.custom_food_db_items_count_format, foods.size), Toast.LENGTH_SHORT).show()
                    } catch (e: Exception) {
                        Toast.makeText(context, "Error downloading URL: ${e.localizedMessage}", Toast.LENGTH_LONG).show()
                    } finally {
                        isProcessing = false
                    }
                }
            }
        )
    }

    if (showTextDialog) {
        ImportTextDialog(
            onDismiss = { showTextDialog = false },
            onConfirm = { text, name ->
                showTextDialog = false
                scope.launch {
                    isProcessing = true
                    try {
                        val (db, foods) = CustomFoodParser.parseContent(text, name.ifBlank { "Pasted DB" })
                        repo.saveDatabase(db, foods)
                        Toast.makeText(context, context.getString(R.string.custom_food_db_items_count_format, foods.size), Toast.LENGTH_SHORT).show()
                    } catch (e: Exception) {
                        Toast.makeText(context, "Error parsing text: ${e.localizedMessage}", Toast.LENGTH_LONG).show()
                    } finally {
                        isProcessing = false
                    }
                }
            }
        )
    }

    if (showCreateEmptyDialog) {
        CreateEmptyDbDialog(
            onDismiss = { showCreateEmptyDialog = false },
            onConfirm = { name, desc ->
                showCreateEmptyDialog = false
                scope.launch {
                    val db = CustomFoodDatabase(
                        name = name.ifBlank { "My Custom DB" },
                        description = desc,
                    )
                    repo.saveDatabase(db, emptyList())
                    onOpenDetail(db.id)
                }
            }
        )
    }

    editingDbMeta?.let { db ->
        EditDbMetaDialog(
            database = db,
            onDismiss = { editingDbMeta = null },
            onConfirm = { name, desc ->
                editingDbMeta = null
                scope.launch {
                    repo.updateDatabaseMeta(db.id, name, desc)
                }
            }
        )
    }

    dbToDelete?.let { db ->
        AlertDialog(
            onDismissRequest = { dbToDelete = null },
            title = { Text(stringResource(R.string.custom_food_db_confirm_delete_title)) },
            text = { Text(stringResource(R.string.custom_food_db_confirm_delete_desc, db.name)) },
            confirmButton = {
                TextButton(
                    onClick = {
                        val id = db.id
                        dbToDelete = null
                        scope.launch { repo.deleteDatabase(id) }
                    }
                ) {
                    Text(stringResource(R.string.action_delete), color = MaterialTheme.colorScheme.error)
                }
            },
            dismissButton = {
                TextButton(onClick = { dbToDelete = null }) {
                    Text(stringResource(R.string.action_cancel))
                }
            }
        )
    }
}

@Composable
private fun DatabaseCard(
    database: CustomFoodDatabase,
    onOpen: () -> Unit,
    onToggle: (Boolean) -> Unit,
    onRefreshUrl: (() -> Unit)?,
    onEditMeta: () -> Unit,
    onExportCsv: () -> Unit,
    onExportJson: () -> Unit,
    onDelete: () -> Unit,
) {
    var showMenu by remember { mutableStateOf(false) }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .clickable(onClick = onOpen),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    Icons.Outlined.Storage,
                    contentDescription = null,
                    tint = if (database.enabled) AppColors.Calorie else MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.size(24.dp)
                )
                Spacer(Modifier.width(10.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        database.name,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Text(
                        stringResource(R.string.custom_food_db_items_count_format, database.itemCount),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                if (onRefreshUrl != null) {
                    IconButton(onClick = onRefreshUrl, modifier = Modifier.size(36.dp)) {
                        Icon(
                            Icons.Outlined.Refresh,
                            contentDescription = stringResource(R.string.custom_food_db_action_refresh_url),
                            tint = AppColors.Calorie,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }

                Switch(
                    checked = database.enabled,
                    onCheckedChange = onToggle,
                    modifier = Modifier.padding(horizontal = 4.dp)
                )

                Box {
                    IconButton(onClick = { showMenu = true }, modifier = Modifier.size(36.dp)) {
                        Icon(Icons.Default.MoreVert, contentDescription = null, modifier = Modifier.size(20.dp))
                    }
                    DropdownMenu(
                        expanded = showMenu,
                        onDismissRequest = { showMenu = false }
                    ) {
                        DropdownMenuItem(
                            text = { Text(stringResource(R.string.custom_food_db_action_edit_meta)) },
                            leadingIcon = { Icon(Icons.Outlined.Edit, contentDescription = null) },
                            onClick = { showMenu = false; onEditMeta() }
                        )
                        if (onRefreshUrl != null) {
                            DropdownMenuItem(
                                text = { Text(stringResource(R.string.custom_food_db_action_refresh_url)) },
                                leadingIcon = { Icon(Icons.Outlined.Refresh, contentDescription = null) },
                                onClick = { showMenu = false; onRefreshUrl() }
                            )
                        }
                        DropdownMenuItem(
                            text = { Text(stringResource(R.string.custom_food_db_action_export_csv)) },
                            leadingIcon = { Icon(Icons.Outlined.FileDownload, contentDescription = null) },
                            onClick = { showMenu = false; onExportCsv() }
                        )
                        DropdownMenuItem(
                            text = { Text(stringResource(R.string.custom_food_db_action_export_json)) },
                            leadingIcon = { Icon(Icons.Outlined.Code, contentDescription = null) },
                            onClick = { showMenu = false; onExportJson() }
                        )
                        DropdownMenuItem(
                            text = { Text(stringResource(R.string.action_delete), color = MaterialTheme.colorScheme.error) },
                            leadingIcon = { Icon(Icons.Outlined.Delete, contentDescription = null, tint = MaterialTheme.colorScheme.error) },
                            onClick = { showMenu = false; onDelete() }
                        )
                    }
                }
            }

            if (database.description.isNotBlank()) {
                Text(
                    database.description,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
            }

            if (!database.sourceUrl.isNullOrBlank()) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Icon(
                        Icons.Outlined.Link,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(14.dp)
                    )
                    Text(
                        database.sourceUrl,
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.primary,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }

            database.lastSyncEpochMs?.let { lastSync ->
                val dateStr = SimpleDateFormat("dd.MM.yyyy HH:mm", Locale.getDefault()).format(Date(lastSync))
                Text(
                    stringResource(R.string.custom_food_db_last_synced_format, dateStr),
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
                )
            }
        }
    }
}

@Composable
private fun EmptyDatabasesCard(
    onImportFile: () -> Unit,
    onImportUrl: () -> Unit,
    onPasteText: () -> Unit,
    onCreateEmpty: () -> Unit,
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Icon(
                Icons.Outlined.Storage,
                contentDescription = null,
                tint = AppColors.Calorie,
                modifier = Modifier.size(48.dp)
            )
            Text(
                stringResource(R.string.custom_food_dbs_empty_title),
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
            Text(
                stringResource(R.string.custom_food_dbs_empty_desc),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(horizontal = 8.dp)
            )

            Button(
                onClick = onImportFile,
                colors = ButtonDefaults.buttonColors(containerColor = AppColors.Calorie),
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(stringResource(R.string.custom_food_db_action_import_file))
            }
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                TextButton(
                    onClick = onImportUrl,
                    modifier = Modifier.weight(1f)
                ) {
                    Icon(Icons.Outlined.Link, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(Modifier.width(4.dp))
                    Text("URL")
                }
                TextButton(
                    onClick = onPasteText,
                    modifier = Modifier.weight(1f)
                ) {
                    Icon(Icons.Outlined.ContentPaste, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(Modifier.width(4.dp))
                    Text(stringResource(R.string.custom_food_db_action_import_text))
                }
                TextButton(
                    onClick = onCreateEmpty,
                    modifier = Modifier.weight(1f)
                ) {
                    Icon(Icons.Outlined.PostAdd, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(Modifier.width(4.dp))
                    Text(stringResource(R.string.custom_food_db_action_create_empty))
                }
            }
        }
    }
}

@Composable
private fun ImportUrlDialog(
    onDismiss: () -> Unit,
    onConfirm: (url: String, name: String) -> Unit,
) {
    var url by remember { mutableStateOf("") }
    var name by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(stringResource(R.string.custom_food_db_action_import_url)) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                OutlinedTextField(
                    value = url,
                    onValueChange = { url = it },
                    label = { Text(stringResource(R.string.custom_food_db_url_label)) },
                    placeholder = { Text(stringResource(R.string.custom_food_db_url_placeholder)) },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text(stringResource(R.string.custom_food_db_name_label)) },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                Text(
                    stringResource(R.string.custom_food_db_security_notice),
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        },
        confirmButton = {
            TextButton(
                enabled = url.isNotBlank(),
                onClick = { onConfirm(url, name) }
            ) {
                Text(stringResource(R.string.action_continue))
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(stringResource(R.string.action_cancel))
            }
        }
    )
}

@Composable
private fun ImportTextDialog(
    onDismiss: () -> Unit,
    onConfirm: (text: String, name: String) -> Unit,
) {
    var text by remember { mutableStateOf("") }
    var name by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(stringResource(R.string.custom_food_db_action_import_text)) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text(stringResource(R.string.custom_food_db_name_label)) },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = text,
                    onValueChange = { text = it },
                    label = { Text(stringResource(R.string.custom_food_db_action_import_text)) },
                    placeholder = { Text(stringResource(R.string.custom_food_db_text_import_placeholder)) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(180.dp)
                )
            }
        },
        confirmButton = {
            TextButton(
                enabled = text.isNotBlank(),
                onClick = { onConfirm(text, name) }
            ) {
                Text(stringResource(R.string.action_done))
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(stringResource(R.string.action_cancel))
            }
        }
    )
}

@Composable
private fun CreateEmptyDbDialog(
    onDismiss: () -> Unit,
    onConfirm: (name: String, desc: String) -> Unit,
) {
    var name by remember { mutableStateOf("") }
    var desc by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(stringResource(R.string.custom_food_db_action_create_empty)) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text(stringResource(R.string.custom_food_db_name_label)) },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = desc,
                    onValueChange = { desc = it },
                    label = { Text(stringResource(R.string.custom_food_db_desc_label)) },
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            TextButton(
                enabled = name.isNotBlank(),
                onClick = { onConfirm(name, desc) }
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

@Composable
private fun EditDbMetaDialog(
    database: CustomFoodDatabase,
    onDismiss: () -> Unit,
    onConfirm: (name: String, desc: String) -> Unit,
) {
    var name by remember { mutableStateOf(database.name) }
    var desc by remember { mutableStateOf(database.description) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(stringResource(R.string.custom_food_db_action_edit_meta)) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text(stringResource(R.string.custom_food_db_name_label)) },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = desc,
                    onValueChange = { desc = it },
                    label = { Text(stringResource(R.string.custom_food_db_desc_label)) },
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            TextButton(
                enabled = name.isNotBlank(),
                onClick = { onConfirm(name, desc) }
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
