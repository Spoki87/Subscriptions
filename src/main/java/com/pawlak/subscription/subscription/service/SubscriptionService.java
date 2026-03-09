package com.pawlak.subscription.subscription.service;

import com.pawlak.subscription.exception.domain.RecordNotFoundException;
import com.pawlak.subscription.subscription.repository.SubscriptionRepository;
import com.pawlak.subscription.subscription.dto.request.CreateSubscriptionRequest;
import com.pawlak.subscription.subscription.dto.request.UpdateSubscriptionRequest;
import com.pawlak.subscription.subscription.dto.response.SubscriptionResponse;
import com.pawlak.subscription.subscription.mapper.SubscriptionMapper;
import com.pawlak.subscription.subscription.model.Subscription;
import com.pawlak.subscription.user.model.User;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class SubscriptionService {
    private final SubscriptionRepository subscriptionRepository;
    private final SubscriptionMapper subscriptionMapper;

    public Page<SubscriptionResponse> getSubscriptionsByUser(User user, Pageable pageable) {
        return subscriptionRepository.findAllByUser(user, pageable)
                .map(subscriptionMapper::toResponse);
    }

    public SubscriptionResponse getSubscriptionById(User user, UUID id) {
        return subscriptionRepository.findByIdAndUser(id, user)
                .map(subscriptionMapper::toResponse)
                .orElseThrow(RecordNotFoundException::new);
    }

    public SubscriptionResponse createSubscription(User user, CreateSubscriptionRequest request) {
        Subscription subscription = subscriptionMapper.toEntity(request);
        subscription.setUser(user);
        subscriptionRepository.save(subscription);
        return subscriptionMapper.toResponse(subscription);
    }

    public SubscriptionResponse updateSubscriptionById(User user, UUID id, UpdateSubscriptionRequest request) {
        Subscription subscription = subscriptionRepository.findByIdAndUser(id, user)
                .orElseThrow(RecordNotFoundException::new);

        subscriptionMapper.updateEntityFromRequest(request, subscription);
        subscriptionRepository.save(subscription);
        return subscriptionMapper.toResponse(subscription);
    }

    public void deleteSubscriptionById(User user, UUID id) {
        Subscription subscription = subscriptionRepository.findByIdAndUser(id, user)
                .orElseThrow(RecordNotFoundException::new);

        subscriptionRepository.delete(subscription);
    }
}
