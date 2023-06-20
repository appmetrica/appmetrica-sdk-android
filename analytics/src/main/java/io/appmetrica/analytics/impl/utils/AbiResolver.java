package io.appmetrica.analytics.impl.utils;

import android.annotation.SuppressLint;
import android.os.Build;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import io.appmetrica.analytics.coreutils.internal.AndroidUtils;
import java.util.Set;

public class AbiResolver {

    @NonNull
    private final Set<String> supportedAbi;

    public AbiResolver(@NonNull Set<String> supportedAbi) {
        this.supportedAbi = supportedAbi;
    }

    @SuppressLint("NewApi")
    @Nullable
    public String getAbi() {
        if (AndroidUtils.isApiAchieved(Build.VERSION_CODES.LOLLIPOP)) {
            return getAbiLollipop();
        } else {
            return lookupAbi(new String[] { Build.CPU_ABI, Build.CPU_ABI2} );
        }
    }

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    @Nullable
    private String getAbiLollipop() {
        String abi64 = lookupAbi(Build.SUPPORTED_64_BIT_ABIS);
        if (abi64 != null) {
            return abi64;
        }
        return lookupAbi(Build.SUPPORTED_32_BIT_ABIS);
    }

    @Nullable
    private String lookupAbi(String[] from) {
        for (String abi: from) {
            if (supportedAbi.contains(abi)) {
                return abi;
            }
        }
        return null;
    }

}
