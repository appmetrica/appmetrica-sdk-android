package io.appmetrica.analytics.impl.billing;

import io.appmetrica.analytics.billinginterface.internal.Period;
import io.appmetrica.analytics.billinginterface.internal.ProductInfo;
import io.appmetrica.analytics.billinginterface.internal.ProductType;
import io.appmetrica.analytics.coreapi.internal.executors.ICommonExecutor;
import io.appmetrica.analytics.coreutils.internal.executors.SafeRunnable;
import io.appmetrica.analytics.impl.CounterReport;
import io.appmetrica.analytics.impl.component.IReportableComponent;
import io.appmetrica.analytics.testutils.CommonTest;
import java.util.Arrays;
import java.util.List;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;
import org.robolectric.RobolectricTestRunner;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

@RunWith(RobolectricTestRunner.class)
public class BillingInfoSenderImplTest extends CommonTest {

    private IReportableComponent reportableComponent;
    private BillingInfoSenderImpl billingInfoSender;

    @Before
    public void setUp() {
        reportableComponent = mock(IReportableComponent.class);
        ICommonExecutor reportExecutor = mock(ICommonExecutor.class);

        doAnswer(new Answer<Void>() {
            @Override
            public Void answer(InvocationOnMock invocation) throws Throwable {
                ((Runnable) invocation.getArgument(0)).run();
                return null;
            }
        }).when(reportExecutor).execute(any(SafeRunnable.class));
        billingInfoSender = new BillingInfoSenderImpl(reportableComponent, reportExecutor);
    }

    @Test
    public void testSendInfo() {
        final List<ProductInfo> productInfos = getProductInfos();
        billingInfoSender.sendInfo(productInfos);

        verify(reportableComponent, times(productInfos.size())).handleReport(any(CounterReport.class));
    }

    private List<ProductInfo> getProductInfos() {
        return Arrays.asList(
            new ProductInfo(ProductType.INAPP, "sku1", 2, 1, "by", 3, new Period(1, Period.TimeUnit.DAY), 1, new Period(1, Period.TimeUnit.MONTH), "signature1", "token1", 10, true, "{\"productId\":\"sku1\", \"autoRenewing\":\"true\"}"),
            new ProductInfo(ProductType.SUBS, "sku2", 2, 2, "by", 4, new Period(1, Period.TimeUnit.DAY), 1, new Period(1, Period.TimeUnit.MONTH), "signature2", "token2", 11, false, "")
        );
    }
}
