package ch.tutteli.atrium.reporting.erroradjusters

import ch.tutteli.atrium.core.polyfills.stackBacktrace
import ch.tutteli.atrium.reporting.AtriumErrorAdjuster

abstract class FilterAtriumErrorAdjuster : AtriumErrorAdjuster {

    /**
     * Replaces `stack` of the given [throwable] (and of its causes) with a lazy property which filters the original
     * `stack` via [adjustStack] on first access.
     *
     * Formatting a `stack` is expensive (especially with deep stacks and source maps), and many adjusted throwables
     * are never reported, e.g. an expected exception caught by `toThrow`.
     */
    final override fun adjust(throwable: Throwable) {
        val error = throwable.asDynamic()
        val originalDescriptor = js("Object").getOwnPropertyDescriptor(error, "stack")
        if (originalDescriptor != null && originalDescriptor.configurable == false) {
            error.stack = filterStack(throwable)
        } else {
            val descriptor: dynamic = js("({ configurable: true, enumerable: false })")
            descriptor.get = {
                restoreStack(error, originalDescriptor)
                filterStack(throwable).also { setStack(error, it) }
            }
            descriptor.set = { stack: dynamic -> setStack(error, stack) }
            js("Object").defineProperty(error, "stack", descriptor)
        }
        throwable.cause?.let { adjust(it) }
    }

    private fun filterStack(throwable: Throwable): String {
        val filteredStack = adjustStack(throwable.stackBacktrace.asSequence())
        val prefix = "    at "
        return filteredStack.joinToString("\n$prefix", prefix)
    }

    private fun restoreStack(error: dynamic, originalDescriptor: dynamic) {
        if (originalDescriptor != null) {
            js("Object").defineProperty(error, "stack", originalDescriptor)
        } else {
            js("Reflect").deleteProperty(error, "stack")
        }
    }

    private fun setStack(error: dynamic, stack: dynamic) {
        val descriptor: dynamic = js("({ configurable: true, enumerable: false, writable: true })")
        descriptor.value = stack
        js("Object").defineProperty(error, "stack", descriptor)
    }

    /**
     * Does nothing (no adjustments) - override in subclass if you want a different behaviour.
     */
    override fun adjustOtherThanStacks(throwable: Throwable) {}
}
