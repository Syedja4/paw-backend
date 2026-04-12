package com.pawnavz.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "pets")
@Getter @Setter @Builder @NoArgsConstructor @AllArgsConstructor
public class Pet extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(nullable = false)
    private String name;

    @Column(nullable = false)
    private String type;

    private String breed;
    private Integer age;
    private Double weight;

    public enum PetType { DOG, CAT, BIRD, FISH, RABBIT, HAMSTER, OTHER }
}
