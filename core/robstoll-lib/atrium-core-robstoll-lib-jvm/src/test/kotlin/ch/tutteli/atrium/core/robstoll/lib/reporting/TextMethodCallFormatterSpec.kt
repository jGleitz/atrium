package ch.tutteli.atrium.core.robstoll.lib.reporting

import ch.tutteli.atrium.api.verbs.internal.AssertionVerbFactory

//TODO #116 migrate spek1 to spek2 - move to common module
object TextMethodCallFormatterSpec : ch.tutteli.atrium.specs.reporting.TextMethodCallFormatterSpec(
    AssertionVerbFactory, { TextMethodCallFormatter }
)
