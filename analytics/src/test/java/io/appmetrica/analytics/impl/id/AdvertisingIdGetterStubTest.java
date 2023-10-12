package io.appmetrica.analytics.impl.id;

import android.content.Context;
import androidx.annotation.Nullable;
import io.appmetrica.analytics.assertions.ObjectPropertyAssertions;
import io.appmetrica.analytics.coreapi.internal.identifiers.AdTrackingInfoResult;
import io.appmetrica.analytics.coreapi.internal.identifiers.AdvertisingIdsHolder;
import io.appmetrica.analytics.coreapi.internal.identifiers.IdentifierStatus;
import io.appmetrica.analytics.impl.startup.StartupState;
import io.appmetrica.analytics.testutils.CommonTest;
import io.appmetrica.analytics.testutils.TestUtils;
import java.util.function.Consumer;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.robolectric.RobolectricTestRunner;

import static io.appmetrica.analytics.assertions.AssertionsKt.ObjectPropertyAssertions;
import static org.mockito.Mockito.verifyNoMoreInteractions;

@RunWith(RobolectricTestRunner.class)
public class AdvertisingIdGetterStubTest extends CommonTest {

    private Context context;
    @Mock
    private StartupState startupState;
    @Mock
    private RetryStrategy retryStrategy;

    private AdvertisingIdGetterStub advertisingIdGetterStub;

    @Before
    public void setUp() throws Exception {
        MockitoAnnotations.openMocks(this);
        context = TestUtils.createMockedContext();

        advertisingIdGetterStub = new AdvertisingIdGetterStub();
    }

    @Test
    public void lazyInit() {
        advertisingIdGetterStub.lazyInit(context);
        verifyNoMoreInteractions(context);
    }

    @Test
    public void init() {
        advertisingIdGetterStub.init(context);
        verifyNoMoreInteractions(context);
    }

    @Test
    public void initWithStartupState() {
        advertisingIdGetterStub.init(context, startupState);
        verifyNoMoreInteractions(context, startupState);
    }

    @Test
    public void getIdentifiers() throws Exception {
        assertAdvertisingIdsHolder(advertisingIdGetterStub.getIdentifiers(context));
        verifyNoMoreInteractions(context);
    }

    @Test
    public void getIdentifiersForced() throws Exception {
        assertAdvertisingIdsHolder(advertisingIdGetterStub.getIdentifiersForced(context));
        verifyNoMoreInteractions(context);
    }

    @Test
    public void getIdentifiersForcedWithRetryStrategy() throws Exception {
        assertAdvertisingIdsHolder(advertisingIdGetterStub.getIdentifiersForced(context, retryStrategy));
        verifyNoMoreInteractions(context, retryStrategy);
    }

    @Test
    public void onStartupStateChanged() {
        advertisingIdGetterStub.onStartupStateChanged(startupState);
        verifyNoMoreInteractions(startupState);
    }

    private void assertAdvertisingIdsHolder(AdvertisingIdsHolder advertisingIdsHolder) throws Exception {
        Consumer<ObjectPropertyAssertions<AdTrackingInfoResult>> adTrackingInfoResultVerifier =
            new Consumer<ObjectPropertyAssertions<AdTrackingInfoResult>>() {
                @Override
                public void accept(@Nullable ObjectPropertyAssertions<AdTrackingInfoResult> assertions) {
                    try {
                        assertions.checkFieldIsNull("mAdTrackingInfo")
                            .checkField("mStatus", IdentifierStatus.UNKNOWN)
                            .checkField("mErrorExplanation", "Device user is in locked state")
                            .checkAll();
                    } catch (Exception e) {
                        throw new RuntimeException(e);
                    }
                }
            };
        ObjectPropertyAssertions(advertisingIdsHolder)
            .withPrivateFields(true)
            .checkFieldRecursively("mGoogle", adTrackingInfoResultVerifier)
            .checkFieldRecursively("mHuawei", adTrackingInfoResultVerifier)
            .checkFieldRecursively("yandex", adTrackingInfoResultVerifier)
            .checkAll();

    }
}
