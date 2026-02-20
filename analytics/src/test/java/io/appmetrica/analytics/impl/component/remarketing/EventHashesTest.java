package io.appmetrica.analytics.impl.component.remarketing;

import io.appmetrica.analytics.testutils.CommonTest;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;
import org.assertj.core.api.SoftAssertions;
import org.junit.Test;

public class EventHashesTest extends CommonTest {

    private EventHashes mEventHashes;

    private static final boolean TREAT_NEW_EVENT_AS_UPDATE = true;
    private static final int LAST_VERSION_CODE = 100;
    private static final int HASHES_COUNT_FROM_LAST_VERSION = 555;
    private static final int[] HASHES_ARRAY = {1, 2323, 45, 76, 12, 58, 209, 567, 236, 999};
    private static final Integer[] HASHES_OBJECTS_ARRAY = {1, 2323, 45, 76, 12, 58, 209, 567, 236, 999};
    private static final Set<Integer> HASHES_SET = new HashSet<Integer>(Arrays.asList(HASHES_OBJECTS_ARRAY));

    @Test
    public void testDefaultConstructor() {
        mEventHashes = new EventHashes();
        SoftAssertions softAssertions = new SoftAssertions();
        softAssertions.assertThat(mEventHashes.getEventNameHashes())
            .isNotNull()
            .isInstanceOf(HashSet.class)
            .isEmpty();
        softAssertions.assertThat(mEventHashes.getHashesCountFromLastVersion()).isEqualTo(0);
        softAssertions.assertThat(mEventHashes.getLastVersionCode()).isEqualTo(0);
        softAssertions.assertThat(mEventHashes.treatUnknownEventAsNew()).isFalse();
        softAssertions.assertAll();
    }

    @Test
    public void testConstructorWithHashesArray() {
        mEventHashes = new EventHashes(
            TREAT_NEW_EVENT_AS_UPDATE,
            LAST_VERSION_CODE,
            HASHES_COUNT_FROM_LAST_VERSION,
            HASHES_ARRAY
        );
        SoftAssertions softAssertions = new SoftAssertions();
        softAssertions.assertThat(mEventHashes.treatUnknownEventAsNew()).isEqualTo(TREAT_NEW_EVENT_AS_UPDATE);
        softAssertions.assertThat(mEventHashes.getLastVersionCode()).isEqualTo(LAST_VERSION_CODE);
        softAssertions.assertThat(mEventHashes.getHashesCountFromLastVersion())
            .isEqualTo(HASHES_COUNT_FROM_LAST_VERSION);
        softAssertions.assertThat(mEventHashes.getEventNameHashes()).containsOnly(HASHES_OBJECTS_ARRAY);
        softAssertions.assertAll();
    }

    @Test
    public void testConstructorWithHashesSet() {
        mEventHashes = new EventHashes(
            TREAT_NEW_EVENT_AS_UPDATE,
            LAST_VERSION_CODE,
            HASHES_COUNT_FROM_LAST_VERSION,
            HASHES_SET
        );
        SoftAssertions softAssertions = new SoftAssertions();
        softAssertions.assertThat(mEventHashes.treatUnknownEventAsNew()).isEqualTo(TREAT_NEW_EVENT_AS_UPDATE);
        softAssertions.assertThat(mEventHashes.getLastVersionCode()).isEqualTo(LAST_VERSION_CODE);
        softAssertions.assertThat(mEventHashes.getHashesCountFromLastVersion())
            .isEqualTo(HASHES_COUNT_FROM_LAST_VERSION);
        softAssertions.assertThat(mEventHashes.getEventNameHashes()).containsOnly(HASHES_OBJECTS_ARRAY);
        softAssertions.assertAll();
    }

    @Test
    public void testClearEventHashes() {
        mEventHashes = new EventHashes(
            false,
            LAST_VERSION_CODE,
            HASHES_COUNT_FROM_LAST_VERSION,
            HASHES_SET
        );
        mEventHashes.clearEventHashes();
        SoftAssertions softAssertions = new SoftAssertions();
        softAssertions.assertThat(mEventHashes.treatUnknownEventAsNew()).isFalse();
        softAssertions.assertThat(mEventHashes.getLastVersionCode()).isEqualTo(LAST_VERSION_CODE);
        softAssertions.assertThat(mEventHashes.getHashesCountFromLastVersion()).isEqualTo(0);
        softAssertions.assertThat(mEventHashes.getEventNameHashes()).isEmpty();
        softAssertions.assertAll();
    }

    @Test
    public void testAddEventNameHash() {
        mEventHashes = new EventHashes(
            TREAT_NEW_EVENT_AS_UPDATE,
            LAST_VERSION_CODE,
            HASHES_COUNT_FROM_LAST_VERSION,
            HASHES_SET
        );
        int newEventHash = 999;
        mEventHashes.addEventNameHash(newEventHash);
        SoftAssertions softAssertions = new SoftAssertions();
        softAssertions.assertThat(mEventHashes.treatUnknownEventAsNew()).isTrue();
        softAssertions.assertThat(mEventHashes.getLastVersionCode()).isEqualTo(LAST_VERSION_CODE);
        softAssertions.assertThat(mEventHashes.getHashesCountFromLastVersion())
            .isEqualTo(HASHES_COUNT_FROM_LAST_VERSION + 1);
        Set<Integer> expectedHashes = new HashSet<Integer>(HASHES_SET);
        expectedHashes.add(newEventHash);
        Integer[] expectedHashesArray = expectedHashes.toArray(new Integer[expectedHashes.size()]);
        softAssertions.assertThat(mEventHashes.getEventNameHashes()).containsOnly(expectedHashesArray);
        softAssertions.assertAll();
    }

    @Test
    public void testSetLastVersionCode() {
        mEventHashes = new EventHashes(
            TREAT_NEW_EVENT_AS_UPDATE,
            LAST_VERSION_CODE,
            HASHES_COUNT_FROM_LAST_VERSION,
            HASHES_SET
        );
        int newVersionCode = 111;
        mEventHashes.setLastVersionCode(newVersionCode);
        SoftAssertions softAssertions = new SoftAssertions();
        softAssertions.assertThat(mEventHashes.treatUnknownEventAsNew()).isEqualTo(TREAT_NEW_EVENT_AS_UPDATE);
        softAssertions.assertThat(mEventHashes.getLastVersionCode()).isEqualTo(newVersionCode);
        softAssertions.assertThat(mEventHashes.getHashesCountFromLastVersion()).isEqualTo(0);
        softAssertions.assertThat(mEventHashes.getEventNameHashes()).containsOnly(HASHES_OBJECTS_ARRAY);
        softAssertions.assertAll();
    }
}
