package io.appmetrica.analytics.billinginterface.internal.library;

import androidx.annotation.NonNull;
import io.appmetrica.analytics.billinginterface.internal.storage.BillingInfoManager;
import io.appmetrica.analytics.billinginterface.internal.storage.BillingInfoSender;
import io.appmetrica.analytics.billinginterface.internal.update.UpdatePolicy;
import io.appmetrica.analytics.coreapi.internal.executors.IHandlerExecutor;
import java.util.concurrent.Executor;

public interface UtilsProvider {

    @NonNull
    BillingInfoManager getBillingInfoManager();

    @NonNull
    UpdatePolicy getUpdatePolicy();

    @NonNull
    BillingInfoSender getBillingInfoSender();

    @NonNull
    Executor getUiExecutor();

    @NonNull
    IHandlerExecutor getWorkerExecutor();
}
