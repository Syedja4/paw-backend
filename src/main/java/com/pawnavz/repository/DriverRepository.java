package com.pawnavz.repository;

import com.pawnavz.entity.Driver;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;

@Repository
public interface DriverRepository extends JpaRepository<Driver, Long> {

    Optional<Driver> findByUserId(String userId);

    Optional<Driver> findByUserEmail(String email);

    List<Driver> findByAvailableTrue();

    List<Driver> findByStatus(String status);

    @Query("SELECT d FROM Driver d WHERE " +
            "(:status IS NULL OR d.status = :status) AND " +
            "(:available IS NULL OR d.available = :available)")
    Page<Driver> findWithFilters(@Param("status") String status,
                                 @Param("available") Boolean available,
                                 Pageable pageable);

    long countByStatus(String status);

    long countByAvailableTrue();
}
