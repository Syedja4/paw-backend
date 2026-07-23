package com.pawnavz.delivery;

import com.pawnavz.entity.Order;
import org.springframework.stereotype.Component;

import java.util.UUID;

/**
 * Default provider used until a real Porter/Ola integration is wired in.
 * It simply records a delivery request so the order can progress; a future
 * PorterDeliveryProvider / OlaDeliveryProvider can be dropped in alongside it.
 */
@Component
public class ManualDeliveryProvider implements DeliveryProvider {

    @Override
    public Order.DeliveryPartner partner() {
        return Order.DeliveryPartner.MANUAL;
    }

    @Override
    public DeliveryDispatchResult requestDelivery(Order order) {
        return DeliveryDispatchResult.builder()
                .partner(Order.DeliveryPartner.MANUAL)
                .trackingId("MAN-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase())
                .status(Order.DeliveryStatus.REQUESTED)
                .message("Delivery request recorded. Integrate Porter/Ola to auto-dispatch.")
                .build();
    }
}
