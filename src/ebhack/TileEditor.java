package ebhack;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Graphics;
import java.awt.GridLayout;
import java.awt.Image;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.AdjustmentEvent;
import java.awt.event.AdjustmentListener;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.ListIterator;

import javax.swing.AbstractButton;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JPanel;
import javax.swing.JScrollBar;
import javax.swing.JSeparator;
import javax.swing.JTextField;
import javax.swing.SwingConstants;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;

public class TileEditor extends ToolModule implements ActionListener{
	public static final int NUM_TILESETS = 20;
	public static final int NUM_MAP_TILESETS = 32;
    public static final String[] TILESET_NAMES = {"Underworld", "Onett",
        "Twoson", "Threed", "Fourside", "Magicant", "Outdoors", "Summers",
        "Desert", "Dalaam", "Indoors 1", "Indoors 2", "Stores 1", "Caves 1",
        "Indoors 3", "Stores 2", "Indoors 4", "Winters", "Scaraba", "Caves 2"};
	public static Tileset[] tilesets;
	public static int[] drawingTilesets;
	
    private TileSelector tileSelector;
    private ArrangementSelector arrangementSelector;

    private JComboBox tilesetSelector, paletteSelector, subPaletteSelector;

    // private JTable collisionEditor;
    // private AbstractTableModel collisionTableModel;
    private CollisionEditor collisionEditor;
    private TileArrangementEditor arrangementEditor;

    private SpritePalette tileDrawingPalette;
    private DrawingToolset tileDrawingToolset;
    private IntArrDrawingArea tileDrawingArea, tileForegroundDrawingArea;

    private FocusIndicator drawingAreaFocusIndicator,
            arrangementFocusIndicator;
    
    private JDialog cbdia; // clipboard dialog
    private TileSelector cbsel; // clipboard tile selector
    private byte[][][][] cb; // clipboard
    
    // TODO use these
    private boolean guiInited = true;
    private boolean paletteIsInited = true;
    private boolean appliedChanges = false;
	
	public TileEditor(YMLPreferences prefs) {
		super(prefs);
		
		reset();
	}
	
	public void reset() {
		tilesets = new Tileset[NUM_TILESETS];
		drawingTilesets = new int[NUM_MAP_TILESETS];
	}
	
	public String getDescription() {
		return "Tile Editor";
	}
	
	public String getVersion() {
		return "0.8a";
	}
	
    public String getCredits()
    {
        return "Written by AnyoneEB for JHack\n" + "Based on code by Goplat\n" + "Ported/adapted by ghost";
    }
	
    private class ArrangementSelector extends AbstractButton implements MouseListener, MouseMotionListener, AdjustmentListener {
        private int currentArrangement = 0;

        public int getCurrentArrangement()
        {
            return currentArrangement;
        }

        private int getArrangementOffset()
        {
            return scroll.getValue() * 4;
            // each increment shows four arrangments (height)
        }

        public void repaintTile(int tile)
        {
            Graphics g = display.getGraphics();
            Image img[] = new Image[8]; // one for each subpal
            for (int a = getArrangementOffset(); a < getArrangementOffset() + 60; a++)
            {
                for (int x = 0; x < 4; x++)
                {
                    for (int y = 0; y < 4; y++)
                    {
                        int arr = getSelectedTileset().getArrangementData(a, x,
                            y);
                        if ((arr & 0x1ff) == tile)
                        {
                            int subPal = (((arr & 0x1C00) >> 10) - 2);
                            if (img[subPal] == null)
                                img[subPal] = getSelectedTileset()
                                    .getTileImage(tile, getCurrentPalette(),
                                        subPal);
                            // (dx, dy) = top-left corner of destination
                            int dx = (((a - getArrangementOffset()) / 4) * 33)
                                + (x * 8), dy = (((a - getArrangementOffset()) % 4) * 33)
                                + (y * 8);
                            g.drawImage(img[subPal], dx, dy, dx + 8, dy + 8,
                                ((arr & 0x4000) == 0 ? 0 : 8),
                                ((arr & 0x8000) == 0 ? 0 : 8),
                                ((arr & 0x4000) != 0 ? 0 : 8),
                                ((arr & 0x8000) != 0 ? 0 : 8), null);
                            // if this just drew on the current arrangement...
                            if (a == getCurrentArrangement())
                            {
                                // rehighlight the part that was just drawn over
                                g.setColor(new Color(255, 255, 0, 128));
                                g.fillRect(
                                    (((a - getArrangementOffset()) / 4) * 33)
                                        + (x * 8),
                                    (((a - getArrangementOffset()) % 4) * 33)
                                        + (y * 8), 8, 8);
                            }
                        }
                    }
                }
            }
        }

        public void repaintCurrentTile()
        {
            repaintTile(getCurrentTile());
        }

        public void repaintCurrentArrangement()
        {
            int a = getCurrentArrangement() - getArrangementOffset();
            // is it being shown?
            if (a >= 0 && a < 60)
            {
                drawArrangement(display.getGraphics(), getCurrentArrangement());
                highlightArrangement(display.getGraphics(),
                    getCurrentArrangement());
            }
        }

        private void drawArrangement(Graphics g, int arr)
        {
            g.drawImage(getSelectedTileset().getArrangementImage(arr,
                getCurrentPalette()),
                ((arr - getArrangementOffset()) / 4) * 33,
                ((arr - getArrangementOffset()) % 4) * 33, null);
        }

        private void highlightArrangement(Graphics g, int arr)
        {
            g.setColor(new Color(255, 255, 0, 128));
            g.fillRect(((arr - getArrangementOffset()) / 4) * 33,
                ((arr - getArrangementOffset()) % 4) * 33, 32, 32);
        }

        public void setCurrentArrangement(int newArrangement)
        {
            // only fire action performed if new arrangment
            if (currentArrangement != newArrangement)
            {
                scroll.setValue(newArrangement / 4);
                reHightlight(currentArrangement, newArrangement);
                currentArrangement = newArrangement;
                this.fireActionPerformed(new ActionEvent(this,
                    ActionEvent.ACTION_PERFORMED, this.getActionCommand()));
            }
        }

        private void setCurrentArrangement(int x, int y)
        {
            int newArrangement = getArrangementOffset() + ((y / 33))
                + ((x / 33) * 4);
            // only fire action performed if new arrangment
            if (currentArrangement != newArrangement)
            {
                reHightlight(currentArrangement, newArrangement);
                currentArrangement = newArrangement;
                this.fireActionPerformed(new ActionEvent(this,
                    ActionEvent.ACTION_PERFORMED, this.getActionCommand()));
            }
        }

        private void reHightlight(int oldArr, int newArr)
        {
            Graphics g = display.getGraphics();
            if (oldArr >= getArrangementOffset()
                && oldArr < getArrangementOffset() + 60)
            {
                drawArrangement(g, oldArr);
                // g.drawImage(getSelectedTileset().getArrangementImage(oldArr,
                // getCurrentPalette()),
                // ((oldArr - getArrangementOffset()) / 4) * 33,
                // ((oldArr - getArrangementOffset()) % 4) * 33, null);
            }
            highlightArrangement(g, newArr);
            // g.setColor(new Color(255, 255, 0, 128));
            // g.fillRect(((newArr - getArrangementOffset()) / 4) * 33,
            // ((newArr - getArrangementOffset()) % 4) * 33, 32, 32);
        }

        public void paint(Graphics g)
        {
            super.paint(g);
            display.repaint();
        }

        private String actionCommand = new String();

        public String getActionCommand()
        {
            return this.actionCommand;
        }

        public void setActionCommand(String arg0)
        {
            this.actionCommand = arg0;
        }

        public void mouseClicked(MouseEvent me)
        {
            setCurrentArrangement(me.getX(), me.getY());
        }

        public void mousePressed(MouseEvent me)
        {
            setCurrentArrangement(me.getX(), me.getY());
        }

        public void mouseReleased(MouseEvent me)
        {
            setCurrentArrangement(me.getX(), me.getY());
        }

        public void mouseEntered(MouseEvent me)
        {}

        public void mouseExited(MouseEvent me)
        {}

        public void mouseDragged(MouseEvent me)
        {
            if (!(me.getX() < 0 || me.getY() < 0 || me.getX() > (33 * 15) - 1 || me
                .getY() > (33 * 4) - 1))
                setCurrentArrangement(me.getX(), me.getY());
        }

        public void mouseMoved(MouseEvent me)
        {}

        public void adjustmentValueChanged(AdjustmentEvent arg0)
        {
            repaint();
        }

        private JPanel display;
        private JScrollBar scroll;

        public ArrangementSelector()
        {

            display = new JPanel()
            {
                public void paint(Graphics g)
                {
                    if (paletteIsInited && guiInited)
                        g.drawImage(getSelectedTileset().getArrangementsImage(
                            getArrangementOffset(), 15, 4, getCurrentPalette(),
                            Color.BLACK, getCurrentArrangement()), 0, 0, null);
                }
            };
            display
                .setPreferredSize(new Dimension((33 * 15) - 1, (33 * 4) - 1));
            scroll = new JScrollBar(JScrollBar.HORIZONTAL, 0, 15, 0, 255);
            scroll.addAdjustmentListener(this);
            this.setLayout(new BorderLayout());
            this.add(ToolModule.createFlowLayout(display), BorderLayout.CENTER);
            this.add(scroll, BorderLayout.SOUTH);

            display.addMouseListener(this);
            display.addMouseMotionListener(this);
        }
    }
    
    private class CollisionEditor extends AbstractButton implements Undoable, CopyAndPaster, FocusListener {

        private JTextField[][] tf = new JTextField[4][4];
        private boolean reading = false;

        private class CollisionDocumentListener implements DocumentListener
        {
            private int x, y;
            private Component c;

            public CollisionDocumentListener(int x, int y, Component c)
            {
                this.x = x;
                this.y = y;
                this.c = c;
            }

            private void valueChanged()
            {
                if (!reading)
                {
                    addUndo();
                    getSelectedTileset().setCollisionData(
                        getCurrentArrangement(),
                        x,
                        y,
                        (byte) Integer.parseInt(ToolModule.addZeros(tf[x][y]
                            .getText(), 2), 16));
                    setFocus(c);
                }
            }

            public void insertUpdate(DocumentEvent arg0)
            {
                valueChanged();
            }

            public void removeUpdate(DocumentEvent arg0)
            {
                valueChanged();
            }

            public void changedUpdate(DocumentEvent arg0)
            {
                valueChanged();
            }
        }

        public void updateTfs()
        {
            reading = true;
            for (int y = 0; y < 4; y++)
            {
                for (int x = 0; x < 4; x++)
                {
                    tf[x][y].setText(ToolModule.addZeros(Integer
                        .toHexString(getSelectedTileset().getCollisionData(
                            getCurrentArrangement(), x, y) & 0xff), 2));
                }
            }
            reading = false;
        }

        public CollisionEditor()
        {
            this.setLayout(new GridLayout(4, 4));
            for (int y = 0; y < 4; y++)
            {
                for (int x = 0; x < 4; x++)
                {
                    this.add(tf[x][y] = ToolModule.createSizedJTextField(2));
                    tf[x][y].setHorizontalAlignment(SwingConstants.CENTER);
                    tf[x][y].getDocument().addDocumentListener(
                        new CollisionDocumentListener(x, y, this));
                    tf[x][y].addFocusListener(this);
                }
            }
            Dimension d = this.getPreferredSize();
            int size = Math.max(d.height, d.width);
            this.setPreferredSize(new Dimension(size, size));
        }

