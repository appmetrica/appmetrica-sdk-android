package io.appmetrica.analytics.impl.crash.ndk.crashpad;

import android.os.Bundle;
import io.appmetrica.analytics.assertions.ObjectPropertyAssertions;
import io.appmetrica.analytics.testutils.CommonTest;
import org.assertj.core.api.Java6Assertions;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;

import static io.appmetrica.analytics.assertions.AssertionsKt.ObjectPropertyAssertions;
import static org.assertj.core.api.Java6Assertions.assertThat;

@RunWith(RobolectricTestRunner.class)
public class CrashpadCrashReportTest extends CommonTest {

    @Test
    public void emptyUuid() {
        Java6Assertions.assertThat(CrashpadCrashReport.fromBundle("", completeData())).isNull();
    }

    @Test
    public void nullUuid() {
        assertThat(CrashpadCrashReport.fromBundle("", completeData())).isNull();
    }

    @Test
    public void emptyBundle() {
        assertThat(CrashpadCrashReport.fromBundle("uuid", new Bundle())).isNull();
    }

    @Test
    public void withoutDumpFile() {
        Bundle data = new Bundle();
        data.putLong(CrashpadCrashReport.ARGUMENT_CREATION_TIME, 11L);
        assertThat(CrashpadCrashReport.fromBundle("uuid", data)).isNull();
    }

    @Test
    public void withoutCreationTime() {
        Bundle data = new Bundle();
        data.putString(CrashpadCrashReport.ARGUMENT_DUMP_FILE, "file");
        assertThat(CrashpadCrashReport.fromBundle("uuid", data)).isNull();
    }

    @Test
    public void emptyDumpFile() {
        Bundle data = new Bundle();
        data.putString(CrashpadCrashReport.ARGUMENT_DUMP_FILE, "");
        data.putLong(CrashpadCrashReport.ARGUMENT_CREATION_TIME, 11L);
        assertThat(CrashpadCrashReport.fromBundle("uuid", data)).isNull();
    }

    @Test
    public void nullDumpFile() {
        Bundle data = new Bundle();
        data.putString(CrashpadCrashReport.ARGUMENT_DUMP_FILE, null);
        data.putLong(CrashpadCrashReport.ARGUMENT_CREATION_TIME, 11L);
        assertThat(CrashpadCrashReport.fromBundle("uuid", data)).isNull();
    }

    @Test
    public void positiveScenario() throws IllegalAccessException {
        CrashpadCrashReport report = CrashpadCrashReport.fromBundle("uuid", completeData());

        ObjectPropertyAssertions<CrashpadCrashReport> assertions = ObjectPropertyAssertions(report);

        assertions.checkField("uuid", "uuid");
        assertions.checkField("dumpFile", "file");
        assertions.checkField("creationTime", 100500L);

        assertions.checkAll();

    }

    private Bundle completeData() {
        Bundle data = new Bundle();

        data.putString(CrashpadCrashReport.ARGUMENT_DUMP_FILE, "file");
        data.putLong(CrashpadCrashReport.ARGUMENT_CREATION_TIME, 100500L);

        return data;
    }
}
