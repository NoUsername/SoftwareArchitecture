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
import org.eclipse.swt.widgets.MessageBox;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;

/**
 * class for setting the host and port for the client to connect to the server
 * sensorviewer port: 4444
 * sensormanager port: 4445
 * 
 * @author Melanie Schmidt
 *
 */

public class ClientIpInput {
    private Display display;
    private Shell shell;
    private int style = SWT.ICON_WARNING | SWT.OK | SWT.CANCEL;

    public ClientIpInput(final SensorViewer client) {
    display = new Display();
    shell = new Shell(display);
    shell.setSize(260, 150);
    
    shell.setText("set Client Connection");
    
    FormLayout formLayout = new FormLayout ();
	formLayout.marginWidth = 10;
	formLayout.marginHeight = 10;
	formLayout.spacing = 10;
	shell.setLayout (formLayout);

    final Label l1 = new Label(shell, SWT.NONE);
    l1.setText("HOST");
    FormData fd = new FormData();
    l1.setLayoutData(fd);

    final Label l2 = new Label(shell, SWT.NONE);
    l2.setText("IP");
    fd = new FormData();
    fd.top = new FormAttachment(l1, 5);
    l2.setLayoutData(fd);

    final Text t1 = new Text(shell, SWT.BORDER | SWT.SINGLE);
    fd = new FormData();
    fd.top = new FormAttachment(l1, 0, SWT.TOP);
    fd.left = new FormAttachment(l1, 10);
    t1.setLayoutData(fd);

    final Text t2 = new Text(shell, SWT.BORDER | SWT.SINGLE);
    fd = new FormData();
    fd.top = new FormAttachment(l2, 0, SWT.TOP);
    fd.left = new FormAttachment(l2, 30);
    t2.setLayoutData(fd);
    
    final Button checkbox = new Button(shell, SWT.CHECK);
    checkbox.setText("localhost");
    fd = new FormData();
    fd.top = new FormAttachment(l1, 0, SWT.TOP);
    fd.left = new FormAttachment(t1, 10);
    checkbox.setLayoutData(fd);

    checkbox.addSelectionListener(new SelectionAdapter() {
	      public void widgetSelected(SelectionEvent event) {
	    	  if(checkbox.getSelection())
	    	  {
	    		  t1.setText("127.0.0.1");
	    		  t1.update();
	    	  }
	    	  else
	    	  {
	    		  t1.setText("");
	    		  t1.update();
	    	  }	                 	          
	        }
	      });
    
    Button cancel = new Button (shell, SWT.PUSH);
	cancel.setText ("Cancel");
	FormData data = new FormData ();
	data.width = 60;
	data.right = new FormAttachment (100, 0);
	data.bottom = new FormAttachment (100, 0);
	cancel.setLayoutData (data);
	cancel.addSelectionListener (new SelectionAdapter () {
		public void widgetSelected (SelectionEvent e) {
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
			
			try {
				if((t1.getText().isEmpty()) && (t2.getText().isEmpty()))
				{				
					MessageBox messageBox = new MessageBox(shell, style);
				    messageBox.setMessage("Please insert HOST and PORT");
				    int rc = messageBox.open();
				    switch (rc) {
				    case SWT.OK:
				        //ok, new input
				        display.syncExec(
				        		  new Runnable() {
				        		    public void run(){
				        		    	display.dispose();			        		  
				        		    	new ClientIpInput(client);
				        		    }
				        		  });
				        break;
				      case SWT.CANCEL:
				    	  //cancel
				        break;
				    }
//				    display.dispose();
				}
				else
				{
					client.setConnectionInfo(t1.getText(), Integer.parseInt(t2.getText()));
					display.dispose();
					client.startSensorViewer(false, 0);
				}
				
			} catch (Exception e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}
			shell.close ();
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