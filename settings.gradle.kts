pluginManagement {
    plugins {
        fun String.v() = extra["$this.version"].toString()
        fun idv(id: String, key: String = id) = id(id) version key.v()
        idv("com.stehno.natives")
        idv("org.sonarqube")
        idv("com.diffplug.spotless")
        idv("edu.sc.seis.launch4j")
        idv("com.github.vlsi.crlf", "com.github.vlsi.vlsi-release-plugins")
        idv("com.github.vlsi.gradle-extensions", "com.github.vlsi.vlsi-release-plugins")
        idv("com.github.vlsi.license-gather", "com.github.vlsi.vlsi-release-plugins")
    }
}

include(
    "litiengine",
    "utiliti",
    "test-common"
)
