package io.appmetrica.analytics.impl.db;

import io.appmetrica.analytics.impl.EventsManager;
import io.appmetrica.analytics.impl.InternalEvents;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.ParameterizedRobolectricTestRunner;

import static org.assertj.core.api.Assertions.assertThat;

@RunWith(ParameterizedRobolectricTestRunner.class)
public class DbCleanupFirstHighestPriorityEventsAreNotRemovedTest extends DbCleanupBaseTest {

    private final int unimportantType;
    private final int maxEventsCount = 100;
    private final int expectedLeftEvents = 91;

    @ParameterizedRobolectricTestRunner.Parameters(name = "Type {0} is not of first highest priority")
    public static Collection<Object[]> data() {
        final List<Object[]> unimportantTypes = new ArrayList<Object[]>();
        for (InternalEvents event : InternalEvents.values()) {
            if (!EventsManager.EVENTS_WITH_FIRST_HIGHEST_PRIORITY.contains(event.getTypeId())) {
                unimportantTypes.add(new Object[]{event.getTypeId()});
            }
        }
        return unimportantTypes;
    }

    public DbCleanupFirstHighestPriorityEventsAreNotRemovedTest(final int unimportantType) throws Exception {
        this.unimportantType = unimportantType;
    }

    @Test
    public void testImportantEventsAreNotRemovedWhenHasOthers() {
        for (int importantType : EventsManager.EVENTS_WITH_FIRST_HIGHEST_PRIORITY) {
            addExcessiveEvents(1, importantType);
        }
        addExcessiveEvents(maxEventsCount + 1 - EventsManager.EVENTS_WITH_FIRST_HIGHEST_PRIORITY.size(), unimportantType);

        mHelper.clearIfTooManyEvents();
        assertThat(selectTypes()).containsAnyElementsOf(EventsManager.EVENTS_WITH_FIRST_HIGHEST_PRIORITY);
        assertThat(execForSingleInt("select count() from events")).isEqualTo(expectedLeftEvents);
    }

    @Override
    long getMaxEventsCount() {
        return maxEventsCount;
    }
}
