package org.openaltimeter.desktopapp;

import java.awt.event.InputEvent;

import javax.swing.SwingUtilities;

import org.jfree.chart.ChartMouseEvent;
import org.jfree.chart.ChartMouseListener;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.plot.XYPlot;
import org.jfree.ui.TextAnchor;

public final class AltimeterChartMouseListener implements ChartMouseListener 
{
	private final ChartPanel cp;
	private double lastAnnotationX = 0d;
	private double lastAnnotationY = 0d;
	
	public AltimeterChartMouseListener(ChartPanel cp) {
		this.cp = cp;
	}

	@Override
	public void chartMouseMoved(ChartMouseEvent arg0) {
		return;
	}

	@Override
	public void chartMouseClicked(final ChartMouseEvent event) {
	    SwingUtilities.invokeLater(new Runnable() {
	       public void run()
	       {
	          XYPlot xyplot = (XYPlot) cp.getChart().getPlot();

	          int onmask = InputEvent.SHIFT_DOWN_MASK;
	          if ((event.getTrigger().getModifiersEx() & (onmask)) == onmask)
	          {
	        	  xyplot.clearAnnotations();
	        	  return;
	          }

	          double x, y;
	          x = xyplot.getDomainCrosshairValue();
	          y = xyplot.getRangeCrosshairValue();

	          XYHeightAnnotation annotation = new XYHeightAnnotation(String.format("%.1f", y), x, y);
	          annotation.setTextAnchor(TextAnchor.BOTTOM_CENTER);
	          xyplot.addAnnotation(annotation);
	                           	                  
	          onmask = InputEvent.CTRL_DOWN_MASK;
	          if ((event.getTrigger().getModifiersEx() & (onmask)) == onmask) 
	          {	        	  
	        	  double vario = (y - lastAnnotationY) / Math.abs(lastAnnotationX - x);
	        	
	        	  XYVarioAnnotation line = new XYVarioAnnotation(String.format("%.2f", vario), lastAnnotationX, lastAnnotationY, x, y);
	        	  xyplot.addAnnotation(line);
	          }
	          
	          lastAnnotationX = x;
	          lastAnnotationY = y;
	       }
	    });
	}
}