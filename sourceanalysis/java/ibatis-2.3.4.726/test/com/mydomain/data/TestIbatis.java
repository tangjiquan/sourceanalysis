package com.mydomain.data;

import java.sql.SQLException;
import java.util.List;

import org.junit.Test;

import com.mydomain.domain.Account;

public class TestIbatis {
	
	@Test
	public void test1() throws SQLException{
		List<Account> list = SimpleExample.selectAllAccounts();
		for(Account account : list){
			System.out.print(account);
		}
	}
}
