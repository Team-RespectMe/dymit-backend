import com.bmuschko.gradle.docker.tasks.image.DockerBuildImage
import com.bmuschko.gradle.docker.tasks.image.DockerPushImage

plugins {
    base
    id("com.bmuschko.docker-remote-api") version "9.4.0"
}

group = "net.noti-me.dymit-backend"
version = "0.1.0"

val dockerImageName = "elensar92/study-crawler:${project.version}"

tasks.register<DockerBuildImage>("buildDockerImage") {
    inputDir.set(project.projectDir)
    images.add(dockerImageName)
    group = "docker"
    description = "Build study-crawler Docker image"
}

tasks.register<DockerPushImage>("pushDockerImage") {
    dependsOn("buildDockerImage")
    images.add(dockerImageName)
    group = "docker"
    description = "Push study-crawler Docker image"
}
