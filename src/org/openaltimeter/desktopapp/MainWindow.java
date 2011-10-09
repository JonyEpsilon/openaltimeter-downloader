/*
    openaltimeter -- an open-source altimeter for RC aircraft
    Copyright (C) 2010-2011  Jony Hudson, Jan Steidl, mycarda
    http://openaltimeter.org

    This program is free software: you can redistribute it and/or modify
    it under the terms of the GNU General Public License as published by
    the Free Software Foundation, either version 3 of the License, or
    (at your option) any later version.

    This program is distributed in the hope that it will be useful,
    but WITHOUT ANY WARRANTY; without even the implied warranty of
    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
    GNU General Public License for more details.

    You should have received a copy of the GNU General Public License
    along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.openaltimeter.desktopapp;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.File;
import java.util.prefs.Preferences;

import javax.swing.ButtonGroup;
import javax.swing.JCheckBoxMenuItem;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JProgressBar;
import javax.swing.JRadioButtonMenuItem;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JTextPane;
import javax.swing.ScrollPaneConstants;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;
import javax.swing.filechooser.FileFilter;
import javax.swing.filechooser.FileNameExtensionFilter;
import javax.swing.text.BadLocationException;
import javax.swing.text.Style;
import javax.swing.text.StyleConstants;
import javax.swing.text.StyleContext;
import javax.swing.text.StyledDocument;

import org.openaltimeter.desktopapp.Controller.ConnectionState;
import org.openaltimeter.desktopapp.Controller.OS;

public class MainWindow {

	private JFrame frmOpenaltimeter;
	public Controller controller;
	JMenu mnSerialPort;
	ButtonGroup serialMenuGroup;
	JMenuItem mntmConnect;
	JMenuItem mntmDisconnect;
	JTextPane logTextPane;
	JScrollPane scrollPane;
	private StyledDocument logDocument;
	private JMenuItem mntmDownloadData;
	private JMenuItem mntmEraseLogger;
	private JCheckBoxMenuItem chckbxmntmTemperature;
	private JCheckBoxMenuItem chckbxmntmVoltage;
	private JCheckBoxMenuItem chckbxmntmServo;
	private JRadioButtonMenuItem rdbtnmntmFeet;
	private JRadioButtonMenuItem rdbtnmntmMetres;
		
	private final JProgressBar progressBar = new JProgressBar();
	private JMenuItem mntmSaveData;
	private JMenuItem mntmSaveSelectionData;
	
	AltimeterChart altimeterChart;
	
	public enum DataState {NO_DATA, HAVE_DATA};
	
	private Preferences prefs;
	private static final String PREF_SHOW_TEMPERATURE = "PREF_SHOW_TEMPERATURE";
	private static final String PREF_SHOW_VOLTAGE = "PREF_SHOW_VOLTAGE";
	private static final String PREF_SHOW_SERVO = "PREF_SHOW_SERVO";
	private static final String PREF_UNIT_FEET = "PREF_UNIT_FEET";
	private static final String PREF_WINDOW_X = "PREF_WINDOW_X";
	private static final String PREF_WINDOW_Y = "PREF_WINDOW_Y";
	private static final String PREF_WINDOW_WIDTH = "PREF_WINDOW_WIDTH";
	private static final String PREF_WINDOW_HEIGHT = "PREF_WINDOW_HEIGHT";
	private static final String PREF_FILE_PATH = "PREF_FILE_PATH";
	
	private File filePath; 
	private JMenuItem mntmSettings;
	private JMenuItem mntmFlashFirmware;
	private JMenu mnAdvanced;
	private JMenuItem mntmUploadSelectionerases;
	private JMenuItem mntmClearAnnotations;
	private JMenu mnAnalysis;
	private JMenuItem mntmMarkDlgLaunches;
	private JMenuItem mntmClearDlgLaunch;
	
	public void show() {
		frmOpenaltimeter.setVisible(true);
	}

	/**
	 * @wbp.parser.entryPoint
	 */
	public void initialise() {
		prefs = Preferences.userNodeForPackage(this.getClass());

		try {
			UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
		} catch (Exception e) {
			System.err.println("Unable to switch to native look and feel.");
		}

		try {
			filePath = new File(prefs.get(PREF_FILE_PATH, "."));
		} catch (Exception e) {
			filePath = null;
		}
		
		frmOpenaltimeter = new JFrame();
		frmOpenaltimeter.setIconImage(Toolkit.getDefaultToolkit().getImage(MainWindow.class.getResource("/logo_short_64.png")));
		frmOpenaltimeter.addWindowListener(new WindowAdapter() {
			public void windowClosing(WindowEvent arg0) {
				controller.exit();
			}
		});
		frmOpenaltimeter.setTitle("openaltimeter");
		frmOpenaltimeter.setBounds(prefs.getInt(PREF_WINDOW_X, 70),
				                   prefs.getInt(PREF_WINDOW_Y, 70),
				                   prefs.getInt(PREF_WINDOW_WIDTH, 1100),
				                   prefs.getInt(PREF_WINDOW_HEIGHT, 700));
		frmOpenaltimeter.setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
		
		

		JMenuBar menuBar = new JMenuBar();
		frmOpenaltimeter.setJMenuBar(menuBar);

		JMenu mnFile = new JMenu("File");
		menuBar.add(mnFile);

		mntmSaveData = new JMenuItem("Save data ...");
		mntmSaveData.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				controller.saveRaw();
			}
		});
		
		JMenuItem mntmLoadRawData = new JMenuItem("Load data ...");
		mntmLoadRawData.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				controller.loadRawData();
			}
		});
		mnFile.add(mntmLoadRawData);
		
		mnFile.addSeparator();
		mntmSaveData.setEnabled(false);
		mnFile.add(mntmSaveData);
		
		mntmSaveSelectionData = new JMenuItem("Save selection data...");
		mntmSaveSelectionData.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				controller.saveRawSelection(altimeterChart.getVisibleDomainLowerBound(), 
						altimeterChart.getVisibleDomainUpperBound());
			}
		});
		mntmSaveSelectionData.setEnabled(false);
		mnFile.add(mntmSaveSelectionData);
		
		JMenuItem mntmExit = new JMenuItem("Exit");
		mntmExit.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				controller.exit();
			}
		});
		
		mnFile.addSeparator();
		mnFile.add(mntmExit);
		
		JMenu mnView = new JMenu("View");
		menuBar.add(mnView);
		
		final JCheckBoxMenuItem chckbxmntmAltitude = new JCheckBoxMenuItem("Altitude");
		chckbxmntmAltitude.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				controller.altitudePlotSelectedChange(chckbxmntmAltitude.isSelected());
			}
		});
		chckbxmntmAltitude.setSelected(true);
		mnView.add(chckbxmntmAltitude);
		
		chckbxmntmVoltage = new JCheckBoxMenuItem("Voltage");
		chckbxmntmVoltage.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				controller.voltagePlotSelectedChange(chckbxmntmVoltage.isSelected());
			}
		});
		chckbxmntmVoltage.setSelected(prefs.getBoolean(PREF_SHOW_VOLTAGE, false));
		mnView.add(chckbxmntmVoltage);
		
		chckbxmntmServo = new JCheckBoxMenuItem("Servo");
		chckbxmntmServo.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				controller.servoPlotSelectedChange(chckbxmntmServo.isSelected());
			}
		});
		chckbxmntmServo.setSelected(prefs.getBoolean(PREF_SHOW_SERVO, false));
		mnView.add(chckbxmntmServo);

		chckbxmntmTemperature = new JCheckBoxMenuItem("Temperature");
		chckbxmntmTemperature.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				controller.temperaturePlotSelectedChange(chckbxmntmTemperature.isSelected());
			}
		});
		chckbxmntmTemperature.setSelected(prefs.getBoolean(PREF_SHOW_TEMPERATURE, false));
		mnView.add(chckbxmntmTemperature);
		
		mnView.addSeparator();
		
		ButtonGroup heightUnitsGroup = new ButtonGroup();
		
		controller.setUnitFeet(prefs.getBoolean(PREF_UNIT_FEET, true));
		rdbtnmntmFeet = new JRadioButtonMenuItem("Feet");
		rdbtnmntmFeet.setEnabled(true);
		rdbtnmntmFeet.setSelected(controller.isUnitFeet());
		rdbtnmntmFeet.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				controller.unitSelectedChange(rdbtnmntmFeet.isSelected());
			}
		});
		heightUnitsGroup.add(rdbtnmntmFeet);
		mnView.add(rdbtnmntmFeet);
		
		rdbtnmntmMetres = new JRadioButtonMenuItem("Metres");
		rdbtnmntmMetres.setEnabled(true);
		rdbtnmntmMetres.setSelected(!controller.isUnitFeet());
		rdbtnmntmMetres.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				controller.unitSelectedChange(rdbtnmntmFeet.isSelected());
			}
		});
		heightUnitsGroup.add(rdbtnmntmMetres);
		mnView.add(rdbtnmntmMetres);
		
		mnAnalysis = new JMenu("Analysis");
		menuBar.add(mnAnalysis);
		
		mntmMarkDlgLaunches = new JMenuItem("Mark DLG launches ...");
		mnAnalysis.add(mntmMarkDlgLaunches);
		
		mntmClearDlgLaunch = new JMenuItem("Clear DLG launch annotations");
		mnAnalysis.add(mntmClearDlgLaunch);
		
		mnAnalysis.addSeparator();
		
		mntmClearAnnotations = new JMenuItem("Clear height and vario annotations");
		mnAnalysis.add(mntmClearAnnotations);
		mntmClearAnnotations.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				altimeterChart.clearAnnotations();
			}
		});

		JMenu mnConnection = new JMenu("Connection");
		menuBar.add(mnConnection);

		mnSerialPort = new JMenu("Serial port");
		mnConnection.add(mnSerialPort);
		serialMenuGroup = new ButtonGroup();

		mntmConnect = new JMenuItem("Connect");
		mntmConnect.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				controller.connect();
			}
		});
		mnConnection.add(mntmConnect);

		mntmDisconnect = new JMenuItem("Disconnect");
		mntmDisconnect.setEnabled(false);
		mntmDisconnect.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				controller.disconnect();
			}
		});
		mnConnection.add(mntmDisconnect);

		JMenu mnLogger = new JMenu("Altimeter");
		menuBar.add(mnLogger);

		mntmDownloadData = new JMenuItem("Download data");
		mntmDownloadData.setEnabled(false);
		mntmDownloadData.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				controller.downloadData();
			}
		});
		mnLogger.add(mntmDownloadData);

		mntmEraseLogger = new JMenuItem("Erase altimeter memory ...");
		mntmEraseLogger.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				controller.erase();
			}
		});
		mntmEraseLogger.setEnabled(false);
		mnLogger.add(mntmEraseLogger);
		
		mntmSettings = new JMenuItem("Settings ...");
		mntmSettings.setEnabled(false);
		mntmSettings.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				controller.runSettingsInterface();
			}
		});
		mnLogger.add(mntmSettings);
		
		mnLogger.addSeparator();
		
		mntmFlashFirmware = new JMenuItem("Flash firmware ...");
		mntmFlashFirmware.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				controller.flashFirmware();
			}
		});
		mnLogger.add(mntmFlashFirmware);
		
		mnAdvanced = new JMenu("Advanced");
		mnAdvanced.setVisible(false);
		menuBar.add(mnAdvanced);
		
		mntmUploadSelectionerases = new JMenuItem("Upload selection (erases OA)");
		mntmUploadSelectionerases.setEnabled(false);
		mntmUploadSelectionerases.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				controller.uploadSelection(altimeterChart.getVisibleDomainLowerBound(),
						altimeterChart.getVisibleDomainLowerBound());
			}
		});
		mnAdvanced.add(mntmUploadSelectionerases);
		// os specific function
		if (controller.os != OS.OTHER) 
			mntmFlashFirmware.setEnabled(true);
		else  mntmFlashFirmware.setEnabled(false);

		JSplitPane splitPane = new JSplitPane();
		splitPane.setContinuousLayout(true);
		splitPane.setResizeWeight(0.6);
		splitPane.setOrientation(JSplitPane.VERTICAL_SPLIT);
		splitPane.setDividerLocation(-1);
		frmOpenaltimeter.getContentPane().add(splitPane, BorderLayout.CENTER);

		altimeterChart = new AltimeterChart();
				
		splitPane.setTopComponent(altimeterChart.getChartPanel());

		altimeterChart.setVoltagePlotVisible(prefs.getBoolean(PREF_SHOW_VOLTAGE, false));
		altimeterChart.setTemperaturePlotVisible(prefs.getBoolean(PREF_SHOW_TEMPERATURE, false));
		altimeterChart.setServoPlotVisible(prefs.getBoolean(PREF_SHOW_SERVO, false));

		// create the text pane, a document for it to view, and some styles
		logTextPane = new JTextPane();
		logTextPane.setPreferredSize(new Dimension(6, 100));
		logDocument = logTextPane.getStyledDocument(); 
	    Style defaultStyle = StyleContext.getDefaultStyleContext().getStyle(StyleContext.DEFAULT_STYLE);
		Style messageStyle = logDocument.addStyle("message", defaultStyle);
		StyleConstants.setFontFamily(messageStyle, "Monospaced");
		StyleConstants.setFontSize(messageStyle, 12);
		StyleConstants.setForeground(messageStyle, Color.BLACK);
		Style altimeterStyle = logDocument.addStyle("altimeter", messageStyle);
		StyleConstants.setForeground(altimeterStyle, Color.BLUE);
		Style errorStyle = logDocument.addStyle("error", messageStyle);
		StyleConstants.setForeground(errorStyle, Color.RED);
		Style helpStyle = logDocument.addStyle("help", messageStyle);
		StyleConstants.setForeground(helpStyle, Color.DARK_GRAY);
		
		scrollPane = new JScrollPane(logTextPane);
		scrollPane.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS);
		scrollPane.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
		splitPane.setRightComponent(scrollPane);
	
		frmOpenaltimeter.getContentPane().add(progressBar, BorderLayout.PAGE_END);
	}

	public void addCOMMenuItem(String portName) {
		JRadioButtonMenuItem radioButtonMenuItem = new JRadioButtonMenuItem(
				portName);
		serialMenuGroup.add(radioButtonMenuItem);
		mnSerialPort.add(radioButtonMenuItem);
	}

	public void selectFirstCOMItem() {
		JMenuItem item = mnSerialPort.getItem(0);
		if (item != null)
			item.setSelected(true);
	}

	public String getSelectedCOMPort() {
		int itemCount = mnSerialPort.getItemCount();
		String selectedPort = "";
		for (int i = 0; i < itemCount; i++)
			if (mnSerialPort.getItem(i).isSelected())
				selectedPort = mnSerialPort.getItem(i).getText();
		return selectedPort;
	}

	public void setConnectedState(final ConnectionState state) {
		SwingUtilities.invokeLater( new Runnable() {
			public void run() {
				switch (state) {
				case CONNECTED: 
					mntmConnect.setEnabled(false);
					mntmDisconnect.setEnabled(true);
					mntmDownloadData.setEnabled(true);
					mntmEraseLogger.setEnabled(true);
					mntmSettings.setEnabled(true);
					mnSerialPort.setEnabled(false);
					mntmUploadSelectionerases.setEnabled(true);
					// os specific function
					if (controller.os != OS.OTHER) 
						mntmFlashFirmware.setEnabled(true);
					break;
				case DISCONNECTED: 
					mntmConnect.setEnabled(true);
					mntmDisconnect.setEnabled(false);
					mntmDownloadData.setEnabled(false);
					mntmEraseLogger.setEnabled(false);
					mntmSettings.setEnabled(false);
					mnSerialPort.setEnabled(true);
					mntmUploadSelectionerases.setEnabled(false);
					// os specific function
					if (controller.os != OS.OTHER) 
						mntmFlashFirmware.setEnabled(true);
					break;
				case BUSY: 
					mntmConnect.setEnabled(false);
					mntmDisconnect.setEnabled(false);
					mntmDownloadData.setEnabled(false);
					mntmEraseLogger.setEnabled(false);
					mntmSettings.setEnabled(false);
					mnSerialPort.setEnabled(false);
					mntmUploadSelectionerases.setEnabled(false);
					// os specific function
					if (controller.os != OS.OTHER) 
						mntmFlashFirmware.setEnabled(false);
					break;
				}
			}
		});
	}
	
	public void setDataState(final DataState state) {
		SwingUtilities.invokeLater( new Runnable() {
			public void run() {
				switch (state) {
				case NO_DATA: 
					mntmSaveData.setEnabled(false);
					mntmSaveSelectionData.setEnabled(false);
					break;
				case HAVE_DATA: 
					mntmSaveData.setEnabled(true);
					mntmSaveSelectionData.setEnabled(true);
					break;
				}
			}
		});
	}
	
	public void close()
	{
		setPreferences();
		frmOpenaltimeter.dispose();
	}

	private void setPreferences() {
		prefs.putBoolean(PREF_SHOW_TEMPERATURE,chckbxmntmTemperature.isSelected());
		prefs.putBoolean(PREF_SHOW_VOLTAGE,chckbxmntmVoltage.isSelected());
		prefs.putBoolean(PREF_SHOW_SERVO, chckbxmntmServo.isSelected());
		prefs.putBoolean(PREF_UNIT_FEET, controller.isUnitFeet());
		prefs.putInt(PREF_WINDOW_X, frmOpenaltimeter.getX());
		prefs.putInt(PREF_WINDOW_Y, frmOpenaltimeter.getY());
		prefs.putInt(PREF_WINDOW_WIDTH, frmOpenaltimeter.getWidth());
		prefs.putInt(PREF_WINDOW_HEIGHT, frmOpenaltimeter.getHeight());
		prefs.put(PREF_FILE_PATH, filePath.getAbsolutePath());
	}

	public void log(final String message, final String style) {
		SwingUtilities.invokeLater(new Runnable() {
			public void run() {
				try {
					logDocument.insertString(logDocument.getLength(), message + "\n", logDocument.getStyle(style));
				} catch (BadLocationException e) {
					System.err.println("Error writing to log.");
				}
			}
		});
	}
	
	public void updateProgress(final int progress)
	{
		SwingUtilities.invokeLater(new Runnable() {
			public void run() {
				progressBar.setValue(progress);
			}
		});
	}
	
	public File showRawSaveDialog()
	{
		JFileChooser fc = new JFileChooser();
		if (filePath != null)
			fc.setCurrentDirectory(filePath);
		FileFilter filter= new FileNameExtensionFilter("Text file", "txt");
		fc.addChoosableFileFilter(filter);
		if (fc.showSaveDialog(this.frmOpenaltimeter) == JFileChooser.APPROVE_OPTION)
		{
			File selectedFile = fc.getSelectedFile(); 
			// if the text file filter is selected we ensure there's a .txt extension
			if (fc.getFileFilter() == filter) {
				if (!selectedFile.getAbsolutePath().endsWith(".txt"))
					selectedFile = new File(selectedFile.getAbsolutePath() + ".txt");
			}
			filePath = selectedFile.getParentFile();
				
			return selectedFile;
		}
		else return null;
	}

	public File showOpenDialog(FileFilter filter)
	{
		JFileChooser fc = new JFileChooser();
		if (filePath != null)
			fc.setCurrentDirectory(filePath);
		fc.addChoosableFileFilter(filter);
		if (fc.showOpenDialog(this.frmOpenaltimeter) == JFileChooser.APPROVE_OPTION)
		{
			File selectedFile = fc.getSelectedFile(); 
			filePath = selectedFile.getParentFile();
				
			return selectedFile;
		}
		else return null;
	}

	public boolean showConfirmDialog(String message, String title)
	{
		int response = JOptionPane.showConfirmDialog(this.frmOpenaltimeter, message, title, JOptionPane.OK_CANCEL_OPTION);
		return (response == JOptionPane.OK_OPTION);
	}

	public void setTitle(String string) {
		frmOpenaltimeter.setTitle(string);
	}

}
