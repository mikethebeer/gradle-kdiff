plugins {
    id("com.lovelysystems.gradle") version ("1.12.0")
}

lovely {
    gitProject()
}

subprojects {
    version = rootProject.version
}
