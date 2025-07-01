plugins {
	kotlin("jvm") version "1.9.25"
	kotlin("plugin.spring") version "1.9.25"
	id("org.springframework.boot") version "3.5.3"
	id("io.spring.dependency-management") version "1.1.7"
}

group = "net.noti-me.dymit"
version = "0.0.1-SNAPSHOT"
val kotestVersion = "5.9.1"

java {
	toolchain {
		languageVersion = JavaLanguageVersion.of(21)
	}
}

repositories {
	mavenCentral()
}

dependencies {
	implementation("org.springframework.boot:spring-boot-starter-data-mongodb")
	implementation("org.springframework.boot:spring-boot-starter-security")
	implementation("org.springframework.boot:spring-boot-starter-web")
	implementation("org.springframework.boot:spring-boot-starter-webflux")
	implementation("com.fasterxml.jackson.module:jackson-module-kotlin")
	implementation("org.jetbrains.kotlin:kotlin-reflect")

	
	testImplementation("io.mockk:mockk:1.13.4")
    testImplementation("io.kotest.extensions:kotest-extensions-spring:1.3.0")
	// testImplementation("io.kotest:kotest-extensions-htmlreporter:5.9.1")
	testImplementation("io.kotest:kotest-extensions-junitxml:${kotestVersion}")
    testImplementation("io.kotest:kotest-assertions-core-jvm:${kotestVersion}")
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
