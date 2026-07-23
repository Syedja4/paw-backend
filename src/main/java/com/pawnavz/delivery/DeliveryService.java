package com.pawnavz.delivery;

import com.pawnavz.entity.Order;
import com.pawnavz.exception.BadRequestException;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.EnumMap;
import java.util.Map;

/**
 * Routes a delivery request to the appropriate {@link DeliveryProvider}.
 * All providers on the classpath are injected and indexed by partner, so adding
 * a Porter/Ola integration requires no change here.
 */
@Service
public class DeliveryService {

    private final Map<Order.DeliveryPartner, DeliveryProvider> providers =
            new EnumMap<>(Order.DeliveryPartner.class);

    public DeliveryService(List<DeliveryProvider> providerBeans) {
        for (DeliveryProvider provider : providerBeans) {
            providers.put(provider.partner(), provider);
        }
    }

    /** Default partner used when the caller does not specify one. */
    public DeliveryDispatchResult requestDelivery(Order order) {
        return requestDelivery(order, Order.DeliveryPartner.MANUAL);
    }

    public DeliveryDispatchResult requestDelivery(Order order, Order.DeliveryPartner partner) {
        DeliveryProvider provider = providers.get(partner);
        if (provider == null) {
            throw new BadRequestException("No delivery provider available for partner: " + partner);
        }
        return provider.requestDelivery(order);
    }
}
