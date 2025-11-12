package ch.tutteli.atrium.reporting.erroradjusters.impl

import ch.tutteli.atrium.core.polyfills.PlatformStackBacktraceEntry
import ch.tutteli.atrium.reporting.erroradjusters.RemoveAtriumFromAtriumError
import ch.tutteli.atrium.reporting.erroradjusters.impl.PathPatternBuilder.Companion.pathPattern

actual class RemoveAtriumFromAtriumErrorImpl : RemoveAtriumFromAtriumError {
    actual override fun adjust(stackBacktrace: Sequence<PlatformStackBacktraceEntry>) = stackBacktrace.filterNot {
        atriumRegex.containsMatchIn(it.normalizedLine)
    }

    companion object {
        val atriumRegex = pathPattern {
            // since Kotlin 1.4 -- writes the src/ path as if the atrium file was in the same project
            // (see also https://youtrack.jetbrains.com/issue/KT-64220/KJS-IR-Stacktrace-should-not-contain-project-path-when-the-library-was-built)
            dir("src")
                .optionalDir("generated")
                .dir("(?:common|js)Main")
                .optionalDir("kotlin")
                .dir("ch")
                .dir("tutteli")
                .dir("atrium")
        }
    }
}
