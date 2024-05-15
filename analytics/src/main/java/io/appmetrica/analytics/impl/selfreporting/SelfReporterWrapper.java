package io.appmetrica.analytics.impl.selfreporting;

import android.content.Context;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import io.appmetrica.analytics.AdRevenue;
import io.appmetrica.analytics.ModuleEvent;
import io.appmetrica.analytics.Revenue;
import io.appmetrica.analytics.ecommerce.ECommerceEvent;
import io.appmetrica.analytics.impl.IReporterExtended;
import io.appmetrica.analytics.impl.SdkData;
import io.appmetrica.analytics.impl.crash.client.AllThreads;
import io.appmetrica.analytics.impl.crash.client.UnhandledException;
import io.appmetrica.analytics.impl.proxy.ReporterProxyStorage;
import io.appmetrica.analytics.logger.internal.DebugLogger;
import io.appmetrica.analytics.plugins.IPluginReporter;
import io.appmetrica.analytics.plugins.PluginErrorDetails;
import io.appmetrica.analytics.profile.UserProfile;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class SelfReporterWrapper implements IReporterExtended, IPluginReporter {

    private static final String TAG = "[SelfReporterWrapper]";

    @NonNull
    private final List<IReporterCommandPerformer> mBufferedEvents = new ArrayList<IReporterCommandPerformer>();

    @Nullable
    private volatile IReporterExtended mReporter;

    synchronized void onInitializationFinished(@NonNull Context context) {
        DebugLogger.info(TAG, "core initialization finished. Initializing SelfReporter");
        mReporter = ReporterProxyStorage.getInstance().getOrCreate(context, SdkData.SDK_API_KEY_UUID);
        for (IReporterCommandPerformer performer : mBufferedEvents) {
            performer.perform(mReporter);
        }
        mBufferedEvents.clear();
    }

    @Override
    public void reportUnhandledException(@NonNull final UnhandledException unhandledException) {
        processCommand(new IReporterCommandPerformer() {
            @Override
            public void perform(@NonNull IReporterExtended reporter) {
                reporter.reportUnhandledException(unhandledException);
            }
        });
    }

    @Override
    public void reportAnr(@NonNull final AllThreads allThreads) {
        processCommand(new IReporterCommandPerformer() {
            @Override
            public void perform(@NonNull IReporterExtended reporter) {
                reporter.reportAnr(allThreads);
            }
        });
    }

    @Override
    public void putAppEnvironmentValue(@NonNull final String key, @Nullable final String value) {
        processCommand(new IReporterCommandPerformer() {
            @Override
            public void perform(@NonNull IReporterExtended reporter) {
                reporter.putAppEnvironmentValue(key, value);
            }
        });
    }

    @Override
    public void clearAppEnvironment() {
        processCommand(new IReporterCommandPerformer() {
            @Override
            public void perform(@NonNull IReporterExtended reporter) {
                reporter.clearAppEnvironment();
            }
        });
    }

    @Override
    public void sendEventsBuffer() {
        processCommand(new IReporterCommandPerformer() {
            @Override
            public void perform(@NonNull IReporterExtended reporter) {
                reporter.sendEventsBuffer();
            }
        });
    }

    @Override
    public void reportEvent(@NonNull final String eventName) {
        processCommand(new IReporterCommandPerformer() {
            @Override
            public void perform(@NonNull IReporterExtended reporter) {
                reporter.reportEvent(eventName);
            }
        });
    }

    @Override
    public void reportEvent(@NonNull final String eventName, @Nullable final String jsonValue) {
        processCommand(new IReporterCommandPerformer() {
            @Override
            public void perform(@NonNull IReporterExtended reporter) {
                reporter.reportEvent(eventName, jsonValue);
            }
        });
    }

    @Override
    public void reportEvent(@NonNull final String eventName, @Nullable final Map<String, Object> attributes) {
        processCommand(new IReporterCommandPerformer() {
            @Override
            public void perform(@NonNull IReporterExtended reporter) {
                reporter.reportEvent(eventName, attributes);
            }
        });
    }

    @Override
    public void reportError(@NonNull final String message, @Nullable final Throwable error) {
        processCommand(new IReporterCommandPerformer() {
            @Override
            public void perform(@NonNull IReporterExtended reporter) {
                reporter.reportError(message, error);
            }
        });
    }

    @Override
    public void reportError(@NonNull String identifier, @Nullable String message) {
        reportError(identifier, message, (Throwable) null);
    }

    @Override
    public void reportError(
            @NonNull final String identifier,
            @Nullable final String message,
            @NonNull final Throwable error
    ) {
        processCommand(new IReporterCommandPerformer() {
            @Override
            public void perform(@NonNull IReporterExtended reporter) {
                reporter.reportError(identifier, message, error);
            }
        });
    }

    @Override
    public void reportUnhandledException(@NonNull final Throwable exception) {
        processCommand(new IReporterCommandPerformer() {
            @Override
            public void perform(@NonNull IReporterExtended reporter) {
                reporter.reportUnhandledException(exception);
            }
        });
    }

    @Override
    public void resumeSession() {
        processCommand(new IReporterCommandPerformer() {
            @Override
            public void perform(@NonNull IReporterExtended reporter) {
                reporter.resumeSession();
            }
        });
    }

    @Override
    public void pauseSession() {
        processCommand(new IReporterCommandPerformer() {
            @Override
            public void perform(@NonNull IReporterExtended reporter) {
                reporter.pauseSession();
            }
        });
    }

    @Override
    public void setUserProfileID(@Nullable final String profileID) {
        processCommand(new IReporterCommandPerformer() {
            @Override
            public void perform(@NonNull IReporterExtended reporter) {
                reporter.setUserProfileID(profileID);
            }
        });
    }

    @Override
    public void reportUserProfile(@NonNull final UserProfile profile) {
        processCommand(new IReporterCommandPerformer() {
            @Override
            public void perform(@NonNull IReporterExtended reporter) {
                reporter.reportUserProfile(profile);
            }
        });
    }

    @Override
    public void reportRevenue(@NonNull final Revenue revenue) {
        processCommand(new IReporterCommandPerformer() {
            @Override
            public void perform(@NonNull IReporterExtended reporter) {
                reporter.reportRevenue(revenue);
            }
        });
    }

    @Override
    public void reportECommerce(@NonNull final ECommerceEvent event) {
        processCommand(new IReporterCommandPerformer() {
            @Override
            public void perform(@NonNull IReporterExtended reporter) {
                reporter.reportECommerce(event);
            }
        });
    }

    @Override
    public void setDataSendingEnabled(final boolean enabled) {
        processCommand(new IReporterCommandPerformer() {
            @Override
            public void perform(@NonNull IReporterExtended reporter) {
                reporter.setDataSendingEnabled(enabled);
            }
        });
    }

    @NonNull
    @Override
    public IPluginReporter getPluginExtension() {
        return this;
    }

    @Override
    public void reportAdRevenue(@NonNull final AdRevenue adRevenue) {
        processCommand(new IReporterCommandPerformer() {
            @Override
            public void perform(@NonNull IReporterExtended reporter) {
                reporter.reportAdRevenue(adRevenue);
            }
        });
    }

    @Override
    public void reportUnhandledException(@NonNull final PluginErrorDetails errorDetails) {
        processCommand(new IReporterCommandPerformer() {
            @Override
            public void perform(@NonNull IReporterExtended reporter) {
                reporter.getPluginExtension().reportUnhandledException(errorDetails);
            }
        });
    }

    @Override
    public void reportError(@NonNull final PluginErrorDetails errorDetails, @Nullable final String message) {
        processCommand(new IReporterCommandPerformer() {
            @Override
            public void perform(@NonNull IReporterExtended reporter) {
                reporter.getPluginExtension().reportError(errorDetails, message);
            }
        });
    }

    @Override
    public void reportError(@NonNull final String identifier,
                            @Nullable final String message,
                            @Nullable final PluginErrorDetails errorDetails) {
        processCommand(new IReporterCommandPerformer() {
            @Override
            public void perform(@NonNull IReporterExtended reporter) {
                reporter.getPluginExtension().reportError(identifier, message, errorDetails);
            }
        });
    }

    private synchronized void processCommand(@NonNull IReporterCommandPerformer commandPerformer) {
        if (mReporter == null) {
            mBufferedEvents.add(commandPerformer);
        } else {
            commandPerformer.perform(mReporter);
        }
    }

    @Override
    public void reportEvent(
        @NonNull final ModuleEvent moduleEvent
    ) {
        processCommand(new IReporterCommandPerformer() {
            @Override
            public void perform(@NonNull IReporterExtended reporter) {
                reporter.reportEvent(moduleEvent);
            }
        });
    }

    @Override
    public void setSessionExtra(@NonNull final String key, @Nullable final byte[] value) {
        processCommand(new IReporterCommandPerformer() {
            @Override
            public void perform(@NonNull IReporterExtended reporter) {
                reporter.setSessionExtra(key, value);
            }
        });
    }

    @Override
    public void reportAdRevenue(@NonNull final AdRevenue adRevenue, final boolean autoCollected) {
        processCommand(new IReporterCommandPerformer() {
            @Override
            public void perform(@NonNull IReporterExtended reporter) {
                reporter.reportAdRevenue(adRevenue, autoCollected);
            }
        });
    }
}
