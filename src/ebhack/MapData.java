package ebhack;

import ebhack.types.EnemyGroup;
import ebhack.types.MapEnemyGroup;
import org.yaml.snakeyaml.DumperOptions;
import org.yaml.snakeyaml.Yaml;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.*;
import java.util.*;
import java.util.List;

public class MapData {
    public static final int WIDTH_IN_TILES = 32 * 8;
    public static final int HEIGHT_IN_TILES = 80 * 4;
    public static final int SECTOR_WIDTH = 8;
    public static final int SECTOR_HEIGHT = 4;
    public static final int WIDTH_IN_SECTORS = WIDTH_IN_TILES
            / SECTOR_WIDTH;
    public static final int HEIGHT_IN_SECTORS = HEIGHT_IN_TILES
            / SECTOR_HEIGHT;
    public static final int HEIGHT_IN_DOUBLE_SECTORS = HEIGHT_IN_TILES / (SECTOR_HEIGHT * 2);
    public static final int TILE_WIDTH = 32;
    public static final int TILE_HEIGHT = 32;

    public static final int NUM_MAP_TSETS = 32;
    public static final int NUM_DRAW_TSETS = 20;

    // Stores the map tiles
    private int[][] mapTiles;
    private Sector[][] sectors;
    private ArrayList<SpriteEntry>[][] spriteAreas;
    private ArrayList<Door>[][] doorAreas;
    private ArrayList<NPC> npcs;
    private static ArrayList<Image[]> spriteGroups;
    private static ArrayList<Integer[]> spriteGroupDims;
    private int[][] enemyPlacement;
    private Hotspot[] hotspots;
    private final ArrayList<MapEnemyGroup> mapEnemyGroups = new ArrayList<>();
    private final ArrayList<EnemyGroup> enemyGroups = new ArrayList<>();
    private final ArrayList<Integer> enemyOverworldSprites = new ArrayList<>();
    private final ArrayList<Image> enemySpriteImages = new ArrayList<>();

    public MapData() {
        reset();
    }

    public void reset() {
        mapTiles = new int[HEIGHT_IN_TILES][WIDTH_IN_TILES];
        sectors = new Sector[HEIGHT_IN_SECTORS][WIDTH_IN_SECTORS];
        for (int i = 0; i < sectors.length; ++i)
            for (int j = 0; j < sectors[i].length; ++j)
                sectors[i][j] = new Sector();
        spriteAreas = new ArrayList[HEIGHT_IN_DOUBLE_SECTORS][WIDTH_IN_SECTORS];
        for (int i = 0; i < spriteAreas.length; ++i)
            for (int j = 0; j < spriteAreas[i].length; ++j)
                spriteAreas[i][j] = new ArrayList<SpriteEntry>();
        doorAreas = new ArrayList[HEIGHT_IN_DOUBLE_SECTORS][WIDTH_IN_SECTORS];
        for (int i = 0; i < doorAreas.length; ++i)
            for (int j = 0; j < doorAreas[i].length; ++j)
                doorAreas[i][j] = new ArrayList<Door>();
        npcs = new ArrayList<NPC>();
        spriteGroups = new ArrayList<Image[]>();
        spriteGroupDims = new ArrayList<Integer[]>();
        enemyPlacement = new int[HEIGHT_IN_TILES / 2][WIDTH_IN_TILES / 2];
        hotspots = new Hotspot[56];
        for (int i = 0; i < hotspots.length; ++i)
            hotspots[i] = new Hotspot();
    }

    public void load(Project proj) {
        importMapTiles(new File(proj.getFilename("eb.MapModule",
                "map_tiles")));
        importSectors(new File(proj.getFilename("eb.MapModule",
                "map_sectors")));
        importSpritePlacements(new File(proj.getFilename(
                "eb.MapSpriteModule", "map_sprites")));
        importDoors(new File(proj.getFilename("eb.DoorModule", "map_doors")));
        importEnemyPlacement(new File(proj.getFilename("eb.MapEnemyModule",
                "map_enemy_placement")));
        importHotspots(new File(proj.getFilename("eb.MiscTablesModule",
                "map_hotspots")));
        loadExtraResources(proj);
    }

    public void loadExtraResources(Project proj) {
        importNPCs(new File(proj.getFilename("eb.MiscTablesModule",
                "npc_config_table")));
        importSpriteGroups(proj);
        importEnemyData(proj);
        importEnemySprites(proj);
    }

