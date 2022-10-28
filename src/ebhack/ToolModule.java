package ebhack;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.FlowLayout;
import java.awt.Graphics;
import java.awt.Image;
import java.awt.event.ActionListener;
import java.awt.image.BufferedImage;
import java.awt.image.PixelGrabber;
import java.awt.image.WritableRaster;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.List;
import java.util.StringTokenizer;

import javax.swing.DefaultComboBoxModel;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JMenu;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.KeyStroke;
import javax.swing.event.ListDataEvent;
import javax.swing.event.ListDataListener;
import javax.swing.filechooser.FileFilter;

public abstract class ToolModule {
	public final static String DEFAULT_BASE_DIR = "src/ebhack/";
	public final static String DEFAULT_DATA_DIR = "src/ebhack/data/";

	protected YMLPreferences prefs;
	protected JFrame mainWindow;
	protected boolean isInited = false;

	public ToolModule(YMLPreferences prefs) {
		this.prefs = prefs;
	}

	public abstract String getDescription();

	public abstract String getVersion();

	public abstract String getCredits();

	public boolean showsInMenu() {
		return true;
	}

	// Inits the GUI
	public abstract void init();

	public abstract void load(Project proj);

	public abstract void save(Project proj);

	public void reset() {
	};

	public void refresh(Project proj) {
	};

	public void show() {
		if (!isInited) {
			isInited = true;
			init();
		}
	}

	public void show(Object in) throws IllegalArgumentException {
		show();
	}

	public abstract void hide();

	public static String toUserString(int i, int base) {
		if (base == 16)
			return "0x" + Integer.toHexString(i);
		else
			return Integer.toString(i);
	}

	public static int parseUserInt(String s) {
		if ((s.length() > 2) && (s.charAt(0) == '0') && (s.charAt(1) == 'x'))
			return Integer.parseInt(s.substring(2), 16);
		else
			return Integer.parseInt(s);
	}

	public static File chooseDirectory(boolean save, String prefStr,
			String defaultDir, String title) {
		JFileChooser jfc;
		String val;
		if (defaultDir != null)
			jfc = new JFileChooser(defaultDir);
		else if ((prefStr != null)
				&& ((val = Ebhack.main.getPrefs().getValue(prefStr)) != null))
			jfc = new JFileChooser(val);
		else
			jfc = new JFileChooser();
		jfc.setDialogTitle(title);
		jfc.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
		jfc.setAcceptAllFileFilterUsed(false);
		if ((save ? jfc.showSaveDialog(null) : jfc.showOpenDialog(null)) == JFileChooser.APPROVE_OPTION) {
			File ret = jfc.getSelectedFile();
			if (prefStr != null)
				Ebhack.main.getPrefs().setValue(prefStr, ret.getParent());
			return jfc.getSelectedFile();
		} else
			return null;
	}

	public static File chooseDirectory(boolean save, String prefStr,
			String title) {
		return chooseDirectory(save, prefStr, null, title);
	}

	public static File chooseFile(boolean save, final String extension,
			final String description, String prefStr, String defaultDir,
			String title) {
		try {
			JFileChooser jfc;
			String val;
			if (defaultDir != null)
				jfc = new JFileChooser(defaultDir);
			else if ((prefStr != null)
					&& ((val = Ebhack.main.getPrefs().getValue(prefStr)) != null))
				jfc = new JFileChooser(new File(val).getParent());
			else if ((val = Ebhack.main.getPrefs().getValue("defaultDir")) != null)
				jfc = new JFileChooser(Ebhack.main.getPrefs().getValue(
						"defaultDir"));
			else
				jfc = new JFileChooser();
			jfc.setDialogTitle(title);
			jfc.setFileFilter(new FileFilter() {
				public boolean accept(File f) {
					if (extension == null)
						return true;
					else if ((f.getAbsolutePath().toLowerCase()
							.endsWith("." + extension) || f.isDirectory())
							&& f.exists()) {
						return true;
					} else
						return false;
				}

				public String getDescription() {
					if (extension == null)
						return description + " (*.*)";
					else
						return description + " (*." + extension + ")";
				}
			});

			if ((save ? jfc.showSaveDialog(null) : jfc.showOpenDialog(null)) == JFileChooser.APPROVE_OPTION) {
				File ret = jfc.getSelectedFile();
				if (prefStr != null)
					Ebhack.main.getPrefs().setValue(prefStr,
							ret.getAbsolutePath());
				return ret;
			} else {
				return null;
			}
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}
	}

	public static File chooseFile(boolean save, final String extension,
			final String description, String prefStr, String title) {
		return chooseFile(save, extension, description, prefStr, null, title);
	}

	/**
	 * If true createJComboBoxFromArray() and getNumberedString() default to hex
	 * numbers. If false, they use decimal. Defaults to true.
	 * 
	 * @see #getNumberedString(String, int)
	 * @see #createJComboBoxFromArray(Object[])
	 * @see #createJComboBoxFromArray(Object[], JComboBox)
	 */
	public static boolean getUseHexNumbers() {
		return Ebhack.main.getPrefs().getValueAsBoolean("useHexNumbers");
	}

	/**
	 * If true createJComboBoxFromArray() and getNumberedString() default to hex
	 * numbers. If false, they use decimal. Defaults to true.
	 * 
	 * @see #getNumberedString(String, int)
	 * @see #createJComboBoxFromArray(Object[])
	 * @see #createJComboBoxFromArray(Object[], JComboBox)
	 * @param useHexNumbers
	 *            If true, functions output hex strings.
	 */
	public static void setUseHexNumbers(boolean useHexNumbers) {
		Ebhack.main.getPrefs()
				.setValueAsBoolean("useHexNumbers", useHexNumbers);
	}

	/**
	 * Draws the image in the specified <code>byte[][]</code> with the specified
	 * palette. Assumes that pixel (x, y) of the image is the color
	 * <code>palette(image[x][y])</code>.
	 * 
	 * @param image
	 *            <code>byte[][]</code> of color numbers in <code>int[x][y] form
	 * @param palette
	 *            <code>Color[]</code> for converting color numbers to colors
	 * @return An <code>Image</code> drawn in the specified palette.
	 */
	public static BufferedImage drawImage(byte[][] image, Color[] palette) {
		// BufferedImage out = new BufferedImage(image.length, image[0].length,
		// BufferedImage.TYPE_INT_ARGB);
		// Graphics g = out.getGraphics();
		// for (int x = 0; x < image.length; x++)
		// {
		// for (int y = 0; y < image[0].length; y++)
		// {
		// g.setColor(palette[image[x][y]]);
		// g.drawLine(x, y, x, y);
		// }
		// }
		// return out;
		return drawImage(image, palette, false, false);
	}

	/**
	 * Draws the image in the specified <code>byte[][]</code> with the specified
	 * palette. Assumes that pixel (x, y) of the image is the color
	 * <code>palette(image[x][y])</code>.
	 * 
	 * @param image
	 *            <code>byte[][]</code> of color numbers in <code>int[x][y] form
	 * @param palette
	 *            <code>Color[]</code> for converting color numbers to colors
	 * @param hFlip
	 *            if true, output will be horizontally flipped
	 * @param vFlip
	 *            if true, output will be vertically flipped
	 * @return An <code>Image</code> drawn in the specified palette.
	 */
	public static BufferedImage drawImage(byte[][] image, Color[] palette,
			boolean hFlip, boolean vFlip) {
		BufferedImage out = new BufferedImage(image.length, image[0].length,
				BufferedImage.TYPE_INT_ARGB);
		WritableRaster r = out.getRaster();
		for (int x = 0; x < image.length; x++) {
			for (int y = 0; y < image[0].length; y++) {
				int colIndex = image[hFlip ? image.length - x - 1 : x][vFlip ? image[0].length
						- y - 1
						: y] & 0xff;
				Color c = palette[colIndex];
				r.setPixel(
						x,
						y,
						new int[] { c.getRed(), c.getGreen(), c.getBlue(),
								c.getAlpha() });
			}
		}
		return out;
	}

