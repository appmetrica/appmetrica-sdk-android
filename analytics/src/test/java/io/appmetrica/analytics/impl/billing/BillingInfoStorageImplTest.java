package io.appmetrica.analytics.impl.billing;

import androidx.annotation.NonNull;
import io.appmetrica.analytics.billinginterface.internal.BillingInfo;
import io.appmetrica.analytics.billinginterface.internal.ProductType;
import io.appmetrica.analytics.coreapi.internal.data.ProtobufStateStorage;
import io.appmetrica.analytics.testutils.CommonTest;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.robolectric.ParameterizedRobolectricTestRunner;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith(ParameterizedRobolectricTestRunner.class)
public class BillingInfoStorageImplTest extends CommonTest {

    @ParameterizedRobolectricTestRunner.Parameters(name = "{index}")
    public static Collection<Object[]> getData() {
        List<BillingInfo> emptyList = new ArrayList<BillingInfo>();
        List<BillingInfo> filledList = Collections.singletonList(
            new BillingInfo(ProductType.INAPP, "sku", "purchaseToken", 0, 0)
        );
        return Arrays.asList(
            // #0
            new Object[]{
                emptyList,
                true
            },
            new Object[]{
                emptyList,
                false
            },
            new Object[]{
                filledList,
                true
            },
            new Object[]{
                filledList,
                false
            }
        );
    }

    @NonNull
    private final List<BillingInfo> billingInfos;
    private final boolean firstInappCheckOccurred;

    public BillingInfoStorageImplTest(@NonNull final List<BillingInfo> billingInfos,
                                      final boolean firstInappCheckOccurred) {
        this.billingInfos = billingInfos;
        this.firstInappCheckOccurred = firstInappCheckOccurred;
    }

    @Mock
    private ProtobufStateStorage<AutoInappCollectingInfo> storage;
    private AutoInappCollectingInfo autoInappCollectingInfo;
    private BillingInfoStorageImpl billingInfoStorage;

    @Before
    public void setUp() {
        autoInappCollectingInfo = new AutoInappCollectingInfo(billingInfos, firstInappCheckOccurred);
        MockitoAnnotations.openMocks(this);
        when(storage.read()).thenReturn(autoInappCollectingInfo);
        billingInfoStorage = new BillingInfoStorageImpl(storage);
    }

    @Test
    public void testSaveInfo() {
        ArgumentCaptor<AutoInappCollectingInfo> argument = ArgumentCaptor.forClass(AutoInappCollectingInfo.class);
        billingInfoStorage.saveInfo(billingInfos, firstInappCheckOccurred);
        verify(storage).save(argument.capture());
        assertThat(argument.getValue().billingInfos).isEqualTo(billingInfos);
        assertThat(argument.getValue().firstInappCheckOccurred).isEqualTo(firstInappCheckOccurred);
    }

    @Test
    public void testLoadInfo() {
        final List<BillingInfo> infos = billingInfoStorage.getBillingInfo();
        verify(storage).read();
        assertThat(infos).isEqualTo(billingInfos);
    }

    @Test
    public void testIsFirstInappCheckOccurred() {
        assertThat(billingInfoStorage.isFirstInappCheckOccurred()).isEqualTo(firstInappCheckOccurred);
    }
}
