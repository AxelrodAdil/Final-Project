plugins {
    java
    id("org.springframework.boot") version "3.0.3"
    id("io.spring.dependency-management") version "1.1.0"
}

group = "kz.axelrod"
version = "1.0.0"
java.sourceCompatibility = JavaVersion.VERSION_17

repositories {
    mavenCentral()
}

dependencies {
    implementation("org.springframework.boot:spring-boot-starter")
    implementation("org.springframework.boot:spring-boot-starter-web")
    implementation("org.springframework.boot:spring-boot-starter-data-jpa")
    implementation("org.springframework:spring-context-support:5.2.22.RELEASE")
    implementation("org.springframework.boot:spring-boot-starter-validation:3.0.3")
    implementation("org.springframework.boot:spring-boot-configuration-processor:3.0.3")

//    https://habr.com/ru/post/683936/#comment_25009196
//    implementation("org.springframework.boot:spring-boot-starter-security")
//    implementation("org.springframework.security:spring-security-config:5.6.1")
//    implementation("de.mkammerer:argon2-jvm:2.11")
//    compileOnly("javax.servlet:servlet-api:2.5")

    implementation("org.springframework.data:spring-data-redis:3.0.1")
    implementation("redis.clients:jedis:4.3.1")

    implementation("org.springdoc:springdoc-openapi-starter-webmvc-ui:2.0.2")

    implementation("org.springframework.boot:spring-boot-starter-mail:3.0.3")
    implementation("javax.mail:mail:1.4.7")
    implementation("jakarta.validation:jakarta.validation-api:3.0.2")
    implementation("org.hibernate:hibernate-validator:8.0.0.Final")
    implementation("io.swagger:swagger-annotations:1.6.10")

    runtimeOnly("org.postgresql:postgresql")
    implementation("org.flywaydb:flyway-core")

    annotationProcessor("org.projectlombok:lombok:1.18.22")
    compileOnly("org.projectlombok:lombok:1.18.22")

    testImplementation("org.springframework.boot:spring-boot-starter-test")
}

tasks.withType<Test> {
    useJUnitPlatform()
}
