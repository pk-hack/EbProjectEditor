package ebhack;

import javax.imageio.ImageIO;
import javax.swing.*;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.filechooser.FileFilter;
import java.awt.*;
import java.awt.event.*;
import java.awt.image.BufferedImage;
import java.io.*;

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
	private MapTileSelector tileSelector;

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

		// Note - xScroll / yScroll bounds get overridden later
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

		tileSelector = new MapTileSelector(tilesetChooser, mapDisplay, 24, 4);
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
		updateXYScrollBars();
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
			mapDisplay.centerScroll(coords[0], coords[1]);
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
		xScroll.setMinimum(mapDisplay.getMinScrollX());
		xScroll.setMaximum(mapDisplay.getMaxScrollX());
		xScroll.setValue(mapDisplay.getScrollX());
		xScroll.addAdjustmentListener(this);
		yScroll.removeAdjustmentListener(this);
		yScroll.setValue(mapDisplay.getScrollY());
		yScroll.setMinimum(mapDisplay.getMinScrollY());
		yScroll.setMaximum(mapDisplay.getMaxScrollY());
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
			mapDisplay.changeMode(MapMode.MAP);
			mapDisplay.repaint();
			tileSelector.changeMode(MapMode.MAP);
			tileSelector.repaint();
		} else if (e.getActionCommand().equals("mode1")) {
			mapDisplay.changeMode(MapMode.SPRITE);
			mapDisplay.repaint();
			tileSelector.changeMode(MapMode.SPRITE);
			tileSelector.repaint();
		} else if (e.getActionCommand().equals("mode2")) {
			mapDisplay.changeMode(MapMode.DOOR);
			mapDisplay.repaint();
			tileSelector.changeMode(MapMode.DOOR);
			tileSelector.repaint();
		} else if (e.getActionCommand().equals("mode6")) {
			mapDisplay.changeMode(MapMode.HOTSPOT);
			mapDisplay.repaint();
			tileSelector.changeMode(MapMode.HOTSPOT);
			tileSelector.repaint();
		} else if (e.getActionCommand().equals("mode7")) {
			mapDisplay.changeMode(MapMode.ENEMY);
			mapDisplay.repaint();
			tileSelector.changeMode(MapMode.ENEMY);
			tileSelector.repaint();
		} else if (e.getActionCommand().equals("mode8")) {
			mapDisplay.changeMode(MapMode.VIEW_ALL);
			mapDisplay.repaint();
			tileSelector.changeMode(MapMode.VIEW_ALL);
			tileSelector.repaint();
		} else if (e.getActionCommand().equals("mode9")) {
			mapDisplay.changeMode(MapMode.PREVIEW);
			mapDisplay.repaint();
			tileSelector.changeMode(MapMode.PREVIEW);
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
			mapDisplay.setMapXYPixel(xScroll.getValue(), yScroll.getValue());
			updateXYFields();
			mapDisplay.repaint();
		}
	}

	public void setMapXYPixel(int x, int y) {
		mapDisplay.setMapXYPixel(x, y);

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
			setMapXYPixel(mapDisplay.getScrollX() + (e.getWheelRotation() * 4 * MapData.TILE_WIDTH),
					mapDisplay.getScrollY());
		} else { // Vertical scrolling
			setMapXYPixel(mapDisplay.getScrollX(),
					mapDisplay.getScrollY() + (e.getWheelRotation() * 4 * MapData.TILE_HEIGHT));
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
		int newSW = (int) Math.ceil(newD.width / 32.0);
		int newSH = 1 + (int) Math.ceil(newD.height / 32.0);
		mapDisplay.setScreenSize(newSW, newSH);
		// Reset this in case they lowered the window size and are now off the side
		mapDisplay.setMapXYPixel(xScroll.getValue(), yScroll.getValue());
		updateXYScrollBars();
		updateXYFields();
		tileSelector.setScreenSize(newSW);
	}

	@Override
	public void componentShown(ComponentEvent arg0) {
		// TODO Auto-generated method stub

	}
}
