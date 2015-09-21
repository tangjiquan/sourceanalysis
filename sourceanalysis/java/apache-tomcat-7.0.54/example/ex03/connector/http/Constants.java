package ex03.connector.http;

import java.io.File;

public class Constants {
	public static final String WEB_ROOT = System.getProperty("user.dir")+File.separator+"webroot";
	public static final String Package = "ex03.connector.http";
	public static final int DEFAULT_CONNECTION_TIMEOUT = 60000;
	public static final int PROCESSOR_IDLE = 0;
	public static final int PROCESSPR_ACTIVE = 1;
}
