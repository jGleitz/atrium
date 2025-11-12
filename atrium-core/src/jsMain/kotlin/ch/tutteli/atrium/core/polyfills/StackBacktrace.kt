package ch.tutteli.atrium.core.polyfills

import ch.tutteli.atrium.reporting.BUG_REPORT_URL
import ch.tutteli.atrium.reporting.erroradjusters.impl.PathPatternBuilder.Companion.pathPattern

const val JS_STACK_LINE_START = "    at "
private const val WINDOWS_ABSOLUTE_PATH_START = /* language=RegExp */ """[A-Z]:\\"""
private val startsWithWindowsAbsolutePathRegex = Regex("""^$WINDOWS_ABSOLUTE_PATH_START""")
private val protoOfRegex = Regex("""(.*\.)protoOf\.(.*?)_[0-9a-z]+(?:_[a-z]\$)?(\s+\(.*)""")
private val locationRegex = Regex(
    // optional absolute path
    """((?:/|${WINDOWS_ABSOLUTE_PATH_START})(?:[^/\\]+[/\\])*)?""" +
            // required file name
            """([^/\\]+\.(?:kt|js|ts))""" +
            // optional line number
            """(?::(\d+))?""" +
            // optional column number
            """(?::(\d+))?"""
            // anchor to the end of the line to avoid false positives
            + """\)?$"""
)
private val isKotlinInternalRegex = pathPattern(
    // kotlin 1.4 + kotlin 1.3, e.g:
    //     at AssertionError_init_0 (D:\projects\atrium\core\api\atrium-core-api-js\build\node_modules\kotlin.js:24991:22)
    "_init_0|"
) {
    oneOf(
        //kotlin 1.6
        { dir("packages_imported").dir("kotlin") },
        // kotlin 1.4
        { dir("node_modules").dir("kotlin") }
    )
}

actual val Throwable.stackBacktrace: List<PlatformStackBacktraceEntry>
    get() = (asDynamic().stack as? String)
        ?.takeIf { it.isNotBlank() }
        ?.let { stack ->
            stack.indexOf('\n' + JS_STACK_LINE_START)
                .takeIf { it >= 0 }
                ?.let { firstFrameIndex ->
                    stack.substring(firstFrameIndex + /* skip the newline */ 1)
                }
        }
        ?.let { stackFrames ->
            // TODO include check for KotlinVersion.CURRENT once https://youtrack.jetbrains.com/issue/KT-64188 is fixed
            // although, it might also be that it is intellij related, if so, try to find out if we can get the intellij
            // version in use
            val hasCutColumnNumberOffBug = stackFrames.contains("KotlinTestTeamCityConsoleAdapter")
            splitAndFilterStackLines(stackFrames)
                .map { parseStackLine(it, hasCutColumnNumberOffBug) }
                .toList()
        }
        ?: listOf(FailedToParseJsBacktraceEntry)

actual interface PlatformStackBacktraceEntry : StackBacktraceEntry {
    /**
     * The full path of the code file at which this stack entry points. `null` if this stack entry does
     * not contain an absolute path.
     *
     * For example:
     *  * `C:\Users\me\projects\my-project\src\main\kotlin\MyClass.kt`
     *  * `/home/me/projects/my-project/src/main/kotlin/MyClass.kt`
     *  * `/home/me/projects/atrium/build/js/packages_imported/kotlin-test-js-runner/src/KotlinTestTeamCityConsoleAdapter.ts`
     *
     * `null`, for example, for these stack lines:
     *  * `    at <global>.fn (kotlin/atrium-atrium-core-test.js:3110:42)`
     *  * `    at <global>.processImmediate (node:internal/timers:478:42)`
     */
    val filePath: String?
}

data class JsStackBacktraceEntry(
    override val fileName: String?,
    override val filePath: String?,
    override val lineNumber: Int?,
    override val columnNumber: Int?,
    override val normalizedLine: String
) : PlatformStackBacktraceEntry

object FailedToParseJsBacktraceEntry : PlatformStackBacktraceEntry {
    override val fileName: String? get() = null
    override val filePath: String? get() = null
    override val lineNumber: Int? get() = null
    override val columnNumber: Int? get() = null
    override val normalizedLine: String
        get() =
            "Failed to parse stack trace line, please file a bug report at $BUG_REPORT_URL"
}


private fun splitAndFilterStackLines(stackFrames: String): Sequence<String> {
    return stackFrames
        .splitToSequence('\n')
        //TODO remove once https://youtrack.jetbrains.com/issue/KT-27920 is fixed
        .dropWhile {
            isKotlinInternalRegex.containsMatchIn(it)
        }
}

private fun parseStackLine(stackFrame: String, hasCutColumnNumberOffBug: Boolean): PlatformStackBacktraceEntry {
    var normalizedStackFrame = stackFrame.substringAfter(JS_STACK_LINE_START)

    val locationMatchGroups = locationRegex.find(normalizedStackFrame)?.groups
    val fileName = locationMatchGroups?.get(2)?.value
    val fileAbsolutePath = if (fileName != null) locationMatchGroups.get(1)?.value?.let { it + fileName } else null
    val line = locationMatchGroups?.get(3)?.value?.toInt()
    val column = locationMatchGroups?.get(4)?.value?.toInt()

    val startsWithAbsolutePath = normalizedStackFrame.startsWith('/') ||
            startsWithWindowsAbsolutePathRegex.containsMatchIn(normalizedStackFrame)
    if (startsWithAbsolutePath) {
        println("startsWithAbsolutePath: <$fileName> from $stackFrame")
        // if the line consists of only the file path (i.e. without the throwing function), we prefix it
        // with the file name to make IntelliJ render the link correctly
        normalizedStackFrame = "$fileName ($normalizedStackFrame)"
    }

    protoOfRegex.matchEntire(normalizedStackFrame)?.destructured?.let { (preProto, postProto, location) ->
        // looks like we use KotlinJs version which includes the `.protoOf.` and function name mangling.
        // They don't add much value and might only confuse people, so we convert attempt to convert
        // back to the original name.
        normalizedStackFrame = "$preProto$postProto$location"
    }

    if (hasCutColumnNumberOffBug && line != null && column != null) {
        // due to a Kotlin bug (https://youtrack.jetbrains.com/issue/KT-64188) we add an imaginary third
        // number at the end because it seems that the KotlinTestTeamCityConsoleAdapter cuts one number off
        normalizedStackFrame = normalizedStackFrame.lastIndexOf(')')
            .takeIf { it >= 0 }
            ?.let { normalizedStackFrame.take(it) + ":0" + normalizedStackFrame.substring(it) }
            ?: normalizedStackFrame
    }

    return JsStackBacktraceEntry(fileName, fileAbsolutePath, line, column, normalizedStackFrame)
}


