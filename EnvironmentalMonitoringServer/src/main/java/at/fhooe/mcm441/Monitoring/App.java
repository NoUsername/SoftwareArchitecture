package at.fhooe.mcm441.Monitoring;

import java.io.InputStream;
import java.util.ResourceBundle;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.slf4j.Logger;

/**
 * Hello world!
 *
 */
public class App 
{
    public static void main( String[] args )
    {
    	Logger log = org.slf4j.LoggerFactory.getLogger("Main");
		log.trace("test");
		log.debug("test");
		log.info("test");
		log.warn("test");
		log.error("test");
		tryGui();
	}
	
	private static void tryGui() {
		Display display = new Display ();
		Shell shell = new Shell(display);
		
		final Label l = new Label(shell, SWT.CENTER);
		l.setText("hello?");
		
		Button b = new Button(shell, SWT.TOGGLE);
		b.setText("click me");
		
		Image img = null;
		try {
			// try loading from filesystem
			img = new Image(display, "src/resources/plugin.png");
		} catch (Exception _e) {}
		if (img == null) {
			try {
				// try loading from within jar
				img = new Image(display, App.class.getResourceAsStream("/resources/plugin.png"));
			} catch (Exception _e) {}	
		}
		if (img != null)
			b.setImage(img);
		
		b.addSelectionListener(new SelectionListener() {
			@Override
			public void widgetDefaultSelected(SelectionEvent e) {
				l.setText(l.getText() + "?");
			}

			@Override
			public void widgetSelected(SelectionEvent e) {
				l.setText(l.getText() + ".");
			}
		});
		
		shell.setText("I am a window title =)");
		
		shell.setLayout(new FillLayout(SWT.VERTICAL));
		shell.setMinimumSize(300, 50);
		shell.pack();
		shell.open();
		while (!shell.isDisposed ()) {
			if (!display.readAndDispatch ()) display.sleep ();
		}
		display.dispose ();	
	}
}
