package io.appmetrica.analytics.productflow;

/** Thrown when invalid parameters are passed to a product flow builder. */
public class IllegalProductFlowParametersException extends RuntimeException {
    /**
     * Creates an exception with the given detail message.
     *
     * @param message Description of the invalid parameters.
     */
    public IllegalProductFlowParametersException(String message) {
        super(message);
    }
}
