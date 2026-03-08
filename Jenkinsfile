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

        stage('Build JAR') {
            steps {
                sh 'mvn -B clean package -DskipTests'
            }
        }

        stage('Build Docker Image') {
            steps {
                sh 'docker build -t $IMAGE_NAME:$IMAGE_TAG .'
            }
        }

        stage('Stop old container') {
            steps {
                sh 'docker stop $CONTAINER_NAME || true'
                sh 'docker rm $CONTAINER_NAME || true'
            }
        }

        stage('Run container') {
            steps {
                sh '''
                docker run -d \
                  --name $CONTAINER_NAME \
                  --env-file /opt/env/spring.env \
                  -e SPRING_PROFILES_ACTIVE=prod \
                  -p 8082:8080 \
                  subscription-spring-app:$IMAGE_TAG
                '''
            }
        }

        stage('Cleanup images') {
            steps {
                sh 'docker image prune -f'
            }
        }
    }
}