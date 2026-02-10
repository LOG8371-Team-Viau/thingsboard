/**
 * Copyright © 2016-2026 The Thingsboard Authors
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
pipeline {
    agent any

    tools {
        maven 'Maven3'
        jdk 'JDK17'
    }

    environment {
        MAVEN_OPTS = '-Xmx1024m'
    }

    triggers {
        githubPush()
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
                        def status = bat(script: 'curl.exe -sf http://localhost:8085/login', returnStatus: true)
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
        }

        stage('Demo - Access ThingsBoard') {
            steps {
                echo '========================================================'
                echo 'ThingsBoard is running! Open your browser and go to:'
                echo 'http://localhost:8085'
                echo ''
                echo 'Default credentials:'
                echo '  Tenant Admin: tenant@thingsboard.org / tenant'
                echo '  System Admin: sysadmin@thingsboard.org / sysadmin'
                echo '========================================================'
                input message: 'ThingsBoard is running on http://localhost:8085. Click "Proceed" when you are done with the demo to shut it down.'
            }
        }

        stage('Cleanup') {
            steps {
                script {
                    bat(script: 'docker compose -f docker-compose-ci.yml down -v', returnStatus: true)
                }
            }
        }
    }

    post {
        always {
            script {
                // Ensure containers are stopped even if pipeline is aborted
                bat(script: 'docker compose -f docker-compose-ci.yml down -v', returnStatus: true)
            }
            cleanWs()
        }
    }
}
