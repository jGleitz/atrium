package ch.tutteli.atrium.core.polyfills

import ch.tutteli.atrium.api.infix.en_GB.*
import ch.tutteli.atrium.api.verbs.internal.expect
import kotlin.test.Test

class ThrowableStackTest {

    @Test
    fun illegalStateException() {
        val stack = IllegalStateException("test").stackBacktrace
        expect(stack) {
            it get 0 feature (StackBacktraceEntry::normalizedLine) toStartWith "${ThrowableStackTest::class.simpleName}.${ThrowableStackTest::illegalStateException.name}"
            it notToContain o the entries(
                { feature(StackBacktraceEntry::normalizedLine) toContain "init" },
                { feature(StackBacktraceEntry::normalizedLine) toContain "[as constructor]" }
            )

            toHaveElementsAndAny {
                feature(StackBacktraceEntry::normalizedLine) toContain "mocha"
            }
        }
    }

    @Test
    fun assertionError() {
        val stack = AssertionError("test").stackBacktrace
        expect(stack) {
            it get 0 feature (StackBacktraceEntry::normalizedLine) toStartWith "${ThrowableStackTest::class.simpleName}.${ThrowableStackTest::assertionError.name}"
            it toHaveElementsAndNone {
                feature(StackBacktraceEntry::normalizedLine) toContain "init"
            }
            it toHaveElementsAndAny {
                feature(StackBacktraceEntry::normalizedLine) toContain "mocha"
            }
        }
    }

    @Test
    fun removesMethodMangling() {
        val error = AssertionError("test")
        // it’s not clear whether method mangling will be applied in all versions of Kotlin on all systems,
        // so we set a stack containing mangled methods by hand.
        error.asDynamic().stack = """
            AtriumError: test
                at <global>.createAtriumError (/home/josh/Projekte/atrium/atrium-core/build/compileSync/js/test/testDevelopmentExecutable/kotlin/src/kotlin/util/Standard.kt:41:200)
                at Companion_47.protoOf.create_xhqqab (/home/josh/Projekte/atrium/atrium-core/src/jsMain/kotlin/ch/tutteli/atrium/reporting/AtriumError.kt:64:60)
                at RootExpectImpl.protoOf.append_5gehtn (/home/josh/Projekte/atrium/atrium-core/src/commonMain/kotlin/ch/tutteli/atrium/creating/impl/RootExpectImpl.kt:67:42)
                at DelegatingExpectImpl.protoOf.append_5gehtn (/home/josh/Projekte/atrium/atrium-core/src/commonMain/kotlin/ch/tutteli/atrium/creating/impl/DelegatingExpectImpl.kt:18:42)
                at DefaultSubjectChanger.protoOf.reported_fhs1ue (/home/josh/Projekte/atrium/logic/atrium-logic/src/commonMain/kotlin/ch/tutteli/atrium/logic/creating/transformers/impl/DefaultSubjectChanger.kt:59:42)
                at <global>.transformIt (/home/josh/Projekte/atrium/logic/atrium-logic/src/commonMain/kotlin/ch/tutteli/atrium/logic/creating/transformers/impl/subjectchanger/defaultImpls.kt:77:42)
                at ExecutionStepImpl_0.action (/home/josh/Projekte/atrium/logic/atrium-logic/src/commonMain/kotlin/ch/tutteli/atrium/logic/creating/transformers/impl/subjectchanger/defaultImpls.kt:72:42)
                at ExecutionStepImpl_0.protoOf.transform_heqauk (/home/josh/Projekte/atrium/logic/atrium-logic/src/commonMain/kotlin/ch/tutteli/atrium/logic/creating/transformers/impl/BaseTransformationExecutionStep.kt:57:42)
                at AdjustStackTest.protoOf.removeAtrium_containsMochaButNotAtriumInCause_25o79a (/home/josh/Projekte/atrium/apis/infix/atrium-api-infix/src/commonMain/kotlin/ch/tutteli/atrium/api/infix/en_GB/anyExpectations.kt:134:42)
                at <global>.fn (kotlin/atrium-atrium-core-test.js:3110:42)
        """.trimIndent()
        expect(error.stackBacktrace.map { it.normalizedLine }) toContainExactly values(
            "<global>.createAtriumError (/home/josh/Projekte/atrium/atrium-core/build/compileSync/js/test/testDevelopmentExecutable/kotlin/src/kotlin/util/Standard.kt:41:200)",
            "Companion_47.create (/home/josh/Projekte/atrium/atrium-core/src/jsMain/kotlin/ch/tutteli/atrium/reporting/AtriumError.kt:64:60)",
            "RootExpectImpl.append (/home/josh/Projekte/atrium/atrium-core/src/commonMain/kotlin/ch/tutteli/atrium/creating/impl/RootExpectImpl.kt:67:42)",
            "DelegatingExpectImpl.append (/home/josh/Projekte/atrium/atrium-core/src/commonMain/kotlin/ch/tutteli/atrium/creating/impl/DelegatingExpectImpl.kt:18:42)",
            "DefaultSubjectChanger.reported (/home/josh/Projekte/atrium/logic/atrium-logic/src/commonMain/kotlin/ch/tutteli/atrium/logic/creating/transformers/impl/DefaultSubjectChanger.kt:59:42)",
            "<global>.transformIt (/home/josh/Projekte/atrium/logic/atrium-logic/src/commonMain/kotlin/ch/tutteli/atrium/logic/creating/transformers/impl/subjectchanger/defaultImpls.kt:77:42)",
            "ExecutionStepImpl_0.action (/home/josh/Projekte/atrium/logic/atrium-logic/src/commonMain/kotlin/ch/tutteli/atrium/logic/creating/transformers/impl/subjectchanger/defaultImpls.kt:72:42)",
            "ExecutionStepImpl_0.transform (/home/josh/Projekte/atrium/logic/atrium-logic/src/commonMain/kotlin/ch/tutteli/atrium/logic/creating/transformers/impl/BaseTransformationExecutionStep.kt:57:42)",
            "AdjustStackTest.removeAtrium_containsMochaButNotAtriumInCause (/home/josh/Projekte/atrium/apis/infix/atrium-api-infix/src/commonMain/kotlin/ch/tutteli/atrium/api/infix/en_GB/anyExpectations.kt:134:42)",
            "<global>.fn (kotlin/atrium-atrium-core-test.js:3110:42)"
        )
    }

