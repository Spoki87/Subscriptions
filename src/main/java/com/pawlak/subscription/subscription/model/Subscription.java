package com.pawlak.subscription.subscription.model;

import com.pawlak.subscription.user.model.User;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.util.UUID;

@Entity
@Getter
@Table(name = "subscription")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Subscription {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Setter
    private String name;

    @Setter
    private String description;

    @Column(precision = 10, scale = 2)
    @Setter
    private BigDecimal price;

    @ManyToOne
    @JoinColumn(name = "app_user_id")
    @Setter
    private User user;

    public Subscription(String name, String description, BigDecimal price) {
        this.name = name;
        this.description = description;
        this.price = price;
    }

}
