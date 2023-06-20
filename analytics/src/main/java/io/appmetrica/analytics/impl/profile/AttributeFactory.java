package io.appmetrica.analytics.impl.profile;

import io.appmetrica.analytics.impl.protobuf.backend.Userprofile;

public interface AttributeFactory {

    Userprofile.Profile.Attribute createAttribute();

}
