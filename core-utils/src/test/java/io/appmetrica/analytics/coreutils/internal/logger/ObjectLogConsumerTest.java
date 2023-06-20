package io.appmetrica.analytics.coreutils.internal.logger;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.robolectric.RobolectricTestRunner;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith(RobolectricTestRunner.class)
public class ObjectLogConsumerTest {

    @Mock
    private MultilineMessageLogConsumer mConsumer;
    @Mock
    private IObjectLogDumper<Object> mDumper;
    @Mock
    private Object input;

    private final String tag = "Tag";
    private final Object[] args = new Object[] {new Object(), new Object()};
    private final String dumpedObject = "Dumped object";

    private ObjectLogConsumer<Object> mObjectLogConsumer;

    @Before
    public void setUp() throws Exception {
        MockitoAnnotations.openMocks(this);

        when(mDumper.dumpObject(input)).thenReturn(dumpedObject);

        mObjectLogConsumer = new ObjectLogConsumer<Object>(mConsumer, mDumper);
    }

    @Test
    public void consumeWithTag() {
        mObjectLogConsumer.consumeWithTag(tag, input, args);
        verify(mConsumer).consumeWithTag(tag, dumpedObject, args);
    }

    @Test
    public void consume() {
        mObjectLogConsumer.consume(input, args);
        verify(mConsumer).consume(dumpedObject, args);
    }
}
