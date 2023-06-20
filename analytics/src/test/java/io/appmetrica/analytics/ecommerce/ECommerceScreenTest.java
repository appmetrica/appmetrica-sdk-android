package io.appmetrica.analytics.ecommerce;

import io.appmetrica.analytics.assertions.ObjectPropertyAssertions;
import io.appmetrica.analytics.testutils.CommonTest;
import java.util.List;
import java.util.Map;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.robolectric.RobolectricTestRunner;

import static io.appmetrica.analytics.assertions.AssertionsKt.ObjectPropertyAssertions;
import static org.assertj.core.api.Assertions.assertThat;

@RunWith(RobolectricTestRunner.class)
public class ECommerceScreenTest extends CommonTest {

    private String name = "name";
    private String secondName = "secondName";
    @Mock
    private List<String> categoriesPath;
    @Mock
    private List<String> secondCategoriesPath;
    private String searchQuery = "search query";
    private String secondSearchQuery = "second search query";
    @Mock
    private Map<String, String> payload;
    @Mock
    private Map<String, String> secondPayload;

    private ECommerceScreen screen;

    @Before
    public void setUp() throws Exception {
        MockitoAnnotations.openMocks(this);

        screen = new ECommerceScreen();
    }

    @Test
    public void constructor() throws Exception {
        ObjectPropertyAssertions<ECommerceScreen> assertions = ObjectPropertyAssertions(screen)
                .withDeclaredAccessibleFields(true)
                .withFinalFieldOnly(false);

        assertions.checkField("name", "getName", null);
        assertions.checkField("categoriesPath", "getCategoriesPath", null);
        assertions.checkField("searchQuery", "getSearchQuery", null);
        assertions.checkField("payload", "getPayload", null);

        assertions.checkAll();
    }

    @Test
    public void setName() {
        screen.setName(name);
        assertThat(screen.getName()).isEqualTo(name);
    }

    @Test
    public void setNameTwice() {
        screen.setName(name);
        screen.setName(secondName);
        assertThat(screen.getName()).isEqualTo(secondName);
    }

    @Test
    public void setNameForNull() {
        screen.setName(null);
        assertThat(screen.getName()).isNull();
    }

    @Test
    public void setNullNameAfterNonNull() {
        screen.setName(name);
        screen.setName(null);
        assertThat(screen.getName()).isNull();
    }

    @Test
    public void setCategoriesPath() {
        screen.setCategoriesPath(categoriesPath);
        assertThat(screen.getCategoriesPath()).isEqualTo(categoriesPath);
    }

    @Test
    public void setCategoriesPathTwice() {
        screen.setCategoriesPath(categoriesPath);
        screen.setCategoriesPath(secondCategoriesPath);
        assertThat(screen.getCategoriesPath()).isEqualTo(secondCategoriesPath);
    }

    @Test
    public void setCategoriesPathForNull() {
        screen.setCategoriesPath(null);
        assertThat(screen.getCategoriesPath()).isNull();
    }

    @Test
    public void setNullCategoriesPathAfterNonNull() {
        screen.setCategoriesPath(categoriesPath);
        screen.setCategoriesPath(null);
        assertThat(screen.getCategoriesPath()).isNull();
    }

    @Test
    public void setSearchQuery() {
        screen.setSearchQuery(searchQuery);
        assertThat(screen.getSearchQuery()).isEqualTo(searchQuery);
    }

    @Test
    public void setSearchQueryTwice() {
        screen.setSearchQuery(searchQuery);
        screen.setSearchQuery(secondSearchQuery);
        assertThat(screen.getSearchQuery()).isEqualTo(secondSearchQuery);
    }

    @Test
    public void setSearchQueryForNull() {
        screen.setSearchQuery(null);
        assertThat(screen.getSearchQuery()).isNull();
    }

    @Test
    public void setNullSearchQueryAfterNonNull() {
        screen.setSearchQuery(searchQuery);
        screen.setSearchQuery(null);
        assertThat(screen.getSearchQuery()).isNull();
    }

    @Test
    public void setPayload() {
        screen.setPayload(payload);
        assertThat(screen.getPayload()).isEqualTo(payload);
    }

    @Test
    public void setPayloadTwice() {
        screen.setPayload(payload);
        screen.setPayload(secondPayload);
        assertThat(screen.getPayload()).isEqualTo(secondPayload);
    }

    @Test
    public void setPayloadForNull() {
        screen.setPayload(null);
        assertThat(screen.getPayload()).isNull();
    }

    @Test
    public void setNullPayloadAfterNonNull() {
        screen.setPayload(payload);
        screen.setPayload(null);
        assertThat(screen.getPayload()).isNull();
    }
}
