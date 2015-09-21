package ex03.connector.http;

public class HttpRequestLine {
	public static final int INITIAL_METHOD_SIZE = 8;
	public static final int INITIAL_URI_SIZE = 64;
	public static final int INITIAL_PROTOCOL_SIZE = 8;
	public static final int MAX_METHOD_SIZE = 1024;
	public static final int MAX_URI_SIZE = 32768;
	public static final int MAX_PROTOCOL_SIZE = 1024;
	
	public char[] method;
	public int methodEnd;
	public char[] uri;
	public int uriEnd;
	public char[] protocol;
	public int protocolEnd;
	
	public HttpRequestLine(char[] method, int methodEnd, char[] uri, int uriEnd, char[] protocol, int protocolEnd){
		this.method = method;
		this.methodEnd = methodEnd;
		this.uri = uri;
		this.uriEnd = uriEnd;
		this.protocol = protocol;
		this.protocolEnd = protocolEnd;
		
	}
	
	public HttpRequestLine(){
		this(new char[INITIAL_METHOD_SIZE], 0, new char[INITIAL_URI_SIZE], 0, new char[INITIAL_PROTOCOL_SIZE], 0);
	}
	
	
	public void recyle(){
		methodEnd = 0;
		uriEnd = 0;
		protocolEnd = 0;
	}
	
	public int indexOf(char[] buf){
		return indexOf(buf, buf.length);
	}
	
	public int indexOf(char[] buf, int end){
		char firstChar = buf[0];
		
	}
	
	public int hashCode(){
		return 0;
	}
	
	public boolean equals(Object obj){
		return false;
	}
	
	
	
	
}
