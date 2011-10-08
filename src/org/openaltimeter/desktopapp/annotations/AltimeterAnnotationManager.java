package org.openaltimeter.desktopapp.annotations;

import java.util.ArrayList;

import org.jfree.chart.ChartPanel;

public class AltimeterAnnotationManager {

	private ChartPanel cp;
	private ArrayList<XYHeightAnnotation> haList = new ArrayList<XYHeightAnnotation>();
	private ArrayList<XYVarioAnnotation> vaList = new ArrayList<XYVarioAnnotation>();

	public AltimeterAnnotationManager(ChartPanel cp) {
		this.cp = cp;
	}

	// Hooks the annotation manager in to receive chart mouse events
	public void addMouseListener() {
		cp.addChartMouseListener(new AltimeterChartMouseListener(cp, this));
	}

	public void heightAnnotationAddedCallback(XYHeightAnnotation ha) {
		haList.add(ha);
	}

	public void varioAnnotationAddedCallback(XYVarioAnnotation va) {
		vaList.add(va);
	}

	public void clearHeightAndVarioAnnotations() {
		for (XYHeightAnnotation ha : haList)
			cp.getChart().getXYPlot().removeAnnotation(ha);
		for (XYVarioAnnotation va : vaList)
			cp.getChart().getXYPlot().removeAnnotation(va);
		haList.clear();
		vaList.clear();
	}
	
	public void resetAnnotations() {
		cp.getChart().getXYPlot().clearAnnotations();
		haList.clear();
		vaList.clear();	
	}

}
