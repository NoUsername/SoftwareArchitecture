package at.fhooe.mcm441.client;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.DisposeEvent;
import org.eclipse.swt.events.DisposeListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.TabFolder;
import org.eclipse.swt.widgets.TabItem;
import org.eclipse.swt.widgets.Text;
import org.slf4j.Logger;
import org.swtchart.Chart;
import org.swtchart.ILineSeries;
import org.swtchart.ISeries;
import org.swtchart.ISeries.SeriesType;
import org.swtchart.ISeriesSet;
import org.swtchart.ITitle;

import at.fhooe.mcm441.commons.Configuration;
import at.fhooe.mcm441.commons.network.Client;
import at.fhooe.mcm441.commons.network.IConnectionStatusListener;
import at.fhooe.mcm441.commons.network.NetworkService;
import at.fhooe.mcm441.commons.network.NetworkServiceClient;
import at.fhooe.mcm441.commons.protocol.IAdminClientSideListener;
import at.fhooe.mcm441.sensor.Sensor;

public class SensorManager implements IAdminClientSideListener,
IConnectionStatusListener {
	private static final Logger log = org.slf4j.LoggerFactory.getLogger(SensorManager.class.getName());

	public static String HOST = "localhost";
	public static int PORT = 4445;
	
	private static final boolean HARDCORETEST = false; // if this is true, not one but MANY clients are started
	private static final boolean LOGGING = !HARDCORETEST;
	
	private static final int MIN_STAY_CONNECTED_TIME = 120; // seconds
	private static final int STARTED_CLIENTS_COUNT = 100;
	private static final int MIN_STARTING_OFFSET = 100; // milliseconds

	public AdminConnection m_con;
	private boolean m_autoRegister = false;
	private ArrayList<String> m_sensors = new ArrayList<String>();

	private Boolean connected = null;
	private static int clientsConnectedCount = 0;
	private static long msgsReceivedCount = 0;
	
	//key = sensorID
	private Map<String, Configuration> m_configItems = new HashMap<String, Configuration>();
	
	public static void main(String[] args) throws Exception{
		SensorManager sc = new SensorManager(true, 0);
		
//		sc.runGui();
	}
	
	private Shell m_shell;
	private Display m_display;
	
	Text winConsole = null;
	Button button = null;
	
	private NetworkService server;
	private NetworkServiceClient client;

	//Gui elements
	private Group m_group1;
	private Group m_group2;
	private Group m_group3;
	private TabFolder m_tabFolder;
	private Chart m_curChart = null;
	
	public SensorManager(boolean autoRegister, int disconnectAfterSeconds) throws Exception {
		m_autoRegister = autoRegister;
		m_con = new AdminConnection(HOST, PORT, this, this);

		while (connected == null) {
			Thread.sleep(20);
		}
		
		if (disconnectAfterSeconds > 0) {
			new Timer().schedule(new TimerTask() {
				@Override
				public void run() {
					if (LOGGING)
						log.info("AUTOMATICALLY DISCONNECTING");
					m_con.close();
				}
			}, 1000 * disconnectAfterSeconds);
		}
		m_display = new Display ();
		m_shell = new Shell(m_display);
		setupGui();
	}
	
	private void setupGui() {
		

		// Create a new Gridlayout with 2 columns where the 2 column do no need
		// to be same size
		GridLayout layout = new GridLayout(3, false);
		// set the layout of the shell
		m_shell.setLayout(layout);
		// Create a label and a button

		GridData gridData;
		
		Composite composite1 = new Composite(m_shell, SWT.NONE);
		gridData = new GridData(150,400);
		composite1.setLayoutData(gridData);
		composite1.setLayout(new GridLayout(2, false));
		composite1.setLayout(new FillLayout(SWT.VERTICAL));
		
		m_group1 = new Group(composite1, SWT.NULL);
		m_group1.setText("Sensors");
		gridData = new GridData(150, 200);
		m_group1.setLayoutData(gridData);
		m_group1.setLayout(new GridLayout(1, false));
	    
		m_group2 = new Group(composite1, SWT.NULL);
		m_group2.setText("connected clients");
		gridData = new GridData(150, 200);
		m_group2.setLayoutData(gridData);
		m_group2.setLayout(new GridLayout(1, false));
		
		Composite composite2 = new Composite(m_shell, SWT.NONE);
		gridData = new GridData(450,400);
		composite2.setLayoutData(gridData);
		composite2.setLayout(new FillLayout(SWT.VERTICAL));
		
		m_tabFolder = new TabFolder(composite2, SWT.BORDER);
	    
	    m_group3 = new Group(composite2, SWT.NULL);
	    m_group3.setText("settings");
		gridData = new GridData(GridData.VERTICAL_ALIGN_FILL);
		m_group3.setLayoutData(gridData);
		m_group3.setLayout(new GridLayout(1, false));
		
		Composite composite3 = new Composite(m_group3, SWT.NONE);
		gridData = new GridData(GridData.END);
		composite3.setLayoutData(gridData);
		composite3.setLayout(new FillLayout(SWT.HORIZONTAL));
		
		Label label_freq = new Label(composite3, SWT.LEFT);
		label_freq.setText("polling frequency    ");

	    final Text text_freq = new Text(composite3, SWT.BORDER);
	    text_freq.setLocation(100, 100);
			
	    Button setButton = new Button(composite3, SWT.PUSH);
	    setButton.setAlignment(SWT.BOTTOM);
	    setButton.setText("set");
	    setButton.addSelectionListener(new SelectionListener() {
			@Override
			public void widgetDefaultSelected(SelectionEvent e) {
			}
			@Override
			public void widgetSelected(SelectionEvent e) {
				System.out.println(text_freq.getText());
			}
	    });
	    
		m_shell.setText("Sensor Manager");
	
		m_shell.setMinimumSize(400, 400);
		m_shell.pack();
		
		m_shell.addDisposeListener(new DisposeListener() {
		      public void widgetDisposed(DisposeEvent event) {
		        m_shell.dispose();
		      }
		    });

		runGui();
	}

	
	public void addSensorCheckbox(final Sensor sensor)
	{
		new Thread( new Runnable() {
            public void run() {
                try {
                    Thread.sleep(1000);
                } catch (InterruptedException e1) {
                    e1.printStackTrace();
                }
                m_shell.getDisplay().syncExec( new Runnable() {
                    public void run() {
                    	
                    	//set checkbox for sensor
                    	final Button checkbox = new Button(m_group1, SWT.CHECK);
                    	checkbox.setData(sensor.ident);
                    	checkbox.setForeground(m_display.getSystemColor(SWT.COLOR_DARK_MAGENTA));
                    	checkbox.setText(sensor.description);
                    	checkbox.addSelectionListener(new SelectionAdapter() {
                    	      public void widgetSelected(SelectionEvent event) {
                    	    	  if(checkbox.getSelection())
                    	    	  {
                    	    		  m_configItems.get(checkbox.getData()).value = "activ";
                    	    	  }
                    	    	  else
                    	    	  {
                    	    		  m_configItems.get(checkbox.getData()).value = "inactiv";
                    	    	  }	                 	          
                    	        }
                    	      });
                    	
                    	//set Tab
                    	TabItem tabItem = new TabItem(m_tabFolder, SWT.NULL);
                	    tabItem.setText(sensor.description);
                	    tabItem.setData(sensor.ident);
                	    tabItem.setData("tabID", sensor.ident);
                	    
                	    //set chart in tab item
                	    Composite container = new Composite(m_tabFolder, SWT.NONE);
    					container.setLayout(new FillLayout(SWT.VERTICAL));
    					container.setData("chartID", sensor.ident);
    					tabItem.setControl (container);
    					m_curChart = LineChartExample.createChart(container);
    					
    					//set first value of chart to 0
    					double[] ySeries = { 0.0 };
    					ISeriesSet seriesSet = m_curChart.getSeriesSet();
    					ISeries series = seriesSet.createSeries(SeriesType.LINE, "line series");
    					series.setYSeries(ySeries);
    					
    					ITitle graphTitle = m_curChart.getTitle();
    					graphTitle.setText(sensor.description);
    					m_curChart.getAxisSet().getXAxes()[0].getTitle().setText("number of values");
    					m_curChart.getAxisSet().getYAxes()[0].getTitle().setText(sensor.dataType);
					
                    	m_group1.pack();
                    }
                }); 
            }
        } ).start();
	}
	
	public void removeSensorCheckbox(final String sensorId)
	{	
		new Thread( new Runnable() {
            public void run() {
                try {
                    Thread.sleep(1000);
                } catch (InterruptedException e1) {
                    e1.printStackTrace();
                }
                m_shell.getDisplay().syncExec( new Runnable() {
                    public void run() {
                    	
                		Control[] checkboxchilds = m_group1.getChildren();
                		for (Control child : checkboxchilds) {
                			if(child.getData().equals(sensorId))
                			{
                				child.dispose();
                				break;
                			}
                		}
                		TabItem[] tabchilds = m_tabFolder.getItems();
                		for (TabItem child : tabchilds) {
                			if(child.getData("tabID").equals(sensorId))
                			{
                				child.dispose();
                				break;
                			}
                		}
                		m_group1.pack();
                    }
                }); 
            }
        } ).start();
	}
	
	public void runGui() {
		m_shell.open();
		while (!m_shell.isDisposed ()) {
			if (!m_display.readAndDispatch ()) m_display.sleep ();
		}
		m_display.dispose();
		
		if (client != null)
			client.stop();
		if (server != null)
			server.stop();
	}

	@Override
	public void onSensorActivated(Sensor s) {
		if (LOGGING)
			log.info("sensor activated: " + s);
		if (m_autoRegister) {
			if (LOGGING)
				log.info("automatically registering for sensor!");
			if (!m_sensors.contains(s.ident)) {
				m_con.registerForSensor(s.ident, true);
				m_sensors.add(s.ident);
			} else {
				//log.info("already registered for that sensor");
			}
		}
		addSensorCheckbox(s);
		msgsReceivedCount++;
	}

	@Override
	public void onSensorDeactivated(String sensorId) {
		if (LOGGING)
			log.info("sensor " + sensorId + " deactivated");
		m_sensors.remove(sensorId);
		removeSensorCheckbox(sensorId);
		msgsReceivedCount++;
	}

	@Override
	public void onNewSensorData(final String sensorId, double value) {
		if (LOGGING)
			log.info("sensordata " + sensorId + " " + value);
		msgsReceivedCount++;

		//add new data to chart diagramm
			try {
				log.info("NEW VAL!");
        		
				final double val = value;
				if (m_curChart != null) {
					
					new Thread( new Runnable() {
			            public void run() {
			                try {
			                    Thread.sleep(1000);
			                } catch (InterruptedException e1) {
			                    e1.printStackTrace();
			                }
			                m_shell.getDisplay().syncExec(new Runnable() {
						public void run() {

							//get right tab item container to adjust chart
							Control[] tabchilds = m_tabFolder.getChildren();
	                		for (Control child : tabchilds) {
	                			if(child.getData("chartID").equals(sensorId))
	                			{
	                				Composite container = (Composite) child;
	                				m_curChart = (Chart) container.getChildren()[0];
	                				break;
	                			}
	                		}
							
							ILineSeries lineSeries = (ILineSeries) m_curChart.getSeriesSet().getSeries()[0];
							double[] vals = lineSeries.getYSeries();
							if (vals == null)
								vals = new double[]{0.0, 1, -1, 0.0};
							double[] newvals = new double[vals.length + 1];
							System.arraycopy(vals, 0, newvals, 0, vals.length);
							newvals[newvals.length - 1] = val;
							lineSeries.setYSeries(newvals);
							m_curChart.getAxisSet().adjustRange();
							m_curChart.redraw();
						}});
				}

		        } ).start();
				
				}	
			} catch (Exception e) {
				log.warn("could not adjust chart ", e);
			}
	}

	@Override
	public void onConnectionEstablished() {
		if (LOGGING)
			log.info("we are connected");
		connected = Boolean.TRUE;
		clientsConnectedCount++;
	}

	@Override
	public void onConnectionLost() {
		if (LOGGING)
			log.info("we got disconnected");
		connected = Boolean.FALSE;
	}

	@Override
	public void onSensorConfigurationItem(String sensorId, Configuration conf) {
		if (LOGGING)
			log.info("sensor conf item for sensor " + sensorId + " " + conf);
		msgsReceivedCount++;
		m_configItems.put(sensorId, conf);
	}

	@Override
	public void onServerConfigurationItem(Configuration conf) {
		if (LOGGING)
			log.info("server conf item " + conf);
		msgsReceivedCount++;
	}

	@Override
	public void onClientConnected(final Client client) {
		if (LOGGING)
			log.info("client connected " + client);
		msgsReceivedCount++;
		
		new Thread( new Runnable() {
            public void run() {
                try {
                    Thread.sleep(1000);
                } catch (InterruptedException e1) {
                    e1.printStackTrace();
                }
                m_shell.getDisplay().syncExec( new Runnable() {
                    public void run() {
                    	
                    	//show connect clients
                    	// Create a read-only text field
                	    Text clients = new Text(m_group2, SWT.READ_ONLY | SWT.BORDER);
                	    clients.setText(client.m_address);
                	    clients.setData("client", client.m_id);
                	    
                	    m_group2.pack();
                    }
                }); 
            }
        } ).start();
	}

	@Override
	public void onClientDisconnected(final Client client) {
		if (LOGGING)
			log.info("client disconnected " + client);
		msgsReceivedCount++;
		
		new Thread( new Runnable() {
            public void run() {
                try {
                    Thread.sleep(1000);
                } catch (InterruptedException e1) {
                    e1.printStackTrace();
                }
                m_shell.getDisplay().syncExec( new Runnable() {
                    public void run() {
                    	
                    	//remove clients from gui
                    	Control[] checkboxchilds = m_group1.getChildren();
                		for (Control child : checkboxchilds) {
                			if(child.getData("client").equals(client.m_id))
                			{
                				child.dispose();
                				break;
                			}
                		}
                    }
                }); 
            }
        } ).start();
	}	
}


