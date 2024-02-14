package io.appmetrica.analytics.impl.billing;

import androidx.annotation.NonNull;
import io.appmetrica.analytics.billinginterface.internal.ProductInfo;
import io.appmetrica.analytics.billinginterface.internal.storage.BillingInfoSender;
import io.appmetrica.analytics.coreapi.internal.executors.ICommonExecutor;
import io.appmetrica.analytics.coreutils.internal.executors.SafeRunnable;
import io.appmetrica.analytics.impl.CounterReport;
import io.appmetrica.analytics.impl.GlobalServiceLocator;
import io.appmetrica.analytics.impl.component.IReportableComponent;
import io.appmetrica.analytics.logger.internal.YLogger;
import java.util.List;

public class BillingInfoSenderImpl implements BillingInfoSender {

    private static final String TAG = "[BillingInfoSenderImpl]";

    @NonNull
    private final IReportableComponent reportableComponent;
    @NonNull
    private final ICommonExecutor reportExecutor;

    public BillingInfoSenderImpl(@NonNull final IReportableComponent reportableComponent) {
        this(
                reportableComponent,
                GlobalServiceLocator.getInstance().getServiceExecutorProvider().getReportRunnableExecutor()
        );
    }

    public BillingInfoSenderImpl(@NonNull final IReportableComponent reportableComponent,
                                 @NonNull final ICommonExecutor reportExecutor) {
        this.reportableComponent = reportableComponent;
        this.reportExecutor = reportExecutor;
    }

    @Override
    public void sendInfo(@NonNull final List<ProductInfo> productInfos) {
        YLogger.info(TAG, "sendInfo");
        for (final ProductInfo productInfo: productInfos) {
            YLogger.info(TAG, "info " + productInfo.sku);
            reportExecutor.execute(new SafeRunnable() {
                @Override
                public void runSafety() throws Exception {
                    reportableComponent.handleReport(formReport(productInfo));
                }
            });
        }
    }

    @NonNull
    private CounterReport formReport(@NonNull final ProductInfo productInfo) {
        final ProductInfoWrapper wrapper = new ProductInfoWrapper(productInfo);
        return CounterReport.formAutoInappEvent(wrapper);
    }
}