        private ArrayList undoList = new ArrayList();

        public void addUndo()
        {
            String[][] newUndo = new String[4][4];
            for (int x = 0; x < 4; x++)
                for (int y = 0; y < 4; y++)
                    newUndo[x][y] = tf[x][y].getText();
            undoList.add(newUndo);
        }

        public void undo()
        {
            if (undoList.size() > 0)
            {
                String[][] undo = (String[][]) undoList
                    .get(undoList.size() - 1);
                for (int x = 0; x < 4; x++)
                    for (int y = 0; y < 4; y++)
                        tf[x][y].setText(undo[x][y]);
                undoList.remove(undoList.size() - 1);
            }
        }

        public void resetUndo()
        {
            undoList = new ArrayList();
        }

        private byte[][] cb = null;

        public void copy()
        {
            cb = getSelectedTileset().getCollisionData(getCurrentArrangement());
        }

        public void paste()
        {
            if (cb == null)
                return;
            getSelectedTileset().setCollisionData(getCurrentArrangement(), cb);
            updateCollisionEditor();
        }

        public void delete()
        {
            addUndo();
            getSelectedTileset().setCollisionData(getCurrentArrangement(),
                new byte[4][4]);
            updateTfs();
            // for (int x = 0; x < 4; x++)
            // for (int y = 0; y < 4; y++)
            // tf[x][y].setText("00");
        }

        public void cut()
        {
            copy();
            delete();
        }

        public void focusGained(FocusEvent arg0)
        {
            setFocus(this);
        }

        public void focusLost(FocusEvent arg0)
        {}
    }
    
    private class TileArrangementEditor extends ArrangementEditor
    {
        protected boolean isEditable()
        {
            return true;
        }

        protected int getCurrentTile()
        {
            return tileSelector.getCurrentTile();
        }

        protected void setCurrentTile(int tile)
        {
            tileSelector.setCurrentTile(tile);
        }

        protected int getTilesWide()
        {
            return 4;
        }

        protected int getTilesHigh()
        {
            return 4;
        }

        protected int getTileSize()
        {
            return 8;
        }

        protected int getZoom()
        {
            return 2;
        }

        protected boolean isDrawGridLines()
        {
        	return Ebhack.main.getPrefs().getValueAsBoolean("eb.TileEditor.arrEditor.gridLines");
        }

        protected boolean isGuiInited()
        {
            return guiInited && paletteIsInited;
        }

        protected int getCurrentSubPalette()
        {
            return TileEditor.this.getCurrentSubPalette();
        }

        protected short getArrangementData(int x, int y)
        {
            return getSelectedTileset().getArrangementData(
                getCurrentArrangement(), x, y);
        }

        protected short[][] getArrangementData()
        {
            return getSelectedTileset().getArrangementData(
                getCurrentArrangement());
        }

        protected void setArrangementData(int x, int y, short data)
        {
            getSelectedTileset().setArrangementData(getCurrentArrangement(), x,
                y, data);
        }

        protected void setArrangementData(short[][] data)
        {
            getSelectedTileset().setArrangementData(getCurrentArrangement(),
                data);
        }

        protected Image getArrangementImage(short[][] selection)
        {
            return getSelectedTileset().getArrangementImage(
                getCurrentArrangement(), getCurrentPalette(), getZoom(),
                isDrawGridLines());
        }

        public Image getTileImage(int tile, int subPal, boolean hFlip,
            boolean vFlip)
        {
            return getSelectedTileset().getTileImage(tile, getCurrentPalette(),
                subPal - 2, hFlip, vFlip);
        }
    }
	
    private class MinitileSelector extends TileSelector
    {
        public int getTilesWide()
        {
            return 32;
        }

        public int getTilesHigh()
        {
            return 16;
        }

        public int getTileSize()
        {
            return 8;
        }

        public int getZoom()
        {
            return 2;
        }

        public boolean isDrawGridLines()
        {
        	return Ebhack.main.getPrefs().getValueAsBoolean("eb.TileEditor.tileSelector.gridLines");
        }

        public int getTileCount()
        {
            return 512;
        }

        public Image getTileImage(int tile)
        {
            return getSelectedTileset().getTileImage(tile, getCurrentPalette(),
                getCurrentSubPalette());
        }

        protected boolean isGuiInited()
        {
            return guiInited && paletteIsInited;
        }
    }
    
    private class FocusIndicator extends AbstractButton implements FocusListener, MouseListener {
        // 0 = other FI, 1 = left component, 2 = right component
        private int focus = 1;
        private Component c1, c2;
        private boolean otherUp;
        private FocusIndicator fi;

        public Component getCurrentFocus()
        {
            return (focus == 0 ? fi.getCurrentFocus() : (focus == 1 ? c1 : c2));
        }

        private void cycleFocus()
        {
            focus++;
            if (focus > 2)
                focus = 1;
            fi.setFocus(this);
            repaint();
        }

        private void setFocus(Component c)
        {
            if (c == c1)
                focus = 1;
            else if (c == c2)
                focus = 2;
            else
                focus = 0;
            repaint();
        }

        public void focusGained(FocusEvent fe)
        {
            System.out.println("FocusIndicator.focusGained(FocusEvent)");
            setFocus(fe.getComponent());
            repaint();
        }

        public void focusLost(FocusEvent arg0)
        {}

        public void mouseClicked(MouseEvent me)
        {
            cycleFocus();
        }

        public void mousePressed(MouseEvent arg0)
        {}

        public void mouseReleased(MouseEvent arg0)
        {}

        public void mouseEntered(MouseEvent arg0)
        {}

        public void mouseExited(MouseEvent arg0)
        {}

        public void paint(Graphics g)
        {
            int[] arrowX = new int[]{10, 40, 40, 50, 40, 40, 10};
            int[] arrowY = new int[]{22, 22, 15, 25, 35, 28, 28};

            // flip if arrow should point the other way
            if (focus == 1 || (focus == 0 && otherUp))
            {
                for (int i = 0; i < arrowX.length; i++)
                {
                    arrowX[i] = 50 - arrowX[i];
                }
            }

            if (focus == 0) // switch X and Y for pointing up
                g.fillPolygon(arrowY, arrowX, 7);
            else
                g.fillPolygon(arrowX, arrowY, 7);
        }

        public void setOtherFocusIndicator(FocusIndicator fi)
        {
            this.fi = fi;
            focus = 0;
            fi.addFocusListener(this);
        }

        public FocusIndicator(Component c1, Component c2,
            Component[] otherComponents, boolean otherUp, FocusIndicator fi)
        {
            if (fi != null)
            {
                this.fi = fi;
                this.fi.addFocusListener(this);
            }

            (this.c1 = c1).addFocusListener(this);
            (this.c2 = c2).addFocusListener(this);

            for (int i = 0; i < otherComponents.length; i++)
                otherComponents[i].addFocusListener(this);

            this.otherUp = otherUp;

            this.addMouseListener(this);

            this.setPreferredSize(new Dimension(50, 50));

            this
                .setToolTipText("This arrow points to which component will recive menu commands. "
                    + "Click to change.");
        }
    }
    
    // update/redraw methods
    private void updatePaletteSelector()
    {
        paletteSelector.removeActionListener(this);
        paletteSelector.removeAllItems();
        for (int i = 0; i < getSelectedTileset().getPaletteCount(); i++)
        {
            paletteSelector.addItem(getSelectedTileset().getPalette(i)
                .toString());
        }
        paletteSelector.addActionListener(this);
    }

    private void updateTileSelector()
    {
        tileSelector.repaint();
        if (cbsel != null)
            cbsel.repaint();
    }

    private void updateArrangementSelector()
    {
        arrangementSelector.repaint();
    }

    private void updateCollisionEditor()
    {
        collisionEditor.updateTfs();
    }

    private void updateArrangementEditor()
    {
        arrangementEditor.repaint();
    }

    private void updatePaletteDisplay()
    {
        this.tileDrawingPalette
            .setPalette(getSelectedTileset().getPaletteColors(
                getCurrentPalette(), getCurrentSubPalette()));
        this.tileDrawingPalette.repaint();
    }

    private void updateTileGraphicsEditor()
    {
        // this.tileDrawingArea.setImage(
        // getSelectedTileset().getTileImage(
        // getCurrentTile(),
        // getCurrentPalette(),
        // getCurrentSubPalette(),
        // false));
        this.tileDrawingArea.setImage(getSelectedTileset().getTile(
            getCurrentTile()));
        // this.tileForegroundDrawingArea.setImage(
        // getSelectedTileset().getTileImage(
        // getCurrentTile() ^ 512,
        // getCurrentPalette(),
        // getCurrentSubPalette(),
        // false));
        this.tileForegroundDrawingArea
            .setEnabled(((getCurrentTile() & 511) < 384));
        this.tileForegroundDrawingArea.setImage(getSelectedTileset().getTile(
            getCurrentTile() ^ 512));
    }

    private void resetArrangementUndo()
    {
        collisionEditor.resetUndo();
        arrangementEditor.resetUndo();
    }
    
    // getCurrent methods
    
    private int getCurrentTileset()
    {
        return this.tilesetSelector.getSelectedIndex();
    }

    private Tileset getSelectedTileset()
    {
        return tilesets[getCurrentTileset()];
    }

    private int getCurrentPalette()
    {
        return getSelectedTileset().getPaletteNum(
            this.paletteSelector.getSelectedItem().toString());
    }

    private int getCurrentSubPalette()
    {
        return this.subPaletteSelector.getSelectedIndex();
    }

    private int getCurrentTile()
    {
        return this.tileSelector.getCurrentTile();
        // that and that + 512 are current
    }

    private int getCurrentArrangement()
    {
        return this.arrangementSelector.getCurrentArrangement();
    }
    
    private CopyAndPaster getCurrentCopyAndPaster()
    {
        return (CopyAndPaster) drawingAreaFocusIndicator.getCurrentFocus();
    }

    private Undoable getCurrentUndoable()
    {
        return (Undoable) drawingAreaFocusIndicator.getCurrentFocus();
    }

    private Component getCurrentComponent()
    {
        return drawingAreaFocusIndicator.getCurrentFocus();
    }
    
    // focus changing
    private void setFocus(Component focusedComponent)
    {
        drawingAreaFocusIndicator.setFocus(focusedComponent);
        drawingAreaFocusIndicator.repaint();
        arrangementFocusIndicator.setFocus(focusedComponent);
        arrangementFocusIndicator.repaint();
    }

    private int getFocusNum()
    {
        Component c = getCurrentComponent();
        if (c == this.tileDrawingArea)
            return 1;
        if (c == this.tileForegroundDrawingArea)
            return 2;
        if (c == this.arrangementEditor)
            return 3;
        if (c == this.collisionEditor)
            return 4;
        return 0;
    }

    private void setFocus(int f)
    {
        switch (f)
        {
            case 2:
                setFocus(this.tileForegroundDrawingArea);
                break;
            case 3:
                setFocus(this.arrangementEditor);
                break;
            case 4:
                setFocus(this.collisionEditor);
                break;
            case 1:
            default:
                setFocus(this.tileDrawingArea);
        }
    }

    private void cycleFocus()
    {
        setFocus(getFocusNum() + 1);
    }

