package com.pawnavz.service.admin;

import com.pawnavz.entity.Order;
import com.pawnavz.entity.Shop;
import com.pawnavz.entity.User;
import com.pawnavz.repository.OrderRepository;
import com.pawnavz.repository.ProductRepository;
import com.pawnavz.repository.ShopRepository;
import com.pawnavz.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class AdminStatsService {

    private final UserRepository userRepository;
    private final ShopRepository shopRepository;
    private final OrderRepository orderRepository;
    private final ProductRepository productRepository;

    public Map<String, Object> getOverview() {
        System.out.println("ADMIN API HIT");
        try {
            LocalDateTime todayStart = LocalDateTime.now().toLocalDate().atStartOfDay();
            Map<String, Object> stats = new LinkedHashMap<>();
            stats.put("totalUsers", safeLong(userRepository.countByRole(User.Role.USER)));
            stats.put("totalShops", safeLong(shopRepository.count()));
            stats.put("totalProducts", safeLong(productRepository.count()));
            stats.put("totalOrders", safeLong(orderRepository.count()));
            stats.put("pendingOrders", safeLong(orderRepository.countByStatus(Order.OrderStatus.PENDING)));
            stats.put("assignedOrders", safeLong(orderRepository.countByStatus(Order.OrderStatus.SHIPPED)));
            stats.put("outForDeliveryOrders", safeLong(orderRepository.countByStatus(Order.OrderStatus.OUT_FOR_DELIVERY)));
            stats.put("deliveredOrders", safeLong(orderRepository.countByStatus(Order.OrderStatus.DELIVERED)));
            stats.put("cancelledOrders", safeLong(orderRepository.countByStatus(Order.OrderStatus.CANCELLED)));
            stats.put("activeShops", safeLong(shopRepository.countByStatus(Shop.ShopStatus.ACTIVE)));
            stats.put("totalRevenue", safeAmount(orderRepository.sumTotalRevenue()));
            stats.put("revenueToday", safeAmount(orderRepository.sumRevenueSince(todayStart)));
            stats.put("ordersToday", safeLong(orderRepository.countByCreatedAtAfter(todayStart)));
            return stats;
        } catch (Exception e) {
            e.printStackTrace();
            return defaultOverview();
        }
    }

    public Map<String, Object> getRevenueStats() {
        System.out.println("ADMIN API HIT");
        try {
            LocalDateTime thisMonthStart = LocalDateTime.now().withDayOfMonth(1).toLocalDate().atStartOfDay();
            LocalDateTime lastMonthStart = thisMonthStart.minusMonths(1);
            LocalDateTime last30Days = LocalDateTime.now().minusDays(30);

            List<Object[]> daily = Optional.ofNullable(orderRepository.dailyRevenueSince(last30Days))
                    .orElse(Collections.emptyList());
            List<Map<String, Object>> dailyRevenue = new ArrayList<>();
            for (Object[] row : daily) {
                Map<String, Object> point = new LinkedHashMap<>();
                point.put("date", row != null && row.length > 0 && row[0] != null ? row[0].toString() : "");
                point.put("amount", row != null && row.length > 1 && row[1] != null ? row[1] : BigDecimal.ZERO);
                point.put("orders", row != null && row.length > 2 && row[2] != null ? ((Number) row[2]).longValue() : 0L);
                dailyRevenue.add(point);
            }

            Map<String, Object> stats = new LinkedHashMap<>();
            stats.put("totalRevenue", safeAmount(orderRepository.sumTotalRevenue()));
            stats.put("revenueThisMonth", safeAmount(orderRepository.sumRevenueSince(thisMonthStart)));
            stats.put("revenueLastMonth", safeAmount(orderRepository.sumRevenueSince(lastMonthStart)));
            stats.put("dailyRevenue", dailyRevenue);
            return stats;
        } catch (Exception e) {
            e.printStackTrace();
            Map<String, Object> fallback = new LinkedHashMap<>();
            fallback.put("totalRevenue", BigDecimal.ZERO);
            fallback.put("revenueThisMonth", BigDecimal.ZERO);
            fallback.put("revenueLastMonth", BigDecimal.ZERO);
            fallback.put("dailyRevenue", Collections.emptyList());
            return fallback;
        }
    }

    public Map<String, Object> getOrderStats() {
        System.out.println("ADMIN API HIT");
        try {
            LocalDateTime todayStart = LocalDateTime.now().toLocalDate().atStartOfDay();
            LocalDateTime thisMonthStart = LocalDateTime.now().withDayOfMonth(1).toLocalDate().atStartOfDay();

            List<Object[]> groups = Optional.ofNullable(orderRepository.countGroupByStatus())
                    .orElse(Collections.emptyList());
            Map<String, Long> byStatus = new LinkedHashMap<>();
            for (Object[] row : groups) {
                String key = row != null && row.length > 0 && row[0] != null ? row[0].toString() : "UNKNOWN";
                Long value = row != null && row.length > 1 && row[1] != null ? ((Number) row[1]).longValue() : 0L;
                byStatus.put(key, value);
            }

            Map<String, Object> stats = new LinkedHashMap<>();
            stats.put("totalOrders", safeLong(orderRepository.count()));
            stats.put("ordersToday", safeLong(orderRepository.countByCreatedAtAfter(todayStart)));
            stats.put("ordersThisMonth", safeLong(orderRepository.countByCreatedAtAfter(thisMonthStart)));
            stats.put("ordersByStatus", byStatus);
            return stats;
        } catch (Exception e) {
            e.printStackTrace();
            Map<String, Object> fallback = new LinkedHashMap<>();
            fallback.put("totalOrders", 0L);
            fallback.put("ordersToday", 0L);
            fallback.put("ordersThisMonth", 0L);
            fallback.put("ordersByStatus", new LinkedHashMap<String, Long>());
            return fallback;
        }
    }

    private Map<String, Object> defaultOverview() {
        Map<String, Object> stats = new LinkedHashMap<>();
        stats.put("totalUsers", 0L);
        stats.put("totalShops", 0L);
        stats.put("totalProducts", 0L);
        stats.put("totalOrders", 0L);
        stats.put("pendingOrders", 0L);
        stats.put("assignedOrders", 0L);
        stats.put("outForDeliveryOrders", 0L);
        stats.put("deliveredOrders", 0L);
        stats.put("cancelledOrders", 0L);
        stats.put("activeShops", 0L);
        stats.put("totalRevenue", BigDecimal.ZERO);
        stats.put("revenueToday", BigDecimal.ZERO);
        stats.put("ordersToday", 0L);
        return stats;
    }

    private long safeLong(Long value) {
        return value != null ? value : 0L;
    }

    private long safeLong(long value) {
        return value;
    }

    private BigDecimal safeAmount(BigDecimal value) {
        return value != null ? value : BigDecimal.ZERO;
    }
}
