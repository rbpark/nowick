package nowick.server;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;

import joptsimple.OptionParser;
import joptsimple.OptionSet;
import joptsimple.OptionSpec;

import nowick.utils.Properties;

import org.apache.log4j.Logger;
import org.apache.velocity.app.VelocityEngine;
import org.apache.velocity.runtime.RuntimeConstants;
import org.apache.velocity.runtime.log.Log4JLogChute;
import org.mortbay.jetty.Server;
import org.mortbay.jetty.bio.SocketConnector;
import org.mortbay.jetty.servlet.Context;
import org.mortbay.jetty.servlet.DefaultServlet;
import org.mortbay.jetty.servlet.ServletHolder;
import org.mortbay.thread.QueuedThreadPool;

public class NowickServer implements NowickParameters{
	private static final Logger logger = Logger.getLogger(NowickServer.class);
	private static final String CONF_FILE_NAME = "nowick.json";
	
	private Properties props;
	private final Server server;
	private final VelocityEngine velocityEngine;
	private final int port;
	
	private File dataDirectory;
	private File siteDirectory;
	
	public static void main(String[] args) throws Exception {
		OptionParser parser = new OptionParser();

		OptionSpec<String> configDirectory = parser
				.acceptsAll(Arrays.asList("c", "conf"), "The conf dir for Nowick.")
				.withRequiredArg()
				.describedAs("conf").ofType(String.class);

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
			@SuppressWarnings("unused")
			NowickServer server = new NowickServer(props);
		}
	}

	private static Properties getPropertiesFromPath(File path) throws IOException {
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
	
	private static Properties getProperties(File confPath) throws IOException {
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

		logger.info("Loading nowick configurations from file " + confFile.getPath());
		Properties props = new Properties();
		props.loadProperties(confFile);
		
		return props;
	}
	
	public NowickServer(Properties props) {
		server = new Server();
		this.props = props;

		Context root = new Context(server, "/", Context.SESSIONS);
		QueuedThreadPool httpThreadPool = new QueuedThreadPool(props.getInt(NUM_CONNECTIONS, DEFAULT_NUM_THREADS));
		server.setThreadPool(httpThreadPool);
		SocketConnector socketConnector = new SocketConnector();
		port = props.getInt(PORT, DEFAULT_PORT);
		socketConnector.setPort(port);
		server.addConnector(socketConnector);
		
		dataDirectory = new File(props.getString("base.dir", "data/"));
		
		root.setResourceBase("resources");
		root.addServlet(new ServletHolder(new PageServlet(this, dataDirectory)),"/*");

		this.velocityEngine = configureVelocityEngine(true, dataDirectory.getPath());
		
		logger.info("Starting server.");
		try {
			server.start();
		} 
		catch (Exception e) {
			logger.error(e);
			System.exit(1);
		}
		
		Runtime.getRuntime().addShutdownHook(new Thread() {
			public void run() {
				logger.info("Shutting down http server...");
				try {
					server.stop();
					server.destroy();
				} 
				catch (Exception e) {
					logger.error("Error while shutting down http server.", e);
				}
				logger.info("kk thx bye.");
			}
		});
		logger.info("Server running on port " + socketConnector.getPort() + ".");
	}
	
	public Properties getProperties() {
		return props;
	}
	
	/**
	 * Creates and configures the velocity engine.
	 * 
	 * @param devMode
	 * @return
	 */
	private VelocityEngine configureVelocityEngine(final boolean devMode, String path) {
		VelocityEngine engine = new VelocityEngine();
		
		engine.setProperty("resource.loader", "classpath, file");
		
		engine.setProperty("file.resource.loader.path", path);
		engine.setProperty("file.resource.loader.cache", !devMode);
		engine.setProperty("file.resource.loader.modificationCheckInterval", 5L);

		engine.setProperty("resource.manager.logwhenfound", false);
		engine.setProperty("input.encoding", "UTF-8");
		engine.setProperty("output.encoding", "UTF-8");
		engine.setProperty("directive.set.null.allowed", true);
		engine.setProperty("resource.manager.logwhenfound", false);
		engine.setProperty("velocimacro.permissions.allow.inline", true);
		engine.setProperty("velocimacro.library.autoreload", devMode);
		engine.setProperty("velocimacro.library", "templates/macros.vm");
		engine.setProperty(RuntimeConstants.VM_PERM_ALLOW_INLINE_REPLACE_GLOBAL, devMode);
		engine.setProperty("velocimacro.permissions.allow.inline.to.replace.global", true);
		engine.setProperty("velocimacro.arguments.strict", true);
		engine.setProperty("runtime.log.invalid.references", devMode);
		engine.setProperty("runtime.log.logsystem.class", Log4JLogChute.class);
		engine.setProperty("runtime.log.logsystem.log4j.logger", Logger.getLogger("org.apache.velocity.Logger"));
		engine.setProperty("parser.pool.size", 3);
		return engine;
	}
	
	public VelocityEngine getVelocityEngine() {
		return velocityEngine;
	}
	
	public int getPort() {
		return port;
	}
}
