import com.bmuschko.gradle.docker.tasks.image.DockerBuildImage
import com.bmuschko.gradle.docker.tasks.image.DockerPushImage

plugins {
	kotlin("jvm") version "1.9.25"
	kotlin("plugin.spring") version "1.9.25"
	id("org.springframework.boot") version "3.5.3"
	id("io.spring.dependency-management") version "1.1.7"
	id("com.bmuschko.docker-remote-api") version "9.4.0"
}

group = "net.noti-me.dymit-backend"
version = "0.1.1"
val kotestVersion = "5.9.1"
val springDocVersion = "2.8.9"

java {
	toolchain {
		languageVersion = JavaLanguageVersion.of(21)
	}
}

repositories {
	mavenCentral()
}

dependencies {
	implementation("io.netty:netty-resolver-dns-native-macos:4.1.100.Final:osx-aarch_64") // Apple Silicon(M1/M2)
	implementation("org.springframework.boot:spring-boot-starter-data-mongodb")
	implementation("org.springframework.boot:spring-boot-starter-security")
	implementation("org.springframework.boot:spring-boot-starter-web")
	implementation("org.springframework.boot:spring-boot-starter-webflux")
	implementation("org.springframework.boot:spring-boot-starter-validation")
	implementation("com.fasterxml.jackson.module:jackson-module-kotlin")
	implementation("org.jetbrains.kotlin:kotlin-reflect")
	implementation("org.springdoc:springdoc-openapi-starter-webmvc-ui:${springDocVersion}")
	implementation("org.springdoc:springdoc-openapi-starter-common:${springDocVersion}")
	implementation("com.fasterxml.jackson.module:jackson-module-kotlin")
	implementation("com.auth0:java-jwt:4.4.0")
    implementation("com.google.firebase:firebase-admin:9.3.0")

	testImplementation("io.mockk:mockk:1.13.4")
    testImplementation("io.kotest.extensions:kotest-extensions-spring:1.3.0")

    // testImplementation("io.kotest:kotest-extensions-htmlreporter:5.9.1")
//	 testImplementation("de.flapdoodle.embed:de.flapdoodle.embed.mongo:4.20.1")
	// implementation("de.flapdoodle.embed:de.flapdoodle.embed.mongo.spring30x:4.11.0")
	testImplementation("de.flapdoodle.embed:de.flapdoodle.embed.mongo.spring3x:4.20.0")
	testImplementation("io.kotest:kotest-extensions-junitxml:${kotestVersion}")
    testImplementation("io.kotest:kotest-assertions-core-jvm:${kotestVersion}")
	testImplementation("io.kotest:kotest-assertions-core:${kotestVersion}")
	testImplementation("io.kotest:kotest-runner-junit5-jvm:${kotestVersion}")
    testImplementation("org.springframework.boot:spring-boot-starter-test")
    testImplementation("org.jetbrains.kotlin:kotlin-test-junit5")
	testRuntimeOnly("org.junit.platform:junit-platform-launcher")
}

kotlin {
	compilerOptions {
		freeCompilerArgs.addAll("-Xjsr305=strict")
	}
	jvmToolchain(21)
}

tasks.withType<org.jetbrains.kotlin.gradle.tasks.KotlinCompile> {
    kotlinOptions {
        freeCompilerArgs = listOf("-Xjsr305=strict")
        jvmTarget = "21"
    }
}

tasks.withType<Test>().configureEach {
	useJUnitPlatform()
	reports {
		junitXml.required.set(false)
	}
  	systemProperty("gradle.build.dir", project.buildDir)
}

tasks.withType<org.springframework.boot.gradle.tasks.bundling.BootJar> {
	archiveBaseName.set("${project.group}")
	archiveVersion.set("latest")
}

tasks.register<DockerBuildImage>("buildDockerImage") {
	dependsOn("bootJar")
	inputDir.set(file(".")) // Dockerfile이 위치한 경로 (프로젝트 root 경로)
	images.add("elensar92/dymit-api:${version}")
	group = "docker"
}

tasks.register<DockerPushImage>("pushDockerImage") {
	dependsOn("buildDockerImage")
	images.add("elensar92/dymit-api:${version}")
	group = "docker"
}
