package nowick.server;

import java.io.IOException;
import java.util.HashMap;
import java.util.UUID;

import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import nowick.template.Page;
import nowick.user.Session;
import nowick.user.SessionCache;
import nowick.user.User;
import nowick.user.UserManager;
import nowick.user.UserManagerException;

public class AuthServlet extends AbstractServlet {
	private static final long serialVersionUID = -1085287133718477821L;
	private NowickServer application;
	
	@Override
	public void init(ServletConfig config) throws ServletException {
	}
	
	public AuthServlet(NowickServer server) {
		super(server);
	}
	
	@Override
	protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
		if (!hasParam(req, "action")) {
			Page page = newPage(req, resp, "login.vm");
			page.render();
		}
		else {
			HashMap<String,Object> map = new HashMap<String,Object>();
			String username = getParam(req, "username");
			String password = getParam(req, "password");
			String url = getParam(req, "url", null);
			
			UserManager manager = application.getUserManager();
			try {
				User user = manager.getUser(username, password);
				if (user == null) {
					map.put("error", "User and password not found.");
				}
				else {
					SessionCache cache = application.getSessionCache();
					Session oldSession = ServletUtils.getSessionFromRequest(cache, req);
					Session session = createSession(req);
					
					if (session == null) {
						map.put("error", "Could not create user session.");
					}
					else {
						cache.removeSession(oldSession.getSessionId());
						cache.addSession(session);
						ServletUtils.addSessionCookie(resp, session);

						map.put("session.id", session.getSessionId());
						if (url != null) {
							map.put("url", url);
						}
					}
				}
			} catch (UserManagerException e) {
				map.put("error", e.getMessage());
			}
			
			this.writeJSON(resp, map, true);
		}
	}
	
	private Session createSession(HttpServletRequest req) throws UserManagerException, ServletException {
		String username = getParam(req, "username");
		String password = getParam(req, "password");
		String ip = req.getRemoteAddr();
		
		return createSession(username, password, ip);
	}
	
	private Session createSession(String username, String password, String ip) throws UserManagerException, ServletException {
		UserManager manager = getApplication().getUserManager();
		User user = manager.getUser(username, password);

		String randomUID = UUID.randomUUID().toString();
		Session session = new Session(randomUID, user, ip);
		
		return session;
	}
	

}
