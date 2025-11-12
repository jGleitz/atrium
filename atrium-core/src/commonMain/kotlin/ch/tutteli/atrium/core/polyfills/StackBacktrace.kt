package ch.tutteli.atrium.core.polyfills

/**
 * Returns the stack trace of [this] [Throwable] as a [List] of [PlatformStackBacktraceEntry]s.
 */
// TODO this calculated property can get called multiple times for one exception and is
//  potentially expensive to compute. Look into caching it.
expect val Throwable.stackBacktrace: List<PlatformStackBacktraceEntry>

/**
 * Represents an entry in the stack trace of a [Throwable].
 *
 * Notice that there is no cross-platform specification defining how stack trace lines
 * are represented (formatted) and whether they contain a line number and/or a column
 * number. This is platform-specific. This interface will extract information on a
 * best-effort basis.
 *
 * Please open a feature request if you would like to rely on a specified representation:
 * https://github.com/robstoll/atrium/issues/new?template=feature_request.md
 */
interface StackBacktraceEntry {
    /**
     * The name (i.e. base name plus extension) of the code file at which stack entry points.
     *
     * For example:
     *  * `Factories.java`
     *  * `MyClass.kt`
     *  * `index.js`
     */
    val fileName: String?

    /**
     * The (1-based) line number in the code file at which this stack entry points.
     */
    val lineNumber: Int?

    /**
     * The (0-based) column number in the code file at which this stack entry points.
     */
    val columnNumber: Int?

    /**
     * A normalized representation of this entry, which has comparable formatting
     * across platforms as far as possible.
     *
     * The target format is:
     *
     * ```
     * <method> (<filePath>:<lineNumber>:<columnNumber>)
     * ```
     */
    val normalizedLine: String
}

/**
 * The platform-specific version of [StackBacktraceEntry], which might expose additional data only available on
 * some platforms.
 */
expect interface PlatformStackBacktraceEntry: StackBacktraceEntry