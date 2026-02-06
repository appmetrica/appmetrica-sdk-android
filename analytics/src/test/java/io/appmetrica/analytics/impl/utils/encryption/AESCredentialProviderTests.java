package io.appmetrica.analytics.impl.utils.encryption;

import android.content.Context;
import io.appmetrica.analytics.impl.GlobalServiceLocator;
import io.appmetrica.analytics.testutils.CommonTest;
import io.appmetrica.analytics.testutils.ContextRule;
import java.util.Arrays;
import java.util.Collection;
import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

@RunWith(Parameterized.class)
public class AESCredentialProviderTests extends CommonTest {

    private final String mPackageName;
    private final byte[] mPassword;
    private final byte[] mIV;
    private AESCredentialProvider mAESCredentialProvider;

    @Parameterized.Parameters(name = "Return expected password and IV for package name = {0}")
    public static Collection<Object[]> data() {
        return Arrays.asList(new Object[][]{
            {
                "com.yandex.test.package.name",
                new byte[]{73, -106, -125, 31, 77, -66, 83, -33, -8, -96, 19, -68, 80, 109, -11, -47},
                new byte[]{-108, -106, 25, 95, -44, -21, 17, -7, -116, -79, 104, -3, -45, 64, 24, -82}
            },
            {
                "c.t.p.n",
                new byte[]{119, 123, 21, 76, -43, 75, -24, -123, -121, 31, 81, 49, 97, -73, -103, -124},
                new byte[]{-57, -1, 44, 16, -94, 34, 106, 74, 121, -40, 32, -81, -72, 117, -88, -93},
            },
            {
                "",
                new byte[]{-44, 29, -116, -39, -113, 0, -78, 4, -23, -128, 9, -104, -20, -8, 66, 126},
                new byte[]{-44, 29, -116, -39, -113, 0, -78, 4, -23, -128, 9, -104, -20, -8, 66, 126}
            }
        });
    }

    public AESCredentialProviderTests(final String packageName,
                                      final byte[] password,
                                      final byte[] iv) {
        mPackageName = packageName;
        mPassword = password;
        mIV = iv;
    }

    @Rule
    public ContextRule contextRule = new ContextRule();

    @Before
    public void setUp() {
        Context context = contextRule.getContext();
        when(context.getPackageName()).thenReturn(mPackageName);
        mAESCredentialProvider = new AESCredentialProvider(context);
    }

    @After
    public void tearDown() {
        GlobalServiceLocator.destroy();
    }

    @Test
    public void testPassword() {
        assertThat(mAESCredentialProvider.getPassword()).isEqualTo(mPassword);
    }

    @Test
    public void testIV() {
        assertThat(mAESCredentialProvider.getIV()).isEqualTo(mIV);
    }
}