	/**
	 * Draws the image in the specified <code>int[][]</code> with the specified
	 * palette. Assumes that pixel (x, y) of the image is the color
	 * <code>palette(image[x][y])</code>.
	 * 
	 * @param image
	 *            <code>int[][]</code> of color numbers in <code>int[x][y] form
	 * @param palette
	 *            <code>Color[]</code> for converting color numbers to colors
	 * @return An <code>Image</code> drawn in the specified palette.
	 */
	public static BufferedImage drawImage(int[][] image, Color[] palette) {
		// BufferedImage out = new BufferedImage(image.length, image[0].length,
		// BufferedImage.TYPE_INT_ARGB);
		// Graphics g = out.getGraphics();
		// for (int x = 0; x < image.length; x++)
		// {
		// for (int y = 0; y < image[0].length; y++)
		// {
		// g.setColor(palette[image[x][y]]);
		// g.drawLine(x, y, x, y);
		// }
		// }
		// return out;
		return drawImage(image, palette, false, false);
	}

	/**
	 * Draws the image in the specified <code>int[][]</code> with the specified
	 * palette. Assumes that pixel (x, y) of the image is the color
	 * <code>palette(image[x][y])</code>.
	 * 
	 * @param image
	 *            <code>int[][]</code> of color numbers in <code>int[x][y] form
	 * @param palette
	 *            <code>Color[]</code> for converting color numbers to colors
	 * @param hFlip
	 *            if true, output will be horizontally flipped
	 * @param vFlip
	 *            if true, output will be vertically flipped
	 * @return An <code>Image</code> drawn in the specified palette.
	 */
	public static BufferedImage drawImage(int[][] image, Color[] palette,
			boolean hFlip, boolean vFlip) {
		BufferedImage out = new BufferedImage(image.length, image[0].length,
				BufferedImage.TYPE_INT_ARGB);
		Graphics g = out.getGraphics();
		for (int x = 0; x < image.length; x++) {
			for (int y = 0; y < image[0].length; y++) {
				g.setColor(palette[image[hFlip ? image.length - x - 1 : x][vFlip ? image[0].length
						- y - 1
						: y]]);
				g.drawLine(x, y, x, y);
			}
		}
		return out;
	}

	public static JMenuItem createJMenuItem(String name, char m, String key,
			String ac, ActionListener al) {
		JMenuItem j = new JMenuItem(name);
		if (m != ' ')
			j.setMnemonic(m);
		if (key != null)
			j.setAccelerator(KeyStroke.getKeyStroke(key));
		j.setActionCommand(ac);
		j.addActionListener(al);
		return j;
	}

	/**
	 * Creates a new <code>JFrame</code> with "Apply Changes" and "Close"
	 * buttons. The <code>JFrame</code>'s content pane has a
	 * <code>BorderLayout</code> with the south used. In the south there are two
	 * buttons: an "Apply Changes" button with the action command "apply" and a
	 * "Close" button with the action command "close". Both
	 * <code>addActionListener(al)</code>.
	 * 
	 * @param al
	 *            <code>ActionListener</code> the buttons register.
	 * @return A <code>JFrame</code> with "Apply Changes" and "Close" buttons.
	 */
	public static JFrame createBaseWindow(ActionListener al) {
		JFrame out = new JFrame();
		out.setLocationRelativeTo(Ebhack.main.getMainWindow());
		out.getContentPane().setLayout(new BorderLayout());

		JPanel buttons = new JPanel();
		buttons.setLayout(new FlowLayout());

		JButton apply = new JButton("Apply Changes");
		apply.setActionCommand("apply");
		apply.addActionListener(al);
		buttons.add(apply);
		JButton close = new JButton("Close");
		close.setActionCommand("close");
		close.addActionListener(al);
		buttons.add(close);

		out.getContentPane().add(buttons, BorderLayout.SOUTH);

		return out;
	}

	/**
	 * Creates a <code>JTextField</code> with a maximum length. This
	 * <code>JTextField</code> uses a
	 * {@link net.starmen.pkhack.MaxLengthDocument}or a
	 * {@link net.starmen.pkhack.NumericMaxLengthDocument}.
	 * 
	 * @param len
	 *            Maximum length for the <code>JTextField</code>
	 * @param numbersOnly
	 *            if true, a <code>NumericMaxLengthDocument</code> is used to
	 *            allow only numbers to be typed
	 * @param hex
	 *            True for hex numbers, false for decimal
	 * @return A new <code>JTextField</code>, which limits the number of input
	 *         characters to <code>len</code>
	 * @see net.starmen.pkhack.MaxLengthDocument
	 * @see NumericMaxLengthDocument
	 */
	public static JTextField createSizedJTextField(int len,
			boolean numbersOnly, boolean hex) {
		return new JTextField(numbersOnly ? new NumericMaxLengthDocument(len,
				hex ? "[^0-9a-fA-F]" : null) : new MaxLengthDocument(len), "",
				len);
	}

	/**
	 * Creates a <code>JTextField</code> with a maximum length. This
	 * <code>JTextField</code> uses a
	 * {@link net.starmen.pkhack.MaxLengthDocument}or a
	 * {@link net.starmen.pkhack.NumericMaxLengthDocument}.
	 * 
	 * @param len
	 *            Maximum length for the <code>JTextField</code>
	 * @param numbersOnly
	 *            if true, a <code>NumericMaxLengthDocument</code> is used to
	 *            allow only numbers to be typed
	 * @return A new <code>JTextField</code>, which limits the number of input
	 *         characters to <code>len</code>
	 * @see net.starmen.pkhack.MaxLengthDocument
	 * @see NumericMaxLengthDocument
	 */
	public static JTextField createSizedJTextField(int len, boolean numbersOnly) {
		return createSizedJTextField(len, numbersOnly, false);
	}

	/**
	 * Creates a <code>JTextField</code> with a maximum length. This
	 * <code>JTextField</code> uses a
	 * {@link net.starmen.pkhack.MaxLengthDocument}.
	 * 
	 * @param len
	 *            Maximum length for the <code>JTextField</code>
	 * @return A new <code>JTextField</code>, which limits the number of input
	 *         characters to <code>len</code>
	 * @see net.starmen.pkhack.MaxLengthDocument
	 */
	public static JTextField createSizedJTextField(int len) {
		return createSizedJTextField(len, false);
	}

	/**
	 * Wraps a component in a <code>JPanel</code> with a <code>JLabel</code> and
	 * that component. This is the same as calling
	 * <code>pairComponents(new JLabel(label), comp, true)</code>, except this
	 * sets {@link JLabel#setLabelFor(java.awt.Component)}to <code>comp</code>
	 * 
	 * @param label
	 *            <code>String</code> to label component with
	 * @param comp
	 *            Component to label
	 * @return A <code>JPanel</code> containing a <code>JLabel</code> and the
	 *         component.
	 * @see #pairComponents(JComponent, JComponent, boolean)
	 */
	public static JPanel getLabeledComponent(String label, JComponent comp) // useful
	// for
	// making
	// layouts
	{
		return getLabeledComponent(label, comp, null);
	}

	/**
	 * Wraps a component in a <code>JPanel</code> with a <code>JLabel</code> and
	 * that component both with a tooltip. This is the same as calling
	 * <code>pairComponents(new JLabel(label), comp, true, tooltip, tooltip)</code>
	 * , except this sets {@link JLabel#setLabelFor(java.awt.Component)}to
	 * <code>comp</code>
	 * 
	 * @param label
	 *            <code>String</code> to label component with
	 * @param comp
	 *            Component to label
	 * @param tooltip
	 *            Tooltip to use. Set for both the label and the component.
	 * @return A <code>JPanel</code> containing a <code>JLabel</code> and the
	 *         component both with a tooltip.
	 * @see #pairComponents(JComponent, JComponent, boolean, String)
	 */
	public static JPanel getLabeledComponent(String label, JComponent comp,
			String tooltip) // useful for making layouts
	{
		/*
		 * return pairComponents(new JLabel(), pairComponents(new JLabel(label),
		 * comp, true, tooltip), false);
		 */
		JLabel l = new JLabel(label);
		l.setLabelFor(comp);
		return pairComponents(l, comp, true, tooltip);
	}

