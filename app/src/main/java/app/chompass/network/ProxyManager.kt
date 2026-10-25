package app.chompass.network

import android.util.Log
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.withContext
import okhttp3.Authenticator
import okhttp3.Credentials
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.Response
import okhttp3.Route
import java.io.IOException
import java.net.InetSocketAddress
import java.net.PasswordAuthentication
import java.net.Proxy
import java.net.ProxySelector
import java.net.SocketAddress
import java.net.URI
import java.util.concurrent.TimeUnit

object ProxyExclusionEngine {

    /**
     * Parse single 32-bit IPv4 string into Long. Returns null if invalid.
     */
    fun ipToLong(ip: String): Long? {
        val parts = ip.trim().split(".")
        if (parts.size != 4) return null
        var result = 0L
        for (part in parts) {
            val num = part.toIntOrNull() ?: return null
            if (num !in 0..255) return null
            result = (result shl 8) or num.toLong()
        }
        return result
    }

    /**
     * Check if an IPv4 address string matches a CIDR range (e.g. 192.168.0.0/16).
     */
    fun matchCidr(ipStr: String, cidrStr: String): Boolean {
        val cidrParts = cidrStr.trim().split("/")
        if (cidrParts.size != 2) return false
        val subnetIp = ipToLong(cidrParts[0]) ?: return false
        val prefixLen = cidrParts[1].trim().toIntOrNull() ?: return false
        if (prefixLen !in 0..32) return false
        val targetIp = ipToLong(ipStr) ?: return false

        val mask = if (prefixLen == 0) 0L else (0xFFFFFFFFL shl (32 - prefixLen)) and 0xFFFFFFFFL
        return (subnetIp and mask) == (targetIp and mask)
    }

    /**
     * Check whether [host] is excluded based on the Firefox-style [exclusionRules] string.
     */
    fun isExcluded(host: String, exclusionRules: String): Boolean {
        if (host.isBlank() || exclusionRules.isBlank()) return false
        val cleanHost = host.trim().lowercase()

        val rules = exclusionRules.split(",", ";", "\n", " ")
            .map { it.trim().lowercase() }
            .filter { it.isNotEmpty() }

        for (rule in rules) {
            if (rule == cleanHost) return true

            // Wildcard or domain suffix match (.example.com or *.example.com)
            val domainPattern = when {
                rule.startsWith("*.") -> rule.substring(1) // -> .example.com
                rule.startsWith(".") -> rule              // -> .example.com
                else -> null
            }

            if (domainPattern != null) {
                val bareDomain = domainPattern.substring(1)
                if (cleanHost == bareDomain || cleanHost.endsWith(domainPattern)) {
                    return true
                }
            }

            // CIDR subnet matching
            if (rule.contains("/")) {
                if (matchCidr(cleanHost, rule)) {
                    return true
                }
            }
        }

        return false
    }
}

object ProxyScopeClassifier {

    /**
     * Classifies a URI into one of the proxy target scopes.
     */
    fun determineScope(uri: URI, host: String): ProxyScope {
        val lowerHost = host.lowercase()
        val path = (uri.path ?: "").lowercase()

        return when {
            // AI APIs & Speech
            lowerHost.contains("openai.com") ||
            lowerHost.contains("anthropic.com") ||
            lowerHost.contains("googleapis.com") ||
            lowerHost.contains("openrouter.ai") ||
            lowerHost.contains("ollama") ||
            path.contains("/v1/chat") ||
            path.contains("/v1/audio") -> ProxyScope.AI_API

            // Food databases
            lowerHost.contains("openfoodfacts.org") ||
            lowerHost.contains("usda.gov") ||
            lowerHost.contains("food") -> ProxyScope.FOOD_DB

            // Sync, WebDAV, App updates
            lowerHost.contains("github.com") ||
            lowerHost.contains("githubusercontent.com") ||
            lowerHost.contains("mealie") ||
            lowerHost.contains("webdav") -> ProxyScope.SYNC

            else -> ProxyScope.FOOD_DB
        }
    }
}

