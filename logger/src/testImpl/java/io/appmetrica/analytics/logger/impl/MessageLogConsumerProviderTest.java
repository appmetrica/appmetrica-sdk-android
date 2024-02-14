package io.appmetrica.analytics.logger.impl;

import org.json.JSONObject;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.robolectric.RobolectricTestRunner;

import static org.assertj.core.api.Assertions.assertThat;

@RunWith(RobolectricTestRunner.class)
public class MessageLogConsumerProviderTest {

    @Mock
    private BaseLogger mBaseLogger;

    private MessageLogConsumerProvider mMessageLogConsumerProvider;
    private MultilineMessageLogConsumer mDebugConsumer;
    private MultilineMessageLogConsumer mInfoConsumer;
    private MultilineMessageLogConsumer mWarningConsumer;
    private ObjectLogConsumer<JSONObject> mJSONObjectObjectLogConsumer;

    @Before
    public void setUp() throws Exception {
        MockitoAnnotations.openMocks(this);

        mMessageLogConsumerProvider = new MessageLogConsumerProvider(mBaseLogger);

        mDebugConsumer = (MultilineMessageLogConsumer) mMessageLogConsumerProvider.getDebugLogConsumer();
        mInfoConsumer = (MultilineMessageLogConsumer) mMessageLogConsumerProvider.getInfoLogConsumer();
        mWarningConsumer = (MultilineMessageLogConsumer) mMessageLogConsumerProvider.getWarningMessageLogConsumer();
        mJSONObjectObjectLogConsumer =
                (ObjectLogConsumer<JSONObject>) mMessageLogConsumerProvider.getJsonInfoLogConsumer();
    }

    @Test
    public void debugLogConsumerSplitter() {
        assertThat(mDebugConsumer.getLogMessageSplitter())
                .isNotNull()
                .isInstanceOf(LogMessageByLineLimitSplitter.class);
    }

    @Test
    public void debugLogInternalMessageConsumer() {
        SingleInfoMessageLogConsumer singleInfoMessageLogConsumer =
                (SingleInfoMessageLogConsumer) mDebugConsumer.getSingleLineLogConsumer();

        assertThat(singleInfoMessageLogConsumer.getLogger()).isEqualTo(mBaseLogger);
    }

    @Test
    public void sameLogConsumerForDebugAndInfo() {
        assertThat(mInfoConsumer).isEqualTo(mDebugConsumer);
    }

    @Test
    public void warningLogConsumerSplitter() {
        assertThat(mWarningConsumer.getLogMessageSplitter())
                .isEqualTo(mDebugConsumer.getLogMessageSplitter());
    }

    @Test
    public void warningLogConsumerInternalMessageConsumer() {
        SingleWarningMessageLogConsumer singleWarningMessageLogConsumer =
                (SingleWarningMessageLogConsumer) mWarningConsumer.getSingleLineLogConsumer();

        assertThat(singleWarningMessageLogConsumer.getLogger()).isEqualTo(mBaseLogger);
    }

    @Test
    public void jsonObjectLogConsumerInternalConsumer() {
        MultilineMessageLogConsumer multilineMessageLogConsumer = mJSONObjectObjectLogConsumer.getMessageLogConsumer();

        assertThat(multilineMessageLogConsumer.getLogMessageSplitter())
                .as("Log message splitter")
                .isNotNull()
                .isInstanceOf(LogMessageByLineBreakSplitter.class);

        assertThat(multilineMessageLogConsumer.getSingleLineLogConsumer())
                .as("Single log consumer")
                .isEqualTo(mDebugConsumer.getSingleLineLogConsumer());
    }

    @Test
    public void jsonObjectLogConsumerObjectDumper() {
        assertThat(mJSONObjectObjectLogConsumer.getObjectLogDumper())
                .isNotNull()
                .isInstanceOf(JsonObjectLogDumper.class);
    }
}
