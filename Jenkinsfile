pipeline {
    agent any
    stages {
        stage('Build') {
            steps {
                sh './mvnw clean build'
            }
        }
        stage('Test') {
            steps {
                sh './mvnw test'
            }
        }
    }
}