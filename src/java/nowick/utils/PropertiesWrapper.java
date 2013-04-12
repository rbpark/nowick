package nowick.utils;

import java.util.Map;
import java.util.Properties;

public class PropertiesWrapper {
	private Properties props;
	
	public PropertiesWrapper(Properties props) {
		this.props = props;
	}
	
	public int getInt(String key, int defaultVal) {
		Object obj = props.get(key);
		if (obj == null) {
			return defaultVal;
		}
		
		if (obj instanceof Number) {
			return ((Number)obj).intValue();
		}
		else if (obj instanceof String) {
			return Integer.parseInt((String)obj);
		}
		
		return defaultVal;
	}
	
	public String getString(String key, String defaultVal) {
		Object obj = props.get(key);
		if (obj == null) {
			return defaultVal;
		}
		
		if (obj instanceof String) {
			return (String)obj;
		}
		
		return defaultVal;
	}
}
