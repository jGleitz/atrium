package ch.tutteli.atrium.reporting.erroradjusters.impl

import ch.tutteli.atrium.core.polyfills.PlatformStackBacktraceEntry
import ch.tutteli.atrium.reporting.erroradjusters.RemoveAtriumFromAtriumError

expect class RemoveAtriumFromAtriumErrorImpl() : RemoveAtriumFromAtriumError {
    override fun adjust(stackBacktrace: Sequence<PlatformStackBacktraceEntry>): Sequence<PlatformStackBacktraceEntry>
}
