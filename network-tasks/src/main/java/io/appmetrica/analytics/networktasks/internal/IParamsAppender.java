package io.appmetrica.analytics.networktasks.internal;

import android.net.Uri;
import androidx.annotation.NonNull;

public interface IParamsAppender<T> {

    void appendParams(@NonNull final Uri.Builder uriBuilder, @NonNull T requestConfig);
}
