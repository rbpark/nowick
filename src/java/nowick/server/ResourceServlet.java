package nowick.server;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.io.IOUtils;


public class ResourceServlet extends HttpServlet {
	private static final long serialVersionUID = 3441087893911184318L;
	private static HashMap<String, String> contextType = new HashMap<String,String>();
	static {
		contextType.put(".js", "application/javascript");
		contextType.put(".css", "text/css");
		contextType.put(".png", "image/png");
		contextType.put(".jpeg", "image/jpeg");
		contextType.put(".jpg", "image/jpeg");
	}
	
	private HashMap<String, List<File>> resourceDirs = new HashMap<String, List<File>>();
	
	public ResourceServlet() {
	}
	
	public void addResourceDir(String servletPath, File file) {
		List<File> files = resourceDirs.get(servletPath);
		if (files == null) {
			files = new ArrayList<File>();
			resourceDirs.put(servletPath, files);
		}
		files.add(file);
	}
	
	protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
		String path = req.getServletPath();
		List<File> files = resourceDirs.get(path);

		if (files == null) {
			throw new ServletException("Resource " + path + " doesn't exist.");
		}
		String prefix = req.getContextPath() + req.getServletPath();
		String filePath = req.getRequestURI().substring(prefix.length());

		File resourceFile = null;
		for (File dir: files) {
			File resFile = new File(dir, filePath);
			if (resFile.exists()) {
				resourceFile = resFile;
				break;
			}
		}
		
		if (resourceFile == null) {
			throw new ServletException("Resource " + path + " doesn't exist.");
		}
		else if (!resourceFile.isFile()) {
			throw new ServletException("Resource " + path + " is not a file.");
		}
		else {
			int lastIndex = filePath.lastIndexOf('.');
			if (lastIndex> 0) {
				String ext = filePath.substring(lastIndex).toLowerCase();
				String mime = contextType.get(ext);
				if (mime != null) {
					resp.setContentType(mime);
				}
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
		}
	}
}
