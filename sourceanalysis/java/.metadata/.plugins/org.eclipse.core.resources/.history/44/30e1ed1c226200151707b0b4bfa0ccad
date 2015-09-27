package ex03.connector.http;

import java.io.IOException;
import java.io.InputStream;

import org.apache.catalina.tribes.util.StringManager;

public class SocketInputStream extends InputStream {
	// CR
	private static final byte CR = (byte) '\r';
	// LF
	private static final byte LF = (byte) '\n';
	// SP
	private static final byte SP = (byte) ' ';
	// HT
	private static final byte HT = (byte) '\t';
	// COLON
	private static final byte COLON = (byte) ':';
	// Lower case offset
	private static final byte LC_OFFSET = 'A'-'a';
	
	protected byte buf[];
	
	protected int count;
	
	protected int pos;
	
	protected InputStream is;
	
	public SocketInputStream(InputStream is, int bufferSize){
		this.is = is;
		buf = new byte[bufferSize];
	}

	protected static StringManager sm = StringManager.getManager(Constants.Package);
	
	public void readRequestLine(HttpRequestLine requestLine){
		
	}
	
	
	@Override
	public int read() throws IOException {
		// TODO Auto-generated method stub
		return 0;
	}

}
