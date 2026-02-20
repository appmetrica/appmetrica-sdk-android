package io.appmetrica.analytics.impl.db;

import io.appmetrica.analytics.coreapi.internal.db.DatabaseScript;
import io.appmetrica.analytics.impl.component.ComponentId;
import io.appmetrica.analytics.impl.db.constants.Constants;
import io.appmetrica.analytics.impl.db.constants.TempCacheTable;
import io.appmetrica.analytics.impl.utils.collection.HashMultimap;
import io.appmetrica.analytics.testutils.CommonTest;
import io.appmetrica.analytics.testutils.GlobalServiceLocatorRule;
import java.util.AbstractMap;
import java.util.HashMap;
import java.util.List;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.ArgumentMatcher;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.same;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class DatabaseManagerProviderTest extends CommonTest {

    @Mock
    private DatabaseScriptsProvider mDatabaseScriptsProvider;
    @Mock
    private DbTablesColumnsProvider mDbTablesColumnsProvider;
    @Mock
    private HashMultimap<Integer, DatabaseScript> mUpgradeScripts;
    @Mock
    private HashMultimap<Integer, DatabaseScript> mUpgradeProviderScripts;
    @Mock
    private HashMultimap<Integer, DatabaseScript> clientDatabaseUpgradeScript;
    @Mock
    private HashMap<String, List<String>> mDbTablesColumns;
    @Mock
    private DatabaseScript mDatabaseCreateScript;
    @Mock
    private DatabaseScript mDatabaseDropScript;
    @Mock
    private DatabaseScript mDatabaseProviderCreateScript;
    @Mock
    private DatabaseScript mDatabaseProviderDropScript;
    @Mock
    private DatabaseScript mDatabaseClientCreateScript;
    @Mock
    private DatabaseScript mDatabaseClientDropScript;
    @Mock
    private DatabaseScript databaseAutoInappCreateScript;
    @Mock
    private DatabaseScript databaseAutoInappDropScript;
    @Mock
    private TablesManager.Creator mTablesManagerCreator;
    @Captor
    private ArgumentCaptor<TablesValidator> mTablesValidatorCaptor;

    private DatabaseManagerProvider mDatabaseManagerProvider;

    @Rule
    public GlobalServiceLocatorRule globalServiceLocatorRule = new GlobalServiceLocatorRule();

    @Before
    public void setUp() {
        MockitoAnnotations.openMocks(this);
        when(mDatabaseScriptsProvider.getComponentDatabaseUpgradeDbScripts()).thenReturn(mUpgradeScripts);
        when(mDatabaseScriptsProvider.getUpgradeServiceDbScripts()).thenReturn(mUpgradeProviderScripts);
        when(mDatabaseScriptsProvider.getComponentDatabaseCreateScript()).thenReturn(mDatabaseCreateScript);
        when(mDatabaseScriptsProvider.getComponentDatabaseDropScript()).thenReturn(mDatabaseDropScript);
        when(mDatabaseScriptsProvider.getDatabaseProviderCreateScript()).thenReturn(mDatabaseProviderCreateScript);
        when(mDatabaseScriptsProvider.getDatabaseProviderDropScript()).thenReturn(mDatabaseProviderDropScript);
        when(mDatabaseScriptsProvider.getDatabaseClientCreateScript()).thenReturn(mDatabaseClientCreateScript);
        when(mDatabaseScriptsProvider.getDatabaseClientDropScript()).thenReturn(mDatabaseClientDropScript);
        when(mDatabaseScriptsProvider.getClientDatabaseUpgradeScripts()).thenReturn(clientDatabaseUpgradeScript);
        when(mDatabaseScriptsProvider.getDatabaseAutoInappCreateScript()).thenReturn(databaseAutoInappCreateScript);
        when(mDatabaseScriptsProvider.getDatabaseAutoInappDropScript()).thenReturn(databaseAutoInappDropScript);
        when(mDbTablesColumnsProvider.getDbTablesColumns()).thenReturn(mDbTablesColumns);
        mDatabaseManagerProvider = new DatabaseManagerProvider(mDatabaseScriptsProvider, mDbTablesColumnsProvider, mTablesManagerCreator);
    }

    @Test
    public void testBuildMainDatabaseManager() {
        String apiKey = "apiKey";
        mDatabaseManagerProvider.buildComponentDatabaseManager(new ComponentId("package", apiKey));
        verify(mTablesManagerCreator).createTablesManager(
            eq("component-" + apiKey),
            same(mDatabaseCreateScript),
            same(mDatabaseDropScript),
            same(mUpgradeScripts),
            mTablesValidatorCaptor.capture()
        );
        assertThat(((TablesValidatorImpl) mTablesValidatorCaptor.getValue()).getTableColumnsToCheck()).isEqualTo(mDbTablesColumns);
    }

    @Test
    public void buildAutoInappDatabaseManager() {
        mDatabaseManagerProvider.buildAutoInappDatabaseManager();
        verify(mTablesManagerCreator).createTablesManager(
            eq("auto_inapp"),
            same(databaseAutoInappCreateScript),
            same(databaseAutoInappDropScript),
            argThat(new ArgumentMatcher<HashMultimap<Integer, DatabaseScript>>() {
                @Override
                public boolean matches(HashMultimap<Integer, DatabaseScript> argument) {
                    return argument.size() == 0;
                }
            }),
            mTablesValidatorCaptor.capture()
        );
        assertThat(((TablesValidatorImpl) mTablesValidatorCaptor.getValue()).getTableColumnsToCheck()).containsOnly(
            new AbstractMap.SimpleEntry(Constants.BinaryDataTable.TABLE_NAME, Constants.BinaryDataTable.ACTUAL_COLUMNS)
        );
    }

    @Test
    public void testBuildClientDatabaseManager() {
        mDatabaseManagerProvider.buildClientDatabaseManager();
        verify(mTablesManagerCreator).createTablesManager(
            eq("client database"),
            same(mDatabaseClientCreateScript),
            same(mDatabaseClientDropScript),
            same(clientDatabaseUpgradeScript),
            mTablesValidatorCaptor.capture()
        );
        assertThat(((TablesValidatorImpl) mTablesValidatorCaptor.getValue()).getTableColumnsToCheck()).containsOnly(
            new AbstractMap.SimpleEntry(Constants.PreferencesTable.TABLE_NAME, Constants.PreferencesTable.ACTUAL_COLUMNS)
        );
    }

    @Test
    public void testBuildServiceDatabaseManager() {
        mDatabaseManagerProvider.buildServiceDatabaseManager();
        verify(mTablesManagerCreator).createTablesManager(
            eq("service database"),
            same(mDatabaseProviderCreateScript),
            same(mDatabaseProviderDropScript),
            same(mUpgradeProviderScripts),
            mTablesValidatorCaptor.capture()
        );
        assertThat(((TablesValidatorImpl) mTablesValidatorCaptor.getValue()).getTableColumnsToCheck()).containsOnly(
            new AbstractMap.SimpleEntry(Constants.PreferencesTable.TABLE_NAME, Constants.PreferencesTable.ACTUAL_COLUMNS),
            new AbstractMap.SimpleEntry(Constants.BinaryDataTable.TABLE_NAME, Constants.BinaryDataTable.ACTUAL_COLUMNS),
            new AbstractMap.SimpleEntry(TempCacheTable.TABLE_NAME, TempCacheTable.COLUMNS)
        );
    }
}
