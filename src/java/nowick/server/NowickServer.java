package nowick.server;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Arrays;
import java.util.Properties;

import joptsimple.OptionParser;
import joptsimple.OptionSet;
import joptsimple.OptionSpec;

import nowick.utils.PropertiesWrapper;

import org.apache.commons.io.IOUtils;
import org.apache.log4j.Logger;
import org.mortbay.jetty.Server;
import org.mortbay.jetty.security.SslSocketConnector;
import org.mortbay.jetty.servlet.Context;
import org.mortbay.thread.QueuedThreadPool;

public class NowickServer {
	private static final Logger logger = Logger.getLogger(NowickServer.class);
	private static final String CONF_FILE_NAME = "nowick.xml";
	
	// Parameters for jetty's thread
	private static final String NUM_THREADS_PARAM = "jetty.num.threads";
	private static final int DEFAULT_NUM_THREADS = 25;

	// Parameters for authentication port
	private static final String AUTHENTICATION_PORT_PARAM = "authentication.port";
	private static final int DEFAULT_AUTHENTICATION_PORT = 443;
	
	
	private Properties settings;
	private final Server server;
	
	public static void main(String[] args) {
		OptionParser parser = new OptionParser();

		OptionSpec<String> configDirectory = parser
				.acceptsAll(Arrays.asList("c", "conf"), "The conf dir for Nowick.")
				.withRequiredArg()
				.describedAs("conf").ofType(String.class);
		
		OptionSpec<String> dataDirectory = parser
				.acceptsAll(Arrays.asList("d", "data"), "The data dir for Nowick.")
				.withRequiredArg()
				.describedAs("data").ofType(String.class);

		OptionSet options = parser.parse(args);
		
		File confPath = null;
		if (options.has(configDirectory)) {
			String path = options.valueOf(configDirectory);
			confPath = new File(path);
		}
		else {
			// Use default
			confPath = new File("conf");
		}
		
		Properties props = getPropertiesFromPath(confPath);
		if (props == null) {
			logger.error("Properties not found.");
		}
		else {
			NowickServer server = new NowickServer(props);
		}
	}

	private static Properties getPropertiesFromPath(File path) {
		logger.info("Loading nowick settings file from " + path);
		if (!path.exists()) {
			logger.error("Conf directory " + path.getPath() + " doesn't exist.");
			return null;
		}
		else if (!path.isDirectory()) {
			// Load from file if it's a file.
			return getProperties(path);
		}
		else {
			File xmlFile = new File(path, CONF_FILE_NAME);
			return getProperties(xmlFile);
		}
	}
	
	private static Properties getProperties(File confPath) {
		if (!confPath.exists()) {
			logger.error("Conf directory " + confPath + " doesn't exist.");
			return null;
		}
		
		File confFile = confPath;
		if (confPath.isDirectory()) {
			confFile = new File(confPath, CONF_FILE_NAME);
			if (!confFile.exists()) {
				logger.error("Conf file " + confFile + " doesn't exist.");
				return null;
			}
		}

		Properties props = new Properties();
		BufferedInputStream stream = null;
		try {
			stream = new BufferedInputStream(new FileInputStream(confFile));
			props.loadFromXML(stream);
		}
		catch (IOException e) {
			logger.error("Error loading conf file", e);
		}
		finally {
			IOUtils.closeQuietly(stream);
		}
		
		return props;
	}
	
	public NowickServer(Properties props) {
		server = new Server();
//		
//		PropertiesWrapper wrapper = new PropertiesWrapper(props);
//		
//		Context root = new Context(server, "/", Context.SESSIONS);
//		QueuedThreadPool httpThreadPool = new QueuedThreadPool(wrapper.getInt(NUM_THREADS_PARAM, DEFAULT_NUM_THREADS));
//		server.setThreadPool(httpThreadPool);
//		
//		SslSocketConnector secureConnector = new SslSocketConnector();
//		secureConnector.setPort(wrapper.getInt(AUTHENTICATION_PORT_PARAM, DEFAULT_AUTHENTICATION_PORT));
//		secureConnector.setKeystore(azkabanSettings.getString("jetty.keystore"));
//		secureConnector.setPassword(azkabanSettings.getString("jetty.password"));
//		secureConnector.setKeyPassword(azkabanSettings.getString("jetty.keypassword"));
//		secureConnector.setTruststore(azkabanSettings.getString("jetty.truststore"));
//		secureConnector.setTrustPassword(azkabanSettings.getString("jetty.trustpassword"));
//		secureConnector.setHeaderBufferSize(MAX_HEADER_BUFFER_SIZE);
	}
}
