#!groovy

import java.text.SimpleDateFormat


def info(message) {
    echo "\033[1;33m[Info] ${message} \033[0m"
}

def success(message) {
    echo "\033[1;32m[Success] ${message} \033[0m"
}

def error(message) {
    echo "\033[1;31m[Error] ${message} \033[0m"
}

def triggerNotification(message) {
    return sh(returnStatus: true, script: """
                                    aws sns publish \
                                    --topic-arn "arn:aws:sns:us-east-1:365151504774:jenkins-build-status" \
                                    --message "${message}"
                            """)
}

pipeline {
    agent any


    // Pipeline options
    options {
        timeout(time: 1, unit: 'HOURS')
        ansiColor('xterm')
    }

    // Environment specific parameters to be used throughout the pipeline
    environment {

        APPLICATION = "spring-app"
        ECR_URL = "365151504774.dkr.ecr.us-east-1.amazonaws.com"
        ENVIRONMENT = "dev"
        BRANCH = "${params.BRANCH}"
        VERSION = "${env.BRANCH}-${new SimpleDateFormat("yyyy-MM-dd-HHmmss").format(new Date())}-${env.BUILD_NUMBER}"
        REPO_URL = "https://github.com/bhagvatulanipun/spring-app.git"
    }

    // Pipeline Stages
    stages {

        // Clean Jenkins workspace
        stage('Clean workspace') {
            steps {
                script {
                    info("Executing Stage 2: Clean workspace")

                    /* Logic starts here */

                    cleanWs()

                    /* Logic ends here */

                    success("Completed Stage 2: Clean workspace")
                }
            }
        }

        // Checkout code from Github
        stage('Code checkout') {
            steps {
                script {
                    info("Executing Stage 3: Code checkout")
                    info("Getting pull of ${env.BRANCH} from spring-boot repository.")

                    /* Logic starts here */

                    git branch: "${env.BRANCH}", changelog: false, credentialsId: "${env.GITHUB_LOGIN}", poll: false, url: "${env.REPO_URL}"


                    /* Logic ends here */

                    info("Fetched latest code from ${env.BRANCH} of spring-boot repository.")
                    success("Completed Stage 3: Code checkout")
                }
            }
        }

        // Build docker image and push to AWS ECR repo with latest pointing to current build tag
        stage('Docker build AWS ECR') {
            steps {
                script {
                    info("Executing Stage 4: Docker build AWS ECR")


                    /* Logic starts here */

                    def tag = "${env.VERSION}"

                    // Build and push docker image
                    IMAGENAME = "${env.ECR_URL}/${env.APPLICATION}"
                    docker.withRegistry("https://${env.ECR_URL}", "${env.ECR_LOGIN}") {
                        image = docker.build("${IMAGENAME}", " .")
                        docker.image("${IMAGENAME}").push("${tag}")
                    }

                    /* Logic ends here */

                    info("Pushed docker image ${tag}")
                    success("Completed Stage 4: Docker build AWS ECR")
                }
            }
        }

        // Deploy 
        stage('Deploy') {
            steps {
                script {
                    info("Starting deployment")

                            /* logic start */
                            IMAGE = "${env.ECR_URL}/${env.APPLICATION}:${env.VERSION}"
                            commandToExecute= "kubectl -n production set image deployment spring-deploy spring-actuator=${IMAGE} --record"


                            def isDeploy = sh (
                                    script: commandToExecute,
                                    returnStatus: true
                            )


                            if(isDeploy != 0) {
                                error("Deployment startup fialed: Image can not be updated")
                                currentBuild.result = 'FAILED'
                            }

                            else {

                                success("Deployment to EKS Started")
                                waiterCommand = "kubectl -n production rollout status deployment spring-deploy"
                                def isWaiter = sh (
                                      script : waiterCommand,
                                      returnStatus: true
                                )

                                if(isWaiter !=0) {
                                   error("Deployment fialed")
                                   error("Starting rollback")
                                   rollBack = "kubectl -n production rollout undo deployment spring-deploy"
                                   def isRolback = sh (
                                       script : rollBack,
                                       returnStatus: true
                                   )
                                   currentBuild.result = 'FAILED'
                                }

                                else {
                                    success("Deployment to EKS Complete")
                                }

                            }
                            /*logic ends*/

                }
            }
        }

    }

    // Post actions
    post {
        aborted {
            script {
                info("###############################")
                info('Build process is aborted')
                triggerNotification("Job: ${env.JOB_NAME} with buildnumber ${env.BUILD_NUMBER} was aborted.")
                info("###############################")
            }
        }
        failure {
            script {
                error("#############################")
                error('Build process failed.')
                triggerNotification("Job: ${env.JOB_NAME} with buildnumber ${env.BUILD_NUMBER} Failed.")
                error("#############################")
            }
        }
        success {
            script {
                success("#############################")
                success('Build process completed successfully.')
                triggerNotification("Job: ${env.JOB_NAME} with buildnumber ${env.BUILD_NUMBER} was Successfull.")
                success("#############################")
  
            }
        }
    }
}
