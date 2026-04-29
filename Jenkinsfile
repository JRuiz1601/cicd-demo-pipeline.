pipeline {
    agent any

    environment {
        APP_NAME = 'mi-app'
        IMAGE_NAME = 'mi-app:latest'
        CONTAINER_NAME = 'mi-app'
        SONARQUBE_ENV = 'SonarQube'
        MAVEN_IMAGE = 'maven:3.8.8-eclipse-temurin-11'
        DOCKER_NETWORK = 'jenkins_default'
        JENKINS_CONTAINER = 'jenkins-local'
    }

    options {
        timestamps()
        disableConcurrentBuilds()
    }

    stages {
        stage('Checkout') {
            steps {
                checkout scm
            }
        }

        stage('Build & Test') {
            steps {
                sh '''
                    docker run --rm \
                      --volumes-from ${JENKINS_CONTAINER} \
                      -v maven_cache:/root/.m2 \
                      -w "$PWD" \
                      ${MAVEN_IMAGE} mvn clean verify -DexcludedGroups=au.com.equifax.cicddemo.domain.SystemTest
                '''
            }
            post {
                always {
                    junit allowEmptyResults: true, testResults: 'target/surefire-reports/*.xml,target/failsafe-reports/*.xml'
                    archiveArtifacts allowEmptyArchive: true, artifacts: 'target/*.jar'
                }
            }
        }

        stage('Static Analysis (SonarQube)') {
            steps {
                withSonarQubeEnv("${SONARQUBE_ENV}") {
                    sh '''
                        docker run --rm \
                          --network ${DOCKER_NETWORK} \
                          -e SONAR_HOST_URL \
                          -e SONAR_AUTH_TOKEN \
                          --volumes-from ${JENKINS_CONTAINER} \
                          -v maven_cache:/root/.m2 \
                          -w "$PWD" \
                          ${MAVEN_IMAGE} mvn sonar:sonar \
                          -Dsonar.projectKey=mi-app \
                          -Dsonar.projectName=mi-app \
                          -Dsonar.host.url=${SONAR_HOST_URL} \
                          -Dsonar.token=${SONAR_AUTH_TOKEN}
                    '''
                }
            }
        }

        stage('Quality Gate (SonarQube)') {
            steps {
                timeout(time: 3, unit: 'MINUTES') {
                    waitForQualityGate abortPipeline: true
                }
            }
        }

        stage('Docker Build') {
            steps {
                sh 'docker build -t ${IMAGE_NAME} .'
            }
        }

        stage('Container Security Scan (Trivy)') {
            steps {
                sh '''
                    docker run --rm \
                      -v /var/run/docker.sock:/var/run/docker.sock \
                      aquasec/trivy:latest image \
                      --severity CRITICAL \
                      --exit-code 1 \
                      --ignore-unfixed \
                      ${IMAGE_NAME}
                '''
            }
        }

        stage('Deploy') {
            when {
                anyOf {
                    branch 'main'
                    branch 'master'
                }
            }
            steps {
                sh '''
                    docker rm -f ${CONTAINER_NAME} || true
                    docker run -d --name ${CONTAINER_NAME} -p 80:80 ${IMAGE_NAME}
                '''
            }
        }
    }

    post {
        success {
            echo 'Pipeline ejecutado correctamente. Aplicacion disponible en http://localhost/'
        }
        failure {
            echo 'Pipeline fallido. Revisar logs de Jenkins, SonarQube o Trivy.'
            sh 'docker rm -f ${CONTAINER_NAME} || true'
        }
        always {
            echo 'Limpiando entorno...'
            sh 'docker container prune -f || true'
            sh 'docker image prune -f || true'
            cleanWs()
        }
    }
}
