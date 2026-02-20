package io.appmetrica.analytics.ecommerce;

import io.appmetrica.analytics.assertions.ObjectPropertyAssertions;
import io.appmetrica.analytics.testutils.CommonTest;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import static io.appmetrica.analytics.assertions.AssertionsKt.ObjectPropertyAssertions;
import static org.assertj.core.api.Assertions.assertThat;

public class ECommerceReferrerTest extends CommonTest {

    private String type = "type";
    private String secondType = "seconType";
    private String identifier = "identifier";
    private String secondIdentifier = "secondIdentifier";
    @Mock
    private ECommerceScreen screen;
    @Mock
    private ECommerceScreen secondScreen;

    private ECommerceReferrer referrer;

    @Before
    public void setUp() throws Exception {
        MockitoAnnotations.openMocks(this);

        referrer = new ECommerceReferrer();
    }

    @Test
    public void constructor() throws Exception {
        ObjectPropertyAssertions<ECommerceReferrer> assertions =
                ObjectPropertyAssertions(referrer)
                .withDeclaredAccessibleFields(true)
                .withFinalFieldOnly(false);

        assertions.checkField("type", "getType", null);
        assertions.checkField("identifier", "getIdentifier", null);
        assertions.checkField("screen", "getScreen", null);

        assertions.checkAll();
    }

    @Test
    public void setType() {
        referrer.setType(type);
        assertThat(referrer.getType()).isEqualTo(type);
    }

    @Test
    public void setTypeTwice() {
        referrer.setType(type);
        referrer.setType(secondType);
        assertThat(referrer.getType()).isEqualTo(secondType);
    }

    @Test
    public void setTypeForNull() {
        referrer.setType(null);
        assertThat(referrer.getType()).isNull();
    }

    @Test
    public void setNullTypeAfterNonNull() {
        referrer.setType(type);
        referrer.setType(null);
        assertThat(referrer.getType()).isNull();
    }

    @Test
    public void setIdentifier() {
        referrer.setIdentifier(identifier);
        assertThat(referrer.getIdentifier()).isEqualTo(identifier);
    }

    @Test
    public void setIdentifierTwice() {
        referrer.setIdentifier(identifier);
        referrer.setIdentifier(secondIdentifier);
        assertThat(referrer.getIdentifier()).isEqualTo(secondIdentifier);
    }

    @Test
    public void setIdentifierForNull() {
        referrer.setIdentifier(null);
        assertThat(referrer.getIdentifier()).isNull();
    }

    @Test
    public void setNullIdentifierAfterNonNull() {
        referrer.setIdentifier(identifier);
        referrer.setIdentifier(null);
        assertThat(referrer.getIdentifier()).isNull();
    }

    @Test
    public void setScreen() {
        referrer.setScreen(screen);
        assertThat(referrer.getScreen()).isEqualTo(screen);
    }

    @Test
    public void setScreenTwice() {
        referrer.setScreen(screen);
        referrer.setScreen(secondScreen);
        assertThat(referrer.getScreen()).isEqualTo(secondScreen);
    }

    @Test
    public void setScreenForNull() {
        referrer.setScreen(null);
        assertThat(referrer.getScreen()).isNull();
    }

    @Test
    public void setNullScreenAfterNonNull() {
        referrer.setScreen(screen);
        referrer.setScreen(null);
        assertThat(referrer.getScreen()).isNull();
    }
}
