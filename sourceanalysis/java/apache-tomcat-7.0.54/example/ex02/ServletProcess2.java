package ex02;

import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.net.URLStreamHandler;

import javax.servlet.Servlet;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;

public class ServletProcess2 {
	public void process(Request request, Response response){
		String uri = request.getUri();
		String servletName = uri.substring(uri.lastIndexOf("/")+1);
		URLClassLoader loader = null;
		URL[] urls = new URL[1];
		URLStreamHandler streamHandler = null;
		File classPath  = new File(Constants.WEB_ROOT);
		
		try {
			String repository = (new URL("file", null, classPath.getCanonicalPath() + File.separator)).toString() ;
			urls[0] = new URL(null, repository, streamHandler);
			loader = new URLClassLoader(urls);
		} catch (MalformedURLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		Class clazz = null;
		try {
			clazz = loader.loadClass(servletName);
		} catch (ClassNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		try {
			Servlet servlet = (Servlet) clazz.newInstance();
			try {
				servlet.service(new RequestFacade(request), new ResponseFacade(response));
			} catch (ServletException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		} catch (InstantiationException | IllegalAccessException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
	}
}
