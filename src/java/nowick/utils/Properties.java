package nowick.utils;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.w3c.dom.Document;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

/**
 * Why do I do this? Because WC3 dom sucks, and I'd rather be working with maps.
 *
 */
public class Properties {
	private HashMap<String, Object> properties = new HashMap<String, Object>();
	private HashMap<String, String> attributes;
	private String value;
	private String namespace;
	
	public Properties() {
		
	}

	public void loadXMLFile(File xmlFile) {
		if (!xmlFile.exists()) {
			throw new IllegalArgumentException("User xml file " + xmlFile.getPath() + " doesn't exist.");
		}

		DocumentBuilderFactory docBuilderFactory = DocumentBuilderFactory
				.newInstance();
		DocumentBuilder builder = null;
		try {
			builder = docBuilderFactory.newDocumentBuilder();
		} catch (ParserConfigurationException e) {
			throw new IllegalArgumentException("Exception while parsing user xml. Document builder not created.",e);
		}
		
		Document doc = null;
		try {
			doc = builder.parse(xmlFile);
		} catch (SAXException e) {
			throw new IllegalArgumentException("Exception while parsing "
					+ xmlFile.getPath() + ". Invalid XML.", e);
		} catch (IOException e) {
			throw new IllegalArgumentException("Exception while parsing "
					+ xmlFile.getPath() + ". Error reading file.", e);
		}
		
		NodeList tagList = doc.getChildNodes();
		Node node = tagList.item(0);
		
	}
	
	private void recurseAndBuildProperties(Node node) {
		NamedNodeMap attrMap = node.getAttributes();

		for (int i = 0; i < attrMap.getLength(); ++i) {
			Node attrNode = attrMap.item(i);
			attributes.put(attrNode.getNodeName(), attrNode.getNodeValue());
		}
		
		value = node.getTextContent();
		NodeList list = node.getChildNodes();
		for (int i = 0; i < list.getLength(); ++i) {
			Node childNode = list.item(i);
			if (childNode.getNodeType() == Node.ELEMENT_NODE) {
				
			}
		}
	}
}
