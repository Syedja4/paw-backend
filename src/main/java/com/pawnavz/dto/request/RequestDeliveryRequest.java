package com.pawnavz.dto.request;

import com.pawnavz.entity.Order;
import lombok.Data;

/**
 * Optional body for POST /api/v1/admin/orders/{id}/request-delivery.
 * When {@code partner} is omitted the default (MANUAL) provider is used.
 */
@Data
public class RequestDeliveryRequest {

    private Order.DeliveryPartner partner;
}
