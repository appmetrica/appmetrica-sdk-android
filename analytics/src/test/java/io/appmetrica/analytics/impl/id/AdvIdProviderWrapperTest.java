package io.appmetrica.analytics.impl.id;

import android.content.Context;
import androidx.annotation.NonNull;
import io.appmetrica.analytics.coreapi.internal.backport.BiConsumer;
import io.appmetrica.analytics.coreapi.internal.backport.Function;
import io.appmetrica.analytics.coreapi.internal.identifiers.AdTrackingInfo;
import io.appmetrica.analytics.coreapi.internal.identifiers.AdTrackingInfoResult;
import io.appmetrica.analytics.coreapi.internal.identifiers.IdentifierStatus;
import io.appmetrica.analytics.testutils.CommonTest;
import java.util.Arrays;
import java.util.Collection;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.robolectric.ParameterizedRobolectricTestRunner;

import static org.assertj.core.api.Java6Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@RunWith(ParameterizedRobolectricTestRunner.class)
public class AdvIdProviderWrapperTest extends CommonTest {

    @ParameterizedRobolectricTestRunner.Parameters(name = "{2}")
    public static Collection<Object[]> data() {
        final Context context = mock(Context.class);
        final RetryStrategy retryStrategy = mock(RetryStrategy.class);
        return Arrays.asList(new Object[][]{
                {
                    new BiConsumer<AdvIdProvider, AdTrackingInfoResult>() {
                        @Override
                        public void consume(AdvIdProvider firstArg, AdTrackingInfoResult secondArg) {
                            when(firstArg.getAdTrackingInfo(context)).thenReturn(secondArg);
                        }
                    },
                    new Function<AdvIdProvider, AdTrackingInfoResult>() {
                        @Override
                        public AdTrackingInfoResult apply(AdvIdProvider input) {
                            return input.getAdTrackingInfo(context);
                        }
                    },
                    "Method only with context"
                },
                {
                        new BiConsumer<AdvIdProvider, AdTrackingInfoResult>() {
                            @Override
                            public void consume(AdvIdProvider firstArg, AdTrackingInfoResult secondArg) {
                                when(firstArg.getAdTrackingInfo(context, retryStrategy)).thenReturn(secondArg);
                            }
                        },
                        new Function<AdvIdProvider, AdTrackingInfoResult>() {
                            @Override
                            public AdTrackingInfoResult apply(AdvIdProvider input) {
                                return input.getAdTrackingInfo(context, retryStrategy);
                            }
                        },
                        "Method with context and retry strategy"
                },
        });
    }

    @Mock
    private AdvIdProvider originalProvider;
    private AdvIdProviderWrapper providerWrapper;
    private final BiConsumer<AdvIdProvider, AdTrackingInfoResult> originalProviderMocker;
    private final Function<AdvIdProvider, AdTrackingInfoResult> methodExecutor;

    public AdvIdProviderWrapperTest(@NonNull BiConsumer<AdvIdProvider, AdTrackingInfoResult> originalProviderMocker,
                                    @NonNull Function<AdvIdProvider, AdTrackingInfoResult> methodExecutor,
                                    String description) {
        this.originalProviderMocker = originalProviderMocker;
        this.methodExecutor = methodExecutor;
    }

    @Before
    public void setUp() {
        MockitoAnnotations.openMocks(this);
        providerWrapper = new AdvIdProviderWrapper(originalProvider);
    }

    @Test
    public void getAdTrackingInfoNullAdTrackingInfo() {
        AdTrackingInfoResult result = new AdTrackingInfoResult(null, IdentifierStatus.UNKNOWN, "");
        originalProviderMocker.consume(originalProvider, result);
        assertThat(methodExecutor.apply(providerWrapper)).isSameAs(result);
    }

    @Test
    public void getAdTrackingInfoValidAdvId() {
        AdTrackingInfoResult result = new AdTrackingInfoResult(
                new AdTrackingInfo(AdTrackingInfo.Provider.GOOGLE, "666-777", false),
                IdentifierStatus.OK, null);
        originalProviderMocker.consume(originalProvider, result);
        assertThat(methodExecutor.apply(providerWrapper)).isSameAs(result);
    }

    @Test
    public void getAdTrackingInfoInvalidAdvId() {
        AdTrackingInfoResult result = new AdTrackingInfoResult(
                new AdTrackingInfo(AdTrackingInfo.Provider.GOOGLE, "00000000-0000-0000-0000-000000000000", false),
                IdentifierStatus.OK, null);
        originalProviderMocker.consume(originalProvider, result);
        assertThat(methodExecutor.apply(providerWrapper)).isEqualToComparingFieldByFieldRecursively(new AdTrackingInfoResult(
                null,
                IdentifierStatus.INVALID_ADV_ID,
                "AdvId is invalid: 00000000-0000-0000-0000-000000000000"
        ));
    }
}
