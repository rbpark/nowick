package nowick.server;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Constructor;
import java.util.Arrays;

import joptsimple.OptionParser;
import joptsimple.OptionSet;
import joptsimple.OptionSpec;

import nowick.user.DefaultUserManager;
import nowick.user.SessionCache;
import nowick.user.UserManager;
import nowick.utils.Properties;

import org.apache.log4j.Logger;
import org.apache.velocity.app.VelocityEngine;
import org.apache.velocity.runtime.log.Log4JLogChute;
import org.apache.velocity.runtime.resource.loader.ClasspathResourceLoader;
import org.apache.velocity.runtime.resource.loader.JarResourceLoader;
import org.mortbay.jetty.Server;
import org.mortbay.jetty.bio.SocketConnector;
import org.mortbay.jetty.security.SslSocketConnector;
import org.mortbay.jetty.servlet.Context;
import org.mortbay.jetty.servlet.ServletHolder;
import org.mortbay.thread.QueuedThreadPool;

public class NowickServer implements NowickParameters{
	private static final Logger logger = Logger.getLogger(NowickServer.class);
	private static final String CONF_FILE_NAME = "nowick.json";
	public static final String NOWICK_SERVER_CONTEXT_KEY = "nowick.sever.context";
	
	private Properties props;
	private final Server server;
	private Server secureServer;
	
	private UserManager userManager;
	private SessionCache sessionCache;
	private final VelocityEngine velocityEngine;
	
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
		this.sessionCache = new SessionCache(props);
		this.velocityEngine = configureVelocityEngine(true, props.getString("template.path", "data/templates"));
		Properties jettyProperties = props.getSubProperty("jetty");
		
		Context root = new Context(server, "/", Context.SESSIONS);
		QueuedThreadPool httpThreadPool = new QueuedThreadPool(jettyProperties.getInt(NUM_CONNECTIONS, DEFAULT_NUM_THREADS));
		server.setThreadPool(httpThreadPool);
		SocketConnector socketConnector = new SocketConnector();
		socketConnector.setPort(jettyProperties.getInt(PORT, DEFAULT_PORT));
		server.addConnector(socketConnector);
		
		root.setResourceBase(props.getString(RESOURCE_DIR, "data/web"));
		root.setAttribute(NOWICK_SERVER_CONTEXT_KEY, this);
		
		ResourceServlet resourceServlet = new ResourceServlet();
		resourceServlet.addResourceDir("/js", new File(props.getString(RESOURCE_DIR, "data/web/js")));
		resourceServlet.addResourceDir("/css", new File(props.getString(RESOURCE_DIR, "data/web/css")));
		root.addServlet(new ServletHolder(resourceServlet),"/js/*");
		root.addServlet(new ServletHolder(resourceServlet),"/css/*");
		
		root.addServlet(new ServletHolder(new AbstractSessionServlet(this)),"/*");
		
		// Setup auth to be on ssl port if desired.
		if (jettyProperties.getBoolean(USE_SSL_AUTHENTICATION)) {
			secureServer = new Server();
			Context secureContext = new Context(secureServer, "/", true, true);
			secureContext.setAttribute(NOWICK_SERVER_CONTEXT_KEY, this);
			
			SslSocketConnector secureConnector = new SslSocketConnector();
			secureContext.setResourceBase(props.getString(RESOURCE_DIR, "data/web"));
			Properties jettySSLProperties = jettyProperties.getSubProperty(JETTY_SSL);
			secureConnector.setPort(jettySSLProperties.getInt(PORT, DEFAULT_AUTHENTICATION_PORT));
			secureConnector.setKeystore(jettySSLProperties.getString(JETTY_SSL_KEYSTORE));
			secureConnector.setPassword(jettySSLProperties.getString(JETTY_SSL_PASSWORD));
			secureConnector.setKeyPassword(jettySSLProperties.getString(JETTY_SSL_KEYPASSWORD));
			secureConnector.setTruststore(jettySSLProperties.getString(JETTY_SSL_TRUSTSTORE));
			secureConnector.setTrustPassword(jettySSLProperties.getString(JETTY_SSL_TRUSTPASSWORD));
			secureConnector.setHeaderBufferSize(jettySSLProperties.getInt(MAX_HEADER_CONTEXT_SIZE, DEFAULT_MAX_HEADER_BUFFER_SIZE));

			secureServer.addConnector(secureConnector);
			secureContext.addServlet(new ServletHolder(new AuthServlet(this)),"/auth");
			
			logger.info("Starting auth server on SSL server on port " + secureConnector.getPort());
			try {
				secureServer.start();
			} 
			catch (Exception e) {
				logger.error(e);
				System.exit(1);
			}
		}
		else {
			logger.info("Not using ssl for authentication. Using unsecure port " + socketConnector.getPort());
			root.addServlet(new ServletHolder(new AuthServlet(this)),"/auth");
		}
		
		logger.info("Starting server.");
		try {
			server.start();
		} 
		catch (Exception e) {
			logger.error(e);
			System.exit(1);
		}
		
		userManager = loadUserManager(props.getSubProperty(USER_MANAGER));
		
		Runtime.getRuntime().addShutdownHook(new Thread() {
			public void run() {
				logger.info("Shutting down http server...");
				try {
					server.stop();
					server.destroy();
					
					if (secureServer != null) {
						secureServer.stop();
						secureServer.destroy();
					}
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
	
	private UserManager loadUserManager(Properties userManagerProps) {
		Class<?> userManagerClass = null;
		try {
			userManagerClass = Class.forName(userManagerProps.getString(USER_MANAGER_CLASS));
		} catch (ClassNotFoundException e1) {
			throw new RuntimeException("Cannot load user manager class " + props.getString(USER_MANAGER_CLASS));
		}
		logger.info("Loading user manager class " + userManagerClass.getName());
		UserManager manager = null;

		if (userManagerClass != null && userManagerClass.getConstructors().length > 0) {
			try {
				Constructor<?> userManagerConstructor = userManagerClass.getConstructor(Properties.class);
				manager = (UserManager) userManagerConstructor.newInstance(userManagerProps);
			} 
			catch (Exception e) {
				logger.error("Could not instantiate UserManager "+ userManagerClass.getName());
				throw new RuntimeException(e);
			}

		} 
		else {
			manager = new DefaultUserManager(userManagerProps);
		}

		return manager;
	}
	
	public UserManager getUserManager() {
		return userManager;
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
		
		engine.setProperty("classpath.resource.loader.class", ClasspathResourceLoader.class.getName());
		engine.setProperty("classpath.resource.loader.cache", !devMode);
		engine.setProperty("classpath.resource.loader.modificationCheckInterval", 5L);
		engine.setProperty("jar.resource.loader.class", JarResourceLoader.class.getName());
		engine.setProperty("jar.resource.loader.cache", !devMode);
		engine.setProperty("resource.manager.logwhenfound", false);
		engine.setProperty("input.encoding", "UTF-8");
		engine.setProperty("output.encoding", "UTF-8");
		engine.setProperty("directive.set.null.allowed", true);
		engine.setProperty("resource.manager.logwhenfound", false);
		engine.setProperty("velocimacro.permissions.allow.inline", true);
		engine.setProperty("velocimacro.library.autoreload", devMode);
		engine.setProperty("velocimacro.library", "macros.vm");
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
	
	public SessionCache getSessionCache() {
		return sessionCache;
	}
}
