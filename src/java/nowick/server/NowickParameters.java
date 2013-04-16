package nowick.server;

public interface NowickParameters {

	// Jetty setup parameters
	public static final String JETTY = "jetty";
	public static final String NUM_CONNECTIONS = "num.connections";
	public static final String USE_SSL_AUTHENTICATION = "use.ssl.authentication";
	public static final String PORT = "port";
	public static final String RESOURCE_DIR = "resource.dir";
	public static final String MAX_FORM_CONTEXT_SIZE = "max.form.content.size";
	public static final String MAX_HEADER_CONTEXT_SIZE = "max.header.buffer.size";
	
	// For setup
	public static final String JETTY_SSL = "jetty.ssl";
	public static final String JETTY_SSL_KEYSTORE = "keystore";
	public static final String JETTY_SSL_PASSWORD = "password";
	public static final String JETTY_SSL_KEYPASSWORD = "keypassword";
	public static final String JETTY_SSL_TRUSTSTORE = "truststore";
	public static final String JETTY_SSL_TRUSTPASSWORD = "trustpassword";

	// Defaults
	public static final int DEFAULT_NUM_THREADS = 25;
	public static final int DEFAULT_PORT = 180;
	public static final int DEFAULT_AUTHENTICATION_PORT = 443;
	public static final int DEFAULT_MAX_FORM_CONTENT_SIZE = 10*1024*1024;
	public static final int DEFAULT_MAX_HEADER_BUFFER_SIZE = 10*1024*1024;
	public static final String DEFAULT_RESOURCE_DIR = "web/";
	public static final String MAIN_CONNECTOR_NAME = "MainConnector";
	public static final String SSL_CONNECTOR_NAME = "SslConnector";
	
	// User manager
	public static final String USER_MANAGER = "user.manager";
	public static final String USER_MANAGER_CLASS = "user.manager.class";
}
