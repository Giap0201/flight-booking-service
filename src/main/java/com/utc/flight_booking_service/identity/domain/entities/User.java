package com.utc.flight_booking_service.identity.domain.entities;

import com.utc.flight_booking_service.common.BaseEntity;
import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.FieldDefaults;
import lombok.experimental.SuperBuilder;

import java.util.Set;

@Entity
@Table(name = "users")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class User extends BaseEntity {

    @Column(length = 100, unique = true, nullable = false)
    String email;

    @Column(name = "password_hash", length = 255, nullable = false)
    String passwordHash;

    @Column(name = "full_name", length = 100)
    String fullName;

    @Column(length = 20)
    String phone;

    @Builder.Default
    @Column(nullable = false)
    Boolean active = true;

    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(
            name = "user_roles",
            joinColumns = @JoinColumn(name = "user_id"),
            inverseJoinColumns = @JoinColumn(name = "role_id")
    )
    Set<Role> roles;
}