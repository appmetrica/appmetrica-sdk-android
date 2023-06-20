package io.appmetrica.analytics.impl.profile;

import android.util.SparseArray;
import io.appmetrica.analytics.impl.protobuf.backend.Userprofile;
import io.appmetrica.analytics.testutils.CommonTest;
import java.util.ArrayList;
import java.util.List;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.tuple;

@RunWith(RobolectricTestRunner.class)
public class UserProfileStorageTest extends CommonTest {

    private static final int ATTRIBUTE_ONE = 1;
    private static final int ATTRIBUTE_TWO = 2;

    UserProfileStorage mStorage = new UserProfileStorage(new int[]{ATTRIBUTE_ONE, ATTRIBUTE_TWO});

    @Test
    public void testPutAndRetrieve() {
        Userprofile.Profile.Attribute attribute = CommonUserProfileUpdatePatcherTest.createEmptyAttribute();
        attribute.name = "key".getBytes();
        attribute.type = ATTRIBUTE_ONE;
        mStorage.put(attribute);
        assertThat(mStorage.get(ATTRIBUTE_ONE, "key")).isSameAs(attribute);
    }

    @Test
    public void testSetForOneNamespace() {
        Userprofile.Profile.Attribute attribute = CommonUserProfileUpdatePatcherTest.createEmptyAttribute();
        attribute.name = "key".getBytes();
        attribute.type = ATTRIBUTE_ONE;
        mStorage.put(attribute);
        assertThat(mStorage.get(ATTRIBUTE_TWO, "key")).isNull();
    }

    @Test
    public void testSetSameKeyForDifferentNamespaces() {
        Userprofile.Profile.Attribute attribute = CommonUserProfileUpdatePatcherTest.createEmptyAttribute();
        attribute.name = "key".getBytes();
        attribute.type = ATTRIBUTE_ONE;
        mStorage.put(attribute);
        Userprofile.Profile.Attribute attribute2 = CommonUserProfileUpdatePatcherTest.createEmptyAttribute();
        attribute2.name = "key".getBytes();
        attribute2.type = ATTRIBUTE_TWO;
        mStorage.put(attribute2);

        assertThat(mStorage.get(ATTRIBUTE_ONE, "key")).isSameAs(attribute);
        assertThat(mStorage.get(ATTRIBUTE_TWO, "key")).isSameAs(attribute2);
    }

    @Test
    public void testToProtobuf() {
        Userprofile.Profile.Attribute attribute = CommonUserProfileUpdatePatcherTest.createEmptyAttribute();
        attribute.name = "firstKey".getBytes();
        attribute.type = ATTRIBUTE_ONE;
        mStorage.put(attribute);
        attribute = CommonUserProfileUpdatePatcherTest.createEmptyAttribute();
        attribute.name = "secondKey".getBytes();
        attribute.type = ATTRIBUTE_TWO;
        mStorage.put(attribute);

        Userprofile.Profile profile = mStorage.toProtobuf();
        assertThat(profile.attributes).extracting("name", "type").containsExactly(
                tuple("firstKey".getBytes(), ATTRIBUTE_ONE),
                tuple("secondKey".getBytes(), ATTRIBUTE_TWO)
        );
    }

    @Test
    public void testChangeAttributeTwice() {
        final String KEY = "key";
        Userprofile.Profile.Attribute attribute = CommonUserProfileUpdatePatcherTest.createEmptyAttribute();
        attribute.type = ATTRIBUTE_ONE;
        attribute.name = KEY.getBytes();
        attribute.value.stringValue = "value1".getBytes();
        mStorage.put(attribute);

        Userprofile.Profile.Attribute attribute2 = CommonUserProfileUpdatePatcherTest.createEmptyAttribute();
        attribute.type = ATTRIBUTE_ONE;
        attribute.name = KEY.getBytes();
        attribute.value.stringValue = "value2".getBytes();

        assertThat(mStorage.get(ATTRIBUTE_ONE, KEY).value.stringValue).isEqualTo("value2".getBytes());
    }

    @Test
    public void testDefaulNamespaces() {
        List<Integer> keys = new ArrayList<Integer>();
        SparseArray<?> namespaces = new UserProfileStorage().getAttributesNamespaces();
        for (int i = 0; i < namespaces.size(); i++) {
            keys.add(namespaces.keyAt(i));
        }
        assertThat(keys).containsOnly(
                Userprofile.Profile.Attribute.STRING,
                Userprofile.Profile.Attribute.NUMBER,
                Userprofile.Profile.Attribute.COUNTER,
                Userprofile.Profile.Attribute.BOOL
        );
    }

}
