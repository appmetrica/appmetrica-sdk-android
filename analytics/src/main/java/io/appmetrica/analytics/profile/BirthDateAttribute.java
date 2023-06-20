package io.appmetrica.analytics.profile;

import android.annotation.SuppressLint;
import androidx.annotation.NonNull;
import androidx.annotation.VisibleForTesting;
import io.appmetrica.analytics.impl.profile.BaseSavingStrategy;
import io.appmetrica.analytics.impl.profile.CommonSavingStrategy;
import io.appmetrica.analytics.impl.profile.Constants;
import io.appmetrica.analytics.impl.profile.CustomAttribute;
import io.appmetrica.analytics.impl.profile.ResetUpdatePatcher;
import io.appmetrica.analytics.impl.profile.SetIfUndefinedSavingStrategy;
import io.appmetrica.analytics.impl.profile.SimpleSaver;
import io.appmetrica.analytics.impl.profile.StringUpdatePatcher;
import io.appmetrica.analytics.impl.profile.UserProfileUpdatePatcher;
import io.appmetrica.analytics.impl.protobuf.backend.Userprofile;
import io.appmetrica.analytics.impl.utils.limitation.DummyTrimmer;
import io.appmetrica.analytics.impl.utils.validation.DummyValidator;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.Locale;

/**
 * The birth date attribute class.
 * It enables linking user birth date with the profile.
 * <p>Overloaded methods allow you to set an approximate date of birth for a user.</p>
 *
 * <p><b>EXAMPLE:</b></p>
 * <pre>
 *     {@code UserProfile userProfile = new UserProfile.Builder()
 *                     .apply(Attribute.birthDate().withAge(27))
 *                     .build();}
 * </pre>
 */
public class BirthDateAttribute {

    private static final String YEAR_PATTERN = "yyyy";
    private static final String MONTH_PATTERN = "MM";
    private static final String DAY_PATTERN = "dd";

    private final CustomAttribute mCustomAttribute;

    BirthDateAttribute() {
        mCustomAttribute = new CustomAttribute(
                Constants.APPMETRICA_PREFIX + "birth_date",
                new DummyValidator<String>(),
                new SimpleSaver()
        );
    }

    /**
     * Updates the birth date attribute with the specified value.
     * This methods sets year of the birth date.
     *
     * <p><b>NOTE:</b> It overwrites the existing value.</p>
     *
     * @param year Year of birth
     *
     * @return The {@link io.appmetrica.analytics.profile.UserProfileUpdate} object
     */
    public UserProfileUpdate<? extends UserProfileUpdatePatcher> withBirthDate(int year) {
        return createUpdatePatcher(withYear(year), YEAR_PATTERN, new CommonSavingStrategy(mCustomAttribute.getSaver()));
    }

    /**
     * Updates the birth date attribute with the specified value only if the attribute value is undefined.
     * The method doesn't affect the value if it has been set earlier.
     * <p>This methods sets year of the birth date.</p>
     *
     * @param year Year of birth
     *
     * @return The {@link io.appmetrica.analytics.profile.UserProfileUpdate} object
     */
    public UserProfileUpdate<? extends UserProfileUpdatePatcher> withBirthDateIfUndefined(int year) {
        return createUpdatePatcher(
                withYear(year),
                YEAR_PATTERN,
                new SetIfUndefinedSavingStrategy(mCustomAttribute.getSaver())
        );
    }

    /**
     * Updates the birth date attribute with the specified values.
     * This method sets the year and month of the birth date.
     *
     * <p><b>NOTE:</b> It overwrites the existing value.</p>
     *
     * @param year Year of birth
     * @param month Month of birth
     *
     * @return The {@link io.appmetrica.analytics.profile.UserProfileUpdate} object
     */
    public UserProfileUpdate<? extends UserProfileUpdatePatcher> withBirthDate(int year, int month) {
        return createUpdatePatcher(
                withYearAndMonth(year, month),
                YEAR_PATTERN + "-" + MONTH_PATTERN,
                new CommonSavingStrategy(mCustomAttribute.getSaver())
        );
    }

    /**
     * Updates the birth date attribute with the specified values only if the attribute value is undefined.
     * The method doesn't affect the value if it has been set earlier.
     * <p>This method sets the year and month of the birth date.</p>
     *
     * @param year Year of birth
     * @param month Month of birth
     *
     * @return The {@link io.appmetrica.analytics.profile.UserProfileUpdate} object
     */
    public UserProfileUpdate<? extends UserProfileUpdatePatcher> withBirthDateIfUndefined(int year, int month) {
        return createUpdatePatcher(
                withYearAndMonth(year, month),
                YEAR_PATTERN + "-" + MONTH_PATTERN,
                new SetIfUndefinedSavingStrategy(mCustomAttribute.getSaver())
        );
    }

    /**
     * Updates the birth date attribute with the specified values.
     * <p>This methods sets year, month and day of the month of the birth date.</p>
     *
     * <p><b>NOTE:</b> It overwrites the existing value.</p>
     *
     * @param year Year of birth
     * @param month Month of birth
     * @param dayOfMonth Day of the month of birth
     *
     * @return The {@link io.appmetrica.analytics.profile.UserProfileUpdate} object
     */
    public UserProfileUpdate<? extends UserProfileUpdatePatcher> withBirthDate(int year, int month, int dayOfMonth) {
        return createUpdatePatcher(
                withYearMonthAndDay(year, month, dayOfMonth),
                YEAR_PATTERN + "-"  + MONTH_PATTERN + "-" + DAY_PATTERN,
                new CommonSavingStrategy(mCustomAttribute.getSaver())
        );
    }

