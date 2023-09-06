package io.appmetrica.analytics.impl;

import android.util.Pair;
import androidx.annotation.NonNull;
import io.appmetrica.analytics.Revenue;
import io.appmetrica.analytics.coreutils.internal.StringUtils;
import io.appmetrica.analytics.coreutils.internal.WrapUtils;
import io.appmetrica.analytics.impl.utils.PublicLogger;
import io.appmetrica.analytics.impl.utils.limitation.EventLimitationProcessor;
import io.appmetrica.analytics.impl.utils.limitation.StringByBytesTrimmer;
import io.appmetrica.analytics.impl.utils.limitation.StringTrimmer;
import io.appmetrica.analytics.impl.utils.limitation.SubstituteTrimmer;
import io.appmetrica.analytics.impl.utils.limitation.Trimmer;
import io.appmetrica.analytics.protobuf.nano.MessageNano;

class RevenueWrapper {

    private static final String TRUNCATED_DATA_LINK_TO_TICKET = "<truncated data was not sent, see METRIKALIB-4568>";
    @NonNull
    private final Revenue mRevenue;

    private final Trimmer<String> mPayloadTrimmer;
    private final Trimmer<String> mReceiptDataTrimmer;
    private final Trimmer<String> mReceiptSignatureTrimmer;
    @NonNull private final PublicLogger mPublicLogger;

    RevenueWrapper(@NonNull Revenue revenue, @NonNull PublicLogger logger) {
        mPublicLogger = logger;
        mRevenue = revenue;
        mPayloadTrimmer = new StringByBytesTrimmer(
                EventLimitationProcessor.REVENUE_PAYLOAD_MAX_SIZE, "revenue payload", mPublicLogger
        );
        mReceiptDataTrimmer= new SubstituteTrimmer<String>(
                new StringByBytesTrimmer(EventLimitationProcessor.RECEIPT_DATA_MAX_SIZE, "receipt data",
                        mPublicLogger),
                TRUNCATED_DATA_LINK_TO_TICKET
        );
        mReceiptSignatureTrimmer = new SubstituteTrimmer<String>(
                new StringTrimmer(EventLimitationProcessor.RECEIPT_SIGNATURE_MAX_LENGTH, "receipt signature",
                        mPublicLogger),
                TRUNCATED_DATA_LINK_TO_TICKET
        );
    }

    @NonNull
    Pair<byte[], Integer> getDataToSend() {
        int bytesTruncated = 0;
        io.appmetrica.analytics.impl.protobuf.backend.Revenue proto =
            new io.appmetrica.analytics.impl.protobuf.backend.Revenue();
        proto.currency = mRevenue.currency.getCurrencyCode().getBytes();
        proto.priceMicros = mRevenue.priceMicros;
        proto.productId = StringUtils.stringToBytesForProtobuf(
                new StringTrimmer(EventLimitationProcessor.REVENUE_PRODUCT_ID_MAX_LENGTH,
                        "revenue productID", mPublicLogger)
                        .trim(mRevenue.productID)
        );
        proto.quantity = WrapUtils.getOrDefault(mRevenue.quantity, 1);
        proto.payload = StringUtils.stringToBytesForProtobuf(mPayloadTrimmer.trim(mRevenue.payload));
        if (Utils.isFieldSet(mRevenue.receipt)) {
            io.appmetrica.analytics.impl.protobuf.backend.Revenue.Receipt protoReceipt =
                    new io.appmetrica.analytics.impl.protobuf.backend.Revenue.Receipt();
            String data = mReceiptDataTrimmer.trim(mRevenue.receipt.data);
            if (EventLimitationProcessor.valueWasTrimmed(mRevenue.receipt.data, data)) {
                bytesTruncated += mRevenue.receipt.data.length();
            }
            String signature = mReceiptSignatureTrimmer.trim(mRevenue.receipt.signature);
            protoReceipt.data = StringUtils.stringToBytesForProtobuf(data);
            protoReceipt.signature = StringUtils.stringToBytesForProtobuf(signature);
            proto.receipt = protoReceipt;
        }
        return new Pair<byte[], Integer>(MessageNano.toByteArray(proto), bytesTruncated);
    }
}