    @Test
    fun addsFakeColumnToCutAwayWhenDetectingTeamCityReporter() {
        val error = AssertionError("test")
        error.asDynamic().stack = """
            AtriumError: test
                at <global>.createAtriumError (/home/josh/Projekte/atrium/atrium-core/build/compileSync/js/test/testDevelopmentExecutable/kotlin/src/kotlin/util/Standard.kt:41:200)
                at Context.<anonymous> (/home/josh/Projekte/atrium/build/js/packages_imported/kotlin-test-js-runner/src/KotlinTestTeamCityConsoleAdapter.ts:71:42)
                at <global>.processImmediate (node:internal/timers:478:42)
        """.trimIndent()
        expect(error.stackBacktrace.map { it.normalizedLine }) toContainExactly values(
            "<global>.createAtriumError (/home/josh/Projekte/atrium/atrium-core/build/compileSync/js/test/testDevelopmentExecutable/kotlin/src/kotlin/util/Standard.kt:41:200:0)",
            "Context.<anonymous> (/home/josh/Projekte/atrium/build/js/packages_imported/kotlin-test-js-runner/src/KotlinTestTeamCityConsoleAdapter.ts:71:42:0)",
            "<global>.processImmediate (node:internal/timers:478:42)"
        )
    }

    @Test
    fun prefixesFileIfNoFunction() {
        val error = AssertionError("test")
        error.asDynamic().stack = """
            AtriumError: test
                at /home/josh/Projekte/atrium/logic/atrium-logic/src/commonMain/kotlin/ch/tutteli/atrium/logic/impl/DefaultFun0Assertions.kt:50:19
                at /home/josh/Projekte/atrium/logic/atrium-logic/src/commonMain/kotlin/ch/tutteli/atrium/logic/impl/DefaultFeatureAssertions.kt:70:43
                at /home/josh/Projekte/atrium/apis/infix/atrium-api-infix/build/compileSync/js/test/testDevelopmentExecutable/kotlin/src/kotlin/util/Standard.kt:144:16
                at /home/josh/Projekte/atrium/misc/atrium-specs/src/commonMain/kotlin/ch/tutteli/atrium/specs/integration/AbstractFun0ExpectationsTest.kt:121:15
                at /home/josh/Projekte/atrium/build/js/node_modules/mocha/lib/runner.js:800:12
                at /home/josh/Projekte/atrium/build/js/node_modules/mocha/lib/runner.js:602:7
        """.trimIndent()
        expect(error.stackBacktrace.map { it.normalizedLine }) toContainExactly values(
            "DefaultFun0Assertions.kt (/home/josh/Projekte/atrium/logic/atrium-logic/src/commonMain/kotlin/ch/tutteli/atrium/logic/impl/DefaultFun0Assertions.kt:50:19)",
            "DefaultFeatureAssertions.kt (/home/josh/Projekte/atrium/logic/atrium-logic/src/commonMain/kotlin/ch/tutteli/atrium/logic/impl/DefaultFeatureAssertions.kt:70:43)",
            "Standard.kt (/home/josh/Projekte/atrium/apis/infix/atrium-api-infix/build/compileSync/js/test/testDevelopmentExecutable/kotlin/src/kotlin/util/Standard.kt:144:16)",
            "AbstractFun0ExpectationsTest.kt (/home/josh/Projekte/atrium/misc/atrium-specs/src/commonMain/kotlin/ch/tutteli/atrium/specs/integration/AbstractFun0ExpectationsTest.kt:121:15)",
            "runner.js (/home/josh/Projekte/atrium/build/js/node_modules/mocha/lib/runner.js:800:12)",
            "runner.js (/home/josh/Projekte/atrium/build/js/node_modules/mocha/lib/runner.js:602:7)"
        )
    }

