package com.pawnavz.repository;

import com.pawnavz.entity.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, String> {

    Optional<User> findByEmail(String email);

    Optional<User> findByPhone(String phone);

    boolean existsByEmail(String email);

    boolean existsByPhone(String phone);

    long countByRole(User.Role role);

    // ✅ FIXED QUERY (JPQL SAFE)
    @Query("SELECT u FROM User u WHERE " +
            "(:search IS NULL OR LOWER(u.name) LIKE LOWER(CONCAT('%',:search,'%')) " +
            "OR LOWER(u.email) LIKE LOWER(CONCAT('%',:search,'%')) " +
            "OR LOWER(u.phone) LIKE LOWER(CONCAT('%',:search,'%'))) AND " +
            "(:role IS NULL OR u.role = :role) AND " +
            "(:blocked IS NULL OR u.blocked = :blocked)")
    Page<User> findWithFilters(@Param("search") String search,
                               @Param("role") User.Role role,
                               @Param("blocked") Boolean blocked,
                               Pageable pageable);
}