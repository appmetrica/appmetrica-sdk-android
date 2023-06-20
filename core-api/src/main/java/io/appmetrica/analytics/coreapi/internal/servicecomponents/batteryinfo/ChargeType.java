package io.appmetrica.analytics.coreapi.internal.servicecomponents.batteryinfo;

public enum ChargeType {
    UNKNOWN(-1), NONE(0), USB(1), WIRELESS(2), AC(3);

    private final int id;

    private ChargeType(int id) {
        this.id = id;
    }

    public int getId() {
        return id;
    }

    public static ChargeType fromId(Integer id) {
        if (id != null) {
            for (ChargeType chargeType : ChargeType.values()) {
                if (chargeType.getId() == id) {
                    return chargeType;
                }
            }
        }
        return UNKNOWN;
    }
}
