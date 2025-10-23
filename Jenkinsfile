pipeline {
    agent { label 'Host PC' }

    stages {
        // Integration function start: Vault
        stage("Retrieve Env Vars") {
            steps {
                script {
                    def response = httpRequest(
                        url: 'http://localhost:6020/api/vault/getVariablesByEnvironment/testservice/e3',
                        httpMode: 'GET',
                        acceptType: 'APPLICATION_JSON'
                    )

                    def json = readJSON text: response.content
                    def envFileContent = ''

                    json.each { entry ->
                        envFileContent += "${entry.key}=${entry.value}\n"
                    }

                    writeFile file: '.env', text: envFileContent
                    echo "Environment variables written to .env"
                }
            }
        }
        // Integration function end: Vault
        stage("Deploy & Test") {
            steps {
                script {
                    try {
                        bat "docker-compose --env-file .env up --build"
                    } catch (ex) {
                        echo "Unexpected failure: ${ex.getMessage()}"
                        bat "docker-compose down"
                        error("Deployment crashed.")
                    } finally {
                        // Ensure containers are cleaned up
                        bat "docker-compose down"
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
            bat "docker-compose down -v"
        }
    }
}
