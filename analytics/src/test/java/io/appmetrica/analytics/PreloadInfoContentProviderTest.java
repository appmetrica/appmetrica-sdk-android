package io.appmetrica.analytics;

import android.content.ContentValues;
import android.content.Context;
import android.net.Uri;
import androidx.annotation.Nullable;
import io.appmetrica.analytics.impl.ContentProviderFirstLaunchHelper;
import io.appmetrica.analytics.impl.ContentProviderHelper;
import io.appmetrica.analytics.impl.preloadinfo.ContentProviderHelperFactory;
import io.appmetrica.analytics.impl.preloadinfo.PreloadInfoState;
import io.appmetrica.analytics.testutils.CommonTest;
import io.appmetrica.analytics.testutils.MockedStaticRule;
import java.util.Map;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.MockitoAnnotations;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.RuntimeEnvironment;
import org.robolectric.util.ReflectionHelpers;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@RunWith(RobolectricTestRunner.class)
public class PreloadInfoContentProviderTest extends CommonTest {

    private static final String AUTHORITY_SUFFIX = ".appmetrica.preloadinfo.retail";

    private PreloadInfoContentProvider contentProvider;
    @Mock
    private ContentProviderHelper<PreloadInfoState> preloadInfoHelper;
    @Mock
    private ContentProviderHelper<Map<String, String>> clidsInfoHelper;
    @Mock
    private ContentValues values;
    private Uri validPreloadInfoUri;
    private Uri validClidsInfoUri;
    @Rule
    public MockedStaticRule<ContentProviderHelperFactory> sContentProviderHelperFactory = 
            new MockedStaticRule<>(ContentProviderHelperFactory.class);
    @Rule
    public MockedStaticRule<ContentProviderFirstLaunchHelper> sContentProviderFirstLaunchHelper =
            new MockedStaticRule<>(ContentProviderFirstLaunchHelper.class);

    @Before
    public void setUp() {
        MockitoAnnotations.openMocks(this);
        validPreloadInfoUri = Uri.parse("content://" + RuntimeEnvironment.getApplication().getPackageName() + AUTHORITY_SUFFIX + "/preloadinfo");
        validClidsInfoUri = Uri.parse("content://" + RuntimeEnvironment.getApplication().getPackageName() + AUTHORITY_SUFFIX + "/clids");
        when(ContentProviderHelperFactory.createClidsInfoHelper()).thenReturn(clidsInfoHelper);
        when(ContentProviderHelperFactory.createPreloadInfoHelper()).thenReturn(preloadInfoHelper);
        contentProvider = new PreloadInfoContentProvider();
    }

    @Test
    public void onCreate() {
        assertThat(contentProvider.onCreate()).isTrue();
        sContentProviderFirstLaunchHelper.getStaticMock().verify(new MockedStatic.Verification() {
            @Override
            public void apply() throws Throwable {
                ContentProviderFirstLaunchHelper.onCreate(contentProvider);
            }
        });
    }

    @Test
    public void insertPreloadInfoWithoutOnCreate() {
        setContentProviderContext(RuntimeEnvironment.getApplication());
        assertThat(contentProvider.insert(validPreloadInfoUri, values)).isNull();
        verifyNoInteractions(preloadInfoHelper, clidsInfoHelper);
        verifyOnInsertFinished();
    }

    @Test
    public void insertClidsInfoWithoutOnCreate() {
        setContentProviderContext(RuntimeEnvironment.getApplication());
        assertThat(contentProvider.insert(validClidsInfoUri, values)).isNull();
        verifyNoInteractions(preloadInfoHelper, clidsInfoHelper);
        verifyOnInsertFinished();
    }

    @Test
    public void insertPreloadInfoWithoutContextAtAll() {
        contentProvider.onCreate();
        assertThat(contentProvider.insert(validPreloadInfoUri, values)).isNull();
        verifyNoInteractions(preloadInfoHelper, clidsInfoHelper);
        verifyOnInsertFinished();
    }

    @Test
    public void insertClidsInfoWithoutContextAtAll() {
        contentProvider.onCreate();
        assertThat(contentProvider.insert(validClidsInfoUri, values)).isNull();
        verifyNoInteractions(preloadInfoHelper, clidsInfoHelper);
        verifyOnInsertFinished();
    }

    @Test
    public void insertPreloadInfoWithoutContextInOnCreate() {
        contentProvider.onCreate();
        setContentProviderContext(RuntimeEnvironment.getApplication());
        assertThat(contentProvider.insert(validPreloadInfoUri, values)).isNull();
        verifyNoInteractions(preloadInfoHelper, clidsInfoHelper);
        verifyOnInsertFinished();
    }

    @Test
    public void insertClidsInfoWithoutContextInOnCreate() {
        contentProvider.onCreate();
        setContentProviderContext(RuntimeEnvironment.getApplication());
        assertThat(contentProvider.insert(validClidsInfoUri, values)).isNull();
        verifyNoInteractions(preloadInfoHelper, clidsInfoHelper);
        verifyOnInsertFinished();
    }

    @Test
    public void insertBadUri() {
        setContentProviderContext(RuntimeEnvironment.getApplication());
        contentProvider.onCreate();
        Uri uri = Uri.parse(validPreloadInfoUri.toString() + "/1");
        assertThat(contentProvider.insert(uri, values)).isNull();
        verifyNoInteractions(preloadInfoHelper, clidsInfoHelper);
        verifyOnInsertFinished();
    }

