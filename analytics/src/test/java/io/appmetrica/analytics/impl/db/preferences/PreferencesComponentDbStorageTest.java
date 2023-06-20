package io.appmetrica.analytics.impl.db.preferences;

import io.appmetrica.analytics.impl.AppEnvironment;
import io.appmetrica.analytics.impl.db.storage.MockedKeyValueTableDbHelper;
import io.appmetrica.analytics.testutils.CommonTest;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.when;

@RunWith(RobolectricTestRunner.class)
public class PreferencesComponentDbStorageTest extends CommonTest {

    private MockedKeyValueTableDbHelper dbStorage;
    private PreferencesComponentDbStorage mComponentDbStorage;

    @Before
    public void setUp() throws Exception {
        dbStorage = spy(new MockedKeyValueTableDbHelper(null));
        doNothing().when(dbStorage).commit();
        mComponentDbStorage = new PreferencesComponentDbStorage(dbStorage);
    }

    @Test
    public void testMissingAppEnvironmentRevision() {
        AppEnvironment.EnvironmentRevision revision = mComponentDbStorage.getAppEnvironmentRevision();
        assertThat(revision.value).isEqualTo("{}");
        assertThat(revision.revisionNumber).isEqualTo(0);
    }

    @Test
    public void testPutAppEnvironmentRevision() {
        AppEnvironment.EnvironmentRevision revision = new AppEnvironment.EnvironmentRevision("value", 9);
        mComponentDbStorage.putAppEnvironmentRevision(revision);

        AppEnvironment.EnvironmentRevision loadedRevision = mComponentDbStorage.getAppEnvironmentRevision();

        assertThat(loadedRevision.value).isEqualTo(revision.value);
        assertThat(loadedRevision.revisionNumber).isEqualTo(revision.revisionNumber);
    }

    @Test
    public void testMissingCertificatesSha1Fingerprints() {
        assertThat(mComponentDbStorage.getCertificatesSha1Fingerprints()).isNotNull().isEmpty();
    }

    @Test
    public void testPutCertificatesSha1Fingerprints() {
        final List<String> certificates = Arrays.asList("AA:BB:CC:DD:EE:FF:00:11", "22:33:44:55:66:77:88:99");
        mComponentDbStorage.putCertificatesSha1Fingerprints(certificates);
        assertThat(mComponentDbStorage.getCertificatesSha1Fingerprints()).isEqualTo(certificates);
    }

    @Test
    public void testSaveProfileID() {
        final String profileID = "profileID";
        mComponentDbStorage.putProfileID(profileID);
        assertThat(mComponentDbStorage.getProfileID()).isEqualTo(profileID);
    }

    @Test
    public void testEmptyProfileID() {
        assertThat(mComponentDbStorage.getProfileID()).isNull();
    }

    @Test
    public void testMissingPermissionsEventSendTime() {
        assertThat(mComponentDbStorage.getPermissionsEventSendTime()).isEqualTo(0);
    }

    @Test
    public void testSavePermissionsCheckTime() {
        long time = 123456;
        mComponentDbStorage.putPermissionsCheckTime(time);
        assertThat(mComponentDbStorage.getPermissionsEventSendTime()).isEqualTo(time);
    }

    @Test
    public void testMissingLastAppVersionWithFeatures() {
        assertThat(mComponentDbStorage.getLastAppVersionWithFeatures()).isEqualTo(-1);
    }

    @Test
    public void testSaveLastAppVersionWithFeatures() {
        int version = 21;
        mComponentDbStorage.putLastAppVersionWithFeatures(version);
        assertThat(mComponentDbStorage.getLastAppVersionWithFeatures()).isEqualTo(version);
    }

    @Test
    public void testMissingApplicationFeatures() {
        assertThat(mComponentDbStorage.getApplicationFeatures()).isEqualTo("");
    }

    @Test
    public void testSaveApplicationFeatures() {
        String features = "features";
        mComponentDbStorage.putApplicationFeatures(features);
        assertThat(mComponentDbStorage.getApplicationFeatures()).isEqualTo(features);
    }

    @Test
    public void testMissingLastMigrationVersion() {
        assertThat(mComponentDbStorage.getLastMigrationVersion()).isNull();
    }

    @Test
    public void hasLastMigrationVersion() {
        mComponentDbStorage.writeLong("LAST_MIGRATION_VERSION", 5);
        assertThat(mComponentDbStorage.getLastMigrationVersion()).isEqualTo(5);
    }

    @Test
    public void removeLastMigrationVersion() {
        mComponentDbStorage.writeLong("LAST_MIGRATION_VERSION", 5);
        mComponentDbStorage.removeLastMigrationVersion();
        assertThat(mComponentDbStorage.getLastMigrationVersion()).isNull();
    }

    @Test
    public void testMissingSessionParameters() {
        assertThat(mComponentDbStorage.getSessionParameters("")).isEqualTo("");
    }

    @Test
    public void testSaveSessionParameters() {
        String tag = "tag";
        String value = "value";
        mComponentDbStorage.putSessionParameters(tag, value);
        assertThat(mComponentDbStorage.getSessionParameters(tag)).isEqualTo(value);
    }

    @Test
    public void keysAreNotNull() {
        Set<String> keys = new HashSet<>();
        keys.add("key1");
        keys.add("key2");
        when(dbStorage.keys()).thenReturn(keys);
        assertThat(mComponentDbStorage.keys()).isSameAs(keys);
    }

    @Test
    public void getVitalDataInitial() {
        assertThat(mComponentDbStorage.getVitalData()).isNull();
    }

    @Test
    public void writeVitalData() {
        String data = "234fsdfdasdsfd";
        mComponentDbStorage.putVitalData(data);
        assertThat(mComponentDbStorage.getVitalData()).isEqualTo(data);
    }

    @Test
    public void overwriteFilledVitalData() {
        String data = "dggfhgdsfdsa";
        mComponentDbStorage.putVitalData("safdsdgfa");
        mComponentDbStorage.putVitalData(data);
        assertThat(mComponentDbStorage.getVitalData()).isEqualTo(data);
    }
}
