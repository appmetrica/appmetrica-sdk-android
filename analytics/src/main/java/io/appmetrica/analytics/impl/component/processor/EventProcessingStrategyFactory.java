package io.appmetrica.analytics.impl.component.processor;

import androidx.annotation.Nullable;
import androidx.annotation.VisibleForTesting;
import io.appmetrica.analytics.impl.InternalEvents;
import io.appmetrica.analytics.impl.component.ComponentUnit;
import io.appmetrica.analytics.impl.component.processor.event.ReportComponentHandler;
import io.appmetrica.analytics.impl.component.processor.factory.ActivationFactory;
import io.appmetrica.analytics.impl.component.processor.factory.CommonConditionalFactory;
import io.appmetrica.analytics.impl.component.processor.factory.CommonHandlersFactory;
import io.appmetrica.analytics.impl.component.processor.factory.CurrentSessionNativeCrashHandlerFactory;
import io.appmetrica.analytics.impl.component.processor.factory.HandlersFactory;
import io.appmetrica.analytics.impl.component.processor.factory.JustSaveToDataBaseFactory;
import io.appmetrica.analytics.impl.component.processor.factory.PrevSessionNativeCrashHandlerFactory;
import io.appmetrica.analytics.impl.component.processor.factory.PurgeBufferFactory;
import io.appmetrica.analytics.impl.component.processor.factory.RegularFactory;
import io.appmetrica.analytics.impl.component.processor.factory.ReportAppOpenFactory;
import io.appmetrica.analytics.impl.component.processor.factory.ReportingHandlerProvider;
import io.appmetrica.analytics.impl.component.processor.factory.SingleHandlerFactory;
import io.appmetrica.analytics.impl.component.processor.factory.StartFactory;
import io.appmetrica.analytics.impl.component.processor.factory.UnhandledExceptionFactory;
import io.appmetrica.analytics.impl.component.processor.factory.UnhandledExceptionFromFileFactory;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import static io.appmetrica.analytics.impl.InternalEvents.EVENT_TYPE_ACTIVATION;
import static io.appmetrica.analytics.impl.InternalEvents.EVENT_TYPE_ANR;
import static io.appmetrica.analytics.impl.InternalEvents.EVENT_TYPE_APP_ENVIRONMENT_CLEARED;
import static io.appmetrica.analytics.impl.InternalEvents.EVENT_TYPE_APP_ENVIRONMENT_UPDATED;
import static io.appmetrica.analytics.impl.InternalEvents.EVENT_TYPE_APP_OPEN;
import static io.appmetrica.analytics.impl.InternalEvents.EVENT_TYPE_CLEANUP;
import static io.appmetrica.analytics.impl.InternalEvents.EVENT_TYPE_CURRENT_SESSION_NATIVE_CRASH_PROTOBUF;
import static io.appmetrica.analytics.impl.InternalEvents.EVENT_TYPE_CUSTOM_EVENT;
import static io.appmetrica.analytics.impl.InternalEvents.EVENT_TYPE_EXCEPTION_UNHANDLED_FROM_FILE;
import static io.appmetrica.analytics.impl.InternalEvents.EVENT_TYPE_EXCEPTION_UNHANDLED_FROM_INTENT;
import static io.appmetrica.analytics.impl.InternalEvents.EVENT_TYPE_EXCEPTION_UNHANDLED_PROTOBUF;
import static io.appmetrica.analytics.impl.InternalEvents.EVENT_TYPE_EXCEPTION_USER_CUSTOM_PROTOBUF;
import static io.appmetrica.analytics.impl.InternalEvents.EVENT_TYPE_EXCEPTION_USER_PROTOBUF;
import static io.appmetrica.analytics.impl.InternalEvents.EVENT_TYPE_PREV_SESSION_NATIVE_CRASH_PROTOBUF;
import static io.appmetrica.analytics.impl.InternalEvents.EVENT_TYPE_PURGE_BUFFER;
import static io.appmetrica.analytics.impl.InternalEvents.EVENT_TYPE_REGULAR;
import static io.appmetrica.analytics.impl.InternalEvents.EVENT_TYPE_SEND_AD_REVENUE_EVENT;
import static io.appmetrica.analytics.impl.InternalEvents.EVENT_TYPE_SEND_ECOMMERCE_EVENT;
import static io.appmetrica.analytics.impl.InternalEvents.EVENT_TYPE_SEND_REFERRER;
import static io.appmetrica.analytics.impl.InternalEvents.EVENT_TYPE_SEND_REVENUE_EVENT;
import static io.appmetrica.analytics.impl.InternalEvents.EVENT_TYPE_SEND_USER_PROFILE;
import static io.appmetrica.analytics.impl.InternalEvents.EVENT_TYPE_SET_SESSION_EXTRA;
import static io.appmetrica.analytics.impl.InternalEvents.EVENT_TYPE_SET_USER_PROFILE_ID;
import static io.appmetrica.analytics.impl.InternalEvents.EVENT_TYPE_START;
import static io.appmetrica.analytics.impl.InternalEvents.EVENT_TYPE_WEBVIEW_SYNC;

public class EventProcessingStrategyFactory extends ProcessingStrategyFactory<ReportComponentHandler> {

    private final ReportingHandlerProvider mHandlersProvider;

    private final Map<InternalEvents, HandlersFactory<ReportComponentHandler>> mFactories;