    public void save(Project proj) {
        exportMapTiles(new File(proj.getFilename("eb.MapModule",
                "map_tiles")));
        exportSectors(new File(proj.getFilename("eb.MapModule",
                "map_sectors")));
        exportSpritePlacements(new File(proj.getFilename(
                "eb.MapSpriteModule", "map_sprites")));
        exportDoors(new File(proj.getFilename("eb.DoorModule", "map_doors")));
        exportEnemyPlacement(new File(proj.getFilename("eb.MapEnemyModule",
                "map_enemy_placement")));
        exportHotspots(new File(proj.getFilename("eb.MiscTablesModule",
                "map_hotspots")));
    }

    public NPC getNPC(int n) {
        return npcs.get(n);
    }

    public EnemyGroup getEnemyGroup(int n) {
        return enemyGroups.get(n);
    }

    public MapEnemyGroup getMapEnemyGroup(int n) {
        return mapEnemyGroups.get(n);
    }

    public Image getEnemySprite(int n, boolean inBattle) {
        if (inBattle) {
            Image result = enemySpriteImages.get(n);
            if (result != null) {
                return result;
            }
            // Continue anyway in case it has an overworld sprite.
            // (it's probably a magic butterfly)
        }
        return getSpriteImage(enemyOverworldSprites.get(n), 2);
    }

    public Integer[] getSpriteWH(int n) {
        return spriteGroupDims.get(n);
    }

    // Sprite Editing

    public SpriteEntry getSpriteEntryFromCoords(int areaX, int areaY,
                                                int x, int y) {
        Integer[] wh;
        NPC npc;
        for (SpriteEntry e : spriteAreas[areaY][areaX]) {
            npc = npcs.get(e.npcID);
            wh = spriteGroupDims.get(npc.sprite);
            if ((e.x >= x - wh[0] / 2) && (e.x <= x + wh[0] / 2)
                    && (e.y >= y - wh[1] / 2) && (e.y <= y + wh[1] / 2)) {
                return e;
            }
        }
        return null;
    }

    public int popNPCFromCoords(int areaX, int areaY, int x, int y) {
        Integer[] wh;
        NPC npc;
        for (SpriteEntry e : spriteAreas[areaY][areaX]) {
            npc = npcs.get(e.npcID);
            wh = spriteGroupDims.get(npc.sprite);
            if ((e.x >= x - wh[0] / 2) && (e.x <= x + wh[0] / 2)
                    && (e.y >= y - wh[1] / 2) && (e.y <= y + wh[1] / 2)) {
                spriteAreas[areaY][areaX].remove(e);
                return e.npcID;
            }
        }
        return -1;
    }

    public void pushNPCFromCoords(int npcid, int areaX, int areaY, int x,
                                  int y) {
        if ((areaX >= 0) && (areaY >= 0))
            spriteAreas[areaY][areaX].add(new SpriteEntry(x, y, npcid));
    }

    public List<SpriteEntry> getSpriteArea(int areaX, int areaY) {
        if (areaX < 0 || areaX >= WIDTH_IN_SECTORS
                || areaY < 0 || areaY >= HEIGHT_IN_DOUBLE_SECTORS) {
            return Collections.emptyList();
        }
        return spriteAreas[areaY][areaX];
    }

    // Door Editing

    public java.util.List<Door> getDoorArea(int areaX, int areaY) {
        if (areaX < 0 || areaX >= WIDTH_IN_SECTORS
                || areaY < 0 || areaY >= HEIGHT_IN_DOUBLE_SECTORS) {
            return Collections.emptyList();
        }
        return doorAreas[areaY][areaX];
    }

    public Door getDoorFromCoords(int areaX, int areaY, int x, int y) {
        for (Door e : doorAreas[areaY][areaX]) {
            if ((x <= e.x + 1) && (x >= e.x) && (y <= e.y + 1)
                    && (y >= e.y)) {
                return e;
            }
        }
        return null;
    }

    public Door popDoorFromCoords(int areaX, int areaY, int x, int y) {
        for (Door e : doorAreas[areaY][areaX]) {
            if ((x <= e.x + 1) && (x >= e.x) && (y <= e.y + 1)
                    && (y >= e.y)) {
                doorAreas[areaY][areaX].remove(e);
                return e;
            }
        }
        return null;
    }

