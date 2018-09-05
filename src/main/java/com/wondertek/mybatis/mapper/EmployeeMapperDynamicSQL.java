package com.wondertek.mybatis.mapper;

import com.wondertek.mybatis.domain.Employee;
import org.apache.ibatis.annotations.Param;

import java.util.List;

public interface EmployeeMapperDynamicSQL {

    List<Employee> getEmpsByConditionIf(Employee employee);

    List<Employee> getEmpsByConditionByTrim(Employee employee);

    List<Employee> getEmpsByConditionByChoose(Employee employee);

    /**
     * 使用动态sql的foreach功能
     * @param ids
     * @return
     */
    List<Employee> getEmpByConditionByForeach(@Param("ids") List<Integer> ids);

    void updateEmp(Employee employee);

    /**
     * 批量添加数据
     * @param employees
     */
    void addEmps(@Param("employees") List<Employee> employees);
}
