package com.wondertek.mybatis.mapper;

import com.wondertek.mybatis.domain.Employee;

import java.util.List;

public interface EmployeeMapperPlus {

    public Employee getEmpById(Integer id);

    public Employee getEmpAndTemp(Integer id);

    public List<Employee> getEmpsByDeptId(Integer deptId);

}
