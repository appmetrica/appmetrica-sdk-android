package io.appmetrica.analytics.impl.utils;

import android.content.Context;
import android.util.Base64;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import io.appmetrica.analytics.coreutils.internal.io.Base64Utils;
import io.appmetrica.analytics.impl.IOUtils;
import java.io.UnsupportedEncodingException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

public class SecurityUtils {

    @Nullable
    public static String encode(@NonNull Context context, @Nullable String value) throws UnsupportedEncodingException {
        String compressedValue = Base64Utils.compressBase64String(value);
        byte[] compressedBytes = compressedValue.getBytes(IOUtils.UTF8_ENCODING);
        byte[] encBytes = xor(context, compressedBytes);
        return Base64.encodeToString(encBytes, Base64.DEFAULT);
    }

    @Nullable
    public static String decode(@NonNull Context context, @NonNull String value) throws UnsupportedEncodingException {
        return decode(context, value.getBytes(IOUtils.UTF8_ENCODING));
    }

    @Nullable
    public static String decode(@NonNull Context contexts, @NonNull byte[] value) throws UnsupportedEncodingException {
        String result = null;
        byte[] encBytes = Base64.decode(value, Base64.DEFAULT);
        byte[] compressedBytes = xor(contexts, encBytes);
        if (compressedBytes != null) {
            String compressedString = new String(compressedBytes, IOUtils.UTF8_ENCODING);
            result = Base64Utils.decompressBase64GzipAsString(compressedString);
        }
        return result;
    }

    @Nullable
    private static byte[] xor(@NonNull Context contexts, @NonNull byte[] input) {
        try {
            byte[] key = getMD5BasedOnPackageNameEncryptionKey(contexts);
            byte[] result = new byte[input.length];
            for (int i = 0; i < input.length; i++) {
                result[i] = (byte) (input[i] ^ key[i % key.length]);
            }
            return result;
        } catch (Throwable ignored) {
        }
        return null;
    }

    @NonNull
    private static byte[] getMD5BasedOnPackageNameEncryptionKey(@NonNull Context context)
            throws NoSuchAlgorithmException, UnsupportedEncodingException {
        return getMD5Hash(context.getPackageName());
    }

    @NonNull
    public static byte[] getMD5Hash(@NonNull final String input)
            throws NoSuchAlgorithmException, UnsupportedEncodingException {
        MessageDigest messageDigest = MessageDigest.getInstance("MD5");
        messageDigest.reset();
        messageDigest.update(input.getBytes(IOUtils.UTF8_ENCODING));
        return messageDigest.digest();
    }
}
