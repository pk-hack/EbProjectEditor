package ebhack;

import java.io.File;

public class Ebhack {
    public static final File EBHACK_DIR = new File(System
            .getProperty("user.home")
            + File.separator + ".ebprojeditor");
    
    static
    {
        if (!EBHACK_DIR.exists())
            EBHACK_DIR.mkdir();
    }
    
    public static MainGUI main;
    
	public static void main(String[] args) {
        main = new MainGUI();
        if (args.length >= 1) {
            main.init(args[0]);
        } else {
            main.init();
        }
	}
}
