package nowick.html;

import java.io.IOException;
import java.io.Writer;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public abstract class DomElement {
	
	private static final char[] TABS = new char[] {'\t','\t','\t','\t','\t','\t'};
	private List<DomElement> domElements = new ArrayList<DomElement>();
	private Map<String,String> attributes = new HashMap<String,String>();
	private String content;
	
	public abstract String getTag();
	
	protected void add(DomElement elem) {
		domElements.add(elem);
	}
	
	public String getAttributes(String key) {
		return attributes.get(key);
	}
	
	public void setAttribute(String key, String value) {
		attributes.put(key, value);
	}
	
	public String getContent() {
		return content;
	}
	
	public void writeHtml(int indentNum, Writer writer) throws IOException {
		String tag = getTag();
		writeIndents(indentNum, writer);
		writer.write('<');
		writer.write(tag);
		if (!attributes.isEmpty()) {
			writer.write(' ');
			writeAttributes(writer);	
		}
		writer.write('>');
		writer.write('\n');
		
		for (DomElement element: domElements) {
			element.writeHtml(indentNum + 1, writer);
		}
		
		writer.write("</");
		writer.write(tag);
		writer.write('>');
		writer.write('\n');
	}
	
	private void writeAttributes(Writer writer) throws IOException {
		for (Map.Entry<String, String> entry: attributes.entrySet()) {
			writer.append('"');
			writer.append(entry.getKey());
			writer.append("\"=\"");
			writer.append(entry.getValue());
			writer.append("\" ");
		}
	}
	
	private void writeIndents(int indentNum, Writer writer) throws IOException {
		if (indentNum == 0) {
			return;
		}
		
		while(indentNum > TABS.length) {
			writer.write(TABS, 0, TABS.length);
			indentNum -= TABS.length;
		}
		
		writer.write(TABS, 0, indentNum);
	}
}
