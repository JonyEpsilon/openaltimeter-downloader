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
import org.openaltimeter.data.analysis.DLGFlight;

@SuppressWarnings("serial")
public class DLGAnalysisResultsWindow extends JFrame {

	private JPanel contentPane;
	private HistogramDataset hds;
	private JFreeChart chart;
	List<DLGFlight> flights;
	private DescriptiveStatistics ds;
	private String statisticsText;

	public DLGAnalysisResultsWindow(List<DLGFlight> flights) {
		this.flights = flights;
		prepareData();
		makeGUI();
	}

	private void makeGUI() {
		setIconImage(Toolkit.getDefaultToolkit().getImage(DLGAnalysisDialog.class.getResource("/logo_short_64.png")));
		setTitle("DLG flight analysis");
		setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
		setBounds(100, 100, 663, 494);
		contentPane = new JPanel();
		setContentPane(contentPane);
		contentPane.setLayout(null);
				
		chart = ChartFactory.createHistogram(null,
				"Height (m)", 
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
             
		NumberAxis axisD = new NumberAxis("Launch height (m)");
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
		textArea.setBounds(414, 0, 233, 456);
		textArea.setEditable(false);
		textArea.setText(statisticsText);
		textArea.setFont(new Font(Font.MONOSPACED,Font.PLAIN, 12));
		contentPane.add(textArea);
	}
	
	void prepareData() {
		// extract launch height data
		double[] heights = new double[flights.size()];
		for (int i = 0; i < flights.size(); i++) heights[i] = flights.get(i).launchHeight;
		// prepare stats
		NumberFormat df = DecimalFormat.getInstance();
		df.setMaximumFractionDigits(1);
		ds = new DescriptiveStatistics(heights);
		statisticsText = "Number of launches: " + ds.getN() + "\n" +
				"Mean launch height: " + df.format(ds.getMean()) + " m\n" +
				"Standard deviation: " + df.format(ds.getStandardDeviation()) + " m\n" +
				"Median launch height: " + df.format(ds.getPercentile(50)) + " m\n" +
				"Max launch height: " + df.format(ds.getMax()) + " m\n";

		// prepare
		hds = new HistogramDataset();
		hds.setType(HistogramType.FREQUENCY);
		int numBins = 30;//(int)Math.round(ds.getMax() - ds.getMin());
		hds.addSeries("Launches", heights, numBins);
	}

}
