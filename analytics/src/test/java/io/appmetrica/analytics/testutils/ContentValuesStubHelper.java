package io.appmetrica.analytics.testutils;

import android.content.ContentValues;
import org.mockito.Mockito;
import org.mockito.stubbing.Answer;

import java.util.Map;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;

public class ContentValuesStubHelper {

    public static void stubPutMethod(ContentValues contentValues, Map<String, Object> dataMap) {
        Answer<Void> putAnswer = invocation -> {
            String key = (String) invocation.getArguments()[0];
            Object value = invocation.getArguments()[1];
            dataMap.put(key, value);
            return null;
        };

        // Stub all put() overloads
        Mockito.doAnswer(putAnswer).when(contentValues).put(anyString(), (String) any());
        Mockito.doAnswer(putAnswer).when(contentValues).put(anyString(), (Integer) any());
        Mockito.doAnswer(putAnswer).when(contentValues).put(anyString(), (Long) any());
        Mockito.doAnswer(putAnswer).when(contentValues).put(anyString(), (Boolean) any());
        Mockito.doAnswer(putAnswer).when(contentValues).put(anyString(), (Double) any());
        Mockito.doAnswer(putAnswer).when(contentValues).put(anyString(), (Float) any());
        Mockito.doAnswer(putAnswer).when(contentValues).put(anyString(), (byte[]) any());
        Mockito.doAnswer(putAnswer).when(contentValues).put(anyString(), (Short) any());
        Mockito.doAnswer(putAnswer).when(contentValues).put(anyString(), (Byte) any());
    }
}
