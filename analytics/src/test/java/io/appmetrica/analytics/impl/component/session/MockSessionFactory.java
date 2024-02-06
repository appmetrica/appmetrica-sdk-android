package io.appmetrica.analytics.impl.component.session;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicLong;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;

import static org.mockito.ArgumentMatchers.anyBoolean;
import static org.mockito.Matchers.anyLong;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;

//Always load invalid session and create valid. It's enough For FSM tests.
public class MockSessionFactory implements ISessionFactory<SessionArguments> {

    private static final AtomicLong mSessionId = new AtomicLong(0);
    private Map<Long, AtomicLong> mNextReportIdMap = new HashMap<Long, AtomicLong>();
    private Map<Long, Boolean> aliveNeededMap = new HashMap<Long, Boolean>();

    private final SessionType mType;

    public MockSessionFactory(SessionType type) {
        mType = type;
    }

    @Nullable
    public Session load() {
        return createMock(false, 0);
    }

    @NonNull
    public Session create(SessionArguments arguments) {
        return createMock(true, arguments.creationTimestamp);
    }

    private Session createMock(boolean isValid, long creationTimestamp) {
        Session newSession = mock(Session.class);
        doReturn(mType).when(newSession).getType();
        doReturn(isValid).when(newSession).isValid(anyLong());
        final long sessionId = creationTimestamp == 0 ? mSessionId.incrementAndGet() :
                TimeUnit.MILLISECONDS.toSeconds(creationTimestamp);
        doReturn(sessionId).when(newSession).getId();
        doReturn(true).when(newSession).isAliveNeeded();
        mNextReportIdMap.put(sessionId, new AtomicLong(0));

        doAnswer(new Answer() {
            public Object answer(InvocationOnMock invocation) throws Throwable {
                return mNextReportIdMap.get(sessionId).getAndIncrement();
            }
        }).when(newSession).getNextReportId();

        doAnswer(new Answer() {
            public Object answer(InvocationOnMock invocation) throws Throwable {
                return 0;
            }
        }).when(newSession).getAndUpdateLastEventTimeSeconds(anyLong());

        doAnswer(new Answer() {
            public Object answer(InvocationOnMock invocation) throws Throwable {
                return ((Long) (invocation.getArguments()[0])) / 1000;
            }
        }).when(newSession).getAndUpdateLastEventTimeSeconds(anyLong());
        aliveNeededMap.put(sessionId, true);
        doAnswer(new Answer() {
            @Override
            public Object answer(InvocationOnMock invocation) throws Throwable {
                return aliveNeededMap.get(sessionId);
            }
        }).when(newSession).isAliveNeeded();
        doAnswer(new Answer() {
            @Override
            public Object answer(InvocationOnMock invocation) throws Throwable {
                return aliveNeededMap.put(sessionId, (Boolean) invocation.getArgument(0));
            }
        }).when(newSession).updateAliveReportNeeded(anyBoolean());

        return newSession;

    }
}
