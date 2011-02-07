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

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.File;

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
import javax.swing.text.BadLocationException;
import javax.swing.text.Style;
import javax.swing.text.StyleConstants;
import javax.swing.text.StyleContext;
import javax.swing.text.StyledDocument;

import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.axis.NumberAxis;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.chart.plot.XYPlot;
import org.jfree.chart.renderer.xy.StandardXYItemRenderer;
import org.jfree.data.xy.XYSeries;
import org.jfree.data.xy.XYSeriesCollection;

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
	XYSeries altitudeData = new XYSeries("Altitude");
	XYSeries batteryData = new XYSeries("Battery voltage");
	XYSeries temperatureData = new XYSeries("Temperature");
		
	private final JProgressBar progressBar = new JProgressBar();
	private JMenuItem mntmSaveData;
	private JMenuItem mntmSaveProcessedData;
	private JFreeChart chart;
	
	public enum ConnectionState {CONNECTED, DISCONNECTED, BUSY};
	public enum DataState {NO_DATA, HAVE_DATA};
	
	public void show() {
		frmOpenaltimeter.setVisible(true);
	}

	/**
	 * @wbp.parser.entryPoint
	 */
	public void initialise() {
		try {
			UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
		} catch (Exception e) {
			System.err.println("Unable to switch to native look and feel.");
		}

		frmOpenaltimeter = new JFrame();
		frmOpenaltimeter.addWindowListener(new WindowAdapter() {
			public void windowClosing(WindowEvent arg0) {
				controller.exit();
			}
		});
		frmOpenaltimeter.setTitle("openAltimeter");
		frmOpenaltimeter.setBounds(70, 70, 1100, 700);
		frmOpenaltimeter.setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);

		JMenuBar menuBar = new JMenuBar();
		frmOpenaltimeter.setJMenuBar(menuBar);

		JMenu mnFile = new JMenu("File");
		menuBar.add(mnFile);

		mntmSaveData = new JMenuItem("Save raw data ...");
		mntmSaveData.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				controller.saveRaw();
			}
		});
		
		JMenuItem mntmLoadRawData = new JMenuItem("Load raw data ...");
		mntmLoadRawData.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				controller.loadRawData();
			}
		});
		mnFile.add(mntmLoadRawData);
		
		JMenuItem mntmLoadProcessedData = new JMenuItem("Load processed data ...");
		mntmLoadProcessedData.setEnabled(false);
		mntmLoadProcessedData.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				controller.loadProcessedData();
			}
		});
		mnFile.add(mntmLoadProcessedData);
		
		mnFile.addSeparator();
		mntmSaveData.setEnabled(false);
		mnFile.add(mntmSaveData);
		
		mntmSaveProcessedData = new JMenuItem("Save processed data ...");
		mntmSaveProcessedData.setEnabled(false);
		mntmSaveProcessedData.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				controller.saveProcessed();
			}
		});
		mnFile.add(mntmSaveProcessedData);

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
		
		final JCheckBoxMenuItem chckbxmntmVoltage = new JCheckBoxMenuItem("Voltage");
		chckbxmntmVoltage.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				controller.voltagePlotSelectedChange(chckbxmntmVoltage.isSelected());
			}
		});
		mnView.add(chckbxmntmVoltage);
		
		final JCheckBoxMenuItem chckbxmntmTemperature = new JCheckBoxMenuItem("Temperature");
		chckbxmntmTemperature.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				controller.temperaturePlotSelectedChange(chckbxmntmTemperature.isSelected());
			}
		});
		mnView.add(chckbxmntmTemperature);
		
		mnView.addSeparator();
		
		ButtonGroup heightUnitsGroup = new ButtonGroup();
		
		JRadioButtonMenuItem rdbtnmntmFeet = new JRadioButtonMenuItem("Feet");
		rdbtnmntmFeet.setEnabled(false);
		rdbtnmntmFeet.setSelected(true);
		heightUnitsGroup.add(rdbtnmntmFeet);
		mnView.add(rdbtnmntmFeet);
		
		JRadioButtonMenuItem rdbtnmntmMetres = new JRadioButtonMenuItem("Metres");
		rdbtnmntmMetres.setEnabled(false);
		heightUnitsGroup.add(rdbtnmntmMetres);
		mnView.add(rdbtnmntmMetres);

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

		JSplitPane splitPane = new JSplitPane();
		splitPane.setContinuousLayout(true);
		splitPane.setResizeWeight(0.65);
		splitPane.setOrientation(JSplitPane.VERTICAL_SPLIT);
		splitPane.setDividerLocation(-1);
		frmOpenaltimeter.getContentPane().add(splitPane, BorderLayout.CENTER);

		// create a chart...
		XYSeriesCollection seriesColl = new XYSeriesCollection();
		seriesColl.addSeries(altitudeData);

		XYSeriesCollection batteryColl = new XYSeriesCollection();
		batteryColl.addSeries(batteryData);
		
		XYSeriesCollection tempColl = new XYSeriesCollection();
		tempColl.addSeries(temperatureData);

		chart = ChartFactory.createXYLineChart(
						null,
						"Time (s)", 
						"Altitude (ft)",
						seriesColl,
						PlotOrientation.VERTICAL, 
						false, // legend?
						true, // tooltips?
						false // URLs?
					);

		final XYPlot plot = chart.getXYPlot();
		
		plot.getRangeAxis(0).setTickLabelPaint(Color.RED);
		
        final NumberAxis axisBat = new NumberAxis("Battery (V)");
        axisBat.setAutoRangeIncludesZero(false);
       // axisBat.setLabelPaint(Color.GREEN);
        axisBat.setTickLabelPaint(Color.green);
        plot.setRangeAxis(1, axisBat);

        final NumberAxis axisTemp = new NumberAxis("Temperature");
        axisTemp.setAutoRangeIncludesZero(false);
        //axisTemp.setLabelPaint(Color.BLUE);
        axisTemp.setTickLabelPaint(Color.BLUE);
        plot.setRangeAxis(2, axisTemp);

        plot.setDataset(0, seriesColl);
        plot.setDataset(1, batteryColl);
        plot.setDataset(2, tempColl);
        
        plot.mapDatasetToRangeAxis(0, 0);
        plot.mapDatasetToRangeAxis(1, 1);
        plot.mapDatasetToRangeAxis(2, 2);

        final StandardXYItemRenderer renderer2 = new StandardXYItemRenderer();
        renderer2.setSeriesPaint(0, Color.green);
        plot.setRenderer(1, renderer2);

        final StandardXYItemRenderer renderer3 = new StandardXYItemRenderer();
        renderer3.setSeriesPaint(0, Color.blue);
        plot.setRenderer(2, renderer3);

		ChartPanel cp = new ChartPanel(chart);
		splitPane.setTopComponent(cp);
		
		setVoltagePlotVisible(false);
		setTemperaturePlotVisible(false);

		// create the text pane, a document for it to view, and some styles
		logTextPane = new JTextPane();
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
					mnSerialPort.setEnabled(false);
					break;
				case DISCONNECTED: 
					mntmConnect.setEnabled(true);
					mntmDisconnect.setEnabled(false);
					mntmDownloadData.setEnabled(false);
					mntmEraseLogger.setEnabled(false);
					mnSerialPort.setEnabled(true);
					break;
				case BUSY: 
					mntmConnect.setEnabled(false);
					mntmDisconnect.setEnabled(false);
					mntmDownloadData.setEnabled(false);
					mntmEraseLogger.setEnabled(false);
					mnSerialPort.setEnabled(false);
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
					mntmSaveProcessedData.setEnabled(false);
					break;
				case HAVE_DATA: 
					mntmSaveData.setEnabled(true);
					mntmSaveProcessedData.setEnabled(true);
					break;
				}
			}
		});
	}
	
	public void setAltitudeData(final double[] data, final double timeStep)
	{
		SwingUtilities.invokeLater(new Runnable() {
			public void run() {
				altitudeData.clear();
				for (int i = 0; i < data.length; i++) altitudeData.add(timeStep * i, data[i]);
			}});
	}
	
	public void setBatteryData(final double[] data, final double timeStep)
	{
		SwingUtilities.invokeLater(new Runnable() {
			public void run() {
				batteryData.clear();
				for (int i = 0; i < data.length; i++) batteryData.add(timeStep * i, data[i]);
			}});
	}
	
	public void setTemperatureData(final double[] data, final double timeStep)
	{
		SwingUtilities.invokeLater(new Runnable() {
			public void run () {
				temperatureData.clear();
				for (int i = 0; i < data.length; i++) temperatureData.add(timeStep * i, data[i]);
			}
		});
	}
	
	public void close()
	{
		frmOpenaltimeter.dispose();
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
	
	public File showSaveDialog()
	{
		JFileChooser fc = new JFileChooser();
		if (fc.showSaveDialog(this.frmOpenaltimeter) == JFileChooser.APPROVE_OPTION)
			return fc.getSelectedFile();
		else return null;
	}

	public File showOpenDialog()
	{
		JFileChooser fc = new JFileChooser();
		if (fc.showOpenDialog(this.frmOpenaltimeter) == JFileChooser.APPROVE_OPTION)
			return fc.getSelectedFile();
		else return null;
	}

	public boolean showConfirmDialog(String message, String title)
	{
		int response = JOptionPane.showConfirmDialog(this.frmOpenaltimeter, message, title, JOptionPane.OK_CANCEL_OPTION);
		return (response == JOptionPane.OK_OPTION);
	}

	public void setAltitudePlotVisible(boolean selected) {
		showPlot(selected, 0);
	}
	
	public void setVoltagePlotVisible(boolean selected) {
		showPlot(selected, 1);
	}
	
	public void setTemperaturePlotVisible(boolean selected) {
		showPlot(selected, 2);
	}
		
	private void showPlot(boolean selected, int index) {
		chart.getXYPlot().getRenderer(index).setSeriesVisible(0, selected);
		chart.getXYPlot().getRangeAxis(index).setVisible(selected);
	}

}