    // copy and paste both stuff
    private class Paster
    {
        private byte[][] data1, data2;
        private short[][] arrData;
        private boolean graphicsPaste;

        public Paster(boolean gr, byte[][] data1, byte[][] data2)
        {
            this.graphicsPaste = gr;
            this.data1 = data1;
            this.data2 = data2;
        }

        public Paster(boolean gr, byte[][] data1, short[][] data2)
        {
            this.graphicsPaste = gr;
            this.data1 = data1;
            this.arrData = data2;
        }

        public void paste()
        {
            if (graphicsPaste)
            {
                getSelectedTileset().setTile(getCurrentTile(), data1);
                getSelectedTileset().setTile(getCurrentTile() | 512, data2);
                updateTileGraphicsEditor();
                updateTileSelector();
                updateArrangementEditor();
                updateArrangementSelector();
            }
            else
            {
                getSelectedTileset().setCollisionData(getCurrentArrangement(),
                    data1);
                updateCollisionEditor();
                getSelectedTileset().setArrangementData(
                    getCurrentArrangement(), arrData);
                updateArrangementEditor();
                updateArrangementSelector();
            }
        }
    }
    private Paster arrpaster = null, grpaster = null;

    private void cutBoth()
    {
        copyBoth();
        deleteBoth();
    }

    private void copyBoth()
    {
        if (getCurrentCopyAndPaster() instanceof IntArrDrawingArea)
            grpaster = new Paster(true, getSelectedTileset().getTile(
                getCurrentTile()), getSelectedTileset().getTile(
                getCurrentTile() | 512));
        else
            arrpaster = new Paster(false, getSelectedTileset()
                .getCollisionData(getCurrentArrangement()),
                getSelectedTileset()
                    .getArrangementData(getCurrentArrangement()));
    }

    private void pasteBoth()
    {
        try
        {
            if (getCurrentCopyAndPaster() instanceof IntArrDrawingArea)
                grpaster.paste();
            else
                arrpaster.paste();
        }
        catch (NullPointerException e)
        {}
    }

    private void deleteBoth()
    {
        if (getCurrentCopyAndPaster() instanceof IntArrDrawingArea)
        {
            tileDrawingArea.delete();
            tileForegroundDrawingArea.delete();
        }
        else
        {
            collisionEditor.delete();
            arrangementEditor.delete();
        }
    }

    /** Initialize the clipboard dialog window. */
    private void initCbDia()
    {
        cb = new byte[256][2][8][8];
        cbdia = new JDialog(mainWindow, "Tile Editor Clipboard");
        JButton copy = new JButton("Copy to clipboard"), paste = new JButton(
            "Paste from clipboard");
        copy.setActionCommand("cb_copy");
        copy.addActionListener(this);
        paste.setActionCommand("cb_paste");
        paste.addActionListener(this);
        cbdia.getContentPane().setLayout(new BorderLayout());
        cbdia.getContentPane().add(cbsel = new MinitileSelector()
        {
            public int getTileCount()
            {
                return cb.length;
            }

            public int getTilesWide()
            {
                return 16;
            }

            public Image getTileImage(int i)
            {
                return drawImage(cb[i][0], getSelectedTileset()
                    .getPaletteColors(getCurrentPalette(),
                        getCurrentSubPalette()));
            }
        }, BorderLayout.CENTER);
        cbdia.getContentPane().add(
            createFlowLayout(new JButton[]{copy, paste}), BorderLayout.SOUTH);
        cbdia.pack();
    }
	
	public void init() {
        mainWindow = createBaseWindow(this);
        mainWindow.setTitle(this.getDescription());
        
        // Menu
        JMenuBar mb = new JMenuBar();

        JMenu fileMenu = new JMenu("File");
        fileMenu.setMnemonic('f');

        fileMenu.add(ToolModule.createJMenuItem("Apply Changes", 'y', "ctrl S",
            "apply", this));

        /*fileMenu.addSeparator();

        fileMenu.add(ToolModule.createJMenuItem("Import Minitile...", 'i',
            null, "importTile", this));
        fileMenu.add(ToolModule.createJMenuItem("Export Minitile...", 'e',
            null, "exportTile", this));
        fileMenu.add(ToolModule.createJMenuItem("Import Palette...", 'm', null,
            "importPal", this));
        fileMenu.add(ToolModule.createJMenuItem("Export Palette...", 'o', null,
            "exportPal", this));

        fileMenu.addSeparator();

        fileMenu.add(ToolModule
            .createJMenuItem("Import All Tileset Minitiles...", 'm', null,
                "importTileset", this));
        fileMenu.add(ToolModule
            .createJMenuItem("Export All Tileset Minitiles...", 'x', null,
                "exportTileset", this));
        fileMenu.add(ToolModule.createJMenuItem(
            "Import All Tileset Properties...", 'p', null, "importCollision",
            this));
        fileMenu.add(ToolModule.createJMenuItem(
            "Export All Tileset Properties...", 'r', null, "exportCollision",
            this));

        fileMenu.addSeparator();

        fileMenu.add(ToolModule.createJMenuItem("Import All Tileset Data...",
            't', null, "importAllTileset", this));
        fileMenu.add(ToolModule.createJMenuItem("Export All Tileset Data...",
            's', null, "exportAllTileset", this));

        fileMenu.addSeparator();

        fileMenu.add(ToolModule.createJMenuItem("Import All Tilesets Data...",
            'a', null, "importAll", this));
        fileMenu.add(ToolModule.createJMenuItem("Export All Tilesets Data...",
            'l', null, "exportAll", this));*/

        mb.add(fileMenu);

        JMenu editMenu = ToolModule.createEditMenu(this, true);

        editMenu.add(new JSeparator());

        editMenu.add(ToolModule.createJMenuItem("Cut Both", 'b',
            "ctrl shift X", "cutBoth", this));
        editMenu.add(ToolModule.createJMenuItem("Copy Both", 'o',
            "ctrl shift C", "copyBoth", this));
        editMenu.add(ToolModule.createJMenuItem("Paste Both", 's',
            "ctrl shift V", "pasteBoth", this));
        editMenu.add(ToolModule.createJMenuItem("Delete Both", 'h',
            "shift DELETE", "deleteBoth", this));

        editMenu.addSeparator();

        editMenu.add(createJMenuItem("Show Multi-Clipboard", 'm', "alt M",
            "cb_show", this));

        mb.add(editMenu);

        JMenu optionsMenu = new JMenu("Options");
        optionsMenu.setMnemonic('o');
        optionsMenu.add(new PrefsCheckBox("Enable Tile Selector Grid Lines",
            prefs, "eb.TileEditor.tileSelector.gridLines", false, 't', null,
            "tileSelGridLines", this));
        optionsMenu.add(new PrefsCheckBox(
            "Enable Arrangement Editor Grid Lines", prefs,
            "eb.TileEditor.arrEditor.gridLines", true, 'a', null,
            "arrEdGridLines", this));
        mb.add(optionsMenu);

        JMenu focusMenu = new JMenu("Focus");
        focusMenu.setMnemonic('c');

        focusMenu.add(ToolModule.createJMenuItem("Background Graphics Editor",
            'b', "ctrl B", "bgeFocus", this));
        focusMenu.add(ToolModule.createJMenuItem("Foreground Graphics Editor",
            'f', "ctrl F", "fgeFocus", this));
        focusMenu.add(ToolModule.createJMenuItem(
            "Arrangement Properties Editor", 'p', "ctrl P", "colFocus", this));
        focusMenu.add(ToolModule.createJMenuItem("Arrangement Editor", 'a',
            "ctrl A", "arrFocus", this));

        focusMenu.add(new JSeparator());

        focusMenu.add(ToolModule.createJMenuItem("Cycle Focus", 'y', "ctrl Y",
            "cycFocus", this));

        mb.add(focusMenu);

        mainWindow.setJMenuBar(mb);
        
        JPanel scrolledArea = new JPanel(new BorderLayout());
        JPanel display = new JPanel(new BorderLayout());

        display.add(tileSelector = new MinitileSelector(), BorderLayout.NORTH);
        tileSelector.setActionCommand("tileSelector");
        tileSelector.addActionListener(this);

        display.add(arrangementSelector = new ArrangementSelector(),
            BorderLayout.SOUTH);
        arrangementSelector.setActionCommand("arrangementSelector");
        arrangementSelector.addActionListener(this);

        scrolledArea.add(display, BorderLayout.WEST);
        
        Box edit = new Box(BoxLayout.Y_AXIS);

        Box selectors = new Box(BoxLayout.Y_AXIS);
        // combo boxes
        selectors.add(ToolModule.getLabeledComponent("Tileset: ",
            this.tilesetSelector = ToolModule.createJComboBoxFromArray(
                TileEditor.TILESET_NAMES, false)));
        tilesetSelector.setActionCommand("tilesetSelector");
        tilesetSelector.addActionListener(this);
        selectors.add(ToolModule.getLabeledComponent("Palette: ",
            this.paletteSelector = new JComboBox()));
        paletteSelector.setActionCommand("paletteSelector");
        paletteSelector.addActionListener(this);
        selectors.add(ToolModule.getLabeledComponent("subPalette: ",
            this.subPaletteSelector = new JComboBox()));
        for (int i = 0; i < 6; i++)
        {
            this.subPaletteSelector.addItem(Integer.toString(i));
        }
        subPaletteSelector.setActionCommand("subPaletteSelector");
        subPaletteSelector.addActionListener(this);

        edit.add(pairComponents(selectors, new JLabel(), false));
        // edit.add(Box.createVerticalStrut(70));
        edit.add(Box.createVerticalGlue());
        
        // Tile Editing
        JPanel tileEdit = new JPanel(new FlowLayout());
        this.tileDrawingPalette = new SpritePalette(16, true);
        tileDrawingPalette.setActionCommand("paletteEditor");
        tileDrawingPalette.addActionListener(this);
        this.tileDrawingToolset = new DrawingToolset(this);

        this.tileDrawingArea = new IntArrDrawingArea(this.tileDrawingToolset,
            this.tileDrawingPalette, this);
        tileDrawingArea.setZoom(10);
        tileDrawingArea.setPreferredSize(new Dimension(80, 80));
        tileDrawingArea.setActionCommand("tileDrawingArea");
        this.tileForegroundDrawingArea = new IntArrDrawingArea(
            this.tileDrawingToolset, this.tileDrawingPalette, this);
        tileForegroundDrawingArea.setZoom(10);
        tileForegroundDrawingArea.setPreferredSize(new Dimension(80, 80));
        tileForegroundDrawingArea.setActionCommand("tileForegroundDrawingArea");
        // use same clipboard
        tileForegroundDrawingArea.setIntArrClipboard(tileDrawingArea
            .getIntArrClipboard());

        collisionEditor = new CollisionEditor();
        arrangementEditor = new TileArrangementEditor();
        arrangementEditor.setActionCommand("arrangementEditor");
        arrangementEditor.addActionListener(this);

        this.drawingAreaFocusIndicator = new FocusIndicator(tileDrawingArea,
            tileForegroundDrawingArea, new Component[]{collisionEditor,
                arrangementEditor}, false, null);
        this.arrangementFocusIndicator = new FocusIndicator(collisionEditor,
            arrangementEditor, new Component[]{tileDrawingArea,
                tileForegroundDrawingArea}, true, drawingAreaFocusIndicator);
        this.drawingAreaFocusIndicator
            .setOtherFocusIndicator(arrangementFocusIndicator);

        // background to foreground copy
        JButton bfCopy = new JButton("--> Copy -->");
        bfCopy.setActionCommand("bfCopy");
        bfCopy.addActionListener(this);

        // foreground to background copy
        JButton fbCopy = new JButton("<-- Copy <--");
        fbCopy.setActionCommand("fbCopy");
        fbCopy.addActionListener(this);

        // drawing area buttons
        JPanel daButtons = new JPanel(new BorderLayout());
        daButtons.add(ToolModule
            .createFlowLayout(this.drawingAreaFocusIndicator),
            BorderLayout.CENTER);
        daButtons.add(bfCopy, BorderLayout.NORTH);
        daButtons.add(fbCopy, BorderLayout.SOUTH);

        tileEdit.add(this.tileDrawingArea);
        tileEdit.add(daButtons);
        tileEdit.add(this.tileForegroundDrawingArea);

        edit.add(tileEdit);

        edit.add(ToolModule.createFlowLayout(this.tileDrawingPalette));

        edit.add(Box.createVerticalGlue());

        JPanel arrEdit = new JPanel(new FlowLayout());

        arrEdit.add(collisionEditor);
        arrEdit.add(arrangementFocusIndicator);
        arrEdit.add(arrangementEditor);

        edit.add(arrEdit);
        
        scrolledArea.add(edit, BorderLayout.CENTER);

        Box toolsetBox = new Box(BoxLayout.Y_AXIS);
        toolsetBox.add(tileDrawingToolset);
        toolsetBox.add(Box.createVerticalStrut(200));

        scrolledArea.add(toolsetBox, BorderLayout.EAST);

        //mainWindow.getContentPane().add(new JScrollPane(scrolledArea),
        //    BorderLayout.CENTER);
        mainWindow.getContentPane().add(scrolledArea,
            BorderLayout.CENTER);

        mainWindow.invalidate();
        mainWindow.pack();
        //mainWindow.setSize(300, 400);
        mainWindow.setLocationByPlatform(true);
        mainWindow.validate();
        mainWindow.setResizable(false);
        
		tilesetSelector.setSelectedIndex(0);
	}
	
