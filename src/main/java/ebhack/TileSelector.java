/*
 * Created on Apr 8, 2004
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

import javax.swing.AbstractButton;

/**
 * TODO Write javadoc for this class
 * 
 * @author AnyoneEB
 */
public abstract class TileSelector extends AbstractButton implements
    MouseListener, MouseMotionListener
{
    /**
     * Used internally to store the number of the currently selected tile.
     */
    protected int currentTile = 0;

    /**
     * Returns the number of tiles wide the tile selector should be.
     * 
     * @return number of tiles wide
     */
    public abstract int getTilesWide();

    /**
     * Returns the number of tiles high the tile selector should be.
     * 
     * @return number of tiles wide
     */
    public abstract int getTilesHigh();

    /**
     * Returns the actual size of each tile in pixels. It is assumed that all
     * tiles are squares. This should probably return 8, but your value may be
     * different.
     * 
     * @return side length of a (square) tile
     */
    public abstract int getTileSize();

    /**
     * Returns the zoom factor. For example if this returns 2, the tiles will be
     * drawn with twice the height and twice the width as actual size.
     * 
     * @return zoom factor
     */
    public abstract int getZoom();

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
     * Returns the number of tiles this allows selection from. This will
     * probably be {@link #getTilesWide()}*{@link #getTilesHigh()}, but not
     * always. The first tile will always be refered to as tile 0, and the last
     * tile as the number this returns minus 1.
     * 
     * @return number of tiles to allow selection from
     */
    public abstract int getTileCount();

    /**
     * Returns an actual size image of the specificed tile. This should be
     * {@link #getTileSize()}pixels square in size.
     * 
     * @param tile number of the tile to get the image of
     * @return <code>Image</code> of tile number <code>tile</code> in the
     *         current palette and subpalette
     */
    public abstract Image getTileImage(int tile);

    /**
     * Returns whether or not this should paint. If this returns false, this
     * will not attempt to paint. This is to prevent errors from the painting
     * method attempting to read from non-inited data or to prevent GUI errors
     * from painting.
     * 
     * @return if true, painting will occur, if false, it will not.
     */
    protected abstract boolean isGuiInited();

    /**
     * Returns the currently selected tile.
     * 
     * @return the current tile
     */
    public int getCurrentTile()
    {
        return currentTile;
    }

    /**
     * Sets the current tile. This also visually shows the specificed tile as
     * highlighted.
     * 
     * @param newTile tile to change selection to
     */
    public void setCurrentTile(int newTile)
    {
        // only fire ActionPerformed if new tile and valid tile number
        if (currentTile != newTile && isValidTile(newTile))
        {
            reHighlight(currentTile, newTile);
            currentTile = newTile;
            this.fireActionPerformed(new ActionEvent(this,
                ActionEvent.ACTION_PERFORMED, this.getActionCommand()));
        }
    }

    private void setCurrentTile(int x, int y)
    {
        setCurrentTile(((y / (getDrawnTileSize())) * getTilesWide())
            + (x / (getDrawnTileSize())));
    }

    /**
     * Returns true if specificed number is a valid tile number.
     * 
     * @param tile tile number to check
     * @return true if within valid range, false if out of range
     */
    protected boolean isValidTile(int tile)
    {
        return tile >= 0 && tile < getTileCount();
    }

    /**
     * Draws a tile in the correct place with the correct zoom. This uses
     * {@link #getTileImage(int)}to get the <code>Image</code> of the tile.
     * 
     * @param g <code>Graphics</code> to draw with
     * @param tile which tile to draw
     */
    protected void drawTile(Graphics g, int tile)
    {
        if (tile >= 0 && tile < getTileCount())
        {
            g.drawImage(getTileImage(tile), (tile % getTilesWide())
                * getDrawnTileSize(), (tile / getTilesWide())
                * getDrawnTileSize(), getDrawnTileSize(false),
                getDrawnTileSize(false), null);
        }
    }

    /**
     * Draws a tile in the correct place with the correct zoom. This uses
     * {@link #getTileImage(int)}to get the <code>Image</code> of the tile.
     * 
     * @param tile which tile to draw
     */
    protected void drawTile(int tile)
    {
        drawTile(this.getGraphics(), tile);
    }

    /**
     * Highlights the specificed tile.
     * 
     * @param g <code>Graphics</code> to draw with
     * @param tile which tile to highlight
     */
    protected void highlightTile(Graphics g, int tile)
    {
        g.setColor(new Color(255, 255, 0, 128));
        g.fillRect((tile % getTilesWide()) * getDrawnTileSize(),
            (tile / getTilesWide()) * getDrawnTileSize(),
            getDrawnTileSize(false), getDrawnTileSize(false));
    }

    /**
     * Highlights the specificed tile.
     * 
     * @param tile which tile to highlight
     */
    protected void highlightTile(int tile)
    {
        highlightTile(this.getGraphics(), tile);
    }

    /**
     * Redraws a deselected tile and highlights a newly selected one.
     * 
     * @param oldTile tile to redraw as not highlighted
     * @param newTile tile to highlight
     * @see #drawTile(int)
     * @see #highlightTile(int)
     */
    protected void reHighlight(int oldTile, int newTile)
    {
        drawTile(oldTile);
        highlightTile(newTile);
    }

    /**
     * Redraws the current tile. Call this after the current tile has been
     * modified.
     */
    public void repaintCurrent()
    {
        drawTile(getCurrentTile());
        highlightTile(getCurrentTile());
    }

    public void paint(Graphics g)
    {
        if (isGuiInited())
        {
            Dimension d = this.getPreferredSize();
            // make image buffer so tile selector doesn't flash black
            Image buffer = this.createImage(d.width, d.height);
            Graphics bg = buffer.getGraphics();
            // black background so grid is black
            // bg.setColor(Color.BLACK);
            // bg.fillRect(0, 0, this.getWidth(), this.getHeight());
            // draw tiles
            for (int tile = 0; tile < getTileCount(); tile++)
                drawTile(bg, tile);
            // draw highlight on current tile
            highlightTile(bg, currentTile);
            // draw buffer to screen
            g.drawImage(buffer, 0, 0, this);
        }
    }

    public void mouseClicked(MouseEvent me)
    {
        setCurrentTile(me.getX(), me.getY());
    }

    public void mousePressed(MouseEvent me)
    {
        setCurrentTile(me.getX(), me.getY());
    }

    public void mouseReleased(MouseEvent me)
    {}

    public void mouseEntered(MouseEvent arg0)
    {}

    public void mouseExited(MouseEvent arg0)
    {}

    public void mouseDragged(MouseEvent me)
    {
        if (!(me.getX() < 0 || me.getY() < 0
            || me.getX() > getTilesWide() * getDrawnTileSize() - 1 || me.getY() > getTilesHigh()
            * getDrawnTileSize() - 1))
            setCurrentTile(me.getX(), me.getY());
    }

    public void mouseMoved(MouseEvent arg0)
    {}

    private String actionCommand = new String();

    /**
     * Returns the action command of this.
     * 
     * @return the action command of this
     */
    public String getActionCommand()
    {
        return this.actionCommand;
    }

    /**
     * Sets the action command of this.
     * 
     * @param ac action command to set
     */
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
        setPreferredSize(new Dimension(getTilesWide() * getDrawnTileSize(),
            getTilesHigh() * getDrawnTileSize()));
    }

    public TileSelector()
    {
        resetPreferredSize();
        this.addMouseListener(this);
        this.addMouseMotionListener(this);
    }
}