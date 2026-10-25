package app.chompass.network

import kotlinx.serialization.Serializable

@Serializable
enum class ProxyType {
    HTTP,
    SOCKS4,
    SOCKS5
}

@Serializable
enum class ProxyScope {
    AI_API,
    FOOD_DB,
    SYNC
}

@Serializable
data class ProxyConfig(
    val enabled: Boolean = false,
    val type: ProxyType = ProxyType.HTTP,
    val host: String = "",
    val port: Int = 8080,
    val username: String = "",
    val password: String = "",
    val proxyAiApiEnabled: Boolean = true,
    val proxyFoodDbEnabled: Boolean = true,
    val proxySyncEnabled: Boolean = true,
    val exclusionList: String = DEFAULT_EXCLUSIONS
) {
    companion object {
        const val DEFAULT_EXCLUSIONS = "localhost, 127.0.0.1, .local, 192.168.0.0/16, 10.0.0.0/8"
    }
}