    @Nullable
    private CommonHandlersFactory<ReportComponentHandler> mPreMainFactory;

    public EventProcessingStrategyFactory(ComponentUnit componentUnit) {
        mHandlersProvider = new ReportingHandlerProvider(componentUnit);
        mPreMainFactory = new CommonConditionalFactory(mHandlersProvider);
        mFactories = createHandlersMap();
    }

    @SuppressWarnings("checkstyle:methodLength")
    private HashMap<InternalEvents, HandlersFactory<ReportComponentHandler>> createHandlersMap() {
        HashMap<InternalEvents, HandlersFactory<ReportComponentHandler>> map =
                new HashMap<InternalEvents, HandlersFactory<ReportComponentHandler>>();

        map.put(EVENT_TYPE_ACTIVATION, new ActivationFactory(mHandlersProvider));
        map.put(EVENT_TYPE_START, new StartFactory(mHandlersProvider));

        map.put(EVENT_TYPE_REGULAR, new RegularFactory(mHandlersProvider));

        JustSaveToDataBaseFactory justSaveFactory = new JustSaveToDataBaseFactory(mHandlersProvider);
        map.put(EVENT_TYPE_EXCEPTION_USER_PROTOBUF, justSaveFactory);
        map.put(EVENT_TYPE_EXCEPTION_USER_CUSTOM_PROTOBUF, justSaveFactory);
        map.put(EVENT_TYPE_SEND_REFERRER, justSaveFactory);
        map.put(EVENT_TYPE_CUSTOM_EVENT, justSaveFactory);
        map.put(
            EVENT_TYPE_SET_SESSION_EXTRA,
            new SingleHandlerFactory(mHandlersProvider, mHandlersProvider.getSaveSessionExtrasHandler())
        );
        map.put(EVENT_TYPE_APP_OPEN, new ReportAppOpenFactory(mHandlersProvider));

        map.put(EVENT_TYPE_PURGE_BUFFER, new PurgeBufferFactory(mHandlersProvider));
        map.put(EVENT_TYPE_CURRENT_SESSION_NATIVE_CRASH_PROTOBUF,
            new CurrentSessionNativeCrashHandlerFactory(mHandlersProvider));
        map.put(EVENT_TYPE_PREV_SESSION_NATIVE_CRASH_PROTOBUF,
            new PrevSessionNativeCrashHandlerFactory(mHandlersProvider));
        map.put(EVENT_TYPE_EXCEPTION_UNHANDLED_FROM_FILE, new UnhandledExceptionFromFileFactory(mHandlersProvider));

        UnhandledExceptionFactory unhandledExceptionFactory = new UnhandledExceptionFactory(mHandlersProvider);
        map.put(EVENT_TYPE_EXCEPTION_UNHANDLED_PROTOBUF, unhandledExceptionFactory);
        map.put(EVENT_TYPE_EXCEPTION_UNHANDLED_FROM_INTENT, unhandledExceptionFactory);

        map.put(EVENT_TYPE_ANR, justSaveFactory);
        map.put(EVENT_TYPE_APP_ENVIRONMENT_UPDATED, new SingleHandlerFactory(
                mHandlersProvider,
                mHandlersProvider.getReportAppEnvironmentUpdated())
        );
        map.put(EVENT_TYPE_APP_ENVIRONMENT_CLEARED, new SingleHandlerFactory(
                mHandlersProvider,
                mHandlersProvider.getReportAppEnvironmentCleared())
        );
        map.put(EVENT_TYPE_SEND_USER_PROFILE, justSaveFactory);
        map.put(EVENT_TYPE_SET_USER_PROFILE_ID, new SingleHandlerFactory(
                mHandlersProvider,
                mHandlersProvider.getUpdateUserProfileIDHandler()));
        map.put(EVENT_TYPE_SEND_REVENUE_EVENT, justSaveFactory);
        map.put(EVENT_TYPE_SEND_AD_REVENUE_EVENT, justSaveFactory);
        map.put(EVENT_TYPE_CLEANUP, justSaveFactory);
        map.put(EVENT_TYPE_SEND_ECOMMERCE_EVENT, justSaveFactory);
        map.put(EVENT_TYPE_WEBVIEW_SYNC, justSaveFactory);

        return map;
    }

    public void mutateHandlers(InternalEvents type, HandlersFactory<ReportComponentHandler> factory) {
        mFactories.put(type, factory);
    }

    public ReportingHandlerProvider getHandlersProvider() {
        return mHandlersProvider;
    }

    @Override
    public EventProcessingStrategy<ReportComponentHandler> getProcessingStrategy(int eventTypeId) {
        List<ReportComponentHandler> reportHandlers = new LinkedList<ReportComponentHandler>();
        InternalEvents eventType = InternalEvents.valueOf(eventTypeId);
        if (mPreMainFactory != null) {
            mPreMainFactory.addHandlers(eventType, reportHandlers);
        }
        HandlersFactory<ReportComponentHandler> factory = mFactories.get(eventType);
        if (factory != null) {
            factory.addHandlers(reportHandlers);
        }
        return new EventProcessingDefaultStrategy<ReportComponentHandler>(reportHandlers);
    }

    @VisibleForTesting
    public HandlersFactory getHandlersFactory(InternalEvents type) {
        return mFactories.get(type);
    }

}
