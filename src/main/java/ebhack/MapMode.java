package ebhack;

public enum MapMode {
    MAP, // 0
    SPRITE, // 1
    DOOR, // 2
    // Mode 3 doesn't exist
    SEEK_DOOR, // 4
    // Mode 5 doesn't exist
    HOTSPOT, // 6
    ENEMY, // 7
    VIEW_ALL, // 8
    PREVIEW, // 9
    TELEPORT; // 10

    public boolean drawSprites() {
        return this == SPRITE || this == SEEK_DOOR
                || this == VIEW_ALL || this == PREVIEW;
    }

    public boolean drawDoors() {
        return this == DOOR || this == SEEK_DOOR || this == VIEW_ALL;
    }

    public boolean drawEnemies() {
        return this == ENEMY || this == VIEW_ALL;
    }
    public boolean drawHotspots() {
        return this == HOTSPOT || this == VIEW_ALL;
    }
    public boolean drawTeleports() { return this == TELEPORT || this == VIEW_ALL; }
}