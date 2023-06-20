package io.appmetrica.analytics.impl.component.session;

import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;

@RunWith(RobolectricTestRunner.class)
public class BackgroundSessionTest extends BaseSessionTest {

    @Override
    protected AbstractSessionFactory getSessionFactory() {
        return new BackgroundSessionFactory(mComponent, sessionIDProvider, mSessionStorage, mSelftReporter, timeProvider);
    }

    @Override
    protected int getSessionTimeout() {
        return BackgroundSessionFactory.SESSION_TIMEOUT_SEC;
    }
}
