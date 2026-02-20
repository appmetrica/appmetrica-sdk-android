package io.appmetrica.analytics.profile;

import androidx.annotation.NonNull;
import io.appmetrica.analytics.impl.profile.BaseSavingStrategy;
import io.appmetrica.analytics.impl.profile.ResetUpdatePatcher;
import io.appmetrica.analytics.impl.profile.SetIfUndefinedSavingStrategy;
import io.appmetrica.analytics.impl.profile.StringUpdatePatcher;
import io.appmetrica.analytics.impl.profile.UserProfileUpdatePatcher;
import io.appmetrica.analytics.impl.protobuf.backend.Userprofile;
import io.appmetrica.analytics.impl.utils.limitation.DummyTrimmer;
import io.appmetrica.analytics.impl.utils.validation.DummyValidator;
import io.appmetrica.analytics.testutils.CommonTest;
import java.util.Calendar;
import java.util.GregorianCalendar;
import org.junit.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;

public class BirthDateAttributeTest extends CommonTest {

    @Test
    public void testSetYear() {
        StringUpdatePatcher patcher = cast(new BirthDateAttribute().withBirthDate(1997).getUserProfileUpdatePatcher());
        assertThat(patcher.getValue()).isEqualTo("1997");
        assertThat(patcher.getAttributeSavingStrategy()).isNotInstanceOf(SetIfUndefinedSavingStrategy.class);
    }

    @Test
    public void testSetYearIfUndefined() {
        StringUpdatePatcher patcher = cast(new BirthDateAttribute().withBirthDateIfUndefined(1997).getUserProfileUpdatePatcher());
        assertThat(patcher.getValue()).isEqualTo("1997");
        assertThat(patcher.getAttributeSavingStrategy()).isInstanceOf(SetIfUndefinedSavingStrategy.class);
    }

    @Test
    public void testSetYearAndMonth() {
        StringUpdatePatcher patcher = cast(new BirthDateAttribute().withBirthDate(1997, 9).getUserProfileUpdatePatcher());
        assertThat(patcher.getValue()).isEqualTo("1997-09");
        assertThat(patcher.getAttributeSavingStrategy()).isNotInstanceOf(SetIfUndefinedSavingStrategy.class);
    }

    @Test
    public void testSetYearAndMonthIfUndefined() {
        StringUpdatePatcher patcher = cast(new BirthDateAttribute().withBirthDateIfUndefined(1997, 9).getUserProfileUpdatePatcher());
        assertThat(patcher.getValue()).isEqualTo("1997-09");
        assertThat(patcher.getAttributeSavingStrategy()).isInstanceOf(SetIfUndefinedSavingStrategy.class);
    }

    @Test
    public void testSetYearMonthAndDay() {
        StringUpdatePatcher patcher = cast(new BirthDateAttribute().withBirthDate(1997, 9, 23).getUserProfileUpdatePatcher());
        assertThat(patcher.getValue()).isEqualTo("1997-09-23");
        assertThat(patcher.getAttributeSavingStrategy()).isNotInstanceOf(SetIfUndefinedSavingStrategy.class);
    }

    @Test
    public void testSetYearMonthAndDayIfUndefined() {
        StringUpdatePatcher patcher = cast(new BirthDateAttribute().withBirthDateIfUndefined(1997, 9, 23).getUserProfileUpdatePatcher());
        assertThat(patcher.getValue()).isEqualTo("1997-09-23");
        assertThat(patcher.getAttributeSavingStrategy()).isInstanceOf(SetIfUndefinedSavingStrategy.class);
    }

    @Test
    public void testSetCalendar() {
        Calendar calendar = new GregorianCalendar();
        calendar.set(1997, Calendar.SEPTEMBER, 23);
        StringUpdatePatcher patcher = cast(new BirthDateAttribute().withBirthDate(calendar).getUserProfileUpdatePatcher());
        assertThat(patcher.getValue()).isEqualTo("1997-09-23");
        assertThat(patcher.getAttributeSavingStrategy()).isNotInstanceOf(SetIfUndefinedSavingStrategy.class);
    }

    @Test
    public void testSetCalendarIfUndefined() {
        Calendar calendar = new GregorianCalendar();
        calendar.set(1997, Calendar.SEPTEMBER, 23);
        StringUpdatePatcher patcher = cast(new BirthDateAttribute().withBirthDateIfUndefined(calendar).getUserProfileUpdatePatcher());
        assertThat(patcher.getValue()).isEqualTo("1997-09-23");
        assertThat(patcher.getAttributeSavingStrategy()).isInstanceOf(SetIfUndefinedSavingStrategy.class);
    }

    @Test
    public void testSetAge() {
        StringUpdatePatcher patcher = cast(new BirthDateAttribute().withAge(20).getUserProfileUpdatePatcher());
        assertThat(patcher.getValue()).isEqualTo(String.valueOf(new GregorianCalendar().get(Calendar.YEAR) - 20));
        assertThat(patcher.getAttributeSavingStrategy()).isNotInstanceOf(SetIfUndefinedSavingStrategy.class);
    }

    @Test
    public void testSetAgeIfUndefined() {
        StringUpdatePatcher patcher = cast(new BirthDateAttribute().withAgeIfUndefined(20).getUserProfileUpdatePatcher());
        assertThat(patcher.getValue()).isEqualTo(String.valueOf(new GregorianCalendar().get(Calendar.YEAR) - 20));
        assertThat(patcher.getAttributeSavingStrategy()).isInstanceOf(SetIfUndefinedSavingStrategy.class);
    }

    @Test
    public void testReset() {
        ResetUpdatePatcher patcher = (ResetUpdatePatcher) new BirthDateAttribute().withValueReset().getUserProfileUpdatePatcher();
        assertThat(patcher.getKey()).isEqualTo("appmetrica_birth_date");
        assertThat(patcher.getType()).isEqualTo(Userprofile.Profile.Attribute.STRING);
    }

    @Test
    public void testDummyTrimmer() {
        assertThat(((StringUpdatePatcher) new BirthDateAttribute().createUpdatePatcher(
                GregorianCalendar.getInstance(), "", mock(BaseSavingStrategy.class)
        ).getUserProfileUpdatePatcher()).getValueTrimmer()).isExactlyInstanceOf(DummyTrimmer.class);
    }

    @Test
    public void testDummyValidator() {
        assertThat(((StringUpdatePatcher) new BirthDateAttribute().createUpdatePatcher(
                GregorianCalendar.getInstance(), "", mock(BaseSavingStrategy.class)
        ).getUserProfileUpdatePatcher()).getKeyValidator()).isExactlyInstanceOf(DummyValidator.class);
    }

    private StringUpdatePatcher cast(@NonNull UserProfileUpdatePatcher patcher) {
        return (StringUpdatePatcher) patcher;
    }

}
