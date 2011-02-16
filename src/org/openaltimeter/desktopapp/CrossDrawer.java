package org.openaltimeter.desktopapp;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Paint;
import java.awt.geom.Line2D;
import java.awt.geom.Rectangle2D;

import org.jfree.ui.Drawable;

public class CrossDrawer implements Drawable {

	/** The line paint. */
    private Paint linePaint;

    public CrossDrawer()
    {
    	this.linePaint = Color.BLACK;
    }
    
    public CrossDrawer(Paint paint)
    {
    	this.linePaint = paint;
    }
    
	@Override
	public void draw(Graphics2D g2, Rectangle2D area) 
	{
        g2.setPaint(this.linePaint);
        g2.setStroke(new BasicStroke(1.0f));
        Line2D line1 = new Line2D.Double(area.getCenterX(), area.getMinY(),
                                         area.getCenterX(), area.getMaxY());
        Line2D line2 = new Line2D.Double(area.getMinX(), area.getCenterY(), 
                                         area.getMaxX(), area.getCenterY());
        g2.draw(line1);
        g2.draw(line2);	
	}

}
