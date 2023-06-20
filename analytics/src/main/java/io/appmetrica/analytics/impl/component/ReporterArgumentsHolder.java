package io.appmetrica.analytics.impl.component;

import androidx.annotation.NonNull;

public class ReporterArgumentsHolder {

    @NonNull
    private CommonArguments.ReporterArguments mArguments;

    public ReporterArgumentsHolder(@NonNull CommonArguments.ReporterArguments arguments) {
        mArguments = arguments;
    }

    public void updateArguments(@NonNull CommonArguments.ReporterArguments newArguments) {
        mArguments = mArguments.mergeFrom(newArguments);
    }

    @NonNull
    public CommonArguments.ReporterArguments getArguments() {
        return mArguments;
    }
}
