package io.appmetrica.analytics.impl.db;

import android.content.Context;
import io.appmetrica.analytics.impl.db.storage.DatabaseStorageFactory;
import io.appmetrica.analytics.impl.db.storage.MockedKeyValueTableDbHelper;
import java.lang.reflect.Field;
import java.util.Map;

import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.spy;

public class DatabaseStorageFactoryTestUtils {

    public static void mockNonComponentDatabases(final Context context) {
        try {
            clearStorages(context);
            IKeyValueTableDbHelper helperForService = spy(new MockedKeyValueTableDbHelper(null));
            doNothing().when(helperForService).commit();
            DatabaseStorageFactory.getInstance(context).setServicePreferencesHelper(helperForService);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private static void clearStorages(final Context context) throws NoSuchFieldException, IllegalAccessException {
        Field field = DatabaseStorageFactory.class.getDeclaredField("databaseStorages");
        field.setAccessible(true);
        DatabaseStorageFactory instance = DatabaseStorageFactory.getInstance(context);
        ((Map) field.get(instance)).clear();

        field = DatabaseStorageFactory.class.getDeclaredField("mDbHelpers");
        field.setAccessible(true);
        ((Map) field.get(instance)).clear();
    }
}
