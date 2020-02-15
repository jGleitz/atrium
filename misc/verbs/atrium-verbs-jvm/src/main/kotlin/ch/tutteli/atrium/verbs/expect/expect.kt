package ch.tutteli.atrium.api.verbs

import ch.tutteli.atrium.creating.RootExpect
import ch.tutteli.atrium.domain.builders.reporting.ExpectBuilder
import kotlinx.coroutines.runBlocking

fun <T> expect(subject: suspend () -> T): RootExpect<() -> T> =
    ExpectBuilder.forSubject { runBlocking { subject() } }
        .withVerb(AssertionVerb.EXPECT)
        .withoutOptions()
        .build()
