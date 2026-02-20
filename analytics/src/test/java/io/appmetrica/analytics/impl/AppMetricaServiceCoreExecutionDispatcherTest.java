package io.appmetrica.analytics.impl;

import android.content.Intent;
import android.content.res.Configuration;
import android.os.Bundle;
import io.appmetrica.analytics.coreapi.internal.executors.ICommonExecutor;
import io.appmetrica.analytics.testutils.CommonTest;
import java.util.Arrays;
import java.util.Collection;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameters;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;

@RunWith(Parameterized.class)
public class AppMetricaServiceCoreExecutionDispatcherTest extends CommonTest {

    interface Command {
        void execute(AppMetricaServiceCoreExecutionDispatcher dispatcher);
    }

    interface Verifier {
        void verify(AppMetricaServiceCore appMetricaServiceCore) throws Exception;
    }

    private Command mCommand;
    private Verifier mVerifier;
    private String description;

    public AppMetricaServiceCoreExecutionDispatcherTest(Command command,
                                                        Verifier verifier,
                                                        String description) {
        mCommand = command;
        mVerifier = verifier;
        this.description = description;
    }

    private static final Intent INTENT = new Intent();
    private static final Configuration CONFIGURATION = new Configuration();
    private static final int START_ID = 213;
    private static final int FLAGS = 1;
    private static final Bundle BUNDLE = new Bundle();
    private static final int DATA_TYPE = 34;

    @Parameters(name = "[{index}] {2}")
    public static Collection<Object[]> data() {
        return Arrays.asList(new Object[][]{
                {
                        new Command() {
                            @Override
                            public void execute(AppMetricaServiceCoreExecutionDispatcher dispatcher) {
                                dispatcher.onStart(INTENT, START_ID);
                            }
                        },
                        new Verifier() {
                            @Override
                            public void verify(AppMetricaServiceCore appMetricaServiceCore) {
                                appMetricaServiceCore.onStart(INTENT, START_ID);
                            }
                        },
                        "onStart()"
                },
                {
                        new Command() {
                            @Override
                            public void execute(AppMetricaServiceCoreExecutionDispatcher dispatcher) {
                                dispatcher.onStartCommand(INTENT, FLAGS, START_ID);
                            }
                        },
                        new Verifier() {
                            @Override
                            public void verify(AppMetricaServiceCore appMetricaServiceCore) {
                                appMetricaServiceCore.onStartCommand(INTENT, FLAGS, START_ID);
                            }
                        },
                        "onStartCommand()"
                },
                {
                        new Command() {
                            @Override
                            public void execute(AppMetricaServiceCoreExecutionDispatcher dispatcher) {
                                dispatcher.onBind(INTENT);
                            }
                        },
                        new Verifier() {
                            @Override
                            public void verify(AppMetricaServiceCore appMetricaServiceCore) {
                                appMetricaServiceCore.onBind(INTENT);
                            }
                        },
                        "onBind()"
                },
                {
                        new Command() {
                            @Override
                            public void execute(AppMetricaServiceCoreExecutionDispatcher dispatcher) {
                                dispatcher.onRebind(INTENT);
                            }
                        },
                        new Verifier() {
                            @Override
                            public void verify(AppMetricaServiceCore appMetricaServiceCore) {
                                appMetricaServiceCore.onRebind(INTENT);
                            }
                        },
                        "onRebind()"
                },
                {
                        new Command() {
                            @Override
                            public void execute(AppMetricaServiceCoreExecutionDispatcher dispatcher) {
                                dispatcher.onUnbind(INTENT);
                            }
                        },
                        new Verifier() {
                            @Override
                            public void verify(AppMetricaServiceCore appMetricaServiceCore) {
                                appMetricaServiceCore.onUnbind(INTENT);
                            }
                        },
                        "onUnbind()"
                },
                {
                        new Command() {
                            @Override
                            public void execute(AppMetricaServiceCoreExecutionDispatcher dispatcher) {
                                dispatcher.onConfigurationChanged(CONFIGURATION);
                            }
                        },
                        new Verifier() {
                            @Override
                            public void verify(AppMetricaServiceCore appMetricaServiceCore) throws Exception {
                                appMetricaServiceCore.onConfigurationChanged(CONFIGURATION);
                            }
                        },
                        "onConfigurationChanged"
                },
                {
                        new Command() {
                            @Override
                            public void execute(AppMetricaServiceCoreExecutionDispatcher dispatcher) {
                                dispatcher.resumeUserSession(BUNDLE);
                            }
                        },
                        new Verifier() {
                            @Override
                            public void verify(AppMetricaServiceCore appMetricaServiceCore) throws Exception {
                                appMetricaServiceCore.resumeUserSession(BUNDLE);
                            }
                        },
                        "resumeUserSession()"
                },
                {
                        new Command() {
                            @Override
                            public void execute(AppMetricaServiceCoreExecutionDispatcher dispatcher) {
                                dispatcher.pauseUserSession(BUNDLE);
                            }
                        },
                        new Verifier() {
                            @Override
                            public void verify(AppMetricaServiceCore appMetricaServiceCore) throws Exception {
                                appMetricaServiceCore.pauseUserSession(BUNDLE);
                            }
                        },
                        "pauseUserSession()"
                },
                {
                        new Command() {
                            @Override
                            public void execute(AppMetricaServiceCoreExecutionDispatcher dispatcher) {
                                dispatcher.reportData(DATA_TYPE, BUNDLE);
                            }
                        },
                        new Verifier() {
                            @Override
                            public void verify(AppMetricaServiceCore appMetricaServiceCore) throws Exception {
                                appMetricaServiceCore.reportData(DATA_TYPE, BUNDLE);
                            }
                        },
                        "reportData()"
                }
        });
    }

    @Mock
    private ICommonExecutor mExecutor;
    @Mock
    private AppMetricaServiceCore mAppMetricaServiceCore;
    @Captor
    private ArgumentCaptor<Runnable> mRunnableCaptor;

    private AppMetricaServiceCoreExecutionDispatcher mAppMetricaCoreExecutionDispatcher;

    @Before
    public void setUp() throws Exception {
        MockitoAnnotations.openMocks(this);

        mAppMetricaCoreExecutionDispatcher =
            new AppMetricaServiceCoreExecutionDispatcher(mExecutor, mAppMetricaServiceCore);
    }

    @Test
    public void testDispatchCallToMetricaCoreImpl() throws Exception {
        mCommand.execute(mAppMetricaCoreExecutionDispatcher);
        verifyNoMoreInteractions(mAppMetricaServiceCore);
        verify(mExecutor).execute(mRunnableCaptor.capture());
        mRunnableCaptor.getValue().run();
        mVerifier.verify(verify(mAppMetricaServiceCore));
    }
}
