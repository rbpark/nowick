package nowick.server;

public interface NowickParameters {

	// Jetty setup parameters
	public static final String NUM_CONNECTIONS = "num.connections";
	public static final String PORT = "port";

	public static final String MAX_FORM_CONTEXT_SIZE = "max.form.content.size";
	public static final String MAX_HEADER_CONTEXT_SIZE = "max.header.buffer.size";

	// Defaults
	public static final int DEFAULT_NUM_THREADS = 25;
	public static final int DEFAULT_PORT = 180;
	public static final int DEFAULT_AUTHENTICATION_PORT = 443;
	public static final int DEFAULT_MAX_FORM_CONTENT_SIZE = 10*1024*1024;
	public static final int DEFAULT_MAX_HEADER_BUFFER_SIZE = 10*1024*1024;
	public static final String DEFAULT_RESOURCE_DIR = "web/";
}
