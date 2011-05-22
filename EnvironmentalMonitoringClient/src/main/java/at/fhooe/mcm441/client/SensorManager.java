package at.fhooe.mcm441.client;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Vector;

import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.ScrolledComposite;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.layout.RowLayout;
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

public class SensorManager extends SensorViewer implements
		IAdminClientSideListener {
	private static final Logger log = org.slf4j.LoggerFactory
			.getLogger(SensorManager.class.getName());

	public static String HOST = "localhost";
	public static int PORT = 4445;

	public AdminConnection m_admin_con;
	private static final boolean HARDCORETEST = false; // if this is true, not
														// one but MANY clients
														// are started
	private static final boolean LOGGING = !HARDCORETEST;

	private ArrayList<String> m_all_sensors;
	public Vector<Configuration> configItems;

	// key = sensorID
	private static Map<String, Configuration> m_configItems;

	public static void main(String[] args) throws Exception {
		new SensorManager(false, 0);
	}

	// Gui elements
	private Group m_group2;
	private Group m_group3;
	private Composite composite3;

	public SensorManager(boolean autoRegister, int disconnectAfterSeconds)
			throws Exception {
		super(autoRegister, disconnectAfterSeconds);
	}

	@Override
	public void newConnection() throws Exception {
		m_admin_con = new AdminConnection(HOST, PORT, this, this);
		m_con = m_admin_con;
		m_configItems = new HashMap<String, Configuration>();
		m_all_sensors = new ArrayList<String>();

	}

	@Override
	public void setupGui() {
		super.setupGui();

		GridData gridData;
		m_group2 = new Group(composite1, SWT.NULL);
		m_group2.setText("connected clients");
		gridData = new GridData(150, 200);
		m_group2.setLayoutData(gridData);
		m_group2.setLayout(new GridLayout(1, false));

		m_group3 = new Group(composite2, SWT.NULL);
		m_group3.setText("settings");
		m_group3.setLayout(new FillLayout());

		ScrolledComposite container = new ScrolledComposite(m_group3,
				SWT.V_SCROLL);
		composite3 = new Composite(container, SWT.NULL);

		RowLayout rl = new RowLayout(SWT.VERTICAL);

		rl.pack = true;
		rl.fill = true;

		composite3.setLayout(rl);
		container.setContent(composite3);

		container.setExpandHorizontal(false);
		container.setExpandVertical(true);
		container.setMinHeight(100);
		container.setMinWidth(400);

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
				Text clients = new Text(m_group2, SWT.READ_ONLY | SWT.BORDER);
				clients.setText(client.m_address);
				clients.setData("client", client.m_id);

				m_group2.pack();
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
				System.out.println("blub" + checkboxchilds);
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

		Composite c = new Composite(target, SWT.NULL);
		RowLayout rl = new RowLayout();
		rl.pack = true;

		if (conf.type == SettingType.text || conf.type == SettingType.number) {
			
			Label txt = new Label(c, SWT.NULL);
			txt.setText(conf.displayName);
			
			final Text entry = new Text(c, SWT.SINGLE);
			entry.setText(conf.value);

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

			if(!m_all_sensors.contains(sensorId))
			{
				
			Label txt = new Label(c, SWT.NULL);
			txt.setText(conf.displayName);
				
			m_all_sensors.add(sensorId);
				
			final Button entry = new Button(c, SWT.CHECK);
			entry.setSelection("true".equals(conf.value));

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
		}

		c.setLayout(rl);
		c.pack();
		target.pack(true);
		log.info("added conf item");

	}

}
