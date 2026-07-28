package io.appmetrica.analytics.impl.request;

import android.content.Context;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.VisibleForTesting;
import io.appmetrica.analytics.coreutils.internal.WrapUtils;
import io.appmetrica.analytics.coreutils.internal.services.SafePackageManager;
import io.appmetrica.analytics.impl.ClidsInfoStorage;
import io.appmetrica.analytics.impl.DistributionSource;
import io.appmetrica.analytics.impl.GlobalServiceLocator;
import io.appmetrica.analytics.impl.Utils;
import io.appmetrica.analytics.impl.clids.ClidsInfo;
import io.appmetrica.analytics.impl.client.ClientConfiguration;
import io.appmetrica.analytics.impl.referrer.service.ReferrerManager;
import io.appmetrica.analytics.impl.startup.StartupState;
import io.appmetrica.analytics.logger.appmetrica.internal.DebugLogger;
import io.appmetrica.analytics.networktasks.internal.ArgumentsMerger;
import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class StartupRequestConfig extends CoreRequestConfig {

    private static final String TAG = "[StartupRequestConfig]";

    static final long DEFAULT_FIRST_STARTUP_TIME = 0;

    @Nullable
    private List<String> mStartupHostsFromStartup;
    @Nullable
    private List<String> mStartupHostsFromClient;
    @Nullable
    private List<String> mStartupHostsFromLibraryAdapter;
    @Nullable
    private String mDistributionReferrer;
    @Nullable
    private String mInstallReferrerSource;
    @Nullable
    private Map<String, String> mClidsFromClient;
    @NonNull
    private ClidsInfo.Candidate chosenClids = new ClidsInfo.Candidate(null, DistributionSource.APP);
    @Nullable
    private List<String> mNewCustomHosts;
    private boolean mHasNewCustomHosts;
    @Nullable
    private List<String> mNewLibraryAdapterCustomHosts;
    private boolean mHasNewLibraryAdapterCustomHosts;
    private boolean mSuccessfulStartup;
    private String mCountryInit;
    private long mFirstStartupTime = DEFAULT_FIRST_STARTUP_TIME;
    @NonNull
    private final ReferrerManager referrerManager;
    @NonNull
    private final HostsProvider resourceStartupHostsProvider;
    @NonNull
    private final HostsProvider defaultStartupHostsProvider;

    private StartupRequestConfig() {
        this(
            GlobalServiceLocator.getInstance().getReferrerManager(),
            new ResourceStartupHostsProvider(),
            new DefaultStartupHostsProvider()
        );
    }

    @VisibleForTesting
    public StartupRequestConfig(@NonNull ReferrerManager referrerManager,
                                @NonNull HostsProvider resourceStartupHostsProvider,
                                @NonNull HostsProvider defaultStartupHostsProvider) {
        this.referrerManager = referrerManager;
        this.resourceStartupHostsProvider = resourceStartupHostsProvider;
        this.defaultStartupHostsProvider = defaultStartupHostsProvider;
    }

    //todo (avitenko) used only during request
    public List<String> getStartupHosts() {
        Set<String> hostsUrlList = new LinkedHashSet<>();

        // 1. Hosts from server response — always first
        addAllNullable(hostsUrlList, mStartupHostsFromStartup);

        // 2. App custom hosts (from activation/saved config)
        boolean hasClientHosts = !Utils.isNullOrEmpty(mStartupHostsFromClient);
        addAllNullable(hostsUrlList, mStartupHostsFromClient);

        // 3. Resource-based hosts count as "app hosts" too
        Collection<String> resourceHosts = resourceStartupHostsProvider.getHosts();
        boolean hasResourceHosts = !Utils.isNullOrEmpty(resourceHosts);
        addAllNullable(hostsUrlList, resourceHosts);

        // If app provided any hosts (config or resource) -> return
        if (hasClientHosts || hasResourceHosts) {
            DebugLogger.INSTANCE.info(
                TAG,
                "getStartupHosts: client/resource hosts win (hasClientHosts=%b, hasResourceHosts=%b) -> %s",
                hasClientHosts, hasResourceHosts, hostsUrlList
            );
            return new ArrayList<>(hostsUrlList);
        }

        // 4. LibraryAdapter hosts — used only if app has no custom hosts
        boolean hasLibraryAdapterHosts = !Utils.isNullOrEmpty(mStartupHostsFromLibraryAdapter);
        addAllNullable(hostsUrlList, mStartupHostsFromLibraryAdapter);

        // If library adapter provided any hosts -> return
        if (hasLibraryAdapterHosts) {
            DebugLogger.INSTANCE.info(TAG, "getStartupHosts: library adapter hosts win -> %s", hostsUrlList);
            return new ArrayList<>(hostsUrlList);
        }

        // 5. Fallback: BuildConfig defaults
        addAllNullable(hostsUrlList, defaultStartupHostsProvider.getHosts());
        DebugLogger.INSTANCE.info(TAG, "getStartupHosts: fallback to default hosts -> %s", hostsUrlList);
        return new ArrayList<>(hostsUrlList);
    }

    private void addAllNullable(Set<String> hosts, @Nullable Collection<String> other) {
        if (!Utils.isNullOrEmpty(other)) {
            hosts.addAll(other);
        }
    }

    public boolean hasSuccessfulStartup() {
        return mSuccessfulStartup;
    }

    void setSuccessfulStartup(boolean value) {
        mSuccessfulStartup = value;
    }

    void setFirstStartupTimeIfNeeded(final long firstStartupTime) {
        if (mFirstStartupTime == DEFAULT_FIRST_STARTUP_TIME) {
            mFirstStartupTime = firstStartupTime;
        }
    }

    public long getFirstStartupTime() {
        return mFirstStartupTime;
    }

    public long getOrSetFirstStartupTime(final long curServerTimeSec) {
        setFirstStartupTimeIfNeeded(curServerTimeSec);
        return getFirstStartupTime();
    }

    public List<String> getStartupHostsFromClient() {
        return mStartupHostsFromClient;
    }

    public List<String> getStartupHostsFromLibraryAdapter() {
        return mStartupHostsFromLibraryAdapter;
    }

    public List<String> getStartupHostsFromStartup() {
        return mStartupHostsFromStartup;
    }

    void setStartupHostsFromClient(@Nullable List<String> startupHostsFromClient) {
        mStartupHostsFromClient = startupHostsFromClient;
    }

    void setStartupHostsFromLibraryAdapter(@Nullable List<String> startupHostsFromLibraryAdapter) {
        mStartupHostsFromLibraryAdapter = startupHostsFromLibraryAdapter;
    }

    @Nullable
    public Map<String, String> getClidsFromClient() {
        return mClidsFromClient;
    }

    void setClidsFromClient(@Nullable Map<String, String> clidsFromClient) {
        mClidsFromClient = clidsFromClient;
    }

    void setChosenClids(@NonNull ClidsInfo.Candidate chosenClids) {
        this.chosenClids = chosenClids;
    }

    @NonNull
    public ClidsInfo.Candidate getChosenClids() {
        return chosenClids;
    }

    void setStartupHostsFromStartup(@Nullable final List<String> hosts) {
        mStartupHostsFromStartup = hosts;
    }

    @Nullable
    public String getDistributionReferrer() {
        return mDistributionReferrer;
    }

    private void setDistributionReferrer(@Nullable String distributionReferrer) {
        mDistributionReferrer = distributionReferrer;
    }

    @Nullable
    public String getInstallReferrerSource() {
        return mInstallReferrerSource;
    }

    private void setInstallReferrerSource(@Nullable String installReferrerSource) {
        mInstallReferrerSource = installReferrerSource;
    }

    @Nullable
    public List<String> getNewCustomHosts() {
        return mNewCustomHosts;
    }

    public void setNewCustomHosts(@Nullable List<String> newCustomHosts) {
        mNewCustomHosts = newCustomHosts;
    }

    @Nullable
    public boolean hasNewCustomHosts() {
        return mHasNewCustomHosts;
    }

    public void setHasNewCustomHosts(boolean hasNewCustomHosts) {
        mHasNewCustomHosts = hasNewCustomHosts;
    }

    @Nullable
    public List<String> getNewLibraryAdapterCustomHosts() {
        return mNewLibraryAdapterCustomHosts;
    }

    public void setNewLibraryAdapterCustomHosts(@Nullable List<String> newLibraryAdapterCustomHosts) {
        mNewLibraryAdapterCustomHosts = newLibraryAdapterCustomHosts;
    }

    public boolean hasNewLibraryAdapterCustomHosts() {
        return mHasNewLibraryAdapterCustomHosts;
    }

    public void setHasNewLibraryAdapterCustomHosts(boolean hasNewLibraryAdapterCustomHosts) {
        mHasNewLibraryAdapterCustomHosts = hasNewLibraryAdapterCustomHosts;
    }

    public String getCountryInit() {
        return mCountryInit;
    }

    public void setCountryInit(String countryInit) {
        mCountryInit = countryInit;
    }

    @NonNull
    public ReferrerManager getReferrerManager() {
        return referrerManager;
    }

    @Override
    public String toString() {
        return "StartupRequestConfig{" +
            "mStartupHostsFromStartup=" + mStartupHostsFromStartup +
            ", mStartupHostsFromClient=" + mStartupHostsFromClient +
            ", mStartupHostsFromLibraryAdapter=" + mStartupHostsFromLibraryAdapter +
            ", mDistributionReferrer='" + mDistributionReferrer + '\'' +
            ", mInstallReferrerSource='" + mInstallReferrerSource + '\'' +
            ", mClidsFromClient=" + mClidsFromClient +
            ", mNewCustomHosts=" + mNewCustomHosts +
            ", mHasNewCustomHosts=" + mHasNewCustomHosts +
            ", mNewLibraryAdapterCustomHosts=" + mNewLibraryAdapterCustomHosts +
            ", mHasNewLibraryAdapterCustomHosts=" + mHasNewLibraryAdapterCustomHosts +
            ", mSuccessfulStartup=" + mSuccessfulStartup +
            ", mCountryInit='" + mCountryInit + '\'' +
            ", mFirstStartupTime=" + mFirstStartupTime +
            "} " + super.toString();
    }

    public static class Arguments extends BaseRequestArguments<Arguments, Arguments>
        implements ArgumentsMerger<Arguments, Arguments> {

        public Arguments(@NonNull ClientConfiguration configuration) {
            this(
                configuration.getProcessConfiguration().getDistributionReferrer(),
                configuration.getProcessConfiguration().getInstallReferrerSource(),
                configuration.getProcessConfiguration().getClientClids(),
                configuration.getProcessConfiguration().hasCustomHosts(),
                configuration.getProcessConfiguration().getCustomHosts(),
                configuration.getProcessConfiguration().hasLibraryAdapterCustomHosts(),
                configuration.getProcessConfiguration().getLibraryAdapterCustomHosts()
            );
        }

        @Nullable
        public final String distributionReferrer;
        @Nullable
        public final String installReferrerSource;
        @Nullable
        public final Map<String, String> clientClids;
        public final boolean hasNewCustomHosts;
        @Nullable
        public final List<String> newCustomHosts;
        public final boolean hasNewLibraryAdapterCustomHosts;
        @Nullable
        public final List<String> newLibraryAdapterCustomHosts;

        public Arguments(
            @Nullable String distributionReferrer,
            @Nullable String installReferrerSource,
            @Nullable Map<String, String> clientClids,
            boolean hasNewCustomHosts,
            @Nullable List<String> newCustomHosts,
            boolean hasNewLibraryAdapterCustomHosts,
            @Nullable List<String> newLibraryAdapterCustomHosts
        ) {
            super();
            this.distributionReferrer = distributionReferrer;
            this.installReferrerSource = installReferrerSource;
            this.clientClids = clientClids;
            this.hasNewCustomHosts = hasNewCustomHosts;
            this.newCustomHosts = newCustomHosts;
            this.hasNewLibraryAdapterCustomHosts = hasNewLibraryAdapterCustomHosts;
            this.newLibraryAdapterCustomHosts = newLibraryAdapterCustomHosts;
        }

        public Arguments() {
            this(null, null, null, false, null, false, null);
        }

        boolean chooseHasNewCustomHosts(@NonNull Arguments other) {
            return hasNewCustomHosts || other.hasNewCustomHosts;
        }

        List<String> chooseNewCustomHosts(@NonNull Arguments other) {
            return other.hasNewCustomHosts ? other.newCustomHosts : newCustomHosts;
        }

        boolean chooseHasNewLibraryAdapterCustomHosts(@NonNull Arguments other) {
            return hasNewLibraryAdapterCustomHosts || other.hasNewLibraryAdapterCustomHosts;
        }

        List<String> chooseNewLibraryAdapterCustomHosts(@NonNull Arguments other) {
            return other.hasNewLibraryAdapterCustomHosts ?
                other.newLibraryAdapterCustomHosts : newLibraryAdapterCustomHosts;
        }

        @NonNull
        @Override
        public Arguments mergeFrom(@NonNull Arguments other) {
            return new Arguments(
                WrapUtils.getOrDefaultNullable(distributionReferrer, other.distributionReferrer),
                WrapUtils.getOrDefaultNullable(installReferrerSource, other.installReferrerSource),
                WrapUtils.getOrDefaultNullable(clientClids, other.clientClids),
                chooseHasNewCustomHosts(other),
                chooseNewCustomHosts(other),
                chooseHasNewLibraryAdapterCustomHosts(other),
                chooseNewLibraryAdapterCustomHosts(other)
            );
        }

        @Override
        public boolean compareWithOtherArguments(@NonNull Arguments other) {
            return false;
        }

        @Override
        public String toString() {
            return "Arguments{" +
                "distributionReferrer='" + distributionReferrer + '\'' +
                ", installReferrerSource='" + installReferrerSource + '\'' +
                ", clientClids=" + clientClids +
                ", hasNewCustomHosts=" + hasNewCustomHosts +
                ", newCustomHosts=" + newCustomHosts +
                ", hasNewLibraryAdapterCustomHosts=" + hasNewLibraryAdapterCustomHosts +
                ", newLibraryAdapterCustomHosts=" + newLibraryAdapterCustomHosts +
                '}';
        }
    }

    public static class Loader extends CoreLoader<StartupRequestConfig, Arguments> {

        private static final String TAG = "[StartupRequestConfig.Loader]";

        @NonNull
        private final ClidsInfoStorage clidsStorage;

        public Loader(@NonNull Context context, @NonNull String packageName) {
            this(context, packageName, new SafePackageManager(), GlobalServiceLocator.getInstance().getClidsStorage());
        }

        protected Loader(@NonNull Context context,
                         @NonNull String packageName,
                         @NonNull SafePackageManager safePackageManager,
                         @NonNull ClidsInfoStorage clidsStorage) {
            super(context, packageName, safePackageManager);
            this.clidsStorage = clidsStorage;
        }

        @NonNull
        protected StartupRequestConfig createBlankConfig() {
            return new StartupRequestConfig();
        }

        @Override
        @NonNull
        public StartupRequestConfig load(@NonNull CoreDataSource<Arguments> dataSource) {
            StartupRequestConfig config = super.load(dataSource);
            loadHosts(config, dataSource.startupState);
            if (dataSource.componentArguments.distributionReferrer != null) {
                config.setDistributionReferrer(dataSource.componentArguments.distributionReferrer);
                config.setDistributionReferrer(dataSource.componentArguments.distributionReferrer);
                config.setInstallReferrerSource(dataSource.componentArguments.installReferrerSource);
            }
            Map<String, String> clientClids = dataSource.componentArguments.clientClids;
            config.setClidsFromClient(clientClids);
            ClidsInfo.Candidate clidsCandidate = new ClidsInfo.Candidate(clientClids, DistributionSource.APP);
            config.setChosenClids(clidsStorage.updateAndRetrieveData(clidsCandidate));
            config.setHasNewCustomHosts(dataSource.componentArguments.hasNewCustomHosts);
            config.setNewCustomHosts(dataSource.componentArguments.newCustomHosts);
            config.setHasNewLibraryAdapterCustomHosts(dataSource.componentArguments.hasNewLibraryAdapterCustomHosts);
            config.setNewLibraryAdapterCustomHosts(dataSource.componentArguments.newLibraryAdapterCustomHosts);
            config.setSuccessfulStartup(dataSource.startupState.getHadFirstStartup());
            config.setCountryInit(dataSource.startupState.getCountryInit());
            config.setFirstStartupTimeIfNeeded(dataSource.startupState.getFirstStartupServerTime());
            return config;
        }

        void loadHosts(@NonNull StartupRequestConfig config,
                       @NonNull StartupState startupState) {
            DebugLogger.INSTANCE.info(TAG, "setStartupHostsFromClient: %s", startupState.getHostUrlsFromClient());
            config.setStartupHostsFromStartup(startupState.getHostUrlsFromStartup());
            config.setStartupHostsFromClient(startupState.getHostUrlsFromClient());
            config.setStartupHostsFromLibraryAdapter(startupState.getHostUrlsFromLibraryAdapter());
        }
    }
}
