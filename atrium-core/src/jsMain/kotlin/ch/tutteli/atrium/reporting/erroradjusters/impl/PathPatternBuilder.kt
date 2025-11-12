package ch.tutteli.atrium.reporting.erroradjusters.impl

class PathPatternBuilder private constructor(baseRegex: String) {
    private val resultPattern = StringBuilder(baseRegex)

    /**
     * Appends the result pattern to expect a directory matching [pattern] next.
     */
    fun dir(pattern: String) = apply {
        resultPattern.append(pattern).append(PATH_SEPARATOR)
    }

    /**
     * Appends the result pattern to expect a directory matching [pattern] next.
     */
    fun file(pattern: String) = apply {
        resultPattern.append(pattern)
    }

    /**
     * Appends the result pattern to allow, but not require, a directory matching the [pattern] next.
     */
    fun optionalDir(pattern: String) = apply {
        resultPattern.append("(?:").append(pattern).append(PATH_SEPARATOR).append(")?")
    }

    /**
     * Appends the result pattern to not match if the next directory matches [pattern].
     */
    fun notFollowedByDir(pattern: String) = apply {
        resultPattern.append("(?!").append(pattern).append(PATH_SEPARATOR).append(")")
    }

    fun oneOf( vararg patternCreator: PathPatternBuilder.() -> Unit) = apply {
        resultPattern.append("(?:")
        patternCreator.forEachIndexed { index, creator ->
            if (index > 0) resultPattern.append("|")
            creator()
        }
        resultPattern.append(")")
    }

    private fun build(): Regex = Regex(resultPattern.toString())

    companion object {
        fun pathPattern(baseRegex: String = PATH_SEPARATOR, patternCreator: PathPatternBuilder.() -> Unit): Regex =
            PathPatternBuilder(baseRegex).apply(patternCreator).build()

        // language=RegExp
        const val PATH_SEPARATOR = """[/\\]"""
    }
}