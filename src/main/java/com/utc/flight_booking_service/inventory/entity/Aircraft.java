package com.utc.flight_booking_service.inventory.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.*;
import lombok.experimental.FieldDefaults;

@Entity
@Table(name = "aircrafts")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class Aircraft extends BaseTimeEntity{
    @Id
    @Column(length = 20)
    String code;

    String name;
    Integer totalEconomySeats; //ghế phổ thông
    Integer totalBusinessSeats; //ghế thương gia
}
