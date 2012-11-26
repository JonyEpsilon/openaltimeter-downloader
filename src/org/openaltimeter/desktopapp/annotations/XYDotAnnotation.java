/*
    openaltimeter -- an open-source altimeter for RC aircraft
    Copyright (C) 2010-2011  Jony Hudson
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
package org.openaltimeter.desktopapp.annotations;

import java.awt.BasicStroke;
import java.awt.Graphics2D;
import java.awt.Paint;
import java.awt.geom.Ellipse2D;
import java.awt.geom.Rectangle2D;

import org.jfree.chart.annotations.AbstractXYAnnotation;
import org.jfree.chart.axis.ValueAxis;
import org.jfree.chart.plot.Plot;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.chart.plot.PlotRenderingInfo;
import org.jfree.chart.plot.XYPlot;
import org.jfree.ui.RectangleEdge;

public class XYDotAnnotation extends AbstractXYAnnotation {
	
	private double x;
	private double y;
	private double size;
	private Paint color;
	
	public XYDotAnnotation(double x, double y, double size, Paint color) {
		super();
		this.x = x;
		this.y = y;
		this.size = size;
		this.color = color;
	}

	/* Based on XYTextAnnotation draw method */
	@Override
	public void draw(Graphics2D g2, XYPlot plot, Rectangle2D dataArea,
					 ValueAxis domainAxis, ValueAxis rangeAxis,
			         int rendererIndex, PlotRenderingInfo info) 
	{
		PlotOrientation orientation = plot.getOrientation();
		RectangleEdge domainEdge = Plot.resolveDomainAxisLocation(plot.getDomainAxisLocation(), orientation);
		RectangleEdge rangeEdge = Plot.resolveRangeAxisLocation(plot.getRangeAxisLocation(), orientation);
		
		float anchorX = (float) domainAxis.valueToJava2D(x, dataArea, domainEdge);
		float anchorY = (float) rangeAxis.valueToJava2D(y, dataArea, rangeEdge);
		
		if (orientation == PlotOrientation.HORIZONTAL) {
			float tempAnchor = anchorX;
			anchorX = anchorY;
			anchorY = tempAnchor;
        }
        
		// dot drawing
		g2.setPaint(color);
		g2.setStroke(new BasicStroke(1.0f));
		Ellipse2D e = new Ellipse2D.Double(anchorX - size/2, anchorY - size/2, size, size);
		g2.fill(e);
	}

}
