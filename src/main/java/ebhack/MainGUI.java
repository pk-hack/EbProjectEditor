package ebhack;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;
import java.io.*;
import java.util.ArrayList;
import java.util.ListIterator;
import java.util.StringTokenizer;

public class MainGUI implements ActionListener, WindowListener {
	private JFrame mainWindow;
	private ArrayList<ToolModule> moduleList = new ArrayList<ToolModule>();
	private ArrayList<JButton> moduleButtons = new ArrayList<JButton>();
	private YMLPreferences prefs;
	private Project project;

	JMenuItem close, save, refresh, projProp;
	JButton saveB, refreshB, saveAsB;
	JLabel statusBar;

	private boolean busy = false;

	public static String getDescription() {
		return "EB Project Editor";
	}

	public static String getVersion() {
		return "2.0";
	}

	public static String getCredits() {
		return "Written by ghost\n" + "Based on JHack, by AnyoneEB\n" + "Licensed under GPLv3";
	}

	public JFrame getMainWindow() {
		return mainWindow;
	}

	public MainGUI() {

	}

	public void init(String projectFileName) {
        loadPrefs();
		initModules();
		initGUI();

        if (projectFileName != null)
            launchLoadProject(projectFileName);
	}

    public void init() {
        loadPrefs();
        if (prefs.getValueAsBoolean("autoLoad"))
            init(prefs.getValue("lastProject"));
        else
            init(null);
    }

	private void loadPrefs() {
		project = new Project();

		File ymlFile = new File(Ebhack.EBHACK_DIR.toString() + File.separator
				+ "EbhackPrefs.yml");
		this.prefs = new YMLPreferences(ymlFile);
	}

	public YMLPreferences getPrefs() {
		return prefs;
	}

	private void launchLoadProject(final String projectFileName) {
        SwingWorker<Void, Void> worker = new SwingWorker<Void, Void>() {
            public Void doInBackground() {
                if (!enterBusy() || (project.isLoaded() && !closeProject()))
                    return null;

                loadProject(new File(projectFileName));
                return null;
            }

            public void done() {
                exitBusy();
            }
        };
        worker.execute();
	}

