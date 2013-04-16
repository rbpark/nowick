package nowick.user;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import nowick.utils.Properties;

import org.apache.log4j.Logger;
import org.w3c.dom.Document;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

public class XmlUserManager implements UserManager {
	private static final Logger logger = Logger.getLogger(XmlUserManager.class.getName());

	public static final String XML_FILE_PARAM = "xml.file";
	public static final String AZKABAN_USERS_TAG = "users";
	public static final String USER_TAG = "user";
	public static final String USERNAME_ATTR = "username";
	public static final String PASSWORD_ATTR = "password";

	private File xmlPath;

	private HashMap<String, User> users;
	private HashMap<String, String> userPassword;


	/**
	 * The constructor.
	 * 
	 * @param props
	 * @throws UserManagerException 
	 */
	public XmlUserManager(Properties props) throws UserManagerException {
		xmlPath = getXmlFile(props);
		parseXMLFile(xmlPath);

	}

	private File getXmlFile(Properties props) throws UserManagerException {
		String file = props.getString(XML_FILE_PARAM);
		File xmlFile = new File(file);
		
		if (xmlFile.exists()) {
			return xmlFile;
		}
		else {
			xmlFile = new File(props.getSource(), file);
			return xmlFile;
		}
	}
	
	private void parseXMLFile(File xmlPath) {
		if (!xmlPath.exists()) {
			throw new IllegalArgumentException("User xml file " + xmlPath + " doesn't exist.");
		}

		HashMap<String, User> users = new HashMap<String, User>();
		HashMap<String, String> userPassword = new HashMap<String, String>();
		
		// Creating the document builder to parse xml.
		DocumentBuilderFactory docBuilderFactory = DocumentBuilderFactory
				.newInstance();
		DocumentBuilder builder = null;
		try {
			builder = docBuilderFactory.newDocumentBuilder();
		} catch (ParserConfigurationException e) {
			throw new IllegalArgumentException("Exception while parsing user xml. Document builder not created.",e);
		}

		Document doc = null;
		try {
			doc = builder.parse(xmlPath);
		} catch (SAXException e) {
			throw new IllegalArgumentException("Exception while parsing "
					+ xmlPath + ". Invalid XML.", e);
		} catch (IOException e) {
			throw new IllegalArgumentException("Exception while parsing "
					+ xmlPath + ". Error reading file.", e);
		}

		// Only look at first item, because we should only be seeing
		// azkaban-users tag.
		NodeList tagList = doc.getChildNodes();
		Node azkabanUsers = tagList.item(0);

		NodeList azkabanUsersList = azkabanUsers.getChildNodes();
		for (int i = 0; i < azkabanUsersList.getLength(); ++i) {
			Node node = azkabanUsersList.item(i);
			if (node.getNodeType() == Node.ELEMENT_NODE) {
				if (node.getNodeName().equals(USER_TAG)) {
					parseUserTag(node, users, userPassword);
				}
			}
		}

		// Synchronize the swap. Similarly, the gets are synchronized to this.
		synchronized (this) {
			this.users = users;
			this.userPassword = userPassword;
		}
	}

	private void parseUserTag(Node node, HashMap<String, User> users, HashMap<String, String> userPassword) {
		NamedNodeMap userAttrMap = node.getAttributes();
		Node userNameAttr = userAttrMap.getNamedItem(USERNAME_ATTR);
		if (userNameAttr == null) {
			throw new RuntimeException("Error loading user. The '" + USERNAME_ATTR + "' attribute doesn't exist");
		}
		Node passwordAttr = userAttrMap.getNamedItem(PASSWORD_ATTR);
		if (passwordAttr == null) {
			throw new RuntimeException("Error loading user. The '" + PASSWORD_ATTR + "' attribute doesn't exist");
		}

		// Add user to the user/password map
		String username = userNameAttr.getNodeValue();
		String password = passwordAttr.getNodeValue();
		userPassword.put(username, password);
		// Add the user to the node
		User user = new User(userNameAttr.getNodeValue());
		users.put(username, user);
		logger.info("Loading user " + user.getUserId());
	}

	@Override
	public User getUser(String username, String password) throws UserManagerException {
		if (username == null || username.trim().isEmpty()) {
			throw new UserManagerException("Username is empty.");
		} else if (password == null || password.trim().isEmpty()) {
			throw new UserManagerException("Password is empty.");
		}

		// Minimize the synchronization of the get. Shouldn't matter if it
		// doesn't exist.
		String foundPassword = null;
		User user = null;
		synchronized (this) {
			foundPassword = userPassword.get(username);
			if (foundPassword != null) {
				user = users.get(username);
			}
		}

		if (foundPassword == null || !foundPassword.equals(password)) {
			throw new UserManagerException("Username/Password not found.");
		}
		// Once it gets to this point, no exception has been thrown. User
		// shoudn't be
		// null, but adding this check for if user and user/password hash tables
		// go
		// out of sync.
		if (user == null) {
			throw new UserManagerException("Internal error: User not found.");
		}

		// Add all the roles the group has to the user
		return user;
	}
	
	@Override
	public boolean validateUser(String username) {
		return users.containsKey(username);
	}
}
