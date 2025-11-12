package ch.tutteli.atrium.reporting.erroradjusters

import ch.tutteli.atrium.core.polyfills.JS_STACK_LINE_START
import ch.tutteli.atrium.core.polyfills.stackBacktrace
import ch.tutteli.atrium.reporting.AtriumErrorAdjuster
import ch.tutteli.atrium.reporting.StackBacktraceAdjuster

internal actual class AtriumErrorStackAdjuster actual constructor(private val stackBacktraceAdjuster: StackBacktraceAdjuster) :
    AtriumErrorAdjuster {
    actual override fun adjust(throwable: Throwable): Throwable {
        val adjustedStackBacktrace = stackBacktraceAdjuster.adjust(throwable.stackBacktrace.asSequence())
            .joinToString("\n") { JS_STACK_LINE_START + it.normalizedLine }
        throwable.asDynamic().stack = "${throwable::class.simpleName}: ${throwable.message}\n$adjustedStackBacktrace"
        throwable.cause?.let { adjust(it) }
        return throwable
    }
}