	private void initGUI() {
		mainWindow = new JFrame();
		mainWindow.setTitle(MainGUI.getDescription() + " "
				+ MainGUI.getVersion());
		mainWindow.setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
		mainWindow.getContentPane().setLayout(new BorderLayout(10, 10));
		mainWindow.addWindowListener(this);

		mainWindow.getContentPane().add(new JLabel("test"));

		JButton button;
		JPanel panel;

		JMenuBar menuBar = new JMenuBar();

		JMenu fileMenu = new JMenu("File");
		fileMenu.setMnemonic('f');
		fileMenu.add(ToolModule.createJMenuItem("Open Project...", 'o',
				"ctrl O", "open", this));
		fileMenu.add(close = ToolModule.createJMenuItem("Close Project", 'c',
				"ctrl F4", "close", this));
		fileMenu.add(save = ToolModule.createJMenuItem("Save Project", 's',
				"ctrl S", "save", this));
		fileMenu.add(refresh = ToolModule.createJMenuItem("Refresh Project",
				'r', "ctrl R", "refresh", this));
		// fileMenu.add(saveAs =
		// ToolModule.createJMenuItem("Save Project As...", 'a',
		// "ctrl A", "saveAs", this));
		fileMenu.add(new JSeparator());
		fileMenu.add(projProp = ToolModule.createJMenuItem("Edit Project", 'e',
				"ctrl E", "projProp", this));
		fileMenu.add(new JSeparator());
		fileMenu.add(ToolModule.createJMenuItem("Exit", 'x', "alt F4", "exit",
				this));
		menuBar.add(fileMenu);

		JMenu optionsMenu = new JMenu("Options");
		optionsMenu.setMnemonic('o');
		optionsMenu.add(new PrefsCheckBox("Use Hex Numbers", prefs,
				"useHexNumbers", true, 'h'));
		optionsMenu.add(new PrefsCheckBox("Load Last Project on Startup",
				prefs, "autoLoad", true, 'l'));
		optionsMenu.add(new JSeparator());
		// optionsMenu.add(ToolModule.createJMenuItem("Select Text Editor...",
		// 't', null, "selectTE", this));
		// optionsMenu.add(new JSeparator());
		optionsMenu.add(ToolModule.createJMenuItem("Set Default Author...",
				'a', null, "setAuthor", this));
		menuBar.add(optionsMenu);

		JMenu helpMenu = new JMenu("Help");
		helpMenu.setMnemonic('h');
		helpMenu.add(ToolModule.createJMenuItem("About...", 'a', null, "about",
				this));
		menuBar.add(Box.createHorizontalGlue());
		menuBar.add(helpMenu);

		mainWindow.setJMenuBar(menuBar);

		JToolBar toolbar = new JToolBar();
		toolbar.setFloatable(false);
		toolbar.setRollover(true);
		toolbar.add(genTopButton("Open24.gif", "Open Project", "open"));
		toolbar.add(saveB = genTopButton("Save24.gif", "Save Project", "save"));
		toolbar.add(refreshB = genTopButton("Refresh24.gif", "Refresh Project",
				"refresh"));
		saveAsB = genTopButton("SaveAs24.gif", "Save Project As...", "saveAs");
		// toolbar.add(saveAsB);
		panel = new JPanel();
		panel.add(toolbar);

		mainWindow.getContentPane().add(toolbar, BorderLayout.NORTH);

		Box buttons = new Box(BoxLayout.Y_AXIS);
		JScrollPane scroll = new JScrollPane(buttons,
				JScrollPane.VERTICAL_SCROLLBAR_ALWAYS,
				JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);

		ListIterator<ToolModule> listIterator = moduleList.listIterator();
		int i = 0;
		ToolModule tm;
		while (listIterator.hasNext()) {
			tm = listIterator.next();
			if (tm.showsInMenu()) {
				button = new JButton(tm.getDescription());
				button.setActionCommand("module" + i);
				button.addActionListener(this);
				button.setMaximumSize(new Dimension(175, 26));
				// button.setPreferredSize(new Dimension(175,26));
				buttons.add(button);
				moduleButtons.add(button);
				button.setEnabled(false);
			}
			i++;
		}
		// buttons.setPreferredSize(buttons.getMaximumSize());

		// mainWindow.getContentPane().add(scroll, BorderLayout.CENTER);
		mainWindow.getContentPane().add(buttons, BorderLayout.CENTER);

		statusBar = new JLabel();
		resetStatusBar();
		mainWindow.getContentPane().add(statusBar, BorderLayout.SOUTH);

		mainWindow.invalidate();
		mainWindow.pack();
		// mainWindow.setSize(175, 200);
		mainWindow.setLocationByPlatform(true);
		mainWindow.validate();
		mainWindow.setResizable(false);

		close.setEnabled(false);
		save.setEnabled(false);
		refresh.setEnabled(false);
		projProp.setEnabled(false);
		saveB.setEnabled(false);
		refreshB.setEnabled(false);
		saveAsB.setEnabled(false);

		mainWindow.setVisible(true);
	}

	public void updateTitle() {
		if (project.isLoaded())
			mainWindow.setTitle(MainGUI.getDescription() + " "
					+ MainGUI.getVersion() + " - " + project.getName());
		else
			mainWindow.setTitle(MainGUI.getDescription() + " "
					+ MainGUI.getVersion());
	}

	private void updateGUI() {
		updateTitle();

		if (project.isLoaded()) {
			close.setEnabled(true);
			save.setEnabled(true);
			refresh.setEnabled(true);
			projProp.setEnabled(true);
			saveB.setEnabled(true);
			refreshB.setEnabled(true);
			saveAsB.setEnabled(true);

			ListIterator<JButton> listIterator = moduleButtons.listIterator();
			while (listIterator.hasNext())
				listIterator.next().setEnabled(true);
		} else {
			close.setEnabled(false);
			save.setEnabled(false);
			refresh.setEnabled(false);
			projProp.setEnabled(false);
			saveB.setEnabled(false);
			refreshB.setEnabled(false);
			saveAsB.setEnabled(false);

			ListIterator<JButton> listIterator = moduleButtons.listIterator();
			while (listIterator.hasNext())
				listIterator.next().setEnabled(false);
		}
	}

