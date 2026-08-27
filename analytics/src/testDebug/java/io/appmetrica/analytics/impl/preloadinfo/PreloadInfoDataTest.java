package io.appmetrica.analytics.impl.preloadinfo;

import io.appmetrica.gradle.testutils.CommonTest;
import io.appmetrica.gradle.testutils.assertions.Assertions;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import static org.assertj.core.api.Assertions.assertThat;

public class PreloadInfoDataTest extends CommonTest {

    @Mock
    private PreloadInfoState chosenState;
    @Mock
    private PreloadInfoData.Candidate firstCandidate;
    @Mock
    private PreloadInfoData.Candidate secondCandidate;

    @Before
    public void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    public void constructorFilled() throws Exception {
        List<PreloadInfoData.Candidate> candidates = Arrays.asList(firstCandidate, secondCandidate);
        PreloadInfoData data = new PreloadInfoData(chosenState, candidates);
        Assertions.INSTANCE.ObjectPropertyAssertions(data)
            .checkField("chosenPreloadInfo", chosenState)
            .checkField("candidates", candidates, true)
            .checkAll();
    }

    @Test
    public void constructorEmpty() throws Exception {
        PreloadInfoData data = new PreloadInfoData(chosenState, Collections.emptyList());
        Assertions.INSTANCE.ObjectPropertyAssertions(data)
            .checkField("chosenPreloadInfo", chosenState)
            .checkField("candidates", new ArrayList<PreloadInfoData>())
            .checkAll();
    }

    @Test
    public void getChosen() {
        PreloadInfoData data = new PreloadInfoData(chosenState, Collections.emptyList());
        assertThat(data.getChosen()).isSameAs(chosenState);
    }

    @Test
    public void getCandidates() {
        List<PreloadInfoData.Candidate> candidates = Arrays.asList(firstCandidate, secondCandidate);
        PreloadInfoData data = new PreloadInfoData(chosenState, candidates);
        assertThat(data.getCandidates()).isSameAs(candidates);
    }
}
