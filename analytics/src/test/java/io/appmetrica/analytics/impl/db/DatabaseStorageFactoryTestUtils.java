package io.appmetrica.analytics.impl.db;

import android.content.Context;
import io.appmetrica.analytics.impl.GlobalServiceLocator;
import io.appmetrica.analytics.impl.db.storage.MockedKeyValueTableDbHelper;

import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.when;

public class DatabaseStorageFactoryTestUtils {

    public static void mockNonComponentDatabases(final Context context) {
        try {
            clearStorages(context);
            IKeyValueTableDbHelper helperForService = spy(new MockedKeyValueTableDbHelper(null));
            doNothing().when(helperForService).commit();
            when(GlobalServiceLocator.getInstance().getStorageFactory().getServicePreferenceDbHelper(context))
                .thenReturn(helperForService);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private static void clearStorages(final Context context) throws NoSuchFieldException, IllegalAccessException {
        when(GlobalServiceLocator.getInstance().getStorageFactory().getServicePreferenceDbHelper(context))
            .thenReturn(new MockedKeyValueTableDbHelper(null));
    }
}
