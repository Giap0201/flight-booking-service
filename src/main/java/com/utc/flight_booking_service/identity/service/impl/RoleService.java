package com.utc.flight_booking_service.identity.service.impl;

import com.utc.flight_booking_service.exception.AppException;
import com.utc.flight_booking_service.exception.ErrorCode;
import com.utc.flight_booking_service.identity.domain.entities.Role;
import com.utc.flight_booking_service.identity.domain.repository.RoleRepository;
import com.utc.flight_booking_service.identity.dto.request.RoleCreatetionRequest;
import com.utc.flight_booking_service.identity.service.IRoleService;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class RoleService implements IRoleService {
    RoleRepository roleRepository;

    @Override
    public Role createRole(RoleCreatetionRequest request) {
        if (roleRepository.existsByName(request.getName())) {
            throw new AppException(ErrorCode.ROLE_EXISTED);
        }
        Role role = new Role();
        role.setName(request.getName());
        role.setDescription(request.getDescription());
        return roleRepository.save(role);
    }

    @Override
    public List<Role> getAll() {
        return roleRepository.findAll().stream().toList();
    }

    @Override
    public void deleteRole(String Name) {
        Role role = roleRepository.findByName(Name).orElseThrow(
                () -> new AppException(ErrorCode.ROLE_NOT_FOUND));
        roleRepository.delete(role);
    }
}

