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

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import nowick.user.Session;
import nowick.utils.HttpRequestUtils;

/**
 * Base Servlet for pages
 */
public abstract class AbstractSessionServlet extends AbstractServlet {
	private static final long serialVersionUID = -1;
	
	public AbstractSessionServlet(NowickServer server) {
		super(server);
	}
	
	@Override
	protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
		Session session = HttpRequestUtils.getSessionFromRequest(getApplication().getSessionCache(), req);
		if (session == null) {
			redirectToLogin(req, resp);
		}
		else {
			handleGet(req, resp, session);
		}
	}

	private void redirectToLogin(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
		String requestURI = req.getRequestURI();
		
		String authURL = "https://" + req.getServerName() + ":" + getApplication().getAuthPort() + "/login" + requestURI;
		resp.sendRedirect(resp.encodeRedirectURL(authURL));
	}
	
	protected abstract void handleGet(HttpServletRequest req, HttpServletResponse resp, Session session);
}
