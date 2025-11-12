package ch.tutteli.atrium.reporting.erroradjusters

import ch.tutteli.atrium.creating.ComponentFactoryContainer
import ch.tutteli.atrium.reporting.AtriumError
import ch.tutteli.atrium.reporting.StackBacktraceAdjuster

/**
 * Responsible to remove the stacktrace of the test runner(s) from an [AtriumError].
 *
 * It is a marker interface so that one can [ComponentFactoryContainer.buildOrNull] an implementation of
 * a [StackBacktraceAdjuster] with the desired behaviour.
 */
interface RemoveRunnerFromAtriumError : StackBacktraceAdjuster
