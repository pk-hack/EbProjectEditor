package ebhack;

import ebhack.MapEditor.MapData.Sector;
import org.yaml.snakeyaml.DumperOptions;
import org.yaml.snakeyaml.DumperOptions.FlowStyle;
import org.yaml.snakeyaml.Yaml;

import javax.imageio.ImageIO;
import javax.swing.*;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.filechooser.FileFilter;
import java.awt.*;
import java.awt.event.*;
import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;
import java.io.*;
import java.util.*;
import java.util.List;

public class MapEditor extends ToolModule implements ActionListener,
		DocumentListener, AdjustmentListener, MouseWheelListener,
		ComponentListener {
	private JTextField xField, yField, itemField, townMapXField, townMapYField;
	private JComboBox tilesetChooser, palChooser, musicChooser, townMapChooser,
			settingChooser, townMapImageChooser, townMapArrowChooser;
	private JCheckBox teleportCheckbox;
	private JScrollBar xScroll, yScroll;
	private JPanel coordsPanel, sectorPanel, sectorPanel2;
	private JLabel pixelCoordLabel, warpCoordLabel, tileCoordLabel;
	private JMenu modeMenu;
	private JMenuItem /* findSprite, */copySector, pasteSector, copySector2,
			pasteSector2, undo, redo;

	public static MapData map;
	private MapDisplay mapDisplay;
	private TileSelector tileSelector;

	private MapData.Sector copiedSector, copiedSector2;
	private int[][] copiedSectorTiles = new int[4][8];

	public MapEditor(YMLPreferences prefs) {
		super(prefs);

		map = new MapData();
	}

	public String getDescription() {
		return "Map Editor";
	}

	public String getVersion() {
		return "0.1";
	}

	public String getCredits() {
		return "Written by ghost";
	}

	public void init() {
		mainWindow = createBaseWindow(this);
		mainWindow.setTitle(this.getDescription());
		mainWindow.addComponentListener(this);

		JMenuBar menuBar = new JMenuBar();
		ButtonGroup group = new ButtonGroup();
		JCheckBoxMenuItem checkBox;
		JRadioButtonMenuItem radioButton;
		JMenu menu;

		menu = new JMenu("Edit");
		undo = ToolModule.createJMenuItem("Undo Tile Change", 'u', "control Z",
				"undoMap", this);
		undo.setEnabled(false);
		menu.add(undo);
		redo = ToolModule.createJMenuItem("Redo Tile Change", 'r', "control Y",
				"redoMap", this);
		redo.setEnabled(false);
		menu.add(redo);
		menu.add(new JSeparator());
		copySector = ToolModule.createJMenuItem("Copy Sector", 'c',
				"control C", "copySector", this);
		menu.add(copySector);
		pasteSector = ToolModule.createJMenuItem("Paste Sector", 'p',
				"control V", "pasteSector", this);
		menu.add(pasteSector);
		copySector2 = ToolModule.createJMenuItem("Copy Sector Attributes", ' ',
				"control shift C", "copySector2", this);
		menu.add(copySector2);
		pasteSector2 = ToolModule.createJMenuItem("Paste Sector Attributes",
				' ', "control shift V", "pasteSector2", this);
		menu.add(pasteSector2);
		menu.add(new JSeparator());
		menu.add(ToolModule.createJMenuItem("Clear Map", 'm', null,
				"delAllMap", this));
		menu.add(ToolModule.createJMenuItem("Delete All Sprites", 's', null,
				"delAllSprites", this));
		menu.add(ToolModule.createJMenuItem("Delete All Doors", 'o', null,
				"delAllDoors", this));
		menu.add(ToolModule.createJMenuItem("Clear Enemy Placements", 'e',
				null, "delAllEnemies", this));
		menu.add(ToolModule.createJMenuItem("Delete/Clear All Of The Above",
				'a', null, "delAllEverything", this));
		menu.add(new JSeparator());
		menu.add(ToolModule.createJMenuItem("Clear Tile Image Cache", 't',
				"control R", "resetTileImages", this));
		menuBar.add(menu);

		modeMenu = new JMenu("Mode");
		group = new ButtonGroup();
		radioButton = new JRadioButtonMenuItem("Map Edit");
		radioButton.setAccelerator(KeyStroke.getKeyStroke("F1"));
		radioButton.setSelected(true);
		radioButton.setActionCommand("mode0");
		radioButton.addActionListener(this);
		group.add(radioButton);
		modeMenu.add(radioButton);
		radioButton = new JRadioButtonMenuItem("Sprite Edit");
		radioButton.setAccelerator(KeyStroke.getKeyStroke("F2"));
		radioButton.setSelected(false);
		radioButton.setActionCommand("mode1");
		radioButton.addActionListener(this);
		group.add(radioButton);
		modeMenu.add(radioButton);
		radioButton = new JRadioButtonMenuItem("Door Edit");
		radioButton.setAccelerator(KeyStroke.getKeyStroke("F3"));
		radioButton.setSelected(true);
		radioButton.setActionCommand("mode2");
		radioButton.addActionListener(this);
		group.add(radioButton);
		modeMenu.add(radioButton);
		radioButton = new JRadioButtonMenuItem("Enemy Edit");
		radioButton.setAccelerator(KeyStroke.getKeyStroke("F4"));
		radioButton.setSelected(true);
		radioButton.setActionCommand("mode7");
		radioButton.addActionListener(this);
		group.add(radioButton);
		modeMenu.add(radioButton);
		radioButton = new JRadioButtonMenuItem("Hotspot Edit");
		radioButton.setAccelerator(KeyStroke.getKeyStroke("F5"));
		radioButton.setSelected(true);
		radioButton.setActionCommand("mode6");
		radioButton.addActionListener(this);
		group.add(radioButton);
		modeMenu.add(radioButton);
		radioButton = new JRadioButtonMenuItem("Whole View");
		radioButton.setAccelerator(KeyStroke.getKeyStroke("F6"));
		radioButton.setSelected(true);
		radioButton.setActionCommand("mode8");
		radioButton.addActionListener(this);
		group.add(radioButton);
		modeMenu.add(radioButton);
		menuBar.add(modeMenu);
		radioButton = new JRadioButtonMenuItem("Game View");
		radioButton.setAccelerator(KeyStroke.getKeyStroke("F7"));
		radioButton.setSelected(true);
		radioButton.setActionCommand("mode9");
		radioButton.addActionListener(this);
		group.add(radioButton);
		modeMenu.add(radioButton);

		menu = new JMenu("Options");
		checkBox = new JCheckBoxMenuItem("Show Grid");
		checkBox.setMnemonic('g');
		checkBox.setSelected(true);
		checkBox.setActionCommand("grid");
		checkBox.setAccelerator(KeyStroke.getKeyStroke("control G"));
		checkBox.addActionListener(this);
		menu.add(checkBox);
		checkBox = new JCheckBoxMenuItem("Show Tile Numbers");
		checkBox.setMnemonic('t');
		checkBox.setSelected(false);
		checkBox.setActionCommand("tileNums");
		checkBox.addActionListener(this);
		menu.add(checkBox);
		menu.add(new JSeparator());
		checkBox = new PrefsCheckBox("Show Sector Attributes", prefs,
				"showSectorAttrs", false, 'c');
		checkBox.setActionCommand("showSectorAttrs");
		checkBox.addActionListener(this);
		menu.add(checkBox);
		checkBox = new PrefsCheckBox("Show Sector Town Map Settings", prefs,
				"showSectorAttrs2", false, 'c');
		checkBox.setActionCommand("showSectorAttrs2");
		checkBox.addActionListener(this);
		menu.add(checkBox);
		checkBox = new PrefsCheckBox("Show Coordinates", prefs, "showCoords",
				false, 'c');
		checkBox.setActionCommand("showCoords");
		checkBox.addActionListener(this);
		menu.add(checkBox);
		menu.add(new JSeparator());
		checkBox = new JCheckBoxMenuItem("Show NPC IDs");
		checkBox.setMnemonic('n');
		checkBox.setSelected(true);
		checkBox.setActionCommand("npcNums");
		checkBox.addActionListener(this);
		menu.add(checkBox);
		checkBox = new JCheckBoxMenuItem("Show Sprite Boxes");
		checkBox.setMnemonic('b');
		checkBox.setSelected(true);
		checkBox.setActionCommand("spriteboxes");
		checkBox.addActionListener(this);
		menu.add(checkBox);
		menu.add(new JSeparator());
		checkBox = new JCheckBoxMenuItem("Show Map Changes");
		checkBox.setMnemonic('c');
		checkBox.setSelected(false);
		checkBox.setActionCommand("mapchanges");
		checkBox.addActionListener(this);
		// menu.add(checkBox);
		menu.add(new PrefsCheckBox("Mask Overscan in Preview", prefs,
				"maskOverscan", false, 'o'));
		menuBar.add(menu);

        menu = new JMenu("Tools");
        menu.add(ToolModule.createJMenuItem("Export as Image", 'i', "control i",
                "exportAsImage", this));
        menuBar.add(menu);

		mainWindow.setJMenuBar(menuBar);

		JPanel contentPanel = new JPanel(new BorderLayout());

		JPanel topPanel = new JPanel();
		topPanel.setLayout(new BoxLayout(topPanel, BoxLayout.Y_AXIS));

		JPanel panel = new JPanel(new FlowLayout());
		panel.add(new JLabel("X: "));
		xField = ToolModule.createSizedJTextField(
				Integer.toString(MapData.WIDTH_IN_TILES).length(), true);
		xField.getDocument().addDocumentListener(this);
		panel.add(xField);
		panel.add(new JLabel("Y: "));
		yField = ToolModule.createSizedJTextField(
				Integer.toString(MapData.HEIGHT_IN_TILES).length(), true);
		panel.add(yField);
		panel.add(new JLabel("Tileset: "));
		tilesetChooser = new JComboBox();
		tilesetChooser.addActionListener(this);
		panel.add(tilesetChooser);
		loadTilesetNames();
		panel.add(new JLabel("Palette: "));
		palChooser = new JComboBox();
		palChooser.addActionListener(this);
		panel.add(palChooser);
		panel.add(new JLabel("Music: "));
		// TODO use ToolModule.creatJComboBoxFromArray() here
		musicChooser = new JComboBox();
		musicChooser.addActionListener(this);
		panel.add(musicChooser);
		loadMusicNames();

		topPanel.add(panel);

		// Misc. sector attributes
		sectorPanel = new JPanel(new FlowLayout());
		sectorPanel.add(new JLabel("Item: "));
		itemField = ToolModule.createSizedJTextField(3, true);
		itemField.getDocument().addDocumentListener(this);
		sectorPanel.add(itemField);
		sectorPanel.add(new JLabel("Town Map: "));
		townMapChooser = new JComboBox();
		for (int i = 0; i < MapData.Sector.TOWN_MAP_NAMES.length; ++i) {
			townMapChooser
					.addItem(getNumberedString(ToolModule
							.capitalize(MapData.Sector.TOWN_MAP_NAMES[i]), i));
		}
		townMapChooser.addActionListener(this);
		sectorPanel.add(townMapChooser);
		sectorPanel.add(new JLabel("Setting: "));
		// settingChooser = ToolModule
		// .createJComboBoxFromArray(MapData.Sector.SETTING_NAMES);
		settingChooser = new JComboBox();
		for (int i = 0; i < MapData.Sector.SETTING_NAMES.length; ++i)
			settingChooser.addItem(getNumberedString(
					ToolModule.capitalize(MapData.Sector.SETTING_NAMES[i]), i));
		settingChooser.addActionListener(this);
		sectorPanel.add(settingChooser);
		sectorPanel.add(new JLabel("Teleport Enabled: "));
		teleportCheckbox = new JCheckBox();
		teleportCheckbox.addActionListener(this);
		sectorPanel.add(teleportCheckbox);
		sectorPanel.setVisible(prefs.getValueAsBoolean("showSectorAttrs"));
		topPanel.add(sectorPanel);

		sectorPanel2 = new JPanel(new FlowLayout());
		sectorPanel2.add(new JLabel("Town Map Image: "));
		townMapImageChooser = new JComboBox();
		for (int i = 0; i < MapData.Sector.TOWN_MAP_IMAGES.length; ++i) {
			townMapImageChooser
					.addItem(getNumberedString(ToolModule
							.capitalize(MapData.Sector.TOWN_MAP_IMAGES[i]), i));
		}
		townMapImageChooser.addActionListener(this);
		sectorPanel2.add(townMapImageChooser);
		sectorPanel2.add(new JLabel("X: "));
		townMapXField = ToolModule.createSizedJTextField(3, true);
		townMapXField.getDocument().addDocumentListener(this);
		sectorPanel2.add(townMapXField);
		sectorPanel2.add(new JLabel("Y: "));
		townMapYField = ToolModule.createSizedJTextField(3, true);
		townMapYField.getDocument().addDocumentListener(this);
		sectorPanel2.add(townMapYField);
		sectorPanel2.add(new JLabel("Arrow: "));
		townMapArrowChooser = new JComboBox();
		for (int i = 0; i < MapData.Sector.TOWN_MAP_ARROWS.length; ++i) {
			townMapArrowChooser
					.addItem(getNumberedString(ToolModule
							.capitalize(MapData.Sector.TOWN_MAP_ARROWS[i]), i));
		}
		townMapArrowChooser.addActionListener(this);
		sectorPanel2.add(townMapArrowChooser);
		sectorPanel2.setVisible(prefs.getValueAsBoolean("showSectorAttrs2"));
		topPanel.add(sectorPanel2);

		contentPanel.add(topPanel, BorderLayout.NORTH);

		tilesetChooser.setEnabled(false);
		musicChooser.setEnabled(false);
		itemField.setEnabled(false);
		townMapChooser.setEnabled(false);
		settingChooser.setEnabled(false);
		teleportCheckbox.setEnabled(false);

		pixelCoordLabel = new JLabel("Pixel X,Y: (-,-)");
		warpCoordLabel = new JLabel("Warp X,Y: (-,-)");
		tileCoordLabel = new JLabel("Tiled X,Y: (-,-)");

		mapDisplay = new MapDisplay(map, copySector, pasteSector, copySector2,
				pasteSector2, undo, redo, pixelCoordLabel, warpCoordLabel,
				tileCoordLabel, prefs);
		mapDisplay.addMouseWheelListener(this);
		mapDisplay.addActionListener(this);
		mapDisplay.init();
		contentPanel.add(mapDisplay, BorderLayout.CENTER);

		xScroll = new JScrollBar(JScrollBar.HORIZONTAL, 0,
				mapDisplay.getScreenWidth(), 0, MapData.WIDTH_IN_TILES);
		xScroll.addAdjustmentListener(this);
		contentPanel.add(xScroll, BorderLayout.SOUTH);
		yScroll = new JScrollBar(JScrollBar.VERTICAL, 0,
				mapDisplay.getScreenHeight(), 0, MapData.HEIGHT_IN_TILES);
		yScroll.addAdjustmentListener(this);
		contentPanel.add(yScroll, BorderLayout.EAST);

		mainWindow.getContentPane().add(contentPanel, BorderLayout.CENTER);

		coordsPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
		coordsPanel.add(pixelCoordLabel);
		coordsPanel.add(new JSeparator());
		coordsPanel.add(warpCoordLabel);
		coordsPanel.add(new JSeparator());
		coordsPanel.add(tileCoordLabel);
		coordsPanel.setVisible(prefs.getValueAsBoolean("showCoords"));
		topPanel.add(coordsPanel);

		tileSelector = new TileSelector(24, 4);
		mapDisplay.setTileSelector(tileSelector);
		mainWindow.getContentPane().add(
				ToolModule.pairComponents(
						ToolModule.pairComponents(tileSelector,
								tileSelector.getScrollBar(), false),
						coordsPanel, false), BorderLayout.PAGE_END);

		mainWindow.invalidate();
		mainWindow.pack();
		mainWindow.setLocationByPlatform(true);
		mainWindow.validate();
		mainWindow.setResizable(true);
	}

	private void loadTilesetNames() {
		tilesetChooser.removeActionListener(this);
		tilesetChooser.removeAllItems();
		for (int i = 0; i < MapData.NUM_MAP_TSETS; i++)
			tilesetChooser
					.addItem(getNumberedString(
							TileEditor.TILESET_NAMES[TileEditor
									.getDrawTilesetNumber(i)], i, false));
		tilesetChooser.addActionListener(this);
	}

	// TODO make it actually load names from musiclisting.txt
	public void loadMusicNames() {
		musicChooser.removeActionListener(this);
		musicChooser.removeAllItems();
		for (int i = 0; i < 165; i++)
			musicChooser.addItem(getNumberedString("", i, false));
		musicChooser.addActionListener(this);
	}

	public void show() {
		super.show();

		mainWindow.setVisible(true);
	}

	public void show(Object o) {
		super.show();
		if (o instanceof DoorEditor) {
			mapDisplay.seek((DoorEditor) o);
			mapDisplay.repaint();
		} else if (o instanceof int[]) {
			int[] coords = (int[]) o;
			mapDisplay.setMapXY(coords[0] / MapData.TILE_WIDTH, coords[1]
					/ MapData.TILE_HEIGHT);
			updateXYFields();
			updateXYScrollBars();
			mapDisplay.repaint();
		}
		mainWindow.setVisible(true);
	}

	public void hide() {
		if (isInited)
			mainWindow.setVisible(false);
	}

	public static class MapDisplay extends AbstractButton implements
			ActionListener, MouseListener, MouseMotionListener {
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
			private Sector sector;

			public UndoableSectorPaste(int sectorX, int sectorY, int[][] tiles,
					Sector sector) {
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
		private int x = 0, y = 0;
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
		private int[] movingNPCdim;
		private MapData.Door movingDoor = null;

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
		private int previousMode = 0;
		private int togglePreviousMode = -1;
		private boolean editMap = true, drawTileNums = false;
		private boolean drawSprites = false, editSprites = false,
				drawSpriteNums = true;
		private boolean drawDoors = false, editDoors = false, seekDoor = false;
		private boolean drawEnemies = false, editEnemies = false;
		private boolean drawHotspots = false, editHotspots = false;
		private boolean gamePreview = false;
		private boolean tvPreview = false;
		private int tvPreviewX, tvPreviewY, tvPreviewW, tvPreviewH;

		// Coordinate labels
		private JLabel pixelCoordLabel, warpCoordLabel, tileCoordLabel;

		// Cache enemy colors
		public static Color[] enemyColors = null;

		private TileSelector tileSelector;

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
							((int) (Math.E * 0x100000 * i)) & 0xffffff);
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
			changeMode(0);
			reset();
		}

		public void reset() {
			undoStack.clear();
			undoButton.setEnabled(false);
			redoStack.clear();
			redoButton.setEnabled(false);
		}

		public void setTileSelector(TileSelector tileSelector) {
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
			int i, j, a;
			g.setPaint(Color.white);
			g.setFont(new Font("Arial", Font.PLAIN, 12));

			MapData.Sector sector;
			int pal;
			for (i = 0; i < screenHeight; i++) {
				for (j = 0; j < screenWidth; j++) {
					sector = map.getSector((j + x) / MapData.SECTOR_WIDTH,
							(i + y) / MapData.SECTOR_HEIGHT);
					pal = TileEditor.tilesets[TileEditor
							.getDrawTilesetNumber(sector.tileset)]
							.getPaletteNum(sector.tileset, sector.palette);
					g.drawImage(
							getTileImage(TileEditor
									.getDrawTilesetNumber(sector.tileset), map
									.getMapTile(x + j, y + i), pal), j
									* MapData.TILE_WIDTH + 1, i
									* MapData.TILE_HEIGHT + 1,
							MapData.TILE_WIDTH, MapData.TILE_HEIGHT, this);
					if (drawTileNums && !gamePreview) {
						drawNumber(g, map.getMapTile(x + j, y + i), j
								* MapData.TILE_WIDTH + 1, i
								* MapData.TILE_HEIGHT + 1, false, false);
					}
				}
			}

			if (grid && !gamePreview && !drawEnemies)
				drawGrid(g);

			if (editMap && (selectedSector != null)) {
				int sXt, sYt;
				if (((sXt = sectorX * MapData.SECTOR_WIDTH)
						+ MapData.SECTOR_WIDTH >= x)
						&& (sXt < x + screenWidth)
						&& ((sYt = sectorY * MapData.SECTOR_HEIGHT)
								+ MapData.SECTOR_HEIGHT >= y)
						&& (sYt < y + screenHeight)) {
					g.setPaint(Color.yellow);
					g.draw(new Rectangle2D.Double((sXt - x)
							* MapData.TILE_WIDTH + 1, (sYt - y)
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

			if (drawSprites) {
				MapData.NPC npc;
				int[] wh;
				List<MapData.SpriteEntry> area;
				for (i = y & (~7); i < (y & (~7)) + screenHeight + 8; i += 8) {
					for (j = x & (~7); j < (x & (~7)) + screenWidth + 8; j += 8) {
						try {
							area = map.getSpriteArea(j >> 3, i >> 3);
							for (MapData.SpriteEntry e : area) {
								npc = map.getNPC(e.npcID);
								wh = map.getSpriteWH(npc.sprite);
								if (spriteBoxes && !gamePreview) {
									g.setPaint(Color.RED);
									g.draw(new Rectangle2D.Double(e.x + (j - x)
											* MapData.TILE_WIDTH - wh[0] / 2,
											e.y + (i - y) * MapData.TILE_HEIGHT
													- wh[1] + 8, wh[0] + 1,
											wh[1] + 1));
								}
								g.drawImage(map.getSpriteImage(npc.sprite,
										npc.direction), e.x + (j - x)
										* MapData.TILE_WIDTH - wh[0] / 2 + 1,
										e.y + (i - y) * MapData.TILE_HEIGHT
												- wh[1] + 9, this);
								if (drawSpriteNums && !gamePreview) {
									drawNumber(g, e.npcID, e.x + (j - x)
											* MapData.TILE_WIDTH - wh[0] / 2,
											e.y + (i - y) * MapData.TILE_HEIGHT
													- wh[1] + 8, false, true);
								}
							}
						} catch (Exception e) {

						}
					}
				}

				if (editSprites && (movingNPC != -1)) {
					if (spriteBoxes) {
						g.setPaint(Color.RED);
						g.draw(new Rectangle2D.Double(movingDrawX - 1,
								movingDrawY - 1, movingNPCdim[0] + 1,
								movingNPCdim[1] + 1));
					}
					g.drawImage(movingNPCimg, movingDrawX, movingDrawY, this);
				}
			}

			if (drawDoors) {
				List<MapData.Door> area;
				for (i = y & (~7); i < (y & (~7)) + screenHeight + 8; i += 8) {
					for (j = x & (~7); j < (x & (~7)) + screenWidth + 8; j += 8) {
						try {
							area = map.getDoorArea(j >> 3, i >> 3);
							for (MapData.Door e : area) {
								g.setPaint(e.getColor());
								g.draw(new Rectangle2D.Double(e.x * 8 + (j - x)
										* MapData.TILE_WIDTH + 1, e.y * 8
										+ (i - y) * MapData.TILE_HEIGHT + 1, 8,
										8));
								g.draw(new Rectangle2D.Double(e.x * 8 + (j - x)
										* MapData.TILE_WIDTH + 3, e.y * 8
										+ (i - y) * MapData.TILE_HEIGHT + 3, 4,
										4));
								g.setPaint(Color.WHITE);
								g.draw(new Rectangle2D.Double(e.x * 8 + (j - x)
										* MapData.TILE_WIDTH + 2, e.y * 8
										+ (i - y) * MapData.TILE_HEIGHT + 2, 6,
										6));
								g.draw(new Rectangle2D.Double(e.x * 8 + (j - x)
										* MapData.TILE_WIDTH + 4, e.y * 8
										+ (i - y) * MapData.TILE_HEIGHT + 4, 2,
										2));
							}
						} catch (Exception e) {

						}
					}
				}

				if (editDoors && (movingDoor != null)) {
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

				if (seekDoor) {
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

			if (drawEnemies) {
				g.setFont(new Font("Arial", Font.PLAIN, 12));
				for (i = -(y % 2); i < screenHeight; i += 2) {
					for (j = -(x % 2); j < screenWidth; j += 2) {
						// Draw the grid
						Rectangle2D rect = new Rectangle2D.Double(j
								* MapData.TILE_WIDTH + 1, i
								* MapData.TILE_HEIGHT + 1,
								MapData.TILE_WIDTH * 2, MapData.TILE_HEIGHT * 2);
						if (grid && !gamePreview) {
							g.setColor(Color.BLACK);
							g.draw(rect);
						}

						a = map.getMapEnemyGroup((x + j) / 2, (y + i) / 2);
						if (a != 0) {
							g.setComposite(AlphaComposite.getInstance(
									AlphaComposite.SRC_OVER, 0.5F));
							g.setPaint(enemyColors[a]);
							g.fill(rect);

							g.setComposite(AlphaComposite.getInstance(
									AlphaComposite.SRC_OVER, 1.0F));
							drawNumber(g, a, j * MapData.TILE_WIDTH + 1, i
									* MapData.TILE_HEIGHT + 1, false, false);
						}
					}
				}
			}

			if (drawHotspots) {
				MapData.Hotspot hs;
				g.setComposite(AlphaComposite.getInstance(
						AlphaComposite.SRC_OVER, 0.8F));
				int tx1, ty1, tx2, ty2;
				for (i = 0; i < map.numHotspots(); ++i) {
					hs = map.getHotspot(i);
					if (hs == editHS)
						continue;
					tx1 = hs.x1 / 4 - x;
					ty1 = hs.y1 / 4 - y;
					tx2 = hs.x2 / 4 - x;
					ty2 = hs.y2 / 4 - y;
					if (((tx1 >= 0) && (tx1 <= screenWidth) && (ty1 >= 0) && (ty1 <= screenHeight))
							|| ((tx2 >= 0) && (tx2 <= screenWidth)
									&& (ty2 >= 0) && (ty2 <= screenHeight))) {
						g.setPaint(Color.PINK);
						g.fill(new Rectangle2D.Double(hs.x1 * 8 - x
								* MapData.TILE_WIDTH + 1, hs.y1 * 8 - y
								* MapData.TILE_HEIGHT + 1, (hs.x2 - hs.x1) * 8,
								(hs.y2 - hs.y1) * 8));

						drawNumber(g, i,
								hs.x1 * 8 - x * MapData.TILE_WIDTH + 1, hs.y1
										* 8 - y * MapData.TILE_HEIGHT + 1,
								false, false);
					}
				}

				if (editHotspots && (editHS != null)) {
					g.setPaint(Color.WHITE);
					if (editHSx1 != -1) {
						tx1 = editHSx1 * 8 - x * MapData.TILE_WIDTH + 1;
						ty1 = editHSy1 * 8 - y * MapData.TILE_HEIGHT + 1;
						g.fill(new Rectangle2D.Double(tx1, ty1, hsMouseX - tx1,
								hsMouseY - ty1));
					} else {
						g.fill(new Rectangle2D.Double(hsMouseX + 1,
								hsMouseY + 1, 65, 65));
					}
				}
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

		private Rectangle2D textBG;

		private void drawNumber(Graphics2D g, int n, int x, int y, boolean hex,
				boolean above) {
			String s;
			if (hex)
				s = addZeros(Integer.toHexString(n), 4);
			else
				s = addZeros(Integer.toString(n), 4);

			if (textBG == null)
				textBG = g.getFontMetrics().getStringBounds(s, g);

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

		public void setMapXY(int x, int y) {
			x = Math.max(0, x);
			y = Math.max(0, y);
			this.x = Math.min(x, MapData.WIDTH_IN_TILES - screenWidth);
			this.y = Math.min(y, MapData.HEIGHT_IN_TILES - screenHeight);
		}

		public void setMapX(int x) {
			setMapXY(x, y);
		}

		public void setMapY(int y) {
			setMapXY(x, y);
		}

		public int getMapX() {
			return x;
		}

		public int getMapY() {
			return y;
		}

		public int getSectorX() {
			return sectorX;
		}

		public int getSectorY() {
			return sectorY;
		}

		private void selectSector(int sX, int sY) {
			sectorX = sX;
			sectorY = sY;
			MapData.Sector newS = map.getSector(sectorX, sectorY);
			if (selectedSector != newS) {
				selectedSector = newS;
				sectorPal = TileEditor.tilesets[TileEditor
						.getDrawTilesetNumber(selectedSector.tileset)]
						.getPaletteNum(selectedSector.tileset,
								selectedSector.palette);
				copySector.setEnabled(true);
				pasteSector.setEnabled(true);
				copySector2.setEnabled(true);
				pasteSector2.setEnabled(true);
			} else {
				// Un-select sector
				selectedSector = null;
				copySector.setEnabled(false);
				pasteSector.setEnabled(false);
				copySector2.setEnabled(false);
				pasteSector2.setEnabled(false);
			}
			repaint();
			this.fireActionPerformed(sectorEvent);
		}

		public void mouseClicked(MouseEvent e) {
			// Make sure they didn't click on the border
			if ((e.getX() >= 1)
					&& (e.getX() <= screenWidth * MapData.TILE_WIDTH + 2)
					&& (e.getY() >= 1)
					&& (e.getY() <= screenHeight * MapData.TILE_HEIGHT + 2)) {
				if (editMap) {
					if (e.getButton() == MouseEvent.BUTTON1) {
						int mX = (e.getX() - 1) / MapData.TILE_WIDTH + x;
						int mY = (e.getY() - 1) / MapData.TILE_HEIGHT + y;
						if (e.isShiftDown()) {
							tileSelector.selectTile(map.getMapTile(mX, mY));
						} else if (!e.isControlDown()) {
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
					} else if (e.getButton() == MouseEvent.BUTTON3) {
						// Make sure they didn't click on the border
						int sX = (x + ((e.getX() - 1) / MapData.TILE_WIDTH))
								/ MapData.SECTOR_WIDTH;
						int sY = (y + ((e.getY() - 1) / MapData.TILE_HEIGHT))
								/ MapData.SECTOR_HEIGHT;
						selectSector(sX, sY);
					}
				} else if (editSprites) {
					if (e.getButton() == MouseEvent.BUTTON3) {
						popupX = e.getX();
						popupY = e.getY();
						popupSE = getSpriteEntryFromMouseXY(e.getX(), e.getY());
						if (popupSE == null) {
							detailsNPC.setText("No Sprite Selected");
							delNPC.setEnabled(false);
							cutNPC.setEnabled(false);
							copyNPC.setEnabled(false);
							switchNPC.setText("Switch NPC");
							switchNPC.setEnabled(false);
                            moveNPC.setEnabled(false);
						} else {
							final int areaX = ((x + popupX / MapData.TILE_WIDTH) / 8)
									* MapData.TILE_WIDTH * 8;
							final int areaY = ((y + popupY
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
						spritePopupMenu.show(this, e.getX(), e.getY());
					}
				} else if (editDoors) {
					if (e.getButton() == MouseEvent.BUTTON3) {
						popupX = e.getX();
						popupY = e.getY();
						popupDoor = getDoorFromMouseXY(e.getX(), e.getY());
						if (popupDoor == null) {
							detailsDoor.setText("No Door Selected");
							delDoor.setEnabled(false);
							cutDoor.setEnabled(false);
							copyDoor.setEnabled(false);
							editDoor.setEnabled(false);
							jumpDoor.setEnabled(false);
						} else {
							final int areaX = ((x + popupX / MapData.TILE_WIDTH) / MapData.SECTOR_WIDTH)
									* MapData.SECTOR_WIDTH * (MapData.TILE_WIDTH / 8);
							final int areaY = ((y + popupY / MapData.TILE_HEIGHT) / (MapData.SECTOR_HEIGHT * 2))
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
						doorPopupMenu.show(this, e.getX(), e.getY());
					}
				} else if (seekDoor) {
					doorSeeker.seek(x * 4 + seekDrawX / 8, y * 4 + seekDrawY
							/ 8);
					doorSeeker = null;
					changeMode(previousMode);
					repaint();
				} else if (editEnemies) {
					int eX = ((e.getX() - 1) / MapData.TILE_WIDTH + x) / 2;
					int eY = ((e.getY() - 1) / MapData.TILE_HEIGHT + y) / 2;
					if (e.isShiftDown()) {
						tileSelector.selectTile(map.getMapEnemyGroup(eX, eY));
					} else {
						map.setMapEnemyGroup(eX, eY,
								tileSelector.getSelectedTile());
						repaint();
					}
				} else if (editHotspots) {
					int mx = ((e.getX() - 1) / 8) + (x * 4), my = ((e.getY() - 1) / 8)
							+ (y * 4);
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
								hsMouseX = e.getX() & (~7);
								hsMouseY = e.getY() & (~7);
								repaint();
								return;
							}
						}
					}
				}
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
                int areaX = (x + popupX / MapData.TILE_WIDTH) / 8;
                int areaY = (y + popupY / MapData.TILE_HEIGHT) / 8;

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
				ebhack.Ebhack.main.showModule(DoorEditor.class, popupDoor);
			} else if (ae.getActionCommand().equals("jumpDoor")) {
				ebhack.Ebhack.main.showModule(MapEditor.class, new int[] {
						popupDoor.destX * 8, popupDoor.destY * 8 });
			}
		}

		// Sprites
		private MapData.SpriteEntry getSpriteEntryFromMouseXY(int mouseX,
				int mouseY) {
			int areaX = (x + mouseX / MapData.TILE_WIDTH) / 8, areaY = (y + mouseY
					/ MapData.TILE_HEIGHT) / 8;
			mouseX += (x % 8) * MapData.TILE_WIDTH;
			mouseX %= (MapData.TILE_WIDTH * 8);
			mouseY += (y % 8) * MapData.TILE_HEIGHT;
			mouseY %= (MapData.TILE_HEIGHT * 8);
			return map.getSpriteEntryFromCoords(areaX, areaY, mouseX, mouseY);
		}

		private int popNpcIdFromMouseXY(int mouseX, int mouseY) {
			int areaX = (x + mouseX / MapData.TILE_WIDTH) / 8, areaY = (y + mouseY
					/ MapData.TILE_HEIGHT) / 8;
			mouseX += (x % 8) * MapData.TILE_WIDTH;
			mouseX %= (MapData.TILE_WIDTH * 8);
			mouseY += (y % 8) * MapData.TILE_HEIGHT;
			mouseY %= (MapData.TILE_HEIGHT * 8);
			return map.popNPCFromCoords(areaX, areaY, mouseX, mouseY);
		}

		private void pushNpcIdFromMouseXY(int npc, int mouseX, int mouseY) {
			int areaX = (x + mouseX / MapData.TILE_WIDTH) / 8, areaY = (y + mouseY
					/ MapData.TILE_HEIGHT) / 8;
			mouseX += (x % 8) * MapData.TILE_WIDTH;
			mouseX %= (MapData.TILE_WIDTH * 8);
			mouseY += (y % 8) * MapData.TILE_HEIGHT;
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
			int areaX = (x + mouseX / MapData.TILE_WIDTH) / 8, areaY = (y + mouseY
					/ MapData.TILE_HEIGHT) / 8;
			mouseX += (x % 8) * MapData.TILE_WIDTH;
			mouseX %= (MapData.TILE_WIDTH * 8);
			mouseY += (y % 8) * MapData.TILE_HEIGHT;
			mouseY %= (MapData.TILE_HEIGHT * 8);
			return map.getDoorFromCoords(areaX, areaY, mouseX / 8, mouseY / 8);
		}

		private MapData.Door popDoorFromMouseXY(int mouseX, int mouseY) {
			int areaX = (x + mouseX / MapData.TILE_WIDTH) / 8, areaY = (y + mouseY
					/ MapData.TILE_HEIGHT) / 8;
			mouseX += (x % 8) * MapData.TILE_WIDTH;
			mouseX %= (MapData.TILE_WIDTH * 8);
			mouseY += (y % 8) * MapData.TILE_HEIGHT;
			mouseY %= (MapData.TILE_HEIGHT * 8);
			return map.popDoorFromCoords(areaX, areaY, mouseX / 8, mouseY / 8);
		}

		private void pushDoorFromMouseXY(MapData.Door door, int mouseX,
				int mouseY) {
			int areaX = (x + mouseX / MapData.TILE_WIDTH) / 8, areaY = (y + mouseY
					/ MapData.TILE_HEIGHT) / 8;
			mouseX += (x % 8) * MapData.TILE_WIDTH;
			mouseX %= (MapData.TILE_WIDTH * 8);
			mouseY += (y % 8) * MapData.TILE_HEIGHT;
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
			int mx = e.getX(), my = e.getY();
			if (e.isControlDown() && (e.getButton() == MouseEvent.BUTTON1)) {
				if (togglePreviousMode == -1) {
					togglePreviousMode = previousMode;
					changeMode(9);

					tvPreview = true;
					tvPreviewX = e.getX();
					tvPreviewY = e.getY();

					tvPreviewH = 224 / 2;
					if (prefs.getValueAsBoolean("maskOverscan")) {
						tvPreviewW = 240 / 2;
					} else {
						tvPreviewW = 256 / 2;
					}

					this.setCursor(blankCursor);
					repaint();
				}
			} else if (e.getButton() == MouseEvent.BUTTON1) {
				if (editSprites && (movingNPC == -1)) {
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
				} else if (editDoors && (movingDoor == null)) {
					movingDoor = popDoorFromMouseXY(mx, my);
					if (movingDoor != null) {
						movingDrawX = mx & (~7);
						movingDrawY = my & (~7);
						repaint();
					}
				}
			}
		}

		public void mouseReleased(MouseEvent e) {
			int mx = e.getX(), my = e.getY();
			if (e.getButton() == 1) {
				if (togglePreviousMode != -1) {
					changeMode(togglePreviousMode);
					togglePreviousMode = -1;
					this.setCursor(Cursor.getDefaultCursor());
					tvPreview = false;
					repaint();
				} else if (editSprites && (movingNPC != -1)) {
					pushNpcIdFromMouseXY(movingNPC, mx, my);
					movingNPC = -1;
					repaint();
				} else if (editDoors && (movingDoor != null)) {
					pushDoorFromMouseXY(movingDoor, mx, my);
					movingDoor = null;
					repaint();
				}
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
			if (tvPreview) {
				tvPreviewX = e.getX();
				tvPreviewY = e.getY();
				repaint();
			} else if (movingNPC != -1) {
				movingDrawX = e.getX() - movingNPCdim[0] / 2 + 1;
				movingDrawY = e.getY() - movingNPCdim[1] + 9;
				repaint();
			} else if (movingDoor != null) {
				movingDrawX = e.getX() & (~7);
				movingDrawY = e.getY() & (~7);
				repaint();
			}

            updateCoordLabels(e.getX(), e.getY());
		}

		public void mouseMoved(MouseEvent e) {
			if (seekDoor) {
				seekDrawX = e.getX() & (~7);
				seekDrawY = e.getY() & (~7);
				repaint();
			} else if (editHotspots && (editHS != null)) {
				hsMouseX = e.getX() & (~7);
				hsMouseY = e.getY() & (~7);
				repaint();
			}

            updateCoordLabels(e.getX(), e.getY());
		}

        private void updateCoordLabels(final int mouseX, final int mouseY) {
            if ((mouseX >= 0) && (mouseY >= 0)) {
                pixelCoordLabel.setText("Pixel X,Y: ("
                        + (x * MapData.TILE_WIDTH + mouseX - 1) + ","
                        + (y * MapData.TILE_WIDTH + mouseY - 1) + ")");
                warpCoordLabel.setText("Warp X,Y: ("
                        + (x * 4 + (mouseX - 1) / 8) + ","
                        + (y * 4 + (mouseY - 1) / 8) + ")");
                tileCoordLabel.setText("Tile X,Y: ("
                        + (x + (mouseX - 1) / MapData.TILE_WIDTH) + ","
                        + (y + (mouseY - 1) / MapData.TILE_HEIGHT) + ")");
            }
        }

		public void changeMode(int mode) {
			gamePreview = mode == 9;

			if (mode == 0) {
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

			if (mode == 0) {
				previousMode = mode;
				// Map Mode
				editMap = true;
				drawSprites = false;
				editSprites = false;
				drawDoors = false;
				editDoors = false;
				seekDoor = false;
				drawEnemies = false;
				editEnemies = false;
				drawHotspots = false;
				editHotspots = false;
			} else if (mode == 1) {
				previousMode = mode;
				// Sprite Mode
				editMap = false;
				drawSprites = true;
				editSprites = true;
				drawDoors = false;
				editDoors = false;
				seekDoor = false;
				drawEnemies = false;
				editEnemies = false;
				drawHotspots = false;
				editHotspots = false;
			} else if (mode == 2) {
				previousMode = mode;
				// Door Mode
				editMap = false;
				drawSprites = false;
				editSprites = false;
				drawDoors = true;
				editDoors = true;
				seekDoor = false;
				drawEnemies = false;
				editEnemies = false;
				drawHotspots = false;
				editHotspots = false;
			} else if (mode == 4) {
				// Seek Door Mode
				editMap = false;
				drawSprites = true;
				editSprites = false;
				drawDoors = true;
				editDoors = false;
				seekDoor = true;
				drawEnemies = false;
				editEnemies = false;
				drawHotspots = false;
				editHotspots = false;
			} else if (mode == 6) {
				previousMode = mode;
				// Hotspot Mode
				editMap = false;
				drawSprites = false;
				editSprites = false;
				drawDoors = false;
				editDoors = false;
				seekDoor = false;
				drawEnemies = false;
				editEnemies = false;
				drawHotspots = true;
				editHotspots = true;
			} else if (mode == 7) {
				previousMode = mode;
				// Enemy Mode
				editMap = false;
				drawSprites = false;
				editSprites = false;
				drawDoors = false;
				editDoors = false;
				seekDoor = false;
				drawEnemies = true;
				editEnemies = true;
				drawHotspots = false;
				editHotspots = false;
			} else if (mode == 8) {
				previousMode = mode;
				// View All
				editMap = false;
				drawSprites = true;
				editSprites = false;
				drawDoors = true;
				editDoors = false;
				seekDoor = false;
				drawEnemies = true;
				editEnemies = false;
				drawHotspots = true;
				editHotspots = false;
			} else if (mode == 9) {
				previousMode = mode;
				// Preview
				editMap = false;
				drawSprites = true;
				editSprites = false;
				drawDoors = false;
				editDoors = false;
				seekDoor = false;
				drawEnemies = false;
				editEnemies = false;
				drawHotspots = false;
				editHotspots = false;
			}
		}

		public void seek(DoorEditor de) {
			changeMode(4);
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

		public void setScreenSize(int newSW, int newSH) {
			if ((newSW != screenWidth) || (newSH != screenHeight)) {
				screenWidth = newSW;
				screenHeight = newSH;

				setMapXY(x, y);

				setPreferredSize(new Dimension(screenWidth * MapData.TILE_WIDTH
						+ 2, screenHeight * MapData.TILE_HEIGHT + 2));

				repaint();
			}
		}

		public void pasteSector(Sector copiedSector, int sectorX2,
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

	private class TileSelector extends AbstractButton implements MouseListener,
			AdjustmentListener {
		private int width, height;
		private int tile = 0, mode = 0;
		private JScrollBar scroll;

		public TileSelector(int width, int height) {
			super();

			this.width = width;
			this.height = height;

			scroll = new JScrollBar(JScrollBar.HORIZONTAL, 0, width, 0,
					(1024 / height) + (1024 % height > 0 ? 1 : 0));
			scroll.addAdjustmentListener(this);

			setPreferredSize(new Dimension(width * MapData.TILE_WIDTH + 3,
					height * MapData.TILE_HEIGHT + 3));

			changeMode(0);

			this.addMouseListener(this);
		}

		public void setScreenSize(int newSW) {
			if (newSW != width) {
				width = newSW;

				setPreferredSize(new Dimension(width * MapData.TILE_WIDTH + 3,
						height * MapData.TILE_HEIGHT + 3));
				scroll.setVisibleAmount(width);
				if (scroll.getValue() + width + 1 > scroll.getMaximum()) {
					scroll.setValue(scroll.getMaximum() - width - 1);

				}

				repaint();
			}
		}

		public void changeMode(int mode) {
			this.mode = mode;
			tile = 0;
			if (mode == 7) {
				scroll.setEnabled(true);
				scroll.setMaximum(203 / (height) + 1);
				tile = Math.min(202, tile);
			} else if (mode == 0) {
				scroll.setEnabled(true);
				scroll.setMaximum(1024 / (height) + 1);
			} else {
				scroll.setEnabled(false);
				scroll.setMaximum(0);
			}
		}

		public void selectTile(int tile) {
			this.tile = tile;
			if ((tile < scroll.getValue() * height)
					|| (tile > (scroll.getValue() + width + 1) * height))
				scroll.setValue(tile / height);
			else
				repaint();
		}

		public int getSelectedTile() {
			return tile;
		}

		public JScrollBar getScrollBar() {
			return scroll;
		}

		public void paintComponent(Graphics g) {
			super.paintComponent(g);
			Graphics2D g2d = (Graphics2D) g;

			if (isEnabled()) {
				if (mode == 0)
					drawTiles(g2d);
				else if (mode == 7)
					drawEnemies(g2d);
				drawGrid(g2d);
			} else {
				scroll.setEnabled(false);
			}
		}

		private void drawEnemies(Graphics2D g) {
			int dtile;
			String message;
			Rectangle2D rect;
			for (int i = 0; i < width; i++) {
				for (int j = 0; j < height; j++) {
					dtile = (i + scroll.getValue()) * height + j;
					if (dtile < 203) {
						g.setPaint(MapDisplay.enemyColors[dtile]);
						g.fill(new Rectangle2D.Double(i * MapData.TILE_WIDTH
								+ 1, j * MapData.TILE_HEIGHT + 1,
								MapData.TILE_WIDTH, MapData.TILE_HEIGHT));

						g.setPaint(Color.black);
						message = addZeros(Integer.toString(dtile), 3);
						rect = g.getFontMetrics().getStringBounds(message, g);
						rect.setRect(i * MapData.TILE_WIDTH + 1, j
								* MapData.TILE_HEIGHT + 1, rect.getWidth(),
								rect.getHeight());
						g.fill(rect);
						g.setPaint(Color.white);
						g.drawString(message,
								(float) (i * MapData.TILE_WIDTH + 1),
								(float) (j * MapData.TILE_HEIGHT + rect
										.getHeight()));
						if (dtile == tile) {
							g.setPaint(Color.yellow);
							g.setComposite(AlphaComposite.getInstance(
									AlphaComposite.SRC_OVER, 0.6F));
							g.fillRect(i * MapData.TILE_WIDTH + 1, j
									* MapData.TILE_HEIGHT + 1,
									MapData.TILE_WIDTH, MapData.TILE_HEIGHT);
							g.setComposite(AlphaComposite.getInstance(
									AlphaComposite.SRC_OVER, 1.0F));
						}
					}
				}
			}
		}

		private void drawTiles(Graphics2D g) {
			int dtile;
			for (int i = 0; i < width; i++) {
				for (int j = 0; j < height; j++) {
					dtile = (i + scroll.getValue()) * height + j;
					if (dtile < 1024) {
						g.drawImage(MapDisplay.getTileImage(TileEditor
								.getDrawTilesetNumber(tilesetChooser
										.getSelectedIndex()), dtile, mapDisplay
								.getSelectedSectorPalNumber()), i
								* MapData.TILE_WIDTH + 1, j
								* MapData.TILE_HEIGHT + 1, MapData.TILE_WIDTH,
								MapData.TILE_HEIGHT, this);
						if (dtile == tile) {
							g.setPaint(Color.yellow);
							g.setComposite(AlphaComposite.getInstance(
									AlphaComposite.SRC_OVER, 0.6F));
							g.fillRect(i * MapData.TILE_WIDTH + 1, j
									* MapData.TILE_HEIGHT + 1,
									MapData.TILE_WIDTH, MapData.TILE_HEIGHT);
							g.setComposite(AlphaComposite.getInstance(
									AlphaComposite.SRC_OVER, 1.0F));
						}
					}
				}
			}
		}

		private void drawGrid(Graphics2D g) {
			g.setPaint(Color.black);
			// Draw vertical lines
			for (int i = 0; i < width + 1; i++)
				g.drawLine(1 + i * MapData.TILE_WIDTH, 1, 1 + i
						* MapData.TILE_WIDTH, height * MapData.TILE_HEIGHT);
			// Draw horizontal lines
			for (int i = 0; i < height + 1; i++)
				g.drawLine(1, 1 + i * MapData.TILE_HEIGHT, width
						* MapData.TILE_WIDTH, 1 + i * MapData.TILE_HEIGHT);

			// Blank pixel in the bottom right corner
			g.drawLine(width * MapData.TILE_WIDTH + 1, height
					* MapData.TILE_HEIGHT + 1, width * MapData.TILE_WIDTH + 1,
					height * MapData.TILE_HEIGHT + 1);

			// Draw border
			g.setColor(Color.black);
			g.draw(new Rectangle2D.Double(0, 0, width * MapData.TILE_WIDTH + 2,
					height * MapData.TILE_HEIGHT + 2));
		}

		public void adjustmentValueChanged(AdjustmentEvent e) {
			repaint();
		}

		public void mouseClicked(MouseEvent e) {
			if ((e.getButton() == MouseEvent.BUTTON1) && isEnabled()) {
				tile = (((e.getX() - 1) / MapData.TILE_WIDTH) + scroll
						.getValue())
						* height
						+ ((e.getY() - 1) / MapData.TILE_HEIGHT);
				if (mode == 0)
					tile = Math.min(tile, 1023);
				else if (mode == 7)
					tile = Math.min(tile, 202);
				repaint();
				if (e.isShiftDown()) {
					ebhack.Ebhack.main
							.showModule(
									TileEditor.class,
									new int[] {
											TileEditor
													.getDrawTilesetNumber(tilesetChooser
															.getSelectedIndex()),
											mapDisplay
													.getSelectedSectorPalNumber(),
											tile });
				}
			}
		}

		public void mousePressed(MouseEvent e) {
		}

		public void mouseReleased(MouseEvent e) {
		}

		public void mouseEntered(MouseEvent e) {
		}

		public void mouseExited(MouseEvent e) {
		}
	}

	public static class MapData {
		public static final int WIDTH_IN_TILES = 32 * 8;
		public static final int HEIGHT_IN_TILES = 80 * 4;
		public static final int SECTOR_WIDTH = 8;
		public static final int SECTOR_HEIGHT = 4;
		public static final int WIDTH_IN_SECTORS = WIDTH_IN_TILES
				/ SECTOR_WIDTH;
		public static final int HEIGHT_IN_SECTORS = HEIGHT_IN_TILES
				/ SECTOR_HEIGHT;
		public static final int TILE_WIDTH = 32;
		public static final int TILE_HEIGHT = 32;

		public static final int NUM_MAP_TSETS = 32;
		public static final int NUM_DRAW_TSETS = 20;

		// Stores the map tiles
		private int[][] mapTiles;
		private Sector[][] sectors;
		private ArrayList<SpriteEntry>[][] spriteAreas;
		private ArrayList<Door>[][] doorAreas;
		private NPC[] npcs;
		private static Image[][] spriteGroups;
		private static int[][] spriteGroupDims;
		private int[][] enemyPlacement;
		private Hotspot[] hotspots;

		public MapData() {
			reset();
		}

		public void reset() {
			mapTiles = new int[HEIGHT_IN_TILES][WIDTH_IN_TILES];
			sectors = new Sector[HEIGHT_IN_SECTORS][WIDTH_IN_SECTORS];
			for (int i = 0; i < sectors.length; ++i)
				for (int j = 0; j < sectors[i].length; ++j)
					sectors[i][j] = new Sector();
			spriteAreas = new ArrayList[HEIGHT_IN_SECTORS / 2][WIDTH_IN_SECTORS];
			for (int i = 0; i < spriteAreas.length; ++i)
				for (int j = 0; j < spriteAreas[i].length; ++j)
					spriteAreas[i][j] = new ArrayList<SpriteEntry>();
			doorAreas = new ArrayList[HEIGHT_IN_SECTORS / 2][WIDTH_IN_SECTORS];
			for (int i = 0; i < doorAreas.length; ++i)
				for (int j = 0; j < doorAreas[i].length; ++j)
					doorAreas[i][j] = new ArrayList<Door>();
			npcs = new NPC[1584];
			spriteGroups = new Image[464][4];
			spriteGroupDims = new int[464][2];
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
			return npcs[n];
		}

		public int[] getSpriteWH(int n) {
			return spriteGroupDims[n];
		}

		// Sprite Editing

		public SpriteEntry getSpriteEntryFromCoords(int areaX, int areaY,
				int x, int y) {
			int[] wh;
			NPC npc;
			for (SpriteEntry e : spriteAreas[areaY][areaX]) {
				npc = npcs[e.npcID];
				wh = spriteGroupDims[npc.sprite];
				if ((e.x >= x - wh[0] / 2) && (e.x <= x + wh[0] / 2)
						&& (e.y >= y - wh[1] / 2) && (e.y <= y + wh[1] / 2)) {
					return e;
				}
			}
			return null;
		}

		public int popNPCFromCoords(int areaX, int areaY, int x, int y) {
			int[] wh;
			NPC npc;
			for (SpriteEntry e : spriteAreas[areaY][areaX]) {
				npc = npcs[e.npcID];
				wh = spriteGroupDims[npc.sprite];
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
			return spriteAreas[areaY][areaX];
		}

		// Door Editing

		public List<Door> getDoorArea(int areaX, int areaY) {
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
			return spriteGroups[sprite][direction];
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
				for (Map.Entry<Integer, Map<String, Object>> entry : sectorsMap
						.entrySet()) {
					npc = new NPC((Integer) entry.getValue().get("Sprite"),
							(String) entry.getValue().get("Direction"));
					npcs[entry.getKey()] = npc;
				}
			} catch (FileNotFoundException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}

		private void importSpriteGroups(Project proj) {
			int w, h, x, y, z;
			for (int i = 0; i < spriteGroups.length; ++i) {
				spriteGroups[i] = new Image[4];
				try {
					BufferedImage sheet = ImageIO.read(new File(proj
							.getFilename(
									"eb.SpriteGroupModule",
									"SpriteGroups/"
											+ ToolModule.addZeros(i + "", 3))));
					Graphics2D sg = sheet.createGraphics();

					w = sheet.getWidth() / 4;
					h = sheet.getHeight() / 4;
					spriteGroupDims[i] = new int[] { w, h };
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
							spriteGroups[i][z] = sp;
							++z;
						}
					}
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
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

			private static final Color[] doorColors = new Color[] {
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

		private final String[] climbDirs = new String[] { "nw", "ne", "sw",
				"se", "nowhere" };
		private final String[] destDirs = new String[] { "down", "up", "right",
				"left" };

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
				Map<Integer, Map<Integer, List<Map<String, Object>>>> doorsMap = (Map<Integer, Map<Integer, List<Map<String, Object>>>>) yaml
						.load(input);
				int y, x;
				ArrayList<Door> area;
				for (Map.Entry<Integer, Map<Integer, List<Map<String, Object>>>> rowEntry : doorsMap
						.entrySet()) {
					y = rowEntry.getKey();
					for (Map.Entry<Integer, List<Map<String, Object>>> entry : rowEntry
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
			Map<Integer, Map<Integer, List<Map<String, Object>>>> doorsMap = new HashMap<Integer, Map<Integer, List<Map<String, Object>>>>();
			int x, y = 0;
			for (List<Door>[] row : doorAreas) {
				Map<Integer, List<Map<String, Object>>> rowOut = new HashMap<Integer, List<Map<String, Object>>>();
				x = 0;
				for (List<Door> area : row) {
					if (area.isEmpty())
						rowOut.put(x, null);
					else {
						List<Map<String, Object>> areaOut = new ArrayList<Map<String, Object>>();
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
				options.setDefaultFlowStyle(FlowStyle.BLOCK);
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
				Map<Integer, Map<Integer, List<Map<String, Integer>>>> spritesMap = (Map<Integer, Map<Integer, List<Map<String, Integer>>>>) yaml
						.load(input);
				int y, x;
				ArrayList<SpriteEntry> area;
				for (Map.Entry<Integer, Map<Integer, List<Map<String, Integer>>>> rowEntry : spritesMap
						.entrySet()) {
					y = rowEntry.getKey();
					for (Map.Entry<Integer, List<Map<String, Integer>>> entry : rowEntry
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
			Map<Integer, Map<Integer, List<Map<String, Integer>>>> spritesMap = new HashMap<Integer, Map<Integer, List<Map<String, Integer>>>>();
			int x, y = 0;
			for (List<SpriteEntry>[] row : spriteAreas) {
				Map<Integer, List<Map<String, Integer>>> rowOut = new HashMap<Integer, List<Map<String, Integer>>>();
				x = 0;
				for (List<SpriteEntry> area : row) {
					if (area.isEmpty())
						rowOut.put(x, null);
					else {
						List<Map<String, Integer>> areaOut = new ArrayList<Map<String, Integer>>();
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
				options.setDefaultFlowStyle(FlowStyle.BLOCK);
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
				options.setDefaultFlowStyle(FlowStyle.BLOCK);
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
				options.setDefaultFlowStyle(FlowStyle.BLOCK);
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
				options.setDefaultFlowStyle(FlowStyle.BLOCK);
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
			public static final String[] TOWN_MAP_NAMES = new String[] {
					"none", "onett", "twoson", "threed", "fourside", "scaraba",
					"summers", "none 2" };
			public static final String[] SETTING_NAMES = new String[] { "none",
					"indoors", "exit mouse usable", "lost underworld sprites",
					"magicant sprites", "robot sprites", "butterflies",
					"indoors and butterflies" };
			public static final String[] TOWN_MAP_IMAGES = new String[] {
					"none", "onett", "twoson", "threed", "fourside", "scaraba",
					"summers" };
			public static final String[] TOWN_MAP_ARROWS = new String[] {
					"none", "up", "down", "right", "left" };

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
			for (List<SpriteEntry>[] row : spriteAreas) {
				for (List<SpriteEntry> area : row) {
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
			for (List<Door>[] row : doorAreas) {
				for (List<Door> area : row) {
					area.clear();
				}
			}
		}
	}

	public void load(Project proj) {
		map.load(proj);
	}

	public void save(Project proj) {
		map.save(proj);
	}

	public void refresh(Project proj) {
		map.loadExtraResources(proj);

		if (isInited)
			mapDisplay.repaint();
	}

	public void reset() {
		map.reset();
		if (mapDisplay != null)
			mapDisplay.reset();
	}

	private void updateXYScrollBars() {
		xScroll.removeAdjustmentListener(this);
		xScroll.setValue(mapDisplay.getMapX());
		xScroll.addAdjustmentListener(this);
		yScroll.removeAdjustmentListener(this);
		yScroll.setValue(mapDisplay.getMapY());
		yScroll.addAdjustmentListener(this);
	}

	private void updateXYFields() {
		xField.getDocument().removeDocumentListener(this);
		xField.setText(Integer.toString(mapDisplay.getMapX()));
		xField.getDocument().addDocumentListener(this);
		yField.getDocument().removeDocumentListener(this);
		yField.setText(Integer.toString(mapDisplay.getMapY()));
		yField.getDocument().addDocumentListener(this);
	}

	// Returns true if the map palette is invalid for the new tileset
	private boolean updatePaletteChooser(int mapTset, int mapPal) {
		palChooser.removeActionListener(this);
		palChooser.removeAllItems();
		TileEditor.Tileset tileset = TileEditor.tilesets[TileEditor
				.getDrawTilesetNumber(mapTset)];
		TileEditor.Tileset.Palette pal;
		for (int i = 0; i < tileset.getPaletteCount(); i++) {
			if ((pal = tileset.getPalette(i)).getMapTileset() == mapTset) {
				palChooser.addItem(Integer.toString(pal.getMapPalette()));
			}
		}
		if (mapPal >= palChooser.getItemCount()) {
			palChooser.setSelectedIndex(palChooser.getItemCount() - 1);
			palChooser.addActionListener(this);
			return true;
		} else {
			palChooser.setSelectedIndex(mapPal);
			palChooser.addActionListener(this);
			return false;
		}
	}

	private void updateSectorAttrs() {
		MapData.Sector sect = mapDisplay.getSelectedSector();

		if (musicChooser.getSelectedIndex() != sect.music) {
			musicChooser.removeActionListener(this);
			musicChooser.setSelectedIndex(sect.music);
			musicChooser.addActionListener(this);
		}

		itemField.getDocument().removeDocumentListener(this);
		itemField.setText(Integer.toString(sect.item));
		itemField.getDocument().addDocumentListener(this);

		if (townMapChooser.getSelectedIndex() != sect.getTownMapNum()) {
			townMapChooser.removeActionListener(this);
			townMapChooser.setSelectedIndex(sect.getTownMapNum());
			townMapChooser.addActionListener(this);
		}

		if (settingChooser.getSelectedIndex() != sect.getSettingNum()) {
			settingChooser.removeActionListener(this);
			settingChooser.setSelectedIndex(sect.getSettingNum());
			settingChooser.addActionListener(this);
		}

		if (teleportCheckbox.isSelected() != sect.isTeleportEnabled()) {
			teleportCheckbox.removeActionListener(this);
			teleportCheckbox.setSelected(sect.isTeleportEnabled());
			teleportCheckbox.addActionListener(this);
		}

		if (townMapImageChooser.getSelectedIndex() != sect.getTownMapImageNum()) {
			townMapImageChooser.removeActionListener(this);
			townMapImageChooser.setSelectedIndex(sect.getTownMapImageNum());
			townMapImageChooser.addActionListener(this);
		}

		townMapXField.getDocument().removeDocumentListener(this);
		townMapXField.setText(Integer.toString(sect.townmapX));
		townMapXField.getDocument().addDocumentListener(this);

		townMapYField.getDocument().removeDocumentListener(this);
		townMapYField.setText(Integer.toString(sect.townmapY));
		townMapYField.getDocument().addDocumentListener(this);

		if (townMapArrowChooser.getSelectedIndex() != sect.getTownMapArrowNum()) {
			townMapArrowChooser.removeActionListener(this);
			townMapArrowChooser.setSelectedIndex(sect.getTownMapArrowNum());
			townMapArrowChooser.addActionListener(this);
		}
	}

	public void actionPerformed(ActionEvent e) {
		if (e.getActionCommand().equals("sectorChanged")) {
			MapData.Sector sect = mapDisplay.getSelectedSector();
			if (sect == null) {
				tilesetChooser.setEnabled(false);
				palChooser.setEnabled(false);
				musicChooser.setEnabled(false);
				itemField.setEnabled(false);
				townMapChooser.setEnabled(false);
				settingChooser.setEnabled(false);
				teleportCheckbox.setEnabled(false);
				townMapImageChooser.setEnabled(false);
				townMapXField.setEnabled(false);
				townMapYField.setEnabled(false);
				townMapArrowChooser.setEnabled(false);
			} else {
				if (!tilesetChooser.isEnabled()) {
					tilesetChooser.setEnabled(true);
					palChooser.setEnabled(true);
					musicChooser.setEnabled(true);
					itemField.setEnabled(true);
					townMapChooser.setEnabled(true);
					settingChooser.setEnabled(true);
					teleportCheckbox.setEnabled(true);
					townMapImageChooser.setEnabled(true);
					townMapXField.setEnabled(true);
					townMapYField.setEnabled(true);
					townMapArrowChooser.setEnabled(true);
				}
				if (tilesetChooser.getSelectedIndex() != sect.tileset) {
					updatePaletteChooser(sect.tileset, sect.palette);
					tilesetChooser.removeActionListener(this);
					tilesetChooser.setSelectedIndex(sect.tileset);
					tilesetChooser.addActionListener(this);
				} else if (palChooser.getSelectedIndex() != sect.palette) {
					updatePaletteChooser(sect.tileset, sect.palette);
					palChooser.removeActionListener(this);
					palChooser.setSelectedIndex(sect.palette);
					palChooser.addActionListener(this);
				}

				if (musicChooser.getSelectedIndex() != sect.music) {
					musicChooser.removeActionListener(this);
					musicChooser.setSelectedIndex(sect.music);
					musicChooser.addActionListener(this);
				}

				itemField.getDocument().removeDocumentListener(this);
				itemField.setText(Integer.toString(sect.item));
				itemField.getDocument().addDocumentListener(this);

				if (townMapChooser.getSelectedIndex() != sect.getTownMapNum()) {
					townMapChooser.removeActionListener(this);
					townMapChooser.setSelectedIndex(sect.getTownMapNum());
					townMapChooser.addActionListener(this);
				}

				if (settingChooser.getSelectedIndex() != sect.getSettingNum()) {
					settingChooser.removeActionListener(this);
					settingChooser.setSelectedIndex(sect.getSettingNum());
					settingChooser.addActionListener(this);
				}

				if (teleportCheckbox.isSelected() != sect.isTeleportEnabled()) {
					teleportCheckbox.removeActionListener(this);
					teleportCheckbox.setSelected(sect.isTeleportEnabled());
					teleportCheckbox.addActionListener(this);
				}

				if (townMapImageChooser.getSelectedIndex() != sect
						.getTownMapImageNum()) {
					townMapImageChooser.removeActionListener(this);
					townMapImageChooser.setSelectedIndex(sect
							.getTownMapImageNum());
					townMapImageChooser.addActionListener(this);
				}

				townMapXField.getDocument().removeDocumentListener(this);
				townMapXField.setText(Integer.toString(sect.townmapX));
				townMapXField.getDocument().addDocumentListener(this);

				townMapYField.getDocument().removeDocumentListener(this);
				townMapYField.setText(Integer.toString(sect.townmapY));
				townMapYField.getDocument().addDocumentListener(this);

				if (townMapArrowChooser.getSelectedIndex() != sect
						.getTownMapArrowNum()) {
					townMapArrowChooser.removeActionListener(this);
					townMapArrowChooser.setSelectedIndex(sect
							.getTownMapArrowNum());
					townMapArrowChooser.addActionListener(this);
				}

				if (tileSelector != null)
					tileSelector.repaint();
			}
		} else if (e.getSource().equals(tilesetChooser)) {
			mapDisplay.setSelectedSectorTileset(tilesetChooser
					.getSelectedIndex());
			if (updatePaletteChooser(mapDisplay.getSelectedSector().tileset,
					mapDisplay.getSelectedSector().palette))
				mapDisplay.setSelectedSectorPalette(palChooser
						.getSelectedIndex());
			mapDisplay.repaint();
			tileSelector.repaint();
		} else if (e.getSource().equals(palChooser)) {
			mapDisplay.setSelectedSectorPalette(palChooser.getSelectedIndex());
			tileSelector.repaint();
		} else if (e.getSource().equals(musicChooser)) {
			mapDisplay.getSelectedSector().music = musicChooser
					.getSelectedIndex();
		} else if (e.getSource().equals(townMapChooser)) {
			mapDisplay.getSelectedSector().setTownMapNum(
					townMapChooser.getSelectedIndex());
		} else if (e.getSource().equals(settingChooser)) {
			mapDisplay.getSelectedSector().setSettingNum(
					settingChooser.getSelectedIndex());
		} else if (e.getSource().equals(teleportCheckbox)) {
			mapDisplay.getSelectedSector().setTeleportEnabled(
					teleportCheckbox.isSelected());
		} else if (e.getSource().equals(townMapImageChooser)) {
			mapDisplay.getSelectedSector().setTownMapImageNum(
					townMapImageChooser.getSelectedIndex());
		} else if (e.getSource().equals(townMapArrowChooser)) {
			mapDisplay.getSelectedSector().setTownMapArrowNum(
					townMapArrowChooser.getSelectedIndex());
		} else if (e.getActionCommand().equals("apply")) {
			// TODO
		} else if (e.getActionCommand().equals("close")) {
			hide();
		} else if (e.getActionCommand().equals("mode0")) {
			mapDisplay.changeMode(0);
			mapDisplay.repaint();
			tileSelector.changeMode(0);
			tileSelector.repaint();
		} else if (e.getActionCommand().equals("mode1")) {
			mapDisplay.changeMode(1);
			mapDisplay.repaint();
			tileSelector.changeMode(1);
			tileSelector.repaint();
		} else if (e.getActionCommand().equals("mode2")) {
			mapDisplay.changeMode(2);
			mapDisplay.repaint();
			tileSelector.changeMode(2);
			tileSelector.repaint();
		} else if (e.getActionCommand().equals("mode6")) {
			mapDisplay.changeMode(6);
			mapDisplay.repaint();
			tileSelector.changeMode(6);
			tileSelector.repaint();
		} else if (e.getActionCommand().equals("mode7")) {
			mapDisplay.changeMode(7);
			mapDisplay.repaint();
			tileSelector.changeMode(7);
			tileSelector.repaint();
		} else if (e.getActionCommand().equals("mode8")) {
			mapDisplay.changeMode(8);
			mapDisplay.repaint();
			tileSelector.changeMode(8);
			tileSelector.repaint();
		} else if (e.getActionCommand().equals("mode9")) {
			mapDisplay.changeMode(9);
			mapDisplay.repaint();
			tileSelector.changeMode(9);
			tileSelector.repaint();
		} else if (e.getActionCommand().equals("delAllSprites")) {
			int sure = JOptionPane
					.showConfirmDialog(
							mainWindow,
							"Are you sure you want to delete all of the sprites?\n"
									+ "Note that the game may crash if less than 8 sprites exist on the map.",
							"Are you sure?", JOptionPane.YES_NO_OPTION);
			if (sure == JOptionPane.YES_OPTION) {
				map.nullSpriteData();
				mapDisplay.repaint();
			}
		} else if (e.getActionCommand().equals("delAllMap")) {
			int sure = JOptionPane.showConfirmDialog(mainWindow,
					"Are you sure you want to clear the map and sector data?",
					"Are you sure?", JOptionPane.YES_NO_OPTION);
			if (sure == JOptionPane.YES_OPTION) {
				map.nullMapData();
				mapDisplay.repaint();
			}
		} else if (e.getActionCommand().equals("delAllDoors")) {
			int sure = JOptionPane.showConfirmDialog(mainWindow,
					"Are you sure you want to delete the door placement data?",
					"Are you sure?", JOptionPane.YES_NO_OPTION);
			if (sure == JOptionPane.YES_OPTION) {
				map.nullDoorData();
				mapDisplay.repaint();
			}
		} else if (e.getActionCommand().equals("delAllEnemies")) {
			int sure = JOptionPane.showConfirmDialog(mainWindow,
					"Are you sure you want to clear the enemy data?",
					"Are you sure?", JOptionPane.YES_NO_OPTION);
			if (sure == JOptionPane.YES_OPTION) {
				map.nullEnemyData();
				mapDisplay.repaint();
			}
		} else if (e.getActionCommand().equals("delAllEverything")) {
			int sure = JOptionPane.showConfirmDialog(mainWindow,
					"Are you sure you want to all of the map data?",
					"Are you sure?", JOptionPane.YES_NO_OPTION);
			if (sure == JOptionPane.YES_OPTION) {
				map.nullMapData();
				map.nullSpriteData();
				map.nullDoorData();
				map.nullEnemyData();
				mapDisplay.repaint();
			}
		} else if (e.getActionCommand().equals("resetTileImages")) {
			MapDisplay.resetTileImageCache();
			mapDisplay.repaint();
			tileSelector.repaint();
		} else if (e.getActionCommand().equals("grid")) {
			mapDisplay.toggleGrid();
			mapDisplay.repaint();
		} else if (e.getActionCommand().equals("spriteboxes")) {
			mapDisplay.toggleSpriteBoxes();
			mapDisplay.repaint();
		} else if (e.getActionCommand().equals("tileNums")) {
			mapDisplay.toggleTileNums();
			mapDisplay.repaint();
		} else if (e.getActionCommand().equals("npcNums")) {
			mapDisplay.toggleSpriteNums();
			mapDisplay.repaint();
		} else if (e.getActionCommand().equals("mapchanges")) {
			mapDisplay.toggleMapChanges();
		} else if (e.getActionCommand().equals("showCoords")) {
			coordsPanel.setVisible(prefs.getValueAsBoolean("showCoords"));
			mainWindow.pack();
		} else if (e.getActionCommand().equals("showSectorAttrs")) {
			sectorPanel.setVisible(prefs.getValueAsBoolean("showSectorAttrs"));
			mainWindow.pack();
		} else if (e.getActionCommand().equals("showSectorAttrs2")) {
			sectorPanel2
					.setVisible(prefs.getValueAsBoolean("showSectorAttrs2"));
			mainWindow.pack();
		} else if (e.getActionCommand().equals("sectorEdit")) {
			// net.starmen.pkhack.JHack.main.showModule(
			// MapSectorPropertiesEditor.class, gfxcontrol
			// .getSectorxy());
		} else if (e.getActionCommand().equals("findSprite")) {
			String tpt = JOptionPane.showInputDialog(mainWindow,
					"Enter TPT entry to search for.", Integer.toHexString(0));
			int tptNum, yesno;
			try {
				tptNum = Integer.parseInt(tpt, 16);
			} catch (NumberFormatException nfe) {
				JOptionPane.showMessageDialog(mainWindow, "\"" + tpt
						+ "\" is not a valid hexidecimal number.\n"
						+ "Search was aborted.", "Error",
						JOptionPane.ERROR_MESSAGE);
				return;
			}
			/*
			 * if (tpt != null) { for (int i = 0; i < (MapData.HEIGHT_IN_SECTORS
			 * / 2) MapData.WIDTH_IN_SECTORS; i++) { ArrayList sprites =
			 * map.getSpritesData(i); MapData.SpriteLocation spLoc; int areaY,
			 * areaX; for (int j = 0; j < sprites.size(); j++) { spLoc =
			 * (MapData.SpriteLocation) sprites.get(j); if (spLoc.getTpt() ==
			 * tptNum) { areaY = i / MapEditor.widthInSectors; areaX = i -
			 * (areaY * MapEditor.widthInSectors); gfxcontrol .setMapXY( (areaX
			 * * MapEditor.sectorWidth) + (spLoc.getX() / MapEditor.tileWidth),
			 * (areaY * MapEditor.sectorHeight * 2) + (spLoc.getY() /
			 * MapEditor.tileHeight)); yesno = JOptionPane .showConfirmDialog(
			 * mainWindow,
			 * "I found a sprite with that TPT entry. Do you want to find another?"
			 * , "Continue Search?", JOptionPane.YES_NO_OPTION); if (yesno ==
			 * JOptionPane.NO_OPTION) return; } } }
			 * JOptionPane.showMessageDialog(mainWindow,
			 * "Could not find a sprite entry using TPT entry 0x" + tpt + ".");
			 * }
			 */
		} else if (e.getActionCommand().equals("copySector")) {
			pasteSector.setEnabled(true);

			int sectorX = mapDisplay.getSectorX();
			int sectorY = mapDisplay.getSectorY();
			for (int i = 0; i < copiedSectorTiles.length; i++)
				for (int j = 0; j < copiedSectorTiles[i].length; j++)
					copiedSectorTiles[i][j] = map.getMapTile(j + sectorX * 8, i
							+ sectorY * 4);
			copiedSector = map.getSector(sectorX, sectorY);

			updateSectorAttrs();
		} else if (e.getActionCommand().equals("pasteSector")) {
			int sectorX = mapDisplay.getSectorX();
			int sectorY = mapDisplay.getSectorY();
			mapDisplay.pasteSector(copiedSector, sectorX, sectorY,
					copiedSectorTiles);
			mapDisplay.repaint();

			// Refresh the tileset/palette fields if they changed
			if (tilesetChooser.getSelectedIndex() != copiedSector.tileset) {
				updatePaletteChooser(copiedSector.tileset, copiedSector.palette);
				tilesetChooser.removeActionListener(this);
				tilesetChooser.setSelectedIndex(copiedSector.tileset);
				tilesetChooser.addActionListener(this);
			} else if (palChooser.getSelectedIndex() != copiedSector.palette) {
				updatePaletteChooser(copiedSector.tileset, copiedSector.palette);
				palChooser.removeActionListener(this);
				palChooser.setSelectedIndex(copiedSector.palette);
				palChooser.addActionListener(this);
			}

			updateSectorAttrs();
		} else if (e.getActionCommand().equals("copySector2")) {
			pasteSector.setEnabled(true);

			int sectorX = mapDisplay.getSectorX();
			int sectorY = mapDisplay.getSectorY();
			copiedSector2 = map.getSector(sectorX, sectorY);

			updateSectorAttrs();
		} else if (e.getActionCommand().equals("pasteSector2")) {
			mapDisplay.getSelectedSector().copy(copiedSector2);

			mapDisplay.repaint();

			updateSectorAttrs();

			// gfxcontrol.updateComponents();
			/*
			 * } else if (ac.equals(ENEMY_SPRITES)) {
			 * gfxcontrol.toggleEnemySprites(); } else if
			 * (ac.equals(ENEMY_COLORS)) { gfxcontrol.toggleEnemyColors(); }
			 * else if (ac.equals(EVENTPAL)) { gfxcontrol.toggleEventPalette();
			 */
		} else if (e.getActionCommand().equals("undoMap")) {
			if (!mapDisplay.undoMapAction()) {
				JOptionPane.showMessageDialog(mainWindow,
						"There are no actions to undo.", "Error",
						JOptionPane.ERROR_MESSAGE);
			}
		} else if (e.getActionCommand().equals("redoMap")) {
			if (!mapDisplay.redoMapAction()) {
				JOptionPane.showMessageDialog(mainWindow,
						"There are no actions to redo.", "Error",
						JOptionPane.ERROR_MESSAGE);
			}
		} else if (e.getActionCommand().equals("exportAsImage")) {
            final JTextField inputX = ToolModule.createSizedJTextField(
                    Integer.toString(MapData.WIDTH_IN_TILES).length(), true);
            final JTextField inputX2 = ToolModule.createSizedJTextField(
                    Integer.toString(MapData.WIDTH_IN_TILES).length(), true);
            final JTextField inputY = ToolModule.createSizedJTextField(
                    Integer.toString(MapData.HEIGHT_IN_TILES).length(), true);
            final JTextField inputY2 = ToolModule.createSizedJTextField(
                    Integer.toString(MapData.HEIGHT_IN_TILES).length(), true);
            final Object[] message = {
                    "X1:", inputX,
                    "Y1:", inputY,
                    "X2:", inputX2,
                    "Y2:", inputY2
            };

            int option = JOptionPane.showConfirmDialog(null, message, "Login", JOptionPane.OK_CANCEL_OPTION);
            if (option != JOptionPane.OK_OPTION) {
                return;
            }

            final int imgX1 = Integer.parseInt(inputX.getText());
            final int imgY1 = Integer.parseInt(inputY.getText());
            final int imgX2 = Integer.parseInt(inputX2.getText());
            final int imgY2 = Integer.parseInt(inputY2.getText());

            final int imgX = Math.min(imgX1, imgX2);
            final int imgY = Math.min(imgY1, imgY2);
            final int imgW = Math.max(imgX1, imgX2) - imgX + 1;
            final int imgH = Math.max(imgY1, imgY2) - imgY + 1;

            final int oldScreenWidth = mapDisplay.getScreenWidth();
            final int oldScreenHeight = mapDisplay.getScreenHeight();
            final int oldX = mapDisplay.getMapX();
            final int oldY = mapDisplay.getMapY();

            mapDisplay.setScreenSize(imgW, imgH);
            mapDisplay.setMapXY(imgX, imgY);

            final BufferedImage bImg = new BufferedImage(imgW * MapData.TILE_WIDTH + 2,
                    imgH * MapData.TILE_HEIGHT + 2, BufferedImage.TYPE_INT_RGB);
            Graphics ig = bImg.createGraphics();
            mapDisplay.paintComponent(ig);

            mapDisplay.setScreenSize(oldScreenWidth, oldScreenHeight);
            mapDisplay.setMapXY(oldX, oldY);

            try
            {
                JFileChooser jfc = new JFileChooser();
                jfc.setFileFilter(new FileFilter()
                {
                    public boolean accept(File f)
                    {
                        if ((f.getAbsolutePath().toLowerCase().endsWith(".png")
                                || f.isDirectory())
                                && f.exists())
                        {
                            return true;
                        }
                        return false;
                    }

                    public String getDescription()
                    {
                        return "*.png";
                    }
                });

                if (jfc.showSaveDialog(null) == JFileChooser.APPROVE_OPTION) {
                    File file = jfc.getSelectedFile();
                    if (!file.getAbsolutePath().toLowerCase().endsWith((".png"))) {
                        file = new File(jfc.getSelectedFile() + ".png");
                    }
                    BufferedImage croppedImage = bImg.getSubimage(1, 1,
                            imgW * MapData.TILE_WIDTH, imgH * MapData.TILE_HEIGHT);
                    ImageIO.write(croppedImage, "png", file);

                    JOptionPane.showMessageDialog(mainWindow, "Image has been saved.", "Image Saved",
                            JOptionPane.INFORMATION_MESSAGE);
                }
            }
            catch (Exception ex)
            {
                ex.printStackTrace();
            }
        }
	}

	public void changedUpdate(DocumentEvent e) {
		if ((e.getDocument().equals(xField.getDocument()) || e.getDocument()
				.equals(yField.getDocument()))
				&& (yField.getText().length() > 0)
				&& (xField.getText().length() > 0)) {
			int newX = Integer.parseInt(xField.getText()), newY = Integer
					.parseInt(yField.getText());
			if (newX > MapData.WIDTH_IN_TILES - mapDisplay.getScreenWidth()) {
				newX = MapData.WIDTH_IN_TILES - mapDisplay.getScreenWidth();
			} else if (newX < 0) {
				newX = 0;
			}
			if (newY > MapData.HEIGHT_IN_TILES - mapDisplay.getScreenHeight()) {
				newY = MapData.HEIGHT_IN_TILES - mapDisplay.getScreenHeight();
			} else if (newY < 0) {
				newY = 0;
			}
			if ((newX != mapDisplay.getMapX())
					|| (newY != mapDisplay.getMapY())) {
				mapDisplay.setMapXY(newX, newY);
				updateXYScrollBars();
				mapDisplay.repaint();
			}
		} else if (e.getDocument().equals(itemField.getDocument())
				&& (itemField.getText().length() > 0)) {
			mapDisplay.getSelectedSector().item = Integer.parseInt(itemField
					.getText());
		} else if (e.getDocument().equals(townMapXField.getDocument())
				&& (townMapXField.getText().length() > 0)) {
			mapDisplay.getSelectedSector().townmapX = Integer
					.parseInt(townMapXField.getText());
		} else if (e.getDocument().equals(townMapYField.getDocument())
				&& (townMapYField.getText().length() > 0)) {
			mapDisplay.getSelectedSector().townmapY = Integer
					.parseInt(townMapYField.getText());
		}
	}

	public void insertUpdate(DocumentEvent e) {
		changedUpdate(e);
	}

	public void removeUpdate(DocumentEvent e) {
		changedUpdate(e);
	}

	public void adjustmentValueChanged(AdjustmentEvent e) {
		if (e.getSource().equals(xScroll) || e.getSource().equals(yScroll)) {
			mapDisplay.setMapXY(xScroll.getValue(), yScroll.getValue());
			updateXYFields();
			mapDisplay.repaint();
		}
	}

	public void setMapXY(int x, int y) {
		x = Math.min(x, MapData.WIDTH_IN_TILES - mapDisplay.getScreenWidth());
		x = Math.max(x, 0);

		y = Math.min(y, MapData.HEIGHT_IN_TILES - mapDisplay.getScreenHeight());
		y = Math.max(y, 0);

		mapDisplay.setMapXY(x, y);

		updateXYScrollBars();
		updateXYFields();
		mapDisplay.repaint();

		// These fields will be inaccurate, so just clear them until the mouse
		// moves again
		pixelCoordLabel.setText("Pixel X,Y: (-,-)");
		warpCoordLabel.setText("Warp X,Y: (-,-)");
		tileCoordLabel.setText("Tiledon X,Y: (-,-)");
	}

	public void mouseWheelMoved(MouseWheelEvent e) {
		if (e.isControlDown()) { // Horizontal scrolling
			setMapXY(mapDisplay.getMapX() + (e.getWheelRotation() * 4),
					mapDisplay.getMapY());
		} else { // Vertical scrolling
			setMapXY(mapDisplay.getMapX(),
					mapDisplay.getMapY() + (e.getWheelRotation() * 4));
		}
	}

	@Override
	public void componentHidden(ComponentEvent arg0) {
		// TODO Auto-generated method stub

	}

	@Override
	public void componentMoved(ComponentEvent arg0) {
		// TODO Auto-generated method stub

	}

	@Override
	public void componentResized(ComponentEvent arg0) {
		Dimension newD = mapDisplay.getSize();
		int newSW = newD.width / 32, newSH = newD.height / 32;
		mapDisplay.setScreenSize(newSW, newSH);
		updateXYScrollBars();
		updateXYFields();
		tileSelector.setScreenSize(newSW);
	}

	@Override
	public void componentShown(ComponentEvent arg0) {
		// TODO Auto-generated method stub

	}
}
