package de.gurkenlabs.litiengine.graphics.interpolation.awt;

import java.awt.RenderingHints;
import java.awt.geom.AffineTransform;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.awt.image.AffineTransformOp;
import java.awt.image.BufferedImage;
import java.awt.image.ColorModel;
import java.awt.image.Raster;
import java.awt.image.WritableRaster;

import de.gurkenlabs.litiengine.graphics.interpolation.AffineTransformOperation;

public class AWTTransformOperation extends AffineTransformOperation<AWTInterpolation> {

  private final AffineTransformOp parent;

  AWTTransformOperation(AffineTransform tx, AWTInterpolation interpolation) {
	super(tx, interpolation);
    this.parent = new AffineTransformOp(tx, interpolation.getType());
  }

  @Override
  public BufferedImage filter(BufferedImage src, BufferedImage dest) {
    return parent.filter(src, dest);
  }

  @Override
  public Rectangle2D getBounds2D(BufferedImage src) {
    return parent.getBounds2D(src);
  }

  @Override
  public BufferedImage createCompatibleDestImage(BufferedImage src, ColorModel destCM) {
    return parent.createCompatibleDestImage(src, destCM);
  }

  @Override
  public Point2D getPoint2D(Point2D srcPt, Point2D dstPt) {
    return parent.getPoint2D(srcPt, dstPt);
  }

  @Override
  public RenderingHints getRenderingHints() {
    return parent.getRenderingHints();
  }

  @Override
  public WritableRaster filter(Raster src, WritableRaster dest) {
    return parent.filter(src, dest);
  }

  @Override
  public Rectangle2D getBounds2D(Raster src) {
    return parent.getBounds2D(src);
  }

  @Override
  public WritableRaster createCompatibleDestRaster(Raster src) {
    return parent.createCompatibleDestRaster(src);
  }

}
