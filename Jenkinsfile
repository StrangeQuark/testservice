pipeline {
    agent { label 'linux-agent' }

    environment {
        VAULT_URL = credentials('VAULT_URL') // Integration line: Vault
        CICD_TOKEN = credentials('CICD_TOKEN') // Integration line: Vault
    }

    stages {
        // Integration function start: Vault
        stage("Retrieve Env Vars") {
            steps {
                script {
                    def response = httpRequest(
                        url: VAULT_URL + '/api/vault/cicd',
                        httpMode: 'POST',
                        contentType: 'APPLICATION_JSON',
                        requestBody: '{"serviceName":"testservice","environmentName":"e3"}',
                        customHeaders: [
                            [name: 'X-CICD-TOKEN', value: CICD_TOKEN, maskValue: true]
                        ],
                        validResponseCodes: '200'
                    )

                    writeFile file: 'testservice.env', text: response.content
                    echo "Environment variables written to testservice.env"
                }
            }
        }
        // Integration function end: Vault
        stage("Deploy & Test") {
            steps {
                script {
                    try {
                        sh "docker compose --env-file testservice.env up --build"
                    } catch (ex) {
                        echo "Unexpected failure: ${ex.getMessage()}"
                        sh "docker compose down"
                        error("Deployment crashed.")
                    } finally {
                        // Ensure containers are cleaned up
                        sh "docker compose down"
                    }
                }
            }
        }

        stage("Publish HTML Report") {
            steps {
                publishHTML(target: [
                    allowMissing: true,
                    alwaysLinkToLastBuild: true,
                    keepAll: true,
                    reportDir: 'testservice/test-results',
                    reportFiles: 'report.html',
                    reportName: 'Test Report'
                ])
            }
        }
    }

    post {
        always {
            sh "rm -f testservice.env"
            echo "Cleaned up testservice.env"
            sh "docker compose down -v"
        }
    }
}
