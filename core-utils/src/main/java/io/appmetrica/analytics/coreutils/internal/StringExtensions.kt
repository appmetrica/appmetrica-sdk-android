package io.appmetrica.analytics.coreutils.internal

import java.util.Locale

fun String.replaceFirstCharWithTitleCase() = replaceFirstCharWithTitleCase(Locale.US)

fun String.replaceFirstCharWithTitleCase(locale: Locale) =
    replaceFirstChar { if (it.isLowerCase()) it.titlecase(locale) else it.toString() }
