package io.appmetrica.analytics.impl;

import androidx.annotation.NonNull;
import io.appmetrica.analytics.testutils.CommonTest;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;

import static org.assertj.core.api.Assertions.assertThat;

@RunWith(Parameterized.class)
public class EventSourceFromCodeTest extends CommonTest {

    @Parameterized.Parameters
    public static Collection<Object[]> data() {
        List<Object[]> data =  Arrays.asList(new Object[][]{
                {0, EventSource.NATIVE},
                {1, EventSource.JS},
                {2, EventSource.NATIVE}
        });
        assert data.size() == EventSource.values().length + 1;
        return data;
    }

    private final int input;
    @NonNull
    private final EventSource expected;

    public EventSourceFromCodeTest(int input, @NonNull EventSource expected) {
        this.input = input;
        this.expected = expected;
    }

    @Test
    public void eventSourceFromCode() {
        assertThat(EventSource.fromCode(input)).isEqualTo(expected);
    }
}
