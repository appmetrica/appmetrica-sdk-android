package io.appmetrica.analytics.impl.proxy.validation;

import androidx.annotation.Nullable;
import io.appmetrica.analytics.impl.utils.validation.NonEmptyCollectionValidator;
import io.appmetrica.analytics.impl.utils.validation.NonEmptyStringValidator;
import io.appmetrica.analytics.impl.utils.validation.NonNullValidator;
import io.appmetrica.analytics.impl.utils.validation.ThrowIfFailedValidator;
import io.appmetrica.analytics.impl.utils.validation.Validator;
import io.appmetrica.analytics.plugins.IPluginReporter;
import io.appmetrica.analytics.plugins.PluginErrorDetails;
import io.appmetrica.analytics.plugins.StackTraceItem;
import java.util.Collection;

public class PluginsBarrier implements IPluginReporter {

    private final Validator<PluginErrorDetails> pluginErrorDetailsNonNullValidator =
            new ThrowIfFailedValidator<PluginErrorDetails>(
                    new NonNullValidator<PluginErrorDetails>("Error details")
            );
    private final Validator<String> errorIdentifierValidator = new ThrowIfFailedValidator<String>(
        new NonEmptyStringValidator("Error identifier")
    );
    private final Validator<Collection<StackTraceItem>> silentNonEmptyStacktraceValidator =
            new NonEmptyCollectionValidator<>("Stacktrace");

    @Override
    public void reportUnhandledException(@Nullable PluginErrorDetails errorDetails) {
        pluginErrorDetailsNonNullValidator.validate(errorDetails);
    }

    @Override
    public void reportError(@Nullable PluginErrorDetails errorDetails, @Nullable String message) {
        pluginErrorDetailsNonNullValidator.validate(errorDetails);
    }

    public boolean reportErrorWithFilledStacktrace(@Nullable PluginErrorDetails errorDetails,
                                                   @Nullable String message) {
        reportError(errorDetails, message);
        return silentNonEmptyStacktraceValidator.validate(errorDetails.getStacktrace()).isValid();
    }

    @Override
    public void reportError(@Nullable String identifier,
                            @Nullable String message,
                            @Nullable PluginErrorDetails errorDetails) {
        errorIdentifierValidator.validate(identifier);
    }
}
