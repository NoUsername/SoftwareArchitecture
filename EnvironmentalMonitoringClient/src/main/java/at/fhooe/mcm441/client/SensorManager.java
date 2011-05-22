package at.fhooe.mcm441.client;

import java.util.HashMap;
import java.util.Map;

import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.ScrolledComposite;
import org.eclipse.swt.events.ControlAdapter;
import org.eclipse.swt.events.ControlEvent;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;
import org.slf4j.Logger;

import at.fhooe.mcm441.commons.Configuration;
import at.fhooe.mcm441.commons.Configuration.SettingType;
import at.fhooe.mcm441.commons.network.Client;
import at.fhooe.mcm441.commons.protocol.IAdminClientSideListener;

/**
 * client for the admin
 * shows all active sensors with sensor data
 * and additionally all connected clients and 
 * the admin can configure the sensor settings (polling time, active/inactive)
 * 
 * @author Melanie Schmidt, Paul Klingelhuber
 *
 */
public class SensorManager extends SensorViewer implements
		IAdminClientSideListener {
	private static final Logger log = org.slf4j.LoggerFactory
			.getLogger(SensorManager.class.getName());

	public AdminConnection m_admin_con;

	public static int PORT = 4445;
	
	private Map<String, Control> m_all_configs;

	// key = sensorID
	private static Map<String, Configuration> m_configItems;

	public static void main(String[] args) throws Exception {
		SensorManager client = new SensorManager();
		new ClientIpInput(client);
	}

	// Gui elements
	private Group m_group2;
	private Group m_group3;
	private Composite composite3;
	private Composite composite4;

	public SensorManager()
			throws Exception {
		super();
	}

	@Override
	public void newConnection() throws Exception {
		m_admin_con = new AdminConnection(HOST, PORT, this, this);
		m_con = m_admin_con;
		m_configItems = new HashMap<String, Configuration>();
		m_all_configs = new HashMap<String, Control>();

	}

	@Override
	public void setupGui() {
		super.setupGui();

		GridData gridData;
		
		/*
		 * connected clients
		 */
		
		m_group2 = new Group(m_shell, SWT.V_SCROLL);
	    m_group2.setText("connected clients");
	    
		gridData = new GridData();
		gridData.horizontalAlignment = GridData.FILL;
		gridData.verticalAlignment = GridData.FILL;
		gridData.grabExcessVerticalSpace = true;
		gridData.grabExcessHorizontalSpace = true;
		
		m_group2.setLayoutData(gridData);
		m_group2.setLayout(new GridLayout(1, false));
		
	    final ScrolledComposite container2 = new ScrolledComposite (m_group2, SWT.BORDER | SWT.V_SCROLL | SWT.H_SCROLL);
		composite4 = new Composite(container2, SWT.NULL);

		composite4.setLayout(new GridLayout(1, false));

		container2.setContent(composite4);
		
		gridData = new GridData();
		gridData.grabExcessHorizontalSpace = true;
		gridData.grabExcessVerticalSpace = true;
		gridData.horizontalAlignment = GridData.FILL;
		gridData.verticalAlignment = GridData.FILL;
		container2.setLayoutData(gridData);
		
		container2.addControlListener(new ControlAdapter() {
		      public void controlResized(ControlEvent e) {
		        Rectangle r = container2.getClientArea();
		        container2.setMinSize(m_group2.computeSize(r.width,
		            SWT.DEFAULT));
		      }
		    });
		
		/*
		 * settings
		 */
		
	    m_group3 = new Group(m_shell, SWT.V_SCROLL);
	    m_group3.setText("settings");
	    
		gridData = new GridData();
		gridData.horizontalAlignment = GridData.FILL;
		gridData.verticalAlignment = GridData.FILL;
		gridData.grabExcessVerticalSpace = true;
		gridData.grabExcessHorizontalSpace = true;
		
		m_group3.setLayoutData(gridData);
		m_group3.setLayout(new GridLayout(1, false));
		
	    final ScrolledComposite container = new ScrolledComposite (m_group3, SWT.BORDER | SWT.V_SCROLL | SWT.H_SCROLL);
		composite3 = new Composite(container, SWT.NULL);

		composite3.setLayout(new GridLayout(3, false));

		container.setContent(composite3);
		
		gridData = new GridData();
		gridData.grabExcessHorizontalSpace = true;
		gridData.grabExcessVerticalSpace = true;
		gridData.horizontalAlignment = GridData.FILL;
		gridData.verticalAlignment = GridData.FILL;
		container.setLayoutData(gridData);
		
		container.addControlListener(new ControlAdapter() {
		      public void controlResized(ControlEvent e) {
		        Rectangle r = container.getClientArea();
		        container.setMinSize(m_group3.computeSize(r.width,
		            SWT.DEFAULT));
		      }
		    });
				
		container.setMinHeight(100);
		container.setMinWidth(200);
		
		m_shell.setText("SensorManager");

	}

	@Override
	public void onSensorConfigurationItem(final String sensorId,
			final Configuration conf) {
		if (LOGGING)
			log.info("sensor conf item for sensor " + sensorId + " " + conf);
		msgsReceivedCount++;

		m_configItems.put(sensorId, conf);

		m_display.syncExec(new Runnable() {
			@Override
			public void run() {
				createConfigItem(composite3, sensorId, conf);
			}
		});

	}

	@Override
	public void onServerConfigurationItem(final Configuration conf) {
		if (LOGGING) {
			log.info("server conf item " + conf);
		}

		m_display.syncExec(new Runnable() {
			@Override
			public void run() {
				createConfigItem(composite3, null, conf);
			}
		});

		msgsReceivedCount++;
	}

	@Override
	public void onClientConnected(final Client client) {
		if (LOGGING)
			log.info("client connected " + client);
		msgsReceivedCount++;

		m_shell.getDisplay().syncExec(new Runnable() {
			public void run() {

				// show connect clients
				// Create a read-only text field
				Text clients = new Text(composite4, SWT.READ_ONLY | SWT.BORDER);
				clients.setText(client.m_address);
				clients.setData("client", client.m_id);

				composite4.pack();
			}
		});
	}

	@Override
	public void onClientDisconnected(final Client client) {
		if (LOGGING)
			log.info("client disconnected " + client);
		msgsReceivedCount++;

		m_shell.getDisplay().syncExec(new Runnable() {
			public void run() {
				// remove clients from gui
				Control[] checkboxchilds = m_group2.getChildren();
				for (Control child : checkboxchilds) {
					if (client.m_id.equals(child.getData("client"))) {
						System.out.println("blub");
						child.dispose();
						break;
					}
				}
				m_group2.pack();
			}
		});
	}

	/**
	 * puts the configuration item into the target composite
	 * 
	 * @param target
	 * @param conf
	 */
	public void createConfigItem(Composite target, final String sensorId,
			final Configuration conf) {

		//Composite c = new Composite(target, SWT.NULL);
		//RowLayout rl = new RowLayout();
		Composite c = target;
		//rl.pack = true;
		
		// to prevent adding some config twice
		if(m_all_configs.containsKey(conf.id))
		{
			// try to update control value:
			Control ctrl = m_all_configs.get(conf.id);
			if (conf.type == SettingType.bool) {
				((Button)ctrl).setSelection("true".equals(conf.value));
			} else {
				((Text)ctrl).setText(conf.value);
			}
			return;
		}
		
		
		Label txt = new Label(c, SWT.NULL);
		txt.setText(conf.displayName);
		Control entryControl = null;
		if (conf.type == SettingType.text || conf.type == SettingType.number) {
			
			final Text entry = new Text(c, SWT.SINGLE | SWT.BORDER);
			entryControl = entry;
			entry.setText(conf.value);
			GridData gridData = new GridData(GridData.CENTER);
			gridData.horizontalAlignment = GridData.CENTER;
			entry.setLayoutData(gridData);
			
			Button apply = new Button(c, SWT.NULL);
			apply.setText("apply");

			apply.addSelectionListener(new SelectionListener() {
				@Override
				public void widgetSelected(SelectionEvent arg0) {
					if (sensorId == null) {
						m_admin_con.setServerConfig(conf.id, entry.getText());
					} else {
						m_admin_con.setSensorConfig(sensorId, conf.id,
								entry.getText());
					}
				}

				@Override
				public void widgetDefaultSelected(SelectionEvent arg0) {
				}
			});
		} else {
			// checkbox:			
				
			final Button entry = new Button(c, SWT.CHECK);
			entryControl = entry;
			entry.setSelection("true".equals(conf.value));
			GridData gridData = new GridData(GridData.CENTER);
			gridData.horizontalAlignment = GridData.CENTER;
			entry.setLayoutData(gridData);
			
			Button apply = new Button(c, SWT.NULL);
			apply.setText("apply");
			apply.addSelectionListener(new SelectionListener() {
				@Override
				public void widgetSelected(SelectionEvent arg0) {
					if (sensorId == null) {
						m_admin_con.setServerConfig(conf.id,
								"" + entry.getSelection());
					} else {
						m_admin_con.setSensorConfig(sensorId, conf.id, ""
								+ entry.getSelection());
					}
				}

				@Override
				public void widgetDefaultSelected(SelectionEvent arg0) {
				}
			});
			
		}
		
		m_all_configs.put(conf.id, entryControl);

		//c.setLayout(rl);
		c.pack();
		target.pack(true);
		log.info("added conf item");

	}

}
