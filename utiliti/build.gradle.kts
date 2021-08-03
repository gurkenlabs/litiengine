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

    implementation("com.github.weisj:darklaf-core:2.7.2")

    testImplementation(project(":test-common"))
    testImplementation("org.junit.jupiter:junit-jupiter-api:5.6.0")
    testImplementation("org.junit.jupiter:junit-jupiter-params:5.6.0'")
    testRuntimeOnly("org.junit.jupiter:junit-jupiter-engine:5.6.0")
    testImplementation("org.mockito:mockito-core:3.3.3")
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
