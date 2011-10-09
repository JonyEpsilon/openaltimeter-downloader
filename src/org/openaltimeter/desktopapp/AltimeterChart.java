package org.openaltimeter.desktopapp;

import java.awt.BasicStroke;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Paint;
import java.awt.event.AdjustmentEvent;
import java.awt.event.AdjustmentListener;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.List;

import javax.swing.JPanel;
import javax.swing.JScrollBar;
import javax.swing.SwingUtilities;

import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.axis.NumberAxis;
import org.jfree.chart.axis.ValueAxis;
import org.jfree.chart.event.AxisChangeEvent;
import org.jfree.chart.event.AxisChangeListener;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.chart.plot.XYPlot;
import org.jfree.chart.renderer.xy.StandardXYItemRenderer;
import org.jfree.data.xy.XYSeries;
import org.jfree.data.xy.XYSeriesCollection;
import org.openaltimeter.desktopapp.annotations.AltimeterAnnotationManager;
import org.openaltimeter.desktopapp.annotations.XYDotAnnotation;


public class AltimeterChart  {
	
	private static final Color TEMPERATURE_COLOR = Color.gray;
	private static final Color SERVO_COLOR = Color.blue;
	private static final Color VOLTAGE_COLOR = new Color(82, 255, 99);
	private static final Color PRESSURE_COLOR = new Color(56, 136, 255);
	private static final Color BG_COLOR = new Color(204, 224, 255);
	private static final float LINE_WIDTH = 1.1f;
	
	private JFreeChart chart;
	private JScrollBar domainScrollBar;
	private ChartPanel chartPanel;
	private AltimeterAnnotationManager annotationManager;
	
	XYSeries altitudeData = new XYSeries("Altitude");
	XYSeries batteryData = new XYSeries("Battery voltage");
	XYSeries temperatureData = new XYSeries("Temperature");
	XYSeries servoData = new XYSeries("Servo");
	
	public AltimeterChart() {
		chart = createChart();
		chartPanel = createChartPanel();
		annotationManager = new AltimeterAnnotationManager(chartPanel);
		annotationManager.addMouseListener();
	}
	
	public ChartPanel getChartPanel() {
		return chartPanel;
	}
	
	private JFreeChart createChart() {

		XYSeriesCollection seriesColl = new XYSeriesCollection();
		seriesColl.addSeries(altitudeData);

		XYSeriesCollection batteryColl = new XYSeriesCollection();
		batteryColl.addSeries(batteryData);
		
		XYSeriesCollection tempColl = new XYSeriesCollection();
		tempColl.addSeries(temperatureData);
		
		XYSeriesCollection servoColl = new XYSeriesCollection();
		servoColl.addSeries(servoData);
		
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
        plot.setDomainGridlinesVisible(false);
        plot.setRangeGridlinesVisible(false);
        plot.setBackgroundPaint(BG_COLOR);
        
        addAxis(plot, "Altitude (ft)", 0, 2);        
        addAxis(plot, "Battery (V)", 1, 2);        
        addAxis(plot, "Servo (us)", 2, 2);        
        addAxis(plot, "Temperature (C)", 3, 2);        
                
        plot.setDataset(0, seriesColl);
        plot.setDataset(1, batteryColl);
        plot.setDataset(2, servoColl);
        plot.setDataset(3, tempColl);
        
        plot.mapDatasetToRangeAxis(0, 0);
        plot.mapDatasetToRangeAxis(1, 1);
        plot.mapDatasetToRangeAxis(2, 2);
        plot.mapDatasetToRangeAxis(3, 3);
        
        addRenderer(plot, PRESSURE_COLOR, 0);
        addRenderer(plot, VOLTAGE_COLOR, 1);
        addRenderer(plot, SERVO_COLOR, 2);
        addRenderer(plot, TEMPERATURE_COLOR, 3);
   
        plot.setDomainPannable(false);
        plot.setRangePannable(false);
       
        plot.setDomainCrosshairLockedOnData(true);
        plot.setRangeCrosshairLockedOnData(true);
        
        plot.getDomainAxis().addChangeListener(new AxisChangeListener() {
			@Override
			public void axisChanged(AxisChangeEvent arg0) {
				int l = (int) plot.getDomainAxis().getRange().getLowerBound();
				int r = (int) plot.getDomainAxis().getRange().getUpperBound();
				
				domainScrollBar.setValues(l, r - l, 0, domainScrollBar.getMaximum());
			}
		});
        
        return chart;
	}

