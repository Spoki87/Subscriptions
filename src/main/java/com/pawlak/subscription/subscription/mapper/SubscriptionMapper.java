package com.pawlak.subscription.subscription.mapper;

import com.pawlak.subscription.subscription.dto.request.CreateSubscriptionRequest;
import com.pawlak.subscription.subscription.dto.request.UpdateSubscriptionRequest;
import com.pawlak.subscription.subscription.dto.response.SubscriptionResponse;
import com.pawlak.subscription.subscription.model.Subscription;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;

@Mapper(componentModel = "spring")
public interface SubscriptionMapper {
    SubscriptionResponse toResponse(Subscription subscription);
    void updateEntityFromRequest(UpdateSubscriptionRequest request, @MappingTarget Subscription subscription);

    @Mapping(target = "user", ignore = true)
    @Mapping(target = "id", ignore = true)
    Subscription toEntity(CreateSubscriptionRequest request);
}
