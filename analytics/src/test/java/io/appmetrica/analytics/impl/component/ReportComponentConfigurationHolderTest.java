package io.appmetrica.analytics.impl.component;

import android.content.ContentValues;
import androidx.annotation.NonNull;
import io.appmetrica.analytics.impl.DataSendingRestrictionControllerImpl;
import io.appmetrica.analytics.impl.client.ClientConfiguration;
import io.appmetrica.analytics.impl.client.ClientConfigurationTestUtils;
import io.appmetrica.analytics.impl.request.CoreRequestConfig;
import io.appmetrica.analytics.impl.request.ReportRequestConfig;
import io.appmetrica.analytics.impl.startup.StartupState;
import io.appmetrica.analytics.testutils.CommonTest;
import io.appmetrica.analytics.testutils.ContextRule;
import io.appmetrica.analytics.testutils.GlobalServiceLocatorRule;
import io.appmetrica.analytics.testutils.MockProvider;
import io.appmetrica.analytics.testutils.MockedConstructionRule;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;

public class ReportComponentConfigurationHolderTest extends CommonTest {

    @Rule
    public ContextRule contextRule = new ContextRule();

    private ReportComponentConfigurationHolder mHolder;

    @Rule
    public GlobalServiceLocatorRule mRule = new GlobalServiceLocatorRule();

    private final Map<ContentValues, HashMap<String, Object>> contentValuesDataMaps = new HashMap<>();

    @Rule
    public MockedConstructionRule<ContentValues> contentValuesMockedConstructionRule = new MockedConstructionRule<>(
        ContentValues.class,
        (mock, context) -> {
            HashMap<String, Object> dataMap = new HashMap<>();
            contentValuesDataMaps.put(mock, dataMap);
            MockProvider.stubContentValues(mock, dataMap);
        }
    );

    @Before
    public void setUp() {
        ComponentUnit componentUnit = mock(ComponentUnit.class);
        doReturn(new ComponentId(
            contextRule.getContext().getPackageName(),
            UUID.randomUUID().toString())
        ).when(componentUnit).getComponentId();
        mHolder = new ReportComponentConfigurationHolder(
            new ReportRequestConfig.Loader(componentUnit,
                new ReportRequestConfig.BaseDataSendingStrategy(
                    new DataSendingRestrictionControllerImpl(mock(DataSendingRestrictionControllerImpl.Storage.class))
                ) {
                }
            ) {
                @NonNull
                @Override
                public ReportRequestConfig load(@NonNull CoreRequestConfig.CoreDataSource<ReportRequestConfig.Arguments> dataSource) {
                    return mock(ReportRequestConfig.class);
                }
            },
            mock(StartupState.class),
            ReportRequestConfig.Arguments.empty()
        );
    }

    @Test
    public void testDoNotUpdateArgumentsIfConfigurationNotChanged() {
        ReportRequestConfig requestConfig = mHolder.get();
        StartupState startupState = mHolder.getStartupState();
        ReportRequestConfig.Arguments arguments = mHolder.getArguments();
        mHolder.updateArguments(CommonArgumentsTestUtils.emptyReporterArguments());

        assertThat(mHolder.get()).isSameAs(requestConfig);
        assertThat(mHolder.getStartupState()).isSameAs(startupState);
        assertThat(mHolder.getArguments()).isSameAs(arguments);
    }

    @Test
    public void testUpdateArgumentsIfConfigurationChanged() {
        ReportRequestConfig requestConfig = mHolder.get();
        ClientConfiguration newConfiguration =
            ClientConfigurationTestUtils.createStubbedConfiguration(contextRule.getContext(), null);
        StartupState startupState = mHolder.getStartupState();
        ReportRequestConfig.Arguments arguments = mHolder.getArguments();
        newConfiguration.getReporterConfiguration().setApiKey(UUID.randomUUID().toString());
        newConfiguration.getReporterConfiguration().setCustomAppVersion(
            newConfiguration.getReporterConfiguration().getAppVersion() + "_new"
        );

        mHolder.updateArguments(
            new CommonArguments.ReporterArguments(newConfiguration.getReporterConfiguration(), null)
        );

        assertThat(mHolder.get()).isNotSameAs(requestConfig);
        assertThat(mHolder.getStartupState()).isSameAs(startupState);
        assertThat(mHolder.getArguments()).isNotSameAs(arguments);
    }

}
