package io.appmetrica.analytics.impl.preparer;

import io.appmetrica.analytics.impl.InternalEvents;
import io.appmetrica.analytics.testutils.CommonTest;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import org.junit.Test;

import static org.assertj.core.api.Assertions.assertThat;

public class EventToPreparerGeneralTestCoverageCheck extends CommonTest {

    private final InternalEvents[] ignoredEventTypes = new InternalEvents[]{
        InternalEvents.EVENT_TYPE_SEND_REFERRER, //Tested in EventToPreparerSpecialCasesTest
        InternalEvents.EVENT_TYPE_CUSTOM_EVENT //Tested in EventToPreparerSpecialCasesTest
    };

    @Test
    public void checkCoverage() {
        List<InternalEvents> internalEvents = Arrays.asList(InternalEvents.values());

        List<InternalEvents> coveredEventTypes =
            new ArrayList<InternalEvents>(Arrays.asList(ignoredEventTypes));

        Collection<Object[]> testData = EventToPreparerGeneralTest.data();
        for (Object[] testItem : testData) {
            if (testItem[0] != null) {
                coveredEventTypes.add(((InternalEvents) testItem[0]));
            }
        }

        assertThat(internalEvents)
            .as("Internal events exclude ignored. " +
                "If unexpected event types were detected, add new event type to EventToPreparerGeneralTest or" +
                "ignored list. If couldn't find some event types, delete it from ignoredEventTypes.")
            .isSubsetOf(coveredEventTypes);
    }
}
