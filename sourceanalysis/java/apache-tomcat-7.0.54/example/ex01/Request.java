package ex01;

import java.io.IOException;
import java.io.InputStream;

public class Request {
	private InputStream input;
	private String uri;
	
	public Request(InputStream input){
		this.input = input;
	}
	
	public void parse(){
		StringBuffer requestStr = new StringBuffer();
		int i = 0;
		byte[] buffer = new byte[2048];
		try {
			i = input.read(buffer);
		} catch (IOException e) {
			e.printStackTrace();
		}
		for(int j=0; j<i; j++){
			requestStr.append((char)buffer[j]);
		}
		System.out.println(requestStr.toString());
		uri = parseUri(requestStr.toString());
	}

	//解析输入流，返回uri
	private String parseUri(String requestStr) {
		int index1, index2;
		index1 = requestStr.indexOf(' ');
		if(index1 != -1){
			index2 = requestStr.indexOf(' ', index1+1);
			if(index2>index1){
				return requestStr.substring(index1+1, index2);
			}
		}
		return null;
	}
	
	public String getUri(){
		return uri;
	}
}