	private JButton genTopButton(String icon, String tooltip, String ac) {
		JButton button = new JButton(new ImageIcon(getClass().getResource(
				"icons/" + icon)));
		button.setToolTipText(tooltip);
		button.setActionCommand(ac);
		button.addActionListener(this);
		return button;
	}

	private void initModules() {
		String[] moduleNames = new String[0]; // list of module class names

		try {
			moduleNames = new CommentedLineNumberReader(new InputStreamReader(
					this.getClass().getResourceAsStream("modulelist.txt")))
					.readUsedLines();
		} catch (IOException e) {
			e.printStackTrace();
		}

		try {
			ToolModule tm;
			for (int i = 0; i < moduleNames.length; i++) {
				tm = (ToolModule) Class.forName("ebhack." + moduleNames[i])
						.getConstructor(new Class[] { YMLPreferences.class })
						.newInstance(new Object[] { prefs });
				moduleList.add(tm);
			}
		} catch (Exception e) {
			e.printStackTrace();
		}

	}

	public boolean showModule(Class modClass, Object input) {
		ListIterator<ToolModule> listIterator = moduleList.listIterator();
		while (listIterator.hasNext()) {
			ToolModule tm = listIterator.next();
			if (tm.getClass().equals(modClass)) {
				try {
					tm.show(input);
					return true;
				} catch (IllegalArgumentException e) {
					return false;
				}
			}
		}
		return false;
	}

	private void resetModules() {
		ListIterator<ToolModule> listIterator = moduleList.listIterator();
		while (listIterator.hasNext())
			listIterator.next().reset();
	}

	private void hideModules() {
		ListIterator<ToolModule> listIterator = moduleList.listIterator();
		while (listIterator.hasNext())
			listIterator.next().hide();
	}

	private void loadModulesData() {
		ListIterator<ToolModule> listIterator = moduleList.listIterator();
		while (listIterator.hasNext()) {
			ToolModule x = listIterator.next();
			// System.out.println("Loading " + x.getDescription());
			setStatusBar("Loading " + x.getDescription() + "...");
			x.load(project);
			// listIterator.next().load(project);
		}
	}

	private void refreshModulesData() {
		ListIterator<ToolModule> listIterator = moduleList.listIterator();
		while (listIterator.hasNext()) {
			ToolModule x = listIterator.next();
			setStatusBar("Refreshing " + x.getDescription() + "...");
			x.refresh(project);
		}
	}

	private void saveModulesData() {
		ListIterator<ToolModule> listIterator = moduleList.listIterator();
		while (listIterator.hasNext())
			listIterator.next().save(project);
	}

	public ImageIcon getImage(String strFilename) {
		// Get an instance of our class
		Class thisClass = getClass();

		// Locate the desired image file and create a URL to it
		java.net.URL url = thisClass.getResource("toolbarButtonGraphics/"
				+ strFilename);

		// See if we successfully found the image
		if (url == null) {
			System.out.println("Unable to load the following image: "
					+ strFilename);
			return null;
		}

		// Get a Toolkit object
		Toolkit toolkit = Toolkit.getDefaultToolkit();

		// Create a new image from the image URL
		Image image = toolkit.getImage(url);

		// Build a new ImageIcon from this and return it to the caller
		return new ImageIcon(image);
	}

	private String getModuleCredits() {
		String returnValue = new String();
		ListIterator<ToolModule> listIterator = moduleList.listIterator();
		ToolModule tm;
		while (listIterator.hasNext()) {
			tm = listIterator.next();
			returnValue += "\n\n" + tm.getDescription() + " " + tm.getVersion()
					+ "\n" + tm.getCredits();
		}

		return returnValue;
	}

	private String getFullCredits() {
		return MainGUI.getDescription() + " " + MainGUI.getVersion() + "\n"
				+ MainGUI.getCredits() + this.getModuleCredits();
	}

