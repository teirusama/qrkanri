package org.qrkanri.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.time.LocalDate;

@Getter
@AllArgsConstructor
public class CalendarDayDto {
    private LocalDate date;
    private String status;
    private int count;
}
