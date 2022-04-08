data class Action(var type: String) {

    constructor(actions: Actions) : this(actions.name.lowercase())

    var screen: Screen? = null
    var command: String? = null

    fun screen(screen: Screen?): Action {
        this.screen = screen
        return this
    }

    @JvmName("command1")
    fun command(command: String): Action {
        this.command = command
        return this
    }
}