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

public class SensorManager implements IPackageListener {
	private final Logger log = org.slf4j.LoggerFactory.getLogger(this.getClass().getName());
	
	public static void main(String[] args) {
		SensorManager sc = new SensorManager();
		
		sc.runGui();
	}
	
	public static final int PORT = 4444;
	
	private Shell shell;
	private Display display;
	
	Text winConsole = null;
	Button button = null;
	
	private NetworkService server;
	private NetworkServiceClient client;

	public SensorManager() {
		setupGui();
	}
	
	private void setupGui() {
		display = new Display ();
		shell = new Shell(display);

		// Create a new Gridlayout with 2 columns where the 2 column do no need
		// to be same size
		GridLayout layout = new GridLayout(3, false);
		// set the layout of the shell
		shell.setLayout(layout);
		// Create a label and a button


		GridData gridData;
		
		Composite composite1 = new Composite(shell, SWT.NONE);
		gridData = new GridData(150,400);
		composite1.setLayoutData(gridData);
		composite1.setLayout(new GridLayout(2, false));
		composite1.setLayout(new FillLayout(SWT.VERTICAL));
		
		Group group1 = new Group(composite1, SWT.NULL);
		group1.setText("Sensors");
		gridData = new GridData(150, 200);
		group1.setLayoutData(gridData);
		group1.setLayout(new GridLayout(1, false));
		
		new Button(group1, SWT.CHECK).setText("Checkbox 1");
		new Button(group1, SWT.CHECK).setText("Checkbox 2");
		new Button(group1, SWT.CHECK).setText("Checkbox 3");
		
		Group group2 = new Group(composite1, SWT.NULL);
		group2.setText("connected clients");
		gridData = new GridData(150, 200);
		group2.setLayoutData(gridData);
		group2.setLayout(new GridLayout(1, false));
		
		// Create a read-only text field
	    Text clients = new Text(group2, SWT.READ_ONLY | SWT.BORDER);
	    clients.setText("10.0.0.42");
	    clients.setText("192.186.0.1");
		
		Composite composite2 = new Composite(shell, SWT.NONE);
		gridData = new GridData(450,400);
		composite2.setLayoutData(gridData);
//		composite2.setLayout(new GridLayout(1, false));
		composite2.setLayout(new FillLayout(SWT.VERTICAL));
		
		final TabFolder tabFolder = new TabFolder(composite2, SWT.BORDER);
		TabItem tabItem = new TabItem(tabFolder, SWT.NULL);
	    tabItem.setText("Tab ");
	      
	    TabItem tabItem2 = new TabItem(tabFolder, SWT.NULL);
	    tabItem2.setText("Tab ");
	    
	    Group group3 = new Group(composite2, SWT.NULL);
	    group3.setText("settings");
		gridData = new GridData(GridData.VERTICAL_ALIGN_FILL);
		group3.setLayoutData(gridData);
		group3.setLayout(new GridLayout(1, false));
		
		Composite composite3 = new Composite(group3, SWT.NONE);
		gridData = new GridData(GridData.END);
		composite3.setLayoutData(gridData);
		composite3.setLayout(new FillLayout(SWT.HORIZONTAL));
		
		Label label_freq = new Label(composite3, SWT.LEFT);
		label_freq.setText("polling frequency    ");

	    Text text_freq = new Text(composite3, SWT.BORDER);
	    text_freq.setLocation(100, 100);
			
	    Button setButton = new Button(group3, SWT.PUSH);
	    setButton.setAlignment(SWT.BOTTOM);
	    setButton.setText("set");
	    setButton.pack();
		
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


