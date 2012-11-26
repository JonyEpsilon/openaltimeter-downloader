package org.openaltimeter.desktopapp;

import java.awt.Font;
import java.awt.Toolkit;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.List;

import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JTextArea;

import org.apache.commons.math.stat.descriptive.DescriptiveStatistics;
import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.axis.NumberAxis;
import org.jfree.chart.axis.NumberTickUnit;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.chart.plot.XYPlot;
import org.jfree.chart.renderer.xy.StandardXYBarPainter;
import org.jfree.chart.renderer.xy.XYBarRenderer;
import org.jfree.data.statistics.HistogramDataset;
import org.jfree.data.statistics.HistogramType;
import org.openaltimeter.data.AltitudeConverter;
import org.openaltimeter.data.HeightUnits;
import org.openaltimeter.data.analysis.DLGFlight;

@SuppressWarnings("serial")
public class DLGAnalysisResultsWindow extends JFrame {

	private JPanel contentPane;
	private HistogramDataset hds;
	private JFreeChart chart;
	List<DLGFlight> flights;
	HeightUnits units;
	private DescriptiveStatistics ds;
	private String statisticsText;
	private String unitString;

	public DLGAnalysisResultsWindow(List<DLGFlight> flights, HeightUnits units) {
		setResizable(false);
		this.flights = flights;
		this.units = units;
		if (units == HeightUnits.FT) unitString = "ft";
		else unitString = "m";
		prepareData();
		makeGUI();
	}

	private void makeGUI() {
		setIconImage(Toolkit.getDefaultToolkit().getImage(DLGAnalysisDialog.class.getResource("/logo_short_64.png")));
		setTitle("DLG flight analysis");
		setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
		setBounds(100, 100, 649, 480);
		contentPane = new JPanel();
		setContentPane(contentPane);
		contentPane.setLayout(null);
				
		chart = ChartFactory.createHistogram(null,
				"Height (" + unitString + ")", 
				"Frequency", 
				hds, 
				PlotOrientation.VERTICAL, 
				false, 
				true, 
				false);
		final XYPlot plot = chart.getXYPlot();
        plot.setDomainGridlinesVisible(false);
        plot.setRangeGridlinesVisible(false);
        plot.setBackgroundPaint(AltimeterChart.BG_COLOR);
             
		NumberAxis axisD = new NumberAxis("Launch height (" + unitString + ")");
		axisD.setAutoRange(true);
        plot.setDomainAxis(axisD);
		NumberAxis axisR = new NumberAxis("Frequency");
		axisR.setTickUnit(new NumberTickUnit(1));
		axisR.setAutoRange(true);
        plot.setRangeAxis(axisR);
        
		XYBarRenderer renderer = new XYBarRenderer();
		renderer.setBarPainter(new StandardXYBarPainter());
		renderer.setShadowVisible(false);
        renderer.setSeriesPaint(0, AltimeterChart.PRESSURE_COLOR);
        plot.setRenderer(renderer);
		
		ChartPanel cp = new ChartPanel(chart);
		cp.setBounds(0, 0, 414, 456);
		contentPane.add(cp);
		
		JTextArea textArea = new JTextArea();
		textArea.setBounds(414, 6, 233, 450);
		textArea.setEditable(false);
		textArea.setText(statisticsText);
		textArea.setFont(new Font(Font.MONOSPACED,Font.PLAIN, 12));
		contentPane.add(textArea);
	}
	
	void prepareData() {
		// extract launch height data
		double[] heights = new double[flights.size()];
		for (int i = 0; i < flights.size(); i++) heights[i] = flights.get(i).launchHeight;
		if (units == HeightUnits.FT) {
			for  (int i = 0; i < flights.size(); i++) heights[i] = AltitudeConverter.feetFromM(heights[i]);
		}
		// prepare stats
		NumberFormat df = DecimalFormat.getInstance();
		df.setMaximumFractionDigits(1);
		ds = new DescriptiveStatistics(heights);
		statisticsText = "Number of launches: " + ds.getN() + "\n" +
				"Mean launch height: " + df.format(ds.getMean()) + " " + unitString + "\n" +
				"Standard deviation: " + df.format(ds.getStandardDeviation()) + " " + unitString + "\n" +
				"Median launch height: " + df.format(ds.getPercentile(50)) + " " + unitString + "\n" +
				"Max launch height: " + df.format(ds.getMax()) + " " + unitString + "\n";

		// prepare
		hds = new HistogramDataset();
		hds.setType(HistogramType.FREQUENCY);
		int numBins = 30;//(int)Math.round(ds.getMax() - ds.getMin());
		hds.addSeries("Launches", heights, numBins);
	}

}
