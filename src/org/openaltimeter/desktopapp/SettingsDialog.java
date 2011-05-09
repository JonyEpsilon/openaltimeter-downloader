package org.openaltimeter.desktopapp;

import java.awt.BorderLayout;
import java.awt.Font;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.ButtonGroup;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JTextField;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;
import javax.swing.WindowConstants;

import org.openaltimeter.settings.Settings;
import org.openaltimeter.settings.Settings.BatteryType;
import java.awt.Toolkit;

@SuppressWarnings("serial")
public class SettingsDialog extends JDialog {
	private JTextField logIntervalMSTextField;
	private JTextField heightUnitsTextField;
	private JTextField lowVoltageThresholdTextField;
	private JTextField batteryMonitorCalibrationTextField;
	private JRadioButton rdbtnNimh;
	private JRadioButton rdbtnLipo;
	private JRadioButton rdbtnNone;
	private JCheckBox logServoCheckBox;
	private JButton btnSaveSettingsTo;
	private JButton btnClose;
	private JRadioButton rdbtn3position;
	private JRadioButton rdbtn2position;
	
	public SettingsDialog(final Controller controller) {
		setIconImage(Toolkit.getDefaultToolkit().getImage(SettingsDialog.class.getResource("/logo_short_64.png")));
		setTitle("openaltimeter settings");
		setBounds(100, 100, 725, 399);
		
		JPanel panel = new JPanel();
		getContentPane().add(panel, BorderLayout.SOUTH);
		
		btnSaveSettingsTo = new JButton("Save settings to altimeter");
		btnSaveSettingsTo.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				controller.saveSettingsToAltimeter();
			}
		});
		panel.add(btnSaveSettingsTo);
		
		btnClose = new JButton("Cancel");
		btnClose.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				dispose();
			}
		});
		panel.add(btnClose);
		
		JPanel panel_1 = new JPanel();
		getContentPane().add(panel_1, BorderLayout.CENTER);
		panel_1.setLayout(null);
		
		JLabel lblLoggingIntervalms = new JLabel("Logging interval (ms)");
		lblLoggingIntervalms.setHorizontalAlignment(SwingConstants.TRAILING);
		lblLoggingIntervalms.setBounds(24, 36, 135, 14);
		panel_1.add(lblLoggingIntervalms);
		
		logIntervalMSTextField = new JTextField();
		logIntervalMSTextField.setText("500");
		logIntervalMSTextField.setBounds(168, 32, 86, 20);
		panel_1.add(logIntervalMSTextField);
		logIntervalMSTextField.setColumns(10);
		
		JLabel lblDefaultValueOf = new JLabel("Default value of 500 gives two samples per second. (Min. 333)");
		lblDefaultValueOf.setBounds(269, 35, 440, 14);
		panel_1.add(lblDefaultValueOf);
		
		JLabel lblHeightUnits = new JLabel("Height units");
		lblHeightUnits.setHorizontalAlignment(SwingConstants.TRAILING);
		lblHeightUnits.setBounds(44, 64, 115, 14);
		panel_1.add(lblHeightUnits);
		
		heightUnitsTextField = new JTextField();
		heightUnitsTextField.setText("3.281");
		heightUnitsTextField.setColumns(10);
		heightUnitsTextField.setBounds(168, 60, 86, 20);
		panel_1.add(heightUnitsTextField);
		
		JLabel lblDefaultValueOf_1 = new JLabel("Default value of 3.281 for feet, 1.0 for metres.");
		lblDefaultValueOf_1.setBounds(269, 63, 313, 14);
		panel_1.add(lblDefaultValueOf_1);
		
		JLabel lblBatteryType = new JLabel("Battery type");
		lblBatteryType.setHorizontalAlignment(SwingConstants.TRAILING);
		lblBatteryType.setBounds(44, 92, 115, 14);
		panel_1.add(lblBatteryType);
		
		ButtonGroup batteryTypeGroup = new ButtonGroup();
		
		rdbtnNimh = new JRadioButton("NiMH");
		rdbtnNimh.setSelected(true);
		rdbtnNimh.setBounds(168, 87, 71, 23);
		batteryTypeGroup.add(rdbtnNimh);
		panel_1.add(rdbtnNimh);
		
		rdbtnLipo = new JRadioButton("LiPo");
		rdbtnLipo.setBounds(168, 112, 71, 23);
		batteryTypeGroup.add(rdbtnLipo);
		panel_1.add(rdbtnLipo);
		
		rdbtnNone = new JRadioButton("None");
		rdbtnNone.setBounds(168, 138, 71, 23);
		batteryTypeGroup.add(rdbtnNone);
		panel_1.add(rdbtnNone);
		
		JLabel lblDeterminesTheBehaviour = new JLabel("Be sure to set an appropriate threshold below.");
		lblDeterminesTheBehaviour.setFont(new Font("Tahoma", Font.BOLD, 11));
		lblDeterminesTheBehaviour.setBounds(269, 91, 265, 14);
		panel_1.add(lblDeterminesTheBehaviour);
		
		JLabel lblLowVoltageThreshold = new JLabel("Low voltage threshold (V)");
		lblLowVoltageThreshold.setHorizontalAlignment(SwingConstants.TRAILING);
		lblLowVoltageThreshold.setBounds(10, 167, 149, 14);
		panel_1.add(lblLowVoltageThreshold);
		
		lowVoltageThresholdTextField = new JTextField();
		lowVoltageThresholdTextField.setText("4.7");
		lowVoltageThresholdTextField.setColumns(10);
		lowVoltageThresholdTextField.setBounds(168, 164, 86, 20);
		panel_1.add(lowVoltageThresholdTextField);
		
		JLabel lblValueIsPer = new JLabel("Value is per pack for NiMH, and per cell for LiPo.");
		lblValueIsPer.setBounds(269, 167, 388, 14);
		panel_1.add(lblValueIsPer);
		
		JLabel lblDefaultValuesOf = new JLabel("Default values of 4.7 for NiMH and 3.5 for LiPo work well.");
		lblDefaultValuesOf.setFont(new Font("Tahoma", Font.BOLD, 11));
		lblDefaultValuesOf.setBounds(269, 185, 388, 14);
		panel_1.add(lblDefaultValuesOf);
		
		JLabel lblBatteryMonitorCalibration = new JLabel("Battery monitor calibration");
		lblBatteryMonitorCalibration.setHorizontalAlignment(SwingConstants.TRAILING);
		lblBatteryMonitorCalibration.setBounds(10, 218, 149, 14);
		panel_1.add(lblBatteryMonitorCalibration);
		
		batteryMonitorCalibrationTextField = new JTextField();
		batteryMonitorCalibrationTextField.setText("1.0");
		batteryMonitorCalibrationTextField.setColumns(10);
		batteryMonitorCalibrationTextField.setBounds(168, 214, 86, 20);
		panel_1.add(batteryMonitorCalibrationTextField);
		
		JLabel lblOnlyChangeFrom = new JLabel("Only change from 1.0 if you've got a very good meter and know what you're doing!");
		lblOnlyChangeFrom.setBounds(269, 217, 440, 14);
		panel_1.add(lblOnlyChangeFrom);
		
		JLabel lblLogSecondServo = new JLabel("Log second servo channel");
		lblLogSecondServo.setHorizontalAlignment(SwingConstants.TRAILING);
		lblLogSecondServo.setBounds(10, 246, 149, 14);
		panel_1.add(lblLogSecondServo);
		
		logServoCheckBox = new JCheckBox("");
		logServoCheckBox.setSelected(true);
		logServoCheckBox.setBounds(168, 241, 28, 23);
		panel_1.add(logServoCheckBox);
		
		JLabel lblOnlyChangeFrom_1 = new JLabel("Note: enabling uses no extra log memory.");
		lblOnlyChangeFrom_1.setBounds(269, 245, 402, 14);
		panel_1.add(lblOnlyChangeFrom_1);
		
		JLabel lblSettingsAreNot = new JLabel("Settings are not saved until you press \"Save settings to altimeter\" below.");
		lblSettingsAreNot.setFont(new Font("Tahoma", Font.BOLD, 11));
		lblSettingsAreNot.setBounds(10, 11, 460, 14);
		panel_1.add(lblSettingsAreNot);
		
		JLabel lblSwitchType = new JLabel("Switch type");
		lblSwitchType.setHorizontalAlignment(SwingConstants.TRAILING);
		lblSwitchType.setBounds(44, 271, 115, 14);
		panel_1.add(lblSwitchType);
		
		ButtonGroup switchTypeButtonGroup = new ButtonGroup();
		
		rdbtn3position = new JRadioButton("3-position");
		rdbtn3position.setSelected(true);
		rdbtn3position.setBounds(168, 266, 86, 23);
		switchTypeButtonGroup.add(rdbtn3position);
		panel_1.add(rdbtn3position);
		
		JLabel lblThreePositionGives = new JLabel("Three position gives access to height readout and lost model alarm.");
		lblThreePositionGives.setBounds(269, 270, 440, 14);
		panel_1.add(lblThreePositionGives);
		
		rdbtn2position = new JRadioButton("2-position");
		rdbtn2position.setBounds(168, 291, 86, 23);
		switchTypeButtonGroup.add(rdbtn2position);
		panel_1.add(rdbtn2position);
		
		JLabel lblTwoPositionGives = new JLabel("Two position gives access to height readout only.");
		lblTwoPositionGives.setBounds(269, 295, 388, 14);
		panel_1.add(lblTwoPositionGives);
	}
	
	public Settings getSettings() {
		Settings s = new Settings();
		s.logIntervalMS = Integer.parseInt(logIntervalMSTextField.getText());
		s.heightUnits = Float.parseFloat(heightUnitsTextField.getText());
		s.batteryType = getBatteryType();
		s.lowVoltageThreshold = Float.parseFloat(lowVoltageThresholdTextField.getText());
		s.batteryMonitorCalibration = Float.parseFloat(batteryMonitorCalibrationTextField.getText());
		s.logServo = logServoCheckBox.isSelected();
		s.threePositionSwitch = rdbtn3position.isSelected();
		return s;
	}
	
	private BatteryType getBatteryType() {
		if (rdbtnNimh.isSelected()) return BatteryType.NIMH;
		if (rdbtnLipo.isSelected()) return BatteryType.LIPO;
		else return BatteryType.NONE;
	}
	
	public void setSettings(Settings s) {
		logIntervalMSTextField.setText(Integer.toString(s.logIntervalMS));
		heightUnitsTextField.setText(Float.toString(s.heightUnits));
		switch (s.batteryType) {
		case NIMH:
			rdbtnNimh.setSelected(true);
			break;
		case LIPO:
			rdbtnLipo.setSelected(true);
			break;
		case NONE:
			rdbtnNone.setSelected(true);
			break;
		}			
		lowVoltageThresholdTextField.setText(Float.toString(s.lowVoltageThreshold));
		batteryMonitorCalibrationTextField.setText(Float.toString(s.batteryMonitorCalibration));
		logServoCheckBox.setSelected(s.logServo);
		if (s.threePositionSwitch) {
			rdbtn3position.setSelected(true);
		} else {
			rdbtn2position.setSelected(true);
		}
	}
	
	public void enableButtons(final boolean enable) {
		SwingUtilities.invokeLater( new Runnable() {
			public void run() {
				btnSaveSettingsTo.setEnabled(enable);
				btnClose.setEnabled(enable);
				if (enable) setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE); 
				else setDefaultCloseOperation(WindowConstants.DO_NOTHING_ON_CLOSE); 
			}
		});
	}	
}
