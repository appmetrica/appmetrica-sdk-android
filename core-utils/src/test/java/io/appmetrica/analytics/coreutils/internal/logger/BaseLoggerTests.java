package io.appmetrica.analytics.coreutils.internal.logger;

import android.util.Log;
import java.util.Locale;
import org.assertj.core.internal.bytebuddy.utility.RandomString;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import org.robolectric.RobolectricTestRunner;

import static org.mockito.ArgumentMatchers.contains;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;

@RunWith(RobolectricTestRunner.class)
public class BaseLoggerTests {

    private BaseLogger mLogger;

    private static final String TAG = "TestLogger";
    private static final String PREFIX = "[TEST]";

    @Before
    public void setUp() {
        mLogger = new BaseLogger(true) {
            @Override
            protected String getTag() {
                return TAG;
            }

            @Override
            protected String getPrefix() {
                return PREFIX;
            }

            @Override
            protected String formatMessage(String message, Object[] params) {
                return String.format(Locale.US, message, params);
            }
        };
    }

    @Test
    public void testDShouldNotThrowExceptionIfMsgIsNull() {
        mLogger.d(null);
    }

    @Test
    public void testDShouldNotThrowExceptionIfMsgIsNullWithArgs() {
        mLogger.fd(null, RandomString.make(100));
    }

    @Test
    public void testDShouldNotThrowExceptionIfMsgIsEmpty() {
        mLogger.d("");
    }

    @Test
    public void testDShouldNotThrowExceptionIfMsgIsEmptyWithArgs() {
        mLogger.fd("", RandomString.make(100));
    }

    @Test
    public void testWShouldNotThrowExceptionIfMsgIsNull() {
        mLogger.w(null);
    }

    @Test
    public void testWShouldNotThrowExceptionIfMsgIsNullWithArgs() {
        mLogger.fw(null, RandomString.make(100));
    }

    @Test
    public void testWShouldNotThrowExceptionIfMsgIsEmpty() {
        mLogger.w("");
    }

    @Test
    public void testWShouldNotThrowExceptionIfMsgIsEmptyWithArgs() {
        mLogger.fw("", RandomString.make(100));
    }

    @Test
    public void testIShouldNotThrowExceptionIfMsgInNull() {
        mLogger.i(null);
    }

    @Test
    public void testIShouldNotThrowExceptionIfMsgIsNullWithArgs() {
        mLogger.fi(null, RandomString.make(100));
    }

    @Test
    public void testIShouldNotThrowExceptionIfMsgIsEmpty() {
        mLogger.i("");
    }

    @Test
    public void testIShouldNotThrowExceptionIfMsgIsEmptyWithArgs() {
        mLogger.fi("", RandomString.make(100));
    }

    @Test
    public void testEShouldNotThrowExceptionIfExceptionIsNull() {
        mLogger.e(null, "msg");
    }

    @Test
    public void testEShouldNotThrowExceptionIfMsgIsNull() {
        mLogger.e(null);
    }

    @Test
    public void testEShouldNotThrowExceptionIfMsgIsNullWithArgs() {
        mLogger.fe((String) null, RandomString.make(100));
    }

    @Test
    public void testEShouldNotThrowExceptionIfMsgIsEmpty() {
        mLogger.e("");
    }

    @Test
    public void testEShouldNotThrowExceptionIfMsgIsEmptyWithArgs() {
        mLogger.fe("", RandomString.make(100));
    }

    @Test
    public void testEWithThrowableShouldNotThrowExceptionIfMsgIsNull() {
        mLogger.e(new Exception(), null);
    }

    @Test
    public void testEWithThrowableShouldNotThrowExceptionIfMsgIsNullWithArgs() {
        mLogger.fe(new Exception(), null, RandomString.make(100));
    }

    @Test
    public void testEWithThrowableShouldNotThrowExceptionIfMsgIsEmpty() {
        mLogger.e(new Exception(), "");
    }

    @Test
    public void testEWithThrowableShouldNotThrowExceptionIfMsgIsEmptyWithArgs() {
        mLogger.fe(new Exception(), "", RandomString.make(100));
    }

    @Test
    public void testPercentCharacterInValue() {
        mLogger.fi("%s", "29%D");
    }

    @Test
    public void testPercentInMessage() {
        try (MockedStatic<Log> mockStatic = Mockito.mockStatic(Log.class)) {
            final String message = "%D45%DF";
            mLogger.log(Log.DEBUG, message);
            mockStatic.verify(new MockedStatic.Verification() {
                @Override
                public void apply() {
                    Log.println(eq(Log.DEBUG), eq(TAG), eq(PREFIX + message));
                }
            });
        }
    }

    @Test
    public void testPercentInFormattedMessage() {
        try (MockedStatic<Log> mockStatic = Mockito.mockStatic(Log.class)) {
            mLogger.log(Log.DEBUG, "%D45%DF", "argument");
            mockStatic.verify(new MockedStatic.Verification() {
                @Override
                public void apply() {
                    Log.println(eq(Log.DEBUG), eq(TAG), eq(PREFIX));
                }
            });
        }
    }

    @Test
    public void testPercentInFormattedMessageArgument() {
        try (MockedStatic<Log> mockStatic = Mockito.mockStatic(Log.class)) {
            final String argument = "argu%DF2ment";
            mLogger.log(Log.DEBUG, "%s", argument);
            mockStatic.verify(new MockedStatic.Verification() {
                @Override
                public void apply() {
                    Log.println(eq(Log.DEBUG), eq(TAG), eq(PREFIX + argument));
                }
            });
        }
    }

    @Test
    public void testForceW() {
        try (MockedStatic<Log> mockStatic = Mockito.mockStatic(Log.class)) {
            final String warning = "warning";
            mLogger.setDisabled();
            mLogger.forceW(warning);
            mockStatic.verify(new MockedStatic.Verification() {
                @Override
                public void apply() {
                    Log.println(eq(Log.WARN), eq(TAG), contains(warning));
                }
            });
        }
    }

    @Test
    public void testForceE() {
        try (MockedStatic<Log> mockStatic = Mockito.mockStatic(Log.class)) {
            final String exceptionMsg = "exceptionMsg";
            mLogger.setDisabled();
            mLogger.forceE(mock(Throwable.class), exceptionMsg);
            mockStatic.verify(new MockedStatic.Verification() {
                @Override
                public void apply() {
                    Log.println(eq(Log.ERROR), eq(TAG), contains(exceptionMsg));
                }
            });
        }
    }

    @Test
    public void testForceI() {
        try (MockedStatic<Log> mockStatic = Mockito.mockStatic(Log.class)) {
            mLogger.setDisabled();
            mLogger.forceI("Int: %d, string: %s", 73, "test");
            mockStatic.verify(new MockedStatic.Verification() {
                @Override
                public void apply() {
                    Log.println(eq(Log.INFO), eq(TAG), contains("Int: 73, string: test"));
                }
            });
        }
    }
}
