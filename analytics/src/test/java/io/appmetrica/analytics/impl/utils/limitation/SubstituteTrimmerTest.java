package io.appmetrica.analytics.impl.utils.limitation;

import androidx.annotation.Nullable;
import io.appmetrica.analytics.testutils.CommonTest;
import org.junit.Test;

import static org.assertj.core.api.Assertions.assertThat;

public class SubstituteTrimmerTest extends CommonTest {

    @Test
    public void testNotTrimmed() {
        Object object = new Object();
        assertThat(new SubstituteTrimmer<Object>(new Trimmer<Object>() {
            @Nullable
            @Override
            public Object trim(@Nullable Object data) {
                return data;
            }
        }, null).trim(object)).isSameAs(object);
    }

    @Test
    public void testTrimmed() {
        Object substitute = new Object();
        assertThat(new SubstituteTrimmer<Object>(
            new Trimmer<Object>() {
                @Nullable
                @Override
                public Object trim(@Nullable Object data) {
                    return new Object();
                }
            }, substitute
        ).trim(new Object())).isSameAs(substitute);
    }

}
