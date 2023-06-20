package io.appmetrica.analytics.impl.db.state.factory;

import android.content.Context;
import androidx.annotation.NonNull;
import io.appmetrica.analytics.coreapi.internal.data.ProtobufStateStorage;
import io.appmetrica.analytics.impl.billing.AutoInappCollectingInfo;
import io.appmetrica.analytics.impl.billing.AutoInappCollectingInfoConverter;
import io.appmetrica.analytics.impl.clids.ClidsInfo;
import io.appmetrica.analytics.impl.db.IBinaryDataHelper;
import io.appmetrica.analytics.impl.db.protobuf.ProtobufStateStorageImpl;
import io.appmetrica.analytics.impl.db.state.converter.AppPermissionsStateConverter;
import io.appmetrica.analytics.impl.db.state.converter.ClidsInfoConverter;
import io.appmetrica.analytics.impl.db.state.converter.StartupStateConverter;
import io.appmetrica.analytics.impl.db.storage.DatabaseStorageFactory;
import io.appmetrica.analytics.impl.permissions.AppPermissionsState;
import io.appmetrica.analytics.impl.preloadinfo.PreloadInfoData;
import io.appmetrica.analytics.impl.preloadinfo.PreloadInfoDataConverter;
import io.appmetrica.analytics.impl.protobuf.client.AppPermissionsStateProtobuf;
import io.appmetrica.analytics.impl.protobuf.client.AutoInappCollectingInfoProto;
import io.appmetrica.analytics.impl.protobuf.client.ClidsInfoProto;
import io.appmetrica.analytics.impl.protobuf.client.PreloadInfoProto;
import io.appmetrica.analytics.impl.protobuf.client.StartupStateProtobuf;
import io.appmetrica.analytics.impl.startup.StartupStateModel;
import java.util.Collection;
import java.util.HashMap;

public interface StorageFactory<T> {

    ProtobufStateStorage<T> create(@NonNull Context context);

    ProtobufStateStorage<T> createForMigration(@NonNull Context context);

    class Provider {

        private static final class InstanceHolder {
            static final Provider INSTANCE = new Provider();
        }

        public static <T> StorageFactory<T> get(Class<T> clazz) {
            return InstanceHolder.INSTANCE.getInternal(clazz);
        }

        public static <T> StorageFactory<Collection<T>> getFactoryForCollection(Class<T> clazz) {
            return InstanceHolder.INSTANCE.getInternalForCollection(clazz);
        }

        private final HashMap<Class<?>, StorageFactory<?>> mFactories = new HashMap<Class<?>, StorageFactory<?>>();
        private final StorageFactory<StartupStateModel> mStartupStateStorageFactory =
                new StorageFactoryImpl<StartupStateModel>() {
                    @NonNull
                    @Override
                    protected ProtobufStateStorage<StartupStateModel> createWithHelper(
                            @NonNull Context context,
                            @NonNull IBinaryDataHelper helper
                    ) {
                        return new ProtobufStateStorageImpl<StartupStateModel, StartupStateProtobuf.StartupState>(
                                "startup_state",
                                helper,
                                new StateSerializerFactory(context).createStartupStateSerializer(),
                                new StartupStateConverter()
                        );
                    }

                    @NonNull
                    @Override
                    protected IBinaryDataHelper getMainBinaryDataHelper(@NonNull Context context) {
                        return DatabaseStorageFactory.getInstance(context).getServiceBinaryDataHelper();
                    }

                    @NonNull
                    @Override
                    protected IBinaryDataHelper getMigrationBinaryDataHelper(@NonNull Context context) {
                        return DatabaseStorageFactory.getInstance(context).getServiceBinaryDataHelperForMigration();
                    }
                };

        private final StorageFactory<AppPermissionsState> mAppPermissionsStateStorageFactory =
                new StorageFactoryImpl<AppPermissionsState>() {
                    @NonNull
                    @Override
                    protected ProtobufStateStorage<AppPermissionsState> createWithHelper(
                            @NonNull Context context,
                            @NonNull IBinaryDataHelper helper
                    ) {
                        return new ProtobufStateStorageImpl<AppPermissionsState,
                                AppPermissionsStateProtobuf.AppPermissionsState>(
                                "app_permissions_state",
                                helper,
                                new StateSerializerFactory(context).createAppPermissionsStateSerializer(),
                                new AppPermissionsStateConverter()
                        );
                    }

                    @NonNull
                    @Override
                    protected IBinaryDataHelper getMainBinaryDataHelper(@NonNull Context context) {
                        return DatabaseStorageFactory.getInstance(context).getServiceBinaryDataHelper();
                    }

                    @NonNull
                    @Override
                    protected IBinaryDataHelper getMigrationBinaryDataHelper(@NonNull Context context) {
                        return DatabaseStorageFactory.getInstance(context).getServiceBinaryDataHelperForMigration();
                    }
                };