	/**
	 * Wraps two components into a single <code>JPanel</code>. The
	 * <code>JPanel</code> uses a <code>BorderLayout</code>.
	 * 
	 * @param comp1
	 *            Component to put in west or north.
	 * @param comp2
	 *            Component to put in east, south, or center
	 * @param isHorizontal
	 *            If true west and east are used, if false north and south are
	 *            used.
	 * @param stretch
	 *            If true center is used for <code>comp2</code> instead of east
	 *            or south.
	 * @param tooltip1
	 *            Tooltip to set for <code>comp1</code>. If <code>null</code> no
	 *            tooltip is set.
	 * @param tooltip2
	 *            Tooltip to set for <code>comp2</code>. If <code>null</code> no
	 *            tooltip is set.
	 * @return A <code>JPanel</code> containing the two components using a
	 *         <code>BorderLayout</code>
	 */
	public static JPanel pairComponents(JComponent comp1, JComponent comp2,
			boolean isHorizontal, boolean stretch, String tooltip1,
			String tooltip2) {
		JPanel out = new JPanel();
		out.setLayout(new BorderLayout());
		if (comp1 == null)
			comp1 = new JLabel();
		if (comp2 == null)
			comp2 = new JLabel();
		comp1.setMaximumSize(comp1.getPreferredSize());
		if (tooltip1 != null) {
			comp1.setToolTipText(tooltip1);
		}
		out.add(comp1, (isHorizontal ? BorderLayout.WEST : BorderLayout.NORTH));
		comp2.setMaximumSize(comp2.getPreferredSize());
		if (tooltip2 != null) {
			comp2.setToolTipText(tooltip2);
		}
		out.add(comp2, (stretch ? BorderLayout.CENTER
				: (isHorizontal ? BorderLayout.EAST : BorderLayout.SOUTH)));
		return out;
	}

	/**
	 * Wraps two components into a single <code>JPanel</code>. The
	 * <code>JPanel</code> uses a <code>BorderLayout</code>.
	 * 
	 * @param comp1
	 *            Component to put in west or north.
	 * @param comp2
	 *            Component to put in east, south, or center
	 * @param isHorizontal
	 *            If true west and east are used, if false north and south are
	 *            used.
	 * @param stretch
	 *            If true center is used for <code>comp2</code> instead of east
	 *            or south.
	 * @return A <code>JPanel</code> containing the two components using a
	 *         <code>BorderLayout</code>
	 */
	public static JPanel pairComponents(JComponent comp1, JComponent comp2,
			boolean isHorizontal, boolean stretch) {
		return pairComponents(comp1, comp2, isHorizontal, stretch, null, null);
	}

	/**
	 * Wraps two components into a single <code>JPanel</code>. The
	 * <code>JPanel</code> uses a <code>BorderLayout</code>.
	 * 
	 * @param comp1
	 *            Component to put in west or north.
	 * @param comp2
	 *            Component to put in east, south, or center
	 * @param isHorizontal
	 *            If true west and east are used, if false north and south are
	 *            used.
	 * @param tooltip
	 *            Tooltip to set for <code>comp1</code> and <code>comp2</code>.
	 *            If <code>null</code> no tooltip is set.
	 * @return A <code>JPanel</code> containing the two components using a
	 *         <code>BorderLayout</code>
	 * @see #pairComponents(JComponent, JComponent, boolean, boolean, String,
	 *      String)
	 */
	public static JPanel pairComponents(JComponent comp1, JComponent comp2,
			boolean isHorizontal, String tooltip) {
		return pairComponents(comp1, comp2, isHorizontal, false, tooltip,
				tooltip);
	}

	/**
	 * Wraps two components into a single <code>JPanel</code>. The
	 * <code>JPanel</code> uses a <code>BorderLayout</code>.
	 * 
	 * @param comp1
	 *            Component to put in west or north.
	 * @param comp2
	 *            Component to put in east, south, or center
	 * @param isHorizontal
	 *            If true west and east are used, if false north and south are
	 *            used.
	 * @return A <code>JPanel</code> containing the two components using a
	 *         <code>BorderLayout</code>
	 * @see #pairComponents(JComponent, JComponent, boolean, boolean, String,
	 *      String)
	 */
	public static JPanel pairComponents(JComponent comp1, JComponent comp2,
			boolean isHorizontal) {
		return pairComponents(comp1, comp2, isHorizontal, false, null, null);
	}

	protected static abstract class SimpleComboBoxModel extends
			DefaultComboBoxModel implements ListDataListener {
		protected ArrayList listDataListeners = new ArrayList();
		protected Object selectedItem = null;
		/** Offset of this list from the array it represents. */
		protected int offset = 0; //

		/**
		 * @param offset
		 *            The offset to set.
		 */
		public void setOffset(int offset) {
			this.offset = offset;
		}

		/**
		 * @return Returns the offset.
		 */
		public int getOffset() {
			return offset;
		}

		/**
         * 
         */
		public SimpleComboBoxModel() {
		}

		public void contentsChanged(ListDataEvent lde) {
			this.fireContentsChanged(lde.getSource(), lde.getIndex0(),
					lde.getIndex1());
			this.notifyListDataListeners(new ListDataEvent(this, lde.getType(),
					lde.getIndex0() + offset, lde.getIndex1() + offset));
		}

		public void intervalAdded(ListDataEvent arg0) {
		}

		public void intervalRemoved(ListDataEvent arg0) {
		}

		public Object getSelectedItem() {
			return selectedItem;
		}

		public void setSelectedItem(Object obj) {
			this.selectedItem = obj;
		}

		public void addListDataListener(ListDataListener ldl) {
			listDataListeners.add(ldl);
		}

		public void removeListDataListener(ListDataListener ldl) {
			listDataListeners.remove(ldl);
		}

		public void notifyListDataListeners(ListDataEvent lde) {
			for (Iterator i = listDataListeners.iterator(); i.hasNext();) {
				((ListDataListener) i.next()).contentsChanged(lde);
			}
		}
	}

	/**
	 * Returns the input <code>String</code> with the number in []'s before it.
	 * If <code>useHexNumbers</code> is true then the number is in hex,
	 * otherwise it's in decimal. If the hex number comes out to a single digit,
	 * a zero will be added before it. This was written to be used for making
	 * lists for <code>JComboBox</code> 's.
	 * 
	 * @param in
	 *            <code>String</code> to add number to
	 * @param num
	 *            Number to add to String
	 * @param useHexNumbers
	 *            If true a hex number is put in, otherwise decimal is used.
	 * @return A new <code>String</code> in the form "["+num+"] "+in
	 * @see #getNumberedString(String, int)
	 * @see #createJComboBoxFromArray(Object[])
	 */
	public static String getNumberedString(String in, int num,
			boolean useHexNumbers) {
		return "["
				+ (useHexNumbers ? (Integer.toHexString(num).length() == 1 ? "0"
						+ Integer.toHexString(num)
						: Integer.toHexString(num))
						: Integer.toString(num)) + "] " + in.trim();
	}

	/**
	 * Returns the input <code>String</code> with the number in []'s before it.
	 * If useHexNumbers pref is true then the number is in hex, otherwise it's
	 * in decimal. If the hex number comes out to a single digit, a zero will be
	 * added before it. This was written to be used for making lists for
	 * <code>JComboBox</code> 's.
	 * 
	 * @param in
	 *            String to add number to
	 * @param num
	 *            Number to add to String
	 * @return A new <code>String</code> in the form "["+num+"] "+in
	 * @see #getNumberedString(String, int, boolean)
	 * @see #createJComboBoxFromArray(Object[])
	 */
	public static String getNumberedString(String in, int num) {
		return getNumberedString(in, num, getUseHexNumbers());
	}

	/**
	 * Returns the number added to the given String by
	 * {@link #getNumberedString(String, int, boolean)}.
	 * 
	 * @param in
	 *            String created by
	 *            {@link #getNumberedString(String, int, boolean)}.
	 * @param useHexNumbers
	 *            If true number is read as hex, if false decimal.
	 * @return The number in a numbered string. Negtive if fails.
	 */
	public static int getNumberOfString(String in, boolean useHexNumbers) {
		try {
			return Integer.parseInt(
					new StringTokenizer(in, "[]", false).nextToken(),
					useHexNumbers ? 16 : 10);
		} catch (NumberFormatException e) {
			return -1;
		} catch (NullPointerException e) {
			return -1;
		}
	}

	/**
	 * Returns the number added to the given String by
	 * {@link #getNumberedString(String, int)}.
	 * 
	 * @param in
	 *            String created by {@link #getNumberedString(String, int)}.
	 * @return The number in a numbered string.
	 */
	public static int getNumberOfString(String in) {
		return getNumberOfString(in, getUseHexNumbers());
	}

	protected static class NumberedComboBoxModel extends SimpleComboBoxModel {
		private Object obj[];

		public NumberedComboBoxModel(Object[] in) {
			obj = in;
		}

		public Object getElementAt(int i) {
			try {
				if (obj[i].toString().startsWith("[")) {
					return obj[i];
				} else {
					return ToolModule.getNumberedString(obj[i].toString(), i);
				}
			} catch (NullPointerException e) {
				return ToolModule.getNumberedString("", i);
			} catch (ArrayIndexOutOfBoundsException e) {
				return getElementAt(0);
			}
		}

