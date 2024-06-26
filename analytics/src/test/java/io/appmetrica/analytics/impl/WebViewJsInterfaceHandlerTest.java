package io.appmetrica.analytics.impl;

import android.webkit.WebSettings;
import android.webkit.WebView;
import io.appmetrica.analytics.impl.proxy.AppMetricaProxy;
import io.appmetrica.analytics.logger.appmetrica.internal.PublicLogger;
import io.appmetrica.analytics.internal.js.AppMetricaInitializerJsInterface;
import io.appmetrica.analytics.internal.js.AppMetricaJsInterface;
import io.appmetrica.analytics.testutils.CommonTest;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.robolectric.RobolectricTestRunner;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.clearInvocations;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

@RunWith(RobolectricTestRunner.class)
public class WebViewJsInterfaceHandlerTest extends CommonTest {

    @Mock
    private WebView webView;
    @Mock
    private WebSettings webSettings;
    @Mock
    private PublicLogger logger;
    @Mock
    private AppMetricaProxy proxy;
    private WebViewJsInterfaceHandler webViewJsInterfaceHandler;

    @Before
    public void setUp() {
        MockitoAnnotations.openMocks(this);
        when(webView.getSettings()).thenReturn(webSettings);
        webViewJsInterfaceHandler = new WebViewJsInterfaceHandler();
    }

    @Test
    public void initWebViewReportingJavascriptDisabledNoLogger() {
        when(webSettings.getJavaScriptEnabled()).thenReturn(false);
        webViewJsInterfaceHandler.initWebViewReporting(webView, proxy);
        verify(webView, never()).addJavascriptInterface(any(Object.class), anyString());
        webViewJsInterfaceHandler.setLogger(logger);
        verify(logger).warning("WebView interface setup failed because javascript is disabled for the WebView.");
    }

    @Test
    public void initWebViewReportingJavascriptDisabledHasLogger() {
        when(webSettings.getJavaScriptEnabled()).thenReturn(false);
        webViewJsInterfaceHandler.setLogger(logger);
        webViewJsInterfaceHandler.initWebViewReporting(webView, proxy);
        verify(webView, never()).addJavascriptInterface(any(Object.class), anyString());
        verify(logger).warning("WebView interface setup failed because javascript is disabled for the WebView.");
    }

    @Test
    public void initWebViewReportingJavascriptExceptionNoLogger() {
        Throwable exception = new RuntimeException();
        when(webSettings.getJavaScriptEnabled()).thenReturn(true);
        doThrow(exception).when(webView).addJavascriptInterface(any(Object.class), anyString());
        webViewJsInterfaceHandler.initWebViewReporting(webView, proxy);
        verify(webView).addJavascriptInterface(any(Object.class), anyString());
        webViewJsInterfaceHandler.setLogger(logger);
        verify(logger).error(exception, "WebView interface setup failed because of an exception.");
    }

    @Test
    public void initWebViewReportingJavascriptExceptionHasLogger() {
        Throwable exception = new RuntimeException();
        when(webSettings.getJavaScriptEnabled()).thenReturn(true);
        doThrow(exception).when(webView).addJavascriptInterface(any(Object.class), anyString());
        webViewJsInterfaceHandler.setLogger(logger);
        webViewJsInterfaceHandler.initWebViewReporting(webView, proxy);
        verify(webView).addJavascriptInterface(any(Object.class), anyString());
        verify(logger).error(exception, "WebView interface setup failed because of an exception.");
    }

    @Test
    public void initWebViewReportingJavascriptSuccessNoLogger() {
        when(webSettings.getJavaScriptEnabled()).thenReturn(true);
        webViewJsInterfaceHandler.initWebViewReporting(webView, proxy);
        verify(webView).addJavascriptInterface(any(AppMetricaJsInterface.class), eq("AppMetrica"));
        verify(webView).addJavascriptInterface(any(AppMetricaInitializerJsInterface.class), eq("AppMetricaInitializer"));
        webViewJsInterfaceHandler.setLogger(logger);
        verify(logger).info("WebView interface setup is successful.");
    }

    @Test
    public void initWebViewReportingJavascriptSuccessHasLogger() {
        when(webSettings.getJavaScriptEnabled()).thenReturn(true);
        webViewJsInterfaceHandler.setLogger(logger);
        webViewJsInterfaceHandler.initWebViewReporting(webView, proxy);
        verify(webView).addJavascriptInterface(any(AppMetricaJsInterface.class), eq("AppMetrica"));
        verify(webView).addJavascriptInterface(any(AppMetricaInitializerJsInterface.class), eq("AppMetricaInitializer"));
        verify(logger).info("WebView interface setup is successful.");
    }

    @Test
    public void callMethodsSeveralTimes() {
        when(webSettings.getJavaScriptEnabled()).thenReturn(true);
        webViewJsInterfaceHandler.setLogger(logger);
        webViewJsInterfaceHandler.initWebViewReporting(webView, proxy);
        verify(webView).addJavascriptInterface(any(AppMetricaJsInterface.class), eq("AppMetrica"));
        verify(webView).addJavascriptInterface(any(AppMetricaInitializerJsInterface.class), eq("AppMetricaInitializer"));
        verify(logger).info("WebView interface setup is successful.");
        clearInvocations(webView, logger);
        webViewJsInterfaceHandler.setLogger(logger);
        verifyNoMoreInteractions(logger);
        webViewJsInterfaceHandler.initWebViewReporting(webView, proxy);
        verify(webView).addJavascriptInterface(any(AppMetricaJsInterface.class), eq("AppMetrica"));
        verify(webView).addJavascriptInterface(any(AppMetricaInitializerJsInterface.class), eq("AppMetricaInitializer"));
        verify(logger).info("WebView interface setup is successful.");
        clearInvocations(webView, logger);
        webViewJsInterfaceHandler.setLogger(logger);
        verifyNoMoreInteractions(logger);
    }
}
