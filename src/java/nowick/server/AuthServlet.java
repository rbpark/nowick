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
import nowick.utils.HttpRequestUtils;

public class AuthServlet extends AbstractServlet {
	private static final long serialVersionUID = -1085287133718477821L;
	
	@Override
	public void init(ServletConfig config) throws ServletException {
	}
	
	public AuthServlet(NowickServer server) {
		super(server);
	}
	
	@Override
	protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
		String action = getParam(req, "action", null);
		HashMap<String,Object> map = new HashMap<String,Object>();
		if (action == null) {
			Page page = newPage(req, resp,"nowick/login.vm");

			String path = req.getRequestURI().substring(req.getServletPath().length());
			if (path.isEmpty()) {
				path = "/admin";
			}
			String redirectURL = "http://" + req.getServerName() + ":" + getApplication().getPort() + path;
			page.add("redirectURL", redirectURL);

			page.render();
		}
		else if ("login".equals(action))
		{
			String username = getParam(req, "username");
			String password = getParam(req, "password");
			
			UserManager manager = getApplication().getUserManager();
			try {
				User user = manager.getUser(username, password);
				if (user == null) {
					map.put("error", "User and password not found.");
				}
				else {
					SessionCache cache = getApplication().getSessionCache();
					Session oldSession = HttpRequestUtils.getSessionFromRequest(cache, req);
					Session session = createSession(req);
					
					if (session == null) {
						map.put("error", "Could not create user session.");
					}
					else {
						if (oldSession != null) {
							cache.removeSession(oldSession.getSessionId());
						}
						cache.addSession(session);
						HttpRequestUtils.addSessionCookie(resp, session);

						map.put("session.id", session.getSessionId());
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
