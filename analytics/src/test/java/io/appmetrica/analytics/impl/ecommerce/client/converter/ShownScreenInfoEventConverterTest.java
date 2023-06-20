package io.appmetrica.analytics.impl.ecommerce.client.converter;

import io.appmetrica.analytics.impl.ecommerce.client.model.ScreenWrapper;
import io.appmetrica.analytics.impl.ecommerce.client.model.ShownScreenInfoEvent;
import io.appmetrica.analytics.impl.protobuf.backend.Ecommerce;
import io.appmetrica.analytics.impl.utils.limitation.BytesTruncatedInfo;
import io.appmetrica.analytics.impl.utils.limitation.BytesTruncatedProvider;
import io.appmetrica.analytics.testutils.CommonTest;
import io.appmetrica.analytics.testutils.TruncationInfoConsumer;
import java.util.Collections;
import java.util.List;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.robolectric.RobolectricTestRunner;

import static io.appmetrica.analytics.assertions.AssertionsKt.ObjectPropertyAssertions;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

@RunWith(RobolectricTestRunner.class)
public class ShownScreenInfoEventConverterTest extends CommonTest {

    @Mock
    private ScreenConverter screenConverter;
    @Mock
    private ScreenWrapper screen;
    @Mock
    private Ecommerce.ECommerceEvent.Screen screenProto;

    private ShownScreenInfoEventConverter shownScreenInfoEventConverter;

    private final int screenBytesTruncated = 1;
    private final int totalBytesTruncated = screenBytesTruncated;

    @Before
    public void setUp() throws Exception {
        MockitoAnnotations.openMocks(this);

        when(screenConverter.fromModel(screen))
                .thenReturn(new Result<Ecommerce.ECommerceEvent.Screen, BytesTruncatedProvider>(
                        screenProto,
                        new BytesTruncatedInfo(screenBytesTruncated)
                ));

        shownScreenInfoEventConverter = new ShownScreenInfoEventConverter(screenConverter);
    }

    @Test
    public void constructor() throws Exception {
        ObjectPropertyAssertions(new ShownScreenInfoEventConverter())
                .withPrivateFields(true)
                .checkFieldNonNull("screenConverter")
                .checkAll();
    }

    @Test
    public void toProto() throws Exception {
        ShownScreenInfoEvent event = new ShownScreenInfoEvent(screen, shownScreenInfoEventConverter);

        List<Result<Ecommerce.ECommerceEvent, BytesTruncatedProvider>> protos = shownScreenInfoEventConverter.fromModel(event);

        Ecommerce.ECommerceEvent.ShownScreenInfo expectedShownScreenInfo =
                new Ecommerce.ECommerceEvent.ShownScreenInfo();
        expectedShownScreenInfo.screen = screenProto;

        assertThat(protos.size()).isEqualTo(1);

        ObjectPropertyAssertions(protos.get(0))
                .checkFieldRecursively(
                        "metaInfo",
                        new TruncationInfoConsumer(totalBytesTruncated)
                )
                .checkFieldRecursively(
                        "result",
                        new ECommerceEventAssertionsConsumer(Ecommerce.ECommerceEvent.ECOMMERCE_EVENT_TYPE_SHOW_SCREEN)
                                .setExpectedShowScreenInfo(expectedShownScreenInfo)
                )
                .checkAll();
    }

    @Test(expected = UnsupportedOperationException.class)
    public void toModel() {
        shownScreenInfoEventConverter.toModel(
                Collections.<Result<Ecommerce.ECommerceEvent, BytesTruncatedProvider>>emptyList()
        );
    }
}
