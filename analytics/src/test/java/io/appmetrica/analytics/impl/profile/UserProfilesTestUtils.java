package io.appmetrica.analytics.impl.profile;

import io.appmetrica.analytics.impl.protobuf.backend.Userprofile;
import io.appmetrica.analytics.testutils.CommonTest;

class UserProfilesTestUtils extends CommonTest {

    public static Userprofile.Profile.Attribute createEmpty() {
        Userprofile.Profile.Attribute attribute = new Userprofile.Profile.Attribute();
        attribute.metaInfo = new Userprofile.Profile.AttributeMetaInfo();
        return attribute;
    }
}
