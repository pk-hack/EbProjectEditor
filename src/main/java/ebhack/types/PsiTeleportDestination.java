package ebhack.types;

import java.util.HashMap;
import java.util.Map;

public class PsiTeleportDestination extends TileObject {
    public int id;
    public int eventFlag;
    public String name;

    // Note: this is intended to be constructed using the object that gets
    // spit out by SnakeYaml.
    public PsiTeleportDestination(int id, Object deserialize) {
        this.id = id;
        Map<String, Object> top = (Map<String, Object>) deserialize;
        eventFlag = (int) top.get("Event Flag");
        name = (String) top.get("Name");
        x = (int) top.get("X");
        y = (int) top.get("Y");
    }

    // Exports this to an object that Snakeyaml knows how to serialize.
    // (in this case it's a HashMap, but could be a nested HashMap nightmare
    // for different ones.)
    public Object serialize() {
        HashMap<String, Object> result = new HashMap<>();
        result.put("Event Flag", eventFlag);
        result.put("Name", name);
        result.put("X", x);
        result.put("Y", y);
        return result;
    }
}
