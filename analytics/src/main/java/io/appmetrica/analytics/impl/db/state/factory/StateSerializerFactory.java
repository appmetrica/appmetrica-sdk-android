package io.appmetrica.analytics.impl.db.state.factory;

import android.content.Context;
import androidx.annotation.NonNull;
import androidx.annotation.VisibleForTesting;
import io.appmetrica.analytics.coreapi.internal.data.ProtobufStateSerializer;
import io.appmetrica.analytics.coreutils.internal.encryption.AESEncrypter;
import io.appmetrica.analytics.impl.billing.AutoInappCollectingInfoSerializer;
import io.appmetrica.analytics.impl.db.protobuf.AppPermissionsStateSerializer;
import io.appmetrica.analytics.impl.db.protobuf.ClidsInfoStateSerializer;
import io.appmetrica.analytics.impl.db.protobuf.EncryptedProtobufStateSerializer;
import io.appmetrica.analytics.impl.db.protobuf.StartupStateSerializer;
import io.appmetrica.analytics.impl.preloadinfo.PreloadInfoDataSerializer;
import io.appmetrica.analytics.impl.protobuf.client.AppPermissionsStateProtobuf;
import io.appmetrica.analytics.impl.protobuf.client.AutoInappCollectingInfoProto;
import io.appmetrica.analytics.impl.protobuf.client.ClidsInfoProto;
import io.appmetrica.analytics.impl.protobuf.client.PreloadInfoProto;
import io.appmetrica.analytics.impl.protobuf.client.StartupStateProtobuf.StartupState;
import io.appmetrica.analytics.impl.utils.encryption.AESCredentialProvider;

public class StateSerializerFactory {

    private AESCredentialProvider mCredentialProvider;

    public StateSerializerFactory(@NonNull final Context context) {
        this(new AESCredentialProvider(context));
    }

    @VisibleForTesting
    StateSerializerFactory(final AESCredentialProvider credentialProvider) {
        mCredentialProvider = credentialProvider;
    }

    public ProtobufStateSerializer<StartupState> createStartupStateSerializer() {
        return new EncryptedProtobufStateSerializer<StartupState>(
                new StartupStateSerializer(),
                new AESEncrypter(
                        AESEncrypter.DEFAULT_ALGORITHM,
                        mCredentialProvider.getPassword(),
                        mCredentialProvider.getIV()
                )
        );
    }

    @NonNull
    public ProtobufStateSerializer<AppPermissionsStateProtobuf.AppPermissionsState>
        createAppPermissionsStateSerializer() {

        return new EncryptedProtobufStateSerializer<AppPermissionsStateProtobuf.AppPermissionsState>(
                new AppPermissionsStateSerializer(),
                new AESEncrypter(
                        AESEncrypter.DEFAULT_ALGORITHM,
                        mCredentialProvider.getPassword(),
                        mCredentialProvider.getIV()
                )
        );
    }

    @NonNull
    public ProtobufStateSerializer<PreloadInfoProto.PreloadInfoData> createPreloadInfoDataSerializer() {

        return new EncryptedProtobufStateSerializer<PreloadInfoProto.PreloadInfoData>(
                new PreloadInfoDataSerializer(),
                new AESEncrypter(
                        AESEncrypter.DEFAULT_ALGORITHM,
                        mCredentialProvider.getPassword(),
                        mCredentialProvider.getIV()
                )
        );
    }

    @SuppressWarnings("LineLength")
    @NonNull
    public ProtobufStateSerializer<AutoInappCollectingInfoProto.AutoInappCollectingInfo> createAutoInappCollectingInfoSerializer() {

        return new EncryptedProtobufStateSerializer<AutoInappCollectingInfoProto.AutoInappCollectingInfo>(
                new AutoInappCollectingInfoSerializer(),
                new AESEncrypter(
                        AESEncrypter.DEFAULT_ALGORITHM,
                        mCredentialProvider.getPassword(),
                        mCredentialProvider.getIV()
                )
        );
    }

    @NonNull
    public ProtobufStateSerializer<ClidsInfoProto.ClidsInfo> createClidsInfoSerializer() {
        return new EncryptedProtobufStateSerializer<ClidsInfoProto.ClidsInfo>(
                new ClidsInfoStateSerializer(),
                new AESEncrypter(
                        AESEncrypter.DEFAULT_ALGORITHM,
                        mCredentialProvider.getPassword(),
                        mCredentialProvider.getIV()
                )
        );
    }
}
