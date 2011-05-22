package at.fhooe.mcm441.client;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.DisposeEvent;
import org.eclipse.swt.events.DisposeListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Group;
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

import at.fhooe.mcm441.commons.network.IConnectionStatusListener;
import at.fhooe.mcm441.commons.network.NetworkServiceClient;
import at.fhooe.mcm441.commons.protocol.IClientSideListener;
import at.fhooe.mcm441.sensor.Sensor;

public class SensorViewer implements IClientSideListener, IConnectionStatusListener {
	private static final Logger log = org.slf4j.LoggerFactory.getLogger(SensorViewer.class.getName());

	public static String HOST = "localhost";
	public static int PORT = 4444;
	
	protected static final boolean LOGGING = true;

	public Connection m_con;
	protected boolean m_autoRegister = false;
	protected ArrayList<String> m_sensors = new ArrayList<String>();
	//key = sensorID
	protected static Map<String, Sensor> m_sensors_map = new HashMap<String, Sensor>();

	protected Boolean connected = null;
	private static int clientsConnectedCount = 0;
	protected static long msgsReceivedCount = 0;
	
	public static void main(String[] args) throws Exception{
		
		SensorViewer client = new SensorViewer();
		new ClientIpInput(client);
	}
	
	Shell m_shell;
	protected Display m_display;
	
	Text winConsole = null;
	Button button = null;
	
	private NetworkServiceClient client;

	//Gui elements
	protected Composite composite1;
	protected Composite composite2;
	protected Group m_group1;
	protected TabFolder m_tabFolder;
	
	public void startSensorViewer(boolean autoRegister, int disconnectAfterSeconds) throws Exception {
		m_autoRegister = autoRegister;
		
		
		m_display = Display.getDefault();
		m_shell = new Shell(m_display);
		setupGui();
		
		newConnection();

		while (connected == null) {
			Thread.sleep(20);
		}		
		
		runGui();
	}
	
	public void setConnectionInfo(String HOST, int PORT)
	{
		SensorViewer.HOST = HOST;
		SensorViewer.PORT = PORT;
	}
	
	public void newConnection() throws Exception
	{
		m_con = new Connection(HOST, PORT, this, this);
	}
	
	public void setupGui() {
		GridData gridData;
		
		GridLayout layout = new GridLayout(2, false);
		gridData = new GridData();
		m_shell.setLayout(layout);
		
		composite1 = new Composite(m_shell, SWT.NONE);
		gridData = new GridData(150,400);
		composite1.setLayoutData(gridData);
		composite1.setLayout(new GridLayout(2, false));
		//composite1.setLayout(new FillLayout(SWT.VERTICAL));
		
		m_group1 = new Group(composite1, SWT.NULL);
		m_group1.setText("Sensors");
		gridData = new GridData(150, 200);
		gridData.verticalAlignment = GridData.BEGINNING;
		m_group1.setLayoutData(gridData);
		m_group1.setLayout(new GridLayout(1, false));
		
		composite2 = new Composite(m_shell, SWT.NONE);
		gridData = new GridData(450,400);
		gridData.horizontalAlignment = GridData.FILL;
		gridData.verticalAlignment = GridData.FILL;
		gridData.grabExcessHorizontalSpace = true;
		gridData.grabExcessVerticalSpace = true;
		composite2.setLayoutData(gridData);
		composite2.setLayout(new FillLayout());
		
		m_tabFolder = new TabFolder(composite2, SWT.BORDER);
		m_tabFolder.setLayout(new FillLayout());
	    
	    
		m_shell.setText("Sensor Manager");
		
		m_shell.addDisposeListener(new DisposeListener() {
		      public void widgetDisposed(DisposeEvent event) {
		        m_shell.dispose();
		      }
	    });
	}
	
	public void runGui() {
			
			m_shell.pack();
			
			m_shell.open();
			while (!m_shell.isDisposed ()) {
				if (!m_display.readAndDispatch ()) m_display.sleep ();
			}
			m_display.dispose();
			
			if (client != null)
				client.stop();
			System.exit(0);
		}

	
	public void addGuiElements(final Sensor sensor)
	{
        m_shell.getDisplay().syncExec( new Runnable() {
            public void run() {

            	//set checkbox for sensor
            	final Button sensorCheckbox = new Button(m_group1, SWT.CHECK);
            	sensorCheckbox.setData("sensor", sensor);
            	sensorCheckbox.setForeground(m_display.getSystemColor(SWT.COLOR_DARK_MAGENTA));
            	sensorCheckbox.setText(sensor.description);

            	sensorCheckbox.addSelectionListener(new SelectionAdapter() {
            	      public void widgetSelected(SelectionEvent event) {
            	    	  if(sensorCheckbox.getSelection())
            	    	  {
            	    		  addChartTab(sensor);
            	    		  registerForSensor(sensor.ident, true);
            	    		  if (LOGGING) {
            	    			  log.info("registering for sensor " + sensor.ident);
            	    		  }
            	    	  }
            	    	  else
            	    	  {
            	    		  removeChartTab(sensor.ident);
            	    		  registerForSensor(sensor.ident, false);
            	    	  }	                 	          
            	        }
            	      });
            	
            	m_group1.pack();
            }
        }); 
	}
	
	
	public void registerForSensor(String sensorID, boolean register)
	{
		m_con.registerForSensor(sensorID, register);
	}

