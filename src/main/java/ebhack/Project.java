package ebhack;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.util.Map;

import javax.swing.JFileChooser;
import javax.swing.filechooser.FileFilter;

import org.yaml.snakeyaml.Yaml;

public class Project {
	private Map<String, Object> proj;
	
	private boolean loaded;
	private String path;
	private File projFile;
	
	public Project() {
		loaded = false;
	}
	
	public static String getDefaultDir() {
		return Ebhack.main.getPrefs().getValue("defaultDir");
	}
	
    public static void setDefaultDir(String dir) {
        Ebhack.main.getPrefs().setValue("defaultDir", dir);
    }
    
    public String getName() {
    	return (String) proj.get("Title");
    }
    
    public String getAuthor() {
    	return (String) proj.get("Author");
    }
    
    public String getDescription() {
    	return (String) proj.get("Description");
    }
    
    public void setName(String name) {
    	proj.put("Title", name);
    }
    
    public void setAuthor(String author) {
    	proj.put("Author", author);
    }
    
    public void setDescription(String desc) {
    	proj.put("Description", desc);
    }
    
    public boolean isLoaded() {
    	return loaded;
    }
    
    public boolean load() {
        try
        {
            JFileChooser jfc = new JFileChooser(Project.getDefaultDir());
            jfc.setFileFilter(new FileFilter()
            {
                public boolean accept(File f)
                {
                    if ((f.getAbsolutePath().toLowerCase().endsWith(".snake")
                    		|| f.isDirectory())
                        && f.exists())
                    {
                        return true;
                    }
                    return false;
                }

                public String getDescription()
                {
                    return "CoilSnake Project (*.snake)";
                }
            });

            if (jfc.showOpenDialog(null) == JFileChooser.APPROVE_OPTION)
            {
                return load(jfc.getSelectedFile());
            }
            else
            {
                return false;
            }
        }
        catch (Exception e)
        {
            e.printStackTrace();
            return false;
        }
    }
    
    // Creates "new" fields with some default values
    private void addNewMetaData() {
    	setName("Untitled");
    	setAuthor("Anonymous");
    	setDescription("No Description");
    }
    
    private boolean hasMetaData() {
    	return proj.containsKey("Title");
    }
    
    public boolean load(File f) {
    	setDefaultDir(f.getParent());
    	Ebhack.main.getPrefs().setValue("lastProject", f.getAbsolutePath());
    	path = f.getParent();
    	
    	if (!f.exists())
    		return false;
    	
        InputStream input;
		try {
			input = new FileInputStream(f);
			Yaml yaml = new Yaml();
            proj = (Map<String, Object>) yaml.load(input);
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		projFile = f;
		
		if (!hasMetaData()) {
			addNewMetaData();
			save();
		}
		
		loaded = true;
    	
    	return true;
    }
    
    public boolean save()
    {
        try {
        	Yaml yaml = new Yaml();
        	FileWriter fw = new FileWriter(projFile);
        	yaml.dump(proj, fw);
            return true;
        } catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
        return false;
    }
    
    public String getDirectory() {
    	return path;
    }
    
    public String getFilenameOrNull(String mod, String fid) {
        var modResourceMap = ((Map<String,Map<String, String>>) proj.get("resources")).get(mod);
        if (!modResourceMap.containsKey(fid)) {
            // Check if a file following the default naming convention exists - if so, add it to the project resource
            // map.
            var success = tryFindMissingResource(mod, modResourceMap, fid);
            if (!success) return null;
        }
    	return path + File.separator + modResourceMap.get(fid);
    }

    public String getFilename(String mod, String fid) {
        String result = getFilenameOrNull(mod, fid);
        if (result == null) {
            throw new IllegalStateException("Resource '" + mod + "/" + fid +
                                            "' does not exist in project and cannot be located");
        }
        return result;
    }

    public boolean tryFindMissingResource(String mod, Map<String, String> modResourceMap, String fid) {
        String defaultFileName = null;
        switch (mod) {
            case "eb.SpriteGroupModule":
                if (fid.startsWith("SpriteGroups/")) {
                    defaultFileName = fid + ".png";
                }
                break;
        }
        if (defaultFileName != null) {
            File f = new File(path + File.separator + defaultFileName);
            if (f.exists()) {
                modResourceMap.put(fid, defaultFileName);
                return true;
            }
        }
        return false;
    }
    
    public void close() {
    	loaded = false;
    	proj = null;
    }
}
