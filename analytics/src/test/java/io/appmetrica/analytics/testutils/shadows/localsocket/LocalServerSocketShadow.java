package io.appmetrica.analytics.testutils.shadows.localsocket;

import android.net.LocalServerSocket;
import android.net.LocalSocket;
import android.net.LocalSocketAddress;
import java.io.IOException;
import org.robolectric.annotation.Implementation;
import org.robolectric.annotation.Implements;

@Implements(value = LocalServerSocket.class, callThroughByDefault = true)
public class LocalServerSocketShadow {

    private static String mName;

    @Implementation
    public void __constructor__(String name) throws IOException {
        synchronized (LocalSocketShadowStorage.SOCKETS) {
            if (LocalSocketShadowStorage.SOCKETS.contains(name)) {
                throw new IOException("Socket exsists");
            }
            LocalSocketShadowStorage.SOCKETS.add(name);
        }
        mName = name;
    }

    @Implementation
    public void close() {
        synchronized (LocalSocketShadowStorage.SOCKETS) {
            LocalSocketShadowStorage.SOCKETS.remove(mName);
            LocalSocketShadowStorage.CONNECTIONS.remove(mName);
            LocalSocketShadowStorage.STREAMS.remove(mName);
        }
    }

    @Implementation
    public LocalSocket accept() throws IOException {
        LocalSocket localSocket = new LocalSocket();
        localSocket.bind(new LocalSocketAddress(mName));
        return localSocket;
    }
}