    /**
     * Updates the birth date attribute with the specified values only if the attribute value is undefined.
     * The method doesn't affect the value if it has been set earlier.
     * <p>This methods sets year, month and day of the month of the birth date.</p>
     *
     * @param year Year of birth
     * @param month Month of birth
     * @param dayOfMonth Day of the month of birth
     *
     * @return The {@link io.appmetrica.analytics.profile.UserProfileUpdate} object
     */
    public UserProfileUpdate<? extends UserProfileUpdatePatcher> withBirthDateIfUndefined(int year,
                                                                                          int month,
                                                                                          int dayOfMonth) {
        return createUpdatePatcher(
                withYearMonthAndDay(year, month, dayOfMonth),
                YEAR_PATTERN + "-" + MONTH_PATTERN + "-" + DAY_PATTERN,
                new SetIfUndefinedSavingStrategy(mCustomAttribute.getSaver())
        );
    }

    /**
     * Updates the birth date attribute with the specified value.
     * It calculates the birth year by using the following formula:
     * Birth Year = currentYear - age.
     *
     * <p><b>NOTE:</b> It overwrites the existing value.</p>
     *
     * @param age Age of the user
     *
     * @return The {@link io.appmetrica.analytics.profile.UserProfileUpdate} object
     */
    public UserProfileUpdate<? extends UserProfileUpdatePatcher> withAge(int age) {
        return createUpdatePatcher(
                withYear(Calendar.getInstance(Locale.US).get(Calendar.YEAR) - age),
                YEAR_PATTERN,
                new CommonSavingStrategy(mCustomAttribute.getSaver())
        );
    }

    /**
     * Updates the birth date attribute with the specified value only if the attribute value is undefined.
     * The method doesn't affect the value if it has been set earlier.
     *
     * <p>It calculates the birth year by using the following formula:
     * Birth Year = currentYear - age.
     *
     * @param age Age of the user
     *
     * @return The {@link io.appmetrica.analytics.profile.UserProfileUpdate} object
     */
    public UserProfileUpdate<? extends UserProfileUpdatePatcher> withAgeIfUndefined(int age) {
        return createUpdatePatcher(
                withYear(Calendar.getInstance(Locale.US).get(Calendar.YEAR) - age),
                YEAR_PATTERN,
                new SetIfUndefinedSavingStrategy(mCustomAttribute.getSaver())
        );
    }

    /**
     * Updates the birth date attribute with the specified value.
     *
     * <p><b>NOTE:</b> It overwrites the existing value.</p>
     *
     * @param date Date of birth
     *
     * @return The {@link io.appmetrica.analytics.profile.UserProfileUpdate} object
     */
    public UserProfileUpdate<? extends UserProfileUpdatePatcher> withBirthDate(@NonNull Calendar date) {
        return createUpdatePatcher(
                date,
                YEAR_PATTERN + "-" + MONTH_PATTERN + "-" + DAY_PATTERN,
                new CommonSavingStrategy(mCustomAttribute.getSaver())
        );
    }

    /**
     * Updates the birth date attribute with the specified value only if the attribute value is undefined.
     * The method doesn't affect the value if it has been set earlier.
     *
     * @param date Date of birth
     *
     * @return The {@link io.appmetrica.analytics.profile.UserProfileUpdate} object
     */
    public UserProfileUpdate<? extends UserProfileUpdatePatcher> withBirthDateIfUndefined(@NonNull Calendar date) {
        return createUpdatePatcher(
                date,
                YEAR_PATTERN + "-" + MONTH_PATTERN + "-" + DAY_PATTERN,
                new SetIfUndefinedSavingStrategy(mCustomAttribute.getSaver())
        );
    }

    /**
     * Resets the birth date attribute value.
     *
     * @return The {@link io.appmetrica.analytics.profile.UserProfileUpdate} object
     */
    public UserProfileUpdate<? extends UserProfileUpdatePatcher> withValueReset() {
        return new UserProfileUpdate<UserProfileUpdatePatcher>(
                new ResetUpdatePatcher(
                        Userprofile.Profile.Attribute.STRING, mCustomAttribute.getKey(),
                        new DummyValidator<String>(),
                        new SimpleSaver()
                )
        );
    }

    private Calendar withYear(int year) {
        Calendar date = new GregorianCalendar();
        date.set(Calendar.YEAR, year);
        return date;
    }

    private Calendar withYearAndMonth(int year, int month) {
        Calendar date = new GregorianCalendar();
        date.set(Calendar.YEAR, year);
        date.set(Calendar.MONTH, month - 1);
        //date is initiated with current date, so otherwise there will be problems on 31st
        date.set(Calendar.DAY_OF_MONTH, 1);
        return date;
    }

    private Calendar withYearMonthAndDay(int year, int month, int dayOfMonth) {
        Calendar date = new GregorianCalendar();
        date.set(Calendar.YEAR, year);
        date.set(Calendar.MONTH, month - 1);
        date.set(Calendar.DAY_OF_MONTH, dayOfMonth);
        return date;
    }

    @SuppressLint("SimpleDateFormat")
    @VisibleForTesting
    UserProfileUpdate<? extends UserProfileUpdatePatcher> createUpdatePatcher(
            @NonNull Calendar date,
            @NonNull String pattern,
            @NonNull BaseSavingStrategy attributeSavingStrategy) {
        return new UserProfileUpdate<UserProfileUpdatePatcher>(
                new StringUpdatePatcher(
                        mCustomAttribute.getKey(),
                        new SimpleDateFormat(pattern).format(date.getTime()),
                        new DummyTrimmer<String>(),
                        new DummyValidator<String>(),
                        attributeSavingStrategy
                )
        );
    }

}
