package com.pawlak.subscription.subscription.repository;

import com.pawlak.subscription.subscription.model.Subscription;
import com.pawlak.subscription.user.model.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface SubscriptionRepository extends JpaRepository<Subscription, UUID> {
    Page<Subscription> findAllByUser(User user, Pageable pageable);
    Optional<Subscription> findByIdAndUser(UUID id, User user);
}
