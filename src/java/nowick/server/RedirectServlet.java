package nowick.server;

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

public class RedirectServlet extends HttpServlet {
	private static final long serialVersionUID = 3171275594502148287L;
	
	private NowickServer application;
	
	public RedirectServlet(NowickServer server) {
		application = server;
	}
	
	@Override
	protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
		String serverName = req.getServerName();
		application.getPort();

		String requestURL = "http://" + serverName + ":" + application.getPort() + "/admin";
		resp.sendRedirect(resp.encodeRedirectURL(requestURL));
	}
}