	private JScrollPane createScollingLabel(String text) {
		int emptyLine = new JLabel("newline").getPreferredSize().height;
		JPanel labels = new JPanel();
		labels.setLayout(new BoxLayout(labels, BoxLayout.Y_AXIS));

		text = text.replaceAll("\n\n", "\nnewline\n");

		StringTokenizer st = new StringTokenizer(text, "\n");
		JLabel temp;
		while (st.hasMoreTokens()) {
			temp = new JLabel(st.nextToken());
			if (temp.getText().equals("newline")) {
				labels.add(Box.createVerticalStrut(emptyLine));
			} else {
				labels.add(temp);
			}
		}

		JScrollPane out = new JScrollPane(labels,
				JScrollPane.VERTICAL_SCROLLBAR_ALWAYS,
				JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);

		out.setPreferredSize(new Dimension(out.getPreferredSize().width, 200));

		return out;
	}

	private boolean enterBusy() {
		if (busy) {
			JOptionPane
					.showMessageDialog(
							mainWindow,
							"Please wait until the action in progress is complete, then try again.",
							"Busy", JOptionPane.WARNING_MESSAGE);
			return false;
		} else {
			busy = true;
			return true;
		}
	}

	private void exitBusy() {
		busy = false;
	}

	// Note: use these methods INSIDE a "busy" mutex block
	// These methods should only be executed from inside SwingWorkers
	// They only return false when cancelled
	private void setStatusBar(String status) {
		statusBar.setText("Status: " + status);
	}

	private void resetStatusBar() {
		setStatusBar("Ready.");
	}

	private boolean loadProject(File projFile) {
		setStatusBar("Loading Project...");
		if ((projFile == null ? project.load() : project.load(projFile))) {
			loadModulesData();
			updateGUI();
			resetStatusBar();
			return true;
		} else {
			resetStatusBar();
			return false;
		}
	}

	private boolean refreshProject() {
		setStatusBar("Loading Project...");
		if (project.isLoaded()) {
			refreshModulesData();
			updateGUI();
			resetStatusBar();
			return true;
		} else {
			resetStatusBar();
			return false;
		}
	}

	private boolean loadProject() {
		return loadProject(null);
	}

	private boolean saveProject(boolean ask) {
		setStatusBar("Saving Project...");
		if (project.isLoaded()) {
			if (ask) {
				int ques = JOptionPane.showConfirmDialog(mainWindow,
						"Do you want to save your changes?", "Save?",
						JOptionPane.YES_NO_CANCEL_OPTION);
				if (ques == JOptionPane.CANCEL_OPTION) {
					resetStatusBar();
					return false;
				} else if (ques == JOptionPane.YES_OPTION) {
					saveModulesData();
					project.save();
					resetStatusBar();
					return true;
				} else {
					resetStatusBar();
					return true;
				}
			} else {
				saveModulesData();
				project.save();
				resetStatusBar();
				return true;
			}
		} else {
			resetStatusBar();
			return true;
		}
	}

	private boolean saveProject() {
		return saveProject(true);
	}

	// Returns true ONLY if the user chooses to save
	private boolean saveProjectRequireSave() {
		setStatusBar("Saving Project...");
		if (project.isLoaded()) {
			int ques = JOptionPane.showConfirmDialog(mainWindow,
					"Do you want to save your changes?", "Save?",
					JOptionPane.YES_NO_CANCEL_OPTION);
			if (ques == JOptionPane.CANCEL_OPTION) {
				resetStatusBar();
				return false;
			} else if (ques == JOptionPane.YES_OPTION) {
				saveModulesData();
				project.save();
				resetStatusBar();
				return true;
			} else {
				resetStatusBar();
				return false;
			}
		} else {
			resetStatusBar();
			return true;
		}
	}

	private boolean closeProject(boolean manual) {
		setStatusBar("Closing Project...");
		if (saveProject()) {
			hideModules();
			if (project.isLoaded())
				project.close();
			resetModules();
			updateGUI();
			if (manual)
				prefs.removeValue("lastProject");
			resetStatusBar();
			return true;
		} else {
			resetStatusBar();
			return false;
		}
	}

	private boolean closeProject() {
		return closeProject(false);
	}

