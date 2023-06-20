package io.appmetrica.analytics;

import io.appmetrica.analytics.coreapi.internal.constants.DeviceTypeValues;

@Deprecated
public enum DeviceType {

    PHONE(DeviceTypeValues.PHONE), TABLET(DeviceTypeValues.TABLET), TV(DeviceTypeValues.TV);

    private final String type;

    private DeviceType(String value) {
        this.type = value;
    }

    public String getType() {
        return type;
    }

    public static DeviceType of(String value) {
        for (DeviceType deviceType : values()) {
            if (deviceType.type.equals(value)) {
                return deviceType;
            }
        }
        return null;
    }
}
