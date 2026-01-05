dependencies {
    implementation(project(":bank-domain"))
    implementation(project(":bank-monitoring"))
    implementation(project(":bank-core"))

    implementation("org.springframework.boot:spring-boot-starter")
    implementation("org.springframework.boot:spring-boot-starter-data-jpa")
    implementation("ch.qos.logback:logback-classic:1.5.13")
    implementation("org.springframework.retry:spring-retry")
}