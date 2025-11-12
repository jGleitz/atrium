package ch.tutteli.atrium.reporting.erroradjusters

import ch.tutteli.atrium.api.infix.en_GB.*
import ch.tutteli.atrium.api.verbs.internal.expect
import ch.tutteli.atrium.core.ExperimentalNewExpectTypes
import ch.tutteli.atrium.core.polyfills.PlatformStackBacktraceEntry
import ch.tutteli.atrium.core.polyfills.StackBacktraceEntry
import ch.tutteli.atrium.core.polyfills.stackBacktrace
import ch.tutteli.atrium.creating.ComponentFactoryContainer
import ch.tutteli.atrium.creating.Expect
import ch.tutteli.atrium.creating.ExperimentalComponentFactoryContainer
import ch.tutteli.atrium.creating.build
import ch.tutteli.atrium.logic._logic
import ch.tutteli.atrium.logic.creating.RootExpectBuilder
import ch.tutteli.atrium.logic.utils.expectLambda
import ch.tutteli.atrium.reporting.AtriumErrorAdjuster
import ch.tutteli.atrium.reporting.StackBacktraceAdjuster
import org.spekframework.spek2.Spek
import org.spekframework.spek2.style.specification.describe

@OptIn(ExperimentalWithOptions::class)
@ExperimentalNewExpectTypes
@ExperimentalComponentFactoryContainer
class AdjustStackSpec : Spek({

    fun <T> expectWithNoOpErrorAdjuster(subject: T) =
        expect(subject).withOptions {
            withComponent(AtriumErrorAdjuster::class) { AtriumErrorAdjuster.NoOp }
        }

    fun <T> expectWithNoOpErrorAdjuster(subject: T, assertionCreator: Expect<T>.() -> Unit): Expect<T> =
        expectWithNoOpErrorAdjuster(subject)._logic.appendAsGroup(assertionCreator)

    describe("no-op adjuster") {
        fun <T : Any> assertNoOp(subject: T) =
            createExpect(subject) { StackBacktraceAdjuster.NoOp }

        it("contains spek, junit, atrium.creating and atrium.reporting") {
            expectWithNoOpErrorAdjuster {
                assertNoOp(1) toEqual 2
            }.toThrow<AssertionError> {
                feature { f(it::stackBacktrace) } toContain entries(
                    { feature(StackBacktraceEntry::normalizedLine) toStartWith "org.spekframework.spek2" },
                    { feature(StackBacktraceEntry::normalizedLine) toStartWith "ch.tutteli.atrium.creating" },
                    { feature(StackBacktraceEntry::normalizedLine) toStartWith "ch.tutteli.atrium.reporting" }
                )
            }
        }
    }

    fun mapNormalizedLineStartsWith(list: List<String>): Pair<Expect<PlatformStackBacktraceEntry>.() -> Unit, Array<out Expect<PlatformStackBacktraceEntry>.() -> Unit>> {
        val asserts = list.map { c ->
            expectLambda<PlatformStackBacktraceEntry> {
                feature(StackBacktraceEntry::normalizedLine) toStartWith (c)
            }
        }
        return asserts.first() to asserts.drop(1).toTypedArray()
    }

    mapOf<String, Triple<(ComponentFactoryContainer) -> StackBacktraceAdjuster, List<String>, List<String>>>(
        "remove test runner adjuster" to Triple(
            { c -> c.build<RemoveRunnerFromAtriumError>() },
            listOf("org.spekframework.spek2", "kotlin.coroutines", "kotlinx.coroutines"),
            listOf("ch.tutteli.atrium")
        ),
        "remove atrium adjuster" to Triple(
            { c -> c.build<RemoveAtriumFromAtriumError>() },
            listOf("ch.tutteli.atrium"),
            listOf("org.spekframework.spek2")
        )
    ).forEach { (description, triple) ->
        val (factory, containsNot, contains) = triple
        val (containsNotFirst, containsNotRest) = mapNormalizedLineStartsWith(containsNot)
        val (containsFirst, containsRest) = mapNormalizedLineStartsWith(contains)
        describe(description) {
            it("does not contain $containsNot in stackBacktrace but $contains") {
                expectWithNoOpErrorAdjuster {
                    createExpect(1, factory) toEqual 2
                }.toThrow<AssertionError> {
                    feature { f(it::stackBacktrace) } and {
                        it notToContain o the entries(containsNotFirst, *containsNotRest)
                        it toContain entries(containsFirst, *containsRest)
                    }
                }
            }


            it("does not contain $containsNot in stackBacktrace of cause, but $contains") {
                val throwable = IllegalArgumentException("hello", UnsupportedOperationException("world"))
                val adjuster = createExpect(1, factory)._logic.components.build<AtriumErrorAdjuster>()
                adjuster.adjust(throwable)
                expectWithNoOpErrorAdjuster(throwable.cause!!.stackBacktrace) {
                    it notToContain o the entries(containsNotFirst, *containsNotRest)
                    it toContain entries(containsFirst, *containsRest)
                }
            }

            it("does not contain $containsNot in stackBacktrace of cause of cause, but $contains") {
                val throwable = IllegalArgumentException(
                    "hello",
                    UnsupportedOperationException("world", IllegalStateException("and good night"))
                )
                val adjuster = createExpect(1, factory)._logic.components.build<AtriumErrorAdjuster>()
                adjuster.adjust(throwable)
                expectWithNoOpErrorAdjuster(throwable.cause!!.cause!!.stackBacktrace) {
                    it notToContain o the entries(containsNotFirst, *containsNotRest)
                    it toContain entries(containsFirst, *containsRest)
                }
            }

            it("does not contain $containsNot in stackBacktrace of suppressed exception, but $contains") {
                val throwable1 = IllegalArgumentException("hello", UnsupportedOperationException("world"))
                val throwable2 = IllegalArgumentException("hello", UnsupportedOperationException("world"))
                val throwable = IllegalStateException("with suppressed")
                throwable.addSuppressed(throwable1)
                throwable.addSuppressed(throwable2)
                val adjuster = createExpect(1, factory)._logic.components.build<AtriumErrorAdjuster>()
                adjuster.adjust(throwable)
                (expectWithNoOpErrorAdjuster(throwable.suppressed) asList o).toHaveElementsAndAll(fun Expect<Throwable>.() {
                    feature { f(it::stackBacktrace) } and {
                        it notToContain o the entries(containsNotFirst, *containsNotRest)
                        it toContain entries(containsFirst, *containsRest)
                    }
                })
            }

            it("does not contain $containsNot in stackBacktrace of cause of suppressed exception, but $contains") {
                val throwable1 = IllegalArgumentException("hello", UnsupportedOperationException("world"))
                val throwable2 = IllegalArgumentException("hello", UnsupportedOperationException("world"))
                val throwable = IllegalStateException("with suppressed")
                throwable.addSuppressed(throwable1)
                throwable.addSuppressed(throwable2)
                val adjuster = createExpect(1, factory)._logic.components.build<AtriumErrorAdjuster>()
                adjuster.adjust(throwable)
                (expectWithNoOpErrorAdjuster(throwable.suppressed) asList o).toHaveElementsAndAll(fun Expect<Throwable>.() {
                    cause<UnsupportedOperationException> {
                        feature { f(it::stackBacktrace) } and {
                            it notToContain o the entries(containsNotFirst, *containsNotRest)
                            it toContain entries(containsFirst, *containsRest)
                        }
                    }
                })
            }
        }
    }

    mapOf<String, (ComponentFactoryContainer) -> StackBacktraceAdjuster>(
        "combine remove runner adjuster and remove atrium adjuster" to { c ->
            c.build<RemoveRunnerFromAtriumError>()
                .then(c.build<RemoveAtriumFromAtriumError>())
        },
        "combine remove atrium adjuster and remove runner adjuster" to { c ->
            c.build<RemoveAtriumFromAtriumError>()
                .then(c.build<RemoveRunnerFromAtriumError>())
        },
        "combine remove atrium adjuster and remove runner adjuster several times" to { c ->
            c.build<RemoveAtriumFromAtriumError>()
                .then(c.build<RemoveRunnerFromAtriumError>())
                .then(c.build<RemoveRunnerFromAtriumError>())
                .then(c.build<RemoveAtriumFromAtriumError>())
        }
    ).forEach { (description, factory) ->
        describe(description) {
            it("stackBacktrace is empty as we filter out everything") {
                expectWithNoOpErrorAdjuster {
                    createExpect(1, factory) toEqual 2
                }.toThrow<AssertionError> {
                    it feature { f(it::stackBacktrace) } toBe empty
                }
            }

            it("stackBacktrace of cause is empty as we filter out everything") {
                val throwable = IllegalArgumentException("hello", UnsupportedOperationException("world"))
                val adjuster = createExpect(1, factory)._logic.components.build<AtriumErrorAdjuster>()
                adjuster.adjust(throwable)
                expectWithNoOpErrorAdjuster(throwable.cause!!.stackBacktrace) toBe empty
            }

            it("stackBacktrace of suppressed is empty as we filter out everything") {
                val throwable1 = IllegalArgumentException("hello", UnsupportedOperationException("world"))
                val throwable2 = IllegalArgumentException("hello", UnsupportedOperationException("world"))
                val throwable = IllegalStateException("with suppressed")
                throwable.addSuppressed(throwable1)
                throwable.addSuppressed(throwable2)
                val adjuster = createExpect(1, factory)._logic.components.build<AtriumErrorAdjuster>()
                adjuster.adjust(throwable)
                (expectWithNoOpErrorAdjuster(throwable.suppressed) asList o).toHaveElementsAndAll(fun Expect<Throwable>.() {
                    it feature { f(it::stackBacktrace) } toBe empty
                })
            }
        }
    }
})

@ExperimentalNewExpectTypes
@ExperimentalComponentFactoryContainer
private fun <T : Any> createExpect(subject: T, factory: (ComponentFactoryContainer) -> StackBacktraceAdjuster) =
    RootExpectBuilder.forSubject(subject)
        .withVerb("I expected subject")
        .withOptions {
            withComponent(StackBacktraceAdjuster::class, factory)
        }
        .build()
