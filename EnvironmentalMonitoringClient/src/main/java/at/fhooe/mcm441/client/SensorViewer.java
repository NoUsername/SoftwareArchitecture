package at.fhooe.mcm441.client;

import java.awt.Color;
import java.net.InetAddress;
import java.net.UnknownHostException;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.layout.RowLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Spinner;
import org.eclipse.swt.widgets.TabFolder;
import org.eclipse.swt.widgets.TabItem;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;
import org.eclipse.swt.widgets.TableItem;
import org.eclipse.swt.widgets.Text;
import org.slf4j.Logger;

import at.fhooe.mcm441.commons.network.IPackageListener;
import at.fhooe.mcm441.commons.network.NetworkService;
import at.fhooe.mcm441.commons.network.NetworkServiceClient;

public class SensorViewer implements IPackageListener {
	private final Logger log = org.slf4j.LoggerFactory.getLogger(this.getClass().getName());
	
	public static void main(String[] args) {
		SensorViewer sc = new SensorViewer();
		
		sc.runGui();
	}
	
	public static final int PORT = 4444;
	
	private Shell shell;
	private Display display;
	
	Text winConsole = null;
	Button button = null;
	
	private NetworkService server;
	private NetworkServiceClient client;

	public SensorViewer() {
		setupGui();
	}
	
	private void setupGui() {
		display = new Display ();
		shell = new Shell(display);

		// Create a new Gridlayout with 2 columns where the 2 column do no need
		// to be same size
		GridLayout layout = new GridLayout(2, false);
		// set the layout of the shell
		shell.setLayout(layout);
		// Create a label and a button


		GridData gridData;
		
		Composite composite = new Composite(shell, SWT.BORDER);
		gridData = new GridData(150, 400);
		composite.setLayoutData(gridData);
		composite.setLayout(new GridLayout(1, false));
		
		new Button(composite, SWT.CHECK).setText("Checkbox 1");
		new Button(composite, SWT.CHECK).setText("Checkbox 2");
		new Button(composite, SWT.CHECK).setText("Checkbox 3");
		
		Composite composite2 = new Composite(shell, SWT.NONE);
		gridData = new GridData(450,400);
		composite2.setLayoutData(gridData);
		composite2.setLayout(new GridLayout(1, false));
		
		final TabFolder tabFolder = new TabFolder(composite2, SWT.BORDER);
		tabFolder.setSize(400, 100);
		TabItem tabItem = new TabItem(tabFolder, SWT.NULL);
	    tabItem.setText("Tab ");
	      
	    TabItem tabItem2 = new TabItem(tabFolder, SWT.NULL);
	    tabItem2.setText("Tab ");
		
		
		shell.setText("Sensor Viewer");
	
		shell.setMinimumSize(400, 400);
		shell.pack();
	}
	
	public void runGui() {
		shell.open();
		while (!shell.isDisposed ()) {
			if (!display.readAndDispatch ()) display.sleep ();
		}
		display.dispose ();
		
		if (client != null)
			client.stop();
		if (server != null)
			server.stop();
	}

	@Override
	public void onNewPackage(final String s) {
		if (server != null) {
			// on the server
			log.info("MSG FROM CLIENT: " + s);
			server.sendMessage("recieved!");
			
			String[] values = s.split(" ");
			if (values.length < 2) {
				return;
			}
			final String text = values[0];
			final String tag = values[1];
			display.syncExec(new Runnable() {
				public void run() {
					Button b = new Button(shell, SWT.DEFAULT);
					b.setText(text);
					
					b.addSelectionListener(new SelectionListener() {
						@Override
						public void widgetDefaultSelected(SelectionEvent e) {
						}
	
						@Override
						public void widgetSelected(SelectionEvent evt) {
							//System.out.println("button pressed with associated data: " + tag);
							try {
								server.sendMessage("clicked " + tag);
							} catch (Exception e) {
								log.warn("could not send associated data: " + tag);
							}
						}
					});
					
					shell.pack(true);
				}
			});
		
		} else {
			// this is on the client
			log.info("MSG FROM SERVER: " + s);
			
			display.syncExec(new Runnable() {
				public void run() {
					winConsole.append("\r\nFROM SERVER: " + s);
				}
			});
		}
	}
	
}


