/*
 * Copyright 2012 LinkedIn, Inc
 * 
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */

package nowick.server;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.log4j.Logger;


import nowick.template.Page;
import nowick.user.Session;
import nowick.utils.HttpRequestUtils;
import nowick.utils.JSONUtils;

/**
 * Base Servlet for pages
 */
public class AbstractServlet extends HttpServlet {
	private static final long serialVersionUID = -1;
	private static final Logger logger = Logger.getLogger(AbstractServlet.class.getName());
	
	public static final String HTML_TYPE = "text/html";
	public static final String XML_MIME_TYPE = "application/xhtml+xml";
	public static final String JSON_MIME_TYPE = "application/json";
	
	private static final String SESSION_ID_NAME = "nowick.browser.session.id";
	private static final int DEFAULT_UPLOAD_DISK_SPOOL_SIZE = 20 * 1024 * 1024;
	
	private NowickServer application;
	private MultipartParser multipartParser;
	
	public NowickServer getApplication() {
		return application;
	}

	@Override
	public void init(ServletConfig config) throws ServletException {
		application = (NowickServer) config.getServletContext().getAttribute(NowickServer.NOWICK_SERVER_CONTEXT_KEY);

		if (application == null) {
			throw new IllegalStateException(
					"No batch application is defined in the servlet context!");
		}
	}

	/**
	 * Checks for the existance of the parameter in the request
	 * 
	 * @param request
	 * @param param
	 * @return
	 */
	public boolean hasParam(HttpServletRequest request, String param) {
		return HttpRequestUtils.hasParam(request, param);
	}

	/**
	 * Retrieves the param from the http servlet request. Will throw an
	 * exception if not found
	 * 
	 * @param request
	 * @param name
	 * @return
	 * @throws ServletException
	 */
	public String getParam(HttpServletRequest request, String name) throws ServletException {
		return HttpRequestUtils.getParam(request, name);
	}

	/**
	 * Retrieves the param from the http servlet request.
	 * 
	 * @param request
	 * @param name
	 * @param default
	 * 
	 * @return
	 */
	public String getParam(HttpServletRequest request, String name, String defaultVal){
		return HttpRequestUtils.getParam(request, name, defaultVal);
	}

	
	/**
	 * Returns the param and parses it into an int. Will throw an exception if
	 * not found, or a parse error if the type is incorrect.
	 * 
	 * @param request
	 * @param name
	 * @return
	 * @throws ServletException
	 */
	public int getIntParam(HttpServletRequest request, String name) throws ServletException {
		return HttpRequestUtils.getIntParam(request, name);
	}
	
	public int getIntParam(HttpServletRequest request, String name, int defaultVal) {
		return HttpRequestUtils.getIntParam(request, name, defaultVal);
	}

	public long getLongParam(HttpServletRequest request, String name) throws ServletException {
		return HttpRequestUtils.getLongParam(request, name);
	}
	
	public long getLongParam(HttpServletRequest request, String name, long defaultVal) {
		return HttpRequestUtils.getLongParam(request, name, defaultVal);
	}

	
	public Map<String, String> getParamGroup(HttpServletRequest request, String groupName)  throws ServletException {
		return HttpRequestUtils.getParamGroup(request, groupName);
	}
	
	/**
	 * Returns the session value of the request.
	 * 
	 * @param request
	 * @param key
	 * @param value
	 */
	protected void setSessionValue(HttpServletRequest request, String key, Object value) {
		request.getSession(true).setAttribute(key, value);
	}

	/**
	 * Adds a session value to the request
	 * 
	 * @param request
	 * @param key
	 * @param value
	 */
	@SuppressWarnings({ "unchecked", "rawtypes" })
	protected void addSessionValue(HttpServletRequest request, String key, Object value) {
		List l = (List) request.getSession(true).getAttribute(key);
		if (l == null)
			l = new ArrayList();
		l.add(value);
		request.getSession(true).setAttribute(key, l);
	}

	/**
	 * Retrieves a cookie by name. Potential issue in performance if a lot of
	 * cookie variables are used.
	 * 
	 * @param request
	 * @return
	 */
	protected Cookie getCookieByName(HttpServletRequest request, String name) {
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

	/**
	 * Creates a new velocity page to use. With session.
	 * 
	 * @param req
	 * @param resp
	 * @param template
	 * @return
	 */
	protected Page newPage(HttpServletRequest req, HttpServletResponse resp, Session session, String template) {
		Page page = new Page(req, resp, application.getVelocityEngine(), template);
		page.add("context", req.getContextPath());
		if (session != null) {
			page.add("user", session.getUser());
		}
		
		return page;
	}

	/**
	 * Creates a new velocity page to use.
	 * 
	 * @param req
	 * @param resp
	 * @param template
	 * @return
	 */
	protected Page newPage(HttpServletRequest req, HttpServletResponse resp, String template) {
		Page page = new Page(req, resp, application.getVelocityEngine(), template);
		page.add("context", req.getContextPath());
		
		return page;
	}
	
	/**
	 * Writes json out to the stream.
	 * 
	 * @param resp
	 * @param obj
	 * @throws IOException
	 */
	protected void writeJSON(HttpServletResponse resp, Object obj) throws IOException {
		writeJSON(resp, obj, false);
	}

	protected void writeJSON(HttpServletResponse resp, Object obj, boolean pretty) throws IOException {
		resp.setContentType(JSON_MIME_TYPE);
		JSONUtils.toJSON(obj, resp.getOutputStream(), true);
	}
	
	private Session getSessionFromRequest(HttpServletRequest req) throws ServletException {
		String remoteIp = req.getRemoteAddr();
		Cookie cookie = getCookieByName(req, SESSION_ID_NAME);
		String sessionId = null;

		if (cookie != null) {
			sessionId = cookie.getValue();
			logger.info("Session id " + sessionId);
		}
		
		if (sessionId == null && hasParam(req, "session.id")) {
			sessionId = getParam(req, "session.id");
		}
		return getSessionFromSessionId(sessionId, remoteIp);
	}
	
	private Session getSessionFromSessionId(String sessionId, String remoteIp) {
		if (sessionId == null) {
			return null;
		}
		
		Session session = getApplication().getSessionCache().getSession(sessionId);
		// Check if the IP's are equal. If not, we invalidate the sesson.
		if (session == null || !remoteIp.equals(session.getIp())) {
			return null;
		}
		
		return session;
	}
	
	@Override
	protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException {
		Session session = getSessionFromRequest(req);
		Page newPage = newPage(req, resp, session, "base.vm");
		newPage.render();
	}

}
