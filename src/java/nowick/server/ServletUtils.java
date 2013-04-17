package nowick.server;

import javax.servlet.ServletException;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import nowick.user.Session;
import nowick.user.SessionCache;
import nowick.utils.HttpRequestUtils;

public class ServletUtils {
	public static final String SESSION_ID_NAME = "nowick.browser.session.id";
	
	public static Session getSessionFromSessionId(SessionCache cache, String sessionId, String remoteIp) {
		if (sessionId == null) {
			return null;
		}
		
		Session session = cache.getSession(sessionId);
		// Check if the IP's are equal. If not, we invalidate the sesson.
		if (session == null || !remoteIp.equals(session.getIp())) {
			return null;
		}
		
		return session;
	}
	
	public static Session getSessionFromRequest(SessionCache cache, HttpServletRequest req) throws ServletException {
		String remoteIp = req.getRemoteAddr();
		Cookie cookie = getCookieByName(req, SESSION_ID_NAME);
		String sessionId = null;

		if (cookie != null) {
			sessionId = cookie.getValue();
		}
		
		if (sessionId == null && HttpRequestUtils.hasParam(req, "session.id")) {
			sessionId = HttpRequestUtils.getParam(req, "session.id");
		}
		return getSessionFromSessionId(cache, sessionId, remoteIp);
	}
	
	/**
	 * Retrieves a cookie by name. Potential issue in performance if a lot of
	 * cookie variables are used.
	 * 
	 * @param request
	 * @return
	 */
	public static Cookie getCookieByName(HttpServletRequest request, String name) {
		Cookie[] cookies = request.getCookies();
		if (cookies != null) {
			for (Cookie cookie : cookies) {
				//if (name.equals(cookie.getName()) && cookie.getPath()!=null && cookie.getPath().equals("/")) {
				if (name.equals(cookie.getName())) {
					return cookie;
				}
			}
		}
		
		return null;
	}

	public static void addSessionCookie(HttpServletResponse response, Session session) {
		Cookie cookie = new Cookie(SESSION_ID_NAME, session.getSessionId());
		cookie.setPath("/");
		response.addCookie(cookie);
	}
}
