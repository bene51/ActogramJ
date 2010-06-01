package actoj;

import java.util.HashMap;
import java.util.Properties;
import java.io.*;

public class Settings {

	public static final String PROPERTY_FILE =
		System.getProperty("user.home") +
		File.separator +
		".ActogramJ.props";

	public static final int DEFAULT_START_COL = 11;
	public static final int DEFAULT_COL_COUNT = 32;
	public static final int DEFAULT_START_ROW = 1;
	public static final int DEFAULT_ROW_COUNT = -1;
	public static final int DEFAULT_SPP       = 24 * 60;
	public static final int DEFAULT_CAL_VALUE = 1;
	public static final int DEFAULT_CAL_UNIT  = 2; // MINUTES

	public static final String START_COL = "start_column";
	public static final String COL_COUNT = "column_count";
	public static final String START_ROW = "start_row";
	public static final String ROW_COUNT = "row_count";
	public static final String SPP       = "samples_per_period";
	public static final String CAL_VALUE = "calibration_value";
	public static final String CAL_UNIT  = "calibration_unit";

	public static final HashMap<String, String> defaults =
		new HashMap<String, String>();

	static {
		defaults.put(START_COL, Integer.toString(DEFAULT_START_COL));
		defaults.put(COL_COUNT, Integer.toString(DEFAULT_COL_COUNT));
		defaults.put(START_ROW, Integer.toString(DEFAULT_START_ROW));
		defaults.put(ROW_COUNT, Integer.toString(DEFAULT_ROW_COUNT));
		defaults.put(SPP,       Integer.toString(DEFAULT_SPP));
		defaults.put(CAL_UNIT,  Integer.toString(DEFAULT_CAL_UNIT));
		defaults.put(CAL_VALUE, Integer.toString(DEFAULT_CAL_VALUE));
	}

	public static synchronized void set(String key, Object value) throws IOException {
		Properties props = new Properties();
		try {
			props.load(new FileInputStream(PROPERTY_FILE));
		} catch(FileNotFoundException e) {
			props.putAll(defaults);
		} catch(IOException e) {
		}
		props.put(key, value.toString());
		props.store(new FileOutputStream(PROPERTY_FILE),
				"ActogramJ properties");
	}

	public static synchronized String get(String key) {
		Properties props = new Properties();
		try {
			props.load(new FileInputStream(PROPERTY_FILE));
		} catch(FileNotFoundException e) {
			props.putAll(defaults);
		} catch(IOException e) {
			return null;
		}
		if(props.containsKey(key))
			return props.getProperty(key);
		if(defaults.containsKey(key))
			return defaults.get(key);
		return null;
	}
}

