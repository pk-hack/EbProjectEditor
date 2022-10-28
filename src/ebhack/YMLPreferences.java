package ebhack;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;

import org.yaml.snakeyaml.Yaml;


public class YMLPreferences
{
    private Map<String, String> prefs;
    private File f = null;

    /**
     * Read preferences in from a file, using the given default values. If there
     * is an IO error reading the file, a new file will be created based on the
     * provided defaults.
     * 
     * @param f File to read from.
     */
    public YMLPreferences(File f)
    {
        this.f = f;
        
        if (!f.exists()) {
        	prefs = new HashMap<String, String>();
        } else {
            InputStream input;
			try {
				input = new FileInputStream(f);
				Yaml yaml = new Yaml();
	            prefs = (Map<String, String>) yaml.load(input);
			} catch (FileNotFoundException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
        }
    }

    private String correctName(String name)
    {
        name = name.replace(' ', '_');
        name = name.replace('\'', '_');
        name = name.replace('(', '-');
        name = name.replace(')', '-');
        return name;
    }

    /**
     * Returns the specified preference or null if the preference is not set.
     * 
     * @param name Name of preference.
     * @return Value of preference or null
     */
    public String getValue(String name)
    {
        name = correctName(name);
        String s = (String) this.prefs.get(name);
        if (s != null)
            return s;
        else
            return null;
    }
    
    /**
     * Returns the specified preference.
     * Accepts a default value for the preference.
     * 
     * @param name Name of preference.
     * @param defaultValue Default value of preference.
     * @return Value of preference or null
     */
    public String getValue(String name, String defaultValue)
    {
        name = correctName(name);
        String v = this.prefs.get(name);
        if (v != null)
            return v;
        else {
        	setValue(name, defaultValue);
        	return defaultValue;
        }
    }

    /**
     * Returns if this contains the specified preference.
     * 
     * @param name Name of preference.
     * @return <code>true</code> if preference set or <code>false</code> if
     *         preference not set
     */
    public boolean hasValue(String name)
    {
        return prefs.containsKey(name);
    }

    /**
     * Sets the specified preference to the specified value. Either changes the
     * value on an old preference of the same name or creates a new preference.
     * If either is null they are set to the <code>String</code> "null".
     * 
     * @param name Name of preference.
     * @param value Value of preference.
     */
    public void setValue(String name, String value)
    {
        if (name == null)
            name = "null";
        if (value == null)
            value = "null";
        name = correctName(name);
        prefs.put(name, value);
    }

    /**
     * Returns the specified preference or null if the preference is not set.
     * 
     * @see #getValue(String)
     * @param name Name of preference.
     * @return Value of preference as an <code>int</code> or null
     */
    public int getValueAsInteger(String name)
    {
        return Integer.parseInt(getValue(name));
    }

    /**
     * Sets the specified preference to the specified value. Either changes the
     * value on an old preference of the same name or creates a new preference.
     * 
     * @param name Name of preference.
     * @param value int value of preference.
     */
    public void setValueAsInteger(String name, int value)
    {
        setValue(name, Integer.toString(value));
    }

    /**
     * Returns the specified preference or null if the preference is not set.
     * 
     * @see #getValue(String)
     * @param name Name of preference.
     * @return Value of preference as a <code>boolean</code> or null
     */
    public boolean getValueAsBoolean(String name)
    {
        return new Boolean(getValue(name)).booleanValue();
    }

    /**
     * Sets the specified preference to the specified value. Either changes the
     * value on an old preference of the same name or creates a new preference.
     * 
     * @param name Name of preference.
     * @param value boolean value of preference.
     */
    public void setValueAsBoolean(String name, boolean value)
    {
        setValue(name, Boolean.toString(value));
    }

    /**
     * Removes the specified preference.
     * 
     * @param name Name of preference.
     * @return true if preference deleted, false if not found
     */
    public boolean removeValue(String name)
    {
        name = correctName(name);
        return (prefs.remove(name) != null);
    }

    /**
     * Saves preferences to the specified file.
     * 
     * @param f File to save to.
     * @return true on success, false on failure.
     */
    public boolean save(File f)
    {
        try {
        	Yaml yaml = new Yaml();
        	FileWriter fw = new FileWriter(f);
        	yaml.dump(prefs, fw);
            return true;
        } catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
        return false;
    }

    /**
     * {@link #save(File)}'s to the file loaded from.
     * 
     * @return true on success, false on failure.
     */
    public boolean save()
    {
        return save(f);
    }
}