        private final StorageFactory<PreloadInfoData> preloadInfoDataStorageFactory =
                new StorageFactoryImpl<PreloadInfoData>() {
                    @NonNull
                    @Override
                    protected ProtobufStateStorage<PreloadInfoData> createWithHelper(
                            @NonNull Context context,
                            @NonNull IBinaryDataHelper helper
                    ) {
                        return new ProtobufStateStorageImpl<PreloadInfoData, PreloadInfoProto.PreloadInfoData>(
                                "preload_info_data",
                                helper,
                                new StateSerializerFactory(context).createPreloadInfoDataSerializer(),
                                new PreloadInfoDataConverter()
                        );
                    }

                    @NonNull
                    @Override
                    protected IBinaryDataHelper getMainBinaryDataHelper(@NonNull Context context) {
                        return DatabaseStorageFactory.getInstance(context).getServiceBinaryDataHelper();
                    }

                    @NonNull
                    @Override
                    protected IBinaryDataHelper getMigrationBinaryDataHelper(@NonNull Context context) {
                        return DatabaseStorageFactory.getInstance(context).getServiceBinaryDataHelperForMigration();
                    }
                };

        private final StorageFactory<AutoInappCollectingInfo> autoInappCollectingStorageFactory =
                new StorageFactoryImpl<AutoInappCollectingInfo>() {
                    @NonNull
                    @Override
                    protected ProtobufStateStorage<AutoInappCollectingInfo> createWithHelper(
                            @NonNull Context context,
                            @NonNull IBinaryDataHelper helper
                    ) {
                        return new ProtobufStateStorageImpl<AutoInappCollectingInfo,
                                AutoInappCollectingInfoProto.AutoInappCollectingInfo>(
                                "auto_inapp_collecting_info_data",
                                helper,
                                new StateSerializerFactory(context).createAutoInappCollectingInfoSerializer(),
                                new AutoInappCollectingInfoConverter()
                        );
                    }

                    @NonNull
                    @Override
                    protected IBinaryDataHelper getMainBinaryDataHelper(@NonNull Context context) {
                        return DatabaseStorageFactory.getInstance(context).getAutoInappBinaryDataHelper();
                    }

                    @NonNull
                    @Override
                    protected IBinaryDataHelper getMigrationBinaryDataHelper(@NonNull Context context) {
                        return DatabaseStorageFactory.getInstance(context).getAutoInappBinaryDataHelperForMigration();
                    }
                };
        private final StorageFactory<ClidsInfo> clidsInfoStorageFactory =
                new StorageFactoryImpl<ClidsInfo>() {
                    @NonNull
                    @Override
                    protected ProtobufStateStorage<ClidsInfo> createWithHelper(
                            @NonNull Context context,
                            @NonNull IBinaryDataHelper helper
                    ) {
                        return new ProtobufStateStorageImpl<ClidsInfo, ClidsInfoProto.ClidsInfo>(
                                "clids_info",
                                helper,
                                new StateSerializerFactory(context).createClidsInfoSerializer(),
                                new ClidsInfoConverter()
                        );
                    }

                    @NonNull
                    @Override
                    protected IBinaryDataHelper getMainBinaryDataHelper(@NonNull Context context) {
                        return DatabaseStorageFactory.getInstance(context).getServiceBinaryDataHelper();
                    }

                    @NonNull
                    @Override
                    protected IBinaryDataHelper getMigrationBinaryDataHelper(@NonNull Context context) {
                        return DatabaseStorageFactory.getInstance(context).getServiceBinaryDataHelperForMigration();
                    }
                };

        private Provider() {
            mFactories.put(StartupStateModel.class, mStartupStateStorageFactory);
            mFactories.put(AppPermissionsState.class, mAppPermissionsStateStorageFactory);
            mFactories.put(PreloadInfoData.class, preloadInfoDataStorageFactory);
            mFactories.put(AutoInappCollectingInfo.class, autoInappCollectingStorageFactory);
            mFactories.put(ClidsInfo.class, clidsInfoStorageFactory);
        }

        <T> StorageFactory<T> getInternal(Class<T> clazz) {
            return (StorageFactory<T>) mFactories.get(clazz);
        }

        <T> StorageFactory<Collection<T>> getInternalForCollection(Class<T> clazz) {
            return (StorageFactory<Collection<T>>) mFactories.get(clazz);
        }
    }
}
