package ch.tutteli.atrium.reporting.erroradjusters

import ch.tutteli.atrium.api.infix.en_GB.*
import ch.tutteli.atrium.api.verbs.internal.expect
import ch.tutteli.atrium.core.ExperimentalNewExpectTypes
import ch.tutteli.atrium.core.polyfills.StackBacktraceEntry
import ch.tutteli.atrium.core.polyfills.stackBacktrace
import ch.tutteli.atrium.creating.ComponentFactoryContainer
import ch.tutteli.atrium.creating.Expect
import ch.tutteli.atrium.creating.ExperimentalComponentFactoryContainer
import ch.tutteli.atrium.creating.build
import ch.tutteli.atrium.logic._logic
import ch.tutteli.atrium.logic.creating.RootExpectBuilder
import ch.tutteli.atrium.reporting.AtriumErrorAdjuster
import ch.tutteli.atrium.reporting.StackBacktraceAdjuster
import kotlin.test.Test

class AdjustStackTest {

    @OptIn(ExperimentalWithOptions::class, ExperimentalComponentFactoryContainer::class)
    private fun <T> expectWithNoOpErrorAdjuster(subject: T) =
        expect(subject).withOptions {
            withComponent(AtriumErrorAdjuster::class ) { AtriumErrorAdjuster.NoOp }
        }

    @Test
    fun noOp_containsMochaAndAtrium() {
        expectWithNoOpErrorAdjuster {
            assertNoOp(1) toEqual 2
        }.toThrow<AssertionError> {
            feature(AssertionError::stackBacktrace) toContain entries(
                { feature(StackBacktraceEntry::normalizedLine) toContain Regex("""[\\|/]node_modules[\\|/]mocha[\\|/]""") },
                { feature(StackBacktraceEntry::normalizedLine) toContain "createAtriumError" }
            )
        }
    }

    @Test
    fun noOp_makeSureStackBacktraceIsInOutput_issue1383() {
        expectWithNoOpErrorAdjuster {
            expectWithNoOpErrorAdjuster {
                assertNoOp(1) toEqual 2
            }.toThrow<AssertionError> {
                feature(AssertionError::stackBacktrace) toContain entries(
                    { feature(StackBacktraceEntry::normalizedLine) toContain "createAtriumError2" }
                )
            }
        }.toThrow<AssertionError> {
            it messageToContain "stackBacktrace"
        }
    }

    @Test
    fun removeRunner_containsAtriumButNotMocha() {
        expectWithNoOpErrorAdjuster {
            assertRemoveRunner(1) toEqual 2
        }.toThrow<AssertionError> {
            it feature of(AssertionError::stackBacktrace) {
                it notToContain o entry { feature(StackBacktraceEntry::normalizedLine) toContain "mocha" }
                it notToContain o entry { feature(StackBacktraceEntry::normalizedLine) toContain "KotlinTestTeamCityConsoleAdapter" }
                it toContain { feature(StackBacktraceEntry::normalizedLine) toContain "createAtriumError" }
            }
        }
    }

    @Test
    fun removeRunner_containsAtriumButNotMochaInCause() {
        assertRemoveRunner {
            throw IllegalArgumentException("hello", UnsupportedOperationException("world"))
        }.toThrow<IllegalArgumentException> {
            cause<UnsupportedOperationException> {
                it feature of(UnsupportedOperationException::stackBacktrace) {
                    it notToContain o entry { feature(StackBacktraceEntry::normalizedLine) toContain "mocha" }
                    it notToContain o entry { feature(StackBacktraceEntry::normalizedLine) toContain "KotlinTestTeamCityConsoleAdapter" }
                    it toContain { feature(StackBacktraceEntry::normalizedLine) toContain Regex("""atrium[\\|/].*[\\|/]src[\\|/].*[\\|/]ch[\\|/]tutteli[\\|/]atrium""") }
                }
            }
        }
    }

    @Test
    fun removeAtrium_containsMochaButNotAtrium() {
        expectWithNoOpErrorAdjuster {
            assertRemoveAtrium(1) toEqual 2
        }.toThrow<AssertionError> {
            it feature of(AssertionError::stackBacktrace) {
                it toContain { feature(StackBacktraceEntry::normalizedLine) toContain "mocha" }
                it notToContain { it toContain Regex("""atrium[\\|/].*[\\|/]src[\\|/].*[\\|/]ch[\\|/]tutteli[\\|/]atrium""") }
            }
        }
    }

    @ExperimentalComponentFactoryContainer
    @Test
    fun removeAtrium_containsMochaButNotAtriumInCause() {
        assertRemoveAtrium {
            throw IllegalArgumentException("hello", UnsupportedOperationException("world"))
        }.toThrow<IllegalArgumentException> {
            cause<UnsupportedOperationException> {
                it feature of(UnsupportedOperationException::stackBacktrace) {
                    it toContain { feature(StackBacktraceEntry::normalizedLine) toContain "mocha" }
                    it notToContain { it toContain Regex("""atrium[\\|/].*[\\|/]src[\\|/].*[\\|/]ch[\\|/]tutteli[\\|/]atrium""") }
                }
            }
        }
    }

    @OptIn(ExperimentalNewExpectTypes::class, ExperimentalComponentFactoryContainer::class)
    private fun <T : Any> assertNoOp(subject: T) = createExpect(subject) { StackBacktraceAdjuster.NoOp }

    @OptIn(ExperimentalNewExpectTypes::class, ExperimentalComponentFactoryContainer::class)
    private fun <T : Any> assertRemoveRunner(subject: T) =
        createExpect(subject) { c -> c.build<RemoveRunnerFromAtriumError>() }

    @OptIn(ExperimentalNewExpectTypes::class, ExperimentalComponentFactoryContainer::class)
    private fun <T : Any> assertRemoveAtrium(subject: T) =
        createExpect(subject) { c -> c.build<RemoveAtriumFromAtriumError>() }


    @ExperimentalNewExpectTypes
    @ExperimentalComponentFactoryContainer
    private fun <T : Any> createExpect(subject: T, factory: (ComponentFactoryContainer) -> StackBacktraceAdjuster) =
        RootExpectBuilder.forSubject(subject)
            .withVerb("I expected subject")
            .withOptions {
                withComponent(StackBacktraceAdjuster::class, factory)
            }
            .build()
}
