#!groovy

import java.text.SimpleDateFormat

/*
* Jenkins shared library
*/
@Library('utils@v1.0.1') _
@Library('ecs-jenkins-lib@v1.0.0') ecs
@Library('DevOps') ops

def GIT_COMMIT_ID = ""
def  ECS_IMAGE_TO_DEPLOY = ""

def createTargetGroup(name, vpc, port, healthCheckPath, healthCheckIntervalSec, healthyThresholdCount, unhealthyThresholdCount, healthCheckSuccessCode, region) {
    return sh(returnStdout: true, script: """
                                /usr/local/bin/aws elbv2 create-target-group \
                                --name ${name} \
                                --protocol HTTPS \
                                --port ${port} \
                                --vpc-id ${vpc} \
                                --health-check-path ${healthCheckPath} \
                                --health-check-interval-seconds ${healthCheckIntervalSec} \
                                --healthy-threshold-count ${healthyThresholdCount} \
                                --unhealthy-threshold-count ${unhealthyThresholdCount} \
                                --matcher '{"HttpCode": "${healthCheckSuccessCode}"}' \
                                --query 'TargetGroups[0].TargetGroupArn' \
                                --output text \
                                --region ${region}
                            """).trim()
}

def create443ListenerRule(listenerArn, priority, comHost, targetGroupArn, region) {
    return sh(returnStatus: true, script: """
                                    /usr/local/bin/aws elbv2 create-rule \
                                    --listener-arn ${listenerArn} \
                                    --priority ${priority} \
                                    --conditions Field=host-header,Values='"${comHost}"' \
                                    --actions Type=forward,TargetGroupArn=${targetGroupArn} \
                                    --region ${region}
                                """)
}

def create80ListenerRule(listenerArn, priority, comHost, targetGroupArn, region) {
    return sh(returnStatus: true, script: """
                                    /usr/local/bin/aws elbv2 create-rule \
                                    --listener-arn ${listenerArn} \
                                    --priority ${priority} \
                                    --conditions Field=host-header,Values='"${comHost}"' \
                                    --actions '[{"Type":"redirect","RedirectConfig": {"Protocol": "HTTPS","Port": "443","StatusCode": "HTTP_301"}}]' \
                                    --region ${region}
                                """)
}

def killAllJobsWithSameParams() {
    def currentJobName = env.JOB_NAME
    def currentBuildNum = env.BUILD_NUMBER.toInteger()
    def job = Jenkins.instance.getItemByFullName(currentJobName)
    for (build in job.builds) {
        if (!build.isBuilding()) {
            continue;
        }
        if (currentBuildNum == build.getNumber().toInteger()) {
            continue;
        }

        log.info("OLD_BRANCH ${build.environment["BRANCH"]} ::: ${env.BRANCH}")

        if (build.environment["BRANCH"] == env.BRANCH) {
            log.info("Aborting build: ${build}")
            build.doStop()
        }
    }
}



def registerTaskDefinition(taskFamily, taskName, memoryReservation, image, sqlConnStr, sqlDocConnStr, containerPort, tag, region) {
    return sh(returnStatus: true, script: """
                            /usr/local/bin/aws ecs register-task-definition --network-mode bridge \
                            --family ${taskFamily} \
                            --container-definitions '[{"name":"${taskName}", \
                                                    "image":"${image}", \
                                                    "memoryReservation": ${memoryReservation}, \
                                                    "portMappings":[{"containerPort":${containerPort}, "protocol":"tcp"}], \
                                                    "environment":[{"name":"SQLCONNSTR_CLIENTAREA", "value":"${sqlConnStr}"},{"name":"SQLCONNSTR_DOCUMENTS", "value":"${sqlDocConnStr}"},{"name":"ASPNETCORE_ENVIRONMENT", "value": "Development"},{"name":"ASPNETCORE_HTTPS_PORT", "value": "44358"},{"name":"ASPNETCORE_Kestrel__Certificates__Default__Password", "value": "862d7d30-402b-4b2e-8100-659ebd46cc71"},{"name":"ASPNETCORE_Kestrel__Certificates__Default__Path", "value": "/https/ICM.ClientArea.Api.Rest.pfx"},{"name":"ASPNETCORE_URLS", "value": "https://+:443;http://+:80"}], \
                                                    "mountPoints": [{"sourceVolume": "https-cert","containerPath": "/https","readOnly": true }], \
                                                    "logConfiguration": {"logDriver": "fluentd", "options": {"tag": "${tag}"}}}]' \
                            --volumes '[{"name": "https-cert","host":{"sourcePath":"/home/ec2-user/https"}}]' \
                            --region "${region}"
                        """)
}

