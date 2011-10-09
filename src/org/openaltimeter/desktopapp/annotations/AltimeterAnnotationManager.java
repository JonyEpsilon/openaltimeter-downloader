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

	private XYHeightAnnotation AddHeightAnnotationIntenal(double x, double y, Paint color) {
		XYHeightAnnotation annotation = new XYHeightAnnotation(String.format("%.1f", y), x, y, color);
		annotation.setTextAnchor(TextAnchor.BOTTOM_CENTER);
		cp.getChart().getXYPlot().addAnnotation(annotation);
		return annotation;
	}
	
	public void AddUserHeightAnnotation(double x, double y) {
		XYHeightAnnotation ann = AddHeightAnnotationIntenal(x, y, Color.BLACK);
		userHAList.add(ann);
	}
	
	public void AddDLGHeightAnnotation(double x, double y) {
		XYHeightAnnotation ann = AddHeightAnnotationIntenal(x, y, Color.BLUE);
		dlgHAList.add(ann);
	}

	public void AddUserVarioAnnotation(double startX, double startY, double endX, double endY) {
		double vario = (endY - startY) / Math.abs(startX - endX);
		XYVarioAnnotation line = new XYVarioAnnotation(String.format("%.2f", vario), startX, startY, endX, endY);
		cp.getChart().getXYPlot().addAnnotation(line);
		userVAList.add(line);
	}

	public void clearHeightAndVarioAnnotations() {
		for (XYHeightAnnotation ha : userHAList)
			cp.getChart().getXYPlot().removeAnnotation(ha);
		for (XYVarioAnnotation va : userVAList)
			cp.getChart().getXYPlot().removeAnnotation(va);
		userHAList.clear();
		userVAList.clear();
	}

	public void resetAnnotations() {
		cp.getChart().getXYPlot().clearAnnotations();
		userHAList.clear();
		userVAList.clear();
	}

}
