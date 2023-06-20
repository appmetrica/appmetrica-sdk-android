package io.appmetrica.analytics.impl.db;

import io.appmetrica.analytics.impl.InternalEvents;
import java.util.Arrays;
import java.util.Collection;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.ParameterizedRobolectricTestRunner;

import static org.assertj.core.api.Assertions.assertThat;

@RunWith(ParameterizedRobolectricTestRunner.class)
public class DbCleanupImportantEventsAreRemovedTest extends DbCleanupBaseTest {

    @ParameterizedRobolectricTestRunner.Parameters(name = "Type {0}")
    public static Collection<Object[]> data() {
        return Arrays.asList(
                new Object[]{InternalEvents.EVENT_TYPE_INIT.getTypeId()},
                new Object[]{InternalEvents.EVENT_TYPE_FIRST_ACTIVATION.getTypeId()},
                new Object[]{InternalEvents.EVENT_TYPE_SEND_REFERRER.getTypeId()},
                new Object[]{InternalEvents.EVENT_TYPE_APP_UPDATE.getTypeId()},
                new Object[]{InternalEvents.EVENT_TYPE_CLEANUP.getTypeId()}
        );
    }

    private final int mType;
    private final int mMaxReportsCount = 100;
    private final int mExpectedLeftReports = 91;

    public DbCleanupImportantEventsAreRemovedTest(int type) {
        mType = type;
    }

    @Test
    public void testImportantEventIsRemoved() {
        addExcessiveEvents(mMaxReportsCount + 1, mType);
        mHelper.clearIfTooManyEvents();
        assertThat(execForSingleInt("select count() from events")).isEqualTo(mExpectedLeftReports);
    }

    @Override
    long getMaxEventsCount() {
        return mMaxReportsCount;
    }
}