		public int getSize() {
			return obj.length;
		}
	}

	/**
	 * Sets up a <code>JComboBox</code> from an <code>Object[]</code>. Removes
	 * all items from the <code>JComboBox</code> first. Calls
	 * {@link #getNumberedString(String, int, boolean)}before adding each item
	 * unless the item starts with "[". The boolean value is the
	 * <code>useHexNumbers<code>
	 * parameter passed to this method.
	 * 
	 * @param in
	 *            <code>Object[]</code> to add to the <code>JComboBox</code>
	 * @param out
	 *            The <code>JComboBox</code> to add the items to.
	 * @param useHexNumbers
	 *            If true hex is used for the numbers, else decimal is used.
	 * @return <code>out<code> with all the items from <code>in</code> added to
	 *         it.
	 * @see #getNumberedString(String, int, boolean)
	 */
	public static JComboBox createJComboBoxFromArray(Object[] in,
			JComboBox out, boolean useHexNumbers) {
		out.removeAllItems();
		for (int i = 0; i < in.length; i++) {
			if (in[i] == null) {
				out.addItem(getNumberedString("", i, useHexNumbers));
			} else if (in[i].toString().startsWith("[")) {
				out.addItem(in[i]);
			} else {
				out.addItem(getNumberedString(in[i].toString(), i,
						useHexNumbers));
			}
		}
		return out;
	}

	/**
	 * Sets up a <code>JComboBox</code> from an <code>Object[]</code>. Removes
	 * all items from the <code>JComboBox</code> first. Calls
	 * {@link #getNumberedString(String, int, boolean)}before adding each item
	 * unless the item starts with "[". The boolean value is the useHexNumbers
	 * pref.
	 * 
	 * @param in
	 *            <code>Object[]</code> to add to the <code>JComboBox</code>
	 * @param out
	 *            The <code>JComboBox</code> to add the items to.
	 * @return <code>out<code> with all the items from <code>in</code> added to
	 *         it.
	 * @see #getNumberedString(String, int, boolean)
	 * @see #createJComboBoxFromArray(Object[], JComboBox, boolean)
	 */
	public static JComboBox createJComboBoxFromArray(Object[] in, JComboBox out) {
		// return createJComboBoxFromArray(in, out, getUseHexNumbers());
		out.setModel(new NumberedComboBoxModel(in));
		return out;
	}

	/**
	 * Creates and sets up a <code>JComboBox</code> from an
	 * <code>Object[]</code>. Calls
	 * {@link #getNumberedString(String, int, boolean)}before adding each item
	 * unless the item starts with "[". The boolean value is the
	 * <code>useHexNumbers<code>
	 * parameter passed to this method.
	 * 
	 * @param in
	 *            <code>Object[]</code> to add to the <code>JComboBox</code>
	 * @param useHexNumbers
	 *            If true hex is used for the numbers, else decimal is used.
	 * @return A new <code>JComboBox</code> with all the items from
	 *         <code>in</code> added to it.
	 * @see #getNumberedString(String, int, boolean)
	 * @see #createJComboBoxFromArray(Object[], JComboBox, boolean)
	 */
	public static JComboBox createJComboBoxFromArray(Object[] in,
			boolean useHexNumbers) {
		JComboBox out = new JComboBox();
		return createJComboBoxFromArray(in, out, useHexNumbers);
	}

	/**
	 * Creates and sets up a <code>JComboBox</code> from an
	 * <code>Object[]</code>. Calls
	 * {@link #getNumberedString(String, int, boolean)}before adding each item
	 * unless the item starts with "[". The boolean value is the useHexNumbers
	 * pref.
	 * 
	 * @param in
	 *            <code>Object[]</code> to add to the <code>JComboBox</code>
	 * @return A new <code>JComboBox</code> with all the items from
	 *         <code>in</code> added to it.
	 * @see #getNumberedString(String, int, boolean)
	 * @see #createJComboBoxFromArray(Object[], JComboBox, boolean)
	 */
	public static JComboBox createJComboBoxFromArray(Object[] in) {
		// return createJComboBoxFromArray(in, getUseHexNumbers());
		return new JComboBox(new NumberedComboBoxModel(in));
	}

	/**
	 * Searches for a <code>String</code> in a <code>JComboBox</code>. If the
	 * <code>String</code> is found inside an item in the <code>JComboBox</code>
	 * then the item is selected on the JComboBox and true is returned. The
	 * search starts at the item after the currently selected item or the first
	 * item if the last item is currently selected. If the search
	 * <code>String</code> is not found, then an error dialog pops-up and the
	 * function returns false.
	 * 
	 * @param text
	 *            <code>String</code> to seach for. Can be anywhere inside any
	 *            item.
	 * @param selector
	 *            <code>JComboBox</code> to get list from, and set if search is
	 *            found.
	 * @param beginFromStart
	 *            If true, search starts at first item.
	 * @param displayError
	 *            If false, the error dialog will never be displayed
	 * @return Wether the search <code>String</code> is found.
	 */
	public static boolean search(String text, JComboBox selector,
			boolean beginFromStart, boolean displayError) {
		if (text == null || selector == null || selector.getItemCount() == 0)
			return false;
		text = text.toLowerCase();
		for (int i = (selector.getSelectedIndex() + 1 != selector
				.getItemCount() && !beginFromStart ? selector
				.getSelectedIndex() + 1 : 0); i < selector.getItemCount(); i++) {
			if (selector.getItemAt(i).toString().toLowerCase().indexOf(text) != -1) {
				if (selector.getSelectedIndex() == -1 && selector.isEditable()) {
					selector.setEditable(false);
					selector.setSelectedIndex(i == 0 ? 1 : 0);
					selector.setSelectedIndex(i);
					selector.setEditable(true);
				}
				selector.setSelectedIndex(i);
				selector.repaint();
				return true;
			}
		}

		if (beginFromStart) {
			if (displayError)
				JOptionPane.showMessageDialog(null,
						"Sorry, your search was not found.", "Not found!",
						JOptionPane.ERROR_MESSAGE);
			else
				System.out.println("Search not found.");
			return false;
		} else {
			return search(text, selector, true, displayError);
		}
	}

	/**
	 * Searches for a <code>String</code> in a <code>JComboBox</code>. If the
	 * <code>String</code> is found inside an item in the <code>JComboBox</code>
	 * then the item is selected on the JComboBox and true is returned. The
	 * search starts at the item after the currently selected item or the first
	 * item if the last item is currently selected. If the search
	 * <code>String</code> is not found, then an error dialog pops-up and the
	 * function returns false.
	 * 
	 * @param text
	 *            <code>String</code> to seach for. Can be anywhere inside any
	 *            item.
	 * @param selector
	 *            <code>JComboBox</code> to get list from, and set if search is
	 *            found.
	 * @param beginFromStart
	 *            If true, search starts at first item.
	 * @return Wether the search <code>String</code> is found.
	 */
	public static boolean search(String text, JComboBox selector,
			boolean beginFromStart) {
		return search(text, selector, beginFromStart, true);
	}

	/**
	 * Searches for a <code>String</code> in a <code>JComboBox</code>. If the
	 * <code>String</code> is found inside an item in the <code>JComboBox</code>
	 * then the item is selected on the JComboBox and true is returned. The
	 * search starts at the item after the currently selected item or the first
	 * item if the last item is currently selected. If the search
	 * <code>String</code> is not found, then an error dialog pops-up and the
	 * function returns false.
	 * 
	 * @param text
	 *            <code>String</code> to seach for. Can be anywhere inside any
	 *            item.
	 * @param selector
	 *            <code>JComboBox</code> to get list from, and set if search is
	 *            found.
	 * @return Wether the search <code>String</code> is found.
	 */
	public static boolean search(String text, JComboBox selector) {
		return search(text, selector, false);
	}

	/**
	 * Returns a new JPanel with the specified components added to it using a
	 * FlowLayout.
	 * 
	 * @see #createFlowLayout(Component)
	 * @param comps
	 *            JComponent[]
	 * @return A new JPanel with the specified components added to it.
	 */
	public static JComponent createFlowLayout(Component[] comps) {
		JComponent out = new JPanel(new FlowLayout(FlowLayout.CENTER, 5, 0));
		for (int i = 0; i < comps.length; i++) {
			out.add(comps[i]);
		}
		return out;
	}

