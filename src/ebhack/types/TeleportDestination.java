package ebhack.types;

import java.util.HashMap;
import java.util.Map;

public class TeleportDestination extends TileObject  {
    public int id;
    public int direction;
    public int unknown;
    public int warpStyle;

    // Note: this is intended to be constructed using the object that gets
    // spit out by SnakeYaml.
    public TeleportDestination(int id, Object deserialize) {
        this.id = id;
        Map<String, Object> top = (Map<String, Object>) deserialize;
        direction = (int) top.get("Direction");
        unknown = (int) top.get("Unknown");
        warpStyle = (int) top.get("Warp Style");
        x = (int) top.get("X");
        y = (int) top.get("Y");
    }

    // Exports this to an object that Snakeyaml knows how to serialize.
    // (in this case it's a HashMap, but could be a nested HashMap nightmare
    // for different ones.)
    public Object serialize() {
        HashMap<String, Integer> result = new HashMap<>();
        result.put("Direction", direction);
        result.put("Unknown", unknown);
        result.put("Warp Style", warpStyle);
        result.put("X", x);
        result.put("Y", y);
        return result;
    }
}
