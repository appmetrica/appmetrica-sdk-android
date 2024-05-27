package io.appmetrica.analytics.impl.startup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import io.appmetrica.analytics.impl.ClidsInfoStorage;
import io.appmetrica.analytics.impl.DistributionSource;
import io.appmetrica.analytics.impl.Utils;
import io.appmetrica.analytics.impl.clids.ClidsInfo;
import io.appmetrica.analytics.impl.utils.StartupUtils;
import io.appmetrica.analytics.logger.appmetrica.internal.DebugLogger;
import java.util.Map;

public class ClidsStateChecker {

    private static final String TAG = "[ClidsStateChecker]";

    public boolean doChosenClidsForRequestMatchLastRequestClids(@Nullable Map<String, String> clidsFromClient,
                                                                @NonNull StartupState startupState,
                                                                @NonNull ClidsInfoStorage clidsStorage) {
        final Map<String, String> chosenClidsForVerification = clidsStorage
                .updateAndRetrieveData(new ClidsInfo.Candidate(clidsFromClient, DistributionSource.APP)).getClids();
        if (Utils.isNullOrEmpty(chosenClidsForVerification)) {
            return true;
        }
        Map<String, String> prevRequestClids = StartupUtils.decodeClids(startupState.getLastChosenForRequestClids());
        DebugLogger.INSTANCE.info(
            TAG,
            "doChosenClidsForRequestMatchLastRequestClids? clids: from client = %s, chosen = %s " +
                "and prev request %s",
            clidsFromClient,
            chosenClidsForVerification,
            prevRequestClids
        );
        return chosenClidsForVerification.equals(prevRequestClids);
    }

    public boolean doRequestClidsMatchResponseClids(@Nullable Map<String, String> chosenForRequestClids,
                                                    @Nullable String responseClidsString) {
        DebugLogger.INSTANCE.info(
            TAG,
            "doRequestClidsMatchResponseClids? chosenForRequestClids: %s, responseClidsString: %s",
            chosenForRequestClids,
            responseClidsString
        );
        Map<String, String> responseClids = StartupUtils.decodeClids(responseClidsString);
        if (Utils.isNullOrEmpty(chosenForRequestClids)) {
            return Utils.isNullOrEmpty(responseClids);
        }
        return responseClids.equals(chosenForRequestClids);
    }

    public boolean doClientClidsMatchClientClidsForRequest(@Nullable Map<String, String> clidsForClient,
                                                           @Nullable Map<String, String> clientClidsForRequest) {
        DebugLogger.INSTANCE.info(
            TAG,
            "doClientClidsMatchClientClidsForRequest? clidsForClient: %s, clientClidsForRequest: %s",
            clidsForClient,
            clientClidsForRequest
        );
        if (Utils.isNullOrEmpty(clidsForClient)) {
            return Utils.isNullOrEmpty(clientClidsForRequest);
        }
        return clidsForClient.equals(clientClidsForRequest);
    }
}
