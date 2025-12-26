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
    annotationProcessor("org.mapstruct:mapstruct-processor:1.6.3")
	compileOnly("org.projectlombok:lombok")
	developmentOnly("org.springframework.boot:spring-boot-devtools:3.5.6")
	runtimeOnly("com.h2database:h2:2.3.232")

	implementation("org.springframework.boot:spring-boot-starter:3.5.6")
	implementation("org.springframework.boot:spring-boot-starter-web:3.5.6")
	implementation("org.springframework.boot:spring-boot-starter-validation:3.5.6")
	implementation("org.springframework.boot:spring-boot-starter-data-jpa:3.5.6")
	implementation("org.postgresql:postgresql:42.7.7")
	implementation("org.mapstruct:mapstruct:1.6.3")
	implementation("org.openapitools:jackson-databind-nullable:0.2.6")
    implementation("org.springframework.boot:spring-boot-starter-security:3.5.6")
    implementation("org.springframework.boot:spring-boot-starter-oauth2-resource-server:3.5.6")
    implementation("org.springdoc:springdoc-openapi-starter-webmvc-ui:2.8.14")
	implementation("io.sentry:sentry-spring-boot-starter-jakarta:8.29.0")

    testRuntimeOnly("org.junit.platform:junit-platform-launcher")
	testImplementation("org.springframework.boot:spring-boot-starter-test:3.5.6")
	testImplementation("org.springframework:spring-test:6.2.9")
	testImplementation("org.junit.jupiter:junit-jupiter:5.10.2")
	testImplementation("net.javacrumbs.json-unit:json-unit-assertj:4.1.1")
	testImplementation("org.hamcrest:hamcrest:3.0")
	testImplementation("org.springframework.security:spring-security-test:6.5.2")
	testImplementation("net.datafaker:datafaker:2.4.4")
	testImplementation("org.instancio:instancio-junit:5.5.1")
}

tasks.withType<Test> {
	useJUnitPlatform()
}

tasks.test {
    testLogging {
        events("passed", "skipped", "failed")
//        showStandardStreams = true
    }
}

tasks.jacocoTestReport {
    reports {
        xml.required = true
    }
}
