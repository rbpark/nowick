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

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import nowick.template.Page;
import nowick.user.Session;
import nowick.utils.HttpRequestUtils;

/**
 * Base Servlet for pages
 */
public class AbstractSessionServlet extends AbstractServlet {
	private static final long serialVersionUID = -1;

	public AbstractSessionServlet(NowickServer server) {
		super(server);
	}
	
	@Override
	protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException {
		Session session = HttpRequestUtils.getSessionFromRequest(getApplication().getSessionCache(), req);
		Page newPage = newPage(req, resp, session, "base.vm");
		newPage.render();
	}

}