	public void addChartTab(Object object)
	{
		Sensor sensor = (Sensor) object;
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
		Chart newChart = LineChartExample.createChart(container);
		
		//set first value of chart to 0
		double[] ySeries = { 0.0 };
		ISeriesSet seriesSet = newChart.getSeriesSet();
		ISeries series = seriesSet.createSeries(SeriesType.LINE, "line series");
		series.setYSeries(ySeries);
		
		ITitle graphTitle = newChart.getTitle();
		graphTitle.setText(sensor.description);
		newChart.getAxisSet().getXAxes()[0].getTitle().setText("number of values");
		newChart.getAxisSet().getYAxes()[0].getTitle().setText(sensor.dataType);
	}
	
	public void removeGuiElements(final String sensorId)
	{	
		new Thread( new Runnable() {
            public void run() {
                m_shell.getDisplay().syncExec( new Runnable() {
                    public void run() {
                		Control[] checkboxchilds = m_group1.getChildren();
                		for (Control child : checkboxchilds) {
                			Sensor s = (Sensor) child.getData("sensor");
                			if(s.ident.equals(sensorId))
                			{
                				child.dispose();
                				break;
                			}
                		}
                		
                		removeChartTab(sensorId);
                		
                		m_group1.pack();
                    }
                }); 
            }
        } ).start();
	}
	
	public void removeChartTab(String sensorId)
	{
		
		TabItem[] tabchilds = m_tabFolder.getItems();
		for (TabItem child : tabchilds) {
			if(child.getData("tabID").equals(sensorId))
			{
				child.getControl().dispose();
				child.dispose();
				break;
			}
		}
	}

	
	@Override
	public void onSensorActivated(Sensor s) {
		if (LOGGING)
			log.info("sensor activated: " + s);
		if (m_autoRegister) {
			if (LOGGING) {
				log.info("automatically registering for sensor!");
			}
			 else {
				//log.info("already registered for that sensor");
			}
		}
		
		if (!m_sensors.contains(s.ident)) {				
			m_sensors.add(s.ident);
			addGuiElements(s);
		}
		
		msgsReceivedCount++;
	}
	
	

	@Override
	public void onSensorDeactivated(String sensorId) {
		if (LOGGING)
			log.info("sensor " + sensorId + " deactivated");
		m_sensors.remove(sensorId);
		removeGuiElements(sensorId);
		msgsReceivedCount++;
	}

	@Override
	public void onNewSensorData(final String sensorId, double value) {
		if (LOGGING) {
			log.info("sensordata " + sensorId + " " + value);
		}
		msgsReceivedCount++;

		//add new data to chart diagramm
		if (LOGGING) {
			log.info("NEW VAL!");
		}
		
		final double val = value;
		
		m_display.syncExec(new Runnable() {
        	@Override
			public void run() {
try {
				//get right tab item container to adjust chart
				Control[] tabchilds = m_tabFolder.getChildren();
				Chart targetChart = null;
        		for (Control child : tabchilds) {
        			Object o = child.getData("chartID");
        			if(o != null && o.equals(sensorId))
        			{
        				Composite container = (Composite) child;
        				targetChart = (Chart) container.getChildren()[0];
        				break;
        			}
        		}
				
        		if (targetChart != null) {
        			try { 
					ILineSeries lineSeries = (ILineSeries) targetChart.getSeriesSet().getSeries()[0];
					double[] vals = lineSeries.getYSeries();
					if (vals == null) {
						vals = new double[]{0.0, 1, -1, 0.0};
					}
					double[] newvals = new double[vals.length + 1];
					System.arraycopy(vals, 0, newvals, 0, vals.length);
					newvals[newvals.length - 1] = val;
					lineSeries.setYSeries(newvals);
					targetChart.getAxisSet().adjustRange();
					targetChart.redraw();
        			} catch (Exception e) {
        				e.printStackTrace();
        			}
        		} else {
        			log.warn("no such sensor-chart!");
        		}
} catch (Exception e) {
	e.printStackTrace();
}
			}});
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

}


