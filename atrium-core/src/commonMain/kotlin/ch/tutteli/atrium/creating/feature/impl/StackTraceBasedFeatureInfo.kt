package ch.tutteli.atrium.creating.feature.impl

import ch.tutteli.atrium.core.polyfills.stackBacktrace
import ch.tutteli.atrium.creating.feature.ExperimentalFeatureInfo
import ch.tutteli.atrium.creating.feature.FeatureInfo

@ExperimentalFeatureInfo
class StackTraceBasedFeatureInfo() : FeatureInfo {
    override fun <T, R> determine(extractor: T.() -> R, stacksToDrop: Int): String {
        val stackTraces = Exception().stackBacktrace
        val index = stacksToDrop + 1
        require(index < stackTraces.size) {
            "dropping $stacksToDrop stacks is not possible as there are only ${stackTraces.size} stacktraces available"
        }

        val stackTrace = stackTraces[index]
        val location = (stackTrace.fileName ?: "<unknown file>") +
                (stackTrace.lineNumber?.let { ":$it" } ?: "") +
                (stackTrace.columnNumber?.let { ":$it" } ?: "")
        return "its.definedIn($location)"
    }
}

