package at.fhooe.mcm441.server.preferences;

import java.util.Vector;

/**
 * a reader interface that specifies the operations for reading the preferences
 * 
 * @author manuel
 * 
 */
public interface IPreferenceReader {
	/**
	 * gets all the available prefixes in a vector<string> form in the
	 * preferences
	 * 
	 * @return vector<string> that stores the prefixes
	 */
	public Vector<String> getAllPrefixes();

	/**
	 * gets the stored value for the prefix
	 * 
	 * @param prefix
	 *            the prefix you want to know the value of
	 * @return the stored value
	 */
	public String getValue(String prefix);
}
