package io.appmetrica.analytics.testutils.shadows.localsocket;

import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

class LocalSocketShadowStorage {

    static final Set<String> SOCKETS = Collections.synchronizedSet(new HashSet<String>());
    static final Set<String> CONNECTIONS = Collections.synchronizedSet(new HashSet<String>());
    static final Map<String, SocketStreamRedirectThread> STREAMS =
            Collections.synchronizedMap(new HashMap<String, SocketStreamRedirectThread>());

}
