/*
    openaltimeter -- an open-source altimeter for RC aircraft
    Copyright (C) 2010-2011  Jan Steidl, Jony Hudson
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
import java.awt.Shape;
import java.awt.geom.Ellipse2D;
import java.awt.geom.Rectangle2D;

import org.jfree.chart.annotations.XYTextAnnotation;
import org.jfree.chart.axis.ValueAxis;
import org.jfree.chart.plot.Plot;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.chart.plot.PlotRenderingInfo;
import org.jfree.chart.plot.XYPlot;
import org.jfree.text.TextUtilities;
import org.jfree.ui.RectangleEdge;

@SuppressWarnings("serial")
public class XYHeightAnnotation extends XYTextAnnotation {

	private static int DOT_SIZE = 4;
	
	public XYHeightAnnotation(String text, double x, double y, Paint color) {
		super(text, x, y);
		setPaint(color);
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
		
		float anchorX = (float) domainAxis.valueToJava2D(this.getX(), dataArea, domainEdge);
		float anchorY = (float) rangeAxis.valueToJava2D(this.getY(), dataArea, rangeEdge);
		
		if (orientation == PlotOrientation.HORIZONTAL) {
			float tempAnchor = anchorX;
			anchorX = anchorY;
			anchorY = tempAnchor;
        }
    
        g2.setFont(getFont());
        Shape hotspot = TextUtilities.calculateRotatedStringBounds(getText(), g2, anchorX, anchorY, getTextAnchor(),
                    											   getRotationAngle(), getRotationAnchor());
        if (this.getBackgroundPaint() != null) 
        {
        	g2.setPaint(this.getBackgroundPaint());
			g2.fill(hotspot);
		}
        
		g2.setPaint(getPaint());
		TextUtilities.drawRotatedString(getText(), g2, anchorX + (3.6f * DOT_SIZE), anchorY + (0.2f * DOT_SIZE),
										getTextAnchor(), getRotationAngle(), getRotationAnchor());
		if (this.isOutlineVisible()) 
		{
			g2.setStroke(this.getOutlineStroke());
			g2.setPaint(this.getOutlinePaint());
			g2.draw(hotspot);
		}
		
		// cross drawing
//		g2.setPaint(getPaint());
//		g2.setStroke(new BasicStroke(1.0f));
//		g2.drawLine((int) anchorX - CROSS_SIZE / 2, (int) anchorY, (int) anchorX + CROSS_SIZE / 2, (int) anchorY);
//		g2.drawLine((int) anchorX, (int) anchorY - CROSS_SIZE / 2, (int) anchorX, (int) anchorY + CROSS_SIZE / 2);
		
		// dot drawing
		g2.setPaint(getPaint());
		g2.setStroke(new BasicStroke(1.0f));
		Ellipse2D e = new Ellipse2D.Double(anchorX - DOT_SIZE/2, anchorY - DOT_SIZE/2, DOT_SIZE, DOT_SIZE);
		g2.fill(e);
		
		String toolTip = getToolTipText();
		String url = getURL();
		if (toolTip != null || url != null) {
			addEntity(info, hotspot, rendererIndex, toolTip, url);
		}
	}

}
