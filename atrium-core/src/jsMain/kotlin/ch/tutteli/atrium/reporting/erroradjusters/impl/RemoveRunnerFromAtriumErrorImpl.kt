package ch.tutteli.atrium.reporting.erroradjusters.impl

import ch.tutteli.atrium.core.polyfills.PlatformStackBacktraceEntry
import ch.tutteli.atrium.reporting.erroradjusters.RemoveRunnerFromAtriumError
import ch.tutteli.atrium.reporting.erroradjusters.impl.PathPatternBuilder.Companion.pathPattern

actual class RemoveRunnerFromAtriumErrorImpl : RemoveRunnerFromAtriumError {
    actual override fun adjust(stackBacktrace: Sequence<PlatformStackBacktraceEntry>) = stackBacktrace.filterNot {
        runnerRegex.containsMatchIn(it.normalizedLine)
    }

    companion object {
        private val runnerRegex = pathPattern {
            oneOf(
                // kotlin 1.6
                { dir("packages_imported").dir("kotlin-test-js-runner") },
                // kotlin 1.3
                { dir("node_modules").dir("(?:mocha|jasmine|jest)") },
                // IntelliJ-specific
                { dir("node_modules").dir("src").file("KotlinTestTeamCityConsoleAdapter\\.ts") }
            )
        }
    }
}
