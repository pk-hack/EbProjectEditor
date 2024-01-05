package ebhack.types;

import ebhack.MapData;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class MapEnemyGroup {
    public int eventFlag;
    public List<SpawnGroup> subGroup1 = new ArrayList<>();
    public int subGroup1Rate;
    public List<SpawnGroup> subGroup2 = new ArrayList<>();
    public int subGroup2Rate;

    // Note: this is intended to be constructed using the object that gets
    // spit out by SnakeYaml.
    public MapEnemyGroup(Object deserialize) {
        Map<String, Object> top = (Map<String, Object>) deserialize;
        eventFlag = (int) top.get("Event Flag");
        for (Object group : ((Map<Integer, Object>) top.get("Sub-Group 1")).values()) {
            subGroup1.add(new SpawnGroup(group));
        }
        subGroup1Rate = (int) top.get("Sub-Group 1 Rate");
        for (Object group : ((Map<Integer, Object>) top.get("Sub-Group 2")).values()) {
            subGroup2.add(new SpawnGroup(group));
        }
        subGroup2Rate = (int) top.get("Sub-Group 2 Rate");
    }

    public static class SpawnGroup {
        public int enemyGroup;
        public int probability;

        public SpawnGroup(Object deserialize) {
            Map<String, Object> top = (Map<String, Object>) deserialize;
            enemyGroup = (int) top.get("Enemy Group");
            probability = (int) top.get("Probability");
        }
    }
}
