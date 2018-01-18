import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;
import java.util.Properties;

import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JTextField;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Settings extends JFrame implements ActionListener{
	/**
	 * 
	 */
	private static final long serialVersionUID = 6552804661385245948L;
	private JTextField txtServer, txtLocal, txtSynchronizeRate;
	private JButton btnLocal, btnCancel, btnApply;
	private JLabel lblServer, lblMinutes, lblIntervall, lblLocal;
	private Properties properties;
	private final JFileChooser fc = new JFileChooser();
	private Main main;
	private JLabel lblAlarmBefore;
	private JTextField txtAlarmBefore;
	private JLabel lblMinutesAlarm;

	final Logger logger = LoggerFactory.getLogger(Settings.class);
	
	public Settings(Main main, Properties properties) {
		this.main = main;
		this.properties = properties;
		
		fc.setFileSelectionMode(JFileChooser.FILES_ONLY);
		fc.setMultiSelectionEnabled(false);
		
		setTitle("Setting - BookingWatch");
		
		GridBagLayout gridBagLayout = new GridBagLayout();
		gridBagLayout.columnWidths = new int[] {0, 0, 0, 0, 3};
		gridBagLayout.rowHeights = new int[] {0, 0, 0, 0, 0, 0, 7};
		gridBagLayout.columnWeights = new double[]{0.0, 1.0, 0.0, Double.MIN_VALUE};
		gridBagLayout.rowWeights = new double[]{0.0, 0.0, 0.0, 0.0, 0.0, Double.MIN_VALUE};
		getContentPane().setLayout(gridBagLayout);
		
		
		lblServer = new JLabel("Address:");
		GridBagConstraints gbc_lblServer = new GridBagConstraints();
		gbc_lblServer.anchor = GridBagConstraints.EAST;
		gbc_lblServer.insets = new Insets(5, 5, 5, 5);
		gbc_lblServer.gridx = 0;
		gbc_lblServer.gridy = 0;
		getContentPane().add(lblServer, gbc_lblServer);
		
		txtServer = new JTextField();
		GridBagConstraints gbc_txtServer = new GridBagConstraints();
		gbc_txtServer.insets = new Insets(5, 5, 5, 5);
		gbc_txtServer.fill = GridBagConstraints.HORIZONTAL;
		gbc_txtServer.gridx = 1;
		gbc_txtServer.gridy = 0;
		getContentPane().add(txtServer, gbc_txtServer);
		txtServer.setColumns(10);
		
		btnLocal = new JButton("Select File");
		GridBagConstraints gbc_btnLocal = new GridBagConstraints();
		gbc_btnLocal.insets = new Insets(0, 0, 5, 5);
		gbc_btnLocal.gridx = 2;
		gbc_btnLocal.gridy = 1;
		getContentPane().add(btnLocal, gbc_btnLocal);
		
		lblMinutes = new JLabel("minutes.");
		GridBagConstraints gbc_lblMinutes = new GridBagConstraints();
		gbc_lblMinutes.insets = new Insets(0, 0, 5, 5);
		gbc_lblMinutes.anchor = GridBagConstraints.LINE_START;
		gbc_lblMinutes.gridx = 2;
		gbc_lblMinutes.gridy = 2;
		getContentPane().add(lblMinutes, gbc_lblMinutes);
		
		lblLocal = new JLabel("Local Calendar File:");
		GridBagConstraints gbc_lblLocal = new GridBagConstraints();
		gbc_lblLocal.anchor = GridBagConstraints.LINE_END;
		gbc_lblLocal.insets = new Insets(5, 5, 5, 5);
		gbc_lblLocal.gridx = 0;
		gbc_lblLocal.gridy = 1;
		getContentPane().add(lblLocal, gbc_lblLocal);
		
		lblIntervall = new JLabel("Synchronize every");
		GridBagConstraints gbc_lblIntervall = new GridBagConstraints();
		gbc_lblIntervall.insets = new Insets(5, 5, 5, 5);
		gbc_lblIntervall.anchor = GridBagConstraints.EAST;
		gbc_lblIntervall.gridx = 0;
		gbc_lblIntervall.gridy = 2;
		getContentPane().add(lblIntervall, gbc_lblIntervall);
		
		txtLocal = new JTextField();
		GridBagConstraints gbc_txtLocal = new GridBagConstraints();
		gbc_txtLocal.insets = new Insets(5, 5, 5, 5);
		gbc_txtLocal.fill = GridBagConstraints.HORIZONTAL;
		gbc_txtLocal.gridx = 1;
		gbc_txtLocal.gridy = 1;
		getContentPane().add(txtLocal, gbc_txtLocal);
		txtLocal.setColumns(10);
		
		txtSynchronizeRate = new JTextField();
		GridBagConstraints gbc_txtSynchronizeRate = new GridBagConstraints();
		gbc_txtSynchronizeRate.insets = new Insets(5, 5, 5, 5);
		gbc_txtSynchronizeRate.fill = GridBagConstraints.HORIZONTAL;
		gbc_txtSynchronizeRate.gridx = 1;
		gbc_txtSynchronizeRate.gridy = 2;
		getContentPane().add(txtSynchronizeRate, gbc_txtSynchronizeRate);
		txtSynchronizeRate.setColumns(10);
		
		lblAlarmBefore = new JLabel("Alarm before");
		GridBagConstraints gbc_lblAlarmBefore = new GridBagConstraints();
		gbc_lblAlarmBefore.anchor = GridBagConstraints.EAST;
		gbc_lblAlarmBefore.insets = new Insets(0, 0, 5, 5);
		gbc_lblAlarmBefore.gridx = 0;
		gbc_lblAlarmBefore.gridy = 3;
		getContentPane().add(lblAlarmBefore, gbc_lblAlarmBefore);
		
		txtAlarmBefore = new JTextField();
		GridBagConstraints gbc_txtAlarmBefore = new GridBagConstraints();
		gbc_txtAlarmBefore.insets = new Insets(0, 0, 5, 5);
		gbc_txtAlarmBefore.fill = GridBagConstraints.HORIZONTAL;
		gbc_txtAlarmBefore.gridx = 1;
		gbc_txtAlarmBefore.gridy = 3;
		getContentPane().add(txtAlarmBefore, gbc_txtAlarmBefore);
		txtAlarmBefore.setColumns(10);
		
		lblMinutesAlarm = new JLabel("minutes.");
		GridBagConstraints gbc_lblMinutesAlarm = new GridBagConstraints();
		gbc_lblMinutesAlarm.anchor = GridBagConstraints.LINE_START;
		gbc_lblMinutesAlarm.insets = new Insets(0, 0, 5, 5);
		gbc_lblMinutesAlarm.gridx = 2;
		gbc_lblMinutesAlarm.gridy = 3;
		getContentPane().add(lblMinutesAlarm, gbc_lblMinutesAlarm);
		
		btnApply = new JButton("Apply");
		GridBagConstraints gbc_btnApply = new GridBagConstraints();
		gbc_btnApply.anchor = GridBagConstraints.LINE_END;
		gbc_btnApply.insets = new Insets(0, 0, 5, 5);
		gbc_btnApply.gridx = 1;
		gbc_btnApply.gridy = 4;
		getContentPane().add(btnApply, gbc_btnApply);
		
		btnCancel = new JButton("Cancel");
		GridBagConstraints gbc_btnCancel = new GridBagConstraints();
		gbc_btnCancel.insets = new Insets(0, 0, 5, 5);
		gbc_btnCancel.gridx = 2;
		gbc_btnCancel.gridy = 4;
		getContentPane().add(btnCancel, gbc_btnCancel);
		
		btnLocal.addActionListener(this);
		btnApply.addActionListener(this);
		btnCancel.addActionListener(this);
		
		setFields();
		
		setSize(500, 266);
	}
	
	@Override
	public void actionPerformed(ActionEvent e) {
		if(e.getSource() == btnCancel) {
			setFields();
			this.setVisible(false);
		}else if(e.getSource() == btnApply) {
			properties.setProperty(Main.LOCAL_CALENDAR, txtLocal.getText());
			properties.setProperty(Main.SERVER_ADDRESS, txtServer.getText());
			
			String refreshRate = txtSynchronizeRate.getText();
			try {
				properties.setProperty(Main.REFRESH_RATE, String.valueOf(Integer.valueOf(refreshRate)));
			} catch(NumberFormatException exception) {
				txtSynchronizeRate.setToolTipText("Could not be saved. Invalid value.");
			}
			
			String alarmBefore = txtAlarmBefore.getText();
			try {
				properties.setProperty(Main.ALARM_BEFORE, String.valueOf(Integer.valueOf(alarmBefore)));
			} catch(NumberFormatException exception) {
				txtSynchronizeRate.setToolTipText("Could not be saved. Invalid value.");
			}
			
			try {
				main.saveSettings(properties);
			} catch (IOException e1) {
				logger.error(Main.exceptionStacktraceToString(e1));
			}
			
			this.setVisible(false);
		}else if(e.getSource() == btnLocal) {
			int returnValue = fc.showOpenDialog(this);
			if(returnValue == JFileChooser.APPROVE_OPTION) {
				txtLocal.setText(fc.getSelectedFile().getAbsolutePath());
			}
		}
	}

	private void setFields() {
		txtLocal.setText(properties.getProperty(Main.LOCAL_CALENDAR));
		txtServer.setText(properties.getProperty(Main.SERVER_ADDRESS));
		txtSynchronizeRate.setText(properties.getProperty(Main.REFRESH_RATE));
		txtAlarmBefore.setText(properties.getProperty(Main.ALARM_BEFORE));
	}
	
}
