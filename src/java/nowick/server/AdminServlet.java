package nowick.server;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import nowick.template.Page;
import nowick.user.Session;

public class AdminServlet extends AbstractSessionServlet {
	private static final long serialVersionUID = -2116195579210726018L;

	public AdminServlet(NowickServer server) {
		super(server);
	}
	
	public void handleGet(HttpServletRequest req, HttpServletResponse resp, Session session) {
		Page page = newPage(req, resp, "nowick/projectpage.vm");
		page.render();
	}
}
