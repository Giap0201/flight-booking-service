package com.utc.flight_booking_service.identity.mapper;


import com.utc.flight_booking_service.identity.domain.entities.User;
import com.utc.flight_booking_service.identity.dto.request.UserCreationRequest;
import com.utc.flight_booking_service.identity.dto.request.UserUpdateRequest;
import com.utc.flight_booking_service.identity.dto.response.UserResponse;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;

@Mapper(componentModel = "spring")
public interface UserMapper {
    User toUser(UserCreationRequest request);

    UserResponse toUserRespone(User user);

    @Mapping(target = "roles", ignore = true)
    void updateUser(@MappingTarget User user, UserUpdateRequest request);
}
