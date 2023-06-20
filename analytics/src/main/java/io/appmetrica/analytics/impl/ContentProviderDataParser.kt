package io.appmetrica.analytics.impl

import android.content.ContentValues

internal interface ContentProviderDataParser<T> : Function1<ContentValues, T?>
