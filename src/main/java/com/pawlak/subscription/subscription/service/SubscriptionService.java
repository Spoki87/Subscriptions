package com.pawlak.subscription.subscription.service;

import com.pawlak.subscription.currency.Currency;
import com.pawlak.subscription.currency.ExchangeRateService;
import com.pawlak.subscription.exception.domain.RecordNotFoundException;
import com.pawlak.subscription.subscription.repository.SubscriptionRepository;
import com.pawlak.subscription.subscription.dto.request.CreateSubscriptionRequest;
import com.pawlak.subscription.subscription.dto.request.UpdateSubscriptionRequest;
import com.pawlak.subscription.subscription.dto.response.SubscriptionResponse;
import com.pawlak.subscription.subscription.mapper.SubscriptionMapper;
import com.pawlak.subscription.subscription.model.Subscription;
import com.pawlak.subscription.user.model.User;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class SubscriptionService {
    private final SubscriptionRepository subscriptionRepository;
    private final SubscriptionMapper subscriptionMapper;
    private final ExchangeRateService exchangeRateService;

    public Page<SubscriptionResponse> getSubscriptionsByUser(User user, Pageable pageable) {
        return subscriptionRepository.findAllByUser(user, pageable)
                .map(sub -> subscriptionMapper.toResponse(sub, exchangeRateService.convert(sub.getPrice(), sub.getCurrency(),user.getCurrency()), user.getCurrency()));
    }

    public SubscriptionResponse getSubscriptionById(User user, UUID id) {
        return subscriptionRepository.findByIdAndUser(id, user)
                .map(sub -> subscriptionMapper.toResponse(sub, exchangeRateService.convert(sub.getPrice(), sub.getCurrency(),user.getCurrency()), user.getCurrency()))
                .orElseThrow(RecordNotFoundException::new);
    }

    @Transactional
    public SubscriptionResponse createSubscription(User user, CreateSubscriptionRequest request) {
        Subscription subscription = subscriptionMapper.toEntity(request);
        subscription.setUser(user);
        subscriptionRepository.save(subscription);
        BigDecimal convertedPrice = exchangeRateService.convert(subscription.getPrice(), subscription.getCurrency(),user.getCurrency());
        return subscriptionMapper.toResponse(subscription,convertedPrice,user.getCurrency());    }

    @Transactional
    public SubscriptionResponse updateSubscriptionById(User user, UUID id, UpdateSubscriptionRequest request) {
        Subscription subscription = subscriptionRepository.findByIdAndUser(id, user)
                .orElseThrow(RecordNotFoundException::new);

        subscriptionMapper.updateEntityFromRequest(request, subscription);
        subscriptionRepository.save(subscription);
        BigDecimal convertedPrice = exchangeRateService.convert(subscription.getPrice(), subscription.getCurrency(),user.getCurrency());
        return subscriptionMapper.toResponse(subscription,convertedPrice,user.getCurrency());
    }

    public void deleteSubscriptionById(User user, UUID id) {
        Subscription subscription = subscriptionRepository.findByIdAndUser(id, user)
                .orElseThrow(RecordNotFoundException::new);
        subscriptionRepository.delete(subscription);
    }
}
