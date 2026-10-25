package app.chompass.data

import androidx.datastore.preferences.core.edit
import app.chompass.network.ProxyConfig
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

val PreferencesStore.proxyConfig: Flow<ProxyConfig>
    get() = dataStore.data.map { prefs ->
        val raw = prefs[Keys.PROXY_CONFIG]
        if (raw.isNullOrBlank()) {
            ProxyConfig()
        } else {
            runCatching {
                json.decodeFromString(ProxyConfig.serializer(), raw)
            }.getOrDefault(ProxyConfig())
        }
    }

suspend fun PreferencesStore.setProxyConfig(config: ProxyConfig) {
    dataStore.edit { prefs ->
        prefs[Keys.PROXY_CONFIG] = json.encodeToString(ProxyConfig.serializer(), config)
    }
}
