package nowick.server;

import java.io.IOException;
import java.util.Map;

import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import nowick.template.Page;
import nowick.user.Session;
import nowick.utils.HttpRequestUtils;
import nowick.utils.JSONUtils;

public class AbstractServlet extends HttpServlet {
	private static final long serialVersionUID = 3171275594502148287L;
	
	public static final String HTML_TYPE = "text/html";
	public static final String XML_MIME_TYPE = "application/xhtml+xml";
	public static final String JSON_MIME_TYPE = "application/json";
	
	private NowickServer application;
	
	public AbstractServlet(NowickServer server) {
		application = server;
	}
	
	protected NowickServer getApplication() {
		return application;
	}
	
	@Override
	public void init(ServletConfig config) throws ServletException {
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
	 * Creates a new velocity page to use. With session.
	 * 
	 * @param req
	 * @param resp
	 * @param template
	 * @return
	 */
	protected Page newPage(HttpServletRequest req, HttpServletResponse resp, Session session, String template) {
		Page page = new Page(req, resp, getApplication().getVelocityEngine(), template);
		page.add("context", req.getContextPath());
		if (session != null) {
			page.add("userid", session.getUser().getUserId());
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
		Page page = new Page(req, resp, getApplication().getVelocityEngine(), template);
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
		resp.getOutputStream().flush();
	}
}
