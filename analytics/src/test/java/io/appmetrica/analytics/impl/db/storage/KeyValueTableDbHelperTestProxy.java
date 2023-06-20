package io.appmetrica.analytics.impl.db.storage;

import io.appmetrica.analytics.impl.db.DatabaseStorage;

@Deprecated
public class KeyValueTableDbHelperTestProxy extends KeyValueTableDbHelper {

    public KeyValueTableDbHelperTestProxy(DatabaseStorage dbStorage, String tableName) {
        super(dbStorage, tableName);
    }

    @Override
    public void close() {
        super.close();
    }
}
