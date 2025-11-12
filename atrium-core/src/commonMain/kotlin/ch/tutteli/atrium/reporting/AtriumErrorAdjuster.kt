package ch.tutteli.atrium.reporting

import ch.tutteli.atrium.reporting.erroradjusters.AtriumErrorStackAdjuster


//TODO move to package errorAdjusters with 1.3.0
/**
 * Adjusts a [Throwable] (usually an [AtriumError]) for improved reporting.
 */
interface AtriumErrorAdjuster {

    /**
     * Adjusts the given [throwable]. Usually, the [throwable] is an [AtriumError]. However, any
     * [Throwable] can be passed.
     *
     * @see StackBacktraceAdjuster if only the stack trace of [throwable] needs to be adjusted.
     */
    fun adjust(throwable: Throwable): Throwable

    fun then(next: AtriumErrorAdjuster): AtriumErrorAdjuster = object : AtriumErrorAdjuster {
        override fun adjust(throwable: Throwable) = this@AtriumErrorAdjuster.adjust(throwable)
            .let { next.adjust(it) }
    }

    companion object {
        fun forStackBacktraceAdjuster(stackBacktraceAdjuster: StackBacktraceAdjuster): AtriumErrorAdjuster =
            if (stackBacktraceAdjuster === StackBacktraceAdjuster.NoOp) NoOp
            else AtriumErrorStackAdjuster(stackBacktraceAdjuster)
    }

    object NoOp : AtriumErrorAdjuster {
        override fun adjust(throwable: Throwable) = throwable
    }
}
