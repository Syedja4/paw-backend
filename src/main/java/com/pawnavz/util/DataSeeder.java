package com.pawnavz.util;

import com.pawnavz.entity.Cart;
import com.pawnavz.entity.Category;
import com.pawnavz.entity.Product;
import com.pawnavz.entity.User;
import com.pawnavz.entity.Wishlist;
import com.pawnavz.repository.CartRepository;
import com.pawnavz.repository.CategoryRepository;
import com.pawnavz.repository.ProductRepository;
import com.pawnavz.repository.UserRepository;
import com.pawnavz.repository.WishlistRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

@Slf4j
@Component
@RequiredArgsConstructor
public class DataSeeder implements CommandLineRunner {

    private final UserRepository userRepository;
    private final CategoryRepository categoryRepository;
    private final ProductRepository productRepository;
    private final CartRepository cartRepository;
    private final WishlistRepository wishlistRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    public void run(String... args) {
        ensureAdminUser();

        if (userRepository.count() > 1) {
            log.info("Database already seeded - skipping demo/catalog seed.");
            return;
        }
        log.info("Seeding initial data...");

        // Demo user
        User demo = userRepository.save(User.builder()
                .name("Demo User").email("demo@pawnavz.com")
                .password(passwordEncoder.encode("Demo@123"))
                .phone("9876543210").role(User.Role.USER).build());
        cartRepository.save(Cart.builder().user(demo).build());
        wishlistRepository.save(Wishlist.builder().user(demo).build());

        // Categories
        Category dogFood = categoryRepository.save(Category.builder()
                .name("Dog Food").slug("dog-food").sortOrder(1).build());
        Category catFood = categoryRepository.save(Category.builder()
                .name("Cat Food").slug("cat-food").sortOrder(2).build());
        Category accessories = categoryRepository.save(Category.builder()
                .name("Accessories").slug("accessories").sortOrder(3).build());

        // Products
        productRepository.saveAll(List.of(
                Product.builder()
                        .name("Royal Canin Adult Dog Food 3kg").slug("royal-canin-adult-3kg")
                        .description("Premium nutrition for adult dogs")
                        .price(new BigDecimal("1299")).mrp(new BigDecimal("1499"))
                        .discountPercent(13).brand("Royal Canin").sku("RC-DOG-3KG")
                        .stockQuantity(50).category(dogFood).petType("DOG")
                        .avgRating(4.5).reviewCount(120).isFeatured(true)
                        .imageUrls(List.of("https://cdn.pawnavz.com/products/royal-canin.jpg"))
                        .build(),
                Product.builder()
                        .name("Whiskas Adult Cat Food 1.2kg").slug("whiskas-adult-1-2kg")
                        .description("Complete balanced nutrition for cats")
                        .price(new BigDecimal("499")).mrp(new BigDecimal("599"))
                        .discountPercent(17).brand("Whiskas").sku("WH-CAT-1.2KG")
                        .stockQuantity(80).category(catFood).petType("CAT")
                        .avgRating(4.3).reviewCount(85).isFeatured(true)
                        .imageUrls(List.of("https://cdn.pawnavz.com/products/whiskas.jpg"))
                        .build(),
                Product.builder()
                        .name("Leather Dog Collar - Medium").slug("leather-dog-collar-medium")
                        .description("Durable genuine leather collar")
                        .price(new BigDecimal("349")).mrp(new BigDecimal("499"))
                        .discountPercent(30).brand("PetSafe").sku("PS-COLLAR-M")
                        .stockQuantity(200).category(accessories).petType("DOG")
                        .avgRating(4.7).reviewCount(200).isFeatured(false)
                        .imageUrls(List.of("https://cdn.pawnavz.com/products/collar.jpg"))
                        .build()
        ));

        log.info("Seeding complete. Admin: admin@pawnavz.com / 123456 | Demo: demo@pawnavz.com / Demo@123");
    }

    private void ensureAdminUser() {
        Optional<User> existingAdmin = userRepository.findByEmail("admin@pawnavz.com");
        if (existingAdmin.isPresent()) {
            User admin = existingAdmin.get();
            boolean changed = false;
            if (admin.getName() == null || admin.getName().isBlank()) {
                admin.setName("Pawnavz Admin");
                changed = true;
            }
            if (admin.getRole() != User.Role.ADMIN) {
                admin.setRole(User.Role.ADMIN);
                changed = true;
            }
            if (admin.getPassword() == null || !passwordEncoder.matches("123456", admin.getPassword())) {
                admin.setPassword(passwordEncoder.encode("123456"));
                changed = true;
            }
            if (changed) {
                userRepository.save(admin);
                log.info("Updated admin user: admin@pawnavz.com");
            }
            return;
        }

        User admin = new User();
        admin.setName("Pawnavz Admin");
        admin.setEmail("admin@pawnavz.com");
        admin.setPassword(passwordEncoder.encode("123456"));
        admin.setRole(User.Role.ADMIN);
        userRepository.save(admin);
        log.info("Created admin user: admin@pawnavz.com");
    }
}
