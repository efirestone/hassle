package hassemble.core

data class Credentials(
    var name: String = "Home Assistant",
    var host: String,
    var port: Int,
    var accessToken: String,
    var isSecure: Boolean
)
