package io.appmetrica.analytics.internal;

import android.os.Parcel;
import android.os.Parcelable;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import io.appmetrica.analytics.coreapi.internal.identifiers.IdentifierStatus;

/**
 * Contains information about identifiers
 */
public class IdentifiersResult implements Parcelable {

    @Nullable
    public final String id;
    @NonNull
    public final IdentifierStatus status;
    @Nullable
    public final String errorExplanation;

    public IdentifiersResult(@Nullable String id,
                             @NonNull IdentifierStatus status,
                             @Nullable String errorExplanation) {
        this.id = id;
        this.status = status;
        this.errorExplanation = errorExplanation;
    }

    public static final Creator<IdentifiersResult> CREATOR =
            new Creator<IdentifiersResult>() {

                public IdentifiersResult createFromParcel(Parcel srcObj) {
                    return new IdentifiersResult(
                            srcObj.readString(),
                            IdentifierStatus.from(srcObj.readString()),
                            srcObj.readString()
                    );
                }

                public IdentifiersResult[] newArray(int size) {
                    return new IdentifiersResult[size];
                }

            };

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(id);
        dest.writeString(status.getValue());
        dest.writeString(errorExplanation);
    }

    @Override
    public String toString() {
        return "IdentifiersResult{" +
                "id='" + id + '\'' +
                ", status=" + status +
                ", errorExplanation='" + errorExplanation + '\'' +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        IdentifiersResult that = (IdentifiersResult) o;

        if (id != null ? !id.equals(that.id) : that.id != null) return false;
        if (status != that.status) return false;
        return errorExplanation != null ?
                errorExplanation.equals(that.errorExplanation) :
                that.errorExplanation == null;
    }

    @Override
    public int hashCode() {
        int result = id != null ? id.hashCode() : 0;
        result = 31 * result + status.hashCode();
        result = 31 * result + (errorExplanation != null ? errorExplanation.hashCode() : 0);
        return result;
    }
}
