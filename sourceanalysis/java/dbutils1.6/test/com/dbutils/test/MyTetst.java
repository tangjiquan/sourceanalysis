package com.dbutils.test;

import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.apache.commons.dbutils.QueryRunner;
import org.apache.commons.dbutils.ResultSetHandler;
import org.apache.commons.dbutils.handlers.ArrayHandler;
import org.apache.commons.dbutils.handlers.ArrayListHandler;
import org.apache.commons.dbutils.handlers.ColumnListHandler;
import org.apache.commons.dbutils.handlers.MapHandler;
import org.apache.commons.dbutils.handlers.ScalarHandler;

import com.mysql.jdbc.Connection;

public class MyTetst {
	
	
	
	public static void TestDbutils(Connection conn) throws SQLException{
		QueryRunner queryRunner = new QueryRunner();
		String sql = null;
		
		
		//1. 返回POJO对象列表
	/*	ResultSetHandler<List<User>> handler = new BeanListHandler<User>(User.class);
		List<User> result = null;
		sql = "select * from t_user";
		result = queryRunner.query(conn, sql, handler);
		for(User u : result){
			System.out.println(u);
		}*/
		
		//2. 返回POJO对象
		/*ResultSetHandler<User> handler = new BeanHandler<User>(User.class);
		sql = "select * from t_user where id = 1";
		User result = null;
		result = queryRunner.query(conn, sql, handler);
		System.out.println(result);*/
		
		//3. 返回Map列表
		/*ResultSetHandler<List<Map<String, Object>>> handler = new MapListHandler();
		sql = "select * from t_user ";
		List<Map<String, Object>> result = null;
		result = queryRunner.query(conn, sql, handler);
		for(Map<String, Object> map : result){
			System.out.println(map);
		}*/
		
		//4. 返回单个Map
		/*ResultSetHandler<Map<String, Object>> handler = new MapHandler();
		sql = "select * from t_user where id = 1";
		Map<String, Object> result = null;
		result = queryRunner.query(conn, sql, handler);
		System.out.println(result);*/
		
		//5. 使用Array处理多个数据
		/*ResultSetHandler<List<Object[]>> handler = new ArrayListHandler();
		sql = "select * from t_user";
		List<Object[]> result = null;
		result = queryRunner.query(conn,sql, handler);
		for(Iterator<Object[]> iter = result.iterator(); iter.hasNext();){
			Object[] obj = iter.next();
			int objSize = obj.length;
			for(int i=0; i<objSize; i++){
				System.out.println(obj[i]);
			}
		}*/
		
		
		//6. 使用Array处理单个数据
		/*ResultSetHandler<Object[]> handler = new ArrayHandler();
		sql = "select * from t_user where id=1";
		Object[] result = null;
		result = queryRunner.query(conn, sql, handler);
		int resultSize = result.length;
		for(int i=0; i<resultSize; i++){
			System.out.println(result[i]);
		}
			*/
		
		//7. 使用ColumnListHander 处理单行记录，并返回指定的一列的值
		/*ResultSetHandler<List<Object>> handler = new ColumnListHandler("nickname");
		sql = "select * from t_user";
		List<Object> result = null;
		result = queryRunner.query(conn, sql, handler);
		for(Object o : result){
			System.out.println(o);
		}*/
		
		//8. ScalarHandler处理单行记录，只返回结果集第一行中的指定字段，如未指定字段，则返回第一个字段！
		ResultSetHandler<Object> handler = new ScalarHandler("nickname");
		sql = "select * from t_user";
		Object result = null;
		result = queryRunner.query(conn, sql, handler);
		System.out.println(result);
		
		
	}
	
	
	
	
	
	public static void main(String[] args) {
		String driverName = "com.mysql.jdbc.Driver";
		String url = "jdbc:mysql://127.0.0.1:3306/itat_dbunit";
		String username = "root";
		String password = "";
		Connection conn = null;
		try {
			Class.forName(driverName);
		} catch (ClassNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		try {
			conn = (Connection) DriverManager.getConnection(url, username, password);
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		
		try {
			TestDbutils(conn);
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
	}
}
