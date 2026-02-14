package com.utc.flight_booking_service.inventory.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.*;
import lombok.experimental.FieldDefaults;

@Entity
@Table(name = "airlines")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class Airline extends BaseTimeEntity{
    @Id
    @Column(length = 5)
    String code;

    @Column(nullable = false)
    String name;

    String logoUrl;
}
