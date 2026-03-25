package org.qrkanri.repository;

import org.qrkanri.entity.Employee;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface EmployeeRepository extends JpaRepository<Employee, Long> {
    Optional<Employee> findByLoginidAndActiveTrue(String loginid);
    List<Employee> findByNameContainingAndActiveTrueOrderByIdAsc(String name);
    List<Employee> findByLoginidContainingAndActiveTrueOrderByIdAsc(String loginid);
    List<Employee> findByPhoneContainingAndActiveTrueOrderByIdAsc(String phone);
    List<Employee> findByEmailContainingAndActiveTrueOrderByIdAsc(String email);
    List<Employee> findByActiveTrueOrderByIdAsc();
    boolean existsByLoginid(String loginid);
}