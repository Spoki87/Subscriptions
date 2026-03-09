package com.pawlak.subscription.subscription.service;

import com.pawlak.subscription.exception.domain.RecordNotFoundException;
import com.pawlak.subscription.subscription.dto.request.CreateSubscriptionRequest;
import com.pawlak.subscription.subscription.dto.request.UpdateSubscriptionRequest;
import com.pawlak.subscription.subscription.dto.response.SubscriptionResponse;
import com.pawlak.subscription.subscription.mapper.SubscriptionMapper;
import com.pawlak.subscription.subscription.model.Subscription;
import com.pawlak.subscription.subscription.repository.SubscriptionRepository;
import com.pawlak.subscription.user.model.Role;
import com.pawlak.subscription.user.model.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class SubscriptionServiceTest {

    @Mock
    private SubscriptionRepository subscriptionRepository;

    @Mock
    private SubscriptionMapper subscriptionMapper;

    @InjectMocks
    private SubscriptionService subscriptionService;

    private User user;
    private Subscription subscription;
    private SubscriptionResponse subscriptionResponse;
    private UUID subscriptionId;

    @BeforeEach
    void setUp() {
        user = new User("testuser", "test@email.com", "encodedPassword", Role.USER);
        subscriptionId = UUID.randomUUID();
        subscription = new Subscription("Netflix", "Streaming service", new BigDecimal("15.99"));
        subscriptionResponse = new SubscriptionResponse(subscriptionId, "Netflix", "Streaming service", new BigDecimal("15.99"));
    }

    @Nested
    @DisplayName("getSubscriptionsByUser")
    class GetSubscriptionsByUser {

        @Test
        @DisplayName("returns mapped page for user")
        void returnsPageOfResponses() {
            Pageable pageable = PageRequest.of(0, 10);
            Page<Subscription> page = new PageImpl<>(List.of(subscription));
            when(subscriptionRepository.findAllByUser(user, pageable)).thenReturn(page);
            when(subscriptionMapper.toResponse(subscription)).thenReturn(subscriptionResponse);

            Page<SubscriptionResponse> result = subscriptionService.getSubscriptionsByUser(user, pageable);

            assertThat(result.getContent()).hasSize(1);
            assertThat(result.getContent().get(0)).isEqualTo(subscriptionResponse);
        }

        @Test
        @DisplayName("returns empty page when user has no subscriptions")
        void returnsEmptyPage() {
            Pageable pageable = PageRequest.of(0, 10);
            when(subscriptionRepository.findAllByUser(user, pageable)).thenReturn(Page.empty());

            Page<SubscriptionResponse> result = subscriptionService.getSubscriptionsByUser(user, pageable);

            assertThat(result.getContent()).isEmpty();
        }
    }

    @Nested
    @DisplayName("getSubscriptionById")
    class GetSubscriptionById {

        @Test
        @DisplayName("returns response when subscription found")
        void returnsResponseWhenFound() {
            when(subscriptionRepository.findByIdAndUser(subscriptionId, user)).thenReturn(Optional.of(subscription));
            when(subscriptionMapper.toResponse(subscription)).thenReturn(subscriptionResponse);

            SubscriptionResponse result = subscriptionService.getSubscriptionById(user, subscriptionId);

            assertThat(result).isEqualTo(subscriptionResponse);
        }

        @Test
        @DisplayName("throws RecordNotFoundException when subscription not found")
        void throwsWhenNotFound() {
            when(subscriptionRepository.findByIdAndUser(subscriptionId, user)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> subscriptionService.getSubscriptionById(user, subscriptionId))
                    .isInstanceOf(RecordNotFoundException.class);
        }
    }

    @Nested
    @DisplayName("createSubscription")
    class CreateSubscription {

        @Test
        @DisplayName("creates subscription, sets user, saves, and returns response")
        void createsAndReturnsMappedResponse() {
            CreateSubscriptionRequest request = new CreateSubscriptionRequest("Netflix", "Streaming", new BigDecimal("15.99"));
            when(subscriptionMapper.toEntity(request)).thenReturn(subscription);
            when(subscriptionMapper.toResponse(subscription)).thenReturn(subscriptionResponse);

            SubscriptionResponse result = subscriptionService.createSubscription(user, request);

            assertThat(result).isEqualTo(subscriptionResponse);
            assertThat(subscription.getUser()).isEqualTo(user);
            verify(subscriptionRepository).save(subscription);
        }
    }

    @Nested
    @DisplayName("updateSubscriptionById")
    class UpdateSubscriptionById {

        @Test
        @DisplayName("updates, saves, and returns response when found")
        void updatesAndReturnsResponse() {
            UpdateSubscriptionRequest request = new UpdateSubscriptionRequest("Netflix Updated", "New desc", new BigDecimal("19.99"));
            when(subscriptionRepository.findByIdAndUser(subscriptionId, user)).thenReturn(Optional.of(subscription));
            when(subscriptionMapper.toResponse(subscription)).thenReturn(subscriptionResponse);

            SubscriptionResponse result = subscriptionService.updateSubscriptionById(user, subscriptionId, request);

            assertThat(result).isEqualTo(subscriptionResponse);
            verify(subscriptionMapper).updateEntityFromRequest(request, subscription);
            verify(subscriptionRepository).save(subscription);
        }

        @Test
        @DisplayName("throws RecordNotFoundException when subscription not found")
        void throwsWhenNotFound() {
            UpdateSubscriptionRequest request = new UpdateSubscriptionRequest("Name", "Desc", BigDecimal.ONE);
            when(subscriptionRepository.findByIdAndUser(subscriptionId, user)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> subscriptionService.updateSubscriptionById(user, subscriptionId, request))
                    .isInstanceOf(RecordNotFoundException.class);
        }
    }

    @Nested
    @DisplayName("deleteSubscriptionById")
    class DeleteSubscriptionById {

        @Test
        @DisplayName("deletes subscription when found")
        void deletesWhenFound() {
            when(subscriptionRepository.findByIdAndUser(subscriptionId, user)).thenReturn(Optional.of(subscription));

            subscriptionService.deleteSubscriptionById(user, subscriptionId);

            verify(subscriptionRepository).delete(subscription);
        }

        @Test
        @DisplayName("throws RecordNotFoundException when subscription not found")
        void throwsWhenNotFound() {
            when(subscriptionRepository.findByIdAndUser(subscriptionId, user)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> subscriptionService.deleteSubscriptionById(user, subscriptionId))
                    .isInstanceOf(RecordNotFoundException.class);
            verify(subscriptionRepository, never()).delete(any());
        }
    }
}
