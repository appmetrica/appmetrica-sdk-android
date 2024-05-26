package io.appmetrica.analytics.impl.db.protobuf;

import io.appmetrica.analytics.coreapi.internal.data.IBinaryDataHelper;
import io.appmetrica.analytics.coreapi.internal.data.ProtobufConverter;
import io.appmetrica.analytics.protobuf.nano.InvalidProtocolBufferNanoException;
import io.appmetrica.analytics.protobuf.nano.MessageNano;
import io.appmetrica.analytics.testutils.CommonTest;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.same;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

@RunWith(RobolectricTestRunner.class)
public class ProtobufStateStorageImplTest extends CommonTest {

    private final String mKey = "testKey";
    private final IBinaryDataHelper mDataHelper = mock(IBinaryDataHelper.class);
    private final BaseProtobufStateSerializer mSerializer = mock(BaseProtobufStateSerializer.class);
    private final ProtobufConverter mConverter = mock(ProtobufConverter.class);

    private final ProtobufStateStorageImpl mHelper = new ProtobufStateStorageImpl(mKey, mDataHelper, mSerializer, mConverter);

    @Test
    public void testSave() {
        byte[] testArray = new byte[]{};
        doReturn(mock(MessageNano.class)).when(mConverter).fromModel(any());
        doReturn(testArray).when(mSerializer).toByteArray(any(MessageNano.class));

        mHelper.save(mock(Object.class));

        verify(mDataHelper).insert(eq(mKey), same(testArray));
    }

    @Test
    public void testReadExisted() throws Exception {
        MessageNano message = mock(MessageNano.class);
        doReturn(new byte[]{1, 2, 3, 4}).when(mDataHelper).get(mKey);
        doReturn(message).when(mSerializer).toState(any(byte[].class));

        mHelper.read();

        verify(mConverter).toModel(message);
    }

    @Test
    public void testReadNull() {
        MessageNano message = mock(MessageNano.class);
        doReturn(null).when(mDataHelper).get(mKey);
        doReturn(message).when(mSerializer).defaultValue();

        mHelper.read();

        verify(mConverter).toModel(message);
    }

    @Test
    public void testReadEmpty() {
        MessageNano message = mock(MessageNano.class);
        doReturn(new byte[0]).when(mDataHelper).get(mKey);
        doReturn(message).when(mSerializer).defaultValue();

        mHelper.read();

        verify(mConverter).toModel(message);
    }

    @Test
    public void testCreateDefault() throws Exception {
        doThrow(InvalidProtocolBufferNanoException.class).when(mSerializer).toState(any(byte[].class));

        MessageNano message = mock(MessageNano.class);
        doReturn(message).when(mSerializer).defaultValue();

        mHelper.read();

        verify(mConverter).toModel(message);
    }

    @Test
    public void delete() {
        mHelper.delete();
        verify(mDataHelper).remove(mKey);
    }

}
