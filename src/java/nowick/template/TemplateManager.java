package nowick.template;

import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

import nowick.html.Html;

public class TemplateManager {
	private Map<String, Template> templates = new HashMap<String, Template>();
	
	public TemplateManager(Properties templateProps) {
		
	}
	
	public void createHTMLTemplate() {
		Html html = new Html();
		
	}
}
