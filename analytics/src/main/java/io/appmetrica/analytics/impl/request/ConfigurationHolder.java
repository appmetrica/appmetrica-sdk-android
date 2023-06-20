package io.appmetrica.analytics.impl.request;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.VisibleForTesting;
import io.appmetrica.analytics.impl.GlobalServiceLocator;
import io.appmetrica.analytics.impl.LocaleHolder;
import io.appmetrica.analytics.impl.startup.StartupState;
import io.appmetrica.analytics.networktasks.internal.ArgumentsMerger;
import io.appmetrica.analytics.networktasks.internal.BaseRequestConfig;

public abstract class ConfigurationHolder
        <T extends BaseRequestConfig, IA, A extends ArgumentsMerger<IA, A>,
        L extends BaseRequestConfig.RequestConfigLoader<T, CoreRequestConfig.CoreDataSource<A>>>
        implements LocaleHolder.Listener {

    @Nullable
    private T mRequestConfig;
    @NonNull
    private L mRequestConfigLoader;
    @NonNull
    private CoreRequestConfig.CoreDataSource<A> mDataSource;

    public ConfigurationHolder(@NonNull L loader,
                               @NonNull StartupState startupState,
                               @NonNull A initialArguments) {
        mRequestConfigLoader = loader;
        LocaleHolder.getInstance(GlobalServiceLocator.getInstance().getContext()).registerLocaleUpdatedListener(this);
        setDataSource(new CoreRequestConfig.CoreDataSource<A>(
                startupState, initialArguments)
        );
    }

    @Override
    public void onLocalesUpdated() {
        reset();
    }

    public synchronized void reset() {
        mRequestConfig = null;
    }

    protected synchronized void setDataSource(@NonNull CoreRequestConfig.CoreDataSource<A> dataSource) {
        mDataSource = dataSource;
    }

    public synchronized void updateArguments(@NonNull IA newArguments) {
        if (mDataSource.componentArguments.compareWithOtherArguments(newArguments) == false) {
            setDataSource(
                        new CoreRequestConfig.CoreDataSource<A>(getStartupState(),
                        mDataSource.componentArguments.mergeFrom(newArguments)
                    )
            );
            reset();
        }
    }

    public synchronized void updateStartupState(@NonNull StartupState startupState) {
        //it's not so frequent operation. Just load new config, without any comparing with previous state.
        setDataSource(new CoreRequestConfig.CoreDataSource<A>(startupState, getArguments()));
        reset();
    }

    @NonNull
    public synchronized StartupState getStartupState() {
        return mDataSource.startupState;
    }

    @VisibleForTesting(otherwise = VisibleForTesting.PROTECTED)
    @NonNull
    public synchronized A getArguments() {
        return mDataSource.componentArguments;
    }

    @NonNull
    public synchronized T get() {
        if (mRequestConfig == null) {
            mRequestConfig = mRequestConfigLoader.load(mDataSource);
        }
        return mRequestConfig;
    }

}
