dependencies {
    implementation(project(":bank-domain"))
    implementation("org.springframework.boot:spring-boot-starter")
    implementation("org.springframework.boot:spring-boot-starter-data-jpa")
    implementation("org.springframework.boot:spring-tx")
    implementation("org.jetbrains.kotlin:kotlin-reflect")
    implementation("org.redisson:redisson-spring-boot-starter:3.24.3")
}