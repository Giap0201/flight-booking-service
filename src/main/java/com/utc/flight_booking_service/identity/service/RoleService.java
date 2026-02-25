package com.utc.flight_booking_service.identity.service;

import com.utc.flight_booking_service.exception.AppException;
import com.utc.flight_booking_service.exception.ErrorCode;
import com.utc.flight_booking_service.identity.domain.entities.Role;
import com.utc.flight_booking_service.identity.domain.repository.RoleRepository;
import com.utc.flight_booking_service.identity.dto.request.RoleCreatetionRequest;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class RoleService {
    RoleRepository roleRepository;

    public Role createRole(RoleCreatetionRequest request) {
        Role role = new Role();
        role.setName(request.getName());
        role.setDescription(request.getDescription());
        return roleRepository.save(role);
    }

    public void deleteRole(String Name) {
        Role role = roleRepository.findByName(Name).orElseThrow(
                () -> new AppException(ErrorCode.ROLE_NOT_FOUND));
        roleRepository.delete(role);
    }
}

