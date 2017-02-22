package de.gurkenlabs.litiengine.graphics;

public interface ICameraProvider {
  public ICamera getCamera();

  /**
   * Sets the camera.
   *
   * @param camera
   *          the new camera
   */
  public void setCamera(ICamera camera);

}
