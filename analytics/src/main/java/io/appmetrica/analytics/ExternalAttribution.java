package io.appmetrica.analytics;

/**
 * External attribution interface.
 * DO NOT IMPLEMENT IT ON YOUR OWN! Use {@link ExternalAttributions} class.
 * Used with {@link AppMetrica#reportExternalAttribution(ExternalAttribution)} method.
 */
public interface ExternalAttribution {

    /**
     * Method to make byte array with information.
     *
     * @return byte array with protobuf data
     */
    byte[] toBytes();
}
