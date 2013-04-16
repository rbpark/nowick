package nowick.template;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import nowick.utils.Utils;

import org.apache.velocity.VelocityContext;
import org.apache.velocity.app.VelocityEngine;

/**
 * A page to display
 */
public class Page {
	private static final String DEFAULT_MIME_TYPE = "text/html";
	@SuppressWarnings("unused")
	private final HttpServletRequest request;
	private final HttpServletResponse response;
	private final VelocityEngine engine;
	private final VelocityContext context;
	private final String template;
	private String mimeType = DEFAULT_MIME_TYPE;

	/**
	 * Creates a page and sets up the velocity engine to render
	 * 
	 * @param request
	 * @param response
	 * @param engine
	 * @param template
	 */
	public Page(HttpServletRequest request, HttpServletResponse response,
			VelocityEngine engine, String template) {
		this.request = Utils.nonNull(request);
		this.response = Utils.nonNull(response);
		this.engine = Utils.nonNull(engine);
		this.template = Utils.nonNull(template);
		this.context = new VelocityContext();
		this.context.put("session", request.getSession(true));
		this.context.put("context", request.getContextPath());
	}

	/**
	 * Renders the page in UTF-8
	 */
	public void render() {
		try {
			response.setContentType(mimeType);
			engine.mergeTemplate(template, "UTF-8", context, response.getWriter());
		} catch (Exception e) {
			throw new PageRenderException(e);
		}
	}

	/**
	 * Adds variables to the velocity context.
	 */
	public void add(String name, Object value) {
		context.put(name, value);
	}

	public void setMimeType(String type) {
		mimeType = type;
	}
}
