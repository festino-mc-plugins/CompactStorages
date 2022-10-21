package com.festp.config;

public enum Key
{
	STORAGE_SIGNAL_RADIUS("storage-signal-radius", 30.0);
	
	private final String name;
	private final Object defaultValue;

	Key(String name, Object defaultValue) {
		this.name = name;
		this.defaultValue = defaultValue;
	}
	public Object getDefault() { return defaultValue; }
	@Override
	public String toString() { return name; }
	
	public Object validateValue(String valueStr) {
		try {
			if (defaultValue instanceof Boolean) {
				return Boolean.parseBoolean(valueStr);
			}
			if (defaultValue instanceof Integer) {
				return Integer.parseInt(valueStr);
			}
			if (defaultValue instanceof Double) {
				return Double.parseDouble(valueStr);
			}
			if (defaultValue instanceof String) {
				return valueStr;
			}
		} catch (Exception e) {}
		return null;
	}
	
	public Class<?> getValueClass() {
		if (defaultValue instanceof Boolean) {
			return Boolean.class;
		}
		if (defaultValue instanceof Integer) {
			return Integer.class;
		}
		if (defaultValue instanceof Double) {
			return Double.class;
		}
		if (defaultValue instanceof String) {
			return String.class;
		}
		return null;
	}
	
	/*public static boolean isValidKey(String keyStr) {
		return getKey(keyStr) != null;
	}
	
	public static AKey getKey(String keyStr) {
		for (AKey key : AKey.values())
			if (key.name.equalsIgnoreCase(keyStr))
				return key;
		return null;
	}
	
	public static List<String> getKeys() {
		List<String> keys = new ArrayList<>();
		for (AKey key : AKey.values()) {
			keys.add(key.name);
		}
		return keys;
	}*/
}
