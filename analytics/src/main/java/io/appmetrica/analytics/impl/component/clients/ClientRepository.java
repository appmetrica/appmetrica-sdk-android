package io.appmetrica.analytics.impl.component.clients;

import android.content.Context;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.VisibleForTesting;
import io.appmetrica.analytics.impl.Utils;
import io.appmetrica.analytics.impl.component.CommonArguments;
import io.appmetrica.analytics.impl.utils.collection.HashMultimap;
import io.appmetrica.analytics.logger.appmetrica.internal.DebugLogger;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;

public class ClientRepository {

    private static final String TAG = "[ClientsRepository]";

    private final Object mMonitor = new Object();

    private final ComponentsRepository mComponentsRepository;
    private final HashMap<ClientDescription, ClientUnit> mConnectedClients =
            new HashMap<ClientDescription, ClientUnit>();
    private final HashMultimap<Tag, ClientDescription> mTaggedClients = new HashMultimap<Tag, ClientDescription>();

    @NonNull private final Context mContext;
    private volatile int mClientsCount = 0;
    @NonNull
    private final ClientUnitFactoryHolder mClientUnitFactoryHolder;

    public ClientRepository(@NonNull Context context, @NonNull ComponentsRepository componentsRepository) {
        this(context, componentsRepository, new ClientUnitFactoryHolder());
    }

    @VisibleForTesting
    ClientRepository(@NonNull Context context,
                     @NonNull ComponentsRepository componentsRepository,
                     @NonNull ClientUnitFactoryHolder clientUnitFactoryHolder) {
        mContext = context.getApplicationContext();
        mComponentsRepository = componentsRepository;
        mClientUnitFactoryHolder = clientUnitFactoryHolder;
    }

    public ClientUnit getOrCreateClient(@NonNull ClientDescription clientDescription,
                                        @NonNull CommonArguments sdkConfig) {
        DebugLogger.INSTANCE.info(TAG, "try to find client for %s", clientDescription);
        synchronized (mMonitor) {
            ClientUnit unit = mConnectedClients.get(clientDescription);
            if (unit == null) {
                DebugLogger.INSTANCE.info(TAG, "no client found for %s. Create new.", clientDescription);
                unit = mClientUnitFactoryHolder.getClientUnitFactory(clientDescription)
                        .createClientUnit(mContext, mComponentsRepository, clientDescription, sdkConfig);
                mConnectedClients.put(clientDescription, unit);
                mTaggedClients.put(new Tag(clientDescription), clientDescription);
                mClientsCount++;
            }
            return unit;
        }
    }

    public void remove(@NonNull String packageName, int pid, String psid) {
        removeInternal(packageName, pid, psid);
    }

    private void removeInternal(@NonNull String packageName, @Nullable Integer pid, @Nullable String psid) {
        DebugLogger.INSTANCE.info(
            TAG,
            "remove clients for (packageName, pid, psid) = (%s, %d, %s)",
            packageName,
            pid,
            psid
        );
        synchronized (mMonitor) {
            Collection<ClientDescription> clientDescriptions =
                    mTaggedClients.removeAll(new Tag(packageName, pid, psid));
            if (Utils.isNullOrEmpty(clientDescriptions)) {
                DebugLogger.INSTANCE.info(
                    TAG,
                    "remove clients for (packageName, pid, psid) = (%s, %d, %s). No clients found.",
                    packageName,
                    pid,
                    psid
                );
            } else  {
                DebugLogger.INSTANCE.info(
                    TAG,
                    "remove clients for (packageName, pid, psid) = (%s, %d, %s). %d clients removed.",
                    packageName,
                    pid,
                    psid,
                    clientDescriptions.size()
                );
                mClientsCount -= clientDescriptions.size();

                ArrayList<ClientUnit> clientUnits = new ArrayList<ClientUnit>(clientDescriptions.size());
                for (ClientDescription clientDescription : clientDescriptions) {
                    clientUnits.add(mConnectedClients.remove(clientDescription));
                }
                for (ClientUnit unit : clientUnits) {
                    unit.onDisconnect();
                }
            }
        }
    }

    public int getClientsCount() {
        return mClientsCount;
    }

    private static final class Tag {

        @NonNull private final String mPackageName;
        @Nullable private final Integer mPid;
        @Nullable private final String mPsid;

        Tag(@NonNull String packageName, @Nullable Integer pid, @Nullable String psid) {
            mPackageName = packageName;
            mPid = pid;
            mPsid = psid;
        }

        Tag (@NonNull ClientDescription clientDescription) {
            this(clientDescription.getPackageName(),
                 clientDescription.getProcessID(),
                 clientDescription.getProcessSessionID());
        }

        @Override
        public int hashCode() {
            int result = mPackageName.hashCode();
            result = 31 * result + (mPid != null ? mPid.hashCode() : 0);
            result = 31 * result + (mPsid != null ? mPsid.hashCode() : 0);
            return result;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;

            Tag tag = (Tag) o;

            if (!mPackageName.equals(tag.mPackageName)) return false;
            if (mPid != null ? !mPid.equals(tag.mPid) : tag.mPid != null) return false;
            return mPsid != null ? mPsid.equals(tag.mPsid) : tag.mPsid == null;
        }
    }

}
