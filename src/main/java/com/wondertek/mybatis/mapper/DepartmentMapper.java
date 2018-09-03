package com.wondertek.mybatis.mapper;

import com.wondertek.mybatis.domain.Department;

public interface DepartmentMapper {

    /**
     * 查询部门信息的同时，将部门下的员工信息同时查询出来
     * @param id
     * @return
     */
    Department getDeptByIdPlus(Integer id);

    Department getDeptByIdStep(Integer id);
}
