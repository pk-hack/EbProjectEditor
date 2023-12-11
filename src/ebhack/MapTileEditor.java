package ebhack;

import javax.swing.*;
import java.awt.*;
import java.awt.event.AdjustmentEvent;
import java.awt.event.AdjustmentListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.geom.Rectangle2D;

public class MapTileSelector extends AbstractButton implements MouseListener,
        AdjustmentListener {
    private int width, height;
    private int tile = 0, mode = 0;
    private JScrollBar scroll;

    private final JComboBox tilesetChooser;
    private final MapDisplay mapDisplay;

    public MapTileSelector(JComboBox tilesetChooser, MapDisplay mapDisplay, int width, int height) {
        super();
        this.tilesetChooser = tilesetChooser;
        this.mapDisplay = mapDisplay;

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
                    message = ToolModule.addZeros(Integer.toString(dtile), 3);
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
                Ebhack.main
                        .showModule(
                                TileEditor.class,
                                new int[]{
                                        TileEditor
                                                .getDrawTilesetNumber(tilesetChooser
                                                .getSelectedIndex()),
                                        mapDisplay
                                                .getSelectedSectorPalNumber(),
                                        tile});
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
