package actoj;

import java.util.HashMap;
import java.util.Properties;
import java.io.*;

/**
 * Class to store various settings.
 */
public class Settings {

	/**
	 * Path to the settings' file.
	 */
	public static final String PROPERTY_FILE =
		System.getProperty("user.home") +
		File.separator +
		".ActogramJ.props";

	/** Default start column */
	public static final int DEFAULT_START_COL = 11;
	/** Default column count */
	public static final int DEFAULT_END_COL = -1;
	/** Default start row */
	public static final int DEFAULT_START_ROW = 1;
	/** Default row count */
	public static final int DEFAULT_END_ROW = -1;
	/** Default samples per period */
	public static final int DEFAULT_SPP       = 24 * 60;
	/** Default interval value */
	public static final int DEFAULT_CAL_VALUE = 1;
	/** Default interval unit */
	public static final int DEFAULT_CAL_UNIT  = 2; // MINUTES

	public static final String START_COL = "start_column";
	public static final String END_COL   = "end_column";
	public static final String START_ROW = "start_row";
	public static final String END_ROW   = "end_row";
	public static final String SPP       = "samples_per_period";
	public static final String CAL_VALUE = "calibration_value";
	public static final String CAL_UNIT  = "calibration_unit";

	public static final HashMap<String, String> defaults =
		new HashMap<String, String>();

	static {
		defaults.put(START_COL, Integer.toString(DEFAULT_START_COL));
		defaults.put(END_COL,   Integer.toString(DEFAULT_END_COL));
		defaults.put(START_ROW, Integer.toString(DEFAULT_START_ROW));
		defaults.put(END_ROW,   Integer.toString(DEFAULT_END_ROW));
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

