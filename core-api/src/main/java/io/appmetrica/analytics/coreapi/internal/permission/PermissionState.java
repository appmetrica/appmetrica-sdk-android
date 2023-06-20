package io.appmetrica.analytics.coreapi.internal.permission;

import androidx.annotation.NonNull;

public class PermissionState {

    @NonNull public final String name;
    public final boolean granted;

    public PermissionState(@NonNull String name, boolean granted) {
        this.name = name;
        this.granted = granted;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        PermissionState state = (PermissionState) o;

        if (granted != state.granted) return false;
        return name.equals(state.name);

    }

    @Override
    public int hashCode() {
        int result = name.hashCode();
        result = 31 * result + (granted ? 1 : 0);
        return result;
    }

    @Override
    public String toString() {
        return "PermissionState{" +
                "name='" + name + '\'' +
                ", granted=" + granted +
                '}';
    }
}