	private void addRenderer(XYPlot plot, Paint color, int series) {
		StandardXYItemRenderer renderer = new StandardXYItemRenderer();
        renderer.setSeriesPaint(0, color);
        renderer.setSeriesStroke(0, new BasicStroke(LINE_WIDTH));
        plot.setRenderer(series, renderer);
	}

	private void addAxis(XYPlot plot, String name, int series, int digits) {
		NumberAxis axis = new NumberAxis(name);
        axis.setAutoRangeIncludesZero(false);
        NumberFormat df = DecimalFormat.getInstance();
        df.setMaximumFractionDigits(digits);
        axis.setNumberFormatOverride(df);
        plot.setRangeAxis(series, axis);
	}
	
	private ChartPanel createChartPanel() {
		
		final ChartPanel cp = new ChartPanel(chart);
		
		domainScrollBar = getScrollBar(((XYPlot)chart.getPlot()).getDomainAxis());
		JPanel pnl = new JPanel();
		pnl.setLayout(new BorderLayout());
		pnl.add(cp, BorderLayout.CENTER);
		pnl.add(domainScrollBar, BorderLayout.SOUTH);
		
		return cp;
	}
	
    private JScrollBar getScrollBar(final ValueAxis domainAxis){
        final JScrollBar scrollBar = new JScrollBar(JScrollBar.HORIZONTAL, 0, 0, 0, 0);
        scrollBar.addAdjustmentListener( new AdjustmentListener() {
            public void adjustmentValueChanged(AdjustmentEvent e) {
                int x = e.getValue();
                domainAxis.setRange(x, x + scrollBar.getVisibleAmount());
            }
        });
        return scrollBar;
    }
	
	public void setAltitudeData(final double[] data, final double timeStep)
	{
		setDataSeries(data, timeStep, altitudeData);
	}
	
	public void setBatteryData(final double[] data, final double timeStep)
	{
		setDataSeries(data, timeStep, batteryData);
	}
	
	public void setTemperatureData(final double[] data, final double timeStep)
	{
		setDataSeries(data, timeStep, temperatureData);
	}
	
	public void setServoData(final double[] data, final double timeStep)
	{
		setDataSeries(data, timeStep, servoData);
	}

	public void setDataSeries(final double[] data, final double timeStep, final XYSeries series) {
		SwingUtilities.invokeLater(new Runnable() {
			public void run() {
				series.clear();
				for (int i = 0; i < data.length; i++) series.add(timeStep * i, data[i], false);
				series.fireSeriesChanged();
				domainScrollBar.setValues(0, (int) (data.length / timeStep) + 1, 0, (int) (data.length * timeStep) + 1);
			}});
	}
	
	// These annotations are added when the data is loaded, and are never erased. They
	// are not managed (or indeed touched) by the AnnotationManager.
	public void addEOFAnnotations(final List<Integer> eofIndices, final double timeStep)
	{
		SwingUtilities.invokeLater(new Runnable() {
			public void run() {
				XYPlot plot = chart.getXYPlot();
				for(int eofIndex : eofIndices) plot.addAnnotation(
						new XYDotAnnotation(eofIndex * timeStep, 0.0, 4, Color.DARK_GRAY));
			}});
	}
	
	public void setAltitudePlotVisible(boolean selected) {
		showPlot(selected, 0);
	}
	
	public void setVoltagePlotVisible(boolean selected) {
		showPlot(selected, 1);
	}
	
	public void setServoPlotVisible(boolean selected) {
		showPlot(selected, 2);
	}		

	public void setTemperaturePlotVisible(boolean selected) {
		showPlot(selected, 3);
	}

	private void showPlot(boolean selected, int index) {
		chart.getXYPlot().getRenderer(index).setSeriesVisible(0, selected);
		chart.getXYPlot().getRangeAxis(index).setVisible(selected);
	}

	public void setPlotUnit(boolean selected) {
		chart.getXYPlot().getRangeAxis(0).setLabel(selected ? "Altitude (ft)" : "Altitude (m)");
	}

	public void clearAnnotations() {
		annotationManager.clearHeightAndVarioAnnotations();
	}
	
	public void resetAnnotations() {
		annotationManager.resetAnnotations();
	}
	
	public double getVisibleDomainLowerBound() {
		return chart.getXYPlot().getDomainAxis().getLowerBound();
	}
	
	public double getVisibleDomainUpperBound() {
		return chart.getXYPlot().getDomainAxis().getUpperBound();
	}

}
