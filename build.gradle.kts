plugins {
    id("java")
    id("com.diffplug.spotless") version "8.0.0"
}

group = "com.github.yuyuvu"
version = "1.0-SNAPSHOT"

//  указываем, что версия файлов скомпилированных классов должна быть совместима со всеми JRE, начиная с JRE 17
java {
    sourceCompatibility = JavaVersion.VERSION_17
    targetCompatibility = JavaVersion.VERSION_17
}

repositories {
    mavenCentral()
}

// добавляем Jackson для парсинга JSON
dependencies {
    // https://mvnrepository.com/artifact/tools.jackson.core/jackson-databind
    implementation("tools.jackson.core:jackson-databind:3.0.0")
    testImplementation(platform("org.junit:junit-bom:5.10.0"))
    testImplementation("org.junit.jupiter:junit-jupiter")
    testRuntimeOnly("org.junit.platform:junit-platform-launcher")
}

tasks.test {
    useJUnitPlatform()
}

// указываем, что везде нужна кодировка UTF-8; без этого кириллица отображается в консоли неправильно
tasks.withType<JavaCompile> {
    options.encoding = "UTF-8"
}

// указываем, что везде нужна кодировка UTF-8; без этого кириллица отображается в консоли неправильно
tasks.withType<JavaExec> {
    systemProperty("file.encoding", "UTF-8")
    systemProperty("sun.jnu.encoding", "UTF-8")
    jvmArgs = listOf(
        "-Dfile.encoding=UTF-8",
        "-Dsun.stdout.encoding=UTF-8",
        "-Dsun.stderr.encoding=UTF-8"
    )
}

// указываем основной класс с main для MANIFEST.MF, а также то, что нам нужен far-jar со всеми зависимостями
tasks.jar {
    manifest {
        attributes["Main-Class"] = "com.github.yuyuvu.personalbudgetingapp.Main"
    }
    val dependencies = configurations.runtimeClasspath.get().map(::zipTree)
    from(dependencies)
    duplicatesStrategy = DuplicatesStrategy.EXCLUDE
}


spotless {
    java {
        // apply a specific flavor of google-java-format
        //removeUnusedImports()
        //formatAnnotations()
        googleJavaFormat("1.31.0")
    }
}