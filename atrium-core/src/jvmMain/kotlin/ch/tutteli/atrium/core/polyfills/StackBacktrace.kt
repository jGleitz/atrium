package ch.tutteli.atrium.core.polyfills

actual val Throwable.stackBacktrace: List<PlatformStackBacktraceEntry> get() = this.stackTrace.map(::JvmStackBacktraceEntry)

actual interface PlatformStackBacktraceEntry: StackBacktraceEntry {
    val jvmElement: StackTraceElement

    /**
     * The fully qualified name of the class this stack entry point to.
     *
     * For example:
     *  * `java.lang.String`
     *  * `ch.tutteli.atrium.creating.AssertKtTest`
     */
    val className: String
}

// TODO consider making this an inline value class once we use Kotlin >=v1.5
//  this might require using an expect class to actually save object instantiation
data class JvmStackBacktraceEntry(override val jvmElement: StackTraceElement) : PlatformStackBacktraceEntry {
    override val fileName: String? get() = jvmElement.fileName
    override val lineNumber: Int? get() = jvmElement.lineNumber.takeIf { it >= 0 }
    override val columnNumber: Int? get() = null
    override val normalizedLine: String get() = jvmElement.toString()
    override val className: String get() = jvmElement.className
}
