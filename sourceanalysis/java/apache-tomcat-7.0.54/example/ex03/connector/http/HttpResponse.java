package ex03.connector.http;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Locale;

import javax.servlet.ServletOutputStream;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletResponse;

import ex03.connector.ResponseStream;

public class HttpResponse {
	private static final int BUFFER_SIZE = 1024;//默认缓存的大小
	PrintWriter writer;
	
	HttpRequest request;
	OutputStream output;
	
	protected byte[] buffer = new byte[BUFFER_SIZE];
	protected int bufferCount = 0;
	
	protected boolean committed = false;
	protected int contentCount = 0;
	protected int contentLength = -1;
	protected String contentType = null;
	protected String encoding = null;
	protected ArrayList cookies = new ArrayList();
	protected HashMap headers = new HashMap();
	protected final SimpleDateFormat format = new SimpleDateFormat("EEE, dd MMM yyyy HH:mm:ss zzz", Locale.US);
	protected String message = getStatusMessage(HttpServletResponse.SC_OK);
	protected int status = HttpServletResponse.SC_OK;
	
	public HttpResponse(OutputStream output){
		this.output = output;
	}
	
	public void finishResponse(){
		if(writer != null){
			writer.flush();
			writer.close();
		}
	}
	
	public int getContentLength(){
		return contentLength;
	}
	
	public String getContentType(){
		return contentType;
	}
	
	protected String getProtocol(){
		return request.getProtocol();
	}
	
	
	protected String getStatusMessage(int status) {/*
	    switch (status) {
	      case SC_OK:
	        return ("OK");
	      case SC_ACCEPTED:
	        return ("Accepted");
	      case SC_BAD_GATEWAY:
	        return ("Bad Gateway");
	      case SC_BAD_REQUEST:
	        return ("Bad Request");
	      case SC_CONFLICT:
	        return ("Conflict");
	      case SC_CONTINUE:
	        return ("Continue");
	      case SC_CREATED:
	        return ("Created");
	      case SC_EXPECTATION_FAILED:
	        return ("Expectation Failed");
	      case SC_FORBIDDEN:
	        return ("Forbidden");
	      case SC_GATEWAY_TIMEOUT:
	        return ("Gateway Timeout");
	      case SC_GONE:
	        return ("Gone");
	      case SC_HTTP_VERSION_NOT_SUPPORTED:
	        return ("HTTP Version Not Supported");
	      case SC_INTERNAL_SERVER_ERROR:
	        return ("Internal Server Error");
	      case SC_LENGTH_REQUIRED:
	        return ("Length Required");
	      case SC_METHOD_NOT_ALLOWED:
	        return ("Method Not Allowed");
	      case SC_MOVED_PERMANENTLY:
	        return ("Moved Permanently");
	      case SC_MOVED_TEMPORARILY:
	        return ("Moved Temporarily");
	      case SC_MULTIPLE_CHOICES:
	        return ("Multiple Choices");
	      case SC_NO_CONTENT:
	        return ("No Content");
	      case SC_NON_AUTHORITATIVE_INFORMATION:
	        return ("Non-Authoritative Information");
	      case SC_NOT_ACCEPTABLE:
	        return ("Not Acceptable");
	      case SC_NOT_FOUND:
	        return ("Not Found");
	      case SC_NOT_IMPLEMENTED:
	        return ("Not Implemented");
	      case SC_NOT_MODIFIED:
	        return ("Not Modified");
	      case SC_PARTIAL_CONTENT:
	        return ("Partial Content");
	      case SC_PAYMENT_REQUIRED:
	        return ("Payment Required");
	      case SC_PRECONDITION_FAILED:
	        return ("Precondition Failed");
	      case SC_PROXY_AUTHENTICATION_REQUIRED:
	        return ("Proxy Authentication Required");
	      case SC_REQUEST_ENTITY_TOO_LARGE:
	        return ("Request Entity Too Large");
	      case SC_REQUEST_TIMEOUT:
	        return ("Request Timeout");
	      case SC_REQUEST_URI_TOO_LONG:
	        return ("Request URI Too Long");
	      case SC_REQUESTED_RANGE_NOT_SATISFIABLE:
	        return ("Requested Range Not Satisfiable");
	      case SC_RESET_CONTENT:
	        return ("Reset Content");
	      case SC_SEE_OTHER:
	        return ("See Other");
	      case SC_SERVICE_UNAVAILABLE:
	        return ("Service Unavailable");
	      case SC_SWITCHING_PROTOCOLS:
	        return ("Switching Protocols");
	      case SC_UNAUTHORIZED:
	        return ("Unauthorized");
	      case SC_UNSUPPORTED_MEDIA_TYPE:
	        return ("Unsupported Media Type");
	      case SC_USE_PROXY:
	        return ("Use Proxy");
	      case 207:       // WebDAV
	        return ("Multi-Status");
	      case 422:       // WebDAV
	        return ("Unprocessable Entity");
	      case 423:       // WebDAV
	        return ("Locked");
	      case 507:       // WebDAV
	        return ("Insufficient Storage");
	      default:
	        return ("HTTP Response Status " + status);
	    }
	  */}
	
