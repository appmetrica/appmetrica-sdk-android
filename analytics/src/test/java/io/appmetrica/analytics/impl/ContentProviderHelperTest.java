package io.appmetrica.analytics.impl;

import android.content.ContentValues;
import android.content.Context;
import io.appmetrica.analytics.impl.preloadinfo.PreloadInfoState;
import io.appmetrica.analytics.testutils.CommonTest;
import io.appmetrica.analytics.testutils.GlobalServiceLocatorRule;
import io.appmetrica.analytics.testutils.TestUtils;
import org.json.JSONException;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InOrder;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.robolectric.RobolectricTestRunner;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@RunWith(RobolectricTestRunner.class)
public class ContentProviderHelperTest extends CommonTest {

    @Mock
    private FirstServiceEntryPointManager firstServiceEntryPointManager;
    @Mock
    private ContentProviderDataParser<PreloadInfoState> dataParser;
    @Mock
    private ContentProviderDataSaver<PreloadInfoState> dataSaver;
    private ContentProviderHelper<PreloadInfoState> content;
    private Context context;
    @Rule
    public final GlobalServiceLocatorRule globalServiceLocatorRule = new GlobalServiceLocatorRule();

    @Before
    public void setUp() throws JSONException {
        MockitoAnnotations.openMocks(this);
        context = TestUtils.createMockedContext();
        content = new ContentProviderHelper<>(
            dataParser,
            dataSaver,
            firstServiceEntryPointManager,
            "preload_info"
        );
    }

    @Test
    public void parsedDataIsNull() {
        ContentValues values = new ContentValues();
        when(dataParser.invoke(values)).thenReturn(null);
        content.handle(context, values);
        verify(dataParser).invoke(values);
        verifyNoInteractions(dataSaver, firstServiceEntryPointManager);
    }

    @Test
    public void dataParserThrowsException() {
        ContentValues values = new ContentValues();
        when(dataParser.invoke(values)).thenThrow(new RuntimeException());
        content.handle(context, values);
        verify(dataParser).invoke(values);
        verifyNoInteractions(dataSaver, firstServiceEntryPointManager);
    }

    @Test
    public void parsedDataIsNotNull() {
        PreloadInfoState parsedData = mock(PreloadInfoState.class);
        ContentValues values = new ContentValues();
        when(dataParser.invoke(values)).thenReturn(parsedData);
        content.handle(context, values);
        verify(dataParser).invoke(values);
        InOrder inOrder = Mockito.inOrder(firstServiceEntryPointManager, dataSaver);
        inOrder.verify(firstServiceEntryPointManager).onPossibleFirstEntry(context);
        inOrder.verify(dataSaver).invoke(parsedData);
    }

    @Test
    public void noGlobalServiceLocator() {
        GlobalServiceLocator.destroy();
        ContentValues values = new ContentValues();
        content.handle(context, values);
    }
}
