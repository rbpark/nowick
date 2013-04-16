package nowick.server;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;

import org.apache.commons.fileupload.FileItem;
import org.apache.commons.fileupload.FileUploadException;
import org.apache.commons.fileupload.disk.DiskFileItemFactory;
import org.apache.commons.fileupload.servlet.ServletFileUpload;

public class MultipartParser {

	private DiskFileItemFactory uploadItemFactory;

	public MultipartParser(int spillToDiskSize) {
		uploadItemFactory = new DiskFileItemFactory();
		uploadItemFactory.setSizeThreshold(spillToDiskSize);
	}

	@SuppressWarnings("unchecked")
	public Map<String, Object> parseMultipart(HttpServletRequest request)
			throws IOException, ServletException {
		ServletFileUpload upload = new ServletFileUpload(uploadItemFactory);
		List<FileItem> items = null;
		try {
			items = upload.parseRequest(request);
		} catch (FileUploadException e) {
			throw new ServletException(e);
		}

		Map<String, Object> params = new HashMap<String, Object>();
		for (FileItem item : items) {
			if (item.isFormField())
				params.put(item.getFieldName(), item.getString());
			else
				params.put(item.getFieldName(), item);
		}
		return params;
	}

}
