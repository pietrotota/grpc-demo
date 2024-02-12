import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
  kotlin("jvm") version "1.9.22"
  id("com.diffplug.spotless") version "6.18.0"
  id("com.google.protobuf") version "0.9.4"
  jacoco
  application
}

group = "it.pietrot"

version = "1.0-SNAPSHOT"

java.sourceCompatibility = JavaVersion.VERSION_17

tasks.withType<KotlinCompile> { kotlinOptions.jvmTarget = "17" }

repositories { mavenCentral() }

val grpcKotlinStubVersion = "1.4.1"
val grpcProtobufVersion = "1.61.1"
val grpcProtobufKotlinVersion = "3.25.2"

dependencies {
  implementation("io.grpc:grpc-kotlin-stub:$grpcKotlinStubVersion")
  implementation("io.grpc:grpc-protobuf:$grpcProtobufVersion")
  implementation("com.google.protobuf:protobuf-kotlin:$grpcProtobufKotlinVersion")
  implementation("io.grpc:grpc-netty:$grpcProtobufVersion")
  implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.7.3")
  implementation("org.slf4j:slf4j-api:2.0.11")
  runtimeOnly("ch.qos.logback:logback-classic:1.4.14")

  testImplementation(kotlin("test"))
}

tasks.test { useJUnitPlatform() }

application { mainClass.set("MainKt") }

sourceSets {
  main {
    kotlin { srcDirs("src/main/kotlin") }
    resources { srcDirs("src/resources") }
    proto { srcDirs("proto") }
  }
}

protobuf {
  protoc { artifact = "com.google.protobuf:protoc:$grpcProtobufKotlinVersion" }
  plugins {
    create("grpc") { artifact = "io.grpc:protoc-gen-grpc-java:$grpcProtobufVersion" }
    create("grpckt") { artifact = "io.grpc:protoc-gen-grpc-kotlin:$grpcKotlinStubVersion:jdk8@jar" }
  }
  generateProtoTasks {
    all().forEach {
      it.plugins {
        create("grpc")
        create("grpckt")
      }
      it.builtins { create("kotlin") }
    }
  }
}

// configure spotless plugin
configure<com.diffplug.gradle.spotless.SpotlessExtension> {
  kotlin {
    toggleOffOn()
    targetExclude("build/**/*")
    ktfmt().kotlinlangStyle()
  }
  kotlinGradle {
    toggleOffOn()
    targetExclude("build/**/*.kts")
    ktfmt().googleStyle()
  }
  java {
    target("**/*.java")
    targetExclude("build/**/*")
    eclipse().configFile("eclipse-style.xml")
    toggleOffOn()
    removeUnusedImports()
    trimTrailingWhitespace()
    endWithNewline()
  }
}
