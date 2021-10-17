package hassemble.core

data class Credentials(
    var name: String,
    var host: String,
    var port: Int,
    var accessToken: String,
    var isSecure: Boolean
)
