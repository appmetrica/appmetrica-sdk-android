package io.appmetrica.analytics.impl.selfreporting;

import androidx.annotation.NonNull;
import io.appmetrica.analytics.impl.IReporterExtended;

interface IReporterCommandPerformer {

    void perform(@NonNull IReporterExtended reporterExtended);
}