	public void show() {
		super.show();
		
		mainWindow.setVisible(true);
	}
	
	public void show(Object obj) {
		show();
        if (obj instanceof int[])
        {
            int[] arr = (int[]) obj;
            tilesetSelector.setSelectedIndex(arr[0]);
            paletteSelector.setSelectedIndex(arr[1]);
            arrangementSelector.setCurrentArrangement(arr[2]);
            if (arr.length > 3)
            {
                tileSelector.setCurrentTile(arr[3]);
                if (arr.length > 4)
                {
                    subPaletteSelector.setSelectedIndex(arr[4]);
                }
            }
        }
	}
	
	public void hide() {
		if (isInited)
			mainWindow.setVisible(false);
	}
	
    private void doTilesetSelectAction()
    {
    	// TODO
        /*if (!getSelectedTileset().init())
        {
            guiInited = false;
            Object opt = JOptionPane.showInputDialog(mainWindow,
                "Error decompressing the " + getSelectedTileset().name
                    + " tileset (#" + getCurrentTileset() + ").",
                "Decompression Error", JOptionPane.ERROR_MESSAGE, null,
                new String[]{"Abort", "Retry", "Fail"}, "Retry");
            if (opt == null || opt.equals("Abort"))
            {
                tilesetSelector.setSelectedIndex((tilesetSelector
                    .getSelectedIndex() + 1)
                    % tilesetSelector.getItemCount());
                doTilesetSelectAction();
                return;
            }
            else if (opt.equals("Retry"))
            {
                // mapSelector.setSelectedIndex(mapSelector.getSelectedIndex());
                doTilesetSelectAction();
                return;
            }
            else if (opt.equals("Fail"))
            {
                getSelectedTileset().initToNull();
            }
        }*/
        guiInited = true;

        updatePaletteSelector();
        updateTileSelector();
        updateArrangementSelector();
        resetArrangementUndo();
        updateCollisionEditor();
        arrangementEditor.clearSelection();
        updateArrangementEditor();
        updatePaletteDisplay();
        updateTileGraphicsEditor();
    }
	
	public void load(Project proj) {
		// Read FTS data
		for (int i = 0; i < NUM_TILESETS; i++) {
			tilesets[i] = new Tileset();
			importAllTileset(i, new File(proj.getFilename("eb.TilesetModule", "Tilesets/" + addZeros(i+"",2))));
		}
		
		// Create Map/Drawing Tileset Cache (for convenience of other modules)
		for (int i = 0; i < NUM_MAP_TILESETS; i++) {
			for (int j = 0; j < NUM_TILESETS; j++) {
				if (tilesets[j].hasMapTileset(i)) {
					drawingTilesets[i] = j;
					break;
				}
			}
		}
	}
	
	public static int getDrawTilesetNumber(int mtset) {
		return drawingTilesets[mtset];
	}
	
	public void save(Project proj) {
		// Write FTS data
		if (appliedChanges) {
			for (int i = 0; i < NUM_TILESETS; i++) {
				//System.out.println("saving tileset " + i);
				exportAllTileset(i, new File(proj.getFilename("eb.TilesetModule", "Tilesets/" + addZeros(i+"",2))));
			}
			appliedChanges = false;
		}
	}
	
    public static void importAllTileset(int tileset, File f)
    {
        if (f == null)
            return;
        try
        {
            FileReader in = new FileReader(f);
            char[] cbuf = new char[(int) f.length()];
            in.read(cbuf);
            in.close();
            tilesets[tileset].setAllDataAsString(new String(cbuf));
        }
        catch (FileNotFoundException e)
        {
            System.err.println("Unable to find file to import tileset #" + tileset + " from.");
        }
        catch (IOException e)
        {
            System.err.println("Error reading file to import tileset #" + tileset + " from.");
        }
    }
    
    public static void exportAllTileset(int tileset, File f)
    {
        if (f == null)
            return;
        try
        {
            FileWriter out = new FileWriter(f);
            out.write(tilesets[tileset].getAllDataAsString());
            out.close();
        }
        catch (IOException e)
        {
            System.err.println("Error writing file to export tileset to.");
        }
    }
	
    public static class Tileset
    {
        private byte[][][] tiles; // deinterlaced tiles
        private short[][][] arrangements; // arrangements
        private byte[][][] collision; // collision data
        private ArrayList<Palette> palettes;
        
        public Tileset() {
        	tiles = new byte[1024][8][8];
        	arrangements = new short[1024][4][4];
        	collision = new byte[1024][4][4];
        	palettes = new ArrayList<Palette>();
        }
        
        public void clear() {
        	palettes.clear();
        }
        
        // Image stuff
        /**
         * Returns an image of the specified arrangement using the specified
         * palette. Note that sub-palette is specfied by the arrangement data.
         * 
         * @param arrangement Which arrangement (0-1023).
         * @param palette Number of the palette to use (0-59). Note that there
         *            probably are not 60 palettes.
         * @param selection <code>int[4][4]</code> where -1 means not selected
         *            and other values indicate which tile
         * @param gridLines If true, minitiles are spaced out to put gridlines
         *            between them
         * @return Image of the arrangement using the given palette (32x32 or
         *         35x35).
         */
        public Image getArrangementImage(int arrangement, int palette,
            int[][] selection, float zoom, boolean gridLines)
        {
            BufferedImage out = new BufferedImage((gridLines ? 3 : 0)
                + (int) (32 * zoom), (gridLines ? 3 : 0) + (int) (32 * zoom),
                BufferedImage.TYPE_4BYTE_ABGR_PRE);
            Graphics g = out.getGraphics();
            for (int x = 0; x < 4; x++)
            {
                for (int y = 0; y < 4; y++)
                {
                    int tile = (selection[x][y] == -1
                        ? this.arrangements[arrangement][x][y]
                        : selection[x][y]);
                    g.drawImage(getTileImage(tile & 0x01ff, palette,
                        ((tile & 0x1C00) >> 10) - 2, (tile & 0x4000) != 0,
                        (tile & 0x8000) != 0), (int) (x * 8 * zoom)
                        + (gridLines ? x : 0), (int) (y * 8 * zoom)
                        + (gridLines ? y : 0), (int) (8 * zoom),
                        (int) (8 * zoom), null);
                    if (selection[x][y] != -1)
                    {
                        g.setColor(new Color(255, 255, 0, 128));
                        g.fillRect((int) (x * 8 * zoom) + (gridLines ? x : 0),
                            (int) (y * 8 * zoom) + (gridLines ? y : 0),
                            (int) (8 * zoom), (int) (8 * zoom));
                    }
                }
            }
            return out;
        }

        /**
         * Returns an image of the specified arrangement using the specified
         * palette. Note that sub-palette is specfied by the arrangement data.
         * 
         * @param arrangement Which arrangement (0-1023).
         * @param palette Number of the palette to use (0-59). Note that there
         *            probably are not 60 palettes.
         * @param gridLines If true, minitiles are spaced out to put gridlines
         *            between them
         * @return Image of the arrangement using the given palette (32x32 or
         *         35x35).
         */
        public Image getArrangementImage(int arrangement, int palette,
            float zoom, boolean gridLines)
        {
            BufferedImage out = new BufferedImage((gridLines ? 3 : 0)
                + (int) (32 * zoom), (gridLines ? 3 : 0) + (int) (32 * zoom),
                BufferedImage.TYPE_4BYTE_ABGR_PRE);
            Graphics g = out.getGraphics();
            for (int x = 0; x < 4; x++)
            {
                for (int y = 0; y < 4; y++)
                {
                    g
                        .drawImage(
                            getTileImage(
                                this.arrangements[arrangement][x][y] & 0x01ff,
                                palette,
                                ((this.arrangements[arrangement][x][y] & 0x1C00) >> 10) - 2,
                                (this.arrangements[arrangement][x][y] & 0x4000) != 0,
                                (this.arrangements[arrangement][x][y] & 0x8000) != 0),
                            (int) (x * 8 * zoom) + (gridLines ? x : 0),
                            (int) (y * 8 * zoom) + (gridLines ? y : 0),
                            (int) (8 * zoom), (int) (8 * zoom), null);
                }
            }
            return out;
        }

        /**
         * Returns an image of the specified arrangement using the specified
         * palette. Note that sub-palette is specfied by the arrangement data.
         * 
         * @param arrangement Which arrangement (0-1023).
         * @param palette Number of the palette to use (0-59). Note that there
         *            probably are not 60 palettes.
         * @param gridLines If true, minitiles are spaced out to put gridlines
         *            between them
         * @return Image of the arrangement using the given palette (32x32 or
         *         35x35).
         */
        public Image getArrangementImage(int arrangement, int palette,
            boolean gridLines)
        {
            return getArrangementImage(arrangement, palette, 1, gridLines);
        }

        /**
         * Returns an image of the specified arrangement using the specified
         * palette. Note that sub-palette is specfied by the arrangement data.
         * 
         * @param arrangement Which arrangement (0-1023).
         * @param palette Number of the palette to use (0-59). Note that there
         *            probably are not 60 palettes.
         * @return Image of the arrangement using the given palette (32x32).
         */
        public Image getArrangementImage(int arrangement, int palette)
        {
            return getArrangementImage(arrangement, palette, false);
        }

