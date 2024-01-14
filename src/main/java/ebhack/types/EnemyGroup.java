package ebhack.types;

import ebhack.MapData;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class EnemyGroup {
    public int background1;
    public int background2;
    public List<EnemyCount> enemies = new ArrayList<>();
    public int fearEventFlag;
    public String fearMode;
    public int letterboxSize;
    
    // Note: this is intended to be constructed using the object that gets
    // spit out by SnakeYaml.
    public EnemyGroup(Object deserialize) {
        Map<String, Object> top = (Map<String, Object>) deserialize;
        background1 = (int) top.get("Background 1");
        background2 = (int) top.get("Background 2");
        for (Object enemy : ((List<Object>) top.get("Enemies"))) {
            enemies.add(new EnemyCount(enemy));
        }
        fearEventFlag = (int) top.get("Fear event flag");
        fearMode = (String) top.get("Fear mode");
        letterboxSize = (int) top.get("Letterbox Size");
    }

    public static class EnemyCount {
        public int amount;
        public int enemy;

        public EnemyCount(Object deserialize) {
            Map<String, Object> top = (Map<String, Object>) deserialize;
            amount = (int) top.get("Amount");
            enemy = (int) top.get("Enemy");
        }
    }
}
