package at.fhooe.mcm441.client;

import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.widgets.Display;

public class UiUtil {
	private UiUtil() {}
	
	public static Image loadImage(Display display, String name) {
		Image img = null;
		try {
			// try loading from filesystem
			img = new Image(display, "src/resources/" + name);
		} catch (Exception _e) {}
		
		if (img == null) {
			try {
				// try loading from within jar
				img = new Image(display, App.class.getResourceAsStream("/resources/" + name));
			} catch (Exception _e) {}	
		}
		
		return img;
	}

}
