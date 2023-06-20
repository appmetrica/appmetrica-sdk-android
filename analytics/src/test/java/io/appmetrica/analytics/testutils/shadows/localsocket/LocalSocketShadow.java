package io.appmetrica.analytics.testutils.shadows.localsocket;

import android.net.LocalSocket;
import android.net.LocalSocketAddress;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import org.robolectric.annotation.Implementation;
import org.robolectric.annotation.Implements;

@Implements(value = LocalSocket.class, callThroughByDefault = true)
public class LocalSocketShadow {

    private boolean mConnected;
    private String mSocketName;

    @Implementation
    public void connect(LocalSocketAddress endpoint) throws IOException {
        mSocketName = endpoint.getName();
        if (LocalSocketShadowStorage.SOCKETS.contains(mSocketName) == false) {
            throw new IOException("Socket with name: " + mSocketName + " not exists");
        }
        synchronized (LocalSocketShadowStorage.CONNECTIONS) {
            if (LocalSocketShadowStorage.CONNECTIONS.contains(mSocketName)) {
                throw new IOException("Socket has already have input connection");
            }
            LocalSocketShadowStorage.CONNECTIONS.add(mSocketName);
            mConnected = true;
        }
    }

    @Implementation
    public synchronized InputStream getInputStream() throws IOException {
        if (!mConnected) {
            throw new IOException("Hasn't connected yet");
        }
        return getSocketRedirectThread().getInputStream();
    }

    @Implementation
    public void bind(LocalSocketAddress bindpoint) throws IOException {
        mSocketName = bindpoint.getName();
        mConnected = true;
    }

    @Implementation
    public OutputStream getOutputStream() throws IOException {
        if (!mConnected) {
            throw new IOException("Hasn't bind yet");
        }
        return getSocketRedirectThread().getOutputStream();
    }

    @Implementation
    public void close() throws IOException {
        synchronized (LocalSocketShadowStorage.SOCKETS) {
            LocalSocketShadowStorage.CONNECTIONS.remove(mSocketName);
            LocalSocketShadowStorage.STREAMS.remove(mSocketName);
        }
    }

    private SocketStreamRedirectThread getSocketRedirectThread() {
        synchronized (LocalSocketShadowStorage.STREAMS) {
            SocketStreamRedirectThread redirectThread = LocalSocketShadowStorage.STREAMS.get(mSocketName);
            if (redirectThread == null) {
                redirectThread = new SocketStreamRedirectThread();
                LocalSocketShadowStorage.STREAMS.put(mSocketName, redirectThread);
            }
            return redirectThread;
        }
    }
}
