package org.qrkanri.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.time.LocalDate;
import java.time.LocalTime;

@Getter
@AllArgsConstructor
public class DashboardDto {
    private Long id;
    private String name;
    private String duty;
    private String phone;
    private LocalDate joinDate;
    private int lateCount;

    @JsonFormat(pattern = "HH:mm")
    private String start;
    @JsonFormat(pattern = "HH:mm")
    private String end;

    private String color;

    @JsonFormat(pattern = "HH:mm")
    private LocalTime checkIn;
    @JsonFormat(pattern = "HH:mm")
    private LocalTime checkOut;
}