	// Runs command cmd from directory dir
	private void runExtCommand(String[] cmd, File dir, String description,
			boolean print) {
		if (description == null)
			setStatusBar("Calling External Program...");
		else
			setStatusBar(description);
		try {
			Runtime r = Runtime.getRuntime();

			Process p;
			if (dir == null)
				p = r.exec(cmd);
			else
				p = r.exec(cmd, new String[0], dir);

			if (print) {
				System.out.println("Command:");
				for (int i = 0; i < cmd.length; i++)
					System.out.print(cmd[i] + " ");
				System.out.println();
				System.out.println();
				BufferedReader reader = new BufferedReader(
						new InputStreamReader(new DataInputStream(
								new BufferedInputStream(p.getInputStream()))));
				String lineRead = null;
				while ((lineRead = reader.readLine()) != null) {
					System.out.println(lineRead);
				}
			}
			resetStatusBar();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	private void exit() {
		if (enterBusy()) {
			if (closeProject()) {
				setStatusBar("Exiting...");
				this.prefs.save();
				System.exit(0);
			} else
				exitBusy();
		}
	}

	public void actionPerformed(ActionEvent e) {
		if (e.getActionCommand().equals("open")) {
			SwingWorker<Void, Void> worker = new SwingWorker<Void, Void>() {
				public Void doInBackground() {
					if (!enterBusy() || (project.isLoaded() && !closeProject()))
						return null;

					loadProject();
					return null;
				}

				public void done() {
					exitBusy();
				}
			};
			worker.execute();
		} else if (e.getActionCommand().equals("refresh")) {
			SwingWorker<Void, Void> worker = new SwingWorker<Void, Void>() {
				public Void doInBackground() {
					if (!enterBusy())
						return null;

					refreshProject();
					return null;
				}

				public void done() {
					exitBusy();
				}
			};
			worker.execute();
		} else if (e.getActionCommand().equals("close")) {
			SwingWorker<Void, Void> worker = new SwingWorker<Void, Void>() {
				public Void doInBackground() {
					if (!enterBusy())
						return null;

					closeProject(true);
					return null;
				}

				public void done() {
					exitBusy();
				}
			};
			worker.execute();
		} else if (e.getActionCommand().equals("save")) {
			SwingWorker<Void, Void> worker = new SwingWorker<Void, Void>() {
				public Void doInBackground() {
					if (!enterBusy())
						return null;

					saveProject(false);
					return null;
				}

				public void done() {
					exitBusy();
				}
			};
			worker.execute();
		} else if (e.getActionCommand().equals("saveAs")) {
			// TODO
		} else if (e.getActionCommand().equals("projProp")) {
			ListIterator<ToolModule> listIterator = moduleList.listIterator();
			while (listIterator.hasNext()) {
				ToolModule tm = listIterator.next();
				if (tm.getClass().getName().equals("ebhack.ProjectEditor")) {
					tm.show();
					break;
				}
			}
		} else if (e.getActionCommand().equals("selectTE")) {
			if (System.getProperty("os.name").startsWith("Windows")) {
				ToolModule.chooseFile(false, "exe", "Executable", "textEditor",
						"Select Text Editor");
			} else {
				ToolModule.chooseFile(false, null, "Executable", "textEditor",
						"Select Text Editor");
			}
		} else if (e.getActionCommand().equals("setAuthor")) {
			String s = (String) JOptionPane
					.showInputDialog(
							mainWindow,
							"Input your username.\n"
									+ "This will be the default \"author\" name used by new projects.",
							"Set Default Author", JOptionPane.QUESTION_MESSAGE,
							null, null, prefs.getValue("author", "Anonymous"));
			if ((s != null) && (s.length() > 0))
				prefs.setValue("author", s);
		} else if (e.getActionCommand().equals("exit")) {
			exit();
		} else if (e.getActionCommand().equals("about")) {
			JOptionPane.showMessageDialog(
					null,
					this.createScollingLabel(this.getFullCredits()),
					"About " + MainGUI.getDescription() + " "
							+ MainGUI.getVersion(),
					JOptionPane.INFORMATION_MESSAGE);
		} else if (e.getActionCommand().startsWith("module")) {
			moduleList.get(Integer.parseInt(e.getActionCommand().substring(6)))
					.show();
		}
	}

	public void windowOpened(WindowEvent e) {
	}

	public void windowClosing(WindowEvent e) {
		exit();
	}

	public void windowClosed(WindowEvent e) {
	}

	public void windowIconified(WindowEvent e) {
	}

	public void windowDeiconified(WindowEvent e) {
	}

	public void windowActivated(WindowEvent e) {
	}

	public void windowDeactivated(WindowEvent e) {
	}
}
