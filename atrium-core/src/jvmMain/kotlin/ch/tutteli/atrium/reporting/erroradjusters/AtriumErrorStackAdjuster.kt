package ch.tutteli.atrium.reporting.erroradjusters

import ch.tutteli.atrium.core.polyfills.stackBacktrace
import ch.tutteli.atrium.reporting.AtriumErrorAdjuster
import ch.tutteli.atrium.reporting.StackBacktraceAdjuster

internal actual class AtriumErrorStackAdjuster actual constructor(private val stackBacktraceAdjuster: StackBacktraceAdjuster) :
    AtriumErrorAdjuster {
    actual override fun adjust(throwable: Throwable): Throwable {
        val adjustedStackTrace = stackBacktraceAdjuster.adjust(throwable.stackBacktrace.asSequence())
            .map { it.jvmElement }
            .toList()
            .toTypedArray()
        throwable.stackTrace = adjustedStackTrace
        throwable.cause?.let { adjust(it) }
        throwable.suppressed.forEach { adjust(it) }
        return throwable
    }
}