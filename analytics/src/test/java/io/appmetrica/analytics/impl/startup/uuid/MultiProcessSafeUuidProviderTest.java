package io.appmetrica.analytics.impl.startup.uuid;

import android.content.Context;
import io.appmetrica.analytics.coreapi.internal.identifiers.IdentifierStatus;
import io.appmetrica.analytics.impl.db.FileConstants;
import io.appmetrica.analytics.impl.utils.concurrency.ExclusiveMultiProcessFileLock;
import io.appmetrica.analytics.internal.IdentifiersResult;
import io.appmetrica.analytics.testutils.CommonTest;
import io.appmetrica.analytics.testutils.MockedConstructionRule;
import java.util.UUID;
import org.assertj.core.api.SoftAssertions;
import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InOrder;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;
import org.robolectric.RobolectricTestRunner;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.clearInvocations;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

@RunWith(RobolectricTestRunner.class)
public class MultiProcessSafeUuidProviderTest extends CommonTest {

    @Rule
    public MockedConstructionRule<ExclusiveMultiProcessFileLock> exclusiveMultiProcessFileLockMockedConstructionRule =
        new MockedConstructionRule<>(ExclusiveMultiProcessFileLock.class);

    @Mock
    private Context context;
    @Mock
    private IOuterSourceUuidImporter outerSourceUuidImporter;
    @Mock
    private ExclusiveMultiProcessFileLock lock;
    @Mock
    private PersistentUuidHolder persistentUuidHolder;

    private MultiProcessSafeUuidProvider multiProcessSafeUuidProvider;

    private String storedUuid = UUID.randomUUID().toString();
    private String generatedUuid = UUID.randomUUID().toString();
    private String uuidFromOuterSource = UUID.randomUUID().toString();

    @Before
    public void setUp() throws Exception {
        MultiProcessUuidLockProvider.reset();
        MockitoAnnotations.openMocks(this);

        when(persistentUuidHolder.readUuid()).thenReturn(storedUuid);
        when(persistentUuidHolder.handleUuid(anyString())).then(new Answer<String>() {
            @Override
            public String answer(InvocationOnMock invocation) throws Throwable {
                return ((String) invocation.getArgument(0));
            }
        });
        when(persistentUuidHolder.handleUuid(null)).thenReturn(generatedUuid);
        when(outerSourceUuidImporter.get(context)).thenReturn(uuidFromOuterSource);

        multiProcessSafeUuidProvider = new MultiProcessSafeUuidProvider(
            context,
            outerSourceUuidImporter,
            lock,
            persistentUuidHolder
        );
    }

    @After
    public void tearDown() {
        MultiProcessUuidLockProvider.reset();
    }

    @Test
    public void touchMigrationOnCreate() throws Throwable {
        InOrder inOrder = inOrder(lock, persistentUuidHolder);
        inOrder.verify(lock).lock();
        inOrder.verify(persistentUuidHolder).checkMigration();
        inOrder.verify(lock).unlock();
    }

    @Test
    public void readUuidIfExistsInPersistentProviderAfterLock() throws Throwable {
        when(persistentUuidHolder.readUuid()).thenReturn(storedUuid);
        assertValidUuid(multiProcessSafeUuidProvider.readUuid(), storedUuid);
        // Check memory cache
        assertValidUuid(multiProcessSafeUuidProvider.readUuid(), storedUuid);

        InOrder inOrder = inOrder(lock, persistentUuidHolder, outerSourceUuidImporter);

        inOrder.verify(lock).lock();
        inOrder.verify(persistentUuidHolder).readUuid();
        inOrder.verify(lock).unlock();
        inOrder.verifyNoMoreInteractions();
    }

    @Test
    public void readUuidIfExistsInOuterSourceImporter() throws Throwable {
        clearInvocations(lock, persistentUuidHolder);
        when(persistentUuidHolder.readUuid()).thenReturn(null);
        assertValidUuid(multiProcessSafeUuidProvider.readUuid(), uuidFromOuterSource);
        // Check memory cache
        assertValidUuid(multiProcessSafeUuidProvider.readUuid(), uuidFromOuterSource);

        InOrder inOrder = inOrder(persistentUuidHolder, lock, outerSourceUuidImporter);
        inOrder.verify(lock).lock();
        inOrder.verify(persistentUuidHolder).readUuid();
        inOrder.verify(outerSourceUuidImporter).get(context);
        inOrder.verify(persistentUuidHolder).handleUuid(uuidFromOuterSource);
        inOrder.verify(lock).unlock();
        inOrder.verifyNoMoreInteractions();
        verifyNoMoreInteractions(outerSourceUuidImporter, lock);
    }

    @Test
    public void readUuidIfGenerationPreconditionCheckerMatchesPreconditions() throws Throwable {
        when(persistentUuidHolder.readUuid()).thenReturn(null);
        when(outerSourceUuidImporter.get(context)).thenReturn(null);

        assertValidUuid(multiProcessSafeUuidProvider.readUuid(), generatedUuid);
        // Check memory cache
        assertValidUuid(multiProcessSafeUuidProvider.readUuid(), generatedUuid);

        InOrder inOrder = inOrder(persistentUuidHolder, lock, outerSourceUuidImporter, persistentUuidHolder);

        inOrder.verify(lock).lock();
        inOrder.verify(persistentUuidHolder).readUuid();
        inOrder.verify(outerSourceUuidImporter).get(context);
        inOrder.verify(persistentUuidHolder).handleUuid(null);
        inOrder.verify(lock).unlock();
        inOrder.verifyNoMoreInteractions();
    }

    @Test
    public void exclusiveLockCreation() throws Throwable {
        MultiProcessSafeUuidProvider multiProcessUuidLockProvider =
            new MultiProcessSafeUuidProvider(context, outerSourceUuidImporter);
        new MultiProcessSafeUuidProvider(context, outerSourceUuidImporter);

        assertThat(exclusiveMultiProcessFileLockMockedConstructionRule.getConstructionMock().constructed())
            .hasSize(1);
        assertThat(exclusiveMultiProcessFileLockMockedConstructionRule.getArgumentInterceptor().flatArguments())
            .containsExactly(context, FileConstants.UUID_FILE_NAME);

        multiProcessUuidLockProvider.readUuid();

        // 2 times during each MultiProcessSafeUuidProvider creation and one more - during reading uuid
        verify(
            exclusiveMultiProcessFileLockMockedConstructionRule.getConstructionMock().constructed().get(0),
            times(3)
        ).lock();
    }

    private void assertValidUuid(IdentifiersResult result, String expected) {
        assertThat(result).isNotNull();
        SoftAssertions assertions = new SoftAssertions();
        assertions.assertThat(result.id).as("Uuid").isEqualTo(expected);
        assertions.assertThat(result.status).as("Status").isEqualTo(IdentifierStatus.OK);
        assertions.assertThat(result.errorExplanation).as("Error").isNull();
        assertions.assertAll();
    }
}
