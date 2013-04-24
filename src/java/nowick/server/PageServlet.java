package nowick.server;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileFilter;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.HashMap;
import java.util.Map;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import nowick.template.VelocityPage;
import nowick.utils.JSONUtils;
import nowick.utils.Utils;

import org.apache.commons.io.IOUtils;
import org.apache.log4j.Logger;

public class PageServlet extends AbstractServlet {
	private static final long serialVersionUID = -2492048520766971114L;
	private static final Logger logger = Logger.getLogger(PageServlet.class);
	private final File baseDirectory;
	private final File pageDirectory;
	private final File templateDirectory;
	private final File webDirectory;
	
	private ExtensionFileFilter vmDataFileFilter = new ExtensionFileFilter(".vm");
	private static HashMap<String, String> contextType = new HashMap<String,String>();
	
	static {
		contextType.put(".js", "application/javascript");
		contextType.put(".css", "text/css");
		contextType.put(".png", "image/png");
		contextType.put(".jpeg", "image/jpeg");
		contextType.put(".jpg", "image/jpeg");
		contextType.put(".gif", "image/gif");
	}
	
	public PageServlet(NowickServer server, File baseDirectory) {
		super(server);
		this.baseDirectory = baseDirectory;
		this.pageDirectory = new File(this.baseDirectory, "pages");
		this.templateDirectory = new File(this.baseDirectory, "templates");
		this.webDirectory = new File(this.baseDirectory, "web");
	}

	protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
		logger.info("Request Path " + req.getServletPath());
		String pagePath = req.getRequestURI().substring(req.getServletPath().length() + 1);
		logger.info("Page path " + pagePath);
		
		if (handleResource(pagePath, resp)) {
			return;
		}

		File pageDir = new File(pageDirectory, pagePath);
		HashMap<String, Object> templateData = new HashMap<String, Object>();
		setVelocityTemplateMap(templateData, pageDir);
		
		HashMap<String, Object> pageData = new HashMap<String, Object>();
		setPageData(pageData, pageDir);
		
		String template = "default.vm";
		if (pageData.containsKey("template")) {
			template = (String)pageData.get("template");
			if (!template.endsWith(".vm")) {
				template += ".vm";
			}
		}
		
		String templatePath = templateDirectory.getName() + "/" + template;
		VelocityPage page = newPage(req, resp, templatePath);
		page.add("templates", templateData);
		page.add("pageData", pageData);
		page.add("editing", true);
		page.render();
	}
	
	private boolean handleResource(String pagePath, HttpServletResponse resp) throws IOException {
		int lastIndex = pagePath.lastIndexOf('.');
		if (lastIndex> 0) {
			String ext = pagePath.substring(lastIndex).toLowerCase();

			File resourceFile = new File(webDirectory, pagePath);
			if (resourceFile.exists()) {
				String mime = contextType.get(ext);
				if (mime != null) {
					resp.setContentType(mime);
				}
				else {
					resp.setContentType("text/plain");
				}

				OutputStream output = resp.getOutputStream();
				BufferedInputStream input = null;
				try {
					input = new BufferedInputStream(new FileInputStream(resourceFile));
					IOUtils.copy(input, output);
				}
				finally {
					if (input != null) {
						input.close();
					}
				}
				output.flush();
				
				return true;
			}
		}
		
		return false;
	}
	
	private void setVelocityTemplateMap(Map<String, Object> result, File directory) {
		if (directory == null || !directory.isDirectory()) {
			return;
		}
		File[] files = directory.listFiles(vmDataFileFilter);
		
		// Recurse for data first.
		if (!Utils.canonicalFileEquals(directory, pageDirectory) && !Utils.canonicalFileEquals(directory, new File(System.getProperty("user.dir")))) {
			setVelocityTemplateMap(result, directory.getParentFile());
		}

		for (File file: files) {
			// Refer to it as filename
			String filename = file.getName();
			filename = filename.substring(0, filename.length() - ".vm".length());
			String pathName = file.getPath().substring(baseDirectory.getName().length());
			result.put(filename, pathName);
		}
	}
	
	private void setPageData(Map<String, Object> result, File directory) throws IOException {
		if (directory == null || !directory.isDirectory()) {
			return;
		}

		// Recurse for data first.
		if (!Utils.canonicalFileEquals(directory, pageDirectory) && !Utils.canonicalFileEquals(directory, new File(System.getProperty("user.dir")))) {
			setPageData(result, directory.getParentFile());
		}

		File file = new File(directory, "page.json");
		if (file.exists()) {
			Object jsonMap = JSONUtils.parseJSONFromFile(file);
			if (jsonMap instanceof Map<?,?>) {
				@SuppressWarnings("unchecked")
				Map<String, Object> data = (Map<String,Object>)JSONUtils.parseJSONFromFile(file);
				result.putAll(data);
			}
		}
	}
	
	private class ExtensionFileFilter implements FileFilter {
		private String[] extensions;
		
		public ExtensionFileFilter(String ... extensions) {
			this.extensions = extensions;
		}

		@Override
		public boolean accept(File pathname) {
			if (!pathname.isFile()) {
				return false;
			}
			
			String path = pathname.getName();
			for (String extension: extensions) {
				if (path.endsWith(extension)) {
					return true;
				}
			}
			return false;
		}
	}
}
