package ch.tutteli.atrium.api.verbs.internal

import ch.tutteli.atrium.core.polyfills.PlatformStackBacktraceEntry
import ch.tutteli.atrium.reporting.erroradjusters.RemoveAtriumFromAtriumError
import ch.tutteli.atrium.reporting.erroradjusters.impl.PathPatternBuilder.Companion.pathPattern
import ch.tutteli.atrium.reporting.erroradjusters.impl.RemoveAtriumFromAtriumErrorImpl

actual class RemoveAtriumButNotAtriumSpecsFromAtriumErrorImpl : RemoveAtriumFromAtriumError {
    actual override fun adjust(stackBacktrace: Sequence<PlatformStackBacktraceEntry>) = stackBacktrace.filterNot {
        atriumRegex.containsMatchIn(it.normalizedLine)
    }

    private companion object {
        private val atriumRegex = pathPattern(RemoveAtriumFromAtriumErrorImpl.atriumRegex.pattern) {
            notFollowedByDir("specs")
        }
    }
}