	/**
	 * Returns a new JPanel with the specified component added to it using a
	 * FlowLayout.
	 * 
	 * @see #createFlowLayout(Component[])
	 * @param comp
	 *            JComponent
	 * @return A new JPanel with the specified component added to it.
	 */
	public static JComponent createFlowLayout(Component comp) {
		return createFlowLayout(new Component[] { comp });
	}

	/**
	 * Pads the given String on the left with the specified char.
	 * 
	 * @param in
	 *            String to pad
	 * @param maxLen
	 *            length to pad to
	 * @param chr
	 *            char to pad with
	 * @return padded String
	 */
	public static String padString(String in, int maxLen, char chr) {
		while (in.length() < maxLen) {
			in = chr + in;
		}
		return in;
	}

	/**
	 * Adds zeros to the start of a <code>String</code> up to
	 * <code>maxLen</code>. This for making hex values look nice. If
	 * <code>in</code> is already as long, or longer, than <code>maxLen</code>
	 * <code>in</code> will be returned.
	 * 
	 * @param in
	 *            <code>String</code> to add zeros to
	 * @return A new <code>String</code> with length >=<code>maxLen</code> by
	 *         adding "0"'s to the start of <code>in</code>
	 */
	public static String addZeros(String in, int maxLen) {
		return padString(in, maxLen, '0');
	}

