package io.appmetrica.analytics;

import io.appmetrica.analytics.coreapi.internal.constants.DeviceTypeValues;
import java.util.Arrays;
import java.util.List;

/**
 * Predefined device types for {@link AppMetricaConfig.Builder#withDeviceType(String)} method.
 */
public final class PredefinedDeviceTypes {
    private PredefinedDeviceTypes() {}

    /**
     * Phone.
     */
    public static final String PHONE = DeviceTypeValues.PHONE;

    /**
     * Tablet.
     */
    public static final String TABLET = DeviceTypeValues.TABLET;

    /**
     * TV.
     */
    public static final String TV = DeviceTypeValues.TV;

    /**
     * Multimedia system in the car.
     */
    public static final String CAR = DeviceTypeValues.CAR;

    /**
     * All predefined device types.
     */
    public static final List<String> ALL_VALUES = Arrays.asList(
        PredefinedDeviceTypes.CAR,
        PredefinedDeviceTypes.PHONE,
        PredefinedDeviceTypes.TABLET,
        PredefinedDeviceTypes.TV
    );
}