    @Test
    public void insertPreloadInfoValuesAreNull() {
        setContentProviderContext(RuntimeEnvironment.getApplication());
        contentProvider.onCreate();
        assertThat(contentProvider.insert(validPreloadInfoUri, null)).isNull();
        verifyNoInteractions(preloadInfoHelper, clidsInfoHelper);
        verifyOnInsertFinished();
    }

    @Test
    public void insertClidsInfoValuesAreNull() {
        setContentProviderContext(RuntimeEnvironment.getApplication());
        contentProvider.onCreate();
        assertThat(contentProvider.insert(validClidsInfoUri, null)).isNull();
        verifyNoInteractions(preloadInfoHelper, clidsInfoHelper);
        verifyOnInsertFinished();
    }

    @Test
    public void insertPreloadInfoNoContext() {
        setContentProviderContext(null);
        contentProvider.onCreate();
        contentProvider.insert(validPreloadInfoUri, values);
        verifyNoInteractions(preloadInfoHelper, clidsInfoHelper);
        verifyOnInsertFinished();
    }

    @Test
    public void insertClidsInfoNoContext() {
        setContentProviderContext(null);
        contentProvider.onCreate();
        contentProvider.insert(validClidsInfoUri, values);
        verifyNoInteractions(preloadInfoHelper, clidsInfoHelper);
        verifyOnInsertFinished();
    }

    @Test
    public void insertPreloadInfoEverythingIsFine() {
        setContentProviderContext(RuntimeEnvironment.getApplication());
        contentProvider.onCreate();
        assertThat(contentProvider.insert(validPreloadInfoUri, values)).isNull();
        verify(preloadInfoHelper).handle(RuntimeEnvironment.getApplication(), values);
        verifyNoInteractions(clidsInfoHelper);
        verifyOnInsertFinished();
    }

    @Test
    public void insertClidsInfoEverythingIsFine() {
        setContentProviderContext(RuntimeEnvironment.getApplication());
        contentProvider.onCreate();
        assertThat(contentProvider.insert(validClidsInfoUri, values)).isNull();
        verify(clidsInfoHelper).handle(RuntimeEnvironment.getApplication(), values);
        verifyNoInteractions(preloadInfoHelper);
        verifyOnInsertFinished();
    }

    @Test
    public void insertPreloadInfoAfterDisable() {
        setContentProviderContext(RuntimeEnvironment.getApplication());
        contentProvider.onCreate();
        contentProvider.disable();
        assertThat(contentProvider.insert(validPreloadInfoUri, values)).isNull();
        verifyNoInteractions(preloadInfoHelper, clidsInfoHelper);
        sContentProviderFirstLaunchHelper.getStaticMock().verify(new MockedStatic.Verification() {
            @Override
            public void apply() throws Throwable {
                ContentProviderFirstLaunchHelper.onInsertFinished();
            }
        }, never());
    }

    @Test
    public void insertClidsInfoAfterDisable() {
        setContentProviderContext(RuntimeEnvironment.getApplication());
        contentProvider.onCreate();
        contentProvider.disable();
        assertThat(contentProvider.insert(validClidsInfoUri, values)).isNull();
        verifyNoInteractions(preloadInfoHelper, clidsInfoHelper);
        sContentProviderFirstLaunchHelper.getStaticMock().verify(new MockedStatic.Verification() {
            @Override
            public void apply() throws Throwable {
                ContentProviderFirstLaunchHelper.onInsertFinished();
            }
        }, never());
    }

    @Test
    public void delete() {
        setContentProviderContext(RuntimeEnvironment.getApplication());
        contentProvider.onCreate();
        assertThat(contentProvider.delete(validPreloadInfoUri, null, null)).isEqualTo(-1);
        assertThat(contentProvider.delete(validClidsInfoUri, null, null)).isEqualTo(-1);
    }

    @Test
    public void update() {
        setContentProviderContext(RuntimeEnvironment.getApplication());
        contentProvider.onCreate();
        assertThat(contentProvider.update(validPreloadInfoUri, values, null, null)).isEqualTo(-1);
        assertThat(contentProvider.update(validClidsInfoUri, values, null, null)).isEqualTo(-1);
    }

    @Test
    public void query() {
        setContentProviderContext(RuntimeEnvironment.getApplication());
        contentProvider.onCreate();
        assertThat(contentProvider.query(validPreloadInfoUri, null, null, null, null)).isNull();
        assertThat(contentProvider.query(validClidsInfoUri, null, null, null, null)).isNull();
    }

    @Test
    public void getTypeIsNull() {
        setContentProviderContext(RuntimeEnvironment.getApplication());
        contentProvider.onCreate();
        assertThat(contentProvider.getType(validPreloadInfoUri)).isNull();
        assertThat(contentProvider.getType(validClidsInfoUri)).isNull();
    }

    private void setContentProviderContext(@Nullable Context context) {
        ReflectionHelpers.setField(contentProvider, "mContext", context);
    }

    private void verifyOnInsertFinished() {
        sContentProviderFirstLaunchHelper.getStaticMock().verify(new MockedStatic.Verification() {
            @Override
            public void apply() throws Throwable {
                ContentProviderFirstLaunchHelper.onInsertFinished();
            }
        });
    }
}
