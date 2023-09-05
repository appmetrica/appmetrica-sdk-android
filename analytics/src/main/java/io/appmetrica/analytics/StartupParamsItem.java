package io.appmetrica.analytics;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import java.util.Objects;

/**
 * Startup value with status and error description.
 */
public final class StartupParamsItem {

    /**
     * Startup value.
     */
    @Nullable
    private final String id;
    /**
     * Startup value status.
     */
    @NonNull
    private final StartupParamsItemStatus status;
    /**
     * Startup value error details.
     */
    @Nullable
    private final String errorDetails;

    /**
     * Constructor for {@link StartupParamsItem}.
     *
     * @param id Value
     * @param status Status
     * @param errorDetails Error description
     */
    public StartupParamsItem(
        @Nullable final String id,
        @NonNull final StartupParamsItemStatus status,
        @Nullable final String errorDetails
    ) {
        this.id = id;
        this.status = status;
        this.errorDetails = errorDetails;
    }

    /**
     * @return startup value if it is present or null otherwise.
     */
    @Nullable
    public String getId() {
        return id;
    }

    /**
     * @return startup value status.
     */
    @NonNull
    public StartupParamsItemStatus getStatus() {
        return status;
    }

    /**
     * @return startup value error details.
     */
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
