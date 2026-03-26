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
                        url: VAULT_URL + '/api/vault/cicd/testservice/e3',
                        httpMode: 'GET',
                        customHeaders: [
                            [name: 'X-CICD-TOKEN', value: CICD_TOKEN, maskValue: true]
                        ],
                        acceptType: 'APPLICATION_JSON'
                    )

                    def json = readJSON text: response.content
                    def envFileContent = ''

                    json.each { entry ->
                        envFileContent += "${entry.key}=${entry.value}\n"
                    }

                    writeFile file: 'testservice.env', text: envFileContent
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
