package io.appmetrica.analytics.billingv3.impl.storage;

import io.appmetrica.analytics.billinginterface.internal.BillingInfo;
import io.appmetrica.analytics.billinginterface.internal.ProductType;
import io.appmetrica.analytics.billinginterface.internal.storage.BillingInfoStorage;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentMatchers;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.robolectric.RobolectricTestRunner;

import static org.assertj.core.api.Java6Assertions.assertThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith(RobolectricTestRunner.class)
public class BillingInfoManagerImplTest {

    @Mock
    private BillingInfoStorage billingInfoStorage;

    private BillingInfoManagerImpl billingInfoManager;

    @Before
    public void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    public void testUpdate() {
        billingInfoManager = new BillingInfoManagerImpl(billingInfoStorage);
        final Map<String, BillingInfo> history = new HashMap<>();
        history.put("sku1", new BillingInfo(ProductType.INAPP, "sku1", "token1", 1, 2));
        history.put("sku2", new BillingInfo(ProductType.INAPP, "sku2", "token2", 3, 4));

        billingInfoManager.update(history);
        verify(billingInfoStorage).saveInfo(eq(new ArrayList<>(history.values())), eq(false));
    }

    @Test
    public void testUpdateIfFirstInappCheckOccured() {
        when(billingInfoStorage.isFirstInappCheckOccurred()).thenReturn(true);
        billingInfoManager = new BillingInfoManagerImpl(billingInfoStorage);
        final Map<String, BillingInfo> history = new HashMap<>();
        history.put("sku1", new BillingInfo(ProductType.INAPP, "sku1", "token1", 1, 2));
        history.put("sku2", new BillingInfo(ProductType.INAPP, "sku2", "token2", 3, 4));

        billingInfoManager.update(history);
        verify(billingInfoStorage).saveInfo(eq(new ArrayList<>(history.values())), eq(true));
    }

    @Test
    public void testMarkFirstInappCheckOccured() {
        when(billingInfoStorage.isFirstInappCheckOccurred()).thenReturn(false);
        billingInfoManager = new BillingInfoManagerImpl(billingInfoStorage);
        assertThat(billingInfoManager.isFirstInappCheckOccurred()).isFalse();

        billingInfoManager.markFirstInappCheckOccurred();
        assertThat(billingInfoManager.isFirstInappCheckOccurred()).isTrue();
        verify(billingInfoStorage).saveInfo(ArgumentMatchers.<BillingInfo>anyList(), eq(true));
    }

    @Test
    public void testMarkFirstInappCheckOccuredAfterUpdate() {
        when(billingInfoStorage.isFirstInappCheckOccurred()).thenReturn(false);
        billingInfoManager = new BillingInfoManagerImpl(billingInfoStorage);
        final Map<String, BillingInfo> history = new HashMap<>();
        history.put("sku1", new BillingInfo(ProductType.INAPP, "sku1", "token1", 1, 2));
        history.put("sku2", new BillingInfo(ProductType.INAPP, "sku2", "token2", 3, 4));

        billingInfoManager.update(history);
        billingInfoManager.markFirstInappCheckOccurred();
        assertThat(billingInfoManager.isFirstInappCheckOccurred()).isTrue();
        verify(billingInfoStorage).saveInfo(eq(new ArrayList<>(history.values())), eq(true));
    }

    @Test
    public void testGet() {
        final List<BillingInfo> billingInfos = new ArrayList<>();
        billingInfos.add(new BillingInfo(ProductType.INAPP, "sku", "token1", 1, 2));
        when(billingInfoStorage.getBillingInfo()).thenReturn(billingInfos);
        billingInfoManager = new BillingInfoManagerImpl(billingInfoStorage);

        assertThat(billingInfoManager.get("wrong_sku")).isNull();
        assertThat(billingInfoManager.get("sku")).isEqualTo(billingInfos.get(0));
    }

    @Test
    public void testGetAfterUpdate() {
        billingInfoManager = new BillingInfoManagerImpl(billingInfoStorage);
        final Map<String, BillingInfo> history = new HashMap<>();
        history.put("sku1", new BillingInfo(ProductType.INAPP, "sku1", "token1", 1, 2));

        assertThat(billingInfoManager.get("sku1")).isNull();

        billingInfoManager.update(history);
        assertThat(billingInfoManager.get("sku1")).isEqualTo(history.get("sku1"));
    }
}
