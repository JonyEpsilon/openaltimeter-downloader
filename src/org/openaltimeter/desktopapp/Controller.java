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

import java.awt.Dialog.ModalityType;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Iterator;
import java.util.List;
import java.util.Properties;
import java.util.Vector;
import java.util.prefs.Preferences;

import javax.swing.JDialog;
import javax.swing.JOptionPane;
import javax.swing.SwingUtilities;
import javax.swing.filechooser.FileNameExtensionFilter;

import org.openaltimeter.Altimeter;
import org.openaltimeter.Altimeter.DownloadTimeoutException;
import org.openaltimeter.Altimeter.NotAnOpenaltimeterException;
import org.openaltimeter.comms.SerialLink;
import org.openaltimeter.data.FlightLog;
import org.openaltimeter.data.HeightUnits;
import org.openaltimeter.data.analysis.DLGFlight;
import org.openaltimeter.data.analysis.DLGFlightAnalyser;
import org.openaltimeter.desktopapp.MainWindow.DataState;
import org.openaltimeter.settings.Settings;


public class Controller {
	
	public enum ConnectionState {CONNECTED, DISCONNECTED, BUSY}
	private ConnectionState connectionState;
	
	public enum OS { WINDOWS, MAC, LINUX, OTHER };
	public OS os;
	
	static Controller controller;
	Altimeter altimeter;
	MainWindow window;
	FlightLog flightLog;
	public String versionNumber = "";
	public String firmwareVersionNumber = "";
	private HeightUnits hu;
	public HeightUnits getHeightUnits() {
		return hu;
	}

	public void setHeightUnits(HeightUnits hu) {
		this.hu = hu;
	}

	private Preferences prefs;
	private static final String PREF_HEIGHT_UNITS = "PREF_HEIGHT_UNITS";

