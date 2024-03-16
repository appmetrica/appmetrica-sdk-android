package io.appmetrica.analytics.impl.db;

import io.appmetrica.analytics.coreapi.internal.db.DatabaseScript;
import io.appmetrica.analytics.impl.GlobalServiceLocator;
import io.appmetrica.analytics.impl.modules.ModulesController;
import io.appmetrica.analytics.impl.utils.collection.HashMultimap;
import io.appmetrica.analytics.modulesapi.internal.common.TableDescription;
import io.appmetrica.analytics.modulesapi.internal.service.ModuleServicesDatabase;
import io.appmetrica.analytics.testutils.CommonTest;
import io.appmetrica.analytics.testutils.GlobalServiceLocatorRule;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@RunWith(RobolectricTestRunner.class)
public class DatabaseScriptProviderTest extends CommonTest {

    private final String commonScriptsClassNamePrefix = "io.appmetrica.analytics.impl.db.constants.DatabaseScriptsHolder$";
    private final String migrationScriptsClassNamePrefix = "io.appmetrica.analytics.impl.db.constants.migrations.";

    @Rule
    public GlobalServiceLocatorRule globalServiceLocatorRule = new GlobalServiceLocatorRule();

    private DatabaseScriptsProvider mDatabaseScriptsProvider = new DatabaseScriptsProvider();

    @Test
    public void testGetUpgradeDbScripts() throws Exception {
        assertThatHasScriptsForVersions(mDatabaseScriptsProvider.getComponentDatabaseUpgradeDbScripts(), "ComponentDatabaseUpgradeScriptToV", 112);
    }

    @Test
    public void testGetUpgradeServiceDbScripts() throws Exception {
        assertThatHasScriptsForVersions(mDatabaseScriptsProvider.getUpgradeServiceDbScripts(), "ServiceDatabaseUpgradeScriptToV");
    }

    @Test
    public void getClientDatabaseUpgradeScripts() throws Exception {
        assertThatHasScriptsForVersions(mDatabaseScriptsProvider.getClientDatabaseUpgradeScripts(), "ClientDatabaseUpgradeScriptToV", 112);
    }

    @Test
    public void getUpgradeServiceDbScriptsFromModules() {
        int firstTableFirstScriptVersion = 7;
        DatabaseScript firstTableFirstScript = mock(DatabaseScript.class);
        int firstTableSecondScriptVersion = 22;
        DatabaseScript firstTableSecondScript = mock(DatabaseScript.class);
        Map<Integer, DatabaseScript> firstTableScripts = new HashMap<>();
        firstTableScripts.put(firstTableFirstScriptVersion, firstTableFirstScript);
        firstTableScripts.put(firstTableSecondScriptVersion, firstTableSecondScript);

        int secondTableFirstScriptVersion = 7;
        DatabaseScript secondTableFirstScript = mock(DatabaseScript.class);
        int secondTableSecondScriptVersion = 43;
        DatabaseScript secondTableSecondScript = mock(DatabaseScript.class);

        Map<Integer, DatabaseScript> secondTableScripts = new HashMap<>();
        secondTableScripts.put(secondTableFirstScriptVersion, secondTableFirstScript);
        secondTableScripts.put(secondTableSecondScriptVersion, secondTableSecondScript);

        ModulesController modulesController = GlobalServiceLocator.getInstance().getModulesController();
        ModuleServicesDatabase moduleServicesDatabase = mock(ModuleServicesDatabase.class);
        TableDescription firstTableDescription = mock(TableDescription.class);
        when(firstTableDescription.getDatabaseProviderUpgradeScript()).thenReturn(firstTableScripts);
        TableDescription secondTableDescription = mock(TableDescription.class);
        when(secondTableDescription.getDatabaseProviderUpgradeScript()).thenReturn(secondTableScripts);
        when(moduleServicesDatabase.getTables())
            .thenReturn(Arrays.asList(firstTableDescription, secondTableDescription));

        when(modulesController.collectModuleServiceDatabases())
            .thenReturn(Collections.singletonList(moduleServicesDatabase));

        HashMultimap<Integer, DatabaseScript> scripts = mDatabaseScriptsProvider.getUpgradeServiceDbScripts();
        assertThat(scripts.get(7)).containsExactlyInAnyOrder(firstTableFirstScript, secondTableFirstScript);
        assertThat(scripts.get(22)).containsExactlyInAnyOrder(firstTableSecondScript);
        assertThat(scripts.get(43)).containsExactlyInAnyOrder(secondTableSecondScript);
    }

    @Test
    public void testGetDatabaseCreateScript() throws Exception {
        assertThat(mDatabaseScriptsProvider.getComponentDatabaseCreateScript()).isExactlyInstanceOf(Class.forName(commonScriptsClassNamePrefix + "ComponentDatabaseCreateScript"));
    }

    @Test
    public void testGetDatabaseDropScript() throws Exception {
        assertThat(mDatabaseScriptsProvider.getComponentDatabaseDropScript()).isExactlyInstanceOf(Class.forName(commonScriptsClassNamePrefix + "ComponentDatabaseDropScript"));
    }

    @Test
    public void testGetDatabaseClientCreateScript() throws Exception {
        assertThat(mDatabaseScriptsProvider.getDatabaseClientCreateScript()).isExactlyInstanceOf(Class.forName(commonScriptsClassNamePrefix + "DatabaseClientCreateScript"));
    }

    @Test
    public void testGetDatabaseClientDropScript() throws Exception {
        assertThat(mDatabaseScriptsProvider.getDatabaseClientDropScript()).isExactlyInstanceOf(Class.forName(commonScriptsClassNamePrefix + "DatabaseClientDropScript"));
    }

    @Test
    public void getDatabaseAutoInappCreateScript() throws Exception {
        assertThat(mDatabaseScriptsProvider.getDatabaseAutoInappCreateScript()).isExactlyInstanceOf(Class.forName(commonScriptsClassNamePrefix + "DatabaseAutoInappCreateScript"));
    }

    @Test
    public void getDatabaseAutoInappDropScript() throws Exception {
        assertThat(mDatabaseScriptsProvider.getDatabaseAutoInappDropScript()).isExactlyInstanceOf(Class.forName(commonScriptsClassNamePrefix + "DatabaseAutoInappDropScript"));
    }

    @Test
    public void testGetDatabaseProviderCreateScript() throws Exception {
        assertThat(mDatabaseScriptsProvider.getDatabaseProviderCreateScript()).isExactlyInstanceOf(Class.forName(commonScriptsClassNamePrefix + "ServiceDatabaseCreateScript"));
    }

    @Test
    public void testGetDatabaseProviderDropScript() throws Exception {
        assertThat(mDatabaseScriptsProvider.getDatabaseProviderDropScript()).isExactlyInstanceOf(Class.forName(commonScriptsClassNamePrefix + "ServiceDatabaseDropScript"));
    }

    private void assertThatHasScriptsForVersions(HashMultimap<Integer, DatabaseScript> input, String scriptNamePrefix, int... versions) throws Exception {
        Set<Integer> remained = new HashSet<>(input.keySet());

        for (int version : versions) {
            assertThat(input.get(version)).as("Script for version = %s", version).hasSize(1);
            assertThat(input.get(version).iterator().next()).isExactlyInstanceOf(Class.forName(migrationScriptsClassNamePrefix + scriptNamePrefix + version));
            remained.remove(version);
        }

        assertThat(remained.size()).as("Remained items: %s", remained).isZero();
    }

}
