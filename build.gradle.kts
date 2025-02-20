import org.springframework.boot.gradle.tasks.bundling.BootJar

plugins {
    val kotlinVersion = "2.1.10"

    kotlin("jvm") version kotlinVersion
    kotlin("plugin.spring") version kotlinVersion
    kotlin("plugin.jpa") version kotlinVersion
    kotlin("kapt") version kotlinVersion

    id("org.springframework.boot") version "3.2.2"
    id("io.spring.dependency-management") version "1.1.7"
}

group = "coraythan"
version = "647"

kotlin {
    compilerOptions {
        freeCompilerArgs.addAll(
            "-Xjsr305=strict",
            "-opt-in=kotlin.ExperimentalStdlibApi"
        )
    }
}

java {
    toolchain {
        languageVersion = JavaLanguageVersion.of(21)
    }
}

tasks.getByName<BootJar>("bootJar") {
    from(".ebextensions") {
        into(".ebextensions")
    }
}

tasks.register<Copy>("dockerCopyJar") {
    from(File("build/libs/keyswap-$version.jar"))
    rename("keyswap-$version.jar", "keyswap.jar")
    into(File("./docker"))
    include("*.jar")
}

springBoot {
    mainClass.set("coraythan.keyswap.KeyswapApplicationKt")
}

repositories {
    mavenCentral()
}

tasks.register<JavaExec>("genTs") {
    mainClass.set("coraythan.keyswap.generatets.TsGenerator")
    classpath = sourceSets["main"].runtimeClasspath
}

tasks.register("genSrc") {
    dependsOn("genTs")
    doLast {
        File("$projectDir/src/main/resources", "application-generated.yml").writeText("api-version: $version")
        File("$projectDir/ui/src/config", "ClientVersion.ts").writeText("export const clientVersion = $version")
        File("$projectDir", "version.txt").writeText("$version")
        val dockerrunContents = File("$projectDir/docker", "Dockerrun.aws.json.template")
            .readText()
            .replace("\$version", "$version")
        File("$projectDir/docker", "Dockerrun.aws.json")
            .writeText(dockerrunContents)
    }
}

tasks.withType<Test> {
    useJUnitPlatform()
}

dependencies {

    val queryDslVersion = "5.0.0"
    val jjwtVersion = "0.12.5"
    val shedlockVersion = "5.8.0"
    val kotlinAwsSdkVersion = "1.4.24"
    val smithyKotlinVersion = "1.4.4"
    val flywayVersion = "10.22.0"

    implementation("org.springframework.boot:spring-boot-starter-data-jpa")
    implementation("org.springframework.boot:spring-boot-starter-security")
    implementation("org.springframework.boot:spring-boot-starter-web")
    implementation("org.springframework.boot:spring-boot-starter-mail")

    implementation("org.jetbrains.kotlin:kotlin-stdlib-jdk8")
    implementation("org.jetbrains.kotlin:kotlin-reflect")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.7.3")
    implementation("org.jetbrains.kotlinx:kotlinx-html-jvm:0.9.1")

    implementation("com.github.depsypher:pngtastic:1.7")

    implementation("com.fasterxml.jackson.module:jackson-module-kotlin")
    implementation("com.fasterxml.jackson.dataformat:jackson-dataformat-yaml")

    implementation("org.flywaydb:flyway-core:$flywayVersion")
    runtimeOnly("org.flywaydb:flyway-database-postgresql:$flywayVersion")

    implementation("net.javacrumbs.shedlock:shedlock-spring:$shedlockVersion")
    implementation("net.javacrumbs.shedlock:shedlock-provider-jdbc-template:$shedlockVersion")
    implementation("org.apache.commons:commons-lang3:3.13.0")
    implementation("org.logback-extensions:logback-ext-loggly:0.1.5")
    implementation("com.patreon:patreon:0.4.2")

    implementation("org.apache.httpcomponents.client5:httpclient5")

    implementation("aws.sdk.kotlin:s3-jvm:$kotlinAwsSdkVersion")
    implementation("aws.sdk.kotlin:s3control-jvm:$kotlinAwsSdkVersion")
    implementation("aws.sdk.kotlin:sts-jvm:$kotlinAwsSdkVersion")
    implementation("aws.sdk.kotlin:secretsmanager-jvm:$kotlinAwsSdkVersion")
    implementation("aws.smithy.kotlin:http-client-engine-crt-jvm:$smithyKotlinVersion")
    implementation("aws.smithy.kotlin:aws-signing-crt-jvm:$smithyKotlinVersion")
    implementation("aws.smithy.kotlin:http-auth-aws-jvm:$smithyKotlinVersion")

    implementation("io.jsonwebtoken:jjwt-api:$jjwtVersion")
    runtimeOnly("io.jsonwebtoken:jjwt-impl:$jjwtVersion")
    runtimeOnly("io.jsonwebtoken:jjwt-jackson:$jjwtVersion")

    implementation("io.hypersistence:hypersistence-utils-hibernate-62:3.7.1")
    implementation("com.querydsl:querydsl-core:$queryDslVersion")
    implementation("com.querydsl:querydsl-jpa:$queryDslVersion:jakarta")
    kapt("com.querydsl:querydsl-apt:$queryDslVersion:jakarta")

    runtimeOnly("org.postgresql:postgresql")
    testImplementation("org.springframework.boot:spring-boot-starter-test")
    testImplementation("org.springframework.security:spring-security-test")

    testImplementation("org.junit.vintage:junit-vintage-engine") {
        exclude(group = "org.hamcrest", module = "hamecrest-core")
    }
}
