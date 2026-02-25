package com.utc.flight_booking_service.identity.mapper;


import com.utc.flight_booking_service.identity.domain.entities.User;
import com.utc.flight_booking_service.identity.dto.request.UserCreationRequest;
import com.utc.flight_booking_service.identity.dto.response.UserResponse;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface UserMapper {
    @Mapping(target = "passwordHash", ignore = true)
    User toUser(UserCreationRequest request);

    UserResponse toUserRespone(User user);
    //void updateUser(@MappingTarget User user, UserUpdateRequest request);
}
