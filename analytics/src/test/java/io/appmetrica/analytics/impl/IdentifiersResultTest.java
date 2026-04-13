package io.appmetrica.analytics.impl;

import android.os.Parcel;
import io.appmetrica.analytics.coreapi.internal.identifiers.IdentifierStatus;
import io.appmetrica.analytics.internal.IdentifiersResult;
import io.appmetrica.gradle.testutils.CommonTest;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;

import io.appmetrica.gradle.testutils.assertions.Assertions;
import io.appmetrica.gradle.testutils.assertions.ObjectPropertyAssertions;

@RunWith(RobolectricTestRunner.class)
public class IdentifiersResultTest extends CommonTest {

    @Test
    public void testConstructor() throws Exception {
        final String id = "some id";
        IdentifierStatus status = IdentifierStatus.OK;
        final String error = "error";
        IdentifiersResult identifiersResult = new IdentifiersResult(id, status, error);
        ObjectPropertyAssertions<IdentifiersResult> assertions = Assertions.INSTANCE.ObjectPropertyAssertions(identifiersResult);
        assertions.checkField("id", id);
        assertions.checkField("status", status);
        assertions.checkField("errorExplanation", error);
        assertions.checkAll();
    }

    @Test
    public void testParcel() throws Exception {
        Parcel parcel = Parcel.obtain();
        final String id = "some id";
        final String error = "error";
        IdentifierStatus status = IdentifierStatus.OK;
        IdentifiersResult identifiersResult = new IdentifiersResult(id, status, error);
        identifiersResult.writeToParcel(parcel, 0);

        parcel.setDataPosition(0);
        IdentifiersResult fromParcel = IdentifiersResult.CREATOR.createFromParcel(parcel);
        ObjectPropertyAssertions<IdentifiersResult> assertions = Assertions.INSTANCE.ObjectPropertyAssertions(fromParcel);
        assertions.checkField("id", id);
        assertions.checkField("status", status);
        assertions.checkField("errorExplanation", error);
        assertions.checkAll();
    }
}
