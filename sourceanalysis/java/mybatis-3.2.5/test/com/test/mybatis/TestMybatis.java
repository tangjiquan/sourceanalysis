package com.test.mybatis;

import java.io.InputStream;

import org.apache.ibatis.session.SqlSession;
import org.apache.ibatis.session.SqlSessionFactory;
import org.apache.ibatis.session.SqlSessionFactoryBuilder;
import org.junit.Test;

import com.test.mybatis.domain.User;

public class TestMybatis {
	
	@Test
	public void testSql(){
		String resources = "conf.xml";
		InputStream is = TestMybatis.class.getClassLoader().getResourceAsStream(resources);
		//构建sqlSession工厂
		SqlSessionFactory sessionFactory = new SqlSessionFactoryBuilder().build(is);
		//创建能执行sql的sqlSession
		SqlSession session = sessionFactory.openSession();
		/**
         * 映射sql的标识字符串，
         * com.tang.mybatis.userMapper是userMapper.xml文件中mapper标签的namespace属性的值，
         * getUser是select标签的id属性值，通过select标签的id属性值就可以找到要执行的SQL
         */
		String statement = "com.test.mybatis.userMapper.getUser";
		User user = session.selectOne(statement, 1);
		System.out.println(user);
	}
}
