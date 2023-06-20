package io.appmetrica.analytics.impl.db.storage;

import io.appmetrica.analytics.impl.db.DatabaseStorage;

public class MockedKeyValueTableDbHelper extends KeyValueTableDbHelper {
    public MockedKeyValueTableDbHelper(final DatabaseStorage dbStorage) {
        super(dbStorage);
    }

    @Override
    public void commit() {
        super.commit();
    }
}
