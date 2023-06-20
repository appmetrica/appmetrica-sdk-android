package io.appmetrica.analytics.impl.ecommerce.client.converter;

import androidx.annotation.NonNull;
import io.appmetrica.analytics.impl.protobuf.backend.Ecommerce;
import io.appmetrica.analytics.protobuf.nano.CodedOutputByteBufferNano;

class ProtoMessageSizeCalculator {

    int computeAdditionalNestedSize(@NonNull Ecommerce.ECommerceEvent eCommerceEvent) {
        int size = 0;
        if (eCommerceEvent.type != new Ecommerce.ECommerceEvent().type) {
            size += CodedOutputByteBufferNano.computeInt32Size(1, eCommerceEvent.type);
        }
        if (eCommerceEvent.shownScreenInfo != null) {
            size += CodedOutputByteBufferNano.computeMessageSize(2, eCommerceEvent.shownScreenInfo);
        }
        if (eCommerceEvent.shownProductCardInfo != null) {
            size += CodedOutputByteBufferNano.computeMessageSize(3, eCommerceEvent.shownProductCardInfo);
        }
        if (eCommerceEvent.shownProductDetailsInfo != null) {
            size += CodedOutputByteBufferNano.computeMessageSize(4, eCommerceEvent.shownProductDetailsInfo);
        }
        if (eCommerceEvent.cartActionInfo != null) {
            size += CodedOutputByteBufferNano.computeMessageSize(5, eCommerceEvent.cartActionInfo);
        }
        if (eCommerceEvent.orderInfo != null) {
            size += CodedOutputByteBufferNano.computeMessageSize(6, eCommerceEvent.orderInfo);
        }
        return size;
    }

    /**
     * @see CodedOutputByteBufferNano#writeMessageNoTag
     * @see CodedOutputByteBufferNano#writeRawVarint32
     */
    int computeAdditionalNestedSize(@NonNull Ecommerce.ECommerceEvent.OrderCartItem cartItem) {
        int tagSize = CodedOutputByteBufferNano.computeTagSize(4);
        int messageNoTagSize = CodedOutputByteBufferNano.computeMessageSizeNoTag(cartItem);
        //In addition to the message size, we have to to consider the size of the integer that
        // describes the size of that message in the root message
        int selfSizeLengthDescription = (messageNoTagSize & -128) == 0 ? 0 :
            CodedOutputByteBufferNano.computeRawVarint32Size(messageNoTagSize);
        return tagSize + messageNoTagSize + selfSizeLengthDescription;
    }

}
