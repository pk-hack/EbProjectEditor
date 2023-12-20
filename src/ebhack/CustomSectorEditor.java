package ebhack;

import ebhack.types.CustomSectorData;

import javax.swing.*;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import java.awt.*;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.HashMap;
import java.util.Map;

public class CustomSectorEditor extends JPanel {
    private final CustomSectorData sectorData;
    private final MapData mapData;
    private final MapDisplay mapDisplay;
    private final Map<String, FieldData> fieldData = new HashMap<>();
    private Point sectorCoords = new Point(0, 0);
    public CustomSectorEditor(MapDisplay mapDisplay, MapData mapData) {
        this.mapDisplay = mapDisplay;
        this.mapData = mapData;
        sectorData = mapData.getCustomSectorData();
        setLayout(new BoxLayout(this, BoxLayout.PAGE_AXIS));
        // Properties up top
        for (String key : sectorData.getKeys()) {
            add(new FieldData(key, sectorData.getDefault(key)));
        }
        // Box is just a Swing layout spacer.
        add(Box.createVerticalGlue());
        // Button to add new fields
        JButton addFieldButton = new JButton("Add field");
        addFieldButton.addActionListener(actionEvent -> showAddFieldDialog());
        add(addFieldButton);
        // Put some width on our preferred size for when empty
        setPreferredSize(new Dimension(200, 10));
    }

    private void showAddFieldDialog() {
        // Build the option pane
        final JTextField inputKey = new JTextField();
        final JComboBox<String> inputType = new JComboBox<>();
        inputType.addItem("byte");
        inputType.addItem("short");
        inputType.addItem("long");
        final JTextField inputDefault = new JTextField();
        final Object[] message = {
                "CCS Label:", inputKey,
                "Type:", inputType,
                "Default value:", inputDefault
        };
        // Keep trying until the user gives up or gets it right
        boolean valid = false;
        while (!valid) {
            int option = JOptionPane.showConfirmDialog(this, message, "Add field", JOptionPane.OK_CANCEL_OPTION);
            if (option != JOptionPane.OK_OPTION) {
                return;
            }
            // Validate
            valid = true;
            if (!inputKey.getText().matches("[_A-Za-z][_A-Za-z0-9]*")) {
                JOptionPane.showMessageDialog(this,
                        "Invalid CCS label (ThisIsAValidOne).");
                valid = false;
                continue;
            }
            if (sectorData.containsKey(inputKey.getText())) {
                JOptionPane.showMessageDialog(this,
                        "CCS label already exists.");
                valid = false;
                continue;
            }
            if (inputDefault.getText().trim().isEmpty()) {
                JOptionPane.showMessageDialog(this,
                        "Default value can't be empty.");
                valid = false;
                continue;
            }
        }

        // Add this sector to our dataset
        String key = inputKey.getText();
        String type = (String) inputType.getSelectedItem();
        String defaultValue = inputDefault.getText();
        sectorData.createProperty(key, type, defaultValue, mapData.getSectorCount());

        // Insert before the spacer and this button
        int index = getComponentCount() - 2;
        add(new FieldData(key, defaultValue), index);
        revalidate();

        // Map display needs to show the field
        mapDisplay.repaint();
    }

    public void setSectorCoords(Point sectorCoords) {
        this.sectorCoords = sectorCoords;
        for (String key : sectorData.getKeys()) {
            FieldData data = fieldData.get(key);
            data.field.setEnabled(sectorCoords != null);
            if (sectorCoords == null) {
                data.field.setText("");
            } else {
                data.field.setText(sectorData.getSectorValue(key, sectorCoords));
            }
        }
    }

    /**
     * Gui container for one label/field pair for a custom sector component.
     */
    private class FieldData extends JPanel {
        private final String key;
        private final JTextField field;
        private FieldData(String key, String defaultValue) {
            this.key = key;
            fieldData.put(key, this);
            add(new JLabel(key + ": "));
            field = new JTextField();
            field.setText(defaultValue);
            field.getDocument().addDocumentListener(new DocumentListener() {
                // Yeah this interface is dumb and overengineered.
                @Override
                public void insertUpdate(DocumentEvent documentEvent) {
                    updateValue();
                }
                @Override
                public void removeUpdate(DocumentEvent documentEvent) {
                    updateValue();
                }
                @Override
                public void changedUpdate(DocumentEvent documentEvent) {
                    updateValue();
                }
            });
            // Text field needs a preferred size to not be tiny
            field.setPreferredSize(new Dimension(100, 16));
            field.setMinimumSize(new Dimension(50, 16));
            add(field);
            // Box layout needs a max vertical size to not stretch out awkwardly
            // needs two line heights in case Java decides to wrap the label
            setMaximumSize(new Dimension(400, 100));
        }
        private void updateValue() {
            String val = field.getText();
            if (!val.trim().isEmpty() && sectorCoords != null) {
                sectorData.setSectorValue(key, sectorCoords, val);
                mapDisplay.repaint();
            }

        }

    }

}
