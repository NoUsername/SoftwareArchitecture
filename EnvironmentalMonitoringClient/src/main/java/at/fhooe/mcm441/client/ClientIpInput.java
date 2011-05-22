package at.fhooe.mcm441.client;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.FormAttachment;
import org.eclipse.swt.layout.FormData;
import org.eclipse.swt.layout.FormLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;


public class ClientIpInput {
    Display display;
    Shell shell;

    public ClientIpInput(final SensorViewer client) {
    display = new Display();
    shell = new Shell(display);
    shell.setSize(200, 150);
    
    shell.setText("set Client IP");
    
    FormLayout formLayout = new FormLayout ();
	formLayout.marginWidth = 10;
	formLayout.marginHeight = 10;
	formLayout.spacing = 10;
	shell.setLayout (formLayout);

    final Label l1 = new Label(shell, SWT.NONE);
    l1.setText("Host");
    FormData fd = new FormData();
    l1.setLayoutData(fd);

    final Label l2 = new Label(shell, SWT.NONE);
    l2.setText("Port");
    fd = new FormData();
    fd.top = new FormAttachment(l1, 5);
    l2.setLayoutData(fd);

    final Text t1 = new Text(shell, SWT.BORDER | SWT.SINGLE);
    fd = new FormData();
    fd.top = new FormAttachment(l1, 0, SWT.TOP);
    fd.left = new FormAttachment(l1, 10);
    t1.setLayoutData(fd);
    t1.setText("localhost");

    final Text t2 = new Text(shell, SWT.BORDER | SWT.SINGLE);
    fd = new FormData();
    fd.top = new FormAttachment(l2, 0, SWT.TOP);
    fd.left = new FormAttachment(l2, 30);
    t2.setLayoutData(fd);
    t2.setText("4444");
    
    Button cancel = new Button (shell, SWT.PUSH);
	cancel.setText ("Cancel");
	FormData data = new FormData ();
	data.width = 60;
	data.right = new FormAttachment (100, 0);
	data.bottom = new FormAttachment (100, 0);
	cancel.setLayoutData (data);
	cancel.addSelectionListener (new SelectionAdapter () {
		public void widgetSelected (SelectionEvent e) {
			System.out.println("User cancelled dialog");
			shell.close ();
		}
	});
	
	Button ok = new Button (shell, SWT.PUSH);
	ok.setText ("OK");
	data = new FormData ();
	data.width = 60;
	data.right = new FormAttachment (cancel, 0, SWT.DEFAULT);
	data.bottom = new FormAttachment (100, 0);
	ok.setLayoutData (data);
	ok.addSelectionListener (new SelectionAdapter () {
		public void widgetSelected (SelectionEvent e) {
			client.setConnectionInfo(t1.getText(), Integer.parseInt(t2.getText()));
			display.syncExec(new Runnable() {
				public void run() {
					try {
						shell.setVisible(false);
						shell.close ();
						client.startSensorViewer(false, 0);
					} catch (Exception e1) {
						e1.printStackTrace();
					}
				}});
		}
	});

    shell.open();
    while (!shell.isDisposed()) {
      if (!display.readAndDispatch())
        display.sleep();
    }
    display.dispose();
  }

}