        /**
         * Returns an image of the specified arrangement using the specified
         * palette. Note that sub-palette is specfied by the arrangement data.
         * 
         * @param arrangement Which arrangement (0-1023).
         * @param palette <code>Color[][]</code> of specical palette to use.
         * @param selection <code>int[4][4]</code> where -1 means not selected
         *            and other values indicate which tile
         * @param gridLines If true, minitiles are spaced out to put gridlines
         *            between them
         * @return Image of the arrangement using the given palette (32x32 or
         *         35x35).
         * @see #getArrangementImage(int, int, int[][], float, boolean)
         */
        public Image getArrangementImage(int arrangement, Color[][] palette,
            int[][] selection, float zoom, boolean gridLines)
        {
            BufferedImage out = new BufferedImage((gridLines ? 3 : 0)
                + (int) (32 * zoom), (gridLines ? 3 : 0) + (int) (32 * zoom),
                BufferedImage.TYPE_4BYTE_ABGR_PRE);
            Graphics g = out.getGraphics();
            for (int x = 0; x < 4; x++)
            {
                for (int y = 0; y < 4; y++)
                {
                    int tile = (selection[x][y] == -1
                        ? this.arrangements[arrangement][x][y]
                        : selection[x][y]);
                    g
                        .drawImage(
                            getTileImage(
                                tile & 0x01ff,
                                palette,
                                ((this.arrangements[arrangement][x][y] & 0x1C00) >> 10) - 2,
                                (tile & 0x4000) != 0, (tile & 0x8000) != 0),
                            (int) (x * 8 * zoom) + (gridLines ? x : 0),
                            (int) (y * 8 * zoom) + (gridLines ? y : 0),
                            (int) (8 * zoom), (int) (8 * zoom), null);
                    if (selection[x][y] != -1)
                    {
                        g.setColor(new Color(255, 255, 0, 128));
                        g.fillRect((int) (x * 8 * zoom) + (gridLines ? x : 0),
                            (int) (y * 8 * zoom) + (gridLines ? y : 0),
                            (int) (8 * zoom), (int) (8 * zoom));
                    }
                }
            }
            return out;
        }

        /**
         * Returns an image of the specified arrangement using the specified
         * palette. Note that sub-palette is specfied by the arrangement data.
         * 
         * @param arrangement Which arrangement (0-1023).
         * @param palette <code>Color[][]</code> of specical palette to use.
         * @param gridLines If true, minitiles are spaced out to put gridlines
         *            between them
         * @return Image of the arrangement using the given palette (32x32 or
         *         35x35).
         */
        public Image getArrangementImage(int arrangement, Color[][] palette,
            float zoom, boolean gridLines)
        {
            BufferedImage out = new BufferedImage((gridLines ? 3 : 0)
                + (int) (32 * zoom), (gridLines ? 3 : 0) + (int) (32 * zoom),
                BufferedImage.TYPE_4BYTE_ABGR_PRE);
            Graphics g = out.getGraphics();
            for (int x = 0; x < 4; x++)
            {
                for (int y = 0; y < 4; y++)
                {
                    g
                        .drawImage(
                            getTileImage(
                                this.arrangements[arrangement][x][y] & 0x01ff,
                                palette,
                                ((this.arrangements[arrangement][x][y] & 0x1C00) >> 10) - 2,
                                (this.arrangements[arrangement][x][y] & 0x4000) != 0,
                                (this.arrangements[arrangement][x][y] & 0x8000) != 0),
                            (int) (x * 8 * zoom) + (gridLines ? x : 0),
                            (int) (y * 8 * zoom) + (gridLines ? y : 0),
                            (int) (8 * zoom), (int) (8 * zoom), null);
                }
            }
            return out;
        }

        /**
         * Returns an image of the specified arrangement using the specified
         * palette. Note that sub-palette is specfied by the arrangement data.
         * 
         * @param arrangement Which arrangement (0-1023).
         * @param palette <code>Color[][]</code> of specical palette to use.
         * @param gridLines If true, minitiles are spaced out to put gridlines
         *            between them
         * @return Image of the arrangement using the given palette (32x32 or
         *         35x35).
         */
        public Image getArrangementImage(int arrangement, Color[][] palette,
            boolean gridLines)
        {
            return getArrangementImage(arrangement, palette, 1, gridLines);
        }

        /**
         * Returns an image of the specified arrangement using the specified
         * palette. Note that sub-palette is specfied by the arrangement data.
         * 
         * @param arrangement Which arrangement (0-1023).
         * @param palette <code>Color[][]</code> of specical palette to use.
         * @return Image of the arrangement using the given palette (32x32).
         */
        public Image getArrangementImage(int arrangement, Color[][] palette)
        {
            return getArrangementImage(arrangement, palette, false);
        }

        /**
         * Returns an image of multiple arrangments.
         * 
         * @see #getArrangementsImage(int, int, int, int)
         * @param start Arrangement to draw in top-left (0-1023).
         * @param width Number of arrangments wide image should be.
         * @param height Number of arrangements high image should be.
         * @param palette Number of the palette to use (0-59). Note that there
         *            probably are not 60 palettes.
         * @param bg Background color for lines between arrangements.
         * @param hightlightedArrangement If this arrangement is drawn, it will
         *            be highlighted in yellow.
         * @return Image of specified arrangements ((33*width)-1 x
         *         (33*height)-1).
         */
        public Image getArrangementsImage(int start, int width, int height,
            int palette, Color bg, int hightlightedArrangement)
        {
            BufferedImage out = new BufferedImage((33 * width) - 1,
                (33 * height) - 1, BufferedImage.TYPE_4BYTE_ABGR_PRE);
            Graphics g = out.getGraphics();
            g.setColor(bg);
            g.fillRect(0, 0, out.getWidth(), out.getHeight());
            for (int x = 0; x < width; x++)
            {
                for (int y = 0; y < height; y++)
                {
                    int arr = start + y + (x * height);
                    if (arr <= 1023 && arr >= 0)
                    {
                        g.drawImage(getArrangementImage(arr, palette), x * 33,
                            y * 33, null);
                        // if (arr == hightlightedArrangement)
                        // {
                        // g.setColor(new Color(255, 255, 0, 128));
                        // g.fillRect(x * 33, y * 33, 32, 32);
                        // }
                    }
                }
            }
            if (hightlightedArrangement >= start
                && hightlightedArrangement >= 0
                && hightlightedArrangement < start + (width * height)
                && hightlightedArrangement < 1024)
            {
                g.setColor(new Color(255, 255, 0, 128));
                g.fillRect(((hightlightedArrangement - start) / height) * 33,
                    ((hightlightedArrangement - start) % height) * 33, 32, 32);
            }
            return out;
        }

        /**
         * Returns an image of multiple arrangments.
         * 
         * @see #getArrangementsImage(int, int, int, int)
         * @param start Arrangement to draw in top-left (0-1023).
         * @param width Number of arrangments wide image should be.
         * @param height Number of arrangements high image should be.
         * @param palette Number of the palette to use (0-59). Note that there
         *            probably are not 60 palettes.
         * @param bg Background color for lines between arrangements.
         * @return Image of specified arrangements ((33*width)-1 x
         *         (33*height)-1).
         */
        public Image getArrangementsImage(int start, int width, int height,
            int palette, Color bg)
        {
            return this.getArrangementsImage(start, width, height, palette, bg,
                -1);
        }

        /**
         * Returns an image of multiple arrangments with a black background.
         * 
         * @see #getArrangementsImage(int, int, int, int, Color)
         * @param start Arrangement to draw in top-left (0-1023).
         * @param width Number of arrangments wide image should be.
         * @param height Number of arrangements high image should be.
         * @param palette Number of the palette to use (0-59). Note that there
         *            probably are not 60 palettes.
         * @return Image of specified arrangements ((33*width)-1 x
         *         (33*height)-1).
         */
        public Image getArrangementsImage(int start, int width, int height,
            int palette)
        {
            return this.getArrangementsImage(start, width, height, palette,
                Color.BLACK);
        }
        
        /**
         * Returns an image of the specified tile using the specified palette.
         * If trueColors is false then the color number = (red & 1) + ((green &
         * 1) &gt;&gt; 1) + ((blue & 3) &gt;&gt; 2), which is a number 0-15.
         * 
         * @param tile Number tile (0-1023).
         * @param palette Number of the palette to use (0-59). Note that there
         *            probably are not 60 palettes.
         * @param subPalette Number of the subpalette to use (0-5).
         * @param hFlip If true, flip output horizontally.
         * @param vFlip If true, flip output vertically.
         * @return Image of the tile (8x8).
         */
        public Image getTileImage(int tile, int palette, int subPalette,
            boolean hFlip, boolean vFlip)
        {
            BufferedImage out = new BufferedImage(8, 8,
                BufferedImage.TYPE_4BYTE_ABGR_PRE);
            Graphics g = out.getGraphics();
            for (int x = 0; x < 8; x++)
            {
                for (int y = 0; y < 8; y++)
                {
                    g.setColor(this.getTilePixel(tile, (hFlip ? 7 - x : x),
                        (vFlip ? 7 - y : y), palette, subPalette));
                    g.drawLine(x, y, x, y);
                    // there's no draw point, WHY?!?
                }
            }
            return out;
        }

        /**
         * Returns an image of the specified tile using the specified palette.
         * If trueColors is false then the color number = (red & 1) + ((green &
         * 1) &gt;&gt; 1) + ((blue & 3) &gt;&gt; 2), which is a number 0-15.
         * 
         * @see #getTileImage(int, int, int, boolean, boolean, boolean)
         * @param tile Number tile (0-1023).
         * @param palette Number of the palette to use (0-59). Note that there
         *            probably are not 60 palettes.
         * @param subPalette Number of the subpalette to use (0-5).
         * @return Image of the tile (8x8).
         */
        public Image getTileImage(int tile, int palette, int subPalette)
        {
            return this.getTileImage(tile, palette, subPalette, false, false);
        }

        /**
         * Returns an image of the specified tile using the specified palette.
         * 
         * @param tile Number tile (0-1023).
         * @param palette <code>Color[][]</code> of special palette to use
         * @param subPalette Number of the subpalette to use (0-5).
         * @param hFlip If true, flip output horizontally.
         * @param vFlip If true, flip output vertically.
         * @return Image of the tile (8x8).
         * @see #getTileImage(int, int, int, boolean, boolean, boolean)
         */
        public Image getTileImage(int tile, Color[][] palette, int subPalette,
            boolean hFlip, boolean vFlip)
        {
            if (subPalette < 0)
                subPalette += 2;
            return ToolModule.drawImage(getTile(tile), palette[subPalette], hFlip, vFlip);
        }
        
        // Tile stuff
        
        public Color getTilePixel(int tile, int x, int y, int palette,
                int subPalette)
        {
        	return this.getPaletteColor(this.getTilePixel(tile, x, y), palette, subPalette);
        }
        
        /**
         * Returns the number of the color of a point on a specific tile.
         * 
         * @param tile Number tile to get color for (0-1023).
         * @param x X-coordinate on the tile (0-7).
         * @param y Y-coordinate on the tile (0-7).
         * @return Number of the color (0-15).
         */
        public byte getTilePixel(int tile, int x, int y)
        {
            return this.tiles[tile][x][y];
        }

