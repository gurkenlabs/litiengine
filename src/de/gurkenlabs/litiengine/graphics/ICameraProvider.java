package de.gurkenlabs.litiengine.graphics;

public interface ICameraProvider {
  /**
   * Sets the camera.
   *
   * @param camera
   *          the new camera
   */
  public void setCamera(ICamera camera);

  public ICamera getCamera();

}
