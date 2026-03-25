package org.qrkanri.controller;

import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import org.qrkanri.dto.CalendarDayDto;
import org.qrkanri.dto.ShiftDto;
import org.qrkanri.entity.Attendance;
import org.qrkanri.entity.Employee;
import org.qrkanri.entity.Shift;
import org.qrkanri.repository.AttendanceRepository;
import org.qrkanri.repository.ShiftRepository;
import org.qrkanri.service.ShiftService;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequiredArgsConstructor
@RequestMapping("/admin/shifts")
public class ShiftController {
    private final ShiftService shiftService;
    private final ShiftRepository shiftRepository;
    private final AttendanceRepository attendanceRepository;

    @PostMapping
    public String createShift(@RequestParam Long employeeId,
                              @RequestParam LocalDate workDate,
                              @RequestParam LocalTime startTime,
                              @RequestParam LocalTime endTime,
                              HttpSession session){
        Employee loginUser = (Employee) session.getAttribute("employee");
        if (loginUser == null){
            return "ログインが必要です。";
        }
        if (!"ADMIN".equals(loginUser.getRole())){
            return "管理者権限が必要です。";
        }
        try{
            shiftService.createShift(employeeId, workDate, startTime, endTime);
            return "シフトが作成されました。";
        }
        catch (IllegalArgumentException e){
            return e.getMessage();
        }
    }

    @GetMapping("/my")
    public Object getMyShifts(HttpSession session){
        Employee loginUser = (Employee) session.getAttribute("employee");
        if (loginUser == null){
            return "ログインが必要です。";
        }
        return shiftService.getMyShifts(loginUser);
    }

    @GetMapping("/month")
    public Object getMonthlyShifts(@RequestParam int year,
                                   @RequestParam int month,
                                   HttpSession session){
        Employee loginUser = (Employee) session.getAttribute("employee");
        if (loginUser == null){
            return "ログインが必要です。";
        }
        try{
            return shiftService.getMonthlyShifts(loginUser, year, month);
        }
        catch (IllegalArgumentException e){
            return e.getMessage();
        }
    }

    @GetMapping("/calendar")
    public Object getMonthlyCalendar(@RequestParam int year,
                                     @RequestParam int month,
                                     HttpSession session){
        Employee loginUser = (Employee) session.getAttribute("employee");
        if(loginUser == null){
            return "ログインが必要です。";
        }
        try{
            return shiftService.getMonthlyCalendar(loginUser, year, month);
        }
        catch (IllegalArgumentException e){
            return e.getMessage();
        }
    }

    @GetMapping("/manage-calendar")
    public List<CalendarDayDto> getManageCalendar(@RequestParam int year,
                                                  @RequestParam int month,
                                                  HttpSession session) {
        Employee loginUser = (Employee) session.getAttribute("employee");
        if (loginUser == null) {
            throw new RuntimeException("ログインが必要です。");
        }
        if (!"ADMIN".equals(loginUser.getRole())) {
            throw new RuntimeException("管理者権限が必要です。");
        }
        LocalDate firstDay = LocalDate.of(year, month, 1);
        LocalDate lastDay = firstDay.withDayOfMonth(firstDay.lengthOfMonth());
        List<Shift> shifts = shiftRepository.findByWorkDateBetweenAndEmployeeActiveTrue(firstDay, lastDay);
        Map<LocalDate, List<Shift>> map =
                shifts.stream().collect(Collectors.groupingBy(Shift::getWorkDate));
        LocalDate today = LocalDate.now();
        List<CalendarDayDto> result = new ArrayList<>();
        for (LocalDate d = firstDay; !d.isAfter(lastDay); d = d.plusDays(1)) {
            String status = "NONE";
            int count = 0;
            if (d.isBefore(today)){
                count = attendanceRepository.countByWorkDateAndEmployeeActiveTrue(d);
                if (count > 0){
                    status = "COMPLETED";
                }
            }
            else{
                count = shiftRepository.countByWorkDateAndEmployeeActiveTrue(d);
                if (count > 0){
                    status = "SCHEDULED";
                }
            }
            result.add(new CalendarDayDto(d, status, count));
        }
        return result;
    }

    @GetMapping("/detail")
    public Object getDayDetail(@RequestParam String date,
                               HttpSession session){
        Employee loginUser = (Employee) session.getAttribute("employee");
        if (loginUser == null){
            return "ログインが必要です。";
        }
        try{
            LocalDate localDate = LocalDate.parse(date);
            return shiftService.getDayDetail(loginUser, localDate);
        }
        catch (Exception e){
            return e.getMessage();
        }
    }

    @GetMapping("/day")
    public Object getShiftByDate(@RequestParam String date,
                                 HttpSession session){
        Employee loginUser = (Employee) session.getAttribute("employee");
        if (loginUser == null){
            return "ログインが必要です。";
        }
        if (!"ADMIN".equals(loginUser.getRole())){
            return "管理者権限が必要です。";
        }
        LocalDate workDate = LocalDate.parse(date);
        LocalDate today = LocalDate.now();
        if (workDate.isBefore(today)){
            List<Attendance> list =
                    attendanceRepository.findByWorkDateAndEmployeeActiveTrue(workDate);
            return list.stream()
                    .filter(a -> a.getCheckIn() != null && a.getCheckOut() != null)
                    .sorted(Comparator.comparing(Attendance::getCheckIn))
                    .map(a -> {
                        Shift shift = shiftRepository
                                .findByEmployeeAndWorkDate(a.getEmployee(), workDate)
                                .stream().findFirst().orElse(null);
                        LocalTime start = shift != null ? shift.getStartTime() : null;
                        LocalTime end = shift != null ? shift.getEndTime() : null;
                        return new ShiftDto(
                                a.getId(),
                                a.getEmployee().getName(),
                                start,
                                end,
                                a.getCheckIn().toLocalTime(),
                                a.getCheckOut().toLocalTime()
                        );
                    })
                    .toList();
        }
        List<Shift> shifts =
                shiftRepository.findByWorkDateAndEmployeeActiveTrue(workDate);
        return shifts.stream()
                .sorted(Comparator.comparing(Shift::getStartTime))
                .map(s -> new ShiftDto(
                        s.getId(),
                        s.getEmployee().getName(),
                        s.getStartTime(),
                        s.getEndTime(),
                        null,
                        null
                ))
                .toList();
    }

    @DeleteMapping("/{id}")
    public String deleteShift(@PathVariable Long id, HttpSession session){
        Employee loginUser = (Employee) session.getAttribute("employee");
        if (loginUser == null){
            return "ログインが必要です。";
        }
        if (!"ADMIN".equals(loginUser.getRole())){
            return "管理者権限が必要です。";
        }
        Shift shift = shiftRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("シフトが存在しません。"));
        if (shift.getWorkDate().isBefore(LocalDate.now())){
            throw new RuntimeException("過去のシフトは削除できません。");
        }
        shiftRepository.delete(shift);
        return "deleted";
    }
}
