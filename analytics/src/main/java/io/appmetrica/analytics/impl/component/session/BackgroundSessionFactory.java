package io.appmetrica.analytics.impl.component.session;

import androidx.annotation.NonNull;
import androidx.annotation.VisibleForTesting;
import io.appmetrica.analytics.coreutils.internal.time.SystemTimeProvider;
import io.appmetrica.analytics.impl.IReporterExtended;
import io.appmetrica.analytics.impl.component.ComponentUnit;
import io.appmetrica.analytics.impl.selfreporting.AppMetricaSelfReportFacade;

public class BackgroundSessionFactory extends AbstractSessionFactory {

    static final int SESSION_TIMEOUT_SEC = 60 * 60;
    public static final String SESSION_TAG = "background";

    BackgroundSessionFactory(@NonNull final ComponentUnit component,
                             @NonNull SessionIDProvider sessionIDProvider) {
        this(
                component,
                sessionIDProvider,
                new SessionStorageImpl(component.getComponentPreferences(), SESSION_TAG),
                AppMetricaSelfReportFacade.getReporter(),
                new SystemTimeProvider()
        );
    }

    @VisibleForTesting
    BackgroundSessionFactory(@NonNull final ComponentUnit component,
                             @NonNull SessionIDProvider sessionIDProvider,
                             @NonNull final SessionStorageImpl storage,
                             @NonNull IReporterExtended selfReporter,
                             @NonNull SystemTimeProvider timeProvider) {
        super(
                component,
                sessionIDProvider,
                storage,
                SessionFactoryArguments.newBuilder(SessionType.BACKGROUND)
                        .withSessionTimeout(SESSION_TIMEOUT_SEC).build(),
                selfReporter,
                timeProvider
        );
    }

}
