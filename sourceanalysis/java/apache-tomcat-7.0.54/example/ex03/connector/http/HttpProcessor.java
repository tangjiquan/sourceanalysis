package ex03.connector.http;

import java.io.IOException;
import java.io.OutputStream;
import java.net.Socket;

import org.apache.catalina.tribes.util.StringManager;

public class HttpProcessor {
	
	private HttpConnector connector = null;
	private HttpRequest request;
	private HttpResponse response;
	private HttpRequestLine requestLine = new HttpRequestLine();
	
	protected String method = null;
	protected String queryString = null;
	
	protected StringManager sm = StringManager.getManager("ex03.connector.http");
	
	public HttpProcessor(HttpConnector connector){
		this.connector = connector;
	}
	public void process(Socket socket) {
		SocketInputStream input = null;
		OutputStream output = null;
		try {
			input = new SocketInputStream(socket.getInputStream(), 2048);
			output = socket.getOutputStream();
			
			request = new HttpRequest(input);
			response = new HttpResponse(output);
			
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
}
