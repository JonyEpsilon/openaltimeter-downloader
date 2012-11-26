package org.openaltimeter.desktopapp.annotations;

import java.awt.Color;
import java.awt.Paint;
import java.util.ArrayList;
import java.util.List;

import org.jfree.chart.ChartPanel;
import org.jfree.chart.annotations.XYAnnotation;
import org.jfree.chart.plot.XYPlot;
import org.jfree.ui.TextAnchor;

public class AltimeterAnnotationManager {

	private static final Color EOF_COLOR = Color.DARK_GRAY;

	private ChartPanel cp;
	private ArrayList<XYAnnotation> eofAList = new ArrayList<XYAnnotation>();
	private ArrayList<XYHeightAnnotation> userHAList = new ArrayList<XYHeightAnnotation>();
	private ArrayList<XYVarioAnnotation> userVAList = new ArrayList<XYVarioAnnotation>();
	private ArrayList<XYAnnotation> dlgAList = new ArrayList<XYAnnotation>();

	public AltimeterAnnotationManager(ChartPanel cp) {
		this.cp = cp;
	}

	// Hooks the annotation manager in to receive chart mouse events
	public void addMouseListener() {
		cp.addChartMouseListener(new AltimeterChartMouseListener(cp, this));
	}

	private XYHeightAnnotation addHeightAnnotationIntenal(double time, double heightInPlotUnits, Paint color) {
		XYHeightAnnotation annotation = new XYHeightAnnotation(
				String.format("%.1f", heightInPlotUnits), time, heightInPlotUnits, color);
		annotation.setTextAnchor(TextAnchor.BOTTOM_CENTER);
		cp.getChart().getXYPlot().addAnnotation(annotation);
		return annotation;
	}
	
	// these methods for adding annotations take parameters in the plot units, as would be
	// returned by the mouselistener. 
	public void addUserHeightAnnotation(double time, double heightInPlotUnits) {
		XYHeightAnnotation ann = addHeightAnnotationIntenal(time, heightInPlotUnits, Color.BLACK);
		userHAList.add(ann);
	}
	
	public void addDLGHeightAnnotation(double time, double heightInPlotUnits) {
		XYHeightAnnotation ann = addHeightAnnotationIntenal(time, heightInPlotUnits, Color.BLUE);
		dlgAList.add(ann);
	}
	
	public void addDLGMaxHeightAnnotation(double time, double heightInPlotUnits) {
		XYHeightAnnotation ann = addHeightAnnotationIntenal(time, heightInPlotUnits, Color.RED);
		dlgAList.add(ann);		
	}
	

	public void addDLGStartAnnotation(double time, double heightInPlotUnits) {
		XYDotAnnotation ann = new XYDotAnnotation(time, heightInPlotUnits, 4, Color.BLUE);
		cp.getChart().getXYPlot().addAnnotation(ann);
		dlgAList.add(ann);	
	}

	public void addUserVarioAnnotation(double startTime, double startHeightInPlotUnits, 
			double endTime, double endHeightInPlotsUnits) {
		double vario = (endHeightInPlotsUnits - startHeightInPlotUnits) / Math.abs(startTime - endTime);
		String varioText = String.format("%.2f", vario);
		String timeText = String.format("%.1f", Math.abs(startTime - endTime)) + "s";
		XYVarioAnnotation line = new XYVarioAnnotation(
				varioText, timeText, startTime, startHeightInPlotUnits, endTime, endHeightInPlotsUnits);
		cp.getChart().getXYPlot().addAnnotation(line);
		userVAList.add(line);
	}
	
	public void addEOFAnnotations(final List<Integer> eofIndices, final double timeStep)
	{
		XYPlot plot = cp.getChart().getXYPlot();
		for(int eofIndex : eofIndices) {
			XYAnnotation ann = new XYDotAnnotation(eofIndex * timeStep, 0.0, 4, EOF_COLOR);
			plot.addAnnotation(ann);
			eofAList.add(ann);
		}
	}

	// ** These "clear" methods are a little bit quirky.
	// We work around a bug in JFreeChart's remove annotation method
	// by removing all annotations and then adding back the ones that
	// we didn't want to clear.
	
	// this clears just the user added height and vario annotations
	private void restoreAllAnnotations() {
		XYPlot plot = cp.getChart().getXYPlot();
		for (XYAnnotation ann : userHAList) plot.addAnnotation(ann);
		for (XYAnnotation ann : userVAList) plot.addAnnotation(ann);
		for (XYAnnotation ann : dlgAList) plot.addAnnotation(ann);
		for (XYAnnotation ann : eofAList) plot.addAnnotation(ann);
	}
	
	public void clearHeightAndVarioAnnotations() {
		cp.getChart().getXYPlot().clearAnnotations();
		userHAList.clear();
		userVAList.clear();
		restoreAllAnnotations();
	}
	
	public void clearDLGAnnotations() {
		cp.getChart().getXYPlot().clearAnnotations();
		dlgAList.clear();
		restoreAllAnnotations();
	}
	
	// this clears everything but the EOF markers.
	public void clearAllAnnotations() {
		cp.getChart().getXYPlot().clearAnnotations();
		userHAList.clear();
		userVAList.clear();
		dlgAList.clear();
		restoreAllAnnotations();
	}

	// and this clears absolutely all annotations (including EOF).
	// this should only be called when changing the plot data.
	public void resetAnnotations() {
		cp.getChart().getXYPlot().clearAnnotations();
		userHAList.clear();
		userVAList.clear();
		dlgAList.clear();
		eofAList.clear();
	}

}
