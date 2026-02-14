package com.utc.flight_booking_service.inventory.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.*;
import lombok.experimental.FieldDefaults;

@Entity
@Table(name = "airports")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class Airport extends BaseTimeEntity{
    @Id
    @Column(length = 3)
    String code;

    @Column(nullable = false)
    String name;

    String cityCode;
    String countryCode;
    String timezone;
}