        /**
         * Sets the specified pixel to the given color number.
         * 
         * @param tile Number tile to get color for (0-1023).
         * @param x X-coordinate on the tile (0-7).
         * @param y Y-coordinate on the tile (0-7).
         * @param c Number of the color (0-15).
         */
        public void setTilePixel(int tile, int x, int y, byte c)
        {
            this.tiles[tile][x][y] = c;
        }
        
        public byte[][] getTile(int tile)
        {
            byte[][] out = new byte[8][8];
            for (int x = 0; x < 8; x++)
            {
                for (int y = 0; y < 8; y++)
                {
                    out[x][y] = this.getTilePixel(tile, x, y);
                }
            }
            return out;
        }

        public void setTile(int tile, byte[][] in)
        {
            for (int x = 0; x < 8; x++)
            {
                for (int y = 0; y < 8; y++)
                {
                    this.setTilePixel(tile, x, y, in[x][y]);
                }
            }
        }
        
        /**
         * Returns the contents of a tile as a String. Each character is the hex
         * value of the pixel and rows are separated by "\n"'s (newlines).
         * 
         * @see #setTileAsString(int, String)
         * @param tile Which tile to get values for.
         * @return Tile values as a String.
         */
        public String getTileAsString(int tile)
        {
            String out = new String();

            for (int y = 0; y < 8; y++)
            {
                for (int x = 0; x < 8; x++)
                {
                    out += Integer
                        .toHexString(this.getTilePixel(tile, x, y) & 0xff);
                }
            }

            return out;
        }

        /**
         * Sets specified tile based on the given String.
         * 
         * @see #getTileAsString(int)
         * @param tile Which tile to set the values of.
         * @param in String of tile graphics.
         */
        public void setTileAsString(int tile, String in)
        {
            for (int y = 0; y < 8; y++)
            {
                for (int x = 0; x < 8; x++)
                {
                    this.setTilePixel(tile, x, y, (byte) Integer.parseInt(in
                        .substring((y * 8) + x, (y * 8) + x + 1), 16));
                }
            }
        }
    	
        /**
         * Returns the tileset graphics as a String. Each character is the hex
         * value of the pixel and rows are separated by "\n"'s (newlines). There
         * is a blank row to separate tiles. Tiles are in order: 0, 512, 1, 513,
         * etc. (background0, foreground0, background1, foreground1, etc.).
         * 
         * @see #setTilesetAsString(String)
         * @return Tileset graphics values in a String.
         */
        public String getTilesetAsString()
        {
            String out = new String();

            for (int tile = 0; tile < 512; tile++)
            {
                out += (tile != 0 ? "\n\n" : "") + this.getTileAsString(tile)
                    + "\n" + this.getTileAsString(tile ^ 512);
            }

            return out;
        }

        /**
         * Sets tileset graphics based on the given String.
         * 
         * @see #getTilesetAsString()
         * @param in String of tileset graphics.
         */
        public void setTilesetAsString(String in)
        {
            String[] tilesCsv = in.split("\r\n\r\n|\n\n|\r\r");
            for (int tile = 0; tile < tilesCsv.length; tile++)
            {
                String[] tmp = tilesCsv[tile].split("\r\n|\n|\r");
                setTileAsString(tile, tmp[0]);
                setTileAsString(tile ^ 512, tmp[1]);
            }
        }
        
        // Palette stuff
        public static class Palette
        {
            private int mtileset, mpalette;
            private int[][][] colors;

            /**
             * Creates a map palette information object.
             * 
             * @param mtileset the map tileset this palette is associated with
             * @param mpalette the map palette this palette is associated with
             */
            public Palette(int mtileset, int mpalette)
            {
                this.mtileset = mtileset;
                this.mpalette = mpalette;
                colors = new int[6][16][3];
            }
            
            public int getR(int subpal, int n) {
            	if (subpal < 0)
            		subpal = 0;
            	return colors[subpal][n][0];
            }
            
            public int getG(int subpal, int n) {
            	if (subpal < 0)
            		subpal = 0;
            	return colors[subpal][n][1];
            }
            
            public int getB(int subpal, int n) {
            	if (subpal < 0)
            		subpal = 0;
            	return colors[subpal][n][2];
            }
            
            public void setColor(int subpal, int n, int r, int g, int b) {
            	colors[subpal][n][0] = r;
            	colors[subpal][n][1] = g;
            	colors[subpal][n][2] = b;
            }

            /**
             * Returns a String containing the map tileset and palette. For
             * example, "2/4" means map tileset 2, palette 4.
             * 
             * @return a String containing the map tileset, a slash, and then
             *         the map palette
             */
            public String toString()
            {
                return mtileset + "/" + mpalette;
            }

            /**
             * Checks if this has a specific map tileset and palette.
             * 
             * @param mtileset map tileset
             * @param mpalette map palette
             * @return true if this has the same map tileset and palette as the
             *         inputs
             */
            public boolean equals(int mtileset, int mpalette)
            {
                return this.mtileset == mtileset && this.mpalette == mpalette;
            }

            /**
             * Checks if this has the same map tileset and palette as another
             * <code>Palette</code>.
             * 
             * @param other other palette to compare this one to
             * @return true if this has the same map tileset and palette as the
             *         inputted <code>Palette</code>
             */
            public boolean equals(Palette other)
            {
                return equals(other.mtileset, other.mpalette);
            }

            /**
             * Returns false.
             * 
             * @return false
             */
            public boolean equals(Object obj)
            {
                return false;
            }

            /**
             * Returns the number of the map tileset this palette is associated
             * with.
             * 
             * @return the map tileset this palette is associated with
             */
            public int getMapTileset()
            {
                return mtileset;
            }

            /**
             * Returns the number of the map palette this palette is associated
             * with.
             * 
             * @return the map palette this palette is associated with
             */
            public int getMapPalette()
            {
                return mpalette;
            }
        }
        
        /**
         * Returns true if a Palette is present in this Tileset's list of
         * Palettes which has the specified map tileset number.
         * 
         * @param mtset Map tileset ot search for
         * 
         * @return true if this tileset is used for the inputted map tileset
         */
        public boolean hasMapTileset(int mtset) {
            ListIterator<Palette> listIterator = palettes.listIterator();
            while(listIterator.hasNext()) {
            	if (listIterator.next().getMapTileset() == mtset)
            		return true;
            }
            return false;
        }
        
        /**
         * Adds a new palette. Should only be used by TileEditor to init the
         * palettes.
         * 
         * @param palette Palette to add.
         */
        public void addPalette(Palette palette)
        {
            this.palettes.add(palette);
        }
        
        /**
         * Returns the number palettes for this tileset. Note that this may
         * include palettes for multiple map tilesets, but only for this
         * graphics tileset.
         * 
         * @return number of palettes for this tileset
         */
        public int getPaletteCount()
        {
            return palettes.size();
        }
        
        /**
         * Returns the requested palette.
         * 
         * @param number used to identify the palette internally
         * @return the palette
         */
        public Palette getPalette(int palette)
        {
            return (Palette) palettes.get(palette);
        }
        
        /**
         * Finds the internal palette number used for the specified map palette.
         * 
         * @param mtileset map tileset number
         * @param mpalette map palette number
         * @return number used to identify that palette internally
         */
        public int getPaletteNum(int mtileset, int mpalette)
        {
            for (int i = 0; i < getPaletteCount(); i++)
                if (getPalette(i).equals(mtileset, mpalette))
                    return i;
            return -1;
        }

        /**
         * Finds the internal palette number used for the specified map palette.
         * Format for the input is the map tileset number and then the map
         * palette number separated by a forward slash.
         * 
         * @param pal map palette number in the form
         *            <code>mtileset + "/" + mpalette</code>
         * @return number used to identify that palette internally
         */
        public int getPaletteNum(String pal)
        {
            String[] split = pal.split("/");
            int mtileset = Integer.parseInt(split[0]), mpalette = Integer
                .parseInt(split[1]);
            return getPaletteNum(mtileset, mpalette);
        }
        
        /**
         * Returns the <code>Color</code> of a given color number in the
         * specified palette. If trueColors is false then the color number =
         * (red & 1) + ((green & 1) &gt;&gt; 1) + ((blue & 3) &gt;&gt; 2), which
         * is a number 0-15.
         * 
         * @param c Number of the color (0-15).
         * @param palette Number of the palette to use (0-59). Note that there
         *            probably are not 60 palettes.
         * @param subPalette Number of the subpalette to use (0-5).
         * @return Color
         */
        public Color getPaletteColor(int c, int palette, int subPalette)
        {
            /*int col = hm.rom.readMulti(getPalette(palette).getStart(subPalette,
                c), 2) & 0x7fff;
            return new Color(
                ((col & 0x001f) << 3) | (!trueColors ? c & 1 : 0),
                (((col & 0x03e0) >> 5) << 3) | (!trueColors ? (c & 2) >> 1 : 0),
                ((col >> 10) << 3) | (!trueColors ? (c & 0xC) >> 2 : 0));*/
        	Palette p = getPalette(palette);
        	return new Color(p.getR(subPalette, c),
        			p.getG(subPalette, c),
        			p.getB(subPalette, c));
        }

        /**
         * Sets the specified color in the specified palette to the given new
         * color.
         * 
         * @param c Number of the color (1-15).
         * @param palette Number of the palette to use (0-59). Note that there
         *            probably are not 60 palettes.
         * @param subPalette Number of the subpalette to use (0-5).
         * @param r Red
         * @param g Green
         * @param b Blue
         */
        public void setPaletteColor(int c, int palette, int subPalette,
            int r, int g, int b)
        {
            if (c < 1 || c > 15)
            {
                return;
            }

            //hm.rom.writePalette(getPalette(palette).getStart(subPalette, c),
            //    col);
            getPalette(palette).setColor(subPalette, c, r, g, b);
        }
        
        /**
         * Sets the specified color in the specified palette to the given new
         * color.
         * 
         * @param c Number of the color (1-15).
         * @param palette Number of the palette to use (0-59). Note that there
         *            probably are not 60 palettes.
         * @param subPalette Number of the subpalette to use (0-5).
         * @param col New color to set.
         */
        public void setPaletteColor(int c, int palette, int subPalette,
            Color col)
        {
        	setPaletteColor(c, palette, subPalette, col.getRed(), col.getGreen(), col.getBlue());
        }
        
        /**
         * Returns an array of all the <code>Color</code>'s in the specified
         * palette. If trueColors is false then the color number = (red & 1) +
         * ((green & 1) &gt;&gt; 1) + ((blue & 3) &gt;&gt; 2), which is a number
         * 0-15.
         * 
         * @see #getPaletteColor(int, int, int, boolean)
         * @param palette Number of the palette to use (0-59). Note that there
         *            probably are not 60 palettes.
         * @param subPalette Number of the subpalette to use (0-5).
         * @return <code>Color[]</code> of all the color's in a palette.
         */
        public Color[] getPaletteColors(int palette, int subPalette)
        {
            Color[] cols = new Color[16];
            for (int i = 0; i < 16; i++)
            {
                cols[i] = this.getPaletteColor(i, palette, subPalette);
            }
            return cols;
        }
        
