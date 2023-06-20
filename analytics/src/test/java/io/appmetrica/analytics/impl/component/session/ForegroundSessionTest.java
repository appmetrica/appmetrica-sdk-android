package io.appmetrica.analytics.impl.component.session;

import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;

@RunWith(RobolectricTestRunner.class)
public class ForegroundSessionTest extends BaseSessionTest {

    @Override
    protected AbstractSessionFactory getSessionFactory() {
        return new ForegroundSessionFactory(mComponent, sessionIDProvider, mSessionStorage, mSelftReporter, timeProvider);
    }

    @Override
    protected int getSessionTimeout() {
        return 10;
    }

}
