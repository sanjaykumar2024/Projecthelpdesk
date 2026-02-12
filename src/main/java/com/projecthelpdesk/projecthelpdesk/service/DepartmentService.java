package com.projecthelpdesk.projecthelpdesk.service;

import java.util.List;

import org.springframework.stereotype.Service;

import com.projecthelpdesk.projecthelpdesk.entity.Department;
import com.projecthelpdesk.projecthelpdesk.repository.DepartmentRepository;

@Service
public class DepartmentService {

    private final DepartmentRepository departmentRepository;

    public DepartmentService(DepartmentRepository departmentRepository) {
        this.departmentRepository = departmentRepository;
    }

    public Department createDepartment(String name, String description) {
        Department dept = new Department();
        dept.setName(name);
        dept.setDescription(description);
        return departmentRepository.save(dept);
    }

    public List<Department> getAllDepartments() {
        return departmentRepository.findAll();
    }
}