	// TODO proper way to handle this? Maybe just get the URLClassLoader from
	// MainGUI?
	/**
	 * Reads a file into an array. The existance of a ROM-specific file will be
	 * checked before reading the default file. First
	 * <code>rompath.filename</code> will be searched for a ROM-specific file.
	 * If there is no ROM specific file, the default file at net/starmen/pkhack/
	 * <code>filename</code> will be used. The array should be in the format
	 * <code>EntryNum - Entry\n</code>. All whitespace is ignored (spaces are
	 * allowed within <code>Entry</code>.<code>EntryNum</code> will be read as
	 * decimal or hexidecimal depending on <code>hexNum</code>.
	 * 
	 * @param cl
	 *            <code>ClassLoader</code> to use to find the default file
	 * @param filename
	 *            where to look for the file. (rompath.filename or
	 *            /net/starmen/pkhack/filename will be used)
	 * @param hexNum
	 *            if true entry numbers in the file will be interpreted as hex
	 * @param out
	 *            array data will be read into, this same array will be returned
	 * @param size
	 *            size of array to create if <code>out</code> is null
	 * @see #readArray(String, boolean, int)
	 * @see #readArray(String, boolean, String[])
	 * @see #writeArray(String, boolean, String[])
	 */
	public static void readArray(ClassLoader cl, String baseDir,
			String filename, String romPath, boolean hexNum, String[] out,
			int size) {
		if (out == null)
			out = new String[size];
		// if the first one is null, the rest are
		if (out[0] == null)
			Arrays.fill(out, new String());
		try {
			String[] list;
			try {
				if (romPath == null)
					throw new NullPointerException(
							"You will never see this text.");
				list = new CommentedLineNumberReader(new FileReader((romPath
						+ "." + filename))).readUsedLines();
			} catch (Exception e1) {
				// If file in directory of ROM doesn't exist,
				// then read the default file.
				/*
				 * System.out.println("No ROM specific " + filename +
				 * " file was found, using default (" + baseDir + filename +
				 * ").");
				 */
				list = new CommentedLineNumberReader(new InputStreamReader(
						cl.getResourceAsStream(baseDir + filename)))
						.readUsedLines();
			}
			String[] tempStr;
			for (int i = 0; i < list.length; i++) {
				if (list[i].substring(0, Math.min(7, list[i].length()))
						.matches("[^#]*-[^#]*.*")) {
					tempStr = list[i].split("-", 2);
					int num = Integer.parseInt(tempStr[0].trim(), (hexNum ? 16
							: 10));
					out[num] = tempStr[1].trim();
				} else
					out[i] = list[i].trim();
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	/**
	 * Returns the base directory most files are located at. Ends with a
	 * separator.
	 */
	public String getDefaultBaseDir() {
		return "src/ebhack" + File.separator;
	}

	/**
	 * Creates a <code>ComboBoxModel</code> for an array. The
	 * <code>toString()</code> method will be called on all elements of the
	 * array, so only <code>String</code>'s will be seen as elements. This calls
	 * {@link #addDataListener(Object[], ListDataListener)}so that
	 * {@link #notifyDataListeners(Object[], ListDataEvent)}can be used to
	 * notify it of updates. If <code>zeroBased</code> is true, the array values
	 * will be pushed up by one (to offsets <code>1</code> to
	 * <code>array.length</code>). Offset <code>0</code> will be set to
	 * <code>zeroString</code>.
	 * 
	 * @param array
	 *            <code>Object</code>'s whose <code>.toString()</code> method
	 *            will give the <code>String</code> to use for the corresponding
	 *            position in the <code>ComboBoxModel</code>.
	 * @param zeroBased
	 *            If false, the values of <code>array</code> are pushed up one
	 *            and <code>zeroString</code> is used for the zero value.
	 * @param zeroString
	 *            <code>String</code> to use as element zero if
	 *            <code>zeroBased</code> is false.
	 * @return a <code>ComboBoxModel</code> for the specified array
	 * @see SimpleComboBoxModel
	 * @see #createComboBox(Object[], boolean, String, ActionListener)
	 * @see #createComboBoxModel(Object[], String)
	 */
	public static SimpleComboBoxModel createComboBoxModel(final Object[] array,
			final boolean zeroBased, final String zeroString) {
		SimpleComboBoxModel out = new SimpleComboBoxModel() {
			boolean zb = zeroBased;

			public int getSize() {
				return array.length + offset;
			}

			public Object getElementAt(int i) {
				String out;
				try {
					if (i == 0 && !zb)
						out = zeroString;
					else
						out = array[i - offset].toString();
				} catch (NullPointerException e) {
					out = zeroString;
				}
				return ToolModule.getNumberedString(out, i);
			}
		};
		addDataListener(array, out);
		out.setOffset(zeroBased ? 0 : 1);

		return out;
	}

	/**
	 * Creates a <code>ComboBoxModel</code> for an array. The
	 * <code>toString()</code> method will be called on all elements of the
	 * array, so only <code>String</code>'s will be seen as elements. This calls
	 * {@link #addDataListener(Object[], ListDataListener)}so that
	 * {@link #notifyDataListeners(Object[], ListDataEvent)}can be used to
	 * notify it of updates. If <code>zeroString</code> is not null, the array
	 * values will be pushed up by one (to offsets <code>1</code> to
	 * <code>array.length</code>). Offset <code>0</code> will be set to
	 * <code>zeroString</code>.
	 * 
	 * @param array
	 *            <code>Object</code>'s whose <code>.toString()</code> method
	 *            will give the <code>String</code> to use for the corresponding
	 *            position in the <code>ComboBoxModel</code>.
	 * @param zeroString
	 *            <code>null</code> if array should start at element zero,
	 *            otherwise <code>String</code> to use as element zero.
	 * @return a <code>ComboBoxModel</code> for the specified array
	 * @see SimpleComboBoxModel
	 * @see #createComboBox(Object[], boolean, String, ActionListener)
	 * @see #createComboBoxModel(Object[], boolean, String)
	 */
	public static SimpleComboBoxModel createComboBoxModel(final Object[] array,
			final String zeroString) {
		return createComboBoxModel(array, zeroString == null, zeroString);
	}

	/**
	 * <code>ListDataListener<code>'s used to notify <code>ComboBoxModel</code>
	 * 's created by <code>createComboBoxModel()</code> of changes.
	 * 
	 * @see HackModule.SimpleComboBoxModel
	 * @see #createComboBox(Object[], boolean, String, ActionListener)
	 * @see #createComboBoxModel(Object[], boolean, String)
	 * @see #addDataListener(Object[], ListDataListener)
	 * @see #removeDataListener(Object[], ListDataListener)
	 * @see #notifyDataListeners(Object[], ListDataEvent)
	 */
	private static Hashtable comboBoxListeners = new Hashtable();

	/**
	 * Retrives the <code>List</code> containing the
	 * <code>ListDataListener</code>'s for the specified array. If it does not
	 * exist, it will be created and added to {@link #comboBoxListeners}. Since
	 * this is an object, modifying (by adding elements to) the returned value
	 * will modify the value in <code>comboBoxListeners</code>.
	 * 
	 * @param array
	 *            array to get listeners for
	 * @return <code>List</code> of the <code>ListDataListeners</code> for
	 *         <code>array</code>
	 */
	private static List getListeners(Object[] array) {
		Object obj = comboBoxListeners.get(array);
		if (obj == null) {
			obj = new ArrayList();
			comboBoxListeners.put(array, obj);
		}
		return (List) obj;
	}

	/**
	 * Adds a <code>ListDataListener</code> to <code>array</code>. This should
	 * be called by a <code>ComboBoxModel</code> representing <code>array</code>
	 * so it can be notified of changes to <code>array</code> when
	 * <code>notifyDataListeners()</code> is called.
	 * 
	 * @param array
	 *            array to add listener for
	 * @param ldl
	 *            <code>ListDataListener</code> to listen to changes in
	 *            <code>array</code>
	 * @see ListDataListener
	 * @see #removeDataListener(Object[], ListDataListener)
	 * @see #notifyDataListeners(Object[], ListDataEvent)
	 */
	protected static void addDataListener(Object[] array, ListDataListener ldl) {
		if (array != null) {
			getListeners(array).add(ldl);
		}
	}

	/**
	 * Creates a <code>JComboBox</code> showing the elements of
	 * <code>array</code> as numbered <code>String</code>s. Since this adds a
	 * <code>ListDataListener</code> using
	 * {@link HackModule#addDataListener(Object[], ListDataListener)}, the
	 * {@link #notifyDataListeners(Object[], ListDataEvent)}methods can be used
	 * to force it to be redrawn. If <code>zeroBased</code> is false, the array
	 * values will be pushed up by one (to offsets <code>1</code> to
	 * <code>array.length</code>). Offset <code>0</code> will be set to
	 * <code>zeroString</code>. If not null, <code>al</code> will be added as an
	 * <code>ActionListener</code> to the return value.
	 * 
	 * @param array
	 *            <code>Object</code>'s whose <code>.toString()</code> method
	 *            will give the <code>String</code> to use for the corresponding
	 *            position in the <code>JComboBox</code>.
	 * @param zeroBased
	 *            If false, the values of <code>array</code> are pushed up one
	 *            and <code>zeroString</code> is used for the zero value.
	 * @param zeroString
	 *            <code>String</code> to use as element zero if
	 *            <code>zeroBased</code> is false.
	 * @param al
	 *            If not null, <code>JComboBox.addActionListener(al);</code>
	 *            will be called.
	 * @return a <code>JComboBox</code> displaying <code>array</code> as
	 *         <code>String</code>'s with any offset determined by
	 *         <code>zeroBased</code> and <code>al</code> added as its
	 *         <code>ActionListener</code>
	 * @see #createComboBoxModel(Object[], boolean, String)
	 * @see #getNumberedString(String, int)
	 * @see #addDataListener(Object[], ListDataListener)
	 * @see #notifyDataListeners(Object[], ListDataEvent)
	 * @see #createComboBox(Object[], String, ActionListener)
	 * @see #createComboBox(Object[], boolean, ActionListener)
	 * @see #createComboBox(Object[], ActionListener)
	 * @see #createComboBox(Object[], String)
	 * @see #createComboBox(Object[])
	 */
	public static JComboBox createComboBox(Object[] array, boolean zeroBased,
			String zeroString, final ActionListener al) {
		SimpleComboBoxModel model = createComboBoxModel(array, zeroBased,
				zeroString);
		final JComboBox out = new JComboBox(model);
		if (al != null)
			out.addActionListener(al);
		ListDataListener ldl = new ListDataListener() {

			public void contentsChanged(ListDataEvent lde) {
				if (out.getSelectedIndex() == -1) {
					out.removeActionListener(al);
					out.setSelectedIndex(lde.getIndex0());
					out.addActionListener(al);
				}
			}

			public void intervalAdded(ListDataEvent arg0) {
			}

			public void intervalRemoved(ListDataEvent arg0) {
			}
		};
		model.addListDataListener(ldl);

		return out;
	}

	/**
	 * Creates a <code>JComboBox</code> showing the elements of
	 * <code>array</code> as numbered <code>String</code>s. Since this adds a
	 * <code>ListDataListener</code> using
	 * {@link HackModule#addDataListener(Object[], ListDataListener)}, the
	 * {@link #notifyDataListeners(Object[], ListDataEvent)}methods can be used
	 * to force it to be redrawn. If <code>zeroBased</code> is false, the array
	 * values will be pushed up by one (to offsets <code>1</code> to
	 * <code>array.length</code>). Offset <code>0</code> will be set to
	 * "Nothing". If not null, <code>al</code> will be added as an
	 * <code>ActionListener</code> to the return value.
	 * 
	 * @param array
	 *            <code>Object</code>'s whose <code>.toString()</code> method
	 *            will give the <code>String</code> to use for the corresponding
	 *            position in the <code>JComboBox</code>.
	 * @param zeroBased
	 *            If false, the values of <code>array</code> are pushed up one
	 *            and "Nothing" is used for the zero value.
	 * @param al
	 *            If not null, <code>JComboBox.addActionListener(al);</code>
	 *            will be called.
	 * @return a <code>JComboBox</code> displaying <code>array</code> as
	 *         <code>String</code>'s with any offset determined by
	 *         <code>zeroBased</code> and <code>al</code> added as its
	 *         <code>ActionListener</code>
	 * @see #createComboBoxModel(Object[], boolean, String)
	 * @see #getNumberedString(String, int)
	 * @see #addDataListener(Object[], ListDataListener)
	 * @see #notifyDataListeners(Object[], ListDataEvent)
	 * @see #createComboBox(Object[], boolean, String, ActionListener)
	 * @see #createComboBox(Object[], String, ActionListener)
	 * @see #createComboBox(Object[], ActionListener)
	 * @see #createComboBox(Object[], String)
	 * @see #createComboBox(Object[])
	 */
	public static JComboBox createComboBox(Object[] array, boolean zeroBased,
			final ActionListener al) {
		return createComboBox(array, zeroBased, "Nothing", al);
	}

	/**
	 * Creates a <code>JComboBox</code> showing the elements of
	 * <code>array</code> as numbered <code>String</code>s. Since this adds a
	 * <code>ListDataListener</code> using
	 * {@link HackModule#addDataListener(Object[], ListDataListener)}, the
	 * {@link #notifyDataListeners(Object[], ListDataEvent)}methods can be used
	 * to force it to be redrawn. If <code>zeroString</code> is not null, the
	 * array values will be pushed up by one (to offsets <code>1</code> to
	 * <code>array.length</code>). Offset <code>0</code> will be set to
	 * <code>zeroString</code>. If not null, <code>al</code> will be added as an
	 * <code>ActionListener</code> to the return value.
	 * 
	 * @param array
	 *            <code>Object</code>'s whose <code>.toString()</code> method
	 *            will give the <code>String</code> to use for the corresponding
	 *            position in the <code>JComboBox</code>.
	 * @param zeroString
	 *            <code>String</code> to use as element zero if it is not null.
	 * @param al
	 *            If not null, <code>JComboBox.addActionListener(al);</code>
	 *            will be called.
	 * @return a <code>JComboBox</code> displaying <code>array</code> as
	 *         <code>String</code>'s with any offset determined by
	 *         <code>zeroString</code> and <code>al</code> added as its
	 *         <code>ActionListener</code>
	 * @see #createComboBoxModel(Object[], boolean, String)
	 * @see #createComboBoxModel(Object[], String)
	 * @see #getNumberedString(String, int)
	 * @see #addDataListener(Object[], ListDataListener)
	 * @see #notifyDataListeners(Object[], ListDataEvent)
	 * @see #createComboBox(Object[], boolean, String, ActionListener)
	 * @see #createComboBox(Object[], boolean, ActionListener)
	 * @see #createComboBox(Object[], ActionListener)
	 * @see #createComboBox(Object[], String)
	 * @see #createComboBox(Object[])
	 */
	public static JComboBox createComboBox(Object[] array, String zeroString,
			final ActionListener al) {
		return createComboBox(array, zeroString == null, zeroString, al);
	}

	/**
	 * Creates a <code>JComboBox</code> showing the elements of
	 * <code>array</code> as numbered <code>String</code>s. Since this adds a
	 * <code>ListDataListener</code> using
	 * {@link HackModule#addDataListener(Object[], ListDataListener)}, the
	 * {@link #notifyDataListeners(Object[], ListDataEvent)}methods can be used
	 * to force it to be redrawn. If <code>zeroString</code> is not null, the
	 * array values will be pushed up by one (to offsets <code>1</code> to
	 * <code>array.length</code>). Offset <code>0</code> will be set to
	 * <code>zeroString</code>.
	 * 
	 * @param array
	 *            <code>Object</code>'s whose <code>.toString()</code> method
	 *            will give the <code>String</code> to use for the corresponding
	 *            position in the <code>JComboBox</code>.
	 * @param zeroString
	 *            <code>String</code> to use as element zero if
	 *            <code>zeroBased</code> is false.
	 * @return a <code>JComboBox</code> displaying <code>array</code> as
	 *         <code>String</code>'s with any offset determined by
	 *         <code>zeroString</code>
	 * @see #createComboBoxModel(Object[], boolean, String)
	 * @see #createComboBoxModel(Object[], String)
	 * @see #getNumberedString(String, int)
	 * @see #addDataListener(Object[], ListDataListener)
	 * @see #notifyDataListeners(Object[], ListDataEvent)
	 * @see #createComboBox(Object[], boolean, String, ActionListener)
	 * @see #createComboBox(Object[], boolean, ActionListener)
	 * @see #createComboBox(Object[], String, ActionListener)
	 * @see #createComboBox(Object[], ActionListener)
	 * @see #createComboBox(Object[])
	 */
	public static JComboBox createComboBox(Object[] array, String zeroString) {
		return createComboBox(array, zeroString, null);
	}

	/**
	 * Creates a <code>JComboBox</code> showing the elements of
	 * <code>array</code> as numbered <code>String</code>s. Since this adds a
	 * <code>ListDataListener</code> using
	 * {@link HackModule#addDataListener(Object[], ListDataListener)}, the
	 * {@link #notifyDataListeners(Object[], ListDataEvent)}methods can be used
	 * to force it to be redrawn. If not null, <code>al</code> will be added as
	 * an <code>ActionListener</code> to the return value.
	 * 
	 * @param array
	 *            <code>Object</code>'s whose <code>.toString()</code> method
	 *            will give the <code>String</code> to use for the corresponding
	 *            position in the <code>JComboBox</code>.
	 * @param al
	 *            If not null, <code>JComboBox.addActionListener(al);</code>
	 *            will be called.
	 * @return a <code>JComboBox</code> displaying <code>array</code> as
	 *         <code>String</code>'s with <code>al</code> added as its
	 *         <code>ActionListener</code>
	 * @see #createComboBoxModel(Object[], boolean, String)
	 * @see #getNumberedString(String, int)
	 * @see #addDataListener(Object[], ListDataListener)
	 * @see #notifyDataListeners(Object[], ListDataEvent)
	 * @see #createComboBox(Object[], boolean, String, ActionListener)
	 * @see #createComboBox(Object[], String, ActionListener)
	 * @see #createComboBox(Object[], boolean, ActionListener)
	 * @see #createComboBox(Object[], String)
	 * @see #createComboBox(Object[])
	 */
	public static JComboBox createComboBox(Object[] array,
			final ActionListener al) {
		return createComboBox(array, true, null, al);
	}

	/**
	 * Creates a <code>JComboBox</code> showing the elements of
	 * <code>array</code> as numbered <code>String</code>s. Since this adds a
	 * <code>ListDataListener</code> using
	 * {@link HackModule#addDataListener(Object[], ListDataListener)}, the
	 * {@link #notifyDataListeners(Object[], ListDataEvent)}methods can be used
	 * to force it to be redrawn.
	 * 
	 * @param array
	 *            <code>Object</code>'s whose <code>.toString()</code> method
	 *            will give the <code>String</code> to use for the corresponding
	 *            position in the <code>JComboBox</code>.
	 * @return a <code>JComboBox</code> displaying <code>array</code> as
	 *         <code>String</code>'s
	 * @see #createComboBoxModel(Object[], boolean, String)
	 * @see #getNumberedString(String, int)
	 * @see #addDataListener(Object[], ListDataListener)
	 * @see #notifyDataListeners(Object[], ListDataEvent)
	 * @see #createComboBox(Object[], boolean, String, ActionListener)
	 * @see #createComboBox(Object[], String, ActionListener)
	 * @see #createComboBox(Object[], boolean, ActionListener)
	 * @see #createComboBox(Object[], String)
	 * @see #createComboBox(Object[], ActionListener)
	 */
	public static JComboBox createComboBox(Object[] array) {
		return createComboBox(array, (ActionListener) null);
	}

	/**
	 * Reads a file into an array. The existance of a ROM-specific file will be
	 * checked before reading the default file. First
	 * <code>rompath.filename</code> will be searched for a ROM-specific file.
	 * If there is no ROM specific file, the default file at net/starmen/pkhack/
	 * <code>filename</code> will be used. The array should be in the format
	 * <code>EntryNum - Entry\n</code>. All whitespace is ignored (spaces are
	 * allowed within <code>Entry</code>.<code>EntryNum</code> will be read as
	 * decimal or hexidecimal depending on <code>hexNum</code>.
	 * 
	 * @param filename
	 *            where to look for the file. (rompath.filename or
	 *            /net/starmen/pkhack/filename will be used)
	 * @param hexNum
	 *            if true entry numbers in the file will be interpreted as hex
	 * @param out
	 *            array data will be read into, this same array will be returned
	 * @param size
	 *            size of array to create if <code>out</code> is null
	 * @see #readArray(String, boolean, int)
	 * @see #readArray(String, boolean, String[])
	 * @see #writeArray(String, boolean, String[])
	 */
	private static void readArray(String baseDir, String filename,
			String romPath, boolean hexNum, String[] out, int size) {
		readArray(ToolModule.class.getClassLoader(), baseDir, filename,
				romPath, hexNum, out, size);
	}

	/**
	 * Reads a file into an array. See
	 * {@link #readArray(String, String, String, boolean, String[], int)}for
	 * more information.
	 * 
	 * @param filename
	 *            where to look for the file. (rompath.filename or
	 *            /net/starmen/pkhack/filename will be used)
	 * @param hexNum
	 *            if true entry numbers in the file will be interpreted as hex
	 * @param out
	 *            array data will be read into, this same array will be returned
	 * @see #readArray(String, boolean, int)
	 * @see #readArray(String, String, String, boolean, String[], int)
	 * @see #writeArray(String, boolean, String[])
	 */
	private static void readArray(String baseDir, String filename,
			String romPath, boolean hexNum, String[] out) {
		readArray(baseDir, filename, romPath, hexNum, out, out.length);
	}

	/**
	 * Reads a file into an array. See
	 * {@link #readArray(String, String, String, boolean, String[], int)}for
	 * more information.
	 * 
	 * @param cl
	 *            <code>ClassLoader</code> to use to find the default file
	 * @param filename
	 *            where to look for the file. (rompath.filename or
	 *            /net/starmen/pkhack/filename will be used)
	 * @param hexNum
	 *            if true entry numbers in the file will be interpreted as hex
	 * @param out
	 *            array data will be read into, this same array will be returned
	 * @see #readArray(String, boolean, int)
	 * @see #readArray(String, String, String, boolean, String[], int)
	 * @see #writeArray(String, boolean, String[])
	 */
	public static void readArray(ClassLoader cl, String baseDir,
			String filename, String romPath, boolean hexNum, String[] out) {
		readArray(cl, baseDir, filename, romPath, hexNum, out, out.length);
	}

	/**
	 * Reads a file into an array. See
	 * {@link #readArray(String, String, String, boolean, String[], int)}for
	 * more information.
	 * 
	 * @param cl
	 *            <code>ClassLoader</code> to use to find the default file
	 * @param baseDir
	 *            directory to look in for file
	 * @param filename
	 *            where to look for the file. (<code>baseDir</code>+
	 *            <code>filename</code> will be used)
	 * @param hexNum
	 *            if true entry numbers in the file will be interpreted as hex
	 * @param out
	 *            array data will be read into, this same array will be returned
	 * @see #readArray(String, String, boolean, int)
	 * @see #readArray(String, String, String, boolean, String[], int)
	 * @see #writeArray(String, boolean, String[])
	 */
	public static void readArray(ClassLoader cl, String baseDir,
			String filename, boolean hexNum, String[] out) {
		readArray(cl, baseDir, filename, null, hexNum, out, out.length);
	}

	/**
	 * Reads a file into an array. See
	 * {@link #readArray(String, String, String, boolean, String[], int)}for
	 * more information.
	 * 
	 * @param baseDir
	 *            directory to look in for file
	 * @param filename
	 *            where to look for the file. (<code>baseDir</code>+
	 *            <code>filename</code> will be used)
	 * @param hexNum
	 *            if true entry numbers in the file will be interpreted as hex
	 * @param out
	 *            array data will be read into, this same array will be returned
	 * @see #readArray(String, String, boolean, int)
	 * @see #readArray(String, String, String, boolean, String[], int)
	 * @see #writeArray(String, boolean, String[])
	 */
	private static void readArray(String baseDir, String filename,
			boolean hexNum, String[] out) {
		readArray(baseDir, filename, null, hexNum, out, out.length);
	}

	/**
	 * Reads a file into an array. See
	 * {@link #readArray(String, String, String, boolean, String[], int)}for
	 * more information.
	 * 
	 * @param filename
	 *            where to look for the file. (rompath.filename or
	 *            /net/starmen/pkhack/filename will be used)
	 * @param hexNum
	 *            if true entry numbers in the file will be interpreted as hex
	 * @param out
	 *            array data will be read into, this same array will be returned
	 * @see #readArray(String, boolean, int)
	 * @see #readArray(String, String, String, boolean, String[], int)
	 * @see #writeArray(String, boolean, String[])
	 */
	public void readArray(String filename, boolean hexNum, String[] out) {
		readArray(this.getClass().getClassLoader(), getDefaultBaseDir(),
				filename, null, hexNum, out, out.length);
	}

	/**
	 * Reads a file into an array. Creates a new array with length
	 * <code>size</code> to read data into. See
	 * {@link #readArray(String, String, String, boolean, String[], int)}for
	 * more information.
	 * 
	 * @param filename
	 *            where to look for the file. (rompath.filename or
	 *            /net/starmen/pkhack/filename will be used)
	 * @param hexNum
	 *            if true entry numbers in the file will be interpreted as hex
	 * @param size
	 *            size of array to create to read data into
	 * @return <code>String[size]</code> containing information from file
	 * @see #readArray(String, boolean, String[])
	 * @see #readArray(String, String, String, boolean, String[], int)
	 * @see #writeArray(String, boolean, String[])
	 */
	private static String[] readArray(String baseDir, String filename,
			String romPath, boolean hexNum, int size) {
		String[] out = new String[size];
		readArray(baseDir, filename, romPath, hexNum, out, size);
		return out;
	}

	/**
	 * Reads a file into an array. Creates a new array with length
	 * <code>size</code> to read data into. See
	 * {@link #readArray(String, String, String, boolean, String[], int)}for
	 * more information.
	 * 
	 * @param baseDir
	 *            directory to look in for file
	 * @param filename
	 *            where to look for the file. (<code>baseDir</code>+
	 *            <code>filename</code> will be used)
	 * @param hexNum
	 *            if true entry numbers in the file will be interpreted as hex
	 * @param size
	 *            size of array to create to read data into
	 * @return <code>String[size]</code> containing information from file
	 * @see #readArray(String, String, boolean, String[])
	 * @see #readArray(String, String, String, boolean, String[], int)
	 * @see #writeArray(String, boolean, String[])
	 */
	private static String[] readArray(String baseDir, String filename,
			boolean hexNum, int size) {
		String[] out = new String[size];
		readArray(baseDir, filename, null, hexNum, out, size);
		return out;
	}

	/**
	 * Reads a file into an array. Creates a new array with length
	 * <code>size</code> to read data into. See
	 * {@link #readArray(String, String, String, boolean, String[], int)}for
	 * more information.
	 * 
	 * @param filename
	 *            where to look for the file. ( <code>getDefaultBaseDir()</code>
	 *            +<code>filename</code> will be used)
	 * @param hexNum
	 *            if true entry numbers in the file will be interpreted as hex
	 * @param size
	 *            size of array to create to read data into
	 * @return <code>String[size]</code> containing information from file
	 * @see #readArray(String, String, String, boolean, String[])
	 * @see #readArray(String, String, String, boolean, String[], int)
	 * @see #writeArray(String, boolean, String[])
	 * @see #getDefaultBaseDir()
	 */
	public String[] readArray(String filename, boolean hexNum, int size) {
		String[] out = new String[size];
		readArray(this.getClass().getClassLoader(), getDefaultBaseDir(),
				filename, null, hexNum, out, size);
		return out;
	}

	/**
	 * Creates a standard edit menu. Menu includes undo if requested and cut,
	 * copy. paste, and delete. Action commands are "undo" "cut" "copy" "paste"
	 * "delete".
	 * 
	 * @param al
	 *            <code>ActionListener</code> to send <code>ActionEvents</code>
	 *            to
	 * @param includeUndo
	 *            if true an undo menu item is included, if false it is not
	 *            included
	 * @return a edit menu
	 */
	public static JMenu createEditMenu(ActionListener al, boolean includeUndo) {
		JMenu editMenu = new JMenu("Edit");
		editMenu.setMnemonic('e');

		if (includeUndo) {
			editMenu.add(ToolModule.createJMenuItem("Undo", 'u', "ctrl Z",
					"undo", al));
			editMenu.addSeparator();
		}
		editMenu.add(ToolModule
				.createJMenuItem("Cut", 't', "ctrl X", "cut", al));
		editMenu.add(ToolModule.createJMenuItem("Copy", 'c', "ctrl C", "copy",
				al));
		editMenu.add(ToolModule.createJMenuItem("Paste", 'p', "ctrl V",
				"paste", al));
		editMenu.add(ToolModule.createJMenuItem("Delete", 'd', "DELETE",
				"delete", al));

		return editMenu;
	}

	/**
	 * Returns an integer representing how different two colors are. Zero
	 * indicates the same color. 195075 would be the difference between black
	 * and white. The calculation is to sum the squares of the differences of
	 * the color components (red, green, and blue).
	 * 
	 * @param a
	 *            a Color
	 * @param b
	 *            another Color, order does not matter
	 * @return a number representing how different the two input colors are;
	 *         higher means more different
	 */
	public static int getColorDiff(Color a, Color b) {
		int r = a.getRed() - b.getRed(); // Red delta
		int g = a.getGreen() - b.getGreen(); // Green delta
		int u = a.getBlue() - b.getBlue(); // blUe delta

		return r * r + g * g + u * u;
	}

	/**
	 * Returns in the color in a palette which is the least different from a
	 * color. {@link #getColorDiff(Color, Color)}is used to determine how
	 * different two colors are.
	 * 
	 * @param pal
	 *            palette to look through for a close color
	 * @param c
	 *            color to find best match in palette for
	 * @return index in <code>pal</code> of best match for <code>c</code>
	 */
	public static byte getNearestColorIndex(Color[] pal, Color c) {
		int m = Integer.MAX_VALUE; // min difference
		byte mi = -1; // index
		for (byte i = 0; i < pal.length; i++) {
			int tmp;
			if ((tmp = getColorDiff(c, pal[i])) < m) {
				m = tmp;
				mi = i;
			}
		}
		return mi;
	}

	/**
	 * Converts an <code>Image</code> into a <code>int[][]</code> using the
	 * given palette. For each pixel of the image, the corresponding element,
	 * <code>out[x][y]</code> will be set to the index of the closest color in
	 * the palette.
	 * 
	 * @param in
	 *            image to convert. Any data outside the size of
	 *            <code>out</code> will be discarded.
	 * @param out
	 *            <code>int[][]</code> to write palette indexed image into. This
	 *            must be a rectangular array no bigger than the image.
	 * @param pal
	 *            palette to use for converting colors to palette indexes
	 */
	public static void convertImage(Image in, int[][] out, Color[] pal) {
		int w = out.length;
		int h = out[0].length;

		int[] pixels = new int[w * h];

		PixelGrabber pg = new PixelGrabber(in, 0, 0, w, h, pixels, 0, w);
		try {
			pg.grabPixels();
		} catch (InterruptedException e) {
			System.out
					.println("Interrupted waiting for pixels in HackModule.convertImage()!");
			e.printStackTrace(System.out);
			return;
		}

		for (int i = 0; i < w; i++) {
			for (int j = 0; j < h; j++) {
				out[i][j] = getNearestColorIndex(pal, new Color(pixels[(j * w)
						+ i] & 0xf8f8f8));
			}
		}
	}

	public static String capitalize(String s) {
		return Character.toUpperCase(s.charAt(0)) + s.substring(1);
	}
}