        /**
         * Returns a String containing the specified subPalette. Format: Each of
         * 16 colors: red, green, blue each 5 bit values in base 32 (1 character
         * each).
         * 
         * @see #setSubPaletteAsString(int, int, String)
         * @param palette Number of the palette to use (0-59). Note that there
         *            probably are not 60 palettes.
         * @param subPalette Number of the subpalette to use (0-5).
         * @return A 48 character long String containing the specified
         *         subPalette.
         */
        public String getSubPaletteAsString(int palette, int subPalette)
        {
            String out = new String();
            for (int i = 0; i < 16; i++)
            {
                Color tmp = this.getPaletteColor(i, palette, subPalette);
                out += Integer.toString(tmp.getRed() >> 3, 32);
                out += Integer.toString(tmp.getGreen() >> 3, 32);
                out += Integer.toString(tmp.getBlue() >> 3, 32);
            }
            return out;
        }

        /**
         * Sets the specified subPalette according to the given String.
         * 
         * @see #getSubPaletteAsString(int, int)
         * @param palette Number of the palette to use (0-59). Note that there
         *            probably are not 60 palettes.
         * @param subPalette Number of the subpalette to use (0-5).
         * @param in A 48 character long String containing the specified
         *            subPalette.
         */
        public void setSubPaletteAsString(int palette, int subPalette, String in)
        {
            for (int i = 0; i < 16; i++)
            {
                this.setPaletteColor(i, palette, subPalette,
                    Integer.parseInt(in.substring(i * 3, i * 3 + 1), 32) << 3,
                    Integer.parseInt(in.substring(i * 3 + 1, i * 3 + 2), 32) << 3,
                    Integer.parseInt(in.substring(i * 3 + 2, i * 3 + 3), 32) << 3);
            }
        }
        
        /**
         * Returns the specified palette as a String. Format: mtileset in base
         * 32, mpalette in base 32, 6 {@link #getSubPaletteAsString(int, int)}
         * 's.
         * 
         * @see #getSubPaletteAsString(int, int)
         * @see #setPaletteAsString(String)
         * @param palette Number of the palette to use (0-59). Note that there
         *            probably are not 60 palettes.
         * @return A 290 character String containing the specified palette.
         */
        public String getPaletteAsString(int palette)
        {
            String out = new String();

            out += Integer.toString(getPalette(palette).getMapTileset(), 32);
            out += Integer.toString(getPalette(palette).getMapPalette(), 32);
            for (int i = 0; i < 6; i++)
                out += getSubPaletteAsString(palette, i);

            return out;
        }

        /**
         * Sets the specified palette to the values in the String.
         * 
         * @see #setPaletteAsString(String)
         * @param pal A 290 character String containing the specified palette.
         * @param palette which palette number to set
         */
        public void setPaletteAsString(String pal, int palette)
        {
            for (int i = 0; i < 6; i++)
                this.setSubPaletteAsString(palette, i, pal.substring(
                    2 + i * 48, 50 + i * 48));
        }

        /**
         * Sets the palette specified in the String to the values in the String.
         * 
         * @see #getPaletteAsString(int)
         * @param pal A 290 character String containing the specified palette.
         */
        public void setPaletteAsString(String pal)
        {
        	int mtset = Integer.parseInt(pal.substring(0,1), 32),
        	    mpal = Integer.parseInt(pal.substring(1, 2), 32);
            int palette = this.getPaletteNum(mtset, mpal);
            if (palette == -1) {
            	addPalette(new Palette(mtset, mpal));
            }
            setPaletteAsString(pal, palettes.size()-1);
        }
        
        /**
         * Returns all palettes in a single String separated by newlines.
         * 
         * @see #getPaletteAsString(int)
         * @see #setPalettesAsString(String)
         * @return All palettes in a single String separated by newlines.
         */
        public String getPalettesAsString()
        {
            String out = new String();
            for (int i = 0; i < getPaletteCount(); i++)
            {
                out += (i != 0 ? "\n" : "") + this.getPaletteAsString(i);
            }
            return out;
        }

        /**
         * Sets all palettes based on the given String.
         * 
         * @see #getPalettesAsString()
         * @param pal All palettes in a single String separated by newlines.
         */
        public void setPalettesAsString(String pal)
        {
            String[] pals = pal.split("\r\n|\n|\r");
            for (int i = 0; i < pals.length; i++)
                this.setPaletteAsString(pals[i]);
        }
        
        // Arrangement stuff
        
        /**
         * Returns which tile is at the specified position in the specified
         * arrangement. The number contains more than just the tile, see
         * {@link #makeArrangementNumber(int, int, boolean, boolean)}for more
         * information.
         * 
         * @see #makeArrangementNumber(int, int, boolean, boolean)
         * @param arrangement Which arrangement (0-1023).
         * @param x X-coordinate on the arrangement (0-3).
         * @param y Y-coordinate on the arrangement (0-3).
         * @return Which tile & other information.
         */
        public short getArrangementData(int arrangement, int x, int y)
        {
            return this.arrangements[arrangement][x][y];
        }

        /**
         * Sets which tile is at the specified position in the specified
         * arrangement. The data number contains more than just the tile, see
         * {@link #makeArrangementNumber(int, int, boolean, boolean)}for more
         * information.
         * 
         * @see #makeArrangementNumber(int, int, boolean, boolean)
         * @param arrangement Which arrangement (0-1023).
         * @param x X-coordinate on the arrangement (0-3).
         * @param y Y-coordinate on the arrangement (0-3).
         * @param data Which tile & other information.
         */
        public void setArrangementData(int arrangement, int x, int y, short data)
        {
            this.arrangements[arrangement][x][y] = data;
        }
        
        // Array stuff
        /**
         * Returns an int array of the tiles in the specified arrangement. The
         * number contains more than just the tile, see
         * {@link #makeArrangementNumber(int, int, boolean, boolean)}for more
         * information.
         * 
         * @see #makeArrangementNumber(int, int, boolean, boolean)
         * @param arrangement Which arrangement (0-1023).
         * @return int[4][4] of which tile is at the each position & other info.
         */
        public short[][] getArrangementData(int arrangement)
        {
            // Make a new array so you don't have to worry about pointer stuff
            short[][] out = new short[4][4];
            for (int x = 0; x < 4; x++)
            {
                for (int y = 0; y < 4; y++)
                {
                    out[x][y] = this.getArrangementData(arrangement, x, y);
                }
            }
            return out;
        }

        /**
         * Sets arrangement data based on the data in the given int array. The
         * number contains more than just the tile, see
         * {@link #makeArrangementNumber(int, int, boolean, boolean)}for more
         * information.
         * 
         * @see #makeArrangementNumber(int, int, boolean, boolean)
         * @param arrangement Which arrangement (0-1023).
         * @param data int[4][4] of which tile is at the each position & other
         *            info.
         */
        public void setArrangementData(int arrangement, short[][] data)
        {
            for (int x = 0; x < 4; x++)
            {
                for (int y = 0; y < 4; y++)
                {
                    this.setArrangementData(arrangement, x, y, data[x][y]);
                }
            }
        }
        
        /**
         * Returns the collision byte for the specified position in the
         * specified arrangement. Little is known about the collision byte, look
         * for the Tile Editor topic on the forums for information. Any
         * discoveries will be helpful! :)<br>
         * <br>
         * Here is what BlueAntoid has to say about the collision byte (posted
         * 2003-04-07 00:05): <br>
         * By the way, this data is stored as binary, so here are the functions
         * of each bit: <br>
         * <br>[ 80 40 20 10 | 08 04 02 01 ]<br>
         * <br>
         * 80 - Collide (solid) <br>
         * 40 - Unknown? <br>
         * 20 - Unknown? <br>
         * 10 - Activate doors on contact <br>
         * <br>
         * The 08, 04, and 02 bits seem to be some sort of combinant group, not
         * simply the sum of the individual effects. Here are the combinations:
         * <br>
         * <br>
         * [---|000-] (00) - No effect <br>
         * [---|001-] (02) - Unknown? <br>
         * [---|010-] (04) - Sweating and sunstroke <br>
         * [---|011-] (06) - Unknown? <br>
         * [---|100-] (08) - Shallow water <br>
         * [---|101-] (0A) - Unknown? <br>
         * [---|110-] (0C) - Deep water <br>
         * [---|111-] (0E) - Unknown? <br>
         * <br>
         * 01 - Activate Layer-2 graphics (Overhead/floating)
         * 
         * @param arrangement Which arrangement to get collision data on
         *            (0-1023).
         * @param x X-coordinate on the arrangement (0-3).
         * @param y Y-coordinate on the arrangement (0-3).
         * @return Collision byte (0-255).
         */
        public byte getCollisionData(int arrangement, int x, int y)
        {
            return this.collision[arrangement][x][y];
        }

        /**
         * Sets the collision byte for the specified position in the specified
         * arrangement. Little is known about the collision byte, look for the
         * Tile Editor topic on the forums for information. Any discoveries will
         * be helpful! :)
         * 
         * @see #getCollisionData(int, int, int)
         * @param arrangement Which arrangement to set collision data of
         *            (0-1023).
         * @param x X-coordinate on the arrangement (0-3).
         * @param y Y-coordinate on the arrangement (0-3).
         * @param collision Collision byte (0-255).
         */
        public void setCollisionData(int arrangement, int x, int y,
            byte collision)
        {
            this.collision[arrangement][x][y] = collision;
        }
        
        // Array stuff
        /**
         * Returns an array of the collision bytes for the specified
         * arrangement.
         * 
         * @see #getCollisionData(int, int, int)
         * @param arrangement Which arrangement to get collision data on
         *            (0-1023).
         * @return An int[4][4] of collison bytes (0-255).
         */
        public byte[][] getCollisionData(int arrangement)
        {
            // Make a new array so you don't have to worry about pointer stuff
            byte[][] out = new byte[4][4];
            for (int x = 0; x < 4; x++)
            {
                for (int y = 0; y < 4; y++)
                {
                    out[x][y] = this.getCollisionData(arrangement, x, y);
                }
            }
            return out;
        }

        /**
         * Returns an array of the collision bytes for the specified
         * arrangement.
         * 
         * @see #getCollisionData(int, int, int)
         * @see #getCollisionData(int)
         * @param arrangement Which arrangement to get collision data on
         *            (0-1023).
         * @return A byte[16] of collison bytes (0-255).
         */
        public byte[] getCollisionDataFlat(int arrangement)
        {
            // Make a new array so you don't have to worry about pointer stuff
            byte out[] = new byte[16], i = 0;

            for (int y = 0; y < 4; y++)
            {
                for (int x = 0; x < 4; x++)
                {
                    out[i++] = this.getCollisionData(arrangement, x, y);
                }
            }
            return out;
        }

        /**
         * Sets the collision bytes for the specified arrangement to the values
         * in the given array.
         * 
         * @see #getCollisionData(int, int, int)
         * @see #setCollisionData(int, int, int, int)
         * @param arrangement Which arrangement to set collision data of
         *            (0-1023).
         * @param collision An int[4][4] of collison bytes (0-255).
         */
        public void setCollisionData(int arrangement, byte[][] collision)
        {
            for (int x = 0; x < 4; x++)
            {
                for (int y = 0; y < 4; y++)
                {
                    this.setCollisionData(arrangement, x, y, collision[x][y]);
                }
            }
        }
        
