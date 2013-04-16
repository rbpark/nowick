package nowick.user;

import nowick.utils.Properties;

public class DefaultUserManager implements UserManager {
	public DefaultUserManager(Properties props) {
	}
	
	@Override
	public User getUser(String username, String password) throws UserManagerException {
		return new User(username);
	}

	@Override
	public boolean validateUser(String username) {
		return true;
	}
}