    @Test
    fun skipsInit0() {
        val error = AssertionError("failing on purpose")
        error.asDynamic().stack = """
        AssertionError: failing on purpose
            at AssertionError_init_0 (D:\projects\atrium\core\api\atrium-core-api-js\build\node_modules\kotlin.js:24991:22)
            at ThrowableStackTest.illegalStateException (D:\projects\atrium\core\api\atrium-core-api-js\build\classes\kotlin\test\atrium-core-api-js_test.js:731:13)
        """.trimIndent()
        expect(error.stackBacktrace.map { it.normalizedLine }) toContainExactly values(
            """ThrowableStackTest.illegalStateException (D:\projects\atrium\core\api\atrium-core-api-js\build\classes\kotlin\test\atrium-core-api-js_test.js:731:13)""",
        )
    }

    @Test
    fun properties_KotlinFile_mangledMethod() {
        val error = AssertionError("failing on purpose")
        error.asDynamic().stack = """
        AssertionError: failing on purpose
            at RootExpectImpl.protoOf.append_5gehtn (/home/josh/Projekte/atrium/atrium-core/src/commonMain/kotlin/ch/tutteli/atrium/creating/impl/RootExpectImpl.kt:67:42)
        """.trimIndent()
        expect(error.stackBacktrace) toContainExactly JsStackBacktraceEntry(
            fileName = "RootExpectImpl.kt",
            filePath = "/home/josh/Projekte/atrium/atrium-core/src/commonMain/kotlin/ch/tutteli/atrium/creating/impl/RootExpectImpl.kt",
            lineNumber = 67,
            columnNumber = 42,
            normalizedLine = "RootExpectImpl.append (/home/josh/Projekte/atrium/atrium-core/src/commonMain/kotlin/ch/tutteli/atrium/creating/impl/RootExpectImpl.kt:67:42)"
        )
    }

    @Test
    fun properties_TypescriptFile_addFakeColumn() {
        val error = AssertionError("failing on purpose")
        error.asDynamic().stack = """
        AssertionError: failing on purpose
            at Context.<anonymous> (/home/josh/Projekte/atrium/build/js/packages_imported/kotlin-test-js-runner/src/KotlinTestTeamCityConsoleAdapter.ts:71:42)
        """.trimIndent()
        expect(error.stackBacktrace) toContainExactly JsStackBacktraceEntry(
            fileName = "KotlinTestTeamCityConsoleAdapter.ts",
            filePath = "/home/josh/Projekte/atrium/build/js/packages_imported/kotlin-test-js-runner/src/KotlinTestTeamCityConsoleAdapter.ts",
            lineNumber = 71,
            columnNumber = 42,
            normalizedLine = "Context.<anonymous> (/home/josh/Projekte/atrium/build/js/packages_imported/kotlin-test-js-runner/src/KotlinTestTeamCityConsoleAdapter.ts:71:42:0)"
        )
    }

    @Test
    fun properties_JavascriptFile_functionLooksLikeFile() {
        val error = AssertionError("failing on purpose")
        error.asDynamic().stack = """
        AssertionError: failing on purpose
            at mean.kt (/home/me/projects/test/theRealFile.js)
        """.trimIndent()
        expect(error.stackBacktrace) toContainExactly JsStackBacktraceEntry(
            fileName = "theRealFile.js",
            filePath = "/home/me/projects/test/theRealFile.js",
            lineNumber = null,
            columnNumber = null,
            normalizedLine = "mean.kt (/home/me/projects/test/theRealFile.js)"
        )
    }

    @Test
    fun properties_JavascriptFile_noFunction() {
        @Test
        fun prefixesFileIfNoFunction() {
            val error = AssertionError("test")
            error.asDynamic().stack = """
            AtriumError: test
                at /home/josh/Projekte/atrium/build/js/node_modules/mocha/lib/runner.js:602:7
        """.trimIndent()
            expect(error.stackBacktrace) toContainExactly JsStackBacktraceEntry(
                fileName = "runner.js",
                filePath = "/home/josh/Projekte/atrium/build/js/node_modules/mocha/lib/runner.js",
                lineNumber = 602,
                columnNumber = 7,
                normalizedLine = "runner.js (/home/josh/Projekte/atrium/build/js/node_modules/mocha/lib/runner.js:602:7)"
            )
        }
    }
}
