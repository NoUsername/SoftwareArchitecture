package at.fhooe.mcm441.Monitoring;

import java.net.InetAddress;
import java.net.UnknownHostException;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;
import org.slf4j.Logger;

import at.fhooe.mcm441.Monitoring.network.NetworkService;
import at.fhooe.mcm441.Monitoring.network.NetworkServiceClient;
import at.fhooe.mcm441.Monitoring.network.PackageListener;

public class SampleClient implements PackageListener {
	private final Logger log = org.slf4j.LoggerFactory.getLogger(this.getClass().getName());
	
	public static void main(String[] args) {
		SampleClient sc = new SampleClient();
		
		sc.runGui();
	}
	
	public static final int PORT = 4444;
	
	private Shell shell;
	private Display display;
	
	Text winConsole = null;
	Button button = null;
	
	private NetworkService server;
	private NetworkServiceClient client;

	public SampleClient() {
		setupGui();
	}
	
	private void setupGui() {
		display = new Display ();
		shell = new Shell(display);
		
		final Label l = new Label(shell, SWT.CENTER);
		l.setText("hello?");
		
		button = new Button(shell, SWT.TOGGLE);
		button.setText("click me");
		
		final Text t = new Text(shell, SWT.SINGLE);
		t.setText("leave_this_empty_and_click_button_to_be_server click_with_text_to_be_client");
		
		winConsole = new Text(shell, SWT.MULTI | SWT.WRAP | SWT.V_SCROLL | SWT.H_SCROLL);
		winConsole.setText("console");
		winConsole.setSize(300, 200);
		
		
		
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
			button.setImage(img);
		
		final SampleClient thisClient = this;
		
		button.addSelectionListener(new SelectionListener() {
			@Override
			public void widgetDefaultSelected(SelectionEvent e) {
				l.setText(l.getText() + "?");
			}

			@Override
			public void widgetSelected(SelectionEvent e) {
				l.setText(l.getText() + ".");
				if (t.getText().trim().equals("")) {
					// start the server!
					if (server == null) {
						server = new NetworkService(thisClient);
					}
					if (!server.isRunning()) {
						if (server.startListening(PORT)) {
							log.info("server started");
							button.setText("SERVER RUNNING...");
						} else {
							winConsole.append("\r\ncannot start server");
						}
					}
				} else {
					// be a client & send
					if (client == null) {
						client = new NetworkServiceClient(thisClient);
						button.setText("client, click to send text");
					}
					
					if (!client.isRunning()) {
						// could be because of first time or connection lost
						try {
							client.connectAndStart(InetAddress.getByName("localhost"), PORT);
							log.info("client started");
						} catch (UnknownHostException e1) {
							client = null;
							e1.printStackTrace();
						}
					}
					
					if (client != null)
					{
						if (client.sendMessage(t.getText())) {
							log.info("msg sent: " + t.getText());
						} else {
							log.info("could not send?!");
						}
					}
				}
			}
		});
		
		shell.setText("I am a window title =)");
		
		shell.setLayout(new FillLayout(SWT.VERTICAL));
		shell.setMinimumSize(300, 50);
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


