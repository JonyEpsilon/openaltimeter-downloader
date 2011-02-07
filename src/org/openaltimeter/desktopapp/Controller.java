/*
    openaltimeter -- an open-source altimeter for RC aircraft
    Copyright (C) 2010  Jony Hudson, Jan Steidl
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

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.openaltimeter.comms.Altimeter;
import org.openaltimeter.comms.Altimeter.DownloadTimeoutException;
import org.openaltimeter.comms.Altimeter.NotAnOpenaltimeterException;
import org.openaltimeter.comms.SerialLink;
import org.openaltimeter.data.FlightLog;
import org.openaltimeter.desktopapp.MainWindow.ConnectionState;
import org.openaltimeter.desktopapp.MainWindow.DataState;

import flexjson.JSONDeserializer;
import flexjson.JSONSerializer;


public class Controller {

	private static final double LOG_INTERVAL = 0.5;
	static Controller controller;
	Altimeter altimeter;
	MainWindow window;
	FlightLog flightLog;

	/**
	 * @param args
	 */
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
		window = new MainWindow();
		window.controller = this;
		window.initialise();
		altimeter = new Altimeter();
		window.show();
		buildSerialMenu();
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

	// open the serial port and connect to the logger, print some summary information from the altimeter
	public void connect() {
		window.setConnectedState(ConnectionState.BUSY);
		new Thread( new Runnable() {
			public void run() {
				try {
					String comPort = window.getSelectedCOMPort();
					Controller.log("Connecting to serial port " + comPort + " (please wait) ...", "message");
					Controller.log(altimeter.connect(comPort), "altimeter");
					Controller.log("Connected.", "message");
					window.setConnectedState(ConnectionState.CONNECTED);
				} catch (NotAnOpenaltimeterException e) {
					Controller.log("Incorrect reply from device. Check that you've selected the correct serial port, and that " +
							"the openaltimeter is connected and powered.", "error");
					window.setConnectedState(ConnectionState.DISCONNECTED);
				} catch (Exception e) {
					Controller.log("Exception opening serial port. Check your serial port settings.", "error");
					window.setConnectedState(ConnectionState.DISCONNECTED);
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
		altimeter.disconnect();
		Controller.log("Disconnected.", "message");
		window.setConnectedState(ConnectionState.DISCONNECTED);
	}

	public void downloadData() {
		window.setConnectedState(ConnectionState.BUSY);
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
					window.setConnectedState(ConnectionState.CONNECTED);
					Controller.log("Done.", "message");
				}
			}
		}).start();
	}

	public void erase() {
		if (window.showConfirmDialog("Are you sure you want to erase the altimeter's memory?", "Erase ..."))
		{
			window.setConnectedState(ConnectionState.BUSY);
			new Thread( new Runnable() {
				public void run() {
					Controller.log("Erasing altimeter (please wait) ...", "message");
					try {
						log(altimeter.erase(), "altimeter");
					} catch (IOException e) {
						Controller.log("Error sending erase command. Check the serial port.", "error");
					} finally {
						window.setConnectedState(ConnectionState.CONNECTED);
						Controller.log("Done.", "message");
					}
				}
			}).start();
		}
	}
	
	public void setFlightLog(FlightLog log)
	{
		flightLog = log;
		window.setAltitudeData(log.getAltitudeFt(), LOG_INTERVAL);
		window.setBatteryData(log.getBattery(), LOG_INTERVAL);
		window.setTemperatureData(log.getTemperature(), LOG_INTERVAL);
	}
	
	public void saveRaw() {
		File f = window.showSaveDialog();
		if (f == null) return;
		try {
			FileWriter fw = new FileWriter(f);
			fw.write(flightLog.rawDataToString());
			fw.close();
		} catch (IOException e) {
			window.log("Error writing file. Please check the filename and try again.", "error");
		}
	}
	
	public void saveProcessed() {
		File f = window.showSaveDialog();
		if (f == null) return;
		try {
			FileWriter fw = new FileWriter(f);
			JSONSerializer js = new JSONSerializer();
			String json = js
							.include("logData", "annotations")
							.prettyPrint(true)
							.serialize(flightLog);
			fw.write(json);
			fw.close();
		} catch (IOException e) {
			window.log("Error writing file. Please check the filename and try again.", "error");
		}
	}
	
	public void loadRawData() {
		File f = window.showOpenDialog();
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
		
	}

	public void loadProcessedData() {
		File f = window.showOpenDialog();
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
		FlightLog fl = new JSONDeserializer<FlightLog>()
							.use("logData", ArrayList.class)
	//						.use("logData.values", LogEntry.class)
							.use("annotations",  ArrayList.class)
	//						.use("annotations.values", Annotation.class)
							.deserialize(fileStringBuilder.toString());
		setFlightLog(fl);
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
		window.setAltitudePlotVisible(selected);
	}

	public void voltagePlotSelectedChange(boolean selected) {
		window.setVoltagePlotVisible(selected);
	}

	public void temperaturePlotSelectedChange(boolean selected) {
		window.setTemperaturePlotVisible(selected);		
	}
}