class AppProxySelector(
    private val configProvider: () -> ProxyConfig
) : ProxySelector() {

    override fun select(uri: URI?): List<Proxy> {
        if (uri == null) return listOf(Proxy.NO_PROXY)
        val config = configProvider()

        if (!config.enabled || config.host.isBlank() || config.port <= 0) {
            return listOf(Proxy.NO_PROXY)
        }

        val host = uri.host ?: return listOf(Proxy.NO_PROXY)

        // 1. Check exclusion list
        if (ProxyExclusionEngine.isExcluded(host, config.exclusionList)) {
            return listOf(Proxy.NO_PROXY)
        }

        // 2. Check scope routing
        val scope = ProxyScopeClassifier.determineScope(uri, host)
        val isScopeEnabled = when (scope) {
            ProxyScope.AI_API -> config.proxyAiApiEnabled
            ProxyScope.FOOD_DB -> config.proxyFoodDbEnabled
            ProxyScope.SYNC -> config.proxySyncEnabled
        }

        if (!isScopeEnabled) {
            return listOf(Proxy.NO_PROXY)
        }

        // 3. Resolve Java Proxy.Type
        val type = when (config.type) {
            ProxyType.HTTP -> Proxy.Type.HTTP
            ProxyType.SOCKS4, ProxyType.SOCKS5 -> Proxy.Type.SOCKS
        }

        val address = InetSocketAddress.createUnresolved(config.host, config.port)
        return listOf(Proxy(type, address))
    }

    override fun connectFailed(uri: URI?, sa: SocketAddress?, ioe: IOException?) {
        Log.w("AppProxySelector", "Proxy connection failed for URI $uri via $sa: ${ioe?.message}")
    }
}

class AppProxyAuthenticator(
    private val configProvider: () -> ProxyConfig
) : Authenticator {

    override fun authenticate(route: Route?, response: Response): Request? {
        val config = configProvider()
        if (!config.enabled || config.username.isBlank()) return null

        if (response.code == 407) {
            val credential = Credentials.basic(config.username, config.password)
            return response.request.newBuilder()
                .header("Proxy-Authorization", credential)
                .build()
        }
        return null
    }
}

object ProxyManager {

    private val _configFlow = MutableStateFlow(ProxyConfig())
    val configFlow: StateFlow<ProxyConfig> = _configFlow.asStateFlow()

    fun currentConfig(): ProxyConfig = _configFlow.value

    fun updateConfig(config: ProxyConfig) {
        _configFlow.value = config
    }

    val proxySelector by lazy { AppProxySelector { currentConfig() } }
    val proxyAuthenticator by lazy { AppProxyAuthenticator { currentConfig() } }

    private var initialized = false

    fun initGlobalProxySelector() {
        if (initialized) return
        initialized = true

        // Register default JVM ProxySelector
        ProxySelector.setDefault(proxySelector)

        // Register JVM Authenticator for SOCKS/URLConnections
        java.net.Authenticator.setDefault(object : java.net.Authenticator() {
            override fun getPasswordAuthentication(): PasswordAuthentication? {
                val cfg = currentConfig()
                if (cfg.enabled && cfg.username.isNotBlank() && requestingPrompt == "SOCKS authentication") {
                    return PasswordAuthentication(cfg.username, cfg.password.toCharArray())
                }
                return null
            }
        })
    }

    /**
     * Test connectivity through the configured proxy to a test URL.
     */
    suspend fun testConnection(
        testConfig: ProxyConfig,
        testUrl: String = "https://1.1.1.1"
    ): Result<Long> = withContext(Dispatchers.IO) {
        if (testConfig.host.isBlank() || testConfig.port <= 0) {
            return@withContext Result.failure(IllegalArgumentException("Host and port required"))
        }

        val startTime = System.currentTimeMillis()
        try {
            val proxyType = when (testConfig.type) {
                ProxyType.HTTP -> Proxy.Type.HTTP
                ProxyType.SOCKS4, ProxyType.SOCKS5 -> Proxy.Type.SOCKS
            }
            val proxy = Proxy(proxyType, InetSocketAddress(testConfig.host, testConfig.port))

            val builder = OkHttpClient.Builder()
                .connectTimeout(10, TimeUnit.SECONDS)
                .readTimeout(10, TimeUnit.SECONDS)
                .proxy(proxy)

            if (testConfig.username.isNotBlank()) {
                builder.proxyAuthenticator { _, response ->
                    if (response.code == 407) {
                        val credential = Credentials.basic(testConfig.username, testConfig.password)
                        response.request.newBuilder().header("Proxy-Authorization", credential).build()
                    } else null
                }
            }

            val client = builder.build()
            val request = Request.Builder()
                .url(testUrl)
                .head()
                .build()

            client.newCall(request).execute().use { response ->
                val latency = System.currentTimeMillis() - startTime
                if (response.isSuccessful || response.code == 404 || response.code == 405) {
                    Result.success(latency)
                } else {
                    Result.failure(IOException("Proxy returned HTTP code: ${response.code}"))
                }
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
