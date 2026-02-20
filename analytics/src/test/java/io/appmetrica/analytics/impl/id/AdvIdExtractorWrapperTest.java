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
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameters;

import static org.assertj.core.api.Java6Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@RunWith(Parameterized.class)
public class AdvIdExtractorWrapperTest extends CommonTest {

    @Parameters(name = "{2}")
    public static Collection<Object[]> data() {
        final Context context = mock(Context.class);
        final RetryStrategy retryStrategy = mock(RetryStrategy.class);
        return Arrays.asList(new Object[][]{
            {
                new BiConsumer<AdvIdExtractor, AdTrackingInfoResult>() {
                    @Override
                    public void consume(AdvIdExtractor firstArg, AdTrackingInfoResult secondArg) {
                        when(firstArg.extractAdTrackingInfo(context)).thenReturn(secondArg);
                    }
                },
                new Function<AdvIdExtractor, AdTrackingInfoResult>() {
                    @Override
                    public AdTrackingInfoResult apply(AdvIdExtractor input) {
                        return input.extractAdTrackingInfo(context);
                    }
                },
                "Method only with context"
            },
            {
                new BiConsumer<AdvIdExtractor, AdTrackingInfoResult>() {
                    @Override
                    public void consume(AdvIdExtractor firstArg, AdTrackingInfoResult secondArg) {
                        when(firstArg.extractAdTrackingInfo(context, retryStrategy)).thenReturn(secondArg);
                    }
                },
                new Function<AdvIdExtractor, AdTrackingInfoResult>() {
                    @Override
                    public AdTrackingInfoResult apply(AdvIdExtractor input) {
                        return input.extractAdTrackingInfo(context, retryStrategy);
                    }
                },
                "Method with context and retry strategy"
            },
        });
    }

    @Mock
    private AdvIdExtractor originalProvider;
    private AdvIdExtractorWrapper providerWrapper;
    private final BiConsumer<AdvIdExtractor, AdTrackingInfoResult> originalProviderMocker;
    private final Function<AdvIdExtractor, AdTrackingInfoResult> methodExecutor;

    public AdvIdExtractorWrapperTest(@NonNull BiConsumer<AdvIdExtractor, AdTrackingInfoResult> originalProviderMocker,
                                     @NonNull Function<AdvIdExtractor, AdTrackingInfoResult> methodExecutor,
                                     String description) {
        this.originalProviderMocker = originalProviderMocker;
        this.methodExecutor = methodExecutor;
    }

    @Before
    public void setUp() {
        MockitoAnnotations.openMocks(this);
        providerWrapper = new AdvIdExtractorWrapper(originalProvider);
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
        assertThat(methodExecutor.apply(providerWrapper)).usingRecursiveComparison().isEqualTo(new AdTrackingInfoResult(
            null,
            IdentifierStatus.INVALID_ADV_ID,
            "AdvId is invalid: 00000000-0000-0000-0000-000000000000"
        ));
    }
}
