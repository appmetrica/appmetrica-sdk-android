package io.appmetrica.analytics.billingv3.impl.update;

import io.appmetrica.analytics.billinginterface.internal.BillingInfo;
import io.appmetrica.analytics.billinginterface.internal.ProductType;
import io.appmetrica.analytics.billinginterface.internal.config.BillingConfig;
import io.appmetrica.analytics.billinginterface.internal.storage.BillingInfoManager;
import io.appmetrica.analytics.coreutils.internal.time.SystemTimeProvider;
import java.util.HashMap;
import java.util.Map;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.robolectric.RobolectricTestRunner;

import static org.assertj.core.api.Java6Assertions.assertThat;
import static org.mockito.Mockito.when;

@RunWith(RobolectricTestRunner.class)
public class UpdatePolicyImplTest {

    @Mock
    private BillingInfoManager billingInfoManager;
    @Mock
    private SystemTimeProvider systemTimeProvider;

    private final long now = new SystemTimeProvider().currentTimeMillis();
    private final BillingInfo storedInapp = new BillingInfo(ProductType.INAPP, "stored_inapp", "stored_inapp_token", 1, 2);
    private final BillingInfo storedSubs = new BillingInfo(ProductType.SUBS, "stored_subs", "stored_subs_token", 1, 2);
    private final BillingConfig billingConfig = new BillingConfig(10000, 10000);

    private UpdatePolicyImpl updatePolicy;

    @Before
    public void setUp() {
        MockitoAnnotations.openMocks(this);
        when(billingInfoManager.get(storedInapp.sku)).thenReturn(storedInapp);
        when(billingInfoManager.get(storedSubs.sku)).thenReturn(storedSubs);
        when(billingInfoManager.isFirstInappCheckOccurred()).thenReturn(true);
        when(systemTimeProvider.currentTimeMillis()).thenReturn(now);

        updatePolicy = new UpdatePolicyImpl(systemTimeProvider);
    }

    @Test
    public void ignoreSkuIfStored() {
        final Map<String, BillingInfo> history = new HashMap<>();
        history.put(storedInapp.sku, new BillingInfo(storedInapp.type, storedInapp.sku, storedInapp.purchaseToken, 3, 4));

        final Map<String, BillingInfo> result = updatePolicy.getBillingInfoToUpdate(billingConfig, history, billingInfoManager);
        assertThat(result).isEmpty();
    }

    @Test
    public void updateSkuIfStoredWithOtherToken() {
        final Map<String, BillingInfo> history = new HashMap<>();
        history.put(storedInapp.sku, new BillingInfo(storedInapp.type, storedInapp.sku, "other_token", 3, 4));

        final Map<String, BillingInfo> result = updatePolicy.getBillingInfoToUpdate(billingConfig, history, billingInfoManager);
        assertThat(result).isEqualTo(history);
    }

    @Test
    public void updateSkuIfNotStored() {
        final Map<String, BillingInfo> history = new HashMap<>();
        history.put("sku", new BillingInfo(ProductType.INAPP, "sku", "token", 3, 4));

        final Map<String, BillingInfo> result = updatePolicy.getBillingInfoToUpdate(billingConfig, history, billingInfoManager);
        assertThat(result).isEqualTo(history);
    }

    @Test
    public void ignoreOldSku() {
        when(billingInfoManager.isFirstInappCheckOccurred()).thenReturn(false);
        final Map<String, BillingInfo> history = new HashMap<>();
        history.put("sku", new BillingInfo(ProductType.INAPP, "sku", "token", 3, 4));

        final Map<String, BillingInfo> result = updatePolicy.getBillingInfoToUpdate(billingConfig, history, billingInfoManager);
        assertThat(result).isEmpty();
    }

    @Test
    public void updateSkuOnFirstWrite() {
        when(billingInfoManager.isFirstInappCheckOccurred()).thenReturn(false);
        final Map<String, BillingInfo> history = new HashMap<>();
        history.put("sku", new BillingInfo(ProductType.INAPP, "sku", "token", now, 4));

        final Map<String, BillingInfo> result = updatePolicy.getBillingInfoToUpdate(billingConfig, history, billingInfoManager);
        assertThat(result).isEqualTo(history);
    }

    @Test
    public void updateOldSubs() {
        final Map<String, BillingInfo> history = new HashMap<>();
        history.put(storedSubs.sku, new BillingInfo(ProductType.SUBS, storedSubs.sku, "token", 3, 4));

        final Map<String, BillingInfo> result = updatePolicy.getBillingInfoToUpdate(billingConfig, history, billingInfoManager);
        assertThat(result).isEqualTo(history);
    }

    @Test
    public void ignoreNewSubsIfStored() {
        final Map<String, BillingInfo> history = new HashMap<>();
        history.put(storedSubs.sku, new BillingInfo(storedSubs.type, storedSubs.sku, storedSubs.purchaseToken, now, now));
        when(billingInfoManager.get(storedSubs.sku)).thenReturn(
                new BillingInfo(ProductType.SUBS, storedSubs.sku, storedSubs.purchaseToken, storedSubs.purchaseTime, now)
        );

        final Map<String, BillingInfo> result = updatePolicy.getBillingInfoToUpdate(billingConfig, history, billingInfoManager);
        assertThat(result).isEmpty();
    }

    @Test
    public void ignorePartOfSkus() {
        when(billingInfoManager.isFirstInappCheckOccurred()).thenReturn(false);

        final Map<String, BillingInfo> goodHistory = new HashMap<>();
        goodHistory.put("sku1", new BillingInfo(ProductType.INAPP, "sku1", "token1", now, 4));
        goodHistory.put("sku2", new BillingInfo(ProductType.INAPP, "sku2", "token2", now, 4));
        final Map<String, BillingInfo> badHistory = new HashMap<>();
        badHistory.put("sku3", new BillingInfo(ProductType.INAPP, "sku3", "token3", 3, 4));
        badHistory.put("sku4", new BillingInfo(ProductType.INAPP, "sku4", "token4", 3, 4));

        final Map<String, BillingInfo> history = new HashMap<>(goodHistory);
        history.putAll(badHistory);

        final Map<String, BillingInfo> result = updatePolicy.getBillingInfoToUpdate(billingConfig, history, billingInfoManager);
        assertThat(result).isEqualTo(goodHistory);
    }
}
