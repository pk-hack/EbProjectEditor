package ebhack;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;

public class ProjectEditor extends ToolModule implements ActionListener {
	private JTextField title, author, projectDir;
	private JTextArea description;
	private Project proj;
	
	private String titleS, authorS, descriptionS;
	
	public ProjectEditor(YMLPreferences prefs) {
		super(prefs);
	}

	public String getDescription() {
		return "Project Properties Editor";
	}

	public String getVersion() {
		return "0.1";
	}

	public String getCredits() {
		return "Written by ghost";
	}
	
	public boolean showsInMenu() {
		return false;
	}

	public void init() {
        mainWindow = createBaseWindow(this);
        mainWindow.setTitle(this.getDescription());
        
		JPanel panel = new JPanel();
		panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));

        projectDir = new JTextField(30);
        projectDir.setEnabled(false);
        panel.add(ToolModule.getLabeledComponent("Location: ", projectDir, "Project Directory"));
		title = new JTextField(30);
		panel.add(ToolModule.getLabeledComponent("Title: ", title, "Project Title"));
		author = new JTextField(30);
		panel.add(ToolModule.getLabeledComponent("Author: ", author, "Project Author"));
		description = new JTextArea(5,30);
		panel.add(ToolModule.getLabeledComponent("Description: ", description, "Project Description"));
		
        mainWindow.getContentPane().add(panel, BorderLayout.CENTER);

        mainWindow.invalidate();
        mainWindow.pack();
        mainWindow.setLocationByPlatform(true);
        mainWindow.validate();
        mainWindow.setResizable(false);
	}
	
	public void show() {
		super.show();

        projectDir.setText(this.proj.getDirectory());
		title.setText(titleS);
		author.setText(authorS);
		description.setText(descriptionS);
		
		mainWindow.setVisible(true);
	}

	public void load(Project proj) {
		this.proj = proj;

		titleS = proj.getName();
		authorS = proj.getAuthor();
		descriptionS = proj.getDescription();
	}

	public void save(Project proj) {

	}
	
	public void hide() {
		if (isInited)
			mainWindow.setVisible(false);
	}
	
	public static class FileField implements ActionListener {
		private JTextField tf;
		private JButton b;
		private JPanel panel;
		private boolean fileOrFolder;
		String extension, description;
		
		// for choosing a folder
		public FileField() {
			fileOrFolder = false;
			
			tf = new JTextField(30);
			b = new JButton("Browse...");
			b.addActionListener(this);
			
			panel = pairComponents(tf, b, true, "Select a folder");
		}
		
		public FileField(String extension, String description) {
			fileOrFolder = true;
			this.extension = extension;
			this.description = description;
			
			tf = new JTextField(30);
			b = new JButton("Browse...");
			b.addActionListener(this);
			
			panel = pairComponents(tf, b, true, "Select a file");
		}
		
		public JPanel getPanel() {
			return panel;
		}
		
		public String getFilename() {
			return tf.getText();
		}
		
		public void setText(String s) {
			tf.setText(s);
		}

		public void actionPerformed(ActionEvent e) {
			File f;
			if (fileOrFolder) {
				f = ToolModule.chooseFile(true, extension, description, null, new File(tf.getText()).getParent(), "Select a File");
			} else
				f = ToolModule.chooseDirectory(true, null, new File(tf.getText()).getParent(), "Select a Folder");
			if (f != null)
				tf.setText(f.getAbsolutePath());
		}
	}

	public void actionPerformed(ActionEvent e) {
        if (e.getActionCommand().equals("apply"))
        {
        	titleS = title.getText();
        	authorS = author.getText();
        	descriptionS = description.getText();
        	
    		proj.setName(titleS);
    		proj.setAuthor(authorS);
    		proj.setDescription(descriptionS);
    		
    		Ebhack.main.updateTitle();
        }
        else if (e.getActionCommand().equals("close"))
        {
            hide();
        }
	}

}
