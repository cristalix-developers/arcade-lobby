
data class Screen(val text: List<String>) {

    var buttons: List<Button>? = null

    fun buttons(vararg buttons: Button?): Screen {
        this.buttons = buttons.filterNotNull().toList()
        return this
    }
}