def devDeploy(cluster, service, task_family, image, sqlConnStr, sqlDocConnStr, region, boolean is_wait = true, String awscli = "aws") {
    sh """
        OLD_TASK_DEF=\$(${awscli} ecs describe-task-definition \
                                --task-definition ${task_family} \
                                --output json --region ${region})
        OLD_TASK_DEF=\$(echo \$OLD_TASK_DEF | \
                    jq --arg IMAGE ${image} '.taskDefinition.containerDefinitions[0].image=\$IMAGE')
        NEW_TASK_DEF=\$(echo \$OLD_TASK_DEF | \
                    jq --argjson ENV_PARAMS '[{"name":"SQLCONNSTR_CLIENTAREA", "value":"${sqlConnStr}"},{"name":"SQLCONNSTR_DOCUMENTS", "value":"${sqlDocConnStr}"},{"name":"ASPNETCORE_ENVIRONMENT", "value": "Development"},{"name":"ASPNETCORE_HTTPS_PORT", "value": "44358"},{"name":"ASPNETCORE_Kestrel__Certificates__Default__Password", "value": "862d7d30-402b-4b2e-8100-659ebd46cc71"},{"name":"ASPNETCORE_Kestrel__Certificates__Default__Path", "value": "/https/ICM.ClientArea.Api.Rest.pfx"},{"name":"ASPNETCORE_URLS", "value": "https://+:443;http://+:80"}]' '.taskDefinition.containerDefinitions[0].environment=\$ENV_PARAMS')
        FINAL_TASK=\$(echo \$NEW_TASK_DEF | \
                    jq '.taskDefinition | \
                            {family: .family, \
                            networkMode: .networkMode, \
                            volumes: .volumes, \
                            containerDefinitions: .containerDefinitions, \
                            placementConstraints: .placementConstraints}')
                            
        ${awscli} ecs register-task-definition \
                --family ${task_family} \
                --cli-input-json \
                "\$(echo \$FINAL_TASK)" --region "${region}"
        if [ \$? -eq 0 ]
        then
            echo "New task has been registered"
        else
            echo "Error in task registration"
            exit 1
        fi
        
        echo "Now deploying new version..."
                    
        ${awscli} ecs update-service \
            --cluster ${cluster} \
            --service ${service} \
            --force-new-deployment \
            --task-definition ${task_family} \
            --region "${region}"
        
        if ${is_wait}; then
            echo "Waiting for deployment to reflect changes"
            ${awscli} ecs wait services-stable \
                --cluster ${cluster} \
                --service ${service} \
                --region "${region}"
        fi
    """
}

