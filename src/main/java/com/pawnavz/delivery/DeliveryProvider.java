package com.pawnavz.delivery;

import com.pawnavz.entity.Order;

/**
 * Abstraction over a third-party delivery partner (Porter, Ola, ...).
 * <p>
 * Add a real integration by implementing this interface as a Spring {@code @Component};
 * {@link DeliveryService} discovers all implementations automatically, so the rest of the
 * application never changes when a new partner is added.
 */
public interface DeliveryProvider {

    /** Which partner this implementation talks to. */
    Order.DeliveryPartner partner();

    /** Request a delivery for the given order and return a provider-agnostic result. */
    DeliveryDispatchResult requestDelivery(Order order);
}
