/*
 * Created on Apr 3, 2004
 */
package ebhack;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Image;
import java.awt.event.ActionEvent;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.util.ArrayList;

import javax.swing.AbstractButton;

/**
 * Abstract class for editing arrangements. Arrangements are a description of in
 * what order graphics tiles should be displayed. In Earthbound they always use
 * the format described in
 * {@see #makeArrangementNumber(int, int, boolean, boolean)}.
 * 
 * @author AnyoneEB
 */
public abstract class ArrangementEditor extends AbstractButton implements
    MouseListener, MouseMotionListener, Undoable, CopyAndPaster
{
    /**
     * Returns the currently selected tile. This is the tile that will be placed
     * when the user does a simple (no modifier keys) left click. Which tile is
     * currently selected is most likely defined by a separate tile selector
     * wiget. This should return the tile number from that wiget.
     * 
     * @return tile to place when user left clicks without modifier keys
     */
    protected abstract int getCurrentTile();

    /**
     * Sets the current tile. This is called by the [shift]+left click action to
     * show the clicked on tile to the user.
     * 
     * @param tile tile number to highlight/select
     */
    protected abstract void setCurrentTile(int tile);

    /**
     * Returns the number of tiles wide the arrangement is.
     * 
     * @return number of tiles wide
     */
    protected abstract int getTilesWide();

    /**
     * Returns the number of tiles high the arrangement is.
     * 
     * @return number of tiles high
     */
    protected abstract int getTilesHigh();

    /**
     * Returns the actual size of each tile in pixels. It is assumed that all
     * tiles are squares. This should probably return 8, but your value may be
     * different.
     * 
     * @return side length of a (square) tile
     */
    protected abstract int getTileSize();

    /**
     * Returns the zoom factor. For example if this returns 2, the arrangement
     * will be drawn with twice the height and twice the width as actual size.
     * 
     * @return zoom factor
     */
    protected abstract int getZoom();

    /**
     * Returns whether or not grid lines should be drawn.
     * 
     * @return if true, grid lines are drawn, if false they are not
     */
    protected abstract boolean isDrawGridLines();

    /**
     * Returns the drawn size of each tile in pixels. That is, the size that the
     * tiles are actually drawn at accounting for zoom and (optionally) grid
     * lines.
     * 
     * @param includeGrid if true, grid lines are included in the tile size.
     *            default is true
     * @return size in pixels that tiles are drawn
     * @see #getDrawnTileSize()
     * @see #getTileSize()
     * @see #getZoom()
     * @see #isDrawGridLines()
     */
    public int getDrawnTileSize(boolean includeGrid)
    {
        return (getTileSize() * getZoom())
            + (isDrawGridLines() && includeGrid ? 1 : 0);
    }

    /**
     * Returns the drawn size of each tile in pixels. That is, the size that the
     * tiles are actually drawn at accounting for zoom and grid lines.
     * 
     * @return size in pixels that tiles are drawn
     * @see #getDrawnTileSize(boolean)
     * @see #getTileSize()
     * @see #getZoom()
     * @see #isDrawGridLines()
     */
    public int getDrawnTileSize()
    {
        return getDrawnTileSize(true);
    }

    /**
     * Returns whether or not this arrangement is editable. If this returns
     * false, all clicks will jump to the clicked on tile using
     * {@link #setCurrentTile(int)}.
     * 
     * @return if true, this arrangement may be modified
     */
    protected abstract boolean isEditable();

    /**
     * Returns whether or not this should paint. If this returns false, this
     * will not attempt to paint. This is to prevent errors from the painting
     * method attempting to read from non-inited data or to prevent GUI errors
     * from painting.
     * 
     * @return if true, painting will occur, if false, it will not.
     */
    protected abstract boolean isGuiInited();

    // protected abstract int getCurrentPalette();

    /**
     * Returns the current sub-palette. This is stored in the arrangement data
     * when a tile is placed.
     * 
     * @return the current sub palette
     */
    protected abstract int getCurrentSubPalette();

    // protected abstract int getCurrentArrangement();

    /**
     * Returns the arrangement data for the specified point. Note that point
     * does not mean it is measured in pixels. (0, 0) is the top-left tile. (1,
     * 0) is the next tile to the left. Important, note that this data in the
     * arrangement format as described in
     * {@see #makeArrangementNumber(int, int, boolean, boolean)}.
     * 
     * @param x x-coordinate of point
     * @param y y-coordinate of point
     * @return arrangement data at (x, y) of current arrangement
     */
    protected abstract short getArrangementData(int x, int y);

    /**
     * Returns the arrangement data for the entire arrangement. Important, note
     * that this data in the arrangement format as described in
     * {@see #makeArrangementNumber(int, int, boolean, boolean)}.
     * 
     * @return arrangement data of entire current arrangement
     */
    protected abstract short[][] getArrangementData();

    /**
     * Sets the arrangement data at the specified point. Note that point does
     * not mean it is measured in pixels. (0, 0) is the top-left tile. (1, 0) is
     * the next tile to the left. Important, note that this data needs to be set
     * in the arrangement format as described in
     * {@see #makeArrangementNumber(int, int, boolean, boolean)}.
     * 
     * @param x x-coordinate of point
     * @param y y-coordinate of point
     * @param data arrangement data to set at (x, y) of current arrangement
     * @see #makeArrangementNumber(int, int, boolean, boolean)
     */
    protected abstract void setArrangementData(int x, int y, short data);

    /**
     * Sets the arrangement data for the entire arrangement. Important, note
     * that this data needs to be set in the arrangement format as described in
     * {@see #makeArrangementNumber(int, int, boolean, boolean)}.
     * 
     * @param data arrangement data of entire current arrangement to set
     * @see #makeArrangementNumber(int, int, boolean, boolean)
     */
    protected abstract void setArrangementData(short[][] data);

    /**
     * Returns an image of the current arrangement. Note that this may need to
     * call other methods for infomation on how to correctly draw like
     * {@link #getZoom()}and {@link #isDrawGridLines()}. The selection is an
     * array the same size as the arrangement. In it, a -1 indicates
     * transparent, that is, it means to draw whatever is in the arrangement at
     * that point. Any number greater than -1 is an arrangement number, and it
     * should be drawn as one, except selection areas should also be highlighted
     * (translucent yellow is normal, but any color could be used).
     * 
     * @param selection area in selection, -1's indicate transparent (no
     *            selection), other numbers are arrangement numbers to be used
     *            instead of the actual arrangement number at that location and
     *            to be highlighted
     * @return an <code>Image</code> of the arrangement this is editing
     */
    // protected abstract Image getArrangementImage(int[][] selection);
    /**
     * Returns an actual size image of the specificed tile. This should be
     * {@link #getTileSize()}pixels square in size.
     * 
     * @param tile number of the tile to get the image of
     * @param subPal subPalette to use when drawing tile
     * @param hFlip if true, draw this horizontally flipped
     * @param vFlip if true, draw this vertically flipped
     * @return <code>Image</code> of tile number <code>tile</code> in the
     *         current palette
     */
    protected abstract Image getTileImage(int tile, int subPal, boolean hFlip,
        boolean vFlip);

    /**
     * Returns an actual size image of the specificed tile. This should be
     * {@link #getTileSize()}pixels square in size. This is just a convenience
     * method for calling {@link getTileImage(int, int, boolean, boolean)}.
     * 
     * @param tile number of the tile to get the image of
     * @param subPal subPalette to use when drawing tile
     * @return <code>Image</code> of tile number <code>tile</code> in the
     *         current palette
     * @see #getTileImage(int, int, boolean, boolean)
     */
    protected Image getTileImage(int tile, int subPal)
    {
        return getTileImage(tile, subPal, false, false);
    }

    /**
     * Describes the currently selected area. This is the same size of the
     * entire arrangement. Any values of -1 indicate no selection. Values above
     * -1 are arrangement values that indicate there is a selection at that
     * point.
     */
    private short[][] selection = new short[getTilesWide()][getTilesHigh()];
    /** Selection used for last draw. */
    private short[][] drawnSelection = new short[getTilesWide()][getTilesHigh()];

    /**
     * Returns an arrangement number based on the input. Note that is does not
     * actually write to number to anything, it just returns it. If you wish to
     * edit an arrangement which has a different format, override this. <br>
     * <br>
     * The arrangement format is binary: <br>
     * <code>VH?S SSTT  TTTT TTTT</code><br>
     * <br>
     * V = vertical flip flag (1 = flip) <br>
     * H = horizonal flip flag (1 = flip) <br>
     * S = sub-palette + 2 (2-7) <br>
     * T = tile number (0-1023) for the lower tiles depending on collision byte. ? =
     * unkown purpose, always 0
     * 
     * @param tile Number tile (0-1023).
     * @param subPalette Number of the subpalette to use (0-5).
     * @param hFlip If true, tile is flipped horizontally in arrangement.
     * @param vFlip If true, tile is flipped vertically in arrangement.
     * @return Number to be stored in arrangement with given information.
     */
    public short makeArrangementNumber(int tile, int subPalette, boolean hFlip,
        boolean vFlip)
    {
        return (short) ((tile & 0x03ff) | (((subPalette + 2) & 7) << 10)
            | (hFlip ? 0x4000 : 0) | (vFlip ? 0x8000 : 0));
    }

    public int getTileOfArr(int arr)
    {
        return arr & 0x03ff;
    }

    public int getSubPalOfArr(int arr)
    {
        return (((arr & 0x1C00) >> 10)) & 7;
    }

    public boolean isArrHFlip(int arr)
    {
        return (arr & 0x4000) != 0;
    }

    public boolean isArrVFlip(int arr)
    {
        return (arr & 0x8000) != 0;
    }

    public short rotateArr(short arr)
    {
        return (short) ((arr + 0x4000) & 0xffff);
    }

    private boolean isTile(int x, int y)
    {
        return x >= 0 && x < getTilesWide() && y >= 0 && y < getTilesHigh();
    }

    private void leftClickAction(int x, int y)
    {
        if (!isEditable())
        {
            leftShiftClickAction(x, y);
            return;
        }
        // put current tile with current subPalette with no flip at clicked
        // on location
        if (isSelectionNull())
        {
            if (isTile(x, y))
                setArrangementData(x, y, makeArrangementNumber(
                    getCurrentTile(), getCurrentSubPalette(), false, false));
        }
        else
        {
            flattenSelection();
        }
        this.repaint();
    }

    private void rightClickAction(int x, int y)
    {
        if (!isEditable())
        {
            leftShiftClickAction(x, y);
            return;
        }
        if (isSelectionNull())
        {
            // add one to flip of current tile (sorta rotation)
            if (isTile(x, y))
                setArrangementData(x, y, rotateArr(getArrangementData(x, y)));
        }
        else
        {
            flattenSelection();
        }
        this.repaint();
    }

    private void leftShiftClickAction(int x, int y)
    {
        // set tile editor to current tile
        if (isTile(x, y))
            setCurrentTile(getTileOfArr(getArrangementData(x, y)));
    }

    private void leftCtrlClickAction(int x, int y)
    {
        if (!isEditable())
        {
            leftShiftClickAction(x, y);
            return;
        }
        if (isTile(x, y))
        {
            if (selection[x][y] == -1 && selt != 1)
            {
                selection[x][y] = getArrangementData(x, y);
            }
            else if (selection[x][y] != -1 && selt != 0)
            {
                setArrangementData(x, y, selection[x][y]);
                selection[x][y] = -1;
            }
            repaint();
        }
    }

    public void mouseClicked(MouseEvent me)
    {
        if ((me.getModifiers() & MouseEvent.ALT_MASK) != 0)
            return;
        addUndo();
        int x = me.getX()
            / (getTileSize() * getZoom() + (isDrawGridLines() ? 1 : 0));
        int y = me.getY()
            / (getTileSize() * getZoom() + (isDrawGridLines() ? 1 : 0));
        if (me.getButton() == MouseEvent.BUTTON1)
        {
            if ((me.getModifiers() & MouseEvent.SHIFT_MASK) != 0)
            {
                leftShiftClickAction(x, y);
            }
            else if (!((me.getModifiers() & MouseEvent.CTRL_MASK) != 0))
            {
                leftClickAction(x, y);
            }
        }
        else if (me.getButton() == MouseEvent.BUTTON3)
        {
            rightClickAction(x, y);
        }
        this.fireActionPerformed(new ActionEvent(this,
            ActionEvent.ACTION_PERFORMED, this.getActionCommand()));
    }

    private int asx, asy, selt = -1;

    public void mousePressed(MouseEvent me)
    {
        if (me.getButton() == MouseEvent.BUTTON1)
        {
            int x = me.getX()
                / (getTileSize() * getZoom() + (isDrawGridLines() ? 1 : 0));
            int y = me.getY()
                / (getTileSize() * getZoom() + (isDrawGridLines() ? 1 : 0));
            if ((me.getModifiers() & MouseEvent.ALT_MASK) != 0)
            {
                asx = x;
                asy = y;
            }
            else if ((me.getModifiers() & MouseEvent.CTRL_MASK) != 0)
            {
                selt = (selection[x][y] == -1 ? 0 : 1);
                leftCtrlClickAction(x, y);
            }
        }
    }

    private short[][] createNewSelection(int nx, int ny)
    {
        int cx = nx - asx, cy = ny - asy;
        short[][] newsel = new short[getTilesWide()][getTilesHigh()];

        for (int x = 0; x < getTilesWide(); x++)
            for (int y = 0; y < getTilesHigh(); y++)
                newsel[x][y] = -1;
        for (int x = 0; x < getTilesWide(); x++)
            for (int y = 0; y < getTilesHigh(); y++)
                if (x + cx >= 0 && x + cx < getTilesWide() && y + cy >= 0
                    && y + cy < getTilesHigh())
                    newsel[x + cx][y + cy] = selection[x][y];
        return newsel;
    }

    public void mouseReleased(MouseEvent me)
    {
        int x = me.getX()
            / (getTileSize() * getZoom() + (isDrawGridLines() ? 1 : 0));
        int y = me.getY()
            / (getTileSize() * getZoom() + (isDrawGridLines() ? 1 : 0));
        if (me.getButton() == MouseEvent.BUTTON1)
        {
            if ((me.getModifiers() & MouseEvent.ALT_MASK) != 0)
            {
                selection = createNewSelection(x, y);
                repaint();
            }
            else if ((me.getModifiers() & MouseEvent.CTRL_MASK) != 0)
            {
                selt = -1;
            }
        }
    }

    public void mouseEntered(MouseEvent me)
    {}

    public void mouseExited(MouseEvent me)
    {}

    /*
     * (non-Javadoc)
     * 
     * @see java.awt.event.MouseMotionListener#mouseDragged(java.awt.event.MouseEvent)
     */
    public void mouseDragged(MouseEvent me)
    {
        int x = me.getX()
            / (getTileSize() * getZoom() + (isDrawGridLines() ? 1 : 0));
        int y = me.getY()
            / (getTileSize() * getZoom() + (isDrawGridLines() ? 1 : 0));
        if ((me.getModifiers() & MouseEvent.ALT_MASK) != 0)
        {
            if (isGuiInited())
            {
                short[][] newSel = createNewSelection(x, y);
                // draw x, draw y
                for (int dx = 0; dx < getTilesWide(); dx++)
                    for (int dy = 0; dy < getTilesHigh(); dy++)
                        if (newSel[dx][dy] != drawnSelection[dx][dy])
                            drawTile(dx, dy, newSel[dx][dy] == -1
                                ? getArrangementData(dx, dy)
                                : newSel[dx][dy], newSel[dx][dy] != -1);
                // this.getGraphics().drawImage(
                // getArrangementImage(newSel), 0, 0,
                // Color.BLACK, null);
                drawnSelection = newSel;
            }
        }
        else if ((me.getModifiers() & MouseEvent.CTRL_MASK) != 0)
        {
            leftCtrlClickAction(x, y);
        }
        else if ((me.getModifiers() & MouseEvent.SHIFT_MASK) != 0)
        {
            leftShiftClickAction(x, y);
        }
    }

    public void mouseMoved(MouseEvent me)
    {}

    /**
     * Draws the specificed tile at (x, y).
     * 
     * @param g <code>Graphics</code> to draw with
     * @param x x-coordinate of tile to draw (measured in tiles, not pixels)
     * @param y y-coordinate of tile to draw (measured in tiles, not pixels)
     * @param arr arrangement data of tile to draw
     */
    private void drawTile(Graphics g, int x, int y, int arr, boolean highlight)
    {
        g.drawImage(getTileImage(getTileOfArr(arr), getSubPalOfArr(arr),
            isArrHFlip(arr), isArrVFlip(arr)), (x * 8 * getZoom())
            + (isDrawGridLines() ? x : 0), (y * 8 * getZoom())
            + (isDrawGridLines() ? y : 0), (8 * getZoom()), (8 * getZoom()),
            null);
        if (highlight)
        {
            g.setColor(new Color(255, 255, 0, 128));
            g.fillRect((x * 8 * getZoom()) + (isDrawGridLines() ? x : 0),
                (y * 8 * getZoom()) + (isDrawGridLines() ? y : 0),
                (8 * getZoom()), (8 * getZoom()));
        }
    }

    /**
     * Draws the specificed tile at (x, y).
     * 
     * @param x x-coordinate of tile to draw (measured in tiles, not pixels)
     * @param y y-coordinate of tile to draw (measured in tiles, not pixels)
     * @param arr arrangement data of tile to draw
     */
    private void drawTile(int x, int y, int arr, boolean highlight)
    {
        drawTile(this.getGraphics(), x, y, arr, highlight);
    }

    /**
     * Draws the tile at (x, y).
     * 
     * @param g <code>Graphics</code> to draw with
     * @param x x-coordinate of tile to draw (measured in tiles, not pixels)
     * @param y y-coordinate of tile to draw (measured in tiles, not pixels)
     */
    private void drawTile(Graphics g, int x, int y)
    {
        drawTile(g, x, y, selection[x][y] == -1
            ? getArrangementData(x, y)
            : selection[x][y], selection[x][y] != -1);
    }

    /**
     * Draws the tile at (x, y).
     * 
     * @param x x-coordinate of tile to draw (measured in tiles, not pixels)
     * @param y y-coordinate of tile to draw (measured in tiles, not pixels)
     */
    private void drawTile(int x, int y)
    {
        drawTile(this.getGraphics(), x, y);
    }

    public void repaintTile(int tile)
    {
        for (int x = 0; x < getTilesWide(); x++)
            for (int y = 0; y < getTilesHigh(); y++)
                if ((getTileOfArr(selection[x][y])) == tile
                    || getTileOfArr(getArrangementData(x, y)) == tile)
                    drawTile(x, y);
    }

    public void repaintCurrentTile()
    {
        repaintTile(getCurrentTile());
    }

    public void paint(Graphics g)
    {
        if (isGuiInited())
        {
            drawnSelection = selection;
            g.setColor(Color.BLACK);
            Dimension d = this.getPreferredSize();
            g.fillRect(0, 0, d.width, d.height);
            for (int x = 0; x < getTilesWide(); x++)
                for (int y = 0; y < getTilesHigh(); y++)
                    drawTile(g, x, y);
            // g
            // .drawImage(getArrangementImage(selection), 0, 0, Color.BLACK,
            // null);
        }
    }
    private String actionCommand = new String();

    public String getActionCommand()
    {
        return this.actionCommand;
    }

    public void setActionCommand(String ac)
    {
        this.actionCommand = ac;
    }

    /**
     * Sets preferred size to correct value. Bases value on the return values of
     * {@link #getTilesWide()},{@link #getTilesHigh()}, and
     * {@link #getDrawnTileSize()}.
     */
    public void resetPreferredSize()
    {
        this.setPreferredSize(new Dimension(
            (getTilesWide() * (getDrawnTileSize())) - 1,
            (getTilesHigh() * (getDrawnTileSize())) - 1));
    }

    public ArrangementEditor()
    {
        clearSelection();

        // this.setPreferredSize(new Dimension((getTilesWide() * (getTileSize()
        // * getZoom() + 1)) - 1,
        // (getTilesHigh() * (getTileSize() * getZoom() + 1)) - 1));
        resetPreferredSize();
        this.addMouseListener(this);
        this.addMouseMotionListener(this);
    }

    private ArrayList undoList = new ArrayList();

    /*
     * (non-Javadoc)
     * 
     * @see net.starmen.pkhack.Undoable#addUndo()
     */
    public void addUndo()
    {
        undoList.add(getArrangementData());
    }

    /*
     * (non-Javadoc)
     * 
     * @see net.starmen.pkhack.Undoable#undo()
     */
    public void undo()
    {
        if (undoList.size() > 0)
        {
            setArrangementData((short[][]) undoList.get(undoList.size() - 1));
            undoList.remove(undoList.size() - 1);
            repaint();
            this.fireActionPerformed(new ActionEvent(this,
                ActionEvent.ACTION_PERFORMED, this.getActionCommand()));
        }
    }

    public void resetUndo()
    {
        undoList = new ArrayList();
    }

    // clipboard stuff
    private short[][] cb = null;

    private boolean isSelectionNull()
    {
        for (int x = 0; x < getTilesWide(); x++)
            for (int y = 0; y < getTilesHigh(); y++)
                if (selection[x][y] != -1)
                    return false;
        return true;
    }

    /**
     * Resets the selection making it empty (nothing selected).
     */
    public void clearSelection()
    {
        for (int x = 0; x < getTilesWide(); x++)
            for (int y = 0; y < getTilesHigh(); y++)
                selection[x][y] = -1;
        repaint();
    }

    /**
     * Merges the selection into the arrangement data. -1's are ignored, for
     * other values the arrangement data is set to what the selection is. Note
     * that the selection is cleared by this method.
     */
    private void flattenSelection()
    {
        for (int x = 0; x < getTilesWide(); x++)
        {
            for (int y = 0; y < getTilesHigh(); y++)
            {
                if (selection[x][y] != -1)
                {
                    setArrangementData(x, y, selection[x][y]);
                    selection[x][y] = -1;
                }
            }
        }
    }

    private boolean isCbSelection()
    {
        for (int x = 0; x < getTilesWide(); x++)
            for (int y = 0; y < getTilesHigh(); y++)
                if (cb[x][y] == -1)
                    return true;
        return false;
    }

    public void copy()
    {
        if (isSelectionNull())
            cb = getArrangementData();
        else
            cb = IntArrDrawingArea.getNewShortImage(selection);
    }

    public void paste()
    {
        if (cb != null)
        {
            addUndo();
            if (isCbSelection())
                selection = IntArrDrawingArea.getNewShortImage(cb);
            else
                setArrangementData(cb);
        }
        repaint();
        this.fireActionPerformed(new ActionEvent(this,
            ActionEvent.ACTION_PERFORMED, this.getActionCommand()));
    }

    public void delete()
    {
        addUndo();
        short tmp = makeArrangementNumber(0, 0, false, false);
        if (isSelectionNull())
        {
            for (int x = 0; x < getTilesWide(); x++)
                for (int y = 0; y < getTilesHigh(); y++)
                    setArrangementData(x, y, tmp);
        }
        else
        {
            for (int x = 0; x < getTilesWide(); x++)
            {
                for (int y = 0; y < getTilesHigh(); y++)
                {
                    if (selection[x][y] != -1)
                    {
                        setArrangementData(x, y, tmp);
                        selection[x][y] = -1;
                    }
                }
            }
        }
        repaint();
        this.fireActionPerformed(new ActionEvent(this,
            ActionEvent.ACTION_PERFORMED, this.getActionCommand()));
    }

    public void cut()
    {
        copy();
        delete();
    }
}