package ebhack;

import java.awt.BorderLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;

import ebhack.MapEditor.MapData;

public class DoorEditor extends ToolModule implements ActionListener,
		DocumentListener {

	public DoorEditor(YMLPreferences prefs) {
		super(prefs);
		// TODO Auto-generated constructor stub
	}

	public String getDescription() {
		return "Door Editor";
	}

	public String getVersion() {
		return "0.1";
	}

	public String getCredits() {
		return "Written by ghost";
	}

	private int indexOf(Object[] arr, Object target) {
		int i = 0;
		for (Object e : arr) {
			if (e.equals(target))
				return i;
			++i;
		}
		return -1;
	}

	private final String[] climbDirections = { "nw", "ne", "sw", "se",
			"nowhere" };
	private final String[] typeNames = { "switch", "rope", "ladder", "door",
			"escalator", "stairway", "object", "person" };

	private JComboBox typeBox, dirClimbBox, dirBox;
	private JTextField destX, destY, style, flagField, textPtr;
	private JButton seekButton, gotoButton;
	private JLabel destXhint, destYhint;
	private MapEditor.MapData.Door door;

	public void init() {
		mainWindow = createBaseWindow(this);
		mainWindow.setTitle(this.getDescription());

		JPanel doorInfo = new JPanel();
		doorInfo.setLayout(new BoxLayout(doorInfo, BoxLayout.Y_AXIS));

		typeBox = new JComboBox(typeNames);
		typeBox.addActionListener(this);
		doorInfo.add(ToolModule.getLabeledComponent("Type:", typeBox));
		dirClimbBox = new JComboBox(climbDirections);
		doorInfo.add(ToolModule.getLabeledComponent(
				"Stair/Escalator Direction:", dirClimbBox));
		flagField = ToolModule.createSizedJTextField(4, false);
		doorInfo.add(ToolModule.getLabeledComponent("Event Flag:", flagField));
		textPtr = ToolModule.createSizedJTextField(25, false);
		doorInfo.add(ToolModule.getLabeledComponent("Text Pointer:", textPtr));
		dirBox = new JComboBox(new String[] { "Down", "Up", "Right", "Left" });
		doorInfo.add(ToolModule.getLabeledComponent("Door Direction:", dirBox));
		destX = ToolModule.createSizedJTextField(4, true);
		destX.getDocument().addDocumentListener(this);
		destXhint = new JLabel("%4 = 0");
		destY = ToolModule.createSizedJTextField(4, true);
		destY.getDocument().addDocumentListener(this);
		destYhint = new JLabel("%4 = 0");
		gotoButton = new JButton("Go");
		gotoButton.setActionCommand("goto");
		gotoButton.addActionListener(this);
		seekButton = new JButton("Set");
		seekButton.setActionCommand("seek");
		seekButton.addActionListener(this);
		doorInfo.add(ToolModule.pairComponents(ToolModule.pairComponents(
				ToolModule.getLabeledComponent("Destination X:",
						ToolModule.pairComponents(destX, destXhint, false)),
				ToolModule.getLabeledComponent("Destination Y:",
						ToolModule.pairComponents(destY, destYhint, false)),
				true, true), ToolModule.pairComponents(gotoButton, seekButton,
				true, true), true, true));
		style = ToolModule.createSizedJTextField(4, true);
		doorInfo.add(ToolModule.getLabeledComponent("Warp Style:", style));

		mainWindow.getContentPane().add(doorInfo, BorderLayout.CENTER);
		mainWindow.invalidate();
		mainWindow.pack();
		// mainWindow.setSize(300, 400);
		mainWindow.setLocationByPlatform(true);
		mainWindow.validate();
		mainWindow.setResizable(false);
	}

	public void show() {
		super.show();
		if (door == null) {
			typeBox.setEnabled(false);
			typeBox.setSelectedIndex(-1);
		} else
			updateGUI();
		mainWindow.setVisible(true);
	}

	public void show(Object o) {
		super.show();
		this.door = (MapData.Door) o;
		typeBox.setEnabled(true);
		typeBox.setSelectedIndex(indexOf(typeNames, door.type));
		mainWindow.setVisible(true);
	}

	private void updateGUI() {
		// Clear all fields
		dirClimbBox.setSelectedIndex(-1);
		dirBox.setSelectedIndex(-1);
		destX.setText("0");
		destY.setText("0");
		style.setText("0");
		flagField.setText("0");
		textPtr.setText("$0");
		if (door == null) {
			typeBox.setEnabled(false);
			dirClimbBox.setEnabled(false);
			dirBox.setEnabled(false);
			destX.setEnabled(false);
			destY.setEnabled(false);
			seekButton.setEnabled(false);
			gotoButton.setEnabled(false);
			style.setEnabled(false);
			flagField.setEnabled(false);
			textPtr.setEnabled(false);
		} else {
			typeBox.setEnabled(true);
			if ((typeBox.getSelectedIndex() == 1)
					|| (typeBox.getSelectedIndex() == 2)) {
				// Rope/Ladder
				dirClimbBox.setEnabled(false);
				dirBox.setEnabled(false);
				destX.setEnabled(false);
				destY.setEnabled(false);
				seekButton.setEnabled(false);
				gotoButton.setEnabled(false);
				style.setEnabled(false);
				flagField.setEnabled(false);
				textPtr.setEnabled(false);
			} else if ((typeBox.getSelectedIndex() == 4)
					|| (typeBox.getSelectedIndex() == 5)) {
				// Stairs/escalator
				dirClimbBox.setEnabled(true);
				dirClimbBox.setSelectedIndex(door.climbDir);
				dirBox.setEnabled(false);
				destX.setEnabled(false);
				destY.setEnabled(false);
				seekButton.setEnabled(false);
				gotoButton.setEnabled(false);
				style.setEnabled(false);
				flagField.setEnabled(false);
				textPtr.setEnabled(false);
			} else if (typeBox.getSelectedIndex() == 0) {
				// Switch
				dirClimbBox.setEnabled(false);
				dirBox.setEnabled(false);
				destX.setEnabled(false);
				destY.setEnabled(false);
				seekButton.setEnabled(false);
				gotoButton.setEnabled(false);
				style.setEnabled(false);
				flagField.setEnabled(true);
				flagField.setText(Integer.toHexString(door.eventFlag));
				textPtr.setEnabled(true);
				textPtr.setText(door.pointer);
			} else if ((typeBox.getSelectedIndex() == 6)
					|| (typeBox.getSelectedIndex() == 7)) {
				// Object / Person
				dirClimbBox.setEnabled(false);
				dirBox.setEnabled(false);
				destX.setEnabled(false);
				destY.setEnabled(false);
				seekButton.setEnabled(false);
				gotoButton.setEnabled(false);
				style.setEnabled(false);
				flagField.setEnabled(false);
				textPtr.setEnabled(true);
				textPtr.setText(door.pointer);
			} else if (typeBox.getSelectedIndex() == 3) {
				// Door
				dirClimbBox.setEnabled(false);
				dirBox.setEnabled(true);
				dirBox.setSelectedIndex(door.destDir);
				destX.setEnabled(true);
				destX.setText(Integer.toString(door.destX));
				destY.setEnabled(true);
				destY.setText(Integer.toString(door.destY));
				seekButton.setEnabled(true);
				gotoButton.setEnabled(true);
				style.setEnabled(true);
				style.setText(Integer.toString(door.style));
				flagField.setEnabled(true);
				flagField.setText(Integer.toHexString(door.eventFlag));
				textPtr.setEnabled(true);
				textPtr.setText(door.pointer);
			}
		}
	}

	public void load(Project proj) {
		this.door = null;
	}

	public void save(Project proj) {
		// MapEditor handles this
	}

	public void hide() {
		if (mainWindow != null)
			mainWindow.setVisible(false);
	}

	public void seek(int x, int y) {
		destX.setText(Integer.toString(x));
		destY.setText(Integer.toString(y));
		typeBox.setEnabled(true);
		seekButton.setEnabled(true);
	}

	public void actionPerformed(ActionEvent ae) {
		if (ae.getActionCommand().equals("apply")) {
			if (door != null) {
				door.type = typeNames[typeBox.getSelectedIndex()];
				door.climbDir = dirClimbBox.getSelectedIndex();
				door.destDir = dirBox.getSelectedIndex();
				door.destX = Integer.parseInt(destX.getText());
				door.destY = Integer.parseInt(destY.getText());
				door.style = Integer.parseInt(style.getText());
				door.eventFlag = Integer.parseInt(flagField.getText(), 16);
				door.pointer = textPtr.getText();
			}
		} else if (ae.getActionCommand().equals("close")) {
			hide();
		} else if (ae.getActionCommand().equals("seek")) {
			typeBox.setEnabled(false);
			seekButton.setEnabled(false);
			ebhack.Ebhack.main.showModule(MapEditor.class, this);
		} else if (ae.getActionCommand().equals("goto")) {
			ebhack.Ebhack.main.showModule(
					MapEditor.class,
					new int[] { Integer.parseInt(destX.getText()) * 8,
							Integer.parseInt(destY.getText()) * 8 });
		} else if (ae.getSource().equals(typeBox)) {
			updateGUI();
		}
	}

	public void insertUpdate(DocumentEvent e) {
		changedUpdate(e);
	}

	public void removeUpdate(DocumentEvent e) {
		changedUpdate(e);
	}

	public void changedUpdate(DocumentEvent e) {
		if ((destX.getText().length() > 0) && (destY.getText().length() > 0)) {
			destXhint
					.setText("%4 = " + (Integer.parseInt(destX.getText()) % 4));
			destYhint
					.setText("%4 = " + (Integer.parseInt(destY.getText()) % 4));
		}
	}

}
