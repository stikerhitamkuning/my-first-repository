class CommandSpec(
    val inputSize: Int,
    val isRequireLogin: Boolean,
    val action: (List<String>) -> Unit
)