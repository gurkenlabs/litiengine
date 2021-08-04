plugins {
    `java-library`
    application
    id("edu.sc.seis.launch4j")
}

sourceSets {
    main {
        java {
            resources.srcDir("src/main/localization")
        }
    }
}

application {
    mainClass.set("de.gurkenlabs.utiliti.Program")
    applicationDistribution.into(executableDir) {
        from("dist")
    }
}

dependencies {
    implementation(project(":litiengine"))
    implementation(libs.darklaf.core)

    testImplementation(project(":test-common"))
}

tasks {
    test {
        useJUnitPlatform()
    }
}

launch4j {
    mainClassName = application.mainClass.get()
    headerType = "gui"
    icon = project.file("dist/pixel-icon-utiLITI.ico").path
    outfile = "${project.name}-${project.version}.exe"
    companyName = "gurkenlabs.de"
    version = project.version.toString()
    textVersion = project.version.toString()
    copyright = "2020 gurkenlabs.de"
    jvmOptions.addAll(listOf("-Xms256m", "-Xmx1024m"))
    jdkPreference = "preferJdk"
}