def wait(cluster, service, region, String awscli = "aws") {
    sh """
        ${awscli} ecs wait services-stable \
            --cluster ${cluster} \
            --service ${service} \
            --region "${region}"
    """
}
def createECSService(name, cluster, taskFamily, desiredCount, region) {
    sh(returnStatus: true, script: """
                                /usr/local/bin/aws ecs create-service \
                                --service-name ${name} \
                                --launch-type EC2 \
                                --cluster ${cluster} \
                                --task-definition ${taskFamily} \
                                --desired-count ${desiredCount} \
                                --region ${region}
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

        // Application specific parameters
        APPLICATION = "spring-boot"
        ENVIRONMENT = "dev"
        BRANCH = "${params.BRANCH}"
        VERSION = "${env.BRANCH}-${new SimpleDateFormat("yyyy-MM-dd-HHmmss").format(new Date())}"

        // Github specific parameters
        REPO_URL = "https://github.com/ICMarkets/icm-clientarea-api-autojob.git"
    }

    // Pipeline Stages
    stages {

        // Kill other jobs
        stage('Kill other jobs') {
            steps {
                script {
                    log.info("Executing Stage 1: Kill other jobs having same parameters")

                    /* Logic starts here */

                    killAllJobsWithSameParams()

                    /* Logic ends here */

                    log.info("Completed Stage 1: Kill other jobs")
                }
            }
        }

        // Sleep for 15 seconds
        stage('Sleep for 15 seconds') {
            steps {
                script {
                    log.info("Executing Stage 2: Going to sleep for 15 seconds")

                    /* Logic starts here */

                    sleep 15

                    /* Logic ends here */

                    log.info("Completed Stage 2: Awake from sleep now...")

                }
            }
        }

        // Clean Jenkins workspace
        stage('Clean workspace') {
            steps {
                script {
                    log.info("Executing Stage 3: Clean workspace")

                    /* Logic starts here */

                    cleanWs()

                    /* Logic ends here */

                    log.success("Completed Stage 3: Clean workspace")
                }
            }
        }

        // Checkout code from Github
        stage('Code checkout') {
            steps {
                script {
                    log.info("Executing Stage 4: Code checkout")
                    log.info("Getting pull of ${env.BRANCH} from icm-clientarea-api repository.")

                    /* Logic starts here */

                    git branch: "${env.BRANCH}", changelog: false, credentialsId: "${env.ICM_GITHUB_CRED_ID}", poll: false, url: "${env.REPO_URL}"

                    GIT_COMMIT_ID = "${helper.gitShortCommit()}"

                    /* Logic ends here */

                    log.info("Fetched latest code from ${env.BRANCH} of icm-clientarea-api repository.")
                    log.success("Completed Stage 4: Code checkout")
                }
            }
        }

        // Build docker image and push to AWS ECR repo with latest pointing to current build tag
        stage('Docker build AWS ECR') {
            steps {
                script {
                    log.info("Executing Stage 5: Docker build AWS ECR")
                    log.info("Creating docker image for ECR (global)")

                    /* Logic starts here */

                    def tag = "${env.VERSION}-${GIT_COMMIT_ID}"
                    
                    buildArgs = "--build-arg COMMIT=${GIT_COMMIT_ID} --build-arg CI_BRANCH=${env.BRANCH} "


                    // Build and push docker image
                    IMAGENAME = "${env.ICM_ECR_REPO_URL}/${env.APPLICATION}"
                    docker.withRegistry("https://${env.ICM_ECR_REPO_URL}", "${env.ICM_IAM_ROLE}") {
                        image = docker.build("${IMAGENAME}", "${buildArgs} .")
                        docker.image("${IMAGENAME}").push("${tag}")
                    }

                    /* Logic ends here */

                    log.info("Pushed docker image ${tag}")
                    log.success("Completed Stage 5: Docker build AWS ECR")
                }
            }
        }

        // Deploy to dev
        stage('Deploy') {
            steps {
                script {

                    log.info("Executing Stage 6: Deploy")
                    log.info("Starting deployment for ${env.ENVIRONMENT}")

                    ECS_IMAGE_TO_DEPLOY = "${env.ICM_ECR_REPO_URL}/${APPLICATION}:${env.VERSION}-${GIT_COMMIT_ID}"
                    /* Logic starts here */

                    def service = "icm-${env.ENVIRONMENT}-${env.APPLICATION}-${env.BRANCH}-svc"
                    def taskFamily = "icm-${env.ENVIRONMENT}-${env.APPLICATION}-${env.BRANCH}"
                    def taskName = "icm-${env.APPLICATION}-${env.BRANCH}"

                    def isServiceExists = awsHelper.isECSServiceExists("${env.CLUSTER_NAME}", service, "${env.ICM_AWS_DEFAULT_REGION}")

                    if (isServiceExists == 0) {
                        log.info("Service already exists, No need to create stack.")
                        log.info("Deploying now...")
                        devDeploy("${env.CLUSTER_NAME}",
                                service,
                                taskFamily,
                                "${ECS_IMAGE_TO_DEPLOY}",
                                "${params.SQLCONNSTR_CLIENTAREA}",
                                "${params.SQLCONNSTR_DOCUMENTS}",
                                "${env.ICM_AWS_DEFAULT_REGION}",
                                true)
                    }
                    else {
                        log.info("New branch setup: Creating task definition now")

                        def isTaskDefCreated = registerTaskDefinition(taskFamily, taskName,
                                128, "${ECS_IMAGE_TO_DEPLOY}","${params.SQLCONNSTR_CLIENTAREA}","${params.SQLCONNSTR_DOCUMENTS}", 443, "clientarea-api", "${env.ICM_AWS_DEFAULT_REGION}")

                        if (isTaskDefCreated != 0) {
                            currentBuild.result = 'FAILED'
                            error("Error while creating TaskDefinition.")
                        }

                        log.success("TaskDefinition created successfully.")
                        log.info("Creating service now.")

                        def tgArn = createTargetGroup("${env.TARGET_GROUP}", "${env.VPC}", 443,
                                            "/health", 15, 2, 2, 200, "${env.ICM_AWS_DEFAULT_REGION}")

                        if (tgArn == "") {
                            currentBuild.result = 'FAILED'
                            error("Error while creating TargetGroup.")
                        }

                        log.success("TargetGroup created successfully.")
                        log.info("Modifying TG attributes")

                        def isModifiedTgAttr = awsHelper.modifyTargetGroupAttr("${tgArn}", 30, "${env.ICM_AWS_DEFAULT_REGION}")

                        if (isModifiedTgAttr != 0) {
                            currentBuild.result = 'FAILED'
                            error("Error while modifying TargetGroup attributes.")
                        }

                        log.success("TargetGroup attributes modified successfully.")
                        log.info("Creating LB listener now.")

                        def priority = awsHelper.getNextALBRulePriority("${env.DEV_ALB_HTTP_LISTENER_ARN}", "${env.ICM_AWS_DEFAULT_REGION}")

                        def isListenerRuleCreated = create80ListenerRule("${env.DEV_ALB_HTTP_LISTENER_ARN}",
                                "${priority}", "${env.COM_SITE_HOST}", "${tgArn}", "${env.ICM_AWS_DEFAULT_REGION}")


                        if (isListenerRuleCreated != 0) {
                            currentBuild.result = 'FAILED'
                            error("Error while creating LB HTTP listener.")
                        }

                        isListenerRuleCreated = create443ListenerRule("${env.DEV_ALB_HTTPS_LISTENER_ARN}",
                                "${priority}", "${env.COM_SITE_HOST}", "${tgArn}", "${env.ICM_AWS_DEFAULT_REGION}")

                        if (isListenerRuleCreated != 0) {
                            currentBuild.result = 'FAILED'
                            error("Error while creating LB HTTPS listener.")
                        }

                        log.success("LB listener created successfully.")
                        log.info("Creating service now.")

                        def isServiceCreated = awsHelper.createECSService(service, "${env.CLUSTER_NAME}", taskFamily,
                                1, "${tgArn}", taskName, 443, "${env.ICM_AWS_DEFAULT_REGION}")

                        if (isServiceCreated != 0) {
                            currentBuild.result = 'FAILED'
                            error("Error while creating service. Marking the status of build FAILED.")
                        }
                                        
                        log.success("Service created successfully. Waiting for service to be stable.")
                        //ecs.wait("${env.CLUSTER_NAME}", service, "${env.ICM_AWS_DEFAULT_REGION}")
                        log.success("Deployment done.")
                    }
                    log.success("Deployment completed for Dev")
                    log.success("Completed Stage 6: Deploy")
                }
            }
        }

    }

    // Post actions
    post {
        aborted {
            script {
                log.info("###############################")
                log.info('Build process is aborted')
             //   helper.notifySlack("warning", "Job: ${env.JOB_NAME} with buildnumber ${env.BUILD_NUMBER} was aborted.")
                log.info("###############################")
            }
        }
        failure {
            script {
                log.error("#############################")
                log.error('Build process failed.')
              //  helper.notifySlack("danger", "Job: ${env.JOB_NAME} with buildnumber ${env.BUILD_NUMBER} was failed.")
                log.error("#############################")
            }
        }
        success {
            script {
                log.success("*************************************************")
                log.success("Endpoint: \n ${env.COM_SITE_HOST}")
                //helper.notifySlack("good", "Job: ${env.JOB_NAME} with buildnumber ${env.BUILD_NUMBER} was successful.\n URL:\n ${env.COM_SITE_HOST}")
                log.success("*************************************************")
                log.success('Build process completed successfully.')
            }
        }
    }
}
