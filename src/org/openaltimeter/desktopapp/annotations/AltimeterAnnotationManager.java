package org.openaltimeter.desktopapp.annotations;

import java.awt.Color;
import java.awt.Paint;
import java.util.ArrayList;

import org.jfree.chart.ChartPanel;
import org.jfree.ui.TextAnchor;

public class AltimeterAnnotationManager {

	private ChartPanel cp;
	private ArrayList<XYHeightAnnotation> userHAList = new ArrayList<XYHeightAnnotation>();
	private ArrayList<XYVarioAnnotation> userVAList = new ArrayList<XYVarioAnnotation>();
	private ArrayList<XYHeightAnnotation> dlgHAList = new ArrayList<XYHeightAnnotation>();

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
		dlgHAList.add(ann);
	}

	public void addUserVarioAnnotation(double startTime, double startHeightInPlotUnits, 
			double endTime, double endHeightInPlotsUnits) {
		double vario = (endHeightInPlotsUnits - startHeightInPlotUnits) / Math.abs(startTime - endTime);
		XYVarioAnnotation line = new XYVarioAnnotation(
				String.format("%.2f", vario), startTime, startHeightInPlotUnits, endTime, endHeightInPlotsUnits);
		cp.getChart().getXYPlot().addAnnotation(line);
		userVAList.add(line);
	}

	// this clears just the user added height and vario annotations
	public void clearHeightAndVarioAnnotations() {
		for (XYHeightAnnotation ha : userHAList)
			cp.getChart().getXYPlot().removeAnnotation(ha);
		for (XYVarioAnnotation va : userVAList)
			cp.getChart().getXYPlot().removeAnnotation(va);
		userHAList.clear();
		userVAList.clear();
	}
	
	// this clears everything but the EOF markers.
	public void clearAllAnnotations() {
		clearHeightAndVarioAnnotations();		
	}

	// and this clears absolutely all annotations (including EOF).
	// this should only be called when changing the plot data.
	public void resetAnnotations() {
		cp.getChart().getXYPlot().clearAnnotations();
		userHAList.clear();
		userVAList.clear();
	}

}
