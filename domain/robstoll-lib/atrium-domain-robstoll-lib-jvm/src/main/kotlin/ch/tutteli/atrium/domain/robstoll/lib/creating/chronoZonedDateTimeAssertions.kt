package ch.tutteli.atrium.domain.robstoll.lib.creating

import ch.tutteli.atrium.assertions.Assertion
import ch.tutteli.atrium.creating.Expect
import ch.tutteli.atrium.domain.builders.ExpectImpl
import ch.tutteli.atrium.translations.DescriptionDateTimeLikeAssertion.*
import java.time.chrono.ChronoLocalDate
import java.time.chrono.ChronoZonedDateTime

fun <D: ChronoLocalDate, T : ChronoZonedDateTime<D>> _isAfter(assertionContainer: Expect<T>, expected: T): Assertion =
    ExpectImpl.builder.createDescriptive(assertionContainer, IS_AFTER, expected) {it.isAfter(expected)}

fun <D: ChronoLocalDate, T : ChronoZonedDateTime<D>> _isBeforeOrEquals(assertionContainer: Expect<T>, expected: T): Assertion =
    ExpectImpl.builder.createDescriptive(assertionContainer, IS_BEFORE_OR_EQUALS, expected) { it.isBefore(expected) || it.isEqual(expected) }

fun <D: ChronoLocalDate, T : ChronoZonedDateTime<D>> _isBefore(assertionContainer: Expect<T>, expected: T): Assertion =
    ExpectImpl.builder.createDescriptive(assertionContainer, IS_BEFORE, expected) { it.isBefore(expected) }