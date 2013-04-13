package nowick.utils;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

public class Properties {
	private Map<String, Object> parameters;
	private String source = null;
	private String[] path = null;
	
	public Properties() {
		parameters = new HashMap<String, Object>();
	}
	
	public Properties(Map<String,Object> map) {
		parameters = map;
	}
	
	private Properties(Map<String,Object> map, String source, String[] path) {
		parameters = map;
		this.source = source;
		this.path = path;
	}
	
	@SuppressWarnings("unchecked")
	public void loadProperties(File jsonFile) throws IOException {
		source = jsonFile.getPath();
		Object result = JSONUtils.parseJSONFromFile(jsonFile);
		this.parameters = (Map<String,Object>)result;
	}
	
	public Map<String,Object> getMap() {
		return parameters;
	}

	@SuppressWarnings("unchecked")
	public Map<String, Object> getMap(String path) {
		Object obj = getParameter(path);
		if (obj == null) {
			throw new UndefinedPropertyException("Property at " + path + " doesn't exist.");
		}
		else if (obj instanceof Map) {
			return (Map<String,Object>)obj;
		}
		
		throw new UndefinedPropertyException("Property at " + path + " isn't a map.");
	}
	
	public Properties getSubProperty(String path) {
		Map<String, Object> map = getMap(path);
		String[] newPath = Arrays.copyOfRange(this.path, 1, path.length());
		return new Properties(map, source, newPath);
	}
	
	public int getInt(String path) {
		Object obj = getParameter(path);
		if (obj == null) {
			throw new UndefinedPropertyException("Missing required property '" + path + "'");
		}
		else if (obj instanceof Integer) {
			return (Integer)obj;
		}
		else if (obj instanceof String) {
			return Integer.parseInt((String)obj);
		}
		
		throw new UndefinedPropertyException("Required property '" + path + "' can not be converted to an integer");
	}
	
	public int getInt(String path, int defaultVal) {
		Object obj = getParameter(path);
		
		if (obj == null) {
			return defaultVal;
		}
		else if (obj instanceof Integer) {
			return (Integer)obj;
		}
		else if (obj instanceof String) {
			return Integer.parseInt((String)obj);
		}
		
		return defaultVal;
	}
	
	public boolean getBoolean(String path) {
		Object obj = getParameter(path);
		if (obj == null) {
			throw new UndefinedPropertyException("Missing required property '" + path + "'");
		}
		else if (obj instanceof Boolean) {
			return (Boolean)obj;
		}
		else if (obj instanceof String) {
			return Boolean.parseBoolean((String)obj);
		}
		
		throw new UndefinedPropertyException("Required property '" + path + "' can not be converted to an integer");
	}
	
	public boolean getBoolean(String path, boolean defaultVal) {
		Object obj = getParameter(path);
		
		if (obj == null) {
			return defaultVal;
		}
		else if (obj instanceof Integer) {
			return (Boolean)obj;
		}
		else if (obj instanceof String) {
			return Boolean.parseBoolean((String)obj);
		}
		
		return defaultVal;
	}
	
	public String getString(String path) {
		Object obj = getParameter(path);
		if (obj == null) {
			throw new UndefinedPropertyException("Missing required property '" + path + "'");
		}
		else if (obj instanceof String) {
			return (String)obj;
		}
		else if (obj instanceof Number) {
			return String.valueOf(obj);
		}
		else if (obj instanceof Boolean) {
			return String.valueOf(obj);
		}
		
		throw new UndefinedPropertyException("Required property '" + path + "' can not be converted to an integer");
	}
	
	public String getString(String path, String defaultVal) {
		Object obj = getParameter(path);
		if (obj == null) {
			return defaultVal;
		}
		else if (obj instanceof String) {
			return (String)obj;
		}
		else if (obj instanceof Number) {
			return String.valueOf(obj);
		}
		else if (obj instanceof Boolean) {
			return String.valueOf(obj);
		}
		
		return defaultVal;
	}
	
	public boolean hasParameter(String path) {
		return getParameter(path) != null;
	}
	
	public Object getParameter(String path) {
		String[] splitPath = splitPath(path);
		
		return getParameter(parameters, splitPath, 0);
	}
	
	@SuppressWarnings("unchecked")
	private Object getParameter(Map<String, Object> map, String[] path, int index) {
		if (path.length <= index) {
			return null;
		}
		
		if (index < path.length - 1) {
			Object obj = map.get(path[index]);
			if (obj instanceof Map) {
				return getParameter((Map<String,Object>)obj, path, index + 1);
			}
			else {
				return null;
			}
		}
		else {
			return map.get(path[index]);
		}
	}
	
	private String[] splitPath(String path) {
		return path.split("/");
	}
	
	public String getSource() {
		return source;
	}
}
