package io.appmetrica.analytics.impl.component;

import io.appmetrica.analytics.testutils.CommonTest;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class ReporterArgumentsHolderTest extends CommonTest {

    @Mock
    private CommonArguments.ReporterArguments mArguments;
    private ReporterArgumentsHolder mReporterArgumentsHolder;

    @Before
    public void setUp() {
        MockitoAnnotations.openMocks(this);
        mReporterArgumentsHolder = new ReporterArgumentsHolder(mArguments);
    }

    @Test
    public void testGetArguments() {
        assertThat(mReporterArgumentsHolder.getArguments()).isEqualTo(mArguments);
    }

    @Test
    public void testUpdateArguments() {
        CommonArguments.ReporterArguments other = mock(CommonArguments.ReporterArguments.class);
        CommonArguments.ReporterArguments newArguments = mock(CommonArguments.ReporterArguments.class);
        when(mArguments.mergeFrom(other)).thenReturn(newArguments);
        mReporterArgumentsHolder.updateArguments(other);
        assertThat(mReporterArgumentsHolder.getArguments()).isEqualTo(newArguments);
    }
}
