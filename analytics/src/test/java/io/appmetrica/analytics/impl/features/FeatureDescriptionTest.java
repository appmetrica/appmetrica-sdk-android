package io.appmetrica.analytics.impl.features;

import io.appmetrica.analytics.testutils.CommonTest;
import java.util.Random;
import java.util.UUID;
import org.json.JSONException;
import org.json.JSONObject;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;

import static org.assertj.core.api.Assertions.assertThat;

@RunWith(RobolectricTestRunner.class)
public class FeatureDescriptionTest extends CommonTest {

    public static final String NAME = "name";
    public static final String REQUIRED = "required";
    public static final String VERSION = "version";

    private final Random mRandom = new Random();

    private String mName;
    private boolean mRequired;
    private int mVersion;

    @Before
    public void setUp() {
        mName = UUID.randomUUID().toString();
        mRequired = mRandom.nextBoolean();
        mVersion = mRandom.nextInt(50) + 10;
    }

    @Test
    public void testCreationFromJSON() throws JSONException {
        FeatureDescription description = new FeatureDescription(new JSONObject()
            .put(NAME, mName)
            .put(REQUIRED, mRequired)
            .put(VERSION, mVersion)
        );

        assertThat(description.getName()).isEqualTo(mName);
        assertThat(description.isRequired()).isEqualTo(mRequired);
        assertThat(description.getVersion()).isEqualTo(mVersion);
    }

    @Test
    public void testConstructorWithVersion() {
        FeatureDescription description = new FeatureDescription(mName, mVersion, mRequired);

        assertThat(description.getName()).isEqualTo(mName);
        assertThat(description.isRequired()).isEqualTo(mRequired);
        assertThat(description.getVersion()).isEqualTo(mVersion);
    }

    @Test
    public void testConstructorWithoutVersion() {
        FeatureDescription description = new FeatureDescription(mName, mRequired);

        assertThat(description.getName()).isEqualTo(mName);
        assertThat(description.isRequired()).isEqualTo(mRequired);
        assertThat(description.getVersion()).isEqualTo(-1);
    }

    @Test
    public void testJSONWithVersion() throws JSONException {
        FeatureDescription description = new FeatureDescription(mName, mVersion, mRequired);
        JSONObject json = description.toJSON();
        assertThat(json.get(NAME)).isEqualTo(mName);
        assertThat(json.get(REQUIRED)).isEqualTo(mRequired);
        assertThat(json.get(VERSION)).isEqualTo(mVersion);
    }

    @Test
    public void testJSONWithoutVersion() throws JSONException {
        FeatureDescription description = new FeatureDescription(mName, mRequired);
        JSONObject json = description.toJSON();
        assertThat(json.get(NAME)).isEqualTo(mName);
        assertThat(json.get(REQUIRED)).isEqualTo(mRequired);
        assertThat(json.has(VERSION)).isFalse();
    }

    @Test
    public void testEquality() {
        FeatureDescription description1 = new FeatureDescription(mName, mVersion, mRequired);
        FeatureDescription description2 = new FeatureDescription(mName, mVersion, mRequired);
        assertThat(description1).isEqualTo(description2);
        assertThat(description1.hashCode()).isEqualTo(description2.hashCode());
    }

    @Test
    public void testDifferentFeatures() {
        FeatureDescription description1 = new FeatureDescription(mName, mVersion, mRequired);
        FeatureDescription description2 = new FeatureDescription(UUID.randomUUID().toString(), mVersion, mRequired);
        assertThat(description1).isNotEqualTo(description2);
    }

}
