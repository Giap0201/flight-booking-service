package com.utc.flight_booking_service.identity.controller;

import com.utc.flight_booking_service.common.ApiResponse;
import com.utc.flight_booking_service.identity.domain.entities.Role;
import com.utc.flight_booking_service.identity.dto.request.RoleCreatetionRequest;
import com.utc.flight_booking_service.identity.service.IRoleService;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/roles")
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class RoleController {
    IRoleService roleService;

    @PostMapping
    public ApiResponse<Role> createRole(@RequestBody RoleCreatetionRequest request) {
        return ApiResponse.<Role>builder()
                .result(roleService.createRole(request))
                .build();
    }

    @DeleteMapping("/{name}")
    public ApiResponse<Void> deleteRole(@PathVariable String name) {
        roleService.deleteRole(name);
        return ApiResponse.<Void>builder()
                .build();
    }

    @GetMapping
    ApiResponse<List<Role>> getAll() {
        return ApiResponse.<List<Role>>builder()
                .result(roleService.getAll())
                .build();
    }

}
