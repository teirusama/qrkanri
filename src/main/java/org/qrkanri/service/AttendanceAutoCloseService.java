package org.qrkanri.service;

import lombok.RequiredArgsConstructor;
import org.qrkanri.entity.Attendance;
import org.qrkanri.repository.AttendanceRepository;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class AttendanceAutoCloseService {
    private final AttendanceRepository attendanceRepository;

    @Scheduled(fixedRate = 300000)
    public void autoCheckout(){
        List<Attendance> list = attendanceRepository.findByCheckOutIsNull();
        LocalDateTime now = LocalDateTime.now();
        for (Attendance attendance : list){
            if (attendance.getCheckIn() == null) continue;
            LocalDateTime limit = attendance.getCheckIn().plusHours(15);

            if (now.isAfter(limit)){
                attendance.setCheckOut(limit);
                attendanceRepository.save(attendance);
            }
        }
    }
}
