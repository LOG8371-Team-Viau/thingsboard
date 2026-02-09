pipeline {
    agent any

    tools {
        maven 'Maven3'
        jdk 'JDK17'
    }

    environment {
        MAVEN_OPTS = '-Xmx1024m'
    }

    stages {

        stage('Checkout') {
            steps {
                git branch: 'master', url: 'https://github.com/LOG8371-Team-Viau/thingsboard.git'
            }
        }

        stage('Compile') {
            steps {
                bat 'mvn install -DskipTests -pl common/data,rule-engine/rule-engine-components -am'
            }
        }

        stage('Unit Tests') {
            steps {
                bat 'mvn test -pl common/data,rule-engine/rule-engine-components'
            }
            post {
                always {
                    junit allowEmptyResults: true, testResults: '**/target/surefire-reports/*.xml'
                }
            }
        }

        stage('Launch ThingsBoard') {
            steps {
                // Clean up any leftover containers from a previous run
                script {
                    bat(script: 'docker compose -f docker-compose-ci.yml down -v', returnStatus: true)
                }
                // Initialize database schema and load demo data
                bat 'docker compose -f docker-compose-ci.yml run --rm -e INSTALL_TB=true -e LOAD_DEMO=true thingsboard-ce'
                // Start all services in detached mode
                bat 'docker compose -f docker-compose-ci.yml up -d'
                // Wait for ThingsBoard to be ready
                script {
                    def maxAttempts = 20
                    def started = false
                    for (int i = 0; i < maxAttempts; i++) {
                        sleep(time: 15, unit: 'SECONDS')
                        def status = bat(script: 'curl.exe -sf http://localhost:9090/login', returnStatus: true)
                        if (status == 0) {
                            echo 'ThingsBoard started successfully!'
                            started = true
                            break
                        }
                        echo "Waiting for ThingsBoard to start... attempt ${i + 1}/${maxAttempts}"
                    }
                    if (!started) {
                        error('ThingsBoard did not start within the expected time.')
                    }
                }
            }
            post {
                always {
                    script {
                        bat(script: 'docker compose -f docker-compose-ci.yml down -v', returnStatus: true)
                    }
                }
            }
        }
    }

    post {
        always {
            cleanWs()
        }
    }
}
