package com.wondertek.mybatis.mapper;

import com.wondertek.mybatis.domain.Employee;

public interface EmployeeMapper {

    public Employee getEmpById(Integer id);

    public void addEmp(Employee employee);

    public Boolean updateEmp(Employee employee);

    public void deleteEmp(Integer id);

}
