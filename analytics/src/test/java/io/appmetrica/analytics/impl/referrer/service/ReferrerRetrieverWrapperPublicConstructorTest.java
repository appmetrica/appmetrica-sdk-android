package io.appmetrica.analytics.impl.referrer.service;

import android.content.Context;
import io.appmetrica.analytics.coreapi.internal.executors.ICommonExecutor;
import io.appmetrica.analytics.coreutils.internal.reflection.ReflectionUtils;
import io.appmetrica.analytics.testutils.CommonTest;
import io.appmetrica.analytics.testutils.MockedStaticRule;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.MockedConstruction;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.RuntimeEnvironment;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@RunWith(RobolectricTestRunner.class)
public class ReferrerRetrieverWrapperPublicConstructorTest extends CommonTest {

    @Rule
    public final MockedStaticRule<ReflectionUtils> sdkReflectionUtilsMocked =
        new MockedStaticRule<>(ReflectionUtils.class);

    private static final String REFERRER_CLIENT_CLASS = "com.android.installreferrer.api.InstallReferrerClient";

    private Context mContext;
    @Mock
    private ICommonExecutor executor;

    @Before
    public void setUp() {
        MockitoAnnotations.openMocks(this);
        mContext = RuntimeEnvironment.getApplication();
    }

    @Test
    public void testHasClass() {
        when(ReflectionUtils.detectClassExists(REFERRER_CLIENT_CLASS)).thenReturn(true);
        ReferrerRetrieverWrapper wrapper = new ReferrerRetrieverWrapper(mContext, executor);
        assertThat(wrapper.getReferrerRetriever()).isExactlyInstanceOf(ReferrerFromLibraryRetriever.class);
    }

    @Test
    public void testCreateReferrerRetrieverNoClass() {
        when(ReflectionUtils.detectClassExists(REFERRER_CLIENT_CLASS)).thenReturn(false);
        ReferrerRetrieverWrapper wrapper = new ReferrerRetrieverWrapper(mContext, executor);
        assertThat(wrapper.getReferrerRetriever()).isNotInstanceOf(ReferrerFromLibraryRetriever.class);
    }

    @Test(expected = IllegalStateException.class)
    public void testCreateReferrerRetrieverConstructorThrows() throws Throwable {
        try (MockedConstruction<ReferrerFromLibraryRetriever> ignored = Mockito.mockConstruction(
                ReferrerFromLibraryRetriever.class,
                new MockedConstruction.MockInitializer<ReferrerFromLibraryRetriever>() {
                    @Override
                    public void prepare(ReferrerFromLibraryRetriever mock, MockedConstruction.Context context) throws Throwable {
                        throw new RuntimeException();
                    }
                }
        )) {
            when(ReflectionUtils.detectClassExists(REFERRER_CLIENT_CLASS)).thenReturn(true);

            ReferrerRetrieverWrapper wrapper = new ReferrerRetrieverWrapper(mContext, executor);
            assertThat(wrapper.getReferrerRetriever()).isNotInstanceOf(ReferrerFromLibraryRetriever.class);
            wrapper.getReferrerRetriever().retrieveReferrer(mock(ReferrerReceivedListener.class));
        }
    }
}
