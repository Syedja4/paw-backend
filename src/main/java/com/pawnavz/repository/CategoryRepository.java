package com.pawnavz.repository;

import com.pawnavz.entity.Category;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface CategoryRepository extends JpaRepository<Category, String> {

    List<Category> findByIsActiveTrueOrderBySortOrderAsc();

    Optional<Category> findBySlug(String slug);

    // ✅ ADD THIS LINE ONLY
    Optional<Category> findByName(String name);
}