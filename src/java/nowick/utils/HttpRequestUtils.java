package nowick.utils;

import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;

import javax.servlet.ServletException;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;

public class HttpRequestUtils {
	
	/**
	 * Checks for the existance of the parameter in the request
	 * 
	 * @param request
	 * @param param
	 * @return
	 */
	public static boolean hasParam(HttpServletRequest request, String param) {
		return request.getParameter(param) != null;
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
	public static String getParam(HttpServletRequest request, String name) throws ServletException {
		String p = request.getParameter(name);
		if (p == null) {
			throw new ServletException("Missing required parameter '" + name + "'.");
		}
		else {
			return p;
		}
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
	public static String getParam(HttpServletRequest request, String name, String defaultVal){
		String p = request.getParameter(name);
		if (p == null) {
			return defaultVal;
		}
		return p;
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
	public static int getIntParam(HttpServletRequest request, String name) throws ServletException {
		String p = getParam(request, name);
		return Integer.parseInt(p);
	}
	
	public static int getIntParam(HttpServletRequest request, String name, int defaultVal) {
		if (hasParam(request, name)) {
			try {
				return getIntParam(request, name);
			} catch (Exception e) {
				return defaultVal;
			}
		}
		
		return defaultVal;
	}

	public static boolean getBooleanParam(HttpServletRequest request, String name) throws ServletException  {
		String p = getParam(request, name);
		return Boolean.parseBoolean(p);
	}
	
	public static boolean getBooleanParam(HttpServletRequest request, String name, boolean defaultVal) {
		if (hasParam(request, name)) {
			try {
				return getBooleanParam(request, name);
			} catch (Exception e) {
				return defaultVal;
			}
		}
		
		return defaultVal;
	}
	
	public static long getLongParam(HttpServletRequest request, String name) throws ServletException {
		String p = getParam(request, name);
		return Long.valueOf(p);
	}
	
	public static long getLongParam(HttpServletRequest request, String name, long defaultVal) {
		if (hasParam(request, name)) {
			try {
				return getLongParam(request, name);
			} catch (Exception e) {
				return defaultVal;
			}
		}
		
		return defaultVal;
	}

	
	public static Map<String, String> getParamGroup(HttpServletRequest request, String groupName)  throws ServletException {
		@SuppressWarnings("unchecked")
		Enumeration<Object> enumerate = (Enumeration<Object>)request.getParameterNames();
		String matchString = groupName + "[";

		HashMap<String, String> groupParam = new HashMap<String, String>();
		while( enumerate.hasMoreElements() ) {
			String str = (String)enumerate.nextElement();
			if (str.startsWith(matchString)) {
				groupParam.put(str.substring(matchString.length(), str.length() - 1), request.getParameter(str));
			}
			
		}
		return groupParam;
	}
	
	public static final String SESSION_ID_NAME = "nowick.browser.session.id";
	
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

}
