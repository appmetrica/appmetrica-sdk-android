package io.appmetrica.analytics.impl.clids;

import io.appmetrica.analytics.impl.DistributionSource;
import io.appmetrica.analytics.testutils.CommonTest;
import java.util.List;
import org.assertj.core.api.SoftAssertions;
import org.junit.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;

public class ClidsInfoTest extends CommonTest {

    @Test
    public void distributionInfoImplementation() {
        final ClidsInfo.Candidate chosen = mock(ClidsInfo.Candidate.class);
        final List<ClidsInfo.Candidate> candidates = mock(List.class);
        final ClidsInfo clidsInfo = new ClidsInfo(chosen, candidates);
        final SoftAssertions softly = new SoftAssertions();
        softly.assertThat(clidsInfo.getChosen()).isSameAs(chosen);
        softly.assertThat(clidsInfo.getCandidates()).isSameAs(candidates);
        softly.assertAll();
    }

    @Test
    public void candidateDistributionSourceProvider() {
        final ClidsInfo.Candidate candidate = new ClidsInfo.Candidate(null, DistributionSource.SATELLITE);
        assertThat(candidate.getSource()).isEqualTo(DistributionSource.SATELLITE);
    }
}
