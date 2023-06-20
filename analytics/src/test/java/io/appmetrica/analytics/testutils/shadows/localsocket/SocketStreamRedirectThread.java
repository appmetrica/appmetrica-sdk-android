package io.appmetrica.analytics.testutils.shadows.localsocket;

import androidx.annotation.NonNull;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.LinkedList;
import java.util.Queue;
import java.util.concurrent.Semaphore;

class SocketStreamRedirectThread {

    private final Semaphore semaphore = new Semaphore(0);

    private final Queue<byte[]> queue = new LinkedList<byte[]>();

    private final InputStream bis = new InputStream() {
        @Override
        public int read() throws IOException {
            throw new IllegalStateException("not implemented");
        }

        @Override
        public int read(byte[] b) throws IOException {
            try {
                semaphore.acquire();
            } catch (InterruptedException ignored) {
            }
            byte[] next;
            synchronized (queue) {
                next = queue.remove();
            }
            System.arraycopy(next, 0, b, 0, next.length);
            return next.length;
        }
    };
    private final OutputStream bos = new OutputStream() {

        @Override
        public void write(byte[] b) throws IOException {
            synchronized (queue) {
                queue.add(b);
            }
            semaphore.release();
        }

        @Override
        public void write(int b) throws IOException {
        }
    };

    @NonNull
    InputStream getInputStream() {
        return bis;
    }

    @NonNull
    OutputStream getOutputStream() {
        return bos;
    }

}
