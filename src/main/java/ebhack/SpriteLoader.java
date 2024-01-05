package ebhack;

import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;

import javax.imageio.ImageIO;

public class SpriteLoader extends ToolModule {
	private static ArrayList<Image[]> sprites = new ArrayList<Image[]>();
	private static ArrayList<Integer> widths = new ArrayList<Integer>();
	private static ArrayList<Integer> heights = new ArrayList<Integer>();

	public SpriteLoader(YMLPreferences prefs) {
		super(prefs);
	}

	public String getDescription() {
		return "Sprite Loader";
	}

	public String getVersion() {
		return "0.1";
	}

	public String getCredits() {
		return "Written by ghost";
	}

	public void init() {
		//sprites = new Image[464][8];
		//widths = new int[464];
		//heights = new int[464];
	}
	
	public static Image getSprite(int n, int direction) {
		return sprites.get(n)[direction];
	}
	
	public static int getSpriteW(int n) {
		return widths.get(n);
	}
	
	public static int getSpriteH(int n) {
		return heights.get(n);
	}

	public void load(Project proj) {
		int w, h, x, y, z;
		// TODO: this needs to be updated to be dynamic, but on testing it seems like this code is
		// never actually run. I'm stopping here because it's working.
		for (int i = 0; i < 464; ++i) {
			sprites.add(new Image[4]);
			try {
				BufferedImage sheet = ImageIO.read(new File(proj.getFilename(
						"eb.SpriteGroupModule",
						"SpriteGroups/" + ToolModule.addZeros(i+"", 3))));
				Graphics2D sg = sheet.createGraphics();
				w = sheet.getWidth()/4;
				widths.add(w);
				h = sheet.getHeight()/4;
				heights.add(h);
				z = 0;
				for (y=0; y<2; ++y) {
					for (x=0; x<4; x += 2) {
						BufferedImage sp = new BufferedImage(w, h, BufferedImage.TYPE_INT_ARGB);
						Graphics2D g = sp.createGraphics();
						g.setComposite(sg.getComposite());
						g.drawImage(sheet, 0, 0, w, h, w*x, h*y, w*x+w, h*y+h, null);
						g.dispose();
						sprites.get(i)[z] = sp;
						++z;
					}
				}
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}

	public void save(Project proj) {
		// Don't save
	}

	public void hide() {
		
	}

}
