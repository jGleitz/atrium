package ch.tutteli.atrium.reporting

import ch.tutteli.atrium.core.polyfills.PlatformStackBacktraceEntry

/**
 * Responsible for adjusting the [ch.tutteli.atrium.core.polyfills.stackBacktrace] of a [Throwable].
 * Typically applied to an [AtriumError] and its [Throwable.cause] (and its `suppressed` on the JVM).
 *
 * @see AtriumErrorAdjuster.forStackBacktraceAdjuster to create an [AtriumErrorAdjuster] applying interface.
 */
interface StackBacktraceAdjuster {
    fun adjust(stackBacktrace: Sequence<PlatformStackBacktraceEntry>): Sequence<PlatformStackBacktraceEntry>
    fun then(next: StackBacktraceAdjuster): StackBacktraceAdjuster = object : StackBacktraceAdjuster {
        override fun adjust(stackBacktrace: Sequence<PlatformStackBacktraceEntry>) =
            this@StackBacktraceAdjuster.adjust(stackBacktrace).let { next.adjust(it) }
    }

    object NoOp : StackBacktraceAdjuster {
        override fun adjust(stackBacktrace: Sequence<PlatformStackBacktraceEntry>) = stackBacktrace
    }
}