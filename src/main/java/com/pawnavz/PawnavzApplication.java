package com.pawnavz;

import java.sql.Connection;
import javax.sql.DataSource;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.scheduling.annotation.EnableAsync;

@SpringBootApplication
@EnableCaching
@EnableAsync
public class PawnavzApplication {
    public static void main(String[] args) {
        SpringApplication.run(PawnavzApplication.class, args);
    }

    @Bean
    CommandLineRunner printDbUrl(DataSource dataSource) {
        return args -> {
            try (Connection connection = dataSource.getConnection()) {
                System.out.println("DB URL: " + connection.getMetaData().getURL());
            }
        };
    }
}
