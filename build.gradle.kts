plugins {
    kotlin("jvm") version "1.9.0" apply false
    id("com.lovelysystems.gradle") version ("1.12.0")
}

lovely {
    gitProject()
}

subprojects {
    version = rootProject.version
}
