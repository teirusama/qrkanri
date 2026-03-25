package org.qrkanri.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.qrkanri.entity.Attendance;
import org.qrkanri.entity.Shift;

import java.time.LocalDate;

@Getter
@AllArgsConstructor
public class DayDetailDto {
    private LocalDate date;
    private Shift shift;
    private Attendance attendance;
}
