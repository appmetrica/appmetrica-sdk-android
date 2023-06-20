package io.appmetrica.analytics.impl.component;

import io.appmetrica.analytics.impl.db.VitalComponentDataProvider;
import io.appmetrica.analytics.impl.db.preferences.EventNumberOfTypeItemsHolder;
import io.appmetrica.analytics.testutils.CommonTest;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class EventNumberGeneratorTest extends CommonTest {

    @Mock
    private VitalComponentDataProvider vitalComponentDataProvider;
    @Mock
    private EventNumberOfTypeItemsHolder eventNumberOfTypeItemsHolder;
    private EventNumberGenerator mEventNumberGenerator;

    @Before
    public void setUp() {
        MockitoAnnotations.openMocks(this);
        mEventNumberGenerator = new EventNumberGenerator(vitalComponentDataProvider, eventNumberOfTypeItemsHolder);
    }

    @Test
    public void testGenerateGlobalNumber() {
        final long number = 5L;
        when(vitalComponentDataProvider.getGlobalNumber()).thenReturn(number);
        assertThat(mEventNumberGenerator.getEventGlobalNumberAndGenerateNext()).isEqualTo(number);
        verify(vitalComponentDataProvider).setGlobalNumber(number + 1);
    }

    @Test
    public void testGenerateNumberOfType() {
        final int type = 7;
        final long number = 5L;
        when(eventNumberOfTypeItemsHolder.getNumberOfType(type)).thenReturn(number);
        assertThat(mEventNumberGenerator.getEventNumberOfTypeAndGenerateNext(type)).isEqualTo(number);
        verify(eventNumberOfTypeItemsHolder).putNumberOfType(type, number + 1);
    }
}
