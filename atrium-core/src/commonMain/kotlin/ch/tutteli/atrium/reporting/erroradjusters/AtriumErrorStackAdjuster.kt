package ch.tutteli.atrium.reporting.erroradjusters

import ch.tutteli.atrium.reporting.AtriumErrorAdjuster
import ch.tutteli.atrium.reporting.StackBacktraceAdjuster

/**
 * Applies [StackBacktraceAdjuster]s.
 */
internal expect class AtriumErrorStackAdjuster(stackBacktraceAdjuster: StackBacktraceAdjuster) : AtriumErrorAdjuster {
    override fun adjust(throwable: Throwable): Throwable
}