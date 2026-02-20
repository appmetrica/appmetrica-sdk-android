package io.appmetrica.analytics.impl.profile;

import io.appmetrica.analytics.impl.protobuf.backend.Userprofile;
import io.appmetrica.analytics.testutils.CommonTest;
import org.junit.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.nullable;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

public class CommonSavingStrategyTest extends CommonTest {

    private final AttributeSaver mSaver = mock(AttributeSaver.class);
    private final CommonSavingStrategy mStrategy = new CommonSavingStrategy(mSaver);

    @Test
    public void testInvalid() {
        AttributeFactory factory = mock(AttributeFactory.class);
        mStrategy.save(mock(UserProfileStorage.class), null, factory);
        verify(factory, times(1)).createAttribute();
        verify(mSaver, times(1)).save(any(UserProfileStorage.class), nullable(Userprofile.Profile.Attribute.class));
    }

    @Test
    public void testValid() {
        Userprofile.Profile.Attribute attribute = UserProfilesTestUtils.createEmpty();
        Userprofile.Profile.AttributeMetaInfo metaInfo = attribute.metaInfo;
        metaInfo.setIfUndefined = true;

        Userprofile.Profile.Attribute newAttribute = mStrategy.save(mock(UserProfileStorage.class), attribute, mock(AttributeFactory.class));
        assertThat(attribute).isSameAs(newAttribute);
        assertThat(metaInfo).isNotSameAs(newAttribute.metaInfo);
        assertThat(newAttribute.metaInfo.setIfUndefined).isFalse();
    }

}
