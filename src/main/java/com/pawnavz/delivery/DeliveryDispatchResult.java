package com.pawnavz.delivery;

import com.pawnavz.entity.Order;
import lombok.Builder;
import lombok.Data;

/**
 * Provider-agnostic result of requesting a delivery from a third-party logistics partner.
 */
@Data
@Builder
public class DeliveryDispatchResult {
    private Order.DeliveryPartner partner;
    private String trackingId;
    private Order.DeliveryStatus status;
    private String message;
}
