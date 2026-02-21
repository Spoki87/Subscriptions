package com.pawlak.subscription.appuser.repository;

import com.pawlak.subscription.appuser.model.Appuser;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface UserRepository extends JpaRepository<Appuser, UUID> {
    Optional<Appuser> findByEmail(String email);
}