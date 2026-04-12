package com.pawnavz.repository;

import com.pawnavz.entity.Address;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface AddressRepository extends JpaRepository<Address, String> {

    List<Address> findByUserIdOrderByIsDefaultDescCreatedAtDesc(String userId);

    Optional<Address> findByIdAndUserId(String id, String userId);

    Optional<Address> findByUserIdAndIsDefaultTrue(String userId);

    long countByUserId(String userId);

    boolean existsByIdAndUserId(String id, String userId);

    @Modifying
    @Query("UPDATE Address a SET a.isDefault = false WHERE a.user.id = :userId")
    void clearDefaultByUserId(@Param("userId") String userId);
}
