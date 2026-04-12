package com.pawnavz.repository;

import com.pawnavz.entity.Order;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface OrderRepository extends JpaRepository<Order, String> {

    // ── existing user-facing queries ──────────────────────────────────────

    Page<Order> findByUserIdOrderByCreatedAtDesc(String userId, Pageable pageable);

    Optional<Order> findByIdAndUserId(String id, String userId);

    @Query("SELECT o FROM Order o LEFT JOIN FETCH o.items i LEFT JOIN FETCH i.product " +
            "LEFT JOIN FETCH o.statusHistory LEFT JOIN FETCH o.deliveryAddress " +
            "WHERE o.id = :id AND o.user.id = :userId")
    Optional<Order> findByIdAndUserIdWithDetails(@Param("id") String id,
                                                 @Param("userId") String userId);

    @Query("SELECT o FROM Order o LEFT JOIN FETCH o.items i LEFT JOIN FETCH i.product " +
            "LEFT JOIN FETCH o.statusHistory WHERE o.id = :id")
    Optional<Order> findByIdWithDetails(@Param("id") String id);

    Page<Order> findByDriverIdOrderByCreatedAtDesc(Long driverId, Pageable pageable);

    Optional<Order> findByIdAndDriverId(String id, Long driverId);

    // ── admin filter query ────────────────────────────────────────────────

    @Query("SELECT o FROM Order o WHERE " +
            "(:status IS NULL OR o.status = :status) AND " +
            "(:userId IS NULL OR o.user.id = :userId) AND " +
            "(:driverId IS NULL OR o.driver.id = :driverId) AND " +
            "(:from IS NULL OR o.createdAt >= :from) AND " +
            "(:to IS NULL OR o.createdAt <= :to)")
    Page<Order> findWithFilters(@Param("status") Order.OrderStatus status,
                                @Param("userId") String userId,
                                @Param("driverId") Long driverId,
                                @Param("from") LocalDateTime from,
                                @Param("to") LocalDateTime to,
                                Pageable pageable);

    // ── stats queries ─────────────────────────────────────────────────────

    long countByStatus(Order.OrderStatus status);

    long countByCreatedAtAfter(LocalDateTime date);

    @Query("SELECT COALESCE(SUM(o.totalAmount), 0) FROM Order o WHERE o.status = 'DELIVERED'")
    BigDecimal sumTotalRevenue();

    @Query("SELECT COALESCE(SUM(o.totalAmount), 0) FROM Order o " +
            "WHERE o.status = 'DELIVERED' AND o.createdAt >= :from")
    BigDecimal sumRevenueSince(@Param("from") LocalDateTime from);

    @Query("SELECT o.status, COUNT(o) FROM Order o GROUP BY o.status")
    List<Object[]> countGroupByStatus();

    @Query("SELECT FUNCTION('DATE', o.createdAt), SUM(o.totalAmount), COUNT(o) " +
            "FROM Order o WHERE o.status = 'DELIVERED' AND o.createdAt >= :from " +
            "GROUP BY FUNCTION('DATE', o.createdAt) ORDER BY FUNCTION('DATE', o.createdAt)")
    List<Object[]> dailyRevenueSince(@Param("from") LocalDateTime from);
}
