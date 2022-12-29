enableFeaturePreview("TYPESAFE_PROJECT_ACCESSORS")
rootProject.name = "litiengine-sdk"

pluginManagement {
  repositories {
    gradlePluginPortal()
  }
}

dependencyResolutionManagement {
  repositories {
    mavenLocal()
    mavenCentral()
  }
}

include(
  "core",
  "utiliti",
  "shared"
)

for (p in rootProject.children) {
  if (p.children.isEmpty()) {
    if (p.name == "core") {
      p.name = "litiengine"
    } else {
      p.name = "litiengine-${p.name}"
    }
  }
}

gradle.projectsLoaded {
  rootProject.allprojects {
    buildDir = File("../build/${project.name}")
  }
}
