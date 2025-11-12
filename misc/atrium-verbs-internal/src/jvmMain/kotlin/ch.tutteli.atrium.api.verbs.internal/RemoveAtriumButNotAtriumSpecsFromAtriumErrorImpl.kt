package ch.tutteli.atrium.api.verbs.internal

import ch.tutteli.atrium.core.polyfills.PlatformStackBacktraceEntry
import ch.tutteli.atrium.reporting.erroradjusters.RemoveAtriumFromAtriumError

actual class RemoveAtriumButNotAtriumSpecsFromAtriumErrorImpl : RemoveAtriumFromAtriumError {
    actual override fun adjust(stackBacktrace: Sequence<PlatformStackBacktraceEntry>) = stackBacktrace.filterNot {
        it.className.startsWith("ch.tutteli.atrium") && !it.className.startsWith("ch.tutteli.atrium.specs")
    }
}
