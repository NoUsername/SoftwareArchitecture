package at.fhooe.mcm441.client;

import java.net.InetAddress;
import java.net.UnknownHostException;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.layout.RowLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.TabFolder;
import org.eclipse.swt.widgets.TabItem;
import org.eclipse.swt.widgets.Text;
import org.slf4j.Logger;
import org.swtchart.Chart;
import org.swtchart.ILineSeries;
import org.swtchart.ISeries.SeriesType;

import at.fhooe.mcm441.commons.network.IPackageListener;
import at.fhooe.mcm441.commons.network.NetworkService;
import at.fhooe.mcm441.commons.network.NetworkServiceClient;

public class SampleChartApp implements IPackageListener {
	private final Logger log = org.slf4j.LoggerFactory.getLogger(this.getClass().getName());
	
	public static void main(String[] args) {
		SampleChartApp sc = new SampleChartApp();
		sc.runGui();
	}
	
	public static final int PORT = 4444;
	
	private Shell shell;
	private Display display;
	
	Text userInput = null;
	Text winConsole = null;
	Button button = null;
	TabFolder tabFolder = null;
	
	Chart curChart = null;
	
	private NetworkService server;
	private NetworkServiceClient client;

	public SampleChartApp() {
		setupGui();
	}
	
	private void setupGui() {
		display = new Display ();
		shell = new Shell(display);
		
		Composite left = new Composite(shell, SWT.NONE);
		left.setSize(40, 500);
		setupGuiLeft(left);
		
		Composite right = new Composite(shell, SWT.NONE);
		right.setSize(260, 500);
		right.setLayout(new FillLayout(SWT.VERTICAL));
		setupGuiRight(right);
		right.pack();
		
		shell.setText("I am a window title =)");
		
		shell.setLayout(new FillLayout(SWT.HORIZONTAL));
		shell.setMinimumSize(300, 200);
		shell.pack();
	}
	
	private void setupGuiLeft(Composite shell) {
		RowLayout layout = new RowLayout();
		layout.type = SWT.VERTICAL;
		
		shell.setLayout(layout);
		
		final Label l = new Label(shell, SWT.CENTER);
		l.setText("hello?");
		
		button = new Button(shell, SWT.TOGGLE);
		button.setText("click me");
		
		final Text txtHost = new Text(shell, SWT.SINGLE);
		txtHost.setText("localhost");
		
		Image img = UiUtil.loadImage(display, "plugin.png");
		if (img != null)
			button.setImage(img);
		
		final SampleChartApp thisClient = this;
		button.addSelectionListener(new SelectionListener() {
			@Override
			public void widgetDefaultSelected(SelectionEvent e) {
				l.setText(l.getText() + "?");
			}

			@Override
			public void widgetSelected(SelectionEvent e) {
				if (tabFolder.getItemCount() < 2) {
					TabItem item = new TabItem (tabFolder, SWT.NONE);
					
					item.setText ("TabItem " + tabFolder.getItemCount());
					Composite container = new Composite(tabFolder, SWT.NONE);
					container.setLayout(new FillLayout(SWT.VERTICAL));
					item.setControl (container);
					curChart = LineChartExample.createChart(container);
				}
				
				l.setText(l.getText() + ".");
				if (userInput.getText().trim().equals("")) {
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
							client.connectAndStart(InetAddress.getByName(txtHost.getText()), PORT);
							log.info("client started");
						} catch (UnknownHostException e1) {
							client = null;
							e1.printStackTrace();
						}
					}
					
					if (client != null)
					{
						String txt = userInput.getText();
						if (client.sendMessage(txt)) {
							log.info("msg sent: " + txt);
						} else {
							log.info("could not send?!");
						}
					}
				}
			}
		});
	}
	
	private void setupGuiRight(Composite shell) {
		
		tabFolder = new TabFolder (shell, SWT.BORDER);
		TabItem item = new TabItem (tabFolder, SWT.NONE);
		item.setText ("TabItem 0");
		Composite container = new Composite(tabFolder, SWT.NONE);
		container.setLayout(new FillLayout(SWT.VERTICAL));
		item.setControl (container);
		
		userInput = new Text(container, SWT.SINGLE);
		userInput.setText("leave_this_empty_and_click_button_to_be_server click_with_text_to_be_client");
		
		winConsole = new Text(container, SWT.MULTI | SWT.WRAP | SWT.V_SCROLL | SWT.H_SCROLL);
		winConsole.setText("  ");
		
		LineChartExample.createChart(container);	
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
				try {
					log.info("NEW VAL!");
					final int val = Integer.parseInt(s);
					if (curChart != null) {
						
						display.syncExec(new Runnable() {
							public void run() {
						
								ILineSeries lineSeries = (ILineSeries) curChart.getSeriesSet().getSeries()[0];
								double[] vals = lineSeries.getYSeries();
								if (vals == null)
									vals = new double[]{0.0, 1, -1, 0.0};
								double[] newvals = new double[vals.length + 1];
								System.arraycopy(vals, 0, newvals, 0, vals.length);
								newvals[newvals.length - 1] = val;
								lineSeries.setYSeries(newvals);
								curChart.getAxisSet().adjustRange();
								curChart.redraw();
							}});
					}
				} catch (Exception e) {
					log.warn("could not adjust chart ", e);
				}
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



class LineChartExample {

    private static final double[] ySeries = { 0.0, 0.38, 0.71, 0.92, 1.0, 0.92,
            0.71, 0.38, 0.0, -0.38, -0.71, -0.92, -1.0, -0.92, -0.71, -0.38 };

    /**
     * The main method.
     * 
     * @param args
     *            the arguments
     */
    public static void xmain(String[] args) {
        Display display = new Display();
        Shell shell = new Shell(display);
        shell.setText("Line Chart");
        shell.setSize(500, 400);
        shell.setLayout(new FillLayout());

        createChart(shell);

        shell.open();
        while (!shell.isDisposed()) {
            if (!display.readAndDispatch()) {
                display.sleep();
            }
        }
        display.dispose();
    }

    /**
     * create the chart.
     * 
     * @param parent
     *            The parent composite
     * @return The created chart
     */
    static public Chart createChart(Composite parent) {

        // create a chart
        Chart chart = new Chart(parent, SWT.NONE);

        // set titles
        chart.getTitle().setText("Line Chart");
        chart.getAxisSet().getXAxis(0).getTitle().setText("Data Points");
        chart.getAxisSet().getYAxis(0).getTitle().setText("Amplitude");

        // create line series
        ILineSeries lineSeries = (ILineSeries) chart.getSeriesSet()
                .createSeries(SeriesType.LINE, "line series");
        lineSeries.setYSeries(ySeries);

        // adjust the axis range
        chart.getAxisSet().adjustRange();

        return chart;
    }
}


