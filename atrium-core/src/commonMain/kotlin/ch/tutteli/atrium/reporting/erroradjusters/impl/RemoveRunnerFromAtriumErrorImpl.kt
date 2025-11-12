package ch.tutteli.atrium.reporting.erroradjusters.impl

import ch.tutteli.atrium.core.polyfills.PlatformStackBacktraceEntry
import ch.tutteli.atrium.reporting.erroradjusters.RemoveRunnerFromAtriumError

expect class RemoveRunnerFromAtriumErrorImpl() : RemoveRunnerFromAtriumError {
    override fun adjust(stackBacktrace: Sequence<PlatformStackBacktraceEntry>): Sequence<PlatformStackBacktraceEntry>
}
