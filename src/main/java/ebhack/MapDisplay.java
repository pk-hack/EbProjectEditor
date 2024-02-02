package ebhack;

import ebhack.types.*;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.awt.geom.AffineTransform;
import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;
import java.util.Stack;

public class MapDisplay extends AbstractButton implements
        ActionListener, MouseListener, MouseMotionListener {
    private static final double MIN_ZOOM_TO_DISPLAY_ENEMIES = 0.5;
    private static final int ENEMY_ROW_1_Y = 32;
    private static final int ENEMY_ROW_2_Y = 48;
    private YMLPreferences prefs;
    private MapData map;
    private JMenuItem copySector, pasteSector, copySector2, pasteSector2,
            undoButton, redoButton;

    private final ActionEvent sectorEvent = new ActionEvent(this,
            ActionEvent.ACTION_PERFORMED, "sectorChanged");

    private static Image[][][] tileImageCache;

    private class UndoableTileChange {
        public int x, y, oldTile, newTile;

        public UndoableTileChange(int x, int y, int oldTile, int newTile) {
            this.x = x;
            this.y = y;
            this.oldTile = oldTile;
            this.newTile = newTile;
        }
    }

    private class UndoableSectorPaste {
        public int sectorX, sectorY;
        private int[][] tiles;
        private MapData.Sector sector;

        public UndoableSectorPaste(int sectorX, int sectorY, int[][] tiles,
                                   MapData.Sector sector) {
            this.sectorX = sectorX;
            this.sectorY = sectorY;
            this.tiles = tiles;
            this.sector = sector;
        }

    }

    private Stack<Object> undoStack = new Stack<Object>();
    private Stack<Object> redoStack = new Stack<Object>();

    private int screenWidth = 24;
    private int screenHeight = 12;

    // Map X and Y coordinates of the tile displayed in the top left corner
    private int screenX = 0, screenY = 0;
    // Pixel coordinates of top map X and Y
    private double scrollX = 0, scrollY = 0;
    // Zoom!
    private double zoom = 1.0;

    // Recent mouse coords for dragging
    private int mouseDragButton = -1;
    private int lastMouseX = 0, lastMouseY = 0;

    // Data for the selected sector
    private MapData.Sector selectedSector = null;
    private int sectorX, sectorY;
    private int sectorPal;
    private boolean grid = true;
    private boolean spriteBoxes = true;

    // Moving stuff
    private int movingDrawX, movingDrawY;
    private int movingNPC = -1;
    private Image movingNPCimg;
    private Integer[] movingNPCdim;
    private MapData.Door movingDoor = null;
    private TileObject movingTeleport = null;

    // Popup menus
    private int popupX, popupY;
    private JPopupMenu spritePopupMenu, doorPopupMenu;
    private JMenuItem detailsNPC, delNPC, cutNPC, copyNPC, switchNPC, moveNPC;
    private int copiedNPC = 0;
    private MapData.SpriteEntry popupSE;
    private JMenuItem detailsDoor, delDoor, cutDoor, copyDoor, editDoor,
            jumpDoor;
    private MapData.Door popupDoor, copiedDoor;

    // Seeking stuff
    private int seekDrawX, seekDrawY;
    private DoorEditor doorSeeker;

    // Editing hotspot
    private MapData.Hotspot editHS = null;
    private int editHSx1, editHSy1;
    private int hsMouseX, hsMouseY;

    // Mode settings
    private MapMode currentMode = MapMode.MAP;
    private MapMode previousMode = null;
    private boolean drawTileNums = false;
    private boolean drawSpriteNums = true;
    private boolean enemyBattleSprites = false;
    private boolean gamePreview = false;
    private boolean tvPreview = false;
    private int tvPreviewX, tvPreviewY, tvPreviewW, tvPreviewH;

    // Coordinate labels
    private JLabel pixelCoordLabel, warpCoordLabel, tileCoordLabel;

    // Cache enemy colors
    public static Color[] enemyColors = null;

    private MapTileSelector tileSelector;

    public MapDisplay(MapData map, JMenuItem copySector,
                      JMenuItem pasteSector, JMenuItem copySector2,
                      JMenuItem pasteSector2, JMenuItem undoButton,
                      JMenuItem redoButton, JLabel pixelCoordLabel,
                      JLabel warpCoordLabel, JLabel tileCoordLabel,
                      YMLPreferences prefs) {
        super();

        if (enemyColors == null) {
            enemyColors = new Color[203];
            for (int i = 0; i < 203; ++i)
                enemyColors[i] = new Color(
                        ((int) (Math.E * 0x100000 * i)) & 0x7f7f7f7f);
        }

        this.prefs = prefs;

        this.map = map;
        this.copySector = copySector;
        this.pasteSector = pasteSector;
        this.copySector2 = copySector2;
        this.pasteSector2 = pasteSector2;
        this.undoButton = undoButton;
        this.redoButton = redoButton;
        this.pixelCoordLabel = pixelCoordLabel;
        this.warpCoordLabel = warpCoordLabel;
        this.tileCoordLabel = tileCoordLabel;

        if (tileImageCache == null)
            resetTileImageCache();

        // Create Sprite popup menu
        spritePopupMenu = new JPopupMenu();
        spritePopupMenu.add(detailsNPC = ToolModule.createJMenuItem(
                "Sprite @ ", ' ', null, null, this));
        detailsNPC.setEnabled(false);
        spritePopupMenu.add(ToolModule.createJMenuItem("New NPC", 'n',
                null, "newNPC", this));
        spritePopupMenu.add(delNPC = ToolModule.createJMenuItem(
                "Delete NPC", 'd', null, "delNPC", this));
        spritePopupMenu.add(cutNPC = ToolModule.createJMenuItem("Cut NPC",
                'c', null, "cutNPC", this));
        spritePopupMenu.add(copyNPC = ToolModule.createJMenuItem(
                "Copy NPC", 'y', null, "copyNPC", this));
        spritePopupMenu.add(ToolModule.createJMenuItem("Paste NPC", 'p',
                null, "pasteNPC", this));
        spritePopupMenu.add(switchNPC = ToolModule.createJMenuItem(
                "Switch NPC", 's', null, "switchNPC", this));
        spritePopupMenu.add(moveNPC = ToolModule.createJMenuItem(
                "Move NPC", 'm', null, "moveNPC", this));

        // Create Door popup menu
        doorPopupMenu = new JPopupMenu();
        doorPopupMenu.add(detailsDoor = ToolModule.createJMenuItem(
                "Door @ ", ' ', null, null, this));
        detailsDoor.setEnabled(false);
        doorPopupMenu.add(ToolModule.createJMenuItem("New Door", 'n', null,
                "newDoor", this));
        doorPopupMenu.add(delDoor = ToolModule.createJMenuItem(
                "Delete Door", 'd', null, "delDoor", this));
        doorPopupMenu.add(cutDoor = ToolModule.createJMenuItem("Cut Door",
                'c', null, "cutDoor", this));
        doorPopupMenu.add(copyDoor = ToolModule.createJMenuItem(
                "Copy Door", 'y', null, "copyDoor", this));
        doorPopupMenu.add(ToolModule.createJMenuItem("Paste Door", 'p',
                null, "pasteDoor", this));
        doorPopupMenu.add(editDoor = ToolModule.createJMenuItem(
                "Edit Door", 'e', null, "editDoor", this));
        doorPopupMenu.add(jumpDoor = ToolModule.createJMenuItem(
                "Jump to Destination", 'j', null, "jumpDoor", this));

        addMouseListener(this);
        addMouseMotionListener(this);

        setPreferredSize(new Dimension(
                screenWidth * MapData.TILE_WIDTH + 2, screenHeight
                * MapData.TILE_HEIGHT + 2));
    }

    public void init() {
        selectSector(0, 0);
        changeMode(MapMode.MAP);
        reset();
    }

    public void reset() {
        undoStack.clear();
        undoButton.setEnabled(false);
        redoStack.clear();
        redoButton.setEnabled(false);
    }

    public void setTileSelector(MapTileSelector tileSelector) {
        this.tileSelector = tileSelector;
    }

    public void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2d = (Graphics2D) g;
        if (isEnabled())
            drawMap(g2d);
        else {
            // Draw border
            g2d.setColor(Color.black);
            g2d.draw(new Rectangle2D.Double(0, 0, screenWidth
                    * MapData.TILE_WIDTH + 2, screenHeight
                    * MapData.TILE_HEIGHT + 2));
        }
    }

    private void drawMap(Graphics2D g) {
        g.setPaint(Color.white);
        g.setFont(new Font("Arial", Font.PLAIN, 12));

        // Translate the screen for smooth scrolling. This entire codebase is built on tile coordinates, so rather than
        // trying to swap it over, just compare the results and translate that much.
        g.translate(-(scrollX - MapData.TILE_WIDTH * screenX) * zoom,
                -(scrollY - MapData.TILE_HEIGHT * screenY) * zoom);
        // Zoom zoom
        g.scale(zoom, zoom);

        MapData.Sector sector;
        int pal;
        for (int iy = 0; iy < screenHeight; iy++) {
            for (int ix = 0; ix < screenWidth; ix++) {
                sector = map.getSector((ix + screenX) / MapData.SECTOR_WIDTH,
                        (iy + screenY) / MapData.SECTOR_HEIGHT);
                pal = TileEditor.tilesets[TileEditor
                        .getDrawTilesetNumber(sector.tileset)]
                        .getPaletteNum(sector.tileset, sector.palette);
                g.drawImage(
                        getTileImage(TileEditor
                                .getDrawTilesetNumber(sector.tileset), map
                                .getMapTile(screenX + ix, screenY + iy), pal), ix
                                * MapData.TILE_WIDTH + 1, iy
                                * MapData.TILE_HEIGHT + 1,
                        MapData.TILE_WIDTH, MapData.TILE_HEIGHT, this);
                if (drawTileNums && !gamePreview) {
                    drawNumber(g, map.getMapTile(screenX + ix, screenY + iy), ix
                            * MapData.TILE_WIDTH + 1, iy
                            * MapData.TILE_HEIGHT + 1, false, false);
                }
            }
        }

        if (grid && !gamePreview && !currentMode.drawEnemies())
            drawGrid(g);

        if (currentMode == MapMode.MAP && (selectedSector != null)) {
            int sXt, sYt;
            if (((sXt = sectorX * MapData.SECTOR_WIDTH)
                    + MapData.SECTOR_WIDTH >= screenX)
                    && (sXt < screenX + screenWidth)
                    && ((sYt = sectorY * MapData.SECTOR_HEIGHT)
                    + MapData.SECTOR_HEIGHT >= screenY)
                    && (sYt < screenY + screenHeight)) {
                g.setPaint(Color.yellow);
                g.draw(new Rectangle2D.Double((sXt - screenX)
                        * MapData.TILE_WIDTH + 1, (sYt - screenY)
                        * MapData.TILE_HEIGHT + 1, MapData.SECTOR_WIDTH
                        * MapData.TILE_WIDTH, MapData.SECTOR_HEIGHT
                        * MapData.TILE_HEIGHT));
            }
        }

        // Draw border
        g.setColor(Color.black);
        g.draw(new Rectangle2D.Double(0, 0, screenWidth
                * MapData.TILE_WIDTH + 2, screenHeight
                * MapData.TILE_HEIGHT + 2));

        if (currentMode.drawSprites()) {
            MapData.NPC npc;
            Integer[] wh;
            for (int iy = screenY & (~7); iy < (screenY & (~7)) + screenHeight + 8; iy += 8) {
                for (int ix = screenX & (~7); ix < (screenX & (~7)) + screenWidth + 8; ix += 8) {
                    for (MapData.SpriteEntry e : map.getSpriteArea(ix >> 3, iy >> 3)) {
                        npc = map.getNPC(e.npcID);
                        wh = map.getSpriteWH(npc.sprite);
                        if (spriteBoxes && !gamePreview) {
                            g.setPaint(Color.RED);
                            g.draw(new Rectangle2D.Double(e.x + (ix - screenX)
                                    * MapData.TILE_WIDTH - wh[0] / 2,
                                    e.y + (iy - screenY) * MapData.TILE_HEIGHT
                                            - wh[1] + 8, wh[0] + 1,
                                    wh[1] + 1));
                        }
                        g.drawImage(map.getSpriteImage(npc.sprite,
                                        npc.direction), e.x + (ix - screenX)
                                        * MapData.TILE_WIDTH - wh[0] / 2 + 1,
                                e.y + (iy - screenY) * MapData.TILE_HEIGHT
                                        - wh[1] + 9, this);
                        if (drawSpriteNums && !gamePreview) {
                            drawNumber(g, e.npcID, e.x + (ix - screenX)
                                            * MapData.TILE_WIDTH - wh[0] / 2,
                                    e.y + (iy - screenY) * MapData.TILE_HEIGHT
                                            - wh[1] + 8, false, true);
                        }
                    }
                }
            }

            if (currentMode == MapMode.SPRITE && (movingNPC != -1)) {
                if (spriteBoxes) {
                    g.setPaint(Color.RED);
                    g.draw(new Rectangle2D.Double(movingDrawX - 1,
                            movingDrawY - 1, movingNPCdim[0] + 1,
                            movingNPCdim[1] + 1));
                }
                g.drawImage(movingNPCimg, movingDrawX, movingDrawY, this);
            }
        }

        if (currentMode.drawDoors()) {
            for (int iy = screenY & (~7); iy < (screenY & (~7)) + screenHeight + 8; iy += 8) {
                for (int ix = screenX & (~7); ix < (screenX & (~7)) + screenWidth + 8; ix += 8) {
                    for (MapData.Door e : map.getDoorArea(ix >> 3, iy >> 3)) {
                        g.setPaint(e.getColor());
                        g.draw(new Rectangle2D.Double(e.x * 8 + (ix - screenX)
                                * MapData.TILE_WIDTH + 1, e.y * 8
                                + (iy - screenY) * MapData.TILE_HEIGHT + 1, 8,
                                8));
                        g.draw(new Rectangle2D.Double(e.x * 8 + (ix - screenX)
                                * MapData.TILE_WIDTH + 3, e.y * 8
                                + (iy - screenY) * MapData.TILE_HEIGHT + 3, 4,
                                4));
                        g.setPaint(Color.WHITE);
                        g.draw(new Rectangle2D.Double(e.x * 8 + (ix - screenX)
                                * MapData.TILE_WIDTH + 2, e.y * 8
                                + (iy - screenY) * MapData.TILE_HEIGHT + 2, 6,
                                6));
                        g.draw(new Rectangle2D.Double(e.x * 8 + (ix - screenX)
                                * MapData.TILE_WIDTH + 4, e.y * 8
                                + (iy - screenY) * MapData.TILE_HEIGHT + 4, 2,
                                2));
                    }
                }
            }

            if (currentMode == MapMode.DOOR && (movingDoor != null)) {
                g.setPaint(movingDoor.getColor());
                g.draw(new Rectangle2D.Double(movingDrawX + 1,
                        movingDrawY + 1, 8, 8));
                g.draw(new Rectangle2D.Double(movingDrawX + 3,
                        movingDrawY + 3, 4, 4));
                g.setPaint(Color.WHITE);
                g.draw(new Rectangle2D.Double(movingDrawX + 2,
                        movingDrawY + 2, 6, 6));
                g.draw(new Rectangle2D.Double(movingDrawX + 4,
                        movingDrawY + 4, 2, 2));
            }

            if (currentMode == MapMode.SEEK_DOOR) {
                g.setPaint(Color.WHITE);
                g.draw(new Rectangle2D.Double(seekDrawX + 1, seekDrawY + 1,
                        8, 8));
                g.draw(new Rectangle2D.Double(seekDrawX + 3, seekDrawY + 3,
                        4, 4));

                g.setPaint(new Color(57, 106, 177));
                g.draw(new Rectangle2D.Double(seekDrawX + 2, seekDrawY + 2,
                        6, 6));
                g.draw(new Rectangle2D.Double(seekDrawX + 4, seekDrawY + 4,
                        2, 2));
            }
        }

        if (currentMode.drawEnemies()) {
            g.setFont(new Font("Arial", Font.PLAIN, 12));
            for (int iy = -(screenY % 2); iy < screenHeight; iy += 2) {
                for (int ix = -(screenX % 2); ix < screenWidth; ix += 2) {
                    // Draw the grid
                    Rectangle2D rect = new Rectangle2D.Double(ix
                            * MapData.TILE_WIDTH + 1, iy
                            * MapData.TILE_HEIGHT + 1,
                            MapData.TILE_WIDTH * 2, MapData.TILE_HEIGHT * 2);
                    if (grid && !gamePreview) {
                        g.setColor(Color.BLACK);
                        g.draw(rect);
                    }

                    int enemyGroup = map.getMapEnemyGroup((screenX + ix) / 2, (screenY + iy) / 2);
                    if (enemyGroup != 0) {
                        drawEnemyPlate(g, rect, enemyGroup);
                    }
                }
            }
        }

        if (currentMode.drawHotspots()) {
            MapData.Hotspot hs;
            g.setComposite(AlphaComposite.getInstance(
                    AlphaComposite.SRC_OVER, 0.8F));
            int tx1, ty1, tx2, ty2;
            for (int i = 0; i < map.numHotspots(); ++i) {
                hs = map.getHotspot(i);
                if (hs == editHS)
                    continue;
                tx1 = hs.x1 / 4 - screenX;
                ty1 = hs.y1 / 4 - screenY;
                tx2 = hs.x2 / 4 - screenX;
                ty2 = hs.y2 / 4 - screenY;
                if (((tx1 >= 0) && (tx1 <= screenWidth) && (ty1 >= 0) && (ty1 <= screenHeight))
                        || ((tx2 >= 0) && (tx2 <= screenWidth)
                        && (ty2 >= 0) && (ty2 <= screenHeight))) {
                    g.setPaint(Color.PINK);
                    g.fill(new Rectangle2D.Double(hs.x1 * 8 - screenX
                            * MapData.TILE_WIDTH + 1, hs.y1 * 8 - screenY
                            * MapData.TILE_HEIGHT + 1, (hs.x2 - hs.x1) * 8,
                            (hs.y2 - hs.y1) * 8));

                    drawNumber(g, i,
                            hs.x1 * 8 - screenX * MapData.TILE_WIDTH + 1, hs.y1
                                    * 8 - screenY * MapData.TILE_HEIGHT + 1,
                            false, false);
                }
            }

            if (currentMode == MapMode.HOTSPOT && (editHS != null)) {
                g.setPaint(Color.WHITE);
                if (editHSx1 != -1) {
                    tx1 = editHSx1 * 8 - screenX * MapData.TILE_WIDTH + 1;
                    ty1 = editHSy1 * 8 - screenY * MapData.TILE_HEIGHT + 1;
                    g.fill(new Rectangle2D.Double(tx1, ty1, hsMouseX - tx1,
                            hsMouseY - ty1));
                } else {
                    g.fill(new Rectangle2D.Double(hsMouseX + 1,
                            hsMouseY + 1, 65, 65));
                }
            }
        }

        if (currentMode.drawTeleports()) {
            drawTeleports(g);
        }

        if (gamePreview && tvPreview) {
            g.setComposite(AlphaComposite.getInstance(
                    AlphaComposite.SRC_OVER, 1.0F));
            g.setColor(Color.BLACK);
            g.fillRect(0, 0, tvPreviewX - tvPreviewW, screenHeight
                    * MapData.TILE_HEIGHT);
            g.fillRect(tvPreviewX + tvPreviewW, 0,
                    (screenWidth * MapData.TILE_WIDTH) - tvPreviewX
                            - tvPreviewW, screenHeight
                            * MapData.TILE_HEIGHT);
            g.fillRect(0, 0, screenWidth * MapData.TILE_WIDTH, tvPreviewY
                    - tvPreviewH);
            g.fillRect(0, tvPreviewY + tvPreviewH, screenWidth
                            * MapData.TILE_WIDTH,
                    (screenHeight * MapData.TILE_HEIGHT) - tvPreviewY
                            - tvPreviewH);

            // hardcoded for sprite of size 16,24
            g.drawImage(map.getSpriteImage(1, 2), tvPreviewX - 7,
                    tvPreviewY - 15, this);
        }
    }

    public void drawEnemyPlate(Graphics2D g, Rectangle2D rect, int mapEnemyGroupId) {
        MapEnemyGroup mapEnemyGroup = map.getMapEnemyGroup(mapEnemyGroupId);
        // Draw background
        g.setComposite(AlphaComposite.getInstance(
                AlphaComposite.SRC_OVER, 0.75F));
        g.setPaint(enemyColors[mapEnemyGroupId]);
        g.fill(rect);
        g.setComposite(AlphaComposite.getInstance(
                AlphaComposite.SRC_OVER, 1.0F));

        // Draw the enemies in a clip region so it's okay to overflow
        if (zoom >= MIN_ZOOM_TO_DISPLAY_ENEMIES) {
            Rectangle2D originalClip = (Rectangle2D) g.getClip();
            g.setClip(originalClip.createIntersection(rect));
            // Start a group
            if (!mapEnemyGroup.subGroup1.isEmpty() || mapEnemyGroup.subGroup1Rate > 0) {
                int x = 0;
                int y = (int) (ENEMY_ROW_1_Y * enemySpriteZoom());
                for (MapEnemyGroup.SpawnGroup spawnGroup : mapEnemyGroup.subGroup1) {
                    x = 4 + drawEnemyGroup(g, rect, new Point(x, y), spawnGroup.probability, spawnGroup.enemyGroup);
                }
                String spawnRate = mapEnemyGroup.subGroup1Rate + "%";
                Rectangle2D textBG = g.getFontMetrics().getStringBounds(spawnRate, g);
                g.setPaint(Color.white);
                g.drawString(spawnRate, (int) (rect.getMaxX() - textBG.getWidth()), (int) rect.getY() + 32);
            }
            // Start a group
            if (!mapEnemyGroup.subGroup2.isEmpty() || mapEnemyGroup.subGroup2Rate > 0) {
                int x = 0;
                int y = (int) (ENEMY_ROW_2_Y * enemySpriteZoom());
                for (MapEnemyGroup.SpawnGroup spawnGroup : mapEnemyGroup.subGroup2) {
                    x = 4 + drawEnemyGroup(g, rect, new Point(x, y), spawnGroup.probability, spawnGroup.enemyGroup);
                }
                String spawnRate = mapEnemyGroup.subGroup2Rate + "%";
                Rectangle2D textBG = g.getFontMetrics().getStringBounds(spawnRate, g);
                g.setPaint(Color.white);
                g.drawString(spawnRate, (int) (rect.getMaxX() - textBG.getWidth()), (int) rect.getY() + 48);
            }
            g.setClip(originalClip);
        }

        // Draw labels
        drawNumber(g, mapEnemyGroupId, (int) rect.getX(), (int) rect.getY(), false, false);
    }

    /**
     * Draws one enemy group. Bounds is the plate, origin is their coordinates (zoomed) within the plate.
     * Returns the x coordinate of the furthest right thing it drew (to space the next thing)
     */
    public int drawEnemyGroup(Graphics2D g, Rectangle2D bounds, Point origin, int probability, int enemyGroupId) {
        EnemyGroup enemyGroup = map.getEnemyGroup(enemyGroupId);
        if (enemyGroup == null)
	        return 0;
        int maxX = origin.x;
        AffineTransform stashed = g.getTransform();
        g.translate(bounds.getX(), bounds.getY());
        g.scale(1 / enemySpriteZoom(), 1 / enemySpriteZoom());
        int spacingRight = enemyBattleSprites ? 8 : 4;
        int spacingDown = enemyBattleSprites ? 16 : 8;
        int count = 0;
        for (EnemyGroup.EnemyCount enemy : enemyGroup.enemies) {
            Image image = map.getEnemySprite(enemy.enemy, enemyBattleSprites);
            if (image == null) {
                continue;
            }
            for (int i = 0; i < enemy.amount; i++) {
                int ex = origin.x + count * spacingRight;
                int ey = origin.y + count * spacingDown;
                g.drawImage(image,
                        ex, ey - image.getHeight(null),
                        null);
                maxX = Math.max(ex + image.getWidth(null), maxX);
                count++;
            }
        }
        if (zoom > 1) {
            String groupPercent = probability + "/8";
            int stringWidth = (int) g.getFontMetrics()
                    .getStringBounds(groupPercent, g).getWidth();
            int stringX = origin.x + Math.max(0, (maxX - stringWidth - origin.x) / 2);
            g.setPaint(Color.white);
            g.drawString(groupPercent, stringX, origin.y + count * spacingDown);
            maxX = Math.max(maxX, origin.x + stringWidth);
        }

        g.setTransform(stashed);
        return maxX;
    }

    private double enemySpriteZoom() {
        double maxZoom = enemyBattleSprites ? 7 : 4;;
        return Math.min(zoom, maxZoom);
    }

    private void drawTeleports(Graphics2D g) {
        int translateX = -screenX * MapData.TILE_WIDTH;
        int translateY = -screenY * MapData.TILE_WIDTH;
        for (TeleportDestination dest : map.getTeleportDestinations()) {
            int x = dest.x * 8 + translateX;
            int y = dest.y * 8 + translateY;
            // Draw a label for the teleport number
            String label = Integer.toString(dest.id);
            Rectangle2D textBG = g.getFontMetrics().getStringBounds(label, g);
            g.setPaint(new Color(3, 13, 42));
            g.fillRect(x + 8, y, (int) textBG.getWidth(), (int) textBG.getHeight());
            g.setColor(Color.white);
            g.drawString(label, x + 8, (int) (y + textBG.getHeight()));
            // Draw some sort of teleport icon
            g.setPaint(new Color(3, 13, 42));
            g.fillRect(x, y, 9, 9);
            for (int ring = 0; ring < 4; ring+=2) {
                g.setPaint(new Color(70, 113, 220));
                g.fillOval(x + ring, y + ring, 9 - ring * 2, 9 - ring * 2);
                g.setPaint(new Color(29, 54, 89));
                g.fillOval(x + ring + 1, y + ring + 1, 7 - ring * 2, 7 - ring * 2);
            }
        }
        Image teleportImage = map.getSpriteImage(13, 2);
        for (PsiTeleportDestination dest : map.getPsiTeleportDestinations()) {
            int x = dest.x * 8 + translateX;
            int y = dest.y * 8 + translateY;
            // Draw some sort of teleport icon
            g.drawImage(teleportImage, x, y, null);
            // Draw a label for the teleport number
            x += teleportImage.getWidth(null);
            String label = dest.id + " - " + dest.name;
            Rectangle2D textBG = g.getFontMetrics().getStringBounds(label, g);
            g.setPaint(new Color(3, 13, 42));
            g.fillRect(x, y, (int) textBG.getWidth(), (int) textBG.getHeight());
            g.setColor(Color.white);
            g.drawString(label, x, (int) (y + textBG.getHeight()));
        }
    }

    private void drawNumber(Graphics2D g, int n, int x, int y, boolean hex,
                            boolean above) {
        String s;
        if (hex)
            s = ToolModule.addZeros(Integer.toHexString(n), 4);
        else
            s = ToolModule.addZeros(Integer.toString(n), 4);

        Rectangle2D textBG = g.getFontMetrics().getStringBounds(s, g);

        g.setPaint(Color.black);
        if (above) {
            textBG.setRect(x, y - textBG.getHeight(), textBG.getWidth(),
                    textBG.getHeight());
            g.fill(textBG);
            g.setPaint(Color.white);
            g.drawString(s, x, y);
        } else {
            textBG.setRect(x, y, textBG.getWidth(), textBG.getHeight());
            g.fill(textBG);
            g.setPaint(Color.white);
            g.drawString(s, x, y + ((int) textBG.getHeight()));
        }
    }

    private void drawGrid(Graphics2D g) {
        g.setPaint(Color.black);
        // Draw vertical lines
        for (int i = 0; i < screenWidth + 1; i++)
            g.drawLine(1 + i * MapData.TILE_WIDTH, 1, 1 + i
                    * MapData.TILE_WIDTH, screenHeight
                    * MapData.TILE_HEIGHT);
        // Draw horizontal lines
        for (int i = 0; i < screenHeight + 1; i++)
            g.drawLine(1, 1 + i * MapData.TILE_HEIGHT, screenWidth
                    * MapData.TILE_WIDTH, 1 + i * MapData.TILE_HEIGHT);

        // Blank pixel in the bottom right corner
        g.drawLine(screenWidth * MapData.TILE_WIDTH + 1, screenHeight
                * MapData.TILE_HEIGHT + 1, screenWidth * MapData.TILE_WIDTH
                + 1, screenHeight * MapData.TILE_HEIGHT + 1);
    }

    public static Image getTileImage(int loadtset, int loadtile,
                                     int loadpalette) {
        if (tileImageCache[loadtset][loadtile][loadpalette] == null) {
            try {
                tileImageCache[loadtset][loadtile][loadpalette] = TileEditor.tilesets[loadtset]
                        .getArrangementImage(loadtile, loadpalette);
            } catch (IndexOutOfBoundsException ioobe) {
                System.err.println("Invalid tset/tile/pal: " + loadtset
                        + "/" + loadtile + "/" + loadpalette);
                ioobe.printStackTrace();
            }
        }
        return tileImageCache[loadtset][loadtile][loadpalette];
    }

    public static void resetTileImageCache() {
        tileImageCache = new Image[TileEditor.NUM_TILESETS][1024][59];
    }

    public int getScreenWidth() {
        return screenWidth;
    }

    public int getScreenHeight() {
        return screenHeight;
    }

    public MapData.Sector getSelectedSector() {
        return selectedSector;
    }

    public void setSelectedSectorTileset(int tset) {
        selectedSector.tileset = tset;
        sectorPal = TileEditor.tilesets[TileEditor
                .getDrawTilesetNumber(selectedSector.tileset)]
                .getPaletteNum(selectedSector.tileset,
                        selectedSector.palette);
    }

    public void setSelectedSectorPalette(int pal) {
        selectedSector.palette = pal;
        sectorPal = TileEditor.tilesets[TileEditor
                .getDrawTilesetNumber(selectedSector.tileset)]
                .getPaletteNum(selectedSector.tileset,
                        selectedSector.palette);
    }

    public int getSelectedSectorPalNumber() {
        return sectorPal;
    }

    public int getMinScrollX() {
        // Half the screen to the left of the map
        return (int) (-getSize().width / (2 * zoom));
    }

    public int getMaxScrollX() {
        // Half the screen to the right of the map
        int mapSize = MapData.WIDTH_IN_TILES * MapData.TILE_WIDTH;
        int screenSize = (int) (getSize().width / (2 * zoom));
        return mapSize - screenSize;
    }

    public int getMinScrollY() {
        // Half the screen above the map
        return (int) (-getSize().height / (2 * zoom));
    }

    public int getMaxScrollY() {
        // Half the screen below the map
        int mapSize = MapData.HEIGHT_IN_TILES * MapData.TILE_WIDTH;
        int screenSize = (int) (getSize().height / (2 * zoom));
        return mapSize - screenSize;
    }

    public void setMapXY(int x, int y) {
        setMapXYPixel(x * MapData.TILE_WIDTH, y * MapData.TILE_HEIGHT);
    }

    public void clampMapScroll() {
        scrollX = Math.min(scrollX, getMaxScrollX());
        scrollX = Math.max(scrollX, getMinScrollX());
        scrollY = Math.min(scrollY, getMaxScrollY());
        scrollY = Math.max(scrollY, getMinScrollY());
    }

    public void setMapXYPixel(double x, double y) {
        scrollX = x;
        scrollY = y;
        clampMapScroll();
        int tileX = (int) (scrollX / MapData.TILE_WIDTH);
        int tileY = (int) (scrollY / MapData.TILE_HEIGHT);
        tileX = Math.max(0, tileX);
        tileY = Math.max(0, tileY);
        this.screenX = Math.min(tileX, MapData.WIDTH_IN_TILES - screenWidth);
        this.screenY = Math.min(tileY, MapData.HEIGHT_IN_TILES - screenHeight);
    }

    public void centerScroll(int x, int y) {
        setMapXYPixel(
                x - MapData.TILE_WIDTH * screenWidth / 2,
                y - MapData.TILE_HEIGHT * screenHeight / 2);
    }
    public void setMapX(int x) {
        setMapXY(x, screenY);
    }

    public void setMapY(int y) {
        setMapXY(screenX, y);
    }

    public int getMapX() {
        return screenX;
    }

    public int getMapY() {
        return screenY;
    }

    public int getScrollX() {
        return (int) scrollX;
    }

    public int getScrollY() {
        return (int) scrollY;
    }
    public int getSectorX() {
        return sectorX;
    }

    public int getSectorY() {
        return sectorY;
    }

    public void adjustZoom(int delta) {
        // Convert last known mouse coordinates to world coordinates
        // (we'll be restoring these after the zoom)
        double worldX = lastMouseX / zoom + scrollX;
        double worldY = lastMouseY / zoom + scrollY;
        // For high zooms, use integer coords
        if (zoom > 1 || zoom == 1 && delta > 0) {
            zoom += delta;
            zoom = Math.min(zoom, 8);
            zoom = Math.max(zoom, 1);
        } else {
            // For zooming out, go inverted
            int invZoom = (int) (1.0 / zoom);
            invZoom -= delta;
            invZoom = Math.max(1, invZoom);
            invZoom = Math.min(8, invZoom);
            zoom = 1.0 / invZoom;
        }
        // Adjust scroll so worldX/worldY stay the same
        scrollX = worldX - lastMouseX / zoom;
        scrollY = worldY - lastMouseY / zoom;
        clampMapScroll();

        resetScreenSize();
        repaint();
    }

    private void selectSector(int sX, int sY) {
        sectorX = sX;
        sectorY = sY;
        MapData.Sector newS = map.getSector(sectorX, sectorY);
        selectedSector = newS;
        sectorPal = TileEditor.tilesets[TileEditor
                .getDrawTilesetNumber(selectedSector.tileset)]
                .getPaletteNum(selectedSector.tileset,
                        selectedSector.palette);
        copySector.setEnabled(true);
        pasteSector.setEnabled(true);
        copySector2.setEnabled(true);
        pasteSector2.setEnabled(true);
        repaint();
        this.fireActionPerformed(sectorEvent);
    }

    public int translateMouseX(MouseEvent e) {
        return (int) (e.getX() / zoom + scrollX - MapData.TILE_WIDTH * screenX);
    }

    public int translateMouseY(MouseEvent e) {
        return (int) (e.getY() / zoom + scrollY - MapData.TILE_HEIGHT * screenY);
    }

    public void mouseClicked(MouseEvent e) {
        int mouseX = translateMouseX(e);
        int mouseY = translateMouseY(e);

        // Make sure they didn't click on the border
        if ((mouseX >= 1)
                && (mouseX <= screenWidth * MapData.TILE_WIDTH + 2)
                && (mouseY >= 1)
                && (mouseY <= screenHeight * MapData.TILE_HEIGHT + 2)) {
            if (currentMode == MapMode.SPRITE) {
                if (e.getButton() == MouseEvent.BUTTON3) {
                    popupX = mouseX;
                    popupY = mouseY;
                    popupSE = getSpriteEntryFromMouseXY(mouseX, mouseY);
                    if (popupSE == null) {
                        detailsNPC.setText("No Sprite Selected");
                        delNPC.setEnabled(false);
                        cutNPC.setEnabled(false);
                        copyNPC.setEnabled(false);
                        switchNPC.setText("Switch NPC");
                        switchNPC.setEnabled(false);
                        moveNPC.setEnabled(false);
                    } else {
                        final int areaX = ((screenX + mouseX / MapData.TILE_WIDTH) / 8)
                                * MapData.TILE_WIDTH * 8;
                        final int areaY = ((screenY + mouseY
                                / MapData.TILE_HEIGHT) / 8)
                                * MapData.TILE_HEIGHT * 8;
                        detailsNPC.setText("Sprite @ ("
                                + (areaX + popupSE.x) + ","
                                + (areaY + popupSE.y) + ")");
                        delNPC.setEnabled(true);
                        cutNPC.setEnabled(true);
                        copyNPC.setEnabled(true);
                        switchNPC.setText("Switch NPC (" + popupSE.npcID
                                + ")");
                        switchNPC.setEnabled(true);
                        moveNPC.setEnabled(true);
                    }
                    // Don't use translated coords
                    spritePopupMenu.show(this, e.getX(), e.getY());
                }
            } else if (currentMode == MapMode.DOOR) {
                if (e.getButton() == MouseEvent.BUTTON3) {
                    popupX = mouseX;
                    popupY = mouseY;
                    popupDoor = getDoorFromMouseXY(mouseX, mouseY);
                    if (popupDoor == null) {
                        detailsDoor.setText("No Door Selected");
                        delDoor.setEnabled(false);
                        cutDoor.setEnabled(false);
                        copyDoor.setEnabled(false);
                        editDoor.setEnabled(false);
                        jumpDoor.setEnabled(false);
                    } else {
                        final int areaX = ((screenX + mouseX / MapData.TILE_WIDTH) / MapData.SECTOR_WIDTH)
                                * MapData.SECTOR_WIDTH * (MapData.TILE_WIDTH / 8);
                        final int areaY = ((screenY + mouseY / MapData.TILE_HEIGHT) / (MapData.SECTOR_HEIGHT * 2))
                                * MapData.SECTOR_HEIGHT * (MapData.TILE_HEIGHT / 8);
                        detailsDoor.setText(ToolModule
                                .capitalize(popupDoor.type)
                                + " @ ("
                                + (areaX + popupDoor.x)
                                + ","
                                + (areaY + popupDoor.y) + ")");
                        delDoor.setEnabled(true);
                        cutDoor.setEnabled(true);
                        copyDoor.setEnabled(true);
                        editDoor.setEnabled(true);
                        jumpDoor.setEnabled(popupDoor.type.equals("door"));

                    }
                    // Don't use translated coords
                    doorPopupMenu.show(this, e.getX(), e.getY());
                }
            } else if (currentMode == MapMode.SEEK_DOOR) {
                doorSeeker.seek(screenX * 4 + seekDrawX / 8, screenY * 4 + seekDrawY
                        / 8);
                doorSeeker = null;
                changeMode(currentMode);
                repaint();
            } else if (currentMode == MapMode.ENEMY) {
                int eX = ((mouseX - 1) / MapData.TILE_WIDTH + screenX) / 2;
                int eY = ((mouseY - 1) / MapData.TILE_HEIGHT + screenY) / 2;
                if (e.isShiftDown()) {
                    tileSelector.selectTile(map.getMapEnemyGroup(eX, eY));
                } else {
                    map.setMapEnemyGroup(eX, eY,
                            tileSelector.getSelectedTile());
                    repaint();
                }
            } else if (currentMode == MapMode.HOTSPOT) {
                int mx = ((mouseX - 1) / 8) + (screenX * 4), my = ((mouseY - 1) / 8)
                        + (screenY * 4);
                if (editHS != null) {
                    if (editHSx1 == -1) {
                        editHSx1 = mx;
                        editHSy1 = my;
                        repaint();
                    } else {
                        editHS.x1 = editHSx1;
                        editHS.y1 = editHSy1;
                        editHS.x2 = mx;
                        editHS.y2 = my;
                        editHS = null;
                        repaint();
                    }
                } else {
                    for (int i = 0; i < map.numHotspots(); ++i) {
                        MapData.Hotspot hs = map.getHotspot(i);
                        if ((mx >= hs.x1) && (mx <= hs.x2) && (my >= hs.y1)
                                && (my <= hs.y2)) {
                            editHS = hs;
                            editHSx1 = editHSy1 = -1;
                            hsMouseX = mouseX & (~7);
                            hsMouseY = mouseY & (~7);
                            repaint();
                            return;
                        }
                    }
                }
            }
        }
    }

    private void mapModeClick(int mouseX, int mouseY, int button, boolean shift) {
        if (button == MouseEvent.BUTTON1) {
            int mX = (mouseX - 1) / MapData.TILE_WIDTH + screenX;
            int mY = (mouseY - 1) / MapData.TILE_HEIGHT + screenY;
            if (shift) {
                tileSelector.selectTile(map.getMapTile(mX, mY));
            } else {
                // Keep track of the undo stuff
                undoStack.push(new UndoableTileChange(mX, mY, map
                        .getMapTile(mX, mY), tileSelector
                        .getSelectedTile()));
                undoButton.setEnabled(true);
                redoStack.clear();

                map.setMapTile(mX, mY,
                        tileSelector.getSelectedTile());
                repaint();
            }
        } else if (button == MouseEvent.BUTTON3) {
            // Make sure they didn't click on the border
            int sX = (screenX + ((mouseX - 1) / MapData.TILE_WIDTH))
                    / MapData.SECTOR_WIDTH;
            int sY = (screenY + ((mouseY - 1) / MapData.TILE_HEIGHT))
                    / MapData.SECTOR_HEIGHT;
            selectSector(sX, sY);
        }
    }

    public void actionPerformed(ActionEvent ae) {
        if (ae.getActionCommand().equals("newNPC")) {
            pushNpcIdFromMouseXY(0, popupX, popupY);
            repaint();
        } else if (ae.getActionCommand().equals("delNPC")) {
            popNpcIdFromMouseXY(popupX, popupY);
            repaint();
        } else if (ae.getActionCommand().equals("cutNPC")) {
            copiedNPC = popNpcIdFromMouseXY(popupX, popupY);
            repaint();
        } else if (ae.getActionCommand().equals("copyNPC")) {
            copiedNPC = popupSE.npcID;
        } else if (ae.getActionCommand().equals("pasteNPC")) {
            pushNpcIdFromMouseXY(copiedNPC, popupX, popupY);
            repaint();
        } else if (ae.getActionCommand().equals("switchNPC")) {
            String input = JOptionPane.showInputDialog(this,
                    "Switch this to a different NPC", popupSE.npcID);
            if (input != null) {
                popupSE.npcID = Integer.parseInt(input);
                repaint();
            }
        } else if (ae.getActionCommand().equals("moveNPC")) {
            int areaX = (screenX + popupX / MapData.TILE_WIDTH) / 8;
            int areaY = (screenY + popupY / MapData.TILE_HEIGHT) / 8;

            final int newSpriteX, newSpriteY;

            String input = JOptionPane.showInputDialog(this,
                    "New X in pixels", areaX * MapData.TILE_WIDTH * 8 + popupSE.x);
            if (input == null)
                return;
            newSpriteX = Integer.parseInt(input);

            input = JOptionPane.showInputDialog(this,
                    "New Y in pixels", areaY * MapData.TILE_HEIGHT * 8 + popupSE.y);
            if (input == null)
                return;
            newSpriteY = Integer.parseInt(input);

            popNpcIdFromMouseXY(popupX, popupY);
            pushNpcIdFromMapPixelXY(popupSE.npcID, newSpriteX, newSpriteY);

            repaint();
        } else if (ae.getActionCommand().equals("newDoor")) {
            pushDoorFromMouseXY(new MapData.Door(), popupX, popupY);
            repaint();
        } else if (ae.getActionCommand().equals("delDoor")) {
            popDoorFromMouseXY(popupX, popupY);
            repaint();
        } else if (ae.getActionCommand().equals("cutDoor")) {
            copiedDoor = popDoorFromMouseXY(popupX, popupY);
            repaint();
        } else if (ae.getActionCommand().equals("copyDoor")) {
            copiedDoor = popupDoor.copy();
        } else if (ae.getActionCommand().equals("pasteDoor")) {
            pushDoorFromMouseXY(copiedDoor.copy(), popupX, popupY);
            repaint();
        } else if (ae.getActionCommand().equals("editDoor")) {
            Ebhack.main.showModule(DoorEditor.class, popupDoor);
        } else if (ae.getActionCommand().equals("jumpDoor")) {
            Ebhack.main.showModule(MapEditor.class, new int[]{
                    popupDoor.destX * 8, popupDoor.destY * 8});
        }
    }

    // Sprites
    private MapData.SpriteEntry getSpriteEntryFromMouseXY(int mouseX,
                                                          int mouseY) {
        int areaX = (screenX + mouseX / MapData.TILE_WIDTH) / 8, areaY = (screenY + mouseY
                / MapData.TILE_HEIGHT) / 8;
        mouseX += (screenX % 8) * MapData.TILE_WIDTH;
        mouseX %= (MapData.TILE_WIDTH * 8);
        mouseY += (screenY % 8) * MapData.TILE_HEIGHT;
        mouseY %= (MapData.TILE_HEIGHT * 8);
        return map.getSpriteEntryFromCoords(areaX, areaY, mouseX, mouseY);
    }

    private int popNpcIdFromMouseXY(int mouseX, int mouseY) {
        int areaX = (screenX + mouseX / MapData.TILE_WIDTH) / 8, areaY = (screenY + mouseY
                / MapData.TILE_HEIGHT) / 8;
        mouseX += (screenX % 8) * MapData.TILE_WIDTH;
        mouseX %= (MapData.TILE_WIDTH * 8);
        mouseY += (screenY % 8) * MapData.TILE_HEIGHT;
        mouseY %= (MapData.TILE_HEIGHT * 8);
        return map.popNPCFromCoords(areaX, areaY, mouseX, mouseY);
    }

    private void pushNpcIdFromMouseXY(int npc, int mouseX, int mouseY) {
        int areaX = (screenX + mouseX / MapData.TILE_WIDTH) / 8, areaY = (screenY + mouseY
                / MapData.TILE_HEIGHT) / 8;
        mouseX += (screenX % 8) * MapData.TILE_WIDTH;
        mouseX %= (MapData.TILE_WIDTH * 8);
        mouseY += (screenY % 8) * MapData.TILE_HEIGHT;
        mouseY %= (MapData.TILE_HEIGHT * 8);
        map.pushNPCFromCoords(npc, areaX, areaY, mouseX, mouseY);
    }

    private void pushNpcIdFromMapPixelXY(int npc, int mapPixelX, int mapPixelY) {
        final int areaX = (mapPixelX / MapData.TILE_WIDTH) / 8;
        final int areaY = (mapPixelY / MapData.TILE_HEIGHT) / 8;
        mapPixelX %= (MapData.TILE_WIDTH * 8);
        mapPixelY %= (MapData.TILE_HEIGHT * 8);
        map.pushNPCFromCoords(npc, areaX, areaY, mapPixelX, mapPixelY);
    }

    // Doors
    private MapData.Door getDoorFromMouseXY(int mouseX, int mouseY) {
        int areaX = (screenX + mouseX / MapData.TILE_WIDTH) / 8, areaY = (screenY + mouseY
                / MapData.TILE_HEIGHT) / 8;
        mouseX += (screenX % 8) * MapData.TILE_WIDTH;
        mouseX %= (MapData.TILE_WIDTH * 8);
        mouseY += (screenY % 8) * MapData.TILE_HEIGHT;
        mouseY %= (MapData.TILE_HEIGHT * 8);
        return map.getDoorFromCoords(areaX, areaY, mouseX / 8, mouseY / 8);
    }

    private MapData.Door popDoorFromMouseXY(int mouseX, int mouseY) {
        int areaX = (screenX + mouseX / MapData.TILE_WIDTH) / 8, areaY = (screenY + mouseY
                / MapData.TILE_HEIGHT) / 8;
        mouseX += (screenX % 8) * MapData.TILE_WIDTH;
        mouseX %= (MapData.TILE_WIDTH * 8);
        mouseY += (screenY % 8) * MapData.TILE_HEIGHT;
        mouseY %= (MapData.TILE_HEIGHT * 8);
        return map.popDoorFromCoords(areaX, areaY, mouseX / 8, mouseY / 8);
    }

    private void pushDoorFromMouseXY(MapData.Door door, int mouseX,
                                     int mouseY) {
        int areaX = (screenX + mouseX / MapData.TILE_WIDTH) / 8, areaY = (screenY + mouseY
                / MapData.TILE_HEIGHT) / 8;
        mouseX += (screenX % 8) * MapData.TILE_WIDTH;
        mouseX %= (MapData.TILE_WIDTH * 8);
        mouseY += (screenY % 8) * MapData.TILE_HEIGHT;
        mouseY %= (MapData.TILE_HEIGHT * 8);
        door.x = mouseX / 8;
        door.y = mouseY / 8;
        map.pushDoorFromCoords(door, areaX, areaY);
    }

    private static final Cursor blankCursor = Toolkit.getDefaultToolkit()
            .createCustomCursor(
                    new BufferedImage(16, 16, BufferedImage.TYPE_INT_ARGB),
                    new Point(0, 0), "blank cursor");

    public void mousePressed(MouseEvent e) {
        mouseDragButton = e.getButton();
        int mx = translateMouseX(e);
        int my = translateMouseY(e);
        if (e.isControlDown() && (e.getButton() == MouseEvent.BUTTON1)) {
            if (previousMode == null) {
                previousMode = currentMode;
                changeMode(MapMode.PREVIEW);

                tvPreview = true;
                tvPreviewX = mx;
                tvPreviewY = my;

                tvPreviewH = 224 / 2;
                if (prefs.getValueAsBoolean("maskOverscan")) {
                    tvPreviewW = 240 / 2;
                } else {
                    tvPreviewW = 256 / 2;
                }

                this.setCursor(blankCursor);
                repaint();
            }
        } else if (currentMode == MapMode.MAP) {
            mapModeClick(mx, my, e.getButton(), e.isShiftDown());
        } else if (e.getButton() == MouseEvent.BUTTON1) {
            if (currentMode == MapMode.SPRITE && (movingNPC == -1)) {
                movingNPC = popNpcIdFromMouseXY(mx, my);
                if (movingNPC != -1) {
                    MapData.NPC tmp = map.getNPC(movingNPC);
                    movingNPCimg = map.getSpriteImage(tmp.sprite,
                            tmp.direction);
                    movingNPCdim = map.getSpriteWH(tmp.sprite);
                    movingDrawX = mx - movingNPCdim[0] / 2 + 1;
                    movingDrawY = my - movingNPCdim[1] + 9;
                    repaint();
                }
            } else if (currentMode == MapMode.DOOR && (movingDoor == null)) {
                movingDoor = popDoorFromMouseXY(mx, my);
                if (movingDoor != null) {
                    movingDrawX = mx & (~7);
                    movingDrawY = my & (~7);
                    repaint();
                }
            } else if (currentMode == MapMode.TELEPORT && (movingTeleport == null)) {
                // Use world coordinates; mouse coords are a silly thing to be backwards
                // compatible with the old tile-based coordinates.
                int worldX = (int) (e.getX() / zoom + scrollX);
                int worldY = (int) (e.getY() / zoom + scrollY);
                int tileX = worldX / 8;
                int tileY = worldY / 8;
                for (TeleportDestination dest : map.getTeleportDestinations()) {
                    if (dest.x == tileX && dest.y == tileY) {
                        movingTeleport = dest;
                    }
                }
                for (PsiTeleportDestination dest : map.getPsiTeleportDestinations()) {
                    // The teleport graphic is 2x3 tiles
                    if (tileX >= dest.x && tileX <= dest.x + 1
                            && tileY >= dest.y && tileY <= dest.y + 2) {
                        movingTeleport = dest;
                    }
                }
            }
        }
    }

    public void mouseReleased(MouseEvent e) {
        mouseDragButton = -1;
        int mx = translateMouseX(e);
        int my = translateMouseY(e);
        if (e.getButton() == 1) {
            if (previousMode != null) {
                changeMode(previousMode);
                previousMode = null;
                this.setCursor(Cursor.getDefaultCursor());
                tvPreview = false;
                repaint();
            } else if (currentMode == MapMode.SPRITE && (movingNPC != -1)) {
                pushNpcIdFromMouseXY(movingNPC, mx, my);
                movingNPC = -1;
                repaint();
            } else if (currentMode == MapMode.DOOR && (movingDoor != null)) {
                pushDoorFromMouseXY(movingDoor, mx, my);
                movingDoor = null;
                repaint();
            }
            movingTeleport = null;
        }
    }

    @Override
    public void mouseEntered(MouseEvent e) {
        // TODO Auto-generated method stub

    }

    @Override
    public void mouseExited(MouseEvent e) {
        pixelCoordLabel.setText("Pixel X,Y: (-,-)");
        warpCoordLabel.setText("Warp X,Y: (-,-)");
        tileCoordLabel.setText("Tile X,Y: (-,-)");
    }

    public void mouseDragged(MouseEvent e) {
        int deltaX = e.getX() - lastMouseX;
        int deltaY = e.getY() - lastMouseY;
        lastMouseX = e.getX();
        lastMouseY = e.getY();
        int mouseX = translateMouseX(e);
        int mouseY = translateMouseY(e);
        if (tvPreview) {
            tvPreviewX = mouseX;
            tvPreviewY = mouseY;
            repaint();
        } else if (movingNPC != -1) {
            movingDrawX = mouseX - movingNPCdim[0] / 2 + 1;
            movingDrawY = mouseY - movingNPCdim[1] + 9;
            repaint();
        } else if (movingDoor != null) {
            movingDrawX = mouseX & (~7);
            movingDrawY = mouseY & (~7);
            repaint();
        } else if (movingTeleport != null) {
            int worldX = (int) (e.getX() / zoom + scrollX);
            int worldY = (int) (e.getY() / zoom + scrollY);
            int tileX = worldX / 8;
            int tileY = worldY / 8;
            tileX = Math.max(0, tileX);
            tileY = Math.max(0, tileY);
            tileX = Math.min(MapData.WIDTH_IN_TILES * 4 - 1, tileX);
            tileY = Math.min(MapData.HEIGHT_IN_TILES * 4 - 1, tileY);
            movingTeleport.x = tileX;
            movingTeleport.y = tileY;
            repaint();
        } else if (mouseDragButton == 2) {
            setMapXYPixel(scrollX - deltaX / zoom, scrollY - deltaY / zoom);
            this.repaint();
        } else if (currentMode == MapMode.MAP) {
            mapModeClick(mouseX, mouseY, mouseDragButton, e.isShiftDown());
        }

        updateCoordLabels(mouseX, mouseY);
    }

    public void mouseMoved(MouseEvent e) {
        lastMouseX = e.getX();
        lastMouseY = e.getY();
        int mouseX = translateMouseX(e);
        int mouseY = translateMouseY(e);
        if (currentMode == MapMode.SEEK_DOOR) {
            seekDrawX = mouseX & (~7);
            seekDrawY = mouseY & (~7);
            repaint();
        } else if (currentMode == MapMode.HOTSPOT && (editHS != null)) {
            hsMouseX = mouseX & (~7);
            hsMouseY = mouseY & (~7);
            repaint();
        }

        updateCoordLabels(mouseX, mouseY);
    }

    private void updateCoordLabels(final int mouseX, final int mouseY) {
        if ((mouseX >= 0) && (mouseY >= 0)) {
            pixelCoordLabel.setText("Pixel X,Y: ("
                    + (screenX * MapData.TILE_WIDTH + mouseX - 1) + ","
                    + (screenY * MapData.TILE_WIDTH + mouseY - 1) + ")");
            warpCoordLabel.setText("Warp X,Y: ("
                    + (screenX * 4 + (mouseX - 1) / 8) + ","
                    + (screenY * 4 + (mouseY - 1) / 8) + ")");
            tileCoordLabel.setText("Tile X,Y: ("
                    + (screenX + (mouseX - 1) / MapData.TILE_WIDTH) + ","
                    + (screenY + (mouseY - 1) / MapData.TILE_HEIGHT) + ")");
        }
    }

    public void changeMode(MapMode mode) {
        gamePreview = mode == MapMode.PREVIEW;

        if (mode == MapMode.MAP) {
            undoButton.setEnabled(!undoStack.isEmpty());
            redoButton.setEnabled(!redoStack.isEmpty());
            copySector.setEnabled(selectedSector != null);
            pasteSector.setEnabled(selectedSector != null);
            copySector2.setEnabled(selectedSector != null);
            pasteSector2.setEnabled(selectedSector != null);
        } else {
            undoButton.setEnabled(false);
            redoButton.setEnabled(false);
            copySector.setEnabled(false);
            pasteSector.setEnabled(false);
            copySector2.setEnabled(false);
            pasteSector2.setEnabled(false);
        }

        currentMode = mode;
    }

    public void seek(DoorEditor de) {
        changeMode(MapMode.SEEK_DOOR);
        doorSeeker = de;
    }

    public void toggleGrid() {
        grid = !grid;
    }

    public void toggleSpriteBoxes() {
        spriteBoxes = !spriteBoxes;
    }

    public void toggleTileNums() {
        drawTileNums = !drawTileNums;
    }

    public void toggleSpriteNums() {
        drawSpriteNums = !drawSpriteNums;
    }

    public void toggleMapChanges() {
        // TODO Auto-generated method stub

    }

    public void toggleEnemyBattleSprites() {
        enemyBattleSprites = !enemyBattleSprites;
    }

    public boolean undoMapAction() {
        if (!undoStack.empty()) {
            Object undo = undoStack.pop();
            if (undo instanceof UndoableTileChange) {
                UndoableTileChange tc = (UndoableTileChange) undo;
                map.setMapTile(tc.x, tc.y, tc.oldTile);
            } else if (undo instanceof UndoableSectorPaste) {
                // UndoableSectorPaste usp = (UndoableSectorPaste) undo;
                // TODO
            }
            if (undoStack.isEmpty())
                undoButton.setEnabled(false);
            redoStack.push(undo);
            redoButton.setEnabled(true);
            repaint();
            return true;
        } else
            return false;
    }

    public boolean redoMapAction() {
        if (!redoStack.empty()) {
            Object redo = redoStack.pop();
            if (redo instanceof UndoableTileChange) {
                UndoableTileChange tc = (UndoableTileChange) redo;
                map.setMapTile(tc.x, tc.y, tc.newTile);
            } else if (redo instanceof UndoableSectorPaste) {
                // TODO
            }
            if (redoStack.isEmpty())
                redoButton.setEnabled(false);
            undoStack.push(redo);
            undoButton.setEnabled(true);
            repaint();
            return true;
        } else
            return false;
    }

    public void resetScreenSize() {
        Dimension newD = getSize();
        int newSW = (int) Math.ceil(newD.width / 32.0);
        int newSH = 1 + (int) Math.ceil(newD.height / 32.0);
        setScreenSize(newSW, newSH);
    }

    public void setScreenSize(int newSW, int newSH) {
        newSW = (int) Math.ceil(newSW / zoom);
        newSH = (int) Math.ceil(newSH / zoom);
        newSW = Math.min(newSW, MapData.WIDTH_IN_TILES);
        newSH = Math.min(newSH, MapData.HEIGHT_IN_TILES);
        if ((newSW != screenWidth) || (newSH != screenHeight)) {
            screenWidth = newSW;
            screenHeight = newSH;

            setMapXYPixel(scrollX, scrollY);

            setPreferredSize(new Dimension(screenWidth * MapData.TILE_WIDTH
                    + 2, screenHeight * MapData.TILE_HEIGHT + 2));

            repaint();
        }
    }

    public void pasteSector(MapData.Sector copiedSector, int sectorX2,
                            int sectorY2, int[][] copiedSectorTiles) {
        for (int i = 0; i < copiedSectorTiles.length; i++)
            for (int j = 0; j < copiedSectorTiles[i].length; j++) {
                map.setMapTile(sectorX * 8 + j, sectorY * 4 + i,
                        copiedSectorTiles[i][j]);
            }
        map.getSector(sectorX, sectorY).copy(copiedSector);
        // TODO
        // undoStack.push(new UndoableSectorPaste(sectorX, sectorY,
        // copiedSectorTiles, copiedSector));
    }
}
