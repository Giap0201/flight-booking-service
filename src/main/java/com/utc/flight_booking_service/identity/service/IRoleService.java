package com.utc.flight_booking_service.identity.service;

import com.utc.flight_booking_service.identity.domain.entities.Role;
import com.utc.flight_booking_service.identity.dto.request.RoleCreatetionRequest;

import java.util.List;

public interface IRoleService {

    Role createRole(RoleCreatetionRequest request);

    List<Role> getAll();

    void deleteRole(String name);
}