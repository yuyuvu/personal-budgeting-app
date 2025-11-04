import com.diffplug.gradle.spotless.SpotlessTask

plugins {
    id("java")
    id("com.diffplug.spotless") version "8.0.0"
    checkstyle
}

group = "com.github.yuyuvu"
version = "1.0"

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

// указываем, что везде нужна кодировка UTF-8; без этого кириллица отображается в консоли неправильно JavaExec
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
    archiveBaseName.set("personal_budgeting_app")
    manifest {
        attributes["Main-Class"] = "com.github.yuyuvu.personalbudgetingapp.Main"
    }
    val dependencies = configurations.runtimeClasspath.get().map(::zipTree)
    from(dependencies)
    duplicatesStrategy = DuplicatesStrategy.EXCLUDE
}

// Настройки checkstyle и spotless
// Checkstyle осуществляет дополнительные проверки (например импортов и документации), поэтому добавлен

spotless {
    java {
        googleJavaFormat("1.31.0")
    }
}

checkstyle {
    toolVersion = "12.1.1"
    configFile = rootProject.file("config/checkstyle/google_checks.xml")
    // запрещаем сборку с предупреждениями
    maxErrors = 0
    maxWarnings = 0
}

// Указываем явный порядок сборки. Получаем итоговый jar после всех проверок.

tasks.check {
    dependsOn(tasks.spotlessApply)
    dependsOn(tasks.checkstyleMain)
    dependsOn(tasks.checkstyleTest)
}

tasks.test {
    dependsOn(tasks.spotlessApply)
}

tasks.assemble {
    dependsOn(tasks.check )
}

tasks.jar {
    dependsOn(tasks.check )
}

// Явно указываем Gradle выполнять тесты, проверки стиля и исправлять его, а также собирать jar при каждой сборке

tasks.withType<Jar> {
    outputs.upToDateWhen { false }
}

tasks.withType<Test> {
    outputs.upToDateWhen { false }
}

tasks.withType<SpotlessTask> {
    outputs.upToDateWhen { false }
}

tasks.withType<Checkstyle> {
    outputs.upToDateWhen { false }
}