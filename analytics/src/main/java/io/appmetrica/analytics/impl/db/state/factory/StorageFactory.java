package io.appmetrica.analytics.impl.db.state.factory;

import android.content.Context;
import androidx.annotation.NonNull;
import io.appmetrica.analytics.coreapi.internal.data.IBinaryDataHelper;
import io.appmetrica.analytics.coreapi.internal.data.ProtobufBinaryStateStorageFactory;
import io.appmetrica.analytics.coreapi.internal.data.ProtobufConverter;
import io.appmetrica.analytics.coreapi.internal.data.ProtobufStateSerializer;
import io.appmetrica.analytics.coreapi.internal.data.ProtobufStateStorage;
import io.appmetrica.analytics.impl.GlobalServiceLocator;
import io.appmetrica.analytics.impl.clids.ClidsInfo;
import io.appmetrica.analytics.impl.db.protobuf.ProtobufStateStorageImpl;
import io.appmetrica.analytics.impl.db.state.converter.AppPermissionsStateConverter;
import io.appmetrica.analytics.impl.db.state.converter.ClidsInfoConverter;
import io.appmetrica.analytics.impl.db.state.converter.StartupStateConverter;
import io.appmetrica.analytics.impl.permissions.AppPermissionsState;
import io.appmetrica.analytics.impl.preloadinfo.PreloadInfoData;
import io.appmetrica.analytics.impl.preloadinfo.PreloadInfoDataConverter;
import io.appmetrica.analytics.impl.protobuf.client.AppPermissionsStateProtobuf;
import io.appmetrica.analytics.impl.protobuf.client.ClidsInfoProto;
import io.appmetrica.analytics.impl.protobuf.client.PreloadInfoProto;
import io.appmetrica.analytics.impl.protobuf.client.StartupStateProtobuf;
import io.appmetrica.analytics.impl.startup.StartupStateModel;
import io.appmetrica.analytics.logger.appmetrica.internal.DebugLogger;
import io.appmetrica.analytics.protobuf.nano.MessageNano;
import java.util.Collection;
import java.util.HashMap;

public interface StorageFactory<T> extends ProtobufBinaryStateStorageFactory<T> {

    @NonNull
    ProtobufStateStorage<T> create(@NonNull Context context);

    @NonNull
    ProtobufStateStorage<T> createForMigration(@NonNull Context context);

    class Provider {

        private static final String TAG = "[StorageFactory.Provider]";

        private static final class InstanceHolder {
            static final Provider INSTANCE = new Provider();
        }

        public static <T> StorageFactory<T> get(Class<T> clazz) {
            return InstanceHolder.INSTANCE.getInternal(clazz);
        }

        public static <T> StorageFactory<Collection<T>> getFactoryForCollection(Class<T> clazz) {
            return InstanceHolder.INSTANCE.getInternalForCollection(clazz);
        }

        @NonNull
        public static <T, P extends MessageNano> StorageFactory<T> createCustomFactory(
            String key,
            ProtobufStateSerializer<P> serializer,
            ProtobufConverter<T, P> converter
        ) {
            DebugLogger.INSTANCE.info(TAG, "createCustomFactory for key: %s", key);
            return InstanceHolder.INSTANCE.createCustomStorageFactory(key, serializer, converter);
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
                    return GlobalServiceLocator.getInstance().getStorageFactory()
                        .getServiceBinaryDataHelper(context);
                }

                @NonNull
                @Override
                protected IBinaryDataHelper getMigrationBinaryDataHelper(@NonNull Context context) {
                    return GlobalServiceLocator.getInstance().getStorageFactory()
                        .getServiceBinaryDataHelperForMigration(context);
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
                    return GlobalServiceLocator.getInstance().getStorageFactory().getServiceBinaryDataHelper(context);
                }

                @NonNull
                @Override
                protected IBinaryDataHelper getMigrationBinaryDataHelper(@NonNull Context context) {
                    return GlobalServiceLocator.getInstance().getStorageFactory()
                        .getServiceBinaryDataHelperForMigration(context);
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
                    return GlobalServiceLocator.getInstance().getStorageFactory().getServiceBinaryDataHelper(context);
                }

                @NonNull
                @Override
                protected IBinaryDataHelper getMigrationBinaryDataHelper(@NonNull Context context) {
                    return GlobalServiceLocator.getInstance().getStorageFactory()
                        .getServiceBinaryDataHelperForMigration(context);
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
                    return GlobalServiceLocator.getInstance().getStorageFactory().getServiceBinaryDataHelper(context);
                }

                @NonNull
                @Override
                protected IBinaryDataHelper getMigrationBinaryDataHelper(@NonNull Context context) {
                    return GlobalServiceLocator.getInstance().getStorageFactory()
                        .getServiceBinaryDataHelperForMigration(context);
                }
            };

        private Provider() {
            mFactories.put(StartupStateModel.class, mStartupStateStorageFactory);
            mFactories.put(AppPermissionsState.class, mAppPermissionsStateStorageFactory);
            mFactories.put(PreloadInfoData.class, preloadInfoDataStorageFactory);
            mFactories.put(ClidsInfo.class, clidsInfoStorageFactory);
        }

        @SuppressWarnings("unchecked")
        <T> StorageFactory<T> getInternal(Class<T> clazz) {
            return (StorageFactory<T>) mFactories.get(clazz);
        }

        @SuppressWarnings("unchecked")
        <T> StorageFactory<Collection<T>> getInternalForCollection(Class<T> clazz) {
            return (StorageFactory<Collection<T>>) mFactories.get(clazz);
        }

        <T, P extends MessageNano> StorageFactory<T> createCustomStorageFactory(
            String key,
            ProtobufStateSerializer<P> serializer,
            ProtobufConverter<T, P> converter
        ) {
            return new StorageFactoryImpl<T>() {
                @NonNull
                @Override
                protected ProtobufStateStorage<T> createWithHelper(
                    @NonNull Context context,
                    @NonNull IBinaryDataHelper helper
                ) {
                    return new ProtobufStateStorageImpl<T, P>(
                        key,
                        helper,
                        new StateSerializerFactory(context).createCustomSerializer(serializer),
                        converter
                    );
                }

                @NonNull
                @Override
                protected IBinaryDataHelper getMainBinaryDataHelper(@NonNull Context context) {
                    return GlobalServiceLocator.getInstance().getStorageFactory().getServiceBinaryDataHelper(context);
                }

                @NonNull
                @Override
                protected IBinaryDataHelper getMigrationBinaryDataHelper(@NonNull Context context) {
                    return GlobalServiceLocator.getInstance().getStorageFactory()
                        .getServiceBinaryDataHelperForMigration(context);
                }
            };
        }
    }
}
