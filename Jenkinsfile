pipeline {
    agent any

    environment {
        IMAGE_NAME = "subscription-spring-app"
        CONTAINER_NAME = "subscription-backend"
        IMAGE_TAG = "${BUILD_NUMBER}"
    }

    stages {

        stage('Checkout') {
            steps {
                checkout scm
            }
        }

        stage('Build Docker Image') {
            steps {
                sh "docker build -t $IMAGE_NAME:$IMAGE_TAG -t $IMAGE_NAME:latest -f Dockerfile ."
            }
        }

        stage('Stop old container') {
            steps {
                sh "docker stop $CONTAINER_NAME || true"
                sh "docker rm $CONTAINER_NAME || true"
            }
        }

        stage('Run container') {
            steps {
                sh """
                    ENV_ARGS=\$(grep -v '^\$' /opt/env/spring.env | grep -v '^#' | grep -v '^SPRING_PROFILES_ACTIVE' | sed 's/ *= */=/' | sed 's/^/-e /' | tr '\n' ' ')
                    docker run -d --name $CONTAINER_NAME \$ENV_ARGS -e SPRING_PROFILES_ACTIVE=dev -p 8081:8080 $IMAGE_NAME:$IMAGE_TAG
                """
            }
        }

        stage('Health check') {
            steps {
                script {
                    retry(5) {
                        sleep 10
                        sh "docker ps | grep $CONTAINER_NAME"
                        sh "curl -f http://localhost:8081/actuator/health || exit 1"
                    }
                }
            }
        }

        stage('Cleanup images') {
            steps {
                sh """
                    docker images $IMAGE_NAME --format '{{.Tag}}' | grep -v latest | grep -v $IMAGE_TAG | head -n -2 | xargs -r -I {} docker rmi $IMAGE_NAME:{} || true
                """
                sh "docker image prune -f"
            }
        }
    }

    post {
        failure {
            sh "docker logs $CONTAINER_NAME || true"
            sh "docker stop $CONTAINER_NAME || true"
            sh "docker rm $CONTAINER_NAME || true"
        }
        success {
            echo "Deployment successful! Container is running on port 8082"
        }
        always {
            sh "docker system df"
        }
    }
}