	public OutputStream getStream(){
		return this.output;
	}
	
	protected void sendHeaders(){
		if(isCommitted()){
			return;
		}
		OutputStreamWriter osr = null;
		osr = new OutputStreamWriter(getStream(), getCharacterEncoding());
		final PrintWriter outputWriter = new PrintWriter(osr);
		outputWriter.print(this.getProtocol());
		outputWriter.print(" ");
		outputWriter.print(status);
		if(message != null){
			outputWriter.print(" ");
			outputWriter.print(message);
		}
		outputWriter.print("\r\n");
		if(getContentType() != null){
			outputWriter.print("Content-Type: " + getContentType() + "\r\n");
		}
		if(getContentLength() > 0){
			outputWriter.print("Content-Length: " + getContentLength() + "\r\n");
		}
		
		synchronized (headers) {
			Iterator names = headers.keySet().iterator();
			while(names.hasNext()){
				String name = (String)names.next();
				ArrayList values = (ArrayList)headers.get(name);
				Iterator items = values.iterator();
				while(items.hasNext()){
					String value = (String)items.next();
					outputWriter.print(name);
					outputWriter.print(" ");
					outputWriter.print(value);
					outputWriter.print("\r\n");
					
				}
			}
		}
		
//		synchronized (cookies) {
//			Iterator items = cookies.iterator();
//			while(items.hasNext()){
//				Cookie cookie = (Cookie)items.next();
//			}
//		}
		outputWriter.print("\r\n");
		outputWriter.flush();
		committed = true;
	}

	public void setRequest(HttpRequest request){
		this.request = request;
	}
	
	public void sendStaticResource() throws IOException{
		byte[] bytes = new byte[BUFFER_SIZE];
		FileInputStream fis = null;
		File file = new File(Constants.WEB_ROOT, request.getRequestURI());
		try {
			fis = new FileInputStream(file);
			int ch = fis.read(bytes, 0, BUFFER_SIZE);
			while(ch != -1){
				output.write(bytes, 0, BUFFER_SIZE);
				ch = fis.read(bytes, 0, BUFFER_SIZE);
			}
		} catch (FileNotFoundException e) {
			String errorMessage = "HTTP/1.1 404 File Not Found\r\n"+
					"Content-Type:text/html\r\n"+
					"\r\n"+
					"<h1>File Not Found";
			output.write(errorMessage.getBytes());
		}finally{
			if(fis != null){
				fis.close();
			}
		}
	}
	
	public void wirter(int b){
		if(bufferCount >= buffer.length){
			flushBuffer();
			buffer[bufferCount++] = (byte)b;
			contentCount++;
		}
	}
	
	public void write(byte[] b){
		write(b,0, b.length);
	}
	private void write(byte[] b, int off, int length) {
		if(length == 0){
			return;
		}
		if(length<=(buffer.length - bufferCount)){
			System.arraycopy(b, off, buffer, bufferCount, length);
			bufferCount += length;
			contentCount += length;
			return;
		}
		
		flushBuffer();
		int iterations = length/buffer.length;
		int leftoverStart = iterations * buffer.length;
		int leftoverLen = length - leftoverStart;
		for(int i=0; i<buffer.length; i++){
			write(b, off+(i * buffer.length), buffer.length);
		}
		if(leftoverLen > 0){
			write(b, off+leftoverStart, leftoverLen);
		}
		
	}

	public void addCookie(Cookie cookie){
		if(isCommitted()){
			return;
		}
		synchronized (cookies) {
			cookies.add(cookie);
		}
	}
	
	public void addHeader(String name, String value){
		if(isCommitted()){
			return;
		}
		synchronized (headers) {
			ArrayList values = (ArrayList)headers.get(name);
			if(values == null){
				values = new ArrayList();
				headers.put(name, value);
			}
			values.add(value);
		}
	}	
	
	public void addDateHeader(String name, long value){
		if(isCommitted()){
			return;
		}
		addHeader(name, format.format(new Date(value)));
	}
	
	public void addIntHeader(String name, int value){
		if(isCommitted()){
			return;
		}
		addHeader(name, ""+value);
	}
	
	public boolean containsHeader(String name){
		synchronized (headers) {
			return headers.get(name)!=null;
		}
	}
	
	 public String encodeRedirectURL(String url) {
		    return null;
	  }

	  public String encodeRedirectUrl(String url) {
	    return encodeRedirectURL(url);
	  }

	  public String encodeUrl(String url) {
	    return encodeURL(url);
	  }

	  public String encodeURL(String url) {
	    return null;
	  }
	
	public void flushBuffer() {
		if(bufferCount > 0){
			try {
				output.write(buffer, 0, bufferCount);
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}finally{
				bufferCount = 0;
			}
		}
	}
	
	
	public int getBufferSize(){
		return 0;
	}
	
	
	private String getCharacterEncoding() {
		if(encoding == null){
			return "UTF-8";
		}
		return encoding;
	}
	
	public Locale getLocale() {
	    return null;
	}

	public ServletOutputStream getOutputStream() throws IOException {
	    return null;
	}
	
	public PrintWriter getWriter(){
		ResponseStream newStream = new ResponseStream(this);
		
	}
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
}