    public void pushDoorFromCoords(Door door, int areaX, int areaY) {
        if ((areaX >= 0) && (areaY >= 0))
            doorAreas[areaY][areaX].add(door);
    }

    // Enemy Editing

    public int getMapEnemyGroup(int x, int y) {
        return enemyPlacement[y][x];
    }

    public void setMapEnemyGroup(int x, int y, int val) {
        enemyPlacement[y][x] = val;
    }

    // Hotspot

    public int numHotspots() {
        return 56;
    }

    public Hotspot getHotspot(int n) {
        return hotspots[n];
    }

    // Other

    public Sector getSector(int sectorX, int sectorY) {
        return sectors[sectorY][sectorX];
    }

    public Image getSpriteImage(int sprite, int direction) {
        return spriteGroups.get(sprite)[direction];
    }

    private void importMapTiles(File f) {
        if (f == null)
            return;

        try {
            FileInputStream in = new FileInputStream(f);
            setMapTilesFromStream(in);
        } catch (FileNotFoundException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }

    private void exportMapTiles(File f) {
        if (f == null)
            return;

        try {
            FileOutputStream out = new FileOutputStream(f);
            writeMapTilesToStream(out);
        } catch (FileNotFoundException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }

    private void importNPCs(File f) {
        InputStream input;
        try {
            input = new FileInputStream(f);
            Yaml yaml = new Yaml();
            Map<Integer, Map<String, Object>> sectorsMap = (Map<Integer, Map<String, Object>>) yaml
                    .load(input);

            NPC npc;
            for (Map.Entry<Integer, Map<String, Object>> entry : sectorsMap.entrySet()) {
                npc = new NPC((Integer) entry.getValue().get("Sprite"),
                        (String) entry.getValue().get("Direction"));
                npcs.add(npc);
            }
        } catch (FileNotFoundException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }

    private void importSpriteGroups(Project proj) {
        int w, h, x, y, z;
        int i = 0;
        while (true) {
            spriteGroups.add(new Image[4]);
            try {
                String spriteResourceFid = "SpriteGroups/" + ToolModule.addZeros(i + "", 3);
                String spriteFilename = proj.getFilenameOrNull("eb.SpriteGroupModule", spriteResourceFid);
                if (spriteFilename == null) {
                    break;
                }
                BufferedImage sheet = ImageIO.read(new File(spriteFilename));
                Graphics2D sg = sheet.createGraphics();

                w = sheet.getWidth() / 4;
                h = sheet.getHeight() / 4;
                spriteGroupDims.add(new Integer[]{w, h});
                z = 0;
                for (y = 0; y < 2; ++y) {
                    for (x = 0; x < 4; x += 2) {
                        BufferedImage sp = new BufferedImage(w, h,
                                BufferedImage.TYPE_INT_ARGB);
                        Graphics2D g = sp.createGraphics();
                        g.setComposite(sg.getComposite());
                        g.drawImage(sheet, 0, 0, w, h, w * x, h * y, w * x
                                + w, h * y + h, null);
                        g.dispose();
                        spriteGroups.get(i)[z] = sp;
                        ++z;
                    }
                }
            } catch (IOException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
            i++;
        }
    }

    private void importEnemySprites(Project proj) {
        // Going with hard-coded 256 limit in case somebody expands the table, and because
        // some entries may be missing. If someone raises the limit past 256 we can have a
        // party while I update it.
        for (int i = 0; i < 256; i++) {
            Image image = null;
            try {
                String spriteResourceFid = "BattleSprites/" + ToolModule.addZeros(i + "", 3);
                String spriteFilename = proj.getFilenameOrNull("eb.EnemyModule", spriteResourceFid);
                if (spriteFilename != null) {
                    image = ImageIO.read(new File(spriteFilename));
                }
            } catch (IOException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
            // Always add images; nulls will be used to pad out the array
            enemySpriteImages.add(image);
        }
    }

    private void importEnemyData(Project proj) {
        File mapGroupsFile = new File(proj.getFilename(
                "eb.MapEnemyModule", "map_enemy_groups"));
        File enemyGroupsFile = new File(proj.getFilename(
                "eb.EnemyModule", "enemy_groups"));
        File enemiesFile = new File(proj.getFilename(
                "eb.EnemyModule", "enemy_configuration_table"));
        try {
            Map<Integer, Object> mapEnemyGroupYaml = new Yaml().load(new FileInputStream(mapGroupsFile));
            for (Map.Entry<Integer, Object> entry : mapEnemyGroupYaml.entrySet()) {
                MapEnemyGroup group = new MapEnemyGroup(entry.getValue());
                mapEnemyGroups.add(group);
            }
            Map<Integer, Object> enemyGroupYaml = new Yaml().load(new FileInputStream(enemyGroupsFile));
            for (Map.Entry<Integer, Object> entry : enemyGroupYaml.entrySet()) {
                EnemyGroup group = new EnemyGroup(entry.getValue());
                enemyGroups.add(group);
            }
            Map<Integer, Object> enemiesYaml = new Yaml().load(new FileInputStream(enemiesFile));
            for (Map.Entry<Integer, Object> entry : enemiesYaml.entrySet()) {
                Map<String, Object> enemy = (Map<String, Object>) entry.getValue();
                enemyOverworldSprites.add((int) enemy.get("Overworld Sprite"));
            }
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (Throwable e) {
            e.printStackTrace();
            throw e;
        }
    }

    public static class Door {
        public int x, y, eventFlag;
        public String type, pointer;
        // Rope/Ladder
        public int climbDir;
        // Door stuff
        public int destX, destY, destDir, style;

        public Door() {
            this.type = "ladder";
            this.pointer = "$0";
        }

        public Door(int x, int y, String type) {
            this.x = x;
            this.y = y;
            this.type = type;
        }

        public Door copy() {
            Door d = new Door();
            d.eventFlag = eventFlag;
            d.type = type;
            d.pointer = pointer;
            d.climbDir = climbDir;
            d.destX = destX;
            d.destY = destY;
            d.destDir = destDir;
            d.style = style;
            return d;
        }

        private static final Color[] doorColors = new Color[]{
                new Color(83, 81, 84), // Switch
                new Color(204, 37, 41), // Rope
                new Color(62, 150, 81), // Ladder
                new Color(57, 106, 177), // Door
                new Color(146, 36, 40), // Escalator
                new Color(107, 76, 154), // Stairway
                new Color(218, 124, 48), // Object
                new Color(148, 139, 61), // Person
        };

        public Color getColor() {
            if (type.equals("door"))
                return doorColors[3];
            else if (type.equals("rope"))
                return doorColors[1];
            else if (type.equals("ladder"))
                return doorColors[2];
            else if (type.equals("escalator"))
                return doorColors[4];
            else if (type.equals("stairway"))
                return doorColors[5];
            else if (type.equals("object"))
                return doorColors[6];
            else if (type.equals("switch"))
                return doorColors[0];
            else
                return doorColors[7];
        }
    }

    private final String[] climbDirs = new String[]{"nw", "ne", "sw",
            "se", "nowhere"};
    private final String[] destDirs = new String[]{"down", "up", "right",
            "left"};

    private int indexOf(Object[] arr, Object target) {
        int i = 0;
        for (Object e : arr) {
            if (e.equals(target))
                return i;
            ++i;
        }
        return -1;
    }

    private void importDoors(File f) {
        InputStream input;
        try {
            input = new FileInputStream(f);
            Yaml yaml = new Yaml();
            Map<Integer, Map<Integer, java.util.List<Map<String, Object>>>> doorsMap = (Map<Integer, Map<Integer, java.util.List<Map<String, Object>>>>) yaml
                    .load(input);
            int y, x;
            ArrayList<Door> area;
            for (Map.Entry<Integer, Map<Integer, java.util.List<Map<String, Object>>>> rowEntry : doorsMap
                    .entrySet()) {
                y = rowEntry.getKey();
                for (Map.Entry<Integer, java.util.List<Map<String, Object>>> entry : rowEntry
                        .getValue().entrySet()) {
                    x = entry.getKey();
                    area = this.doorAreas[y][x];
                    area.clear();
                    if (entry.getValue() == null)
                        continue;

                    for (Map<String, Object> de : entry.getValue()) {
                        Door d = new Door((Integer) de.get("X"),
                                (Integer) de.get("Y"),
                                ((String) de.get("Type")).toLowerCase());
                        if (d.type.equals("stairway")
                                || d.type.equals("escalator")) {
                            d.climbDir = indexOf(climbDirs,
                                    de.get("Direction"));
                        } else if (d.type.equals("door")) {
                            d.pointer = (String) de.get("Text Pointer");
                            d.eventFlag = (Integer) de.get("Event Flag");
                            d.destX = (Integer) de.get("Destination X");
                            d.destY = (Integer) de.get("Destination Y");
                            d.destDir = indexOf(destDirs,
                                    ((String) de.get("Direction"))
                                            .toLowerCase());
                            d.style = (Integer) de.get("Style");
                        } else if (d.type.equals("switch")) {
                            d.pointer = (String) de.get("Text Pointer");
                            d.eventFlag = (Integer) de.get("Event Flag");
                        } else if (d.type.equals("person")
                                || d.type.equals("object")) {
                            d.pointer = (String) de.get("Text Pointer");
                        }
                        area.add(d);
                    }
                }
            }
        } catch (FileNotFoundException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }

    private void exportDoors(File f) {
        Map<Integer, Map<Integer, java.util.List<Map<String, Object>>>> doorsMap = new HashMap<Integer, Map<Integer, java.util.List<Map<String, Object>>>>();
        int x, y = 0;
        for (java.util.List<Door>[] row : doorAreas) {
            Map<Integer, java.util.List<Map<String, Object>>> rowOut = new HashMap<Integer, java.util.List<Map<String, Object>>>();
            x = 0;
            for (java.util.List<Door> area : row) {
                if (area.isEmpty())
                    rowOut.put(x, null);
                else {
                    java.util.List<Map<String, Object>> areaOut = new ArrayList<Map<String, Object>>();
                    for (Door d : area) {
                        Map<String, Object> dOut = new HashMap<String, Object>();
                        dOut.put("X", d.x);
                        dOut.put("Y", d.y);
                        dOut.put("Type", d.type);
                        if (d.type.equals("stairway")
                                || d.type.equals("escalator")) {
                            dOut.put("Direction", climbDirs[d.climbDir]);
                        } else if (d.type.equals("door")) {
                            dOut.put("Text Pointer", d.pointer);
                            dOut.put("Event Flag", d.eventFlag);
                            dOut.put("Destination X", d.destX);
                            dOut.put("Destination Y", d.destY);
                            dOut.put("Direction", destDirs[d.destDir]);
                            dOut.put("Style", d.style);
                        } else if (d.type.equals("switch")) {
                            dOut.put("Text Pointer", d.pointer);
                            dOut.put("Event Flag", d.eventFlag);
                        } else if (d.type.equals("person")
                                || d.type.equals("object")) {
                            dOut.put("Text Pointer", d.pointer);
                        }
                        areaOut.add(dOut);
                    }
                    rowOut.put(x, areaOut);
                }
                ++x;
            }
            doorsMap.put(y, rowOut);
            ++y;
        }

        try {
            FileWriter fw = new FileWriter(f);
            DumperOptions options = new DumperOptions();
            options.setDefaultFlowStyle(DumperOptions.FlowStyle.BLOCK);
            Yaml yaml = new Yaml(options);
            yaml.dump(doorsMap, fw);
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }

    private void importSpritePlacements(File f) {
        InputStream input;
        try {
            input = new FileInputStream(f);
            Yaml yaml = new Yaml();
            Map<Integer, Map<Integer, java.util.List<Map<String, Integer>>>> spritesMap = (Map<Integer, Map<Integer, java.util.List<Map<String, Integer>>>>) yaml
                    .load(input);
            int y, x;
            ArrayList<SpriteEntry> area;
            for (Map.Entry<Integer, Map<Integer, java.util.List<Map<String, Integer>>>> rowEntry : spritesMap
                    .entrySet()) {
                y = rowEntry.getKey();
                for (Map.Entry<Integer, java.util.List<Map<String, Integer>>> entry : rowEntry
                        .getValue().entrySet()) {
                    x = entry.getKey();
                    area = this.spriteAreas[y][x];
                    area.clear();
                    if (entry.getValue() == null)
                        continue;

                    for (Map<String, Integer> spe : entry.getValue()) {
                        area.add(new SpriteEntry(spe.get("X"),
                                spe.get("Y"), spe.get("NPC ID")));
                    }
                }
            }
        } catch (FileNotFoundException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }

    private void exportSpritePlacements(File f) {
        Map<Integer, Map<Integer, java.util.List<Map<String, Integer>>>> spritesMap = new HashMap<Integer, Map<Integer, java.util.List<Map<String, Integer>>>>();
        int x, y = 0;
        for (java.util.List<SpriteEntry>[] row : spriteAreas) {
            Map<Integer, java.util.List<Map<String, Integer>>> rowOut = new HashMap<Integer, java.util.List<Map<String, Integer>>>();
            x = 0;
            for (java.util.List<SpriteEntry> area : row) {
                if (area.isEmpty())
                    rowOut.put(x, null);
                else {
                    java.util.List<Map<String, Integer>> areaOut = new ArrayList<Map<String, Integer>>();
                    for (SpriteEntry se : area) {
                        Map<String, Integer> seOut = new HashMap<String, Integer>();
                        seOut.put("X", se.x);
                        seOut.put("Y", se.y);
                        seOut.put("NPC ID", se.npcID);
                        areaOut.add(seOut);
                    }
                    rowOut.put(x, areaOut);
                }
                ++x;
            }
            spritesMap.put(y, rowOut);
            ++y;
        }

        try {
            FileWriter fw = new FileWriter(f);
            DumperOptions options = new DumperOptions();
            options.setDefaultFlowStyle(DumperOptions.FlowStyle.BLOCK);
            Yaml yaml = new Yaml(options);
            yaml.dump(spritesMap, fw);
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }

    public static class Hotspot {
        int x1, y1, x2, y2;
    }

    private void importHotspots(File f) {
        InputStream input;
        try {
            input = new FileInputStream(f);
            Yaml yaml = new Yaml();
            Map<Integer, Map<String, Integer>> hsMap = (Map<Integer, Map<String, Integer>>) yaml
                    .load(input);

            int i;
            for (Map.Entry<Integer, Map<String, Integer>> entry : hsMap
                    .entrySet()) {
                i = entry.getKey();
                hotspots[i].x1 = entry.getValue().get("X1");
                hotspots[i].y1 = entry.getValue().get("Y1");
                hotspots[i].x2 = entry.getValue().get("X2");
                hotspots[i].y2 = entry.getValue().get("Y2");
            }
        } catch (FileNotFoundException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }

    private void exportHotspots(File f) {
        Map<Integer, Map<String, Integer>> hsMap = new HashMap<Integer, Map<String, Integer>>();
        int i = 0;
        for (Hotspot hs : hotspots) {
            Map<String, Integer> entry = new HashMap<String, Integer>();
            entry.put("X1", hs.x1);
            entry.put("Y1", hs.y1);
            entry.put("X2", hs.x2);
            entry.put("Y2", hs.y2);
            hsMap.put(i, entry);
            ++i;
        }

        try {
            FileWriter fw = new FileWriter(f);
            DumperOptions options = new DumperOptions();
            options.setDefaultFlowStyle(DumperOptions.FlowStyle.BLOCK);
            Yaml yaml = new Yaml(options);
            yaml.dump(hsMap, fw);
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }

    private void importEnemyPlacement(File f) {
        InputStream input;
        try {
            input = new FileInputStream(f);
            Yaml yaml = new Yaml();
            Map<Integer, Map<String, Integer>> enemiesMap = (Map<Integer, Map<String, Integer>>) yaml
                    .load(input);

            int y, x;
            for (Map.Entry<Integer, Map<String, Integer>> entry : enemiesMap
                    .entrySet()) {
                y = entry.getKey() / (WIDTH_IN_TILES / 2);
                x = entry.getKey() % (WIDTH_IN_TILES / 2);
                enemyPlacement[y][x] = entry.getValue().get(
                        "Enemy Map Group");
            }
        } catch (FileNotFoundException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }

    private void exportEnemyPlacement(File f) {
        Map<Integer, Map<String, Integer>> enemiesMap = new HashMap<Integer, Map<String, Integer>>();
        int i = 0;
        for (int[] row : enemyPlacement) {
            for (int ep : row) {
                Map<String, Integer> entry = new HashMap<String, Integer>();
                entry.put("Enemy Map Group", ep);
                enemiesMap.put(i, entry);
                ++i;
            }
        }

        try {
            FileWriter fw = new FileWriter(f);
            DumperOptions options = new DumperOptions();
            options.setDefaultFlowStyle(DumperOptions.FlowStyle.BLOCK);
            Yaml yaml = new Yaml(options);
            yaml.dump(enemiesMap, fw);
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }

    private void importSectors(File f) {
        InputStream input;
        try {
            input = new FileInputStream(f);
            Yaml yaml = new Yaml();
            Map<Integer, Map<String, Object>> sectorsMap = (Map<Integer, Map<String, Object>>) yaml
                    .load(input);

            int y, x;
            Sector sec;
            for (Map.Entry<Integer, Map<String, Object>> entry : sectorsMap
                    .entrySet()) {
                y = entry.getKey() / WIDTH_IN_SECTORS;
                x = entry.getKey() % WIDTH_IN_SECTORS;
                sec = sectors[y][x];
                sec.tileset = (Integer) (entry.getValue().get("Tileset"));
                sec.palette = (Integer) (entry.getValue().get("Palette"));
                sec.music = (Integer) (entry.getValue().get("Music"));
                sec.item = (Integer) (entry.getValue().get("Item"));
                sec.teleport = (String) (entry.getValue().get("Teleport"));
                sec.townmap = (String) (entry.getValue().get("Town Map"));
                sec.setting = (String) (entry.getValue().get("Setting"));
                sec.townmapImage = (String) (entry.getValue()
                        .get("Town Map Image"));
                sec.townmapArrow = (String) (entry.getValue()
                        .get("Town Map Arrow"));
                sec.townmapX = (Integer) (entry.getValue()
                        .get("Town Map X"));
                sec.townmapY = (Integer) (entry.getValue()
                        .get("Town Map Y"));
            }
        } catch (FileNotFoundException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }

    private void exportSectors(File f) {
        Map<Integer, Map<String, Object>> sectorsMap = new HashMap<Integer, Map<String, Object>>();
        int i = 0;
        for (Sector[] row : sectors) {
            for (Sector s : row) {
                Map<String, Object> entry = new HashMap<String, Object>();
                entry.put("Item", s.item);
                entry.put("Music", s.music);
                entry.put("Palette", s.palette);
                entry.put("Setting", s.setting);
                entry.put("Teleport", s.teleport);
                entry.put("Tileset", s.tileset);
                entry.put("Town Map", s.townmap);
                entry.put("Town Map Image", s.townmapImage);
                entry.put("Town Map Arrow", s.townmapArrow);
                entry.put("Town Map X", s.townmapX);
                entry.put("Town Map Y", s.townmapY);
                sectorsMap.put(i, entry);
                ++i;
            }
        }

        try {
            FileWriter fw = new FileWriter(f);
            DumperOptions options = new DumperOptions();
            options.setDefaultFlowStyle(DumperOptions.FlowStyle.BLOCK);
            Yaml yaml = new Yaml(options);
            yaml.dump(sectorsMap, fw);
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }

    private void setMapTilesFromStream(InputStream in) {
        String tmp;
        try {
            for (int i = 0; i < mapTiles.length; i++) {
                for (int j = 0; j < mapTiles[i].length; j++) {
                    tmp = "" + ((char) in.read());
                    tmp += (char) in.read();
                    tmp += (char) in.read();
                    mapTiles[i][j] = Integer.parseInt(tmp, 16);
                    in.read(); // " " or "\n"
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void writeMapTilesToStream(FileOutputStream out) {
        try {
            String tmp;
            for (int i = 0; i < mapTiles.length; i++) {
                for (int j = 0; j < mapTiles[i].length; j++) {
                    tmp = ToolModule.addZeros(
                            Integer.toHexString(mapTiles[i][j]), 3);
                    out.write(tmp.charAt(0));
                    out.write(tmp.charAt(1));
                    out.write(tmp.charAt(2));
                    if (j != mapTiles[i].length - 1)
                        out.write(' ');
                }
                out.write('\n');
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public int getMapTile(int x, int y) {
        return mapTiles[y][x];
    }

    public void setMapTile(int x, int y, int tile) {
        mapTiles[y][x] = tile;
    }

    public static class Sector {
        public static final String[] TOWN_MAP_NAMES = new String[]{
                "none", "onett", "twoson", "threed", "fourside", "scaraba",
                "summers", "none 2"};
        public static final String[] SETTING_NAMES = new String[]{"none",
                "indoors", "exit mouse usable", "lost underworld sprites",
                "magicant sprites", "robot sprites", "butterflies",
                "indoors and butterflies"};
        public static final String[] TOWN_MAP_IMAGES = new String[]{
                "none", "onett", "twoson", "threed", "fourside", "scaraba",
                "summers"};
        public static final String[] TOWN_MAP_ARROWS = new String[]{
                "none", "up", "down", "right", "left"};

        public int tileset = 0, palette = 0, music = 0, item, townmapX,
                townmapY;
        public String townmap, setting, teleport, townmapImage,
                townmapArrow;

        public void reset() {
            tileset = 0;
            palette = 0;
            music = 0;
            item = 0;
            townmap = "none";
            setting = "none";
            teleport = "disabled";
            townmapX = 0;
            townmapY = 0;
            townmapImage = "none";
            townmapArrow = "none";
        }

        public void copy(Sector other) {
            try {
                this.tileset = other.tileset;
                this.palette = other.palette;
                this.music = other.music;
                this.item = other.item;
                this.townmap = other.townmap;
                this.setting = other.setting;
                this.teleport = other.teleport;
                this.townmapX = other.townmapX;
                this.townmapY = other.townmapY;
                this.townmapImage = other.townmapImage;
                this.townmapArrow = other.townmapArrow;
            } catch (Exception e) {

            }
        }

        private static int indexOf(Object[] arr, Object target) {
            int i = 0;
            for (Object e : arr) {
                if (e.equals(target))
                    return i;
                ++i;
            }
            return -1;
        }

        public int getTownMapNum() {
            return indexOf(TOWN_MAP_NAMES, this.townmap);
        }

        public void setTownMapNum(int i) {
            this.townmap = TOWN_MAP_NAMES[i];
        }

        public int getSettingNum() {
            return indexOf(SETTING_NAMES, this.setting);
        }

        public void setSettingNum(int i) {
            this.setting = SETTING_NAMES[i];
        }

        public boolean isTeleportEnabled() {
            return this.teleport.equals("enabled");
        }

        public void setTeleportEnabled(boolean b) {
            if (b) {
                this.teleport = "enabled";
            } else {
                this.teleport = "disabled";
            }
        }

        public int getTownMapImageNum() {
            return indexOf(TOWN_MAP_IMAGES, this.townmapImage);
        }

        public void setTownMapImageNum(int i) {
            this.townmapImage = TOWN_MAP_IMAGES[i];
        }

        public int getTownMapArrowNum() {
            return indexOf(TOWN_MAP_ARROWS, this.townmapArrow);
        }

        public void setTownMapArrowNum(int i) {
            this.townmapArrow = TOWN_MAP_ARROWS[i];
        }
    }

    public static class SpriteEntry {
        public int x, y, npcID;

        public SpriteEntry(int x, int y, int npcID) {
            this.x = x;
            this.y = y;
            this.npcID = npcID;
        }
    }

    // Only store the info we need
    public static class NPC {
        public int sprite, direction;

        public NPC(int sprite, String direction) {
            this.sprite = sprite;
            direction = direction.toLowerCase();
            if (direction.equals("up"))
                this.direction = 0;
            else if (direction.equals("right"))
                this.direction = 1;
            else if (direction.equals("down"))
                this.direction = 2;
            else
                this.direction = 3;
        }
    }

    public void nullSpriteData() {
        for (java.util.List<SpriteEntry>[] row : spriteAreas) {
            for (java.util.List<SpriteEntry> area : row) {
                area.clear();
            }
        }
    }

    public void nullMapData() {
        for (int i = 0; i < mapTiles.length; i++) {
            for (int j = 0; j < mapTiles[i].length; j++) {
                mapTiles[i][j] = 0;
            }
        }
        for (Sector[] row : sectors) {
            for (Sector s : row) {
                s.reset();
            }
        }
    }

    public void nullEnemyData() {
        for (int i = 0; i < enemyPlacement.length; ++i)
            for (int j = 0; j < enemyPlacement[i].length; ++j)
                enemyPlacement[i][j] = 0;
    }

    public void nullDoorData() {
        for (java.util.List<Door>[] row : doorAreas) {
            for (List<Door> area : row) {
                area.clear();
            }
        }
    }
}
