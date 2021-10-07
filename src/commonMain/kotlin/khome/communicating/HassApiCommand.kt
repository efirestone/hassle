package khome.communicating

internal interface HassApiCommand<SD> {
    val type: CommandType
    var id: Int?
}
