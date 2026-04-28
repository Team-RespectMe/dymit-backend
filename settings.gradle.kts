rootProject.name = "dymit-backend-api"

include("dymit-backend-api")
project(":dymit-backend-api").projectDir = file("dymit-backend-api")

include("micro-apps:study-crawler")
project(":micro-apps:study-crawler").projectDir = file("micro-apps/study-crawler")
