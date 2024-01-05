package ebhack;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.event.ActionEvent;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;

import javax.swing.AbstractButton;
import javax.swing.JColorChooser;

import ebhack.ImageDrawingArea.ColorPalette;

/**
 * A component which allows the user to choose from and change a limited
 * palette. The palette should have an even number of colors, or the last color
 * may not appear. When a color is changed this will fire an ActionEvent so
 * other objects may know to correct their colors accordingly. The color will
 * not actally be changed, but the color the user selected will be accessible
 * through {@link #getNewColor()}and the number of it will be
 * {@link #getSelectedColorIndex()}. If you're looking for a palette for any
 * color, you will need to copy and modify the source of this; I might do so if
 * it's requested.
 * 
 * @author AnyoneEB
 * @see ImageDrawingArea.ColorPalette
 */
public class SpritePalette extends AbstractButton implements ColorPalette,
    MouseListener, MouseMotionListener
{
    private Color[] pal = null;
    private int selectedColor, squareSize, rows, cols;
    private String actionCommand;
    private Color newColor = null;
    private boolean zeroEditable;
    private boolean editable = true;

    /**
     * Constructor for SpritePalette. Assumes 16 colors, square size of 20, and
     * 2 rows.
     */
    public SpritePalette()
    {
        this(16, 20, 2);
    }

    /**
     * Constructor for SpritePalette. Assumes square size of 20 and 2 rows.
     * 
     * @param numCol
     */
    public SpritePalette(int numCol)
    {
        this(numCol, 20, 2);
    }

    /**
     * Creates a SpritePalette with the given number of colors, square size, and
     * rows.
     * 
     * @param numCol number of colors; default is 16
     * @param size Size of each palette square; default is 20
     * @param rows number of rows; default is 2
     */
    public SpritePalette(int numCol, int size, int rows)
    {
        super();
        this.changeSize(size, rows, numCol);
        this.addMouseListener(this);
        this.addMouseMotionListener(this);
        this.setToolTipText("Right-click on a color to change it");
        this.zeroEditable = true;
    }

    /**
     * Creates a SpritePalette with the given zeroEditable setting.
     * 
     * @param zeroEditable If color #0 can be edited, false by default
     */
    public SpritePalette(int numCol, boolean zeroEditable)
    {
        this(numCol);
        this.zeroEditable = zeroEditable;
    }

    /**
     * Updates the size of this palette for the new parameters.
     * 
     * @param numCol number of colors; default is 16
     * @param size Size of each palette square; default is 20
     * @param rows number of rows; default is 2
     * @see #changeSize(int, int)
     */
    public void changeSize(int size, int rows, int numCol)
    {
        this.squareSize = size;
        this.rows = rows;
        this.cols = (int) Math.ceil((float) numCol / (float) rows);
        this.setPreferredSize(new Dimension((squareSize * (cols + 2)) + 1,
            (squareSize * rows) + 1));
        repaint();
    }

    /**
     * Updates the size of this palette for the new parameters. This assumes the
     * current palette size is the number of colors.
     * 
     * @param size Size of each palette square; default is 20
     * @param rows number of rows; default is 2
     * @see #changeSize(int, int, int)
     */
    public void changeSize(int size, int rows)
    {
        changeSize(size, rows, pal.length);
    }

    /**
     * Sets the palette of this to the specified colors. Results may be
     * unpredictable for an array with an odd number of colors. The array index
     * of each color will be it's color number. Two of the same color in the
     * array may mess stuff up.
     * 
     * @param c Array of colors to set.
     */
    public void setPalette(Color[] c)
    {
        if (getSelectedColorIndex() >= c.length)
        {
            setSelectedColorIndex(0);
        }
        this.pal = c;
        this.setPreferredSize(new Dimension(
            (squareSize * ((pal.length / rows) + 2)) + 1,
            (squareSize * rows) + 1));
        this.cols = (int) Math.ceil((float) pal.length / (float) rows);
    }

    /**
     * Returns the palette.
     * 
     * @return The palette as a <code>Color[]</code>
     */
    public Color[] getPalette()
    {
        return pal;
    }

    /**
     * @see net.starmen.pkhack.ImageDrawingArea.ColorPalette#getSelectedColor()
     */
    public Color getSelectedColor()
    {
        return pal[this.getSelectedColorIndex()];
    }

    /**
     * @see net.starmen.pkhack.ImageDrawingArea.ColorPalette#setSelectedColor(Color)
     */
    public void setSelectedColor(Color c)
    {
        this.setSelectedColorIndex(this.getIndexOf(c));
    }

    /**
     * @see net.starmen.pkhack.ImageDrawingArea.ColorPalette#getColorOf(int)
     */
    public Color getColorOf(int c)
    {
        return this.pal[c];
    }

    /**
     * @see net.starmen.pkhack.ImageDrawingArea.ColorPalette#getIndexOf(Color)
     */
    public int getIndexOf(Color c)
    {
        for (int i = 0; i < this.pal.length; i++)
        {
            if (this.pal[i].equals(c))
            {
                return i;
            }
        }
        return 0;
    }

    /**
     * @see net.starmen.pkhack.DrawingArea.Palette#getSelectedColorIndex()
     */
    public int getSelectedColorIndex()
    {
        return this.selectedColor;
    }

    /**
     * @see net.starmen.pkhack.DrawingArea.Palette#setSelectedColorIndex(int)
     */
    public void setSelectedColorIndex(int c)
    {
        if (c >= 0 && pal != null && c < pal.length)
        {
            this.selectedColor = c;
            repaint();
        }
    }

    /**
     * @see java.awt.event.MouseListener#mouseClicked(MouseEvent)
     */
    public void mouseClicked(MouseEvent me)
    {
        // if out of range, don't even look at it
        if (me.getX() < 0 || me.getX() > squareSize * cols || me.getY() < 0
            || me.getY() > squareSize * rows)
            return;
        int x = me.getX() - 1, y = me.getY() - 1;
        if (x < (squareSize * cols))
        {
            // if in area with colors set color to clicked on one

            int newCol = (x / (this.squareSize)) + (y / this.squareSize) * cols;
            // System.out.println(
            // "X: " + me.getX() + " Y: " + me.getY() + " newCol: " + newCol);
            if (newCol < pal.length)
            {
                this.setSelectedColorIndex(newCol);
                if (editable
                    && ((me.getButton() == 3) || ((me.getModifiersEx() & MouseEvent.CTRL_DOWN_MASK) == MouseEvent.CTRL_DOWN_MASK))
                    && (this.getSelectedColorIndex() != 0 || zeroEditable))
                {
                    // if right mouse button clicked
                    Color cc = JColorChooser.showDialog(this,
                        "Select a new color", this.getSelectedColor());
                    if (cc == null)
                        return;
                    this.newColor = new Color(cc.getRed() & 0xf8,
                        cc.getGreen() & 0xf8, cc.getBlue() & 0xf8);
                    this.fireActionPerformed(new ActionEvent(this,
                        ActionEvent.ACTION_PERFORMED, this.getActionCommand()));
                }
            }
        }
    }

    /**
     * @see java.awt.event.MouseListener#mouseEntered(MouseEvent)
     */
    public void mouseEntered(MouseEvent me)
    {}

    /**
     * @see java.awt.event.MouseListener#mouseExited(MouseEvent)
     */
    public void mouseExited(MouseEvent me)
    {}

    /**
     * @see java.awt.event.MouseListener#mousePressed(MouseEvent)
     */
    public void mousePressed(MouseEvent me)
    {}

    /**
     * @see java.awt.event.MouseListener#mouseReleased(MouseEvent)
     */
    public void mouseReleased(MouseEvent me)
    {}

    /*
     * (non-Javadoc)
     * 
     * @see java.awt.event.MouseMotionListener#mouseDragged(java.awt.event.MouseEvent)
     */
    public void mouseDragged(MouseEvent me)
    {
        // if out of range, don't even look at it
        if (me.getX() < 0 || me.getX() > squareSize * cols || me.getY() < 0
            || me.getY() > squareSize * rows)
            return;
        int x = me.getX() - 1, y = me.getY() - 1;
        if (x < squareSize * cols)
        {
            // if in area with colors set color to clicked on one

            int newCol = (x / (this.squareSize)) + (y / this.squareSize) * cols;
            // System.out.println("Selected color #" + newCol);
            if (newCol < pal.length)
                this.setSelectedColorIndex(newCol);
        }
    }

    /*
     * (non-Javadoc)
     * 
     * @see java.awt.event.MouseMotionListener#mouseMoved(java.awt.event.MouseEvent)
     */
    public void mouseMoved(MouseEvent me)
    {}

    /**
     * @see java.awt.Component#paint(Graphics)
     */
    public void paint(Graphics g)
    {
        if (pal == null)
            return;
        g.setColor(Color.BLACK);
        g.fillRect(0, 0, ((pal.length / rows) * squareSize) + 1,
            (rows * squareSize) + 1);
        if (pal == null)
        {
            g.setColor(Color.BLACK);
            for (int i = 0; i < (pal.length / rows); i++)
            {
                g.fillRect(i * squareSize, 0, squareSize - 1, squareSize - 1);
                g.fillRect(i * squareSize, squareSize, squareSize - 1,
                    squareSize - 1);
            }
        }
        else
        {
            for (int r = 0; r < rows; r++)
            {
                for (int c = 0; c < cols; c++)
                {
                    g.setColor(this.getColorOf(c + r * cols));
                    g.fillRect((c * squareSize) + 1, (r * squareSize) + 1,
                        squareSize - 1, squareSize - 1);
                    if (c + r * cols == this.getSelectedColorIndex())
                    {
                        g.setColor(Color.WHITE);
                        g.drawRect(c * squareSize, r * squareSize, squareSize,
                            squareSize);
                    }
                }
            }
        }

        Color col = this.getSelectedColor();
        g.setColor(col);
        g.fillRect((int) ((cols + 0.5) * squareSize),
            (int) (((rows - 1.0) / 2.0) * squareSize), squareSize, squareSize);
        g.setColor(Color.BLACK);
        g.drawRect((int) ((cols + 0.5) * squareSize),
            (int) (((rows - 1.0) / 2.0) * squareSize), squareSize, squareSize);
    }

    /**
     * @see javax.swing.AbstractButton#getActionCommand()
     */
    public String getActionCommand()
    {
        return this.actionCommand;
    }

    /**
     * @see javax.swing.AbstractButton#setActionCommand(String)
     */
    public void setActionCommand(String arg0)
    {
        this.actionCommand = arg0;
    }

    /**
     * Returns the newColor. The red, green, and blue values will each be
     * theValue & 0xe0 because SNES colors only have 3-bit precision (for each:
     * red, green, and blue) and this will be used as in a SNES palette.
     * 
     * @return Color
     */
    public Color getNewColor()
    {
        return newColor;
    }

    /**
     * Sets whether the user can set the colors on this palette.
     * 
     * @param editable If false, the user may not change the colors on the
     *            palette.
     */
    public void setEditable(boolean editable)
    {
        this.editable = editable;

        if (this.editable)
            this.setToolTipText("Right-click on a color to change it");
        else
            this.setToolTipText(null);
    }
}