        /**
         * Returns specified arrangement as a string with collision data.
         * Format: Reading across, for each position: 4 chars of arrangement
         * data in hex, 2 chars of collision data in hex. Total 6 chars each for
         * 16 positions = 92 chars.
         * 
         * @see #setArrangementAsString(int, String)
         * @param arrangement Which arrangement (0-1023).
         * @return A 92 character String containing arrangement and collision
         *         data.
         */
        public String getArrangementAsString(int arrangement)
        {
            String out = new String();
            for (int y = 0; y < 4; y++)
            {
                for (int x = 0; x < 4; x++)
                {
                    out += ToolModule.addZeros(Integer.toHexString(this
                        .getArrangementData(arrangement, x, y) & 0xffff), 4);
                    out += ToolModule.addZeros(Integer.toHexString(this
                        .getCollisionData(arrangement, x, y) & 0xff), 2);
                }
            }
            return out;
        }

        /**
         * Sets the specified arrangement and collision data based on the given
         * String.
         * 
         * @see #getArrangementAsString(int)
         * @param arrangement Which arrangement (0-1023).
         * @param arr A 92 character String containing arrangement and collision
         *            data.
         */
        public void setArrangementAsString(int arrangement, String arr)
        {
            for (int y = 0; y < 4; y++)
            {
                for (int x = 0; x < 4; x++)
                {
                    this.setArrangementData(arrangement, x, y, (short) Integer
                        .parseInt(arr.substring((y * 4 + x) * 6,
                            (y * 4 + x) * 6 + 4), 16));
                    this.setCollisionData(arrangement, x, y, (byte) Integer
                        .parseInt(arr.substring((y * 4 + x) * 6 + 4,
                            (y * 4 + x) * 6 + 6), 16));
                }
            }
        }

        /**
         * Returns a String containing all arrangement and collision data for
         * this tileset.
         * 
         * @see #getArrangementAsString(int)
         * @see #setArrangementsAsString(String)
         * @return All arrangement & collision data in a single String separated
         *         by newlines.
         */
        public String getArrangementsAsString()
        {
            String out = new String();
            for (int i = 0; i < 1024; i++)
            {
                out += (i == 0 ? "" : "\n") + this.getArrangementAsString(i);
            }
            return out;
        }

        /**
         * Sets all arrangement and collision data for this tileset based on the
         * given String.
         * 
         * @see #getArrangementsAsString()
         * @param arr All arrangement & collision data in a single String
         *            separated by newlines.
         */
        public void setArrangementsAsString(String arr)
        {
            String[] arrs = arr.split("\r\n|\n|\r");
            for (int i = 0; i < arrs.length; i++)
                this.setArrangementAsString(i, arrs[i]);
        }
        
        /**
         * Returns all tileset data (graphics, palettes, arrangements,
         * collision) in a single String. Format: All tile graphics, 3 newlines,
         * all palette info, 3 newlines, all arrangement/collision info.
         * 
         * @see #getTilesetAsString()
         * @see #getPalettesAsString()
         * @see #getArrangementsAsString()
         * @see #setAllDataAsString(String)
         * @return All tileset data in a single String.
         */
        public String getAllDataAsString()
        {
            return this.getTilesetAsString() + "\n\n\n"
                + this.getPalettesAsString() + "\n\n\n"
                + this.getArrangementsAsString() + "\n";
        }

        /**
         * Sets all tileset data (graphics, palettes, arrangements, collision)
         * based on the given String.
         * 
         * @see #setTilesetAsString(String)
         * @see #setPalettesAsString(String)
         * @see #setArrangementsAsString(String)
         * @see #getAllDataAsString()
         * @param all All tileset data in a single String.
         */
        public void setAllDataAsString(String all)
        {
            String[] tmp = all.split("\r\n\r\n\r\n|\n\n\n|\r\r\r");
            this.setTilesetAsString(tmp[0]);
            this.setPalettesAsString(tmp[1]);
            this.setArrangementsAsString(tmp[2]);
        }
    }

    public void actionPerformed(ActionEvent ae)
    {
        // respond to each component
        if (ae.getActionCommand().equals("tilesetSelector"))
        {
            doTilesetSelectAction();
        }
        else if (ae.getActionCommand().equals("paletteSelector"))
        {
            updateTileSelector();
            updateArrangementSelector();
            updateArrangementEditor();
            updatePaletteDisplay();
            updateTileGraphicsEditor();
        }
        else if (ae.getActionCommand().equals("subPaletteSelector"))
        {
            updateTileSelector();
            updatePaletteDisplay();
            updateTileGraphicsEditor();
        }
        else if (ae.getActionCommand().equals("tileSelector"))
        {
            updateTileGraphicsEditor();
            if (!(this.getCurrentComponent() instanceof IntArrDrawingArea))
                setFocus(tileDrawingArea);
        }
        else if (ae.getActionCommand().equals("arrangementSelector"))
        {
            resetArrangementUndo();
            arrangementEditor.clearSelection();
            updateCollisionEditor();
            updateArrangementEditor();
            if (this.getCurrentComponent() instanceof IntArrDrawingArea)
                setFocus(arrangementEditor);
        }
        else if (ae.getActionCommand().equals("arrangementEditor"))
        {
            setFocus((Component) ae.getSource());
            // updateArrangementSelector();
            arrangementSelector.repaintCurrentArrangement();
        }
        else if (ae.getActionCommand().equals("tileDrawingArea"))
        {
            setFocus((Component) ae.getSource());
            getSelectedTileset().setTile(getCurrentTile(),
                tileDrawingArea.getByteArrImage());
            tileSelector.repaintCurrent();
            arrangementSelector.repaintCurrentTile();
            arrangementEditor.repaintCurrentTile();
        }
        else if (ae.getActionCommand().equals("paletteEditor"))
        {
            getSelectedTileset().setPaletteColor(
                tileDrawingPalette.getSelectedColorIndex(),
                getCurrentPalette(), getCurrentSubPalette(),
                tileDrawingPalette.getNewColor());
            updatePaletteDisplay();
            updateTileGraphicsEditor();
            updateTileSelector();
            updateArrangementSelector();
            updateArrangementEditor();
        }
        else if (ae.getActionCommand().equalsIgnoreCase(
            "tileForegroundDrawingArea"))
        {
            setFocus((Component) ae.getSource());
            getSelectedTileset().setTile(getCurrentTile() + 512,
                tileForegroundDrawingArea.getByteArrImage());
        }
        // default window stuff
        else if (ae.getActionCommand().equals("apply"))
        {
        	appliedChanges = true;
        }
        else if (ae.getActionCommand().equals("close"))
        {
            hide();
        }
        // flipping
        else if (ae.getActionCommand().equals("hFlip"))
        {
            if (getCurrentComponent() instanceof ImageDrawingArea)
                ((ImageDrawingArea) getCurrentComponent()).doHFlip();
        }
        else if (ae.getActionCommand().equals("vFlip"))
        {
            if (getCurrentComponent() instanceof ImageDrawingArea)
                ((ImageDrawingArea) getCurrentComponent()).doVFlip();
        }
        // undo
        else if (ae.getActionCommand().equals("undo"))
        {
            getCurrentUndoable().undo();
            getCurrentComponent().repaint();
        }
        // copy&paste stuff
        else if (ae.getActionCommand().equals("cut"))
        {
            getCurrentCopyAndPaster().cut();
            getCurrentComponent().repaint();
        }
        else if (ae.getActionCommand().equals("copy"))
        {
            getCurrentCopyAndPaster().copy();
            getCurrentComponent().repaint();
        }
        else if (ae.getActionCommand().equals("paste"))
        {
            getCurrentCopyAndPaster().paste();
            getCurrentComponent().repaint();
        }
        else if (ae.getActionCommand().equals("delete"))
        {
            getCurrentCopyAndPaster().delete();
            getCurrentComponent().repaint();
        }
        // copy&paste both stuff
        else if (ae.getActionCommand().equals("cutBoth"))
        {
            this.cutBoth();
        }
        else if (ae.getActionCommand().equals("copyBoth"))
        {
            this.copyBoth();
        }
        else if (ae.getActionCommand().equals("pasteBoth"))
        {
            this.pasteBoth();
        }
        else if (ae.getActionCommand().equals("deleteBoth"))
        {
            this.deleteBoth();
        }
        // copy&paste cb stuff
        else if (ae.getActionCommand().equals("cb_show"))
        {
            if (cbdia == null)
                initCbDia();
            cbdia.setVisible(true);
        }
        else if (ae.getActionCommand().equals("cb_copy"))
        {
            int i = cbsel.getCurrentTile();
            byte[][] fg = tileForegroundDrawingArea.getByteArrImage(), bg = tileDrawingArea
                .getByteArrImage();
            for (int x = 0; x < 8; x++)
            {
                System.arraycopy(bg[x], 0, cb[i][0][x], 0, 8);
                System.arraycopy(fg[x], 0, cb[i][1][x], 0, 8);
            }
            cbsel.repaintCurrent();
        }
        else if (ae.getActionCommand().equals("cb_paste"))
        {
            int i = cbsel.getCurrentTile();
            byte[][] fg = new byte[8][8], bg = new byte[8][8];
            for (int x = 0; x < 8; x++)
            {
                System.arraycopy(cb[i][0][x], 0, bg[x], 0, 8);
                System.arraycopy(cb[i][1][x], 0, fg[x], 0, 8);
            }
            tileDrawingArea.paste(bg, true);
            tileDrawingArea.repaint();
            tileForegroundDrawingArea.paste(fg, true);
            tileForegroundDrawingArea.repaint();
        }
        // backgroud <--> foreground copies
        else if (ae.getActionCommand().equals("bfCopy"))
        {
            this.tileForegroundDrawingArea.setImage(this.tileDrawingArea
                .getIntArrImage());
            this.tileForegroundDrawingArea.repaint();
            this.setFocus(tileForegroundDrawingArea);
        }
        else if (ae.getActionCommand().equals("fbCopy"))
        {
            this.tileDrawingArea.setImage(this.tileForegroundDrawingArea
                .getIntArrImage());
            this.tileDrawingArea.repaint();
            this.setFocus(tileDrawingArea);
        }
        // gridline toggle
        else if (ae.getActionCommand().equals("tileSelGridLines"))
        {
            mainWindow.getContentPane().invalidate();
            tileSelector.invalidate();
            tileSelector.resetPreferredSize();
            tileSelector.validate();
            tileSelector.repaint();
            mainWindow.getContentPane().validate();
            if (cbdia != null)
            {
                cbdia.invalidate();
                cbsel.invalidate();
                cbsel.resetPreferredSize();
                cbsel.validate();
                cbsel.repaint();
                cbdia.validate();
                cbdia.pack();
            }
        }
        else if (ae.getActionCommand().equals("arrEdGridLines"))
        {
            mainWindow.getContentPane().invalidate();
            arrangementEditor.invalidate();
            arrangementEditor.resetPreferredSize();
            arrangementEditor.validate();
            arrangementEditor.repaint();
            mainWindow.getContentPane().validate();
        }
        // focus stuff
        else if (ae.getActionCommand().equals("bgeFocus"))
        {
            setFocus(this.tileDrawingArea);
        }
        else if (ae.getActionCommand().equals("fgeFocus"))
        {
            setFocus(this.tileForegroundDrawingArea);
        }
        else if (ae.getActionCommand().equals("colFocus"))
        {
            setFocus(this.collisionEditor);
        }
        else if (ae.getActionCommand().equals("arrFocus"))
        {
            setFocus(this.arrangementEditor);
        }
        else if (ae.getActionCommand().equals("cycFocus"))
        {
            cycleFocus();
        }
        else
        {
            System.err.println("Uncaught action command in eb.TileEditor: "
                + ae.getActionCommand());
        }
    }
	
}