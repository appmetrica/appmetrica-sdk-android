package io.appmetrica.analytics.impl.component.remarketing;

import android.content.Context;
import io.appmetrica.analytics.coreapi.internal.data.IBinaryDataHelper;
import io.appmetrica.analytics.impl.component.ComponentId;
import io.appmetrica.analytics.impl.protobuf.client.Eventhashes;
import io.appmetrica.analytics.testutils.CommonTest;
import java.io.IOException;
import java.util.Arrays;
import java.util.HashSet;
import org.assertj.core.api.SoftAssertions;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.RuntimeEnvironment;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith(RobolectricTestRunner.class)
public class EventHashesStorageTest extends CommonTest {

    @Mock
    private EventHashesSerializer mSerializer;
    @Mock
    private EventHashesConverter mConverter;
    @Mock
    private IBinaryDataHelper mBinaryDataHelper;
    @Mock
    private ComponentId mComponentId;

    private final Eventhashes.EventHashes mProtoEventHashes = new Eventhashes.EventHashes();

    private final Context mContext = RuntimeEnvironment.getApplication();
    private EventHashesStorage mStorage;

    private static final String DB_KEY = "Test db key";

    @Before
    public void setUp() {
        MockitoAnnotations.openMocks(this);
        mStorage = new EventHashesStorage(mSerializer, mConverter, mBinaryDataHelper, DB_KEY);
    }

    @Test
    public void testMainConstructor() {
        EventHashesStorage eventHashesStorage = new EventHashesStorage(mContext, mComponentId);

        SoftAssertions softAssertions = new SoftAssertions();
        softAssertions.assertThat(eventHashesStorage.getBinaryDataHelper())
            .as("getBinaryDataHelper()")
            .isNotNull();
        softAssertions.assertThat(eventHashesStorage.getDbKey()).as("getDbKey()")
            .isEqualTo("event_hashes");
        softAssertions.assertThat(eventHashesStorage.getEventHashesSerializer())
            .as("getEventHashesSerializer()")
            .isNotNull();
        softAssertions.assertAll();
    }

    @Test
    public void testReadReturnDefaultObjectForNullFromDb() throws IOException {
        testReadReturnDefaultObject(null);
    }

    @Test
    public void testReadReturnDefaultObjectForEmptyArrayFromDb() throws IOException {
        testReadReturnDefaultObject(new byte[]{});
    }

    private void testReadReturnDefaultObject(byte[] valueFromDb) throws IOException {
        EventHashes eventHashes = new EventHashes();

        when(mBinaryDataHelper.get(DB_KEY)).thenReturn(valueFromDb);
        when(mSerializer.defaultValue()).thenReturn(mProtoEventHashes);
        when(mConverter.toModel(mProtoEventHashes)).thenReturn(eventHashes);
        EventHashes actualEventHashes = mStorage.read();
        verify(mSerializer, times(1)).defaultValue();
        verify(mSerializer, never()).toState(valueFromDb);
        assertThat(actualEventHashes).isEqualToComparingFieldByField(eventHashes);
    }

    @Test
    public void testReadReturnObjectFromSerializer() throws IOException {
        EventHashes eventHashes = new EventHashes(
            true,
            200,
            400,
            new HashSet<Integer>(Arrays.asList(1, 2, 4, 8, 16, 32, 64))
        );

        byte[] rawData = {1, 2, 3, 4, 5, 6, 7, 8, 9};

        when(mBinaryDataHelper.get(DB_KEY)).thenReturn(rawData);
        when(mSerializer.toState(rawData)).thenReturn(mProtoEventHashes);
        when(mConverter.toModel(mProtoEventHashes)).thenReturn(eventHashes);

        EventHashes actualValue = mStorage.read();
        verify(mBinaryDataHelper, times(1)).get(DB_KEY);
        verify(mSerializer, times(1)).toState(rawData);
        verify(mConverter, times(1)).toModel(mProtoEventHashes);
        assertThat(actualValue).isEqualToComparingFieldByField(eventHashes);
    }

    @Test
    public void testWriteSerializedObject() {
        EventHashes eventHashes = new EventHashes(
            true,
            120,
            330,
            new HashSet<Integer>(Arrays.asList(2, 4, 8, 16, 32, 64, 128, 256, 512))
        );

        byte[] rawData = {1, 3, 5, 7, 9, 11, 13, 15, 17, 19, 21};
        when(mConverter.fromModel(eventHashes)).thenReturn(mProtoEventHashes);
        when(mSerializer.toByteArray(mProtoEventHashes)).thenReturn(rawData);
        mStorage.write(eventHashes);

        verify(mConverter, times(1)).fromModel(eventHashes);
        verify(mSerializer, times(1)).toByteArray(mProtoEventHashes);
        verify(mBinaryDataHelper, times(1)).insert(DB_KEY, rawData);
    }
}
