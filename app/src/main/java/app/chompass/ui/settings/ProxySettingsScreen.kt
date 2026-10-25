package app.chompass.ui.settings

import android.widget.Toast
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.CheckCircle
import androidx.compose.material.icons.outlined.ErrorOutline
import androidx.compose.material.icons.outlined.Http
import androidx.compose.material.icons.outlined.Lock
import androidx.compose.material.icons.outlined.NetworkCheck
import androidx.compose.material.icons.outlined.Route
import androidx.compose.material.icons.outlined.Security
import androidx.compose.material.icons.outlined.Visibility
import androidx.compose.material.icons.outlined.VisibilityOff
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import app.chompass.AppContainer
import app.chompass.R
import app.chompass.data.proxyConfig
import app.chompass.data.setProxyConfig
import app.chompass.network.ProxyConfig
import app.chompass.network.ProxyManager
import app.chompass.network.ProxyType
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

@Composable
fun ProxySettingsScreen(
    container: AppContainer,
    onBack: () -> Unit,
    from: String = "",
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    var enabled by remember { mutableStateOf(false) }
    var proxyType by remember { mutableStateOf(ProxyType.HTTP) }
    var host by remember { mutableStateOf("") }
    var portText by remember { mutableStateOf("8080") }
    var username by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var showPassword by remember { mutableStateOf(false) }

    var proxyAiApi by remember { mutableStateOf(true) }
    var proxyFoodDb by remember { mutableStateOf(true) }
    var proxySync by remember { mutableStateOf(true) }

    var exclusionList by remember { mutableStateOf(ProxyConfig.DEFAULT_EXCLUSIONS) }

    var isTesting by remember { mutableStateOf(false) }
    var testResult by remember { mutableStateOf<Result<Long>?>(null) }

    LaunchedEffect(Unit) {
        val cfg = container.prefs.proxyConfig.first()
        enabled = cfg.enabled
        proxyType = cfg.type
        host = cfg.host
        portText = cfg.port.toString()
        username = cfg.username
        password = cfg.password
        proxyAiApi = cfg.proxyAiApiEnabled
        proxyFoodDb = cfg.proxyFoodDbEnabled
        proxySync = cfg.proxySyncEnabled
        exclusionList = cfg.exclusionList
    }

    fun buildConfig(): ProxyConfig {
        val parsedPort = portText.toIntOrNull()?.coerceIn(1, 65535) ?: 8080
        return ProxyConfig(
            enabled = enabled,
            type = proxyType,
            host = host.trim(),
            port = parsedPort,
            username = username.trim(),
            password = password,
            proxyAiApiEnabled = proxyAiApi,
            proxyFoodDbEnabled = proxyFoodDb,
            proxySyncEnabled = proxySync,
            exclusionList = exclusionList
        )
    }

    fun saveConfig() {
        val cfg = buildConfig()
        scope.launch {
            container.prefs.setProxyConfig(cfg)
            ProxyManager.updateConfig(cfg)
            Toast.makeText(context, context.getString(R.string.proxy_saved), Toast.LENGTH_SHORT).show()
        }
    }

    SettingsSubScreen(
        title = stringResource(R.string.settings_proxy_title),
        onBack = onBack,
        backLabel = settingsBackLabel(from),
    ) {
        Column(
            verticalArrangement = Arrangement.spacedBy(16.dp),
            modifier = Modifier.padding(bottom = 24.dp)
        ) {
            // Summary Card / Main Switch
            Card(
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = stringResource(R.string.proxy_enable),
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.SemiBold
                        )
                        Text(
                            text = stringResource(R.string.settings_proxy_summary),
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    Switch(
                        checked = enabled,
                        onCheckedChange = {
                            enabled = it
                            saveConfig()
                        }
                    )
                }
            }

            // Server Configuration Card
            Card(
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Text(
                        text = stringResource(R.string.proxy_type),
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        ProxyType.entries.forEach { type ->
                            val label = when (type) {
                                ProxyType.HTTP -> stringResource(R.string.proxy_type_http)
                                ProxyType.SOCKS4 -> stringResource(R.string.proxy_type_socks4)
                                ProxyType.SOCKS5 -> stringResource(R.string.proxy_type_socks5)
                            }
                            val isSelected = proxyType == type
                            Row(
                                modifier = Modifier
                                    .weight(1f)
                                    .selectable(
                                        selected = isSelected,
                                        onClick = { proxyType = type }
                                    )
                                    .padding(vertical = 4.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                RadioButton(
                                    selected = isSelected,
                                    onClick = { proxyType = type }
                                )
                                Text(
                                    text = label,
                                    fontSize = 14.sp,
                                    modifier = Modifier.padding(start = 2.dp)
                                )
                            }
                        }
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        OutlinedTextField(
                            value = host,
                            onValueChange = { host = it },
                            label = { Text(stringResource(R.string.proxy_host)) },
                            placeholder = { Text("192.168.1.1 or proxy.org") },
                            singleLine = true,
                            leadingIcon = { Icon(Icons.Outlined.Http, contentDescription = null) },
                            modifier = Modifier.weight(2f)
                        )

                        OutlinedTextField(
                            value = portText,
                            onValueChange = { portText = it.filter { c -> c.isDigit() } },
                            label = { Text(stringResource(R.string.proxy_port)) },
                            placeholder = { Text("8080") },
                            singleLine = true,
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            modifier = Modifier.weight(1f)
                        )
                    }
                }
            }

            // Authentication Card
            Card(
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Text(
                        text = stringResource(R.string.proxy_auth_title),
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold
                    )

                    OutlinedTextField(
                        value = username,
                        onValueChange = { username = it },
                        label = { Text(stringResource(R.string.proxy_username)) },
                        singleLine = true,
                        leadingIcon = { Icon(Icons.Outlined.Security, contentDescription = null) },
                        modifier = Modifier.fillMaxWidth()
                    )

                    OutlinedTextField(
                        value = password,
                        onValueChange = { password = it },
                        label = { Text(stringResource(R.string.proxy_password)) },
                        singleLine = true,
                        leadingIcon = { Icon(Icons.Outlined.Lock, contentDescription = null) },
                        trailingIcon = {
                            IconButton(onClick = { showPassword = !showPassword }) {
                                Icon(
                                    imageVector = if (showPassword) Icons.Outlined.VisibilityOff else Icons.Outlined.Visibility,
                                    contentDescription = if (showPassword) "Hide password" else "Show password"
                                )
                            }
                        },
                        visualTransformation = if (showPassword) VisualTransformation.None else PasswordVisualTransformation(),
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }

            // Target Scopes Routing Card
            Card(
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        text = stringResource(R.string.proxy_scopes_title),
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold
                    )
                    Text(
                        text = stringResource(R.string.proxy_scopes_subtitle),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Checkbox(
                            checked = proxyAiApi,
                            onCheckedChange = { proxyAiApi = it }
                        )
                        Text(
                            text = stringResource(R.string.proxy_scope_ai),
                            style = MaterialTheme.typography.bodyMedium,
                            modifier = Modifier.padding(start = 4.dp)
                        )
                    }

                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Checkbox(
                            checked = proxyFoodDb,
                            onCheckedChange = { proxyFoodDb = it }
                        )
                        Text(
                            text = stringResource(R.string.proxy_scope_food_db),
                            style = MaterialTheme.typography.bodyMedium,
                            modifier = Modifier.padding(start = 4.dp)
                        )
                    }

                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Checkbox(
                            checked = proxySync,
                            onCheckedChange = { proxySync = it }
                        )
                        Text(
                            text = stringResource(R.string.proxy_scope_sync),
                            style = MaterialTheme.typography.bodyMedium,
                            modifier = Modifier.padding(start = 4.dp)
                        )
                    }
                }
            }

            // Exclusion List (No Proxy for) Card
            Card(
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        text = stringResource(R.string.proxy_exclusion_title),
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold
                    )
                    Text(
                        text = stringResource(R.string.proxy_exclusion_subtitle),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    OutlinedTextField(
                        value = exclusionList,
                        onValueChange = { exclusionList = it },
                        minLines = 3,
                        maxLines = 5,
                        modifier = Modifier.fillMaxWidth(),
                        leadingIcon = { Icon(Icons.Outlined.Route, contentDescription = null) }
                    )
                }
            }

            // Connection Test & Result Section
            testResult?.let { res ->
                val (isSuccess, text, color) = res.fold(
                    onSuccess = { latency ->
                        Triple(true, stringResource(R.string.proxy_test_success, latency), MaterialTheme.colorScheme.primary)
                    },
                    onFailure = { err ->
                        Triple(false, stringResource(R.string.proxy_test_failed, err.localizedMessage ?: err.toString()), MaterialTheme.colorScheme.error)
                    }
                )

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 4.dp)
                ) {
                    Icon(
                        imageVector = if (isSuccess) Icons.Outlined.CheckCircle else Icons.Outlined.ErrorOutline,
                        contentDescription = null,
                        tint = color,
                        modifier = Modifier.padding(end = 8.dp)
                    )
                    Text(
                        text = text,
                        style = MaterialTheme.typography.bodyMedium,
                        color = color
                    )
                }
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                OutlinedButton(
                    onClick = {
                        val testCfg = buildConfig()
                        scope.launch {
                            isTesting = true
                            testResult = null
                            testResult = ProxyManager.testConnection(testCfg)
                            isTesting = false
                        }
                    },
                    enabled = !isTesting && host.isNotBlank(),
                    modifier = Modifier.weight(1f)
                ) {
                    if (isTesting) {
                        CircularProgressIndicator(
                            strokeWidth = 2.dp,
                            modifier = Modifier
                                .height(18.dp)
                                .width(18.dp)
                                .padding(end = 6.dp)
                        )
                        Text(stringResource(R.string.proxy_testing), fontSize = 12.sp)
                    } else {
                        Icon(
                            Icons.Outlined.NetworkCheck,
                            contentDescription = null,
                            modifier = Modifier.padding(end = 6.dp)
                        )
                        Text(stringResource(R.string.proxy_test_connection))
                    }
                }

                Button(
                    onClick = { saveConfig() },
                    modifier = Modifier.weight(1f)
                ) {
                    Text(stringResource(R.string.action_save))
                }
            }
        }
    }
}
