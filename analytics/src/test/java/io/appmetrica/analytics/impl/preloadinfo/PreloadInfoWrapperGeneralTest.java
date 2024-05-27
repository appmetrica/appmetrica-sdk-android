package io.appmetrica.analytics.impl.preloadinfo;

import io.appmetrica.analytics.PreloadInfo;
import io.appmetrica.analytics.impl.utils.PublicLogger;
import io.appmetrica.analytics.testutils.CommonTest;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.robolectric.RobolectricTestRunner;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;

@RunWith(RobolectricTestRunner.class)
public class PreloadInfoWrapperGeneralTest extends CommonTest {

    private static final String sErrorMessage = "Required field \"PreloadInfo.trackingId\" is empty!\n" +
            "This preload info will be skipped.";

    @Mock
    private PublicLogger mPublicLogger;

    @Before
    public void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    public void validPreloadInfo() {
        new PreloadInfoWrapper(PreloadInfo.newBuilder("1111").build(), mPublicLogger, true);
        verifyNoMoreInteractions(mPublicLogger);
    }

    @Test
    public void nullPreloadInfo() {
        new PreloadInfoWrapper(null, mPublicLogger, true);
        verifyNoMoreInteractions(mPublicLogger);
    }

    @Test
    public void nullTrackingId() {
        new PreloadInfoWrapper(PreloadInfo.newBuilder(null).build(), mPublicLogger, true);
        verify(mPublicLogger).error(sErrorMessage);
    }

    @Test
    public void emptyTrackingId() {
        new PreloadInfoWrapper(PreloadInfo.newBuilder("").build(), mPublicLogger, true);
        verify(mPublicLogger).error(sErrorMessage);
    }
}