	public static void main(String[] args) {
		try {
			Controller c = new Controller();
			controller = c;
			c.run();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	public static Controller getController() {
		return controller;
	}

	// this is the application's main method
	private void run() {
		// determine what os we're running, as some features are currently os specific
		os = OS.OTHER;
		if (System.getProperty("os.name").startsWith("Windows")) os = OS.WINDOWS;
		if (System.getProperty("os.name").startsWith("Mac")) os = OS.MAC;
		if (System.getProperty("os.name").startsWith("Linux")) os = OS.LINUX;
		
		// load up the version properties file
		Properties prop = new Properties();
		try {
			prop.load(new FileInputStream("version.properties"));
			versionNumber = prop.getProperty("version");
			System.out.println("OA version: " + versionNumber);
			firmwareVersionNumber = prop.getProperty("firmware_version");
		} catch (Exception e) {
			// If anything at all goes wrong here we just ignore it and use the default values
			// The stack trace is suppressed because it's annoying when debugging - the load
			// always fails.
			// TODO: this is a limitation of the way things are currently built. The problem
			// is the lib directory.
//			 e.printStackTrace();
		}
		
		// load Controller preferences
		prefs = Preferences.userNodeForPackage(this.getClass());
		String s = prefs.get(PREF_HEIGHT_UNITS, "FT");
		setHeightUnits(HeightUnits.valueOf(s));
		
		window = new MainWindow();
		window.controller = this;
		window.initialise();
		window.setTitle("openaltimeter " + versionNumber + " (" + firmwareVersionNumber + ")");
		altimeter = new Altimeter();
		window.show();
		buildSerialMenu();
		Controller.log("Graph hints: drag over area to zoom in, drag up and left to zoom out, click to annotate height, " +
						"shift-click to annotate vario. Annotations can be cleared from analysis menu.", "help");
	}
	
	// called by the main window when the app is shutting down.
	public void appStopping() {
		savePreferences();
	}
	
	private void savePreferences() {
		prefs.put(PREF_HEIGHT_UNITS, getHeightUnits().name());
	}
	
	private void setConnectionState(ConnectionState state) {
		connectionState = state;
		window.setConnectedState(state);
	}

	// this is for initialisation code that runs before the GUI is built
	private void buildSerialMenu() {
		// build the serial port selection menu
		Controller.log("Finding serial ports ...", "message");
		List<String> serialPorts = SerialLink.getSerialPorts();
		Iterator<String> it = serialPorts.iterator();
		while (it.hasNext())
			window.addCOMMenuItem(it.next());
		window.selectFirstCOMItem();
		Controller.log("Done.", "message");
	}

	// this is a bit cheezy - it's here to support a smooth upgrade path from old 115200 baud versions of the firmware
	// to the new 57600 baud versions. If connect fails a first time with a NotAnOpenaltimeterException then it will try
	// again at a reduced baud rate. This flag holds that state. An upgrade will immediately be offered for the old OA.
	boolean usingOldOABaudRate = false;
	// open the serial port and connect to the logger, print some summary information from the altimeter
	public void connect() {
		setConnectionState(ConnectionState.BUSY);
		new Thread( new Runnable() {
			public void run() {
				String comPort = window.getSelectedCOMPort();
				try {
					Controller.log("Connecting to serial port " + comPort + " (please wait) ...", "message");
					int baudRate = usingOldOABaudRate ? 115200 : 57600;
					Controller.log(altimeter.connect(comPort, baudRate), "altimeter");
					Controller.log("Connected.", "message");
					setConnectionState(ConnectionState.CONNECTED);
					// this is where we test for the firmware versions that are compatible with this
					// release of the downloader. If they aren't compatible then we try and start the
					// upgrade process.
					if (!altimeter.firmwareVersion.equals("V8")) 
					{
						if (!adviseFirmwareUpgrade()) return;
					}
				} catch (NotAnOpenaltimeterException e) {
					setConnectionState(ConnectionState.DISCONNECTED);
					if (!usingOldOABaudRate) {
						Controller.log("Unable to connect, checking for older OA connection (please wait) ...", "error");
						usingOldOABaudRate = true;
						connect();
						return;
					} else {
						Controller.log("Incorrect reply from device. Check that you've selected the correct serial port, and that " +
								"the openaltimeter is connected and powered.", "error");
						usingOldOABaudRate = false;
						return;
					}
				} catch (Exception e) {
					Controller.log("Exception opening serial port. Check your serial port settings.", "error");
					setConnectionState(ConnectionState.DISCONNECTED);
					return;
				}
				try {
					Controller.log("Getting log information ...", "message");
					Controller.log(altimeter.getFileInfo(), "altimeter");
					Controller.log("Done.", "message");
				} catch (IOException e) {
					Controller.log("Unable to get file information from altimeter.", "error");
				}
			}
		}).start();
	}


	// close the serial port
	public void disconnect() {
		if (connectionState != ConnectionState.DISCONNECTED) {
			altimeter.disconnect();
			Controller.log("Disconnected.", "message");
			usingOldOABaudRate = false;
			setConnectionState(ConnectionState.DISCONNECTED);
		}
	}
	
	// this returns a boolean indicating whether to stay connected to the logger or not.
	private boolean adviseFirmwareUpgrade() {
		String[] choices = {"Run firmware update utility ...", "Disconnect", "I'm feeling lucky, leave me connected!"};

		int dialogResult = JOptionPane.showOptionDialog(
				null, 
				"A firmware upgrade is required in order to use this version of the downloader. You can go directly to the firmware\n" +
				"upgrade dialog by clicking the button below.\n\n" +
				"Note that the firmware upgrade will delete all data on the logger. If you wish to save any data then you should \n" +
				"choose \"Disconnect\" below and use an older version of the downloader to save any data before proceeding.\n\n" +
				"If you're feeling brave, you can try and connect anyway, but things will probably not work well!\n",
				"Firmware upgrade required ...", 
				JOptionPane.OK_CANCEL_OPTION, 
				JOptionPane.WARNING_MESSAGE, 
				null, 
				choices,
				choices[0]
				);
		if (dialogResult == 0) 
		{
			flashFirmware();
			return false;
		}
		if (dialogResult == 1) {
			disconnect();
			return false;
		}
		if (dialogResult == 2) return true;
		disconnect();
		return false;
	}

	public void downloadData() {
		setConnectionState(ConnectionState.BUSY);
			new Thread( new Runnable() {
			public void run() {
				Controller.log("Downloading altimeter data (please wait) ...", "message");
				try {
					setFlightLog(altimeter.downloadData());
				} catch (IOException e) {
					Controller.log("Communications error while downloading data. Check the serial port.", "error");
				} catch (DownloadTimeoutException e) {
					Controller.log("Download started but did not complete in time. Check the serial port.", "error");
				} finally {
					window.setDataState(DataState.HAVE_DATA);
					setConnectionState(ConnectionState.CONNECTED);
					Controller.log("Done.", "message");
				}
			}
		}).start();
	}

	public void erase() {
		if (window.showConfirmDialog("Are you sure you want to erase the altimeter's memory?", "Erase ..."))
		{
			setConnectionState(ConnectionState.BUSY);
			new Thread( new Runnable() {
				public void run() {
					Controller.log("Erasing altimeter (please wait) ...", "message");
					try {
						log(altimeter.erase(), "altimeter");
					} catch (IOException e) {
						Controller.log("Error sending erase command. Check the serial port.", "error");
					} finally {
						setConnectionState(ConnectionState.CONNECTED);
						Controller.log("Done.", "message");
					}
				}
			}).start();
		}
	}
	
	public void setFlightLog(FlightLog log)
	{
		flightLog = log;
		window.altimeterChart.resetAnnotations();

		window.altimeterChart.setAltitudeData(log.getAltitude(), flightLog.logInterval);
		window.altimeterChart.setBatteryData(log.getBattery(), flightLog.logInterval);
		window.altimeterChart.setTemperatureData(log.getTemperature(), flightLog.logInterval);
		window.altimeterChart.setServoData(log.getServo(), flightLog.logInterval);
		
		window.altimeterChart.addEOFAnnotations(log.getEOFIndices(), flightLog.logInterval);

	}
	
	public void saveRaw() {
		File f = window.showRawSaveDialog();
		if (f == null) return;
		try {
			FileWriter fw = new FileWriter(f);
			fw.write(flightLog.rawDataToString());
			fw.close();
		} catch (IOException e) {
			window.log("Error writing file. Please check the filename and try again.", "error");
		}
	}
	
	public void saveRawSelection(double lower, double upper) {
		System.out.println("Lower: " + lower + " Upper: " + upper);
		File f = window.showRawSaveDialog();
		if (f == null) return;
		try {
			FileWriter fw = new FileWriter(f);
			fw.write(flightLog.rawDataToString((int) (lower / flightLog.logInterval), (int) (upper / flightLog.logInterval)));
			fw.close();
		} catch (IOException e) {
			window.log("Error writing file. Please check the filename and try again.", "error");
		}		
	}
	
	public void loadRawData() {
		File f = window.showOpenDialog(new FileNameExtensionFilter("Text files", "txt"));
		if (f == null) return;
		StringBuilder fileStringBuilder = new StringBuilder();
		try {
			BufferedReader fr = new BufferedReader(new FileReader(f));
			String line;
			while ((line = fr.readLine()) != null) {
				fileStringBuilder.append(line);
				fileStringBuilder.append("\n");
			}
		} catch (IOException e) {
			log("Unable to open file. Please check file is not open elsewhere and try again.", "error");
		}
		FlightLog fl = new FlightLog();
		try {
			fl.fromRawData(fileStringBuilder.toString());
		} catch (IOException e) {
			log("Unable to parse file. Are you sure that this is a raw data file?", "error");
		}
		setFlightLog(fl);
		window.setDataState(DataState.HAVE_DATA);
	}

	public void exit() {
		window.close();
		System.exit(0);
	}
	
	public static void log(String message, String style)
	{
		Controller.getController().window.log(message, style);
	}
	
	public static void setProgress(int progress)
	{
		Controller.getController().window.updateProgress(progress);
	}

	public void altitudePlotSelectedChange(boolean selected) {
		window.altimeterChart.setAltitudePlotVisible(selected);
	}

	public void voltagePlotSelectedChange(boolean selected) {
		window.altimeterChart.setVoltagePlotVisible(selected);
	}

	public void temperaturePlotSelectedChange(boolean selected) {
		window.altimeterChart.setTemperaturePlotVisible(selected);		
	}

	public void servoPlotSelectedChange(boolean selected) {
		window.altimeterChart.setServoPlotVisible(selected);		
	}

	public void unitSelectedChange(HeightUnits unitsSelected) {
		setHeightUnits(unitsSelected);
		window.altimeterChart.setHeightUnits(unitsSelected);
	}

	// this is called by the settings menu event handler
	SettingsDialog settingsDialog;
	public void runSettingsInterface() {
		settingsDialog = new SettingsDialog(this);
		new Thread(new Runnable() {
			public void run() {
				loadSettingsFromAltimeter();
				try {
					SwingUtilities.invokeAndWait(new Runnable() {
						public void run() {
							settingsDialog.setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
							settingsDialog.setModalityType(ModalityType.APPLICATION_MODAL);
							settingsDialog.setVisible(true);
						}});
				} catch (Exception e) {
					e.printStackTrace();
			}
		}}).start();
	}
	
	// a helper method to load the settings from the OA and update the settings dialog
	private void loadSettingsFromAltimeter()
	{
		try {
			log("Loading settings from altimeter ...", "message");
			altimeter.readSettings();
			settingsDialog.setSettings(altimeter.settings);
			log("Done.", "message");
		} catch (IOException e) {
			log("Unable to read settings from altimeter.", "error");
		}
	}
	
	//this is called by the settings dialog
	void saveSettingsToAltimeter()
	{
		settingsDialog.enableButtons(false);
		new Thread(new Runnable() {
			public void run() {
				try {
					log("Writing settings to altimeter ...", "message");
					Settings settingsToWrite = settingsDialog.getSettings();
					altimeter.settings = settingsToWrite;
					altimeter.writeSettings();
					log("Done.", "message");
					log("Rebooting altimeter with new settings ...", "message");
					altimeter.disconnect();
					String comPort = window.getSelectedCOMPort();
					Controller.log("Connecting to serial port " + comPort + " (please wait) ...", "message");
					Controller.log(altimeter.connect(comPort, 57600), "altimeter");
					Controller.log("Reboot finished.", "message");
					Controller.log("Verifying settings ...", "message");
					loadSettingsFromAltimeter();
					if (!settingsToWrite.equals(altimeter.settings)) {
						log("Error verifying altimeter settings. Please try saving again.", "error");
						settingsDialog.enableButtons(true);
						return;
					}
				} catch (IOException e) {
					log("Unable to write settings to altimeter.", "error");
					e.printStackTrace();
					disconnect();
				} catch (NotAnOpenaltimeterException e) {
					log("Incorrect reply from device. Check that you've selected the correct serial port, and that " +
							"the openaltimeter is connected and powered.", "error");
					e.printStackTrace();
					disconnect();
				} catch (Exception e) {
					log("Exception opening serial port. Check your serial port settings.", "error");
					e.printStackTrace();
					disconnect();
				} finally {
					settingsDialog.enableButtons(true);
					settingsDialog.dispose();
				}
			}}).start();
	}

	// flashes the firmware to a stable version, wipes the settings - putting them back
	// to default - and erases the log memory. This is supposed to be a "sure-fire" way
	// of getting the OA back into a workable state. It doesn't depend on there already
	// being working OA software on the board, only the arduino compatible bootloader.
	FirmwareDialog firmwareDialog;
	public void flashFirmware() {
		firmwareDialog = new FirmwareDialog(this);
		firmwareDialog.setVisible(true);
	}

	public void doFirmwareFlash() {
		firmwareDialog.enableButtons(false);
		new Thread(new Runnable() {
			public void run() {
				try {
					if (connectionState == ConnectionState.CONNECTED) {
						altimeter.disconnect();
						setConnectionState(ConnectionState.BUSY);
					}
					log("Flashing altimeter firmware ...", "message");
					doFirmwareUpload();
					log("Done.", "message");
					log("Rebooting altimeter ...", "message");
					String comPort = window.getSelectedCOMPort();
					log("Connecting to serial port " + comPort + " (please wait) ...", "message");
					log(altimeter.connect(comPort, 57600), "altimeter");
					log("Reboot finished.", "message");
					log("Erasing altimeter (please wait) ...", "message");
					log(altimeter.erase(), "altimeter");
					log("Wiping settings memory ...", "message");
					altimeter.wipeSettings();
					log("Firmware upgrade done - reconnecting ...", "message");
					altimeter.disconnect();
					log("Connecting to serial port " + comPort + " (please wait) ...", "message");
					log(altimeter.connect(comPort, 57600), "altimeter");
					log("Connected.", "message");
					setConnectionState(ConnectionState.CONNECTED);
				} catch (FirmwareFlashException e) {
					e.printStackTrace();
					log("Error flashing firmware.", "error");
					setConnectionState(ConnectionState.DISCONNECTED);
					return;
				} catch (IOException e) {
					e.printStackTrace();
					log("Error communicating with altimeter.", "error");
					altimeter.disconnect();
					setConnectionState(ConnectionState.DISCONNECTED);
					return;
				} catch (NotAnOpenaltimeterException e) {
					e.printStackTrace();
					log("Incorrect reply from device. Check that you've selected the correct serial port, and that " +
							"the openaltimeter is connected and powered.", "error");
					altimeter.disconnect();
					setConnectionState(ConnectionState.DISCONNECTED);
					return;
				} catch (Exception e) {
					e.printStackTrace();
					log("Exception communicating with altimeter. Check your serial port settings.", "error");
					altimeter.disconnect();
					setConnectionState(ConnectionState.DISCONNECTED);
					return;
				} finally {
					firmwareDialog.enableButtons(true);
					firmwareDialog.dispose();
				}

				runSettingsInterface();
			}}).start();
	}
	Properties prop;
	private void doFirmwareUpload() throws FirmwareFlashException {
		int exitCode;
		try {
			ProcessBuilder pb = new ProcessBuilder();
			// this would be where you'd switch based on OS to run the appropriate
			// firmware upload command.
			Vector<String> commandLine = new Vector<String>();
			if (os == OS.WINDOWS) {
//				pb.directory(new File("windows_flash"));
				commandLine.add("windows_flash/avrdude.exe");
				commandLine.add("-Cwindows_flash/avrdude.conf");
			}
			if (os == OS.MAC) {
				commandLine.add("./mac_flash/avrdude");
				commandLine.add("-Cmac_flash/avrdude.conf");
			}
			if (os == OS.LINUX) {
				pb.directory(new File(System.getProperty("user.dir")));
				commandLine.add("avrdude");
			}
			commandLine.add("-Ueeprom:w:firmware/blank_eeprom.hex:i");
			commandLine.add("-Uflash:w:firmware/firmware.hex:i");
			commandLine.add("-q");
			commandLine.add("-patmega328p");
			commandLine.add("-cstk500v1");
			commandLine.add("-P" + window.getSelectedCOMPort());
			commandLine.add("-b57600");
			commandLine.add("-D");
			pb.command(commandLine);

			Process p = pb.start();

			StreamLogPump errorPump = new StreamLogPump(p.getErrorStream(), "error");
			StreamLogPump outputPump = new StreamLogPump(p.getInputStream(), "altimeter");
			errorPump.start();
			outputPump.start();
	
			exitCode = p.waitFor();
		} catch (Exception e) {
			e.printStackTrace();
			throw new FirmwareFlashException();
		}
		if (exitCode != 0) {
			log("avrdude failed with exit code: " + exitCode, "error");
			throw new FirmwareFlashException();
		}
	}
	
	class StreamLogPump extends Thread {
		InputStream stream;
		String type;

		StreamLogPump(InputStream stream, String type) {
			this.stream = stream;
			this.type = type;
		}

		public void run() {
			try {
				BufferedReader reader = new BufferedReader(new InputStreamReader(stream));
				String line;
				while ((line = reader.readLine()) != null) Controller.log(line, type);
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}
	
	public void showFirmwareReadme() {
		try {	
			// seems odd that there isn't a cross platform way to do this!
			if (os == OS.WINDOWS)
				Runtime.getRuntime().exec("cmd.exe /c start firmware/readme.txt");
			if (os == OS.MAC)
				Runtime.getRuntime().exec("open firmware/readme.txt");
			// this seems cheezy, but I don't know a better way to do it.
			if (os == OS.LINUX)
				Controller.log("Not yet working on Linux yet! Please look in the firmware directory.", "error");
				//Runtime.getRuntime().exec("vi " + System.getProperty("user.dir") + "/lib/firmware/readme.txt");
			} catch (IOException e) {
			Controller.log("Unable to open firmware readme file.", "error");
			e.printStackTrace();
		}
	}
	
	public void uploadSelection(double lowerBound, double upperBound) {
		String dataToUpload = flightLog.rawDataToUploadString((int) (lowerBound / flightLog.logInterval), 
				(int) (upperBound / flightLog.logInterval));
		try {
			altimeter.upload(dataToUpload);
		} catch (IOException e){
			Controller.log("Unable to upload data.", "error");
			e.printStackTrace();
		}
		//disconnect();
	}
	
	@SuppressWarnings("serial")
	class FirmwareFlashException extends Exception {}

	public void analyseDLGFlights() {
		window.altimeterChart.clearDLGAnalysis();
		
		// ask the user which parts of the analysis they wish to carry out
		DLGAnalysisDialog dialog = new DLGAnalysisDialog(this);
		dialog.setVisible(true);
		if (dialog.isSuccessful()) {	
			DLGFlightAnalyser finder = new DLGFlightAnalyser();
			List<DLGFlight> flights = finder.findDLGLaunches(flightLog.getAltitude(), flightLog.logInterval);
			if (dialog.shouldCorrectBaseline()) {
				// correct the launch heights
				double[] newAltData = finder.correctDLGBaseline(flightLog.getAltitude(), flights);
				flightLog.setAltitude(newAltData);
				// update the plot
				window.altimeterChart.setAltitudeData(flightLog.getAltitude(), flightLog.logInterval);
			}
			
			// plot the annotations
			if (dialog.shouldMarkLaunchHeights()) {
				for (DLGFlight d : flights) 
					window.altimeterChart.addDLGHeightAnnotation(d.launchIndex * flightLog.logInterval, d.launchHeight);
			}
			
			if (dialog.shouldMarkMaxHeights()) {
				for (DLGFlight d : flights) {
					if (d.launchHeight != d.maxHeight) 
						window.altimeterChart.addDLGMaxHeightAnnotation(d.maxIndex * flightLog.logInterval, d.maxHeight);
				}
			}
			
			for (DLGFlight d: flights)
				window.altimeterChart.addDLGStartAnnotation(d.startIndex * flightLog.logInterval, d.startHeight);

			if (dialog.shouldShowStatistics()) {
				// show the analysis results
				DLGAnalysisResultsWindow resWin = new DLGAnalysisResultsWindow(flights, getHeightUnits());
				resWin.setVisible(true);
			}
		}
	}

	public void clearDLGAnalysis() {
		window.altimeterChart.clearDLGAnalysis();
		// undo the baseline correction
		flightLog.calculateAltitudes();
		window.altimeterChart.setAltitudeData(flightLog.getAltitude(), flightLog.logInterval);
	}

}
