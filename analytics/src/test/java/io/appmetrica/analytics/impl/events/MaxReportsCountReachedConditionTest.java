package io.appmetrica.analytics.impl.events;

import io.appmetrica.analytics.impl.component.ReportComponentConfigurationHolder;
import io.appmetrica.analytics.impl.db.DatabaseHelper;
import io.appmetrica.analytics.impl.request.ReportRequestConfig;
import io.appmetrica.analytics.testutils.CommonTest;
import java.util.Arrays;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class MaxReportsCountReachedConditionTest extends CommonTest {

    @Mock
    private DatabaseHelper mDatabaseHelper;
    @Mock
    private ReportComponentConfigurationHolder mConfigHolder;
    @Mock
    private ReportRequestConfig mConfig;
    private final int mType = 0;
    private MaxReportsCountReachedCondition mMaxReportsCountReachedCondition;

    @Before
    public void setUp() {
        MockitoAnnotations.openMocks(this);
        when(mConfigHolder.get()).thenReturn(mConfig);
        when(mConfig.getMaxReportsCount()).thenReturn(2);
        mMaxReportsCountReachedCondition = new MaxReportsCountReachedCondition(mDatabaseHelper, mConfigHolder);
    }

    @Test
    public void testListenerAdded() {
        verify(mDatabaseHelper).addEventListener(mMaxReportsCountReachedCondition);
    }

    @Test
    public void testReportsCountGreaterThanMax() {
        mMaxReportsCountReachedCondition.onEventsAdded(Arrays.asList(mType, mType, mType));
        assertThat(mMaxReportsCountReachedCondition.isConditionMet()).isTrue();
    }

    @Test
    public void testReportsCountEqualToMax() {
        mMaxReportsCountReachedCondition.onEventsAdded(Arrays.asList(mType, mType));
        assertThat(mMaxReportsCountReachedCondition.isConditionMet()).isTrue();
    }

    @Test
    public void testReportsCountLessThanMax() {
        mMaxReportsCountReachedCondition.onEventsAdded(Arrays.asList(mType));
        assertThat(mMaxReportsCountReachedCondition.isConditionMet()).isFalse();
    }

    @Test
    public void testFreshConfigIsApplied() {
        ReportRequestConfig config = mock(ReportRequestConfig.class);
        when(mConfigHolder.get()).thenReturn(config);
        when(config.getMaxReportsCount()).thenReturn(3);
        mMaxReportsCountReachedCondition.onEventsAdded(Arrays.asList(mType, mType));
        assertThat(mMaxReportsCountReachedCondition.isConditionMet()).isFalse();
    }

    @Test
    public void testStateIsReadFromDb() {
        when(mDatabaseHelper.getEventsCount()).thenReturn(2L);
        when(mConfig.getMaxReportsCount()).thenReturn(2);
        assertThat(new MaxReportsCountReachedCondition(mDatabaseHelper, mConfigHolder).isConditionMet()).isTrue();
    }

    @Test
    public void testEventsRemoved() {
        mMaxReportsCountReachedCondition.onEventsAdded(Arrays.asList(mType, mType, mType));
        assertThat(mMaxReportsCountReachedCondition.isConditionMet()).isTrue();
        mMaxReportsCountReachedCondition.onEventsRemoved(Arrays.asList(mType, mType));
        assertThat(mMaxReportsCountReachedCondition.isConditionMet()).isFalse();
    }
}
