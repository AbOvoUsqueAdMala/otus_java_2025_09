plugins {
    id("com.google.protobuf")
}

val grpcVersion = "1.72.0"
val protobufVersion = "4.30.2"
val javaAnnotationVersion = "1.3.2"

dependencies {
    implementation("ch.qos.logback:logback-classic")
    implementation("io.grpc:grpc-protobuf:$grpcVersion")
    implementation("io.grpc:grpc-stub:$grpcVersion")
    runtimeOnly("io.grpc:grpc-netty-shaded:$grpcVersion")
    compileOnly("javax.annotation:javax.annotation-api:$javaAnnotationVersion")

    testImplementation("org.junit.jupiter:junit-jupiter-api")
    testImplementation("org.junit.jupiter:junit-jupiter-engine")
    testImplementation("org.assertj:assertj-core")
}

protobuf {
    protoc {
        artifact = "com.google.protobuf:protoc:$protobufVersion"
    }
    plugins {
        create("grpc") {
            artifact = "io.grpc:protoc-gen-grpc-java:$grpcVersion"
        }
    }
    generateProtoTasks {
        ofSourceSet("main").forEach { task ->
            task.plugins {
                create("grpc")
            }
        }
    }
}

tasks.register<JavaExec>("numbersServer") {
    group = "application"
    description = "Запускает сервер (запускать первым)"
    classpath = sourceSets["main"].runtimeClasspath
    mainClass.set("ru.otus.numbers.server.NumbersServer")
}

tasks.register<JavaExec>("numbersClient") {
    group = "application"
    description = "Запускает клиент"
    classpath = sourceSets["main"].runtimeClasspath
    mainClass.set("ru.otus.numbers.client.NumbersClient")
}
