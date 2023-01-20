pipeline {
    agent any
    stages {
        stage('Java_Version') {
            steps {
                sh '''
                  env | grep -e PATH -e JAVA_HOME
                  which java
                  java -version
                '''
            }
        }
        stage('Build') {
            steps {
                sh './mvnw clean install'
            }
        }
//         stage('Test') {
//             steps {
//                 sh './mvnw test'
//             }
//         }
    }
}