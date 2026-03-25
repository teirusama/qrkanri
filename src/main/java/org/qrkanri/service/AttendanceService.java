package org.qrkanri.service;

import lombok.RequiredArgsConstructor;
import org.qrkanri.entity.Attendance;
import org.qrkanri.entity.Employee;
import org.qrkanri.entity.QrToken;
import org.qrkanri.entity.Shift;
import org.qrkanri.repository.AttendanceRepository;
import org.qrkanri.repository.EmployeeRepository;
import org.qrkanri.repository.QrTokenRepository;
import org.qrkanri.repository.ShiftRepository;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class AttendanceService {
    private final AttendanceRepository attendanceRepository;
    private final EmployeeRepository employeeRepository;
    private final QrTokenRepository qrTokenRepository;
    private final ShiftRepository shiftRepository;

    public void checkIn(Long employeeId){
        Employee employee = employeeRepository.findById(employeeId).
                orElseThrow(() -> new IllegalArgumentException("社員が見つかりませんでした。"));
        LocalDate today = LocalDate.now();
        attendanceRepository.findByEmployeeAndWorkDate(employee, today)
                .ifPresent(a -> {
                    throw new IllegalStateException("既に出勤済みです。");
                });
        Attendance attendance = new Attendance();
        attendance.setEmployee(employee);
        attendance.setWorkDate(today);
        attendance.setCheckIn(LocalDateTime.now());
        attendanceRepository.save(attendance);
    }

    public void checkOut(Long employeeId){
        Employee employee = employeeRepository.findById(employeeId)
                .orElseThrow(() -> new IllegalArgumentException("社員が見つかりませんでした。"));
        LocalDate today = LocalDate.now();
        Attendance attendance = attendanceRepository
                .findByEmployeeAndWorkDate(employee, today)
                .orElseThrow(() -> new IllegalStateException("出勤記録がありません。"));
        if(attendance.getCheckOut() != null){
            throw new IllegalStateException("既に退勤済みです。");
        }
        attendance.setCheckOut(LocalDateTime.now());
        attendanceRepository.save(attendance);
    }

    public List<Attendance> getAttendancesByEmployee(Long employeeId){
        Employee employee = employeeRepository.findById(employeeId)
                .orElseThrow(() -> new IllegalArgumentException("社員が見つかりませんでした。"));
        return attendanceRepository.findByEmployee_Id(employee.getId());
    }

    public List<Attendance> getMyAttendances(Employee employee){
        return attendanceRepository.findByEmployeeOrderByWorkDateDesc(employee);
    }

    public String processQr(Employee employee, String token, boolean forceCheckout){
        QrToken qrToken = qrTokenRepository.findByToken(token)
                .orElseThrow(() -> new RuntimeException("このQRコードは有効ではありません。"));
        if (qrToken.getExpiresAt().isBefore(LocalDateTime.now())){
            throw new RuntimeException("このQRコードは有効ではありません。");
        }
        LocalDate today = LocalDate.now();
        Shift shift = shiftRepository.findByEmployeeAndWorkDate(employee, today)
                .stream().findFirst().orElse(null);
        if (shift == null){
            throw new RuntimeException("今日は出勤日ではありません。");
        }
        Attendance attendance = attendanceRepository
                .findByEmployeeAndWorkDate(employee, today)
                .stream().findFirst().orElse(null);
        if (attendance == null){
            Attendance newAttendance = new Attendance();
            newAttendance.setEmployee(employee);
            newAttendance.setWorkDate(today);
            LocalDateTime now = LocalDateTime.now();
            newAttendance.setCheckIn(now);
            if (shift.getStartTime() != null){
                if (now.toLocalTime().isAfter(shift.getStartTime())){
                    employee.setLateCount(employee.getLateCount() + 1);
                    employeeRepository.save(employee);
                }
            }
            attendanceRepository.save(newAttendance);
            qrTokenRepository.delete(qrToken);
            return "CHECKIN";
        }
        if (attendance.getCheckOut() != null){
            throw new RuntimeException("既に退勤済みです。");
        }
        LocalTime shiftEnd = shift.getEndTime();
        LocalDateTime endDateTime = LocalDateTime.of(today, shiftEnd);
        LocalDateTime allowedTime = endDateTime.minusMinutes(30);
        if (LocalDateTime.now().isBefore(allowedTime) && !forceCheckout){
            return "EARLY_WARNING";
        }
        attendance.setCheckOut(LocalDateTime.now());
        attendanceRepository.save(attendance);
        qrTokenRepository.delete(qrToken);
        return "CHECKOUT";
    }
}
