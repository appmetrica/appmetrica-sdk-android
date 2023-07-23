package io.appmetrica.analytics.impl;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import io.appmetrica.analytics.IReporter;
import io.appmetrica.analytics.coreutils.internal.io.Base64Utils;
import io.appmetrica.analytics.coreutils.internal.logger.YLogger;
import io.appmetrica.analytics.impl.selfreporting.AppMetricaSelfReportFacade;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.RandomAccessFile;
import java.io.Reader;
import java.io.StringWriter;
import java.io.UnsupportedEncodingException;
import java.io.Writer;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.channels.FileLock;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import kotlin.io.ByteStreamsKt;

public final class IOUtils {

    private static final String TAG = "[IOUtils]";

    /**
     * UTF-8 encoding.
     */
    public static final String UTF8_ENCODING = "UTF-8";

    @NonNull
    private static final FileProvider fileProvider = new FileProvider();

    private IOUtils() {
        /** Prevent installation */
    }

    /**
     * Converts {@link java.io.InputStream} to {@link java.lang.String}.
     * @throws IOException
     */
    public static String toString(InputStream inputStream) throws IOException {
        InputStreamReader input = new InputStreamReader(inputStream, UTF8_ENCODING);
        StringWriter output = new StringWriter();
        copyChars(input, output);
        return output.toString();
    }

    /**
     * Copies {@link java.io.Reader} to {@link java.io.Writer}.
     * @throws IOException
     */
    public static int copyChars(Reader input, Writer output) throws IOException {
        char [] bufferChars = new char [Base64Utils.IO_BUFFER_SIZE];

        int resultCountChars = 0;
        int readCountChars = 0;

        while (-1 != (readCountChars = input.read(bufferChars, 0, Base64Utils.IO_BUFFER_SIZE))) {
            output.write(bufferChars, 0, readCountChars);
            resultCountChars += readCountChars;
        }

        return resultCountChars;
    }

    @Nullable
    public static byte[] readAll(@NonNull File file) {
        FileInputStream fis = null;
        try {
            YLogger.info(TAG, "File with path: %s contains %d bytes", file.getAbsolutePath(), file.length());
            fis = new FileInputStream(file);
            return ByteStreamsKt.readBytes(fis);
        } catch (Throwable throwable) {
            YLogger.error(TAG, throwable);
        } finally {
            Utils.closeCloseable(fis);
        }
        return null;
    }

    @Nullable
    public static byte[] readAll(@Nullable String filePath) {
        if (filePath != null) {
            return readAll(fileProvider.getFileByNonNullPath(filePath));
        } else {
            return null;
        }
    }

    @Nullable
    public static String getStringFileLocked(@Nullable final File file) {
        String result = null;
        byte[] buffer = readFileLocked(file);
        try {
            if (buffer != null) {
                result = new String(buffer, UTF8_ENCODING);
            }
        } catch (UnsupportedEncodingException e) {
            YLogger.error(TAG, e, e.getMessage());
            result = new String(buffer);
            final IReporter reporter = AppMetricaSelfReportFacade.getReporter();
            reporter.reportError("read_share_file_with_unsupported_encoding", e);
        }
        return result;
    }

    @Nullable
    public static byte[] readFileLocked(@Nullable final File file) {
        if (file == null || !file.exists()) {
            YLogger.info(TAG, "Cannot read file as it does not exist");
            return null;
        }
        RandomAccessFile stream = null;
        FileChannel channel;
        ByteBuffer buffer;
        FileLock lock = null;
        try {
            stream = new RandomAccessFile(file, "r");
            channel = stream.getChannel();
            YLogger.info(TAG, "Try to read file %s.", file.getAbsolutePath());
            lock = channel.lock(0, Long.MAX_VALUE, true);
            YLogger.info(TAG, "Try to read file %s. Lock received. File length = %d",
                file.getAbsolutePath(), file.length());
            buffer = ByteBuffer.allocate((int) file.length());
            channel.read(buffer);
            buffer.flip();
            return buffer.array();
        } catch (IOException e) {
            YLogger.error(TAG, e, "can't read file %s", file.getAbsolutePath());
        } catch (SecurityException se) {
            YLogger.error(TAG, se, "Have no enough rights to read file %s. Reason %s",
                file.getAbsolutePath(), se.getMessage());
        } catch (Throwable u) {
            YLogger.error(TAG, u, "Unknown exception during file reading");
            AppMetricaSelfReportFacade.getReporter().reportError("error_during_file_reading", u);
        } finally {
            releaseFileLock(file.getAbsolutePath(), lock);
            Utils.closeCloseable(stream);
        }
        return null;
    }

    public static void releaseFileLock(final String fileName, final FileLock lock) {
        if (lock != null && lock.isValid()) {
            try {
                lock.release();
                YLogger.info(TAG, "Lock released for %s.", fileName);
            } catch (IOException e) {
                YLogger.info(TAG, "Failed to release lock for %s.", fileName);
            }
        }
    }

    public static void writeStringFileLocked(String data, String fileName, FileOutputStream fileOutputStream) {
        FileChannel channel;
        FileLock lock = null;
        ByteBuffer buffer;
        try {
            channel = fileOutputStream.getChannel();
            YLogger.info(TAG, "Try to write file: %s", fileName);
            lock = channel.lock();
            YLogger.info(TAG, "Try to write file: %s. Lock received.", fileName);
            byte[] bytes = data.getBytes(UTF8_ENCODING);
            buffer = ByteBuffer.allocate(bytes.length);
            buffer.put(bytes);
            buffer.flip();
            channel.write(buffer);
            channel.force(true);
        } catch (IOException e) {
            YLogger.error(TAG, e, "can't update file: %s", fileName);
        } finally {
            releaseFileLock(fileName, lock);
            Utils.closeCloseable(fileOutputStream);
        }
    }

    public static byte[] md5(@NonNull byte[] input) {
        try {
            return MessageDigest.getInstance("MD5").digest(input);
        } catch (NoSuchAlgorithmException e) {

        }
        return new byte[0];
    }
}
