package io.appmetrica.analytics.impl.db.state.factory;

import android.content.Context;
import io.appmetrica.analytics.coreutils.internal.encryption.AESEncrypter;
import io.appmetrica.analytics.impl.billing.AutoInappCollectingInfo;
import io.appmetrica.analytics.impl.billing.AutoInappCollectingInfoConverter;
import io.appmetrica.analytics.impl.billing.AutoInappCollectingInfoSerializer;
import io.appmetrica.analytics.impl.clids.ClidsInfo;
import io.appmetrica.analytics.impl.db.IBinaryDataHelper;
import io.appmetrica.analytics.impl.db.protobuf.AppPermissionsStateSerializer;
import io.appmetrica.analytics.impl.db.protobuf.ClidsInfoStateSerializer;
import io.appmetrica.analytics.impl.db.protobuf.EncryptedProtobufStateSerializer;
import io.appmetrica.analytics.impl.db.protobuf.ProtobufStateStorageImpl;
import io.appmetrica.analytics.impl.db.protobuf.StartupStateSerializer;
import io.appmetrica.analytics.impl.db.state.converter.AppPermissionsStateConverter;
import io.appmetrica.analytics.impl.db.state.converter.ClidsInfoConverter;
import io.appmetrica.analytics.impl.db.state.converter.StartupStateConverter;
import io.appmetrica.analytics.impl.permissions.AppPermissionsState;
import io.appmetrica.analytics.impl.preloadinfo.PreloadInfoData;
import io.appmetrica.analytics.impl.preloadinfo.PreloadInfoDataConverter;
import io.appmetrica.analytics.impl.preloadinfo.PreloadInfoDataSerializer;
import io.appmetrica.analytics.impl.startup.StartupStateModel;
import io.appmetrica.analytics.impl.utils.SecurityUtils;
import io.appmetrica.analytics.testutils.CommonTest;
import io.appmetrica.analytics.testutils.GlobalServiceLocatorRule;
import io.appmetrica.analytics.testutils.TestUtils;
import java.util.Arrays;
import java.util.Collection;
import org.assertj.core.api.AbstractObjectAssert;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.robolectric.ParameterizedRobolectricTestRunner;

import static org.assertj.core.api.Assertions.assertThat;

@RunWith(ParameterizedRobolectricTestRunner.class)
public class StorageFactoriesTest extends CommonTest {

    private final Class entityClass;
    private final String expectedDbKey;
    private final Class serializerClass;
    private final Class converterClass;

    public StorageFactoriesTest(Class entityClass, String expectedDbKey, Class serializerClass, Class converterClass) {
        this.entityClass = entityClass;
        this.expectedDbKey = expectedDbKey;
        this.serializerClass = serializerClass;
        this.converterClass = converterClass;
    }

    @ParameterizedRobolectricTestRunner.Parameters(name = "{0}")
    public static Collection<Object[]> data() {
        //noinspection deprecation
        return Arrays.asList(new Object[][]{
                {
                        StartupStateModel.class, "startup_state", StartupStateSerializer.class, StartupStateConverter.class
                },
                {
                        AppPermissionsState.class, "app_permissions_state", AppPermissionsStateSerializer.class,
                        AppPermissionsStateConverter.class
                },
                {
                        PreloadInfoData.class, "preload_info_data", PreloadInfoDataSerializer.class,
                        PreloadInfoDataConverter.class
                },
                {
                        AutoInappCollectingInfo.class, "auto_inapp_collecting_info_data", AutoInappCollectingInfoSerializer.class,
                        AutoInappCollectingInfoConverter.class
                },
                {
                        ClidsInfo.class, "clids_info", ClidsInfoStateSerializer.class,
                        ClidsInfoConverter.class
                },
        });
    }

    @Rule
    public final GlobalServiceLocatorRule globalServiceLocatorRule = new GlobalServiceLocatorRule();

    private Context context;
    @Mock
    private IBinaryDataHelper binaryDataHelper;
    private StorageFactoryImpl storageFactory;
    private ProtobufStateStorageImpl protobufStateStorage;

    private byte[] aesPassword;
    private byte[] aesIV;

    @Before
    public void setUp() throws Exception {
        MockitoAnnotations.openMocks(this);
        context = TestUtils.createMockedContext();
        storageFactory = (StorageFactoryImpl) StorageFactory.Provider.get(entityClass);
        protobufStateStorage = (ProtobufStateStorageImpl) storageFactory.createWithHelper(context, binaryDataHelper);

        aesPassword = SecurityUtils.getMD5Hash(context.getPackageName());
        aesIV = SecurityUtils.getMD5Hash(new StringBuilder(context.getPackageName()).reverse().toString());
    }

    @Test
    public void binaryDbRecordKey() {
        assertThat(protobufStateStorage).extracting("mKey").isEqualTo(expectedDbKey);
    }

    @Test
    public void binaryDbHelper() {
        assertThat(protobufStateStorage).extracting("mDbHelper").isEqualTo(binaryDataHelper);
    }

    @Test
    public void protobufStateSerializer() {
        AbstractObjectAssert<?, ?> objectAssert = assertThat(protobufStateStorage).extracting("mSerializer");
        objectAssert.isNotNull();
        objectAssert.isInstanceOf(EncryptedProtobufStateSerializer.class);
        objectAssert.extracting("mBackedSerializer").isNotNull().isInstanceOf(serializerClass);
        AbstractObjectAssert<?, ?> encrypterAssert = objectAssert.extracting("mEncrypter");
        encrypterAssert.isNotNull().isInstanceOf(AESEncrypter.class);
        encrypterAssert.extracting("mAlgorithm", "mPassword", "mIV")
            .containsExactly("AES/CBC/PKCS5Padding", aesPassword, aesIV);
    }

    @Test
    public void converter() {
        assertThat(protobufStateStorage).extracting("mConverter").isNotNull().isInstanceOf(converterClass);
    }
}
