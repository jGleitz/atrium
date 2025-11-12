package ch.tutteli.atrium.reporting.erroradjusters.impl

import ch.tutteli.atrium.core.polyfills.PlatformStackBacktraceEntry
import ch.tutteli.atrium.reporting.erroradjusters.RemoveRunnerFromAtriumError

actual class RemoveRunnerFromAtriumErrorImpl : RemoveRunnerFromAtriumError {
    actual override fun adjust(stackBacktrace: Sequence<PlatformStackBacktraceEntry>) = stackBacktrace.takeWhile {
        !it.className.run {
            startsWith("org.junit") ||
                    startsWith("org.jetbrains.spek") ||
                    startsWith("org.spekframework.spek2") ||
                    startsWith("io.kotest") ||
                    startsWith("org.testng") ||
                    startsWith("io.kotlintest")
        }
    }
}
