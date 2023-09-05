package io.appmetrica.analytics;

import androidx.annotation.NonNull;

/**
 * Exception that is thrown if some of the mandatory conditions for calling AppMetrica SDK methods are not met.
 */
public class ValidationException extends IllegalArgumentException {

    /**
     * Constructor for {@link ValidationException}
     * @param message description
     */
    public ValidationException(@NonNull String message) {
        super(message);
    }

}
