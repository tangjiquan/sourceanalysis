package ex02;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;

/**
 * 模拟服务器
 * @author tangjiquan
 *
 */
public class HttpServer2 {
	//shutdown command
	private static final String SHUTDOWN_COMMAND = "/SHUTDOWN";
	
	//有没有接收到shutdown命令 （false-没有接收到，true-接收到）
	private boolean shutdown = false;
	
	public static void main(String[] args){
		HttpServer2 server = new HttpServer2();
		server.await();
	}

	private void await() {
		ServerSocket serverSocket = null;
		int port = 8080;
		try {
			serverSocket = new ServerSocket(port, 1, InetAddress.getByName("127.0.0.1"));
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		while(!shutdown){
			Socket socket = null;
			InputStream input = null;
			OutputStream output = null;
			
			try {
				socket = serverSocket.accept();
				input = socket.getInputStream();
				output = socket.getOutputStream();
				
				//创建请求对象
				Request request = new Request(input);
				request.parse();
				//创建响应对象
				Response response = new Response(output);
				response.setRequest(request);
				//response.sendStaticResource();
				
				if(request.getUri().startsWith("/servlet")){
					ServletProcess1 process = new ServletProcess1();
					process.process(request, response);
				}else{
					StaticResourceProcessor process = new StaticResourceProcessor();
					process.process(request, response);
				}
				
				//关闭socket对象
				socket.close();
				
				shutdown = request.getUri().equals(SHUTDOWN_COMMAND);
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		
	}
	
}