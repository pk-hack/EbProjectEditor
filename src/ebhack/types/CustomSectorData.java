package ebhack.types;

import ebhack.MapData;

import java.awt.*;
import java.io.File;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PrintStream;
import java.util.*;
import java.util.List;

public class CustomSectorData {
    public static final String sectorDataPath = File.separator + "ccscript" + File.separator + "custom_sector_data.ccs";
    private final Map<String, SectorProperty> properties = new LinkedHashMap<>();
    public CustomSectorData() {}

    public Collection<String> getKeys() {
        return properties.keySet();
    }

    public boolean containsKey(String key) {
        return properties.containsKey(key);
    }

    public void load(InputStream inStream) {
        Scanner in = new Scanner(inStream);
        while (in.hasNextLine()) {
            String line = in.nextLine();
            if (line.startsWith("// Type")) {
                loadProperty(line, in);
            }
        }
    }

    public void clear() {
        properties.clear();
    }

    public void write(OutputStream outStream) {
        PrintStream out = new PrintStream(outStream);
        out.println("// Hey! Be very careful if you edit this manually; it's designed so EbProjectEditor can read it.");
        out.println();
        for (Map.Entry<String, SectorProperty> sectorEntry : properties.entrySet()) {
            String key = sectorEntry.getKey();
            SectorProperty property = sectorEntry.getValue();
            property.write(key, out);
            out.println();
        }
    }

    public void createProperty(String key, String type, String defaultValue, int sectorCount) {
        SectorProperty property = new SectorProperty(type, defaultValue);
        properties.put(key, property);
        for (int i = 0; i < sectorCount; i++) {
            property.values.add(defaultValue);
        }
    }

    private void loadProperty(String firstLine, Scanner in) {
        // Read a type header
        String type = firstLine.substring(("// Type: ".length()));
        String defaultLine = in.nextLine();
        String defaultValue = defaultLine.substring("// Default: ".length());
        String keyLine = in.nextLine();
        String key = keyLine.substring(0, keyLine.indexOf(":"));
        SectorProperty property = new SectorProperty(type, defaultValue);
        properties.put(key, property);
        // Read values until we hit a blank line
        String nextLine = in.nextLine();
        while (in.hasNextLine() && !nextLine.isEmpty()) {
            String value = nextLine.substring(type.length() + 3);
            property.values.add(value);
            nextLine = in.nextLine();
        }
    }

    public String getDefault(String key) {
        return properties.get(key).defaultValue;
    }

    public String getSectorValue(String key, Point sectorCoords) {
        SectorProperty property = properties.get(key);
        int index = sectorCoords.x + sectorCoords.y * MapData.WIDTH_IN_SECTORS;
        return property.values.get(index);
    }

    public void setSectorValue(String key, Point sectorCoords, String value) {
        SectorProperty property = properties.get(key);
        int index = sectorCoords.x + sectorCoords.y * MapData.WIDTH_IN_SECTORS;
        property.values.set(index, value);
    }

    public void extend(int rows) {
        for (SectorProperty property : properties.values()) {
            for (int i = 0; i < rows; i++) {
                for (int j = 0; j < MapData.WIDTH_IN_SECTORS; j++) {
                    property.values.add(property.defaultValue);
                }
            }
        }
    }

    private static class SectorProperty {
        String type;
        String defaultValue;
        List<String> values = new ArrayList<>();

        private SectorProperty(String type, String defaultValue) {
            this.type = type;
            this.defaultValue = defaultValue;
        }

        private void write(String key, PrintStream out) {
            out.println("// Type: " + this.type);
            out.println("// Default: " + this.defaultValue);
            out.println(key + ":");
            for (String value : values) {
                out.println("  " + type + " " + value);
            }
        }
    }
}
