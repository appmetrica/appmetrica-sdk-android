package io.appmetrica.analytics.impl.db;

import android.content.ContentValues;
import io.appmetrica.analytics.impl.component.ComponentUnit;

public class DummyDatabaseHelper extends DatabaseHelper {

    public DummyDatabaseHelper(final ComponentUnit component, final DatabaseStorage databaseStorage) {
        super(component, databaseStorage);
    }

    @Override
    public void addReportValues(final ContentValues reportValues) {
        //do nothing
    }

    @Override
    public void addSessionValues(final ContentValues sessionValues) {
        //do nothing
    }
}
