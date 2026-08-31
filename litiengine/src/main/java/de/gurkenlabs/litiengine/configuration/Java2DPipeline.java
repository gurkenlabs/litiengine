package de.gurkenlabs.litiengine.configuration;

/// Represents the Java2D rendering pipeline to use.
/// The pipeline is applied automatically during `Game.init()` via
/// [de.gurkenlabs.litiengine.graphics.RenderComponent#configurePipeline()],
/// which must run before any `Graphics2D` context is created.
///
/// @see GraphicConfiguration#setJava2DPipeline(Java2DPipeline)
public enum Java2DPipeline {
  /// Use the platform-default Java2D pipeline. On most systems this is a
  /// software rasterizer, but on some platforms it may select a hardware
  /// accelerated pipeline automatically.
  DEFAULT,

  /// Force the OpenGL rendering pipeline (`sun.java2d.opengl=true`).
  /// This typically gives a large performance boost on desktop systems with
  /// capable GPU drivers. Falls back to software if OpenGL is unavailable.
  OPENGL,

  /// Force the Direct3D rendering pipeline (`sun.java2d.d3d=true`).
  /// Only effective on Windows. Falls back to software on other platforms.
  DIRECT3D,

  /// Force software rendering by disabling the OpenGL and Direct3D pipelines.
  /// Use this if hardware acceleration causes rendering glitches.
  SOFTWARE
}
