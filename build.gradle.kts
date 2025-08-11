plugins {
	application
	id("org.springframework.boot") version "3.5.4"
	id("io.spring.dependency-management") version "1.1.7"
	checkstyle
	jacoco
	id("org.sonarqube") version "6.2.0.5505"
	id("io.freefair.lombok") version "8.14"
}

group = "hexlet.code"
version = "0.0.1-SNAPSHOT"

application {
    mainClass = "hexlet.code.app.AppApplication"
}

java {
	sourceCompatibility = JavaVersion.VERSION_21
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
	annotationProcessor("org.projectlombok:lombok")
	compileOnly("org.projectlombok:lombok")
	developmentOnly("org.springframework.boot:spring-boot-devtools")
	runtimeOnly("com.h2database:h2:2.3.232")

	implementation("org.springframework.boot:spring-boot-starter")
	implementation("org.springframework.boot:spring-boot-starter-web")
	implementation("org.springframework.boot:spring-boot-starter-data-jpa")
	implementation("org.postgresql:postgresql:42.7.7")

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
