package com.wondertek.mybatis.common;

import com.wondertek.mybatis.domain.Employee;
import org.apache.ibatis.io.Resources;
import org.apache.ibatis.session.SqlSession;
import org.apache.ibatis.session.SqlSessionFactory;
import org.apache.ibatis.session.SqlSessionFactoryBuilder;
import org.junit.Test;

import java.io.IOException;
import java.io.InputStream;

/**
 * mybatis初使用
 */
public class MybatisTest {

    /**
     * mybatis hello-world使用
     * @throws IOException
     */
    @Test
    public void test1() throws IOException {
        String resourcePath = "mybatis-config.xml";
        InputStream inputStream = Resources.getResourceAsStream(resourcePath);

        SqlSessionFactory sqlSessionFactory = new SqlSessionFactoryBuilder().build(inputStream);

        //获取sqlSession对象
        SqlSession sqlSession = sqlSessionFactory.openSession();

        try {
            Employee employee = sqlSession.selectOne("org.mybatis.example.EmployeeMapper.selectEmp", 1);
            System.out.println(employee);
        } finally {
            sqlSession.close();
        }


    }
}
