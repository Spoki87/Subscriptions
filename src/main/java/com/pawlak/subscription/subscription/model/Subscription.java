package com.pawlak.subscription.subscription.model;

import com.pawlak.subscription.currency.Currency;
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

    @Enumerated(EnumType.STRING)
    @Setter
    private Currency currency = Currency.PLN;

    @Enumerated(EnumType.STRING)
    @Setter
    private SubscriptionModel subscriptionModel = SubscriptionModel.MONTHLY;

    @ManyToOne
    @JoinColumn(name = "app_user_id")
    @Setter
    private User user;

    public Subscription(String name, String description, BigDecimal price, Currency currency, SubscriptionModel subscriptionModel) {
        this.name = name;
        this.description = description;
        this.price = price;
        this.currency = currency;
        this.subscriptionModel = subscriptionModel;
    }

}
