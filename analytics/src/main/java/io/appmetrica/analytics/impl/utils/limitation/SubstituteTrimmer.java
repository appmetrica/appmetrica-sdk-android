package io.appmetrica.analytics.impl.utils.limitation;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

public class SubstituteTrimmer<T> implements Trimmer<T> {

    @NonNull
    private final Trimmer<T> mBaseTrimmer;
    @Nullable
    private final T mSubstituteWith;

    public SubstituteTrimmer(@NonNull Trimmer<T> baseTrimmer,
                             @Nullable T substituteWith) {
        mBaseTrimmer = baseTrimmer;
        mSubstituteWith = substituteWith;
    }

    @Nullable
    @Override
    public T trim(@Nullable T data) {
        if (data != mBaseTrimmer.trim(data)) {
            return mSubstituteWith;
        } else {
            return data;
        }
    }
}
