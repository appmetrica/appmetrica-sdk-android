package io.appmetrica.analytics.impl;

import io.appmetrica.analytics.testutils.CommonTest;
import java.util.ArrayList;
import java.util.Collection;
import org.assertj.core.api.SoftAssertions;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.ParameterizedRobolectricTestRunner;

@RunWith(ParameterizedRobolectricTestRunner.class)
public class StatisticsRestrictionControllerImplTest extends CommonTest {

    private static class EmptyStorage implements StatisticsRestrictionControllerImpl.Storage {

        @Override
        public void storeRestrictionFromMainReporter(boolean value) {

        }

        @Override
        public Boolean readRestrictionFromMainReporter() {
            return null;
        }
    }

    @ParameterizedRobolectricTestRunner.Parameters(name = "{0}")
    public static Collection<Object[]> data() {
        ArrayList<Object[]> data = new ArrayList<Object[]>();
        StatisticsRestrictionControllerImpl controller;

        controller = new StatisticsRestrictionControllerImpl(new EmptyStorage());
        controller.setEnabledFromMainReporter(true);
        data.add(new Object[]{"enabled in main reporter", controller, false, false, false});

        controller = new StatisticsRestrictionControllerImpl(new EmptyStorage());
        controller.setEnabledFromMainReporter(false);
        data.add(new Object[]{"disabled in main reporter", controller, true, true, true});

        controller = new StatisticsRestrictionControllerImpl(new EmptyStorage());
        controller.setEnabledFromSharedReporter("1", true);
        data.add(new Object[]{"enabled in all reporters", controller, false, false, false});

        controller = new StatisticsRestrictionControllerImpl(new EmptyStorage());
        controller.setEnabledFromSharedReporter("1", false);
        data.add(new Object[]{"disabled in all reporters", controller, false, true, true});

        controller = new StatisticsRestrictionControllerImpl(new EmptyStorage());
        controller.setEnabledFromSharedReporter("1", false);
        controller.setEnabledFromSharedReporter("2", true);
        data.add(new Object[]{"different in all reporters", controller, false, false, true});

        controller = new StatisticsRestrictionControllerImpl(new EmptyStorage());
        data.add(new Object[]{"test no data", controller, true, true, true});

        return data;
    }

    private final StatisticsRestrictionControllerImpl mController;
    private final boolean mReporter;
    private final boolean mAppMetrica;
    private final boolean mLocation;

    public StatisticsRestrictionControllerImplTest(String description,
                                                   StatisticsRestrictionControllerImpl controller,
                                                   boolean reporter,
                                                   boolean appMetrica,
                                                   boolean location) {
        mController = controller;
        mReporter = reporter;
        mAppMetrica = appMetrica;
        mLocation = location;
    }

    @Test
    public void test() {
        SoftAssertions assertions = new SoftAssertions();
        assertions.assertThat(mController.isRestrictedForReporter()).as("reporter").isEqualTo(mReporter);
        assertions.assertThat(mController.isRestrictedForSdk()).as("appMetrica").isEqualTo(mAppMetrica);
        assertions.assertThat(mController.isRestrictedForBackgroundDataCollection()).as("location").isEqualTo(mLocation);
        assertions.assertAll();
    }

}
