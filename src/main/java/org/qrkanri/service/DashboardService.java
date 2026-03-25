package org.qrkanri.service;

import lombok.RequiredArgsConstructor;
import org.qrkanri.dto.DashboardDto;
import org.qrkanri.entity.Attendance;
import org.qrkanri.entity.Employee;
import org.qrkanri.entity.Shift;
import org.qrkanri.repository.AttendanceRepository;
import org.qrkanri.repository.EmployeeRepository;
import org.qrkanri.repository.ShiftRepository;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class DashboardService {
    private final ShiftRepository shiftRepository;
    private final AttendanceRepository attendanceRepository;
    private final EmployeeRepository employeeRepository;

    public List<DashboardDto> getTodayDashboard() {
        LocalDate today = LocalDate.now();
        List<Shift> shifts = shiftRepository.findByWorkDateAndEmployeeActiveTrue(today);
        List<Attendance> allAttendances = attendanceRepository.findAll();
        List<DashboardDto> result = new ArrayList<>();
        List<String> colors = List.of(
                "#ff9f43","#4dabf7","#69db7c","#c792ea",
                "#ff6b6b","#ffd43b","#63e6be","#74c0fc"
        );
        for (int i = 0; i < shifts.size(); i++) {
            Shift shift = shifts.get(i);
            Employee employee = shift.getEmployee();
            List<Shift> employeeShifts = shiftRepository
                    .findByEmployeeAndWorkDateBetween(
                            employee,
                            employee.getCreatedAt().toLocalDate(),
                            today
                    );
            int lateCount = (int) allAttendances.stream()
                    .filter(a -> a.getEmployee().getId().equals(employee.getId()))
                    .filter(a -> a.getCheckIn() != null)
                    .filter(a -> {
                        return employeeShifts.stream()
                                .filter(s -> s.getWorkDate().equals(a.getWorkDate()))
                                .anyMatch(s ->
                                        a.getCheckIn().toLocalTime().isAfter(s.getStartTime())
                                );
                    })
                    .count();
            Attendance todayAttendance = allAttendances.stream()
                    .filter(a -> a.getEmployee().getId().equals(employee.getId()))
                    .filter(a -> a.getCheckIn() != null)
                    .filter(a -> a.getCheckIn().toLocalDate().equals(today))
                    .findFirst()
                    .orElse(null);
            result.add(new DashboardDto(
                    employee.getId(),
                    employee.getName(),
                    employee.getDuty(),
                    employee.getPhone(),
                    employee.getCreatedAt() != null ? employee.getCreatedAt().toLocalDate() : null,
                    lateCount,
                    shift.getStartTime().toString(),
                    shift.getEndTime().toString(),
                    colors.get(i % colors.size()),
                    todayAttendance != null && todayAttendance.getCheckIn() != null
                            ? todayAttendance.getCheckIn().toLocalTime()
                            : null,
                    todayAttendance != null && todayAttendance.getCheckOut() != null
                            ? todayAttendance.getCheckOut().toLocalTime()
                            : null
            ));
        }
        return result;
    }

    public void updateAllLateCounts(){ // 서버 켤 때마다 누적 지각 횟수 계산
        List<Employee> employees = employeeRepository.findAll();
        List<Attendance> allAttendances = attendanceRepository.findAll();
        LocalDate today = LocalDate.now();
        for (Employee employee : employees){
            if (employee.getCreatedAt() == null) continue;
            List<Shift> shifts = shiftRepository
                    .findByEmployeeAndWorkDateBetween(
                            employee,
                            employee.getCreatedAt().toLocalDate(),
                            today
                    );
            int lateCount = (int) allAttendances.stream()
                    .filter(a -> a.getEmployee().getId().equals(employee.getId()))
                    .filter(a -> a.getCheckIn() != null)
                    .filter(a -> shifts.stream()
                            .filter(s -> s.getWorkDate().equals(a.getWorkDate()))
                            .anyMatch(s ->
                                    a.getCheckIn().toLocalTime().isAfter(s.getStartTime())
                            )
                    )
                    .count();
            employee.setLateCount(lateCount);
            employeeRepository.save(employee);
        }
    }
}