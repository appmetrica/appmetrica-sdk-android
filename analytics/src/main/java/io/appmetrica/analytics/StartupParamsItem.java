package io.appmetrica.analytics;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import java.util.Objects;

public final class StartupParamsItem {

    @Nullable
    private final String id;
    @NonNull
    private final StartupParamsItemStatus status;
    @Nullable
    private final String errorDetails;

    public StartupParamsItem(
        @Nullable final String id,
        @NonNull final StartupParamsItemStatus status,
        @Nullable final String errorDetails
    ) {
        this.id = id;
        this.status = status;
        this.errorDetails = errorDetails;
    }

    @Nullable
    public String getId() {
        return id;
    }

    @NonNull
    public StartupParamsItemStatus getStatus() {
        return status;
    }

    @Nullable
    public String getErrorDetails() {
        return errorDetails;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        StartupParamsItem that = (StartupParamsItem) o;
        return Objects.equals(id, that.id) && status == that.status && Objects.equals(errorDetails, that.errorDetails);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, status, errorDetails);
    }

    @NonNull
    @Override
    public String toString() {
        return "StartupParamsItem{" +
            "id='" + id + '\'' +
            ", status=" + status +
            ", errorDetails='" + errorDetails + '\'' +
            '}';
    }
}
