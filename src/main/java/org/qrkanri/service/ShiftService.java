package org.qrkanri.service;

import lombok.RequiredArgsConstructor;
import org.qrkanri.dto.CalendarDayDto;
import org.qrkanri.dto.DayDetailDto;
import org.qrkanri.dto.ShiftDto;
import org.qrkanri.entity.Attendance;
import org.qrkanri.entity.Employee;
import org.qrkanri.entity.Shift;
import org.qrkanri.repository.AttendanceRepository;
import org.qrkanri.repository.EmployeeRepository;
import org.qrkanri.repository.ShiftRepository;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalTime;
import java.time.YearMonth;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ShiftService {
    private final ShiftRepository shiftRepository;
    private final EmployeeRepository employeeRepository;
    private final AttendanceRepository attendanceRepository;

    public void createShift(Long employeeId,
                            LocalDate workDate,
                            LocalTime startTime,
                            LocalTime endTime){
        Employee employee = employeeRepository.findById(employeeId)
                .orElseThrow(() -> new IllegalArgumentException("社員が見つかりませんでした。"));
        if (!employee.isActive()){
            throw new IllegalArgumentException("社員が見つかりませんでした。");
        }
        if (workDate.isBefore(LocalDate.now())){
            throw new IllegalArgumentException("過去のシフトは作成できません。");
        }
        boolean exists = shiftRepository.existsByEmployeeAndWorkDate(employee, workDate);
        if (exists){
            throw new IllegalArgumentException("シフトが既に存在します。");
        }
        Shift shift = new Shift();
        shift.setEmployee(employee);
        shift.setWorkDate(workDate);
        shift.setStartTime(startTime);
        shift.setEndTime(endTime);
        shiftRepository.save(shift);
    }

    public List<Shift> getMyShifts(Employee employee){
        return shiftRepository.findByEmployee(employee);
    }

    public List<Shift> getMonthlyShifts(Employee employee,
                                        int year,
                                        int month){
        YearMonth target = YearMonth.of(year, month);
        if (!"ADMIN".equals(employee.getRole())) {
            YearMonth now = YearMonth.now();
            YearMonth prev = now.minusMonths(1);
            YearMonth next = now.plusMonths(1);
            if (target.isBefore(prev) || target.isAfter(next)) {
                throw new IllegalArgumentException("前月から翌月までの範囲のみ確認可能です。");
            }
        }
        LocalDate start = target.atDay(1);
        LocalDate end = target.atEndOfMonth();
        return shiftRepository.findByEmployeeAndWorkDateBetween(employee, start, end);
    }

    public List<CalendarDayDto> getMonthlyCalendar(Employee employee,
                                                   int year,
                                                   int month){
        YearMonth target = YearMonth.of(year, month);
        if (!"ADMIN".equals(employee.getRole())) {
            YearMonth now = YearMonth.now();
            YearMonth prev = now.minusMonths(1);
            YearMonth next = now.plusMonths(1);
            if (target.isBefore(prev) || target.isAfter(next)) {
                throw new IllegalArgumentException("前月から翌月までの範囲のみ確認可能です。");
            }
        }
        LocalDate start = target.atDay(1);
        LocalDate end = target.atEndOfMonth();
        List<Shift> shifts =
                shiftRepository.findByEmployeeAndWorkDateBetween(employee, start, end);
        List<Attendance> attendances =
                attendanceRepository.findByEmployeeAndWorkDateBetween(employee, start, end);
        Map<LocalDate, String> statusMap = new HashMap<>();
        Map<LocalDate, Integer> countMap = new HashMap<>();
        for (int i = 1; i <= target.lengthOfMonth(); i++){
            LocalDate date = target.atDay(i);
            statusMap.put(date, "NONE");
            countMap.put(date, 0);
        }
        LocalDate today = LocalDate.now();
        Map<LocalDate, List<Shift>> shiftMap =
                shifts.stream().collect(Collectors.groupingBy(Shift::getWorkDate));
        Map<LocalDate, List<Attendance>> attendanceMap =
                attendances.stream().collect(Collectors.groupingBy(Attendance::getWorkDate));
        for (int i = 1; i <= target.lengthOfMonth(); i++) {
            LocalDate date = target.atDay(i);
            List<Attendance> dayAttendances =
                    attendanceMap.getOrDefault(date, new ArrayList<>());
            List<Shift> dayShifts =
                    shiftMap.getOrDefault(date, new ArrayList<>());
            boolean hasCompletedAttendance = dayAttendances.stream().anyMatch(a ->
                    a.getCheckIn() != null && a.getCheckOut() != null
            );
            if (hasCompletedAttendance) {
                statusMap.put(date, "COMPLETED");
                countMap.put(date, 1);
            }
            else if (!dayShifts.isEmpty()) {
                statusMap.put(date, "SCHEDULED");
                countMap.put(date, 1);
            }
        }
        List<CalendarDayDto> result = new ArrayList<>();
        for (LocalDate date : statusMap.keySet()){
            result.add(new CalendarDayDto(
                    date,
                    statusMap.get(date),
                    countMap.get(date)
            ));
        }
        result.sort(Comparator.comparing(CalendarDayDto::getDate));
        return result;
    }

    public List<ShiftDto> getShiftsByDate(Employee employee, LocalDate date){
        List<Shift> shifts = shiftRepository.findByEmployeeAndWorkDateBetween(employee, date, date);
        return shifts.stream()
                .map(shift -> {
                    Attendance attendance = attendanceRepository
                            .findByEmployeeAndWorkDate(employee, date)
                            .stream().findFirst().orElse(null);
                    LocalTime checkIn = null;
                    LocalTime checkOut = null;
                    if (attendance != null && attendance.getCheckIn() != null && attendance.getCheckOut() != null){
                        checkIn = attendance.getCheckIn().toLocalTime();
                        checkOut =attendance.getCheckOut().toLocalTime();
                    }
                    return new ShiftDto(
                            shift.getId(),
                            shift.getEmployee().getName(),
                            shift.getStartTime(),
                            shift.getEndTime(),
                            checkIn,
                            checkOut
                    );
                })
                .sorted(Comparator.comparing(ShiftDto::getStartTime))
                .toList();
    }

    public DayDetailDto getDayDetail(Employee employee, LocalDate date){
        Shift shift = shiftRepository
                .findByEmployeeAndWorkDateBetween(employee, date, date)
                .stream().findFirst().orElse(null);
        Attendance attendance = attendanceRepository
                .findByEmployeeAndWorkDate(employee, date)
                .stream().findFirst().orElse(null);
        boolean hasCompletedAttendance =
                attendance != null &&
                        attendance.getCheckIn() != null &&
                        attendance.getCheckOut() != null;
        if (hasCompletedAttendance) {
            shift = null;
        }
        else if (attendance != null &&
                (attendance.getCheckIn() == null || attendance.getCheckOut() == null)) {
            attendance = null;
        }
        return new DayDetailDto(date, shift, attendance);
    }
}
