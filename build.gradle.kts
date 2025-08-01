plugins {
	application
	id("org.springframework.boot") version "3.5.4"
	id("io.spring.dependency-management") version "1.1.7"
	checkstyle
	jacoco
	id("org.sonarqube") version "6.0.1.5171"
	id("io.freefair.lombok") version "8.14"
}

group = "hexlet.code"
version = "0.0.1-SNAPSHOT"

application {
    mainClass = "hexlet.code.app.AppApplication"
}

java {
	toolchain {
		languageVersion = JavaLanguageVersion.of(21)
	}
}

sonar {
    properties {
        property("sonar.projectKey", "rom-kavyrshin_java-project-99")
        property("sonar.organization", "rom-kavyrshin")
        property("sonar.host.url", "https://sonarcloud.io")
    }
}

configurations {
	compileOnly {
		extendsFrom(configurations.annotationProcessor.get())
	}
}

repositories {
	mavenCentral()
}

dependencies {
	implementation("org.springframework.boot:spring-boot-starter-web")
	compileOnly("org.projectlombok:lombok")
	developmentOnly("org.springframework.boot:spring-boot-devtools")
	annotationProcessor("org.projectlombok:lombok")
	testImplementation("org.springframework.boot:spring-boot-starter-test")
	testRuntimeOnly("org.junit.platform:junit-platform-launcher")
}

tasks.withType<Test> {
	useJUnitPlatform()
}

tasks.jacocoTestReport {
    reports {
        xml.required = true
    }
}
