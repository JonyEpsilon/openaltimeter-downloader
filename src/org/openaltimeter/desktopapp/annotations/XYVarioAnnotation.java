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
import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.Paint;
import java.awt.Stroke;
import java.awt.geom.Rectangle2D;

import org.jfree.chart.annotations.XYLineAnnotation;
import org.jfree.chart.annotations.XYTextAnnotation;
import org.jfree.chart.axis.ValueAxis;
import org.jfree.chart.plot.Plot;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.chart.plot.PlotRenderingInfo;
import org.jfree.chart.plot.XYPlot;
import org.jfree.text.TextUtilities;
import org.jfree.ui.RectangleEdge;
import org.jfree.ui.TextAnchor;

public class XYVarioAnnotation extends XYLineAnnotation {

	/**
	 * 
	 */
	private static final long serialVersionUID = -6423311354784455761L;
	private static final int OFFSET_SIZE = 10;
	private int offset = OFFSET_SIZE;
	private double x1;
	private double x2;
	private double y1;
	private double y2;
	
	private Font font = XYTextAnnotation.DEFAULT_FONT;
	private String text;
	private TextAnchor textAnchor;
	private TextAnchor rotationAnchor;
	private double rotationAngle;
	private Paint paint;

	public Font getFont() {
		return font;
	}

	public void setFont(Font font) {
		this.font = font;
	}

	public void setText(String text) {
		this.text = text;
	}

	public TextAnchor getTextAnchor() {
		return textAnchor;
	}

	public void setTextAnchor(TextAnchor textAnchor) {
		this.textAnchor = textAnchor;
	}

	public TextAnchor getRotationAnchor() {
		return rotationAnchor;
	}

	public void setRotationAnchor(TextAnchor rotationAnchor) {
		this.rotationAnchor = rotationAnchor;
	}

	public double getRotationAngle() {
		return rotationAngle;
	}

	public void setRotationAngle(double rotationAngle) {
		this.rotationAngle = rotationAngle;
	}

	public void setPaint(Paint paint) {
		this.paint = paint;
	}

	public Paint getPaint() {
		return paint;
	}

	public String getText() {
		return text;
	}

	public XYVarioAnnotation(String text, double x1, double y1, double x2, double y2) 
	{
		this(text, x1, y1, x2, y2, new BasicStroke(1.0f), Color.black);
	}
		
	public XYVarioAnnotation(String text, double x1, double y1, double x2, double y2,
				 			Stroke stroke, Paint paint) 
	{
		super(x1, y1, x2, y2, stroke, paint);
		this.x1 = x1;
		this.x2 = x2;
		this.y1 = y1;
		this.y2 = y2; 

		if (y1 < y2) 
			this.offset = -OFFSET_SIZE;
		
		this.setTextAnchor(TextAnchor.CENTER);
		
		this.setPaint(paint);
		this.setText(text);
	}

	/* Based on XYTextAnnotation draw method */
	@Override
	public void draw(Graphics2D g2, XYPlot plot, Rectangle2D dataArea,
			                         ValueAxis domainAxis, ValueAxis rangeAxis,
			                         int rendererIndex, PlotRenderingInfo info) 
	{
		// draw line
		super.draw(g2, plot, dataArea, domainAxis, rangeAxis, rendererIndex, info);
		
		// draw text
		PlotOrientation orientation = plot.getOrientation();
		RectangleEdge domainEdge = Plot.resolveDomainAxisLocation(plot.getDomainAxisLocation(), orientation);
		RectangleEdge rangeEdge = Plot.resolveRangeAxisLocation(plot.getRangeAxisLocation(), orientation);
		
		float anchorX = (float) domainAxis.valueToJava2D((x1 + x2) / 2, dataArea, domainEdge);
		float anchorY = (float) rangeAxis.valueToJava2D((y1 + y2) / 2, dataArea, rangeEdge);
		
		if (orientation == PlotOrientation.HORIZONTAL) {
			float tempAnchor = anchorX;
			anchorX = anchorY;
			anchorY = tempAnchor;
        }
    
        g2.setFont(getFont());
		g2.setPaint(getPaint());
		TextUtilities.drawRotatedString(getText(), g2, anchorX + this.offset, anchorY - OFFSET_SIZE,
										getTextAnchor(), getRotationAngle(), getRotationAnchor());
	}

}
