package io.appmetrica.analytics.impl.db;

import io.appmetrica.analytics.impl.db.constants.Constants;
import io.appmetrica.analytics.testutils.CommonTest;
import java.util.AbstractMap;
import java.util.List;
import org.junit.Test;

import static org.assertj.core.api.Assertions.assertThat;

public class DbTablesColumnsProviderTest extends CommonTest {

    private final DbTablesColumnsProvider mDbTablesColumnsProvider = new DbTablesColumnsProvider();

    @Test
    public void testGetDbTablesColumns() {
        assertThat(mDbTablesColumnsProvider.getDbTablesColumns()).containsOnly(
            new AbstractMap.SimpleEntry<String, List<String>>(Constants.EventsTable.TABLE_NAME, Constants.EventsTable.ACTUAL_COLUMNS),
            new AbstractMap.SimpleEntry<String, List<String>>(Constants.SessionTable.TABLE_NAME, Constants.SessionTable.ACTUAL_COLUMNS),
            new AbstractMap.SimpleEntry<String, List<String>>(Constants.PreferencesTable.TABLE_NAME, Constants.PreferencesTable.ACTUAL_COLUMNS),
            new AbstractMap.SimpleEntry<String, List<String>>(Constants.BinaryDataTable.TABLE_NAME, Constants.BinaryDataTable.ACTUAL_COLUMNS)
        );
    }
}
