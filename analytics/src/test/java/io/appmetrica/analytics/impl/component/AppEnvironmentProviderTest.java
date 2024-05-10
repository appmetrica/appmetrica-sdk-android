package io.appmetrica.analytics.impl.component;

import io.appmetrica.analytics.impl.AppEnvironment;
import io.appmetrica.analytics.impl.TestsData;
import io.appmetrica.analytics.impl.db.preferences.PreferencesComponentDbStorage;
import io.appmetrica.analytics.impl.utils.PublicLogger;
import io.appmetrica.analytics.testutils.CommonTest;
import java.util.Random;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentMatcher;
import org.robolectric.RobolectricTestRunner;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.AdditionalMatchers.not;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith(RobolectricTestRunner.class)
public class AppEnvironmentProviderTest extends CommonTest {

    public static AppEnvironmentProvider createAppEnvironmentProviderStubMock() {
        AppEnvironmentProvider provider = mock(AppEnvironmentProvider.class);
        AppEnvironment appEnvironment = new AppEnvironment("{}", 0, mock(PublicLogger.class));
        when(provider.getOrCreate(any(ComponentId.class), any(PublicLogger.class), any(PreferencesComponentDbStorage.class)))
                .thenReturn(appEnvironment);
        return provider;
    }

    @Test
    public void testProvider() {
        AppEnvironment targetEnvironment = mock(AppEnvironment.class);

        final MainReporterComponentId targetComponentIdMock = new MainReporterComponentId(
                TestsData.generatePackage(),
                TestsData.generateApiKey()
        );

        ArgumentMatcher<ComponentId> targetComponentIdMatcher = new ArgumentMatcher<ComponentId>() {
            @Override
            public boolean matches(ComponentId argument) {
                if (argument == null) {
                    return false;
                }
                return targetComponentIdMock.toString().equals(argument.toString());
            }
        };

        AppEnvironmentProvider provider = mock(AppEnvironmentProvider.class);
        AppEnvironment appEnvironment = new AppEnvironment("{}", 0, mock(PublicLogger.class));
        when(provider.getOrCreate(not(argThat(targetComponentIdMatcher)), any(PublicLogger.class), any(PreferencesComponentDbStorage.class))).thenReturn(appEnvironment);
        when(provider.getOrCreate(argThat(targetComponentIdMatcher), any(PublicLogger.class), any(PreferencesComponentDbStorage.class))).thenReturn(targetEnvironment);
        Random random = new Random();
        for (int i = 0; i < 10; i++) {
            PreferencesComponentDbStorage preferences = mock(PreferencesComponentDbStorage.class);
            ComponentId componentId;
            if (random.nextBoolean()) {
                componentId = new MainReporterComponentId(
                        TestsData.generatePackage(),
                        TestsData.generateApiKey()
                );
            } else {
                componentId = new ComponentId(
                        TestsData.generatePackage(),
                        TestsData.generateApiKey()
                );
            }
            assertThat(provider.getOrCreate(componentId, mock(PublicLogger.class), preferences)).isNotEqualTo(targetEnvironment);
        }
        PreferencesComponentDbStorage preferences = mock(PreferencesComponentDbStorage.class);
        assertThat(provider.getOrCreate(targetComponentIdMock, mock(PublicLogger.class), preferences)).isEqualTo(targetEnvironment);
    }

    @Test
    public void testEnvironmentStoredInMap() {
        AppEnvironmentProvider provider = new AppEnvironmentProvider();
        ComponentId id = mock(ComponentId.class);
        PreferencesComponentDbStorage componentSession = mock(PreferencesComponentDbStorage.class);

        when(id.toString()).thenReturn("test_id");
        when(componentSession.getAppEnvironmentRevision()).thenReturn(new AppEnvironment.EnvironmentRevision("{}", 0));

        AppEnvironment environment = provider.getOrCreate(id, mock(PublicLogger.class), componentSession);
        assertThat(environment).isIn(environment);
        verify(componentSession, times(1)).getAppEnvironmentRevision();
    }

    @Test
    public void testCommit() {
        AppEnvironment.EnvironmentRevision revision = new AppEnvironment.EnvironmentRevision("{}", 0);
        AppEnvironmentProvider provider = new AppEnvironmentProvider();

        PreferencesComponentDbStorage componentSession = mock(PreferencesComponentDbStorage.class);
        when(componentSession.putAppEnvironmentRevision(any(AppEnvironment.EnvironmentRevision.class))).thenReturn(componentSession);

        provider.commit(revision, componentSession);
        verify(componentSession, times(1)).putAppEnvironmentRevision(revision);
        verify(componentSession, times(1)).commit();
    }

    @Test
    public void testCommitWhenNeeded() {
        AppEnvironment.EnvironmentRevision revision = new AppEnvironment.EnvironmentRevision("{}", 10);
        AppEnvironmentProvider provider = new AppEnvironmentProvider();
        PreferencesComponentDbStorage componentSession = mock(PreferencesComponentDbStorage.class);

        when(componentSession.getAppEnvironmentRevision()).thenReturn(new AppEnvironment.EnvironmentRevision("{}", 9));
        when(componentSession.putAppEnvironmentRevision(any(AppEnvironment.EnvironmentRevision.class))).thenReturn(componentSession);

        assertThat(provider.commitIfNeeded(revision, componentSession)).isTrue();
        verify(componentSession, times(1)).putAppEnvironmentRevision(revision);
        verify(componentSession, times(1)).commit();
    }

    @Test
    public void testCommitWhenItsNotNeeded() {
        AppEnvironment.EnvironmentRevision revision = new AppEnvironment.EnvironmentRevision("{}", 0);
        AppEnvironmentProvider provider = new AppEnvironmentProvider();
        PreferencesComponentDbStorage componentSession = mock(PreferencesComponentDbStorage.class);

        when(componentSession.getAppEnvironmentRevision()).thenReturn(new AppEnvironment.EnvironmentRevision("{}", 9));
        when(componentSession.putAppEnvironmentRevision(any(AppEnvironment.EnvironmentRevision.class))).thenReturn(componentSession);

        assertThat(provider.commitIfNeeded(revision, componentSession)).isFalse();
        verify(componentSession, never()).putAppEnvironmentRevision(revision);
        verify(componentSession, never()).commit();
    }

}
