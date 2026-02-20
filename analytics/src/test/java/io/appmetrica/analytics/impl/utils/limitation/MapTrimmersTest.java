package io.appmetrica.analytics.impl.utils.limitation;

import io.appmetrica.analytics.logger.appmetrica.internal.PublicLogger;
import io.appmetrica.analytics.testutils.CommonTest;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class MapTrimmersTest extends CommonTest {

    @Mock
    private PublicLogger mPublicLogger;
    @Mock
    private CollectionLimitation mLimitation;
    @Mock
    private StringTrimmer mKeyTrimmer;
    @Mock
    private StringTrimmer mValueTrimmer;
    private final String mTag = "TAG";

    private MapTrimmers mMapTrimmers;

    @Before
    public void setUp() {
        MockitoAnnotations.openMocks(this);
        mMapTrimmers = new MapTrimmers(mLimitation, mKeyTrimmer, mValueTrimmer, mTag, mPublicLogger);
    }

    @Test
    public void testGetCollectionLimitation() {
        assertThat(mMapTrimmers.getCollectionLimitation()).isEqualTo(mLimitation);
    }

    @Test
    public void testGetKeyTrimmer() {
        assertThat(mMapTrimmers.getKeyTrimmer()).isEqualTo(mKeyTrimmer);
    }

    @Test
    public void testGetValueTrimmer() {
        assertThat(mMapTrimmers.getValueTrimmer()).isEqualTo(mValueTrimmer);
    }

    @Test
    public void testLogContainerLimitReachedLoggerEnabled() {
        final String key = "key";
        final int maxSize = 30;
        when(mLimitation.getMaxSize()).thenReturn(maxSize);
        mMapTrimmers.logContainerLimitReached(key);
        verify(mPublicLogger).warning(anyString(), eq(mTag), eq(maxSize), eq(key));
    }
}
