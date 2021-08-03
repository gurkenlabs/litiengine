plugins {
    `java-library`
    application
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
