#!groovy

import java.text.SimpleDateFormat

def GIT_COMMIT_ID = ""
def  ECS_IMAGE_TO_DEPLOY = ""

// def createTargetGroup(name, vpc, port, healthCheckPath, healthCheckIntervalSec, healthyThresholdCount, unhealthyThresholdCount, healthCheckSuccessCode, region) {
//     return sh(returnStdout: true, script: """
//                                 /usr/local/bin/aws elbv2 create-target-group \
//                                 --name ${name} \
//                                 --protocol HTTPS \
//                                 --port ${port} \
//                                 --vpc-id ${vpc} \
//                                 --health-check-path ${healthCheckPath} \
//                                 --health-check-interval-seconds ${healthCheckIntervalSec} \
//                                 --healthy-threshold-count ${healthyThresholdCount} \
//                                 --unhealthy-threshold-count ${unhealthyThresholdCount} \
//                                 --matcher '{"HttpCode": "${healthCheckSuccessCode}"}' \
//                                 --query 'TargetGroups[0].TargetGroupArn' \
//                                 --output text \
//                                 --region ${region}
//                             """).trim()
// }

// def create443ListenerRule(listenerArn, priority, comHost, targetGroupArn, region) {
//     return sh(returnStatus: true, script: """
//                                     /usr/local/bin/aws elbv2 create-rule \
//                                     --listener-arn ${listenerArn} \
//                                     --priority ${priority} \
//                                     --conditions Field=host-header,Values='"${comHost}"' \
//                                     --actions Type=forward,TargetGroupArn=${targetGroupArn} \
//                                     --region ${region}
//                                 """)
// }

// def create80ListenerRule(listenerArn, priority, comHost, targetGroupArn, region) {
//     return sh(returnStatus: true, script: """
//                                     /usr/local/bin/aws elbv2 create-rule \
//                                     --listener-arn ${listenerArn} \
//                                     --priority ${priority} \
//                                     --conditions Field=host-header,Values='"${comHost}"' \
//                                     --actions '[{"Type":"redirect","RedirectConfig": {"Protocol": "HTTPS","Port": "443","StatusCode": "HTTP_301"}}]' \
//                                     --region ${region}
//                                 """)
// }

// def killAllJobsWithSameParams() {
//     def currentJobName = env.JOB_NAME
//     def currentBuildNum = env.BUILD_NUMBER.toInteger()
//     def job = Jenkins.instance.getItemByFullName(currentJobName)
//     for (build in job.builds) {
//         if (!build.isBuilding()) {
//             continue;
//         }
//         if (currentBuildNum == build.getNumber().toInteger()) {
//             continue;
//         }

//         info("OLD_BRANCH ${build.environment["BRANCH"]} ::: ${env.BRANCH}")

//         if (build.environment["BRANCH"] == env.BRANCH) {
//             info("Aborting build: ${build}")
//             build.doStop()
//         }
//     }
// }



// def registerTaskDefinition(taskFamily, taskName, memoryReservation, image, sqlConnStr, sqlDocConnStr, containerPort, tag, region) {
//     return sh(returnStatus: true, script: """
//                             /usr/local/bin/aws ecs register-task-definition --network-mode bridge \
//                             --family ${taskFamily} \
//                             --container-definitions '[{"name":"${taskName}", \
//                                                     "image":"${image}", \
//                                                     "memoryReservation": ${memoryReservation}, \
//                                                     "portMappings":[{"containerPort":${containerPort}, "protocol":"tcp"}], \
//                                                     "environment":[{"name":"SQLCONNSTR_CLIENTAREA", "value":"${sqlConnStr}"},{"name":"SQLCONNSTR_DOCUMENTS", "value":"${sqlDocConnStr}"},{"name":"ASPNETCORE_ENVIRONMENT", "value": "Development"},{"name":"ASPNETCORE_HTTPS_PORT", "value": "44358"},{"name":"ASPNETCORE_Kestrel__Certificates__Default__Password", "value": "862d7d30-402b-4b2e-8100-659ebd46cc71"},{"name":"ASPNETCORE_Kestrel__Certificates__Default__Path", "value": "/https/ICM.ClientArea.Api.Rest.pfx"},{"name":"ASPNETCORE_URLS", "value": "https://+:443;http://+:80"}], \
//                                                     "mountPoints": [{"sourceVolume": "https-cert","containerPath": "/https","readOnly": true }], \
//                                                     "logConfiguration": {"logDriver": "fluentd", "options": {"tag": "${tag}"}}}]' \
//                             --volumes '[{"name": "https-cert","host":{"sourcePath":"/home/ec2-user/https"}}]' \
//                             --region "${region}"
//                         """)
// }

// def devDeploy(cluster, service, task_family, image, sqlConnStr, sqlDocConnStr, region, boolean is_wait = true, String awscli = "aws") {
//     sh """
//         OLD_TASK_DEF=\$(${awscli} ecs describe-task-definition \
//                                 --task-definition ${task_family} \
//                                 --output json --region ${region})
//         OLD_TASK_DEF=\$(echo \$OLD_TASK_DEF | \
//                     jq --arg IMAGE ${image} '.taskDefinition.containerDefinitions[0].image=\$IMAGE')
//         NEW_TASK_DEF=\$(echo \$OLD_TASK_DEF | \
//                     jq --argjson ENV_PARAMS '[{"name":"SQLCONNSTR_CLIENTAREA", "value":"${sqlConnStr}"},{"name":"SQLCONNSTR_DOCUMENTS", "value":"${sqlDocConnStr}"},{"name":"ASPNETCORE_ENVIRONMENT", "value": "Development"},{"name":"ASPNETCORE_HTTPS_PORT", "value": "44358"},{"name":"ASPNETCORE_Kestrel__Certificates__Default__Password", "value": "862d7d30-402b-4b2e-8100-659ebd46cc71"},{"name":"ASPNETCORE_Kestrel__Certificates__Default__Path", "value": "/https/ICM.ClientArea.Api.Rest.pfx"},{"name":"ASPNETCORE_URLS", "value": "https://+:443;http://+:80"}]' '.taskDefinition.containerDefinitions[0].environment=\$ENV_PARAMS')
//         FINAL_TASK=\$(echo \$NEW_TASK_DEF | \
//                     jq '.taskDefinition | \
//                             {family: .family, \
//                             networkMode: .networkMode, \
//                             volumes: .volumes, \
//                             containerDefinitions: .containerDefinitions, \
//                             placementConstraints: .placementConstraints}')
                            
//         ${awscli} ecs register-task-definition \
//                 --family ${task_family} \
//                 --cli-input-json \
//                 "\$(echo \$FINAL_TASK)" --region "${region}"
//         if [ \$? -eq 0 ]
//         then
//             echo "New task has been registered"
//         else
//             echo "Error in task registration"
//             exit 1
//         fi
        
//         echo "Now deploying new version..."
                    
//         ${awscli} ecs update-service \
//             --cluster ${cluster} \
//             --service ${service} \
//             --force-new-deployment \
//             --task-definition ${task_family} \
//             --region "${region}"
        
//         if ${is_wait}; then
//             echo "Waiting for deployment to reflect changes"
//             ${awscli} ecs wait services-stable \
//                 --cluster ${cluster} \
//                 --service ${service} \
//                 --region "${region}"
//         fi
//     """
// }

// def wait(cluster, service, region, String awscli = "aws") {
//     sh """
//         ${awscli} ecs wait services-stable \
//             --cluster ${cluster} \
//             --service ${service} \
//             --region "${region}"
//     """
// }
// def createECSService(name, cluster, taskFamily, desiredCount, region) {
//     sh(returnStatus: true, script: """
//                                 /usr/local/bin/aws ecs create-service \
//                                 --service-name ${name} \
//                                 --launch-type EC2 \
//                                 --cluster ${cluster} \
//                                 --task-definition ${taskFamily} \
//                                 --desired-count ${desiredCount} \
//                                 --region ${region}
//     """)
// }
def info(message) {
    echo "\033[1;33m[Info] ${message} \033[0m"
}

def success(message) {
    echo "\033[1;32m[Success] ${message} \033[0m"
}

def error(message) {
    echo "\033[1;31m[Error] ${message} \033[0m"
}




pipeline {
    agent any


    // Pipeline options
    options {
        timeout(time: 1, unit: 'HOURS')

    }

    // Environment specific parameters to be used throughout the pipeline
    environment {

        // Application specific parameters
        APPLICATION = "spring-app"
        ECR_URL = "365151504774.dkr.ecr.us-east-1.amazonaws.com/"
        ENVIRONMENT = "dev"
        BRANCH = "${params.BRANCH}"
        VERSION = "${env.BRANCH}-${new SimpleDateFormat("yyyy-MM-dd-HHmmss").format(new Date())}-${env.BUILD_NUMBER}"
        

        // Github specific parameters
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

                    info("Fetched latest code from ${env.BRANCH} of spring-boot  repository.")
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
                    docker.withRegistry("https://${env.ICM_ECR_REPO_URL}") {
                        image = docker.build("${IMAGENAME}", " .")
                        docker.image("${IMAGENAME}").push("${tag}")
                    }

                    /* Logic ends here */

                    info("Pushed docker image ${tag}")
                    success("Completed Stage 4: Docker build AWS ECR")
                }
            }
        }

        // // Deploy to dev
        // stage('Deploy') {
        //     steps {
        //         script {

        //             info("Executing Stage 6: Deploy")
        //             info("Starting deployment for ${env.ENVIRONMENT}")

        //             ECS_IMAGE_TO_DEPLOY = "${env.ICM_ECR_REPO_URL}/${APPLICATION}:${env.VERSION}-${GIT_COMMIT_ID}"
        //             /* Logic starts here */

        //             def service = "icm-${env.ENVIRONMENT}-${env.APPLICATION}-${env.BRANCH}-svc"
        //             def taskFamily = "icm-${env.ENVIRONMENT}-${env.APPLICATION}-${env.BRANCH}"
        //             def taskName = "icm-${env.APPLICATION}-${env.BRANCH}"

        //             def isServiceExists = awsHelper.isECSServiceExists("${env.CLUSTER_NAME}", service, "${env.ICM_AWS_DEFAULT_REGION}")

        //             if (isServiceExists == 0) {
        //                 info("Service already exists, No need to create stack.")
        //                 info("Deploying now...")
        //                 devDeploy("${env.CLUSTER_NAME}",
        //                         service,
        //                         taskFamily,
        //                         "${ECS_IMAGE_TO_DEPLOY}",
        //                         "${params.SQLCONNSTR_CLIENTAREA}",
        //                         "${params.SQLCONNSTR_DOCUMENTS}",
        //                         "${env.ICM_AWS_DEFAULT_REGION}",
        //                         true)
        //             }
        //             else {
        //                 info("New branch setup: Creating task definition now")

        //                 def isTaskDefCreated = registerTaskDefinition(taskFamily, taskName,
        //                         128, "${ECS_IMAGE_TO_DEPLOY}","${params.SQLCONNSTR_CLIENTAREA}","${params.SQLCONNSTR_DOCUMENTS}", 443, "clientarea-api", "${env.ICM_AWS_DEFAULT_REGION}")

        //                 if (isTaskDefCreated != 0) {
        //                     currentBuild.result = 'FAILED'
        //                     error("Error while creating TaskDefinition.")
        //                 }

        //                 success("TaskDefinition created successfully.")
        //                 info("Creating service now.")

        //                 def tgArn = createTargetGroup("${env.TARGET_GROUP}", "${env.VPC}", 443,
        //                                     "/health", 15, 2, 2, 200, "${env.ICM_AWS_DEFAULT_REGION}")

        //                 if (tgArn == "") {
        //                     currentBuild.result = 'FAILED'
        //                     error("Error while creating TargetGroup.")
        //                 }

        //                 success("TargetGroup created successfully.")
        //                 info("Modifying TG attributes")

        //                 def isModifiedTgAttr = awsHelper.modifyTargetGroupAttr("${tgArn}", 30, "${env.ICM_AWS_DEFAULT_REGION}")

        //                 if (isModifiedTgAttr != 0) {
        //                     currentBuild.result = 'FAILED'
        //                     error("Error while modifying TargetGroup attributes.")
        //                 }

        //                 success("TargetGroup attributes modified successfully.")
        //                 info("Creating LB listener now.")

        //                 def priority = awsHelper.getNextALBRulePriority("${env.DEV_ALB_HTTP_LISTENER_ARN}", "${env.ICM_AWS_DEFAULT_REGION}")

        //                 def isListenerRuleCreated = create80ListenerRule("${env.DEV_ALB_HTTP_LISTENER_ARN}",
        //                         "${priority}", "${env.COM_SITE_HOST}", "${tgArn}", "${env.ICM_AWS_DEFAULT_REGION}")


        //                 if (isListenerRuleCreated != 0) {
        //                     currentBuild.result = 'FAILED'
        //                     error("Error while creating LB HTTP listener.")
        //                 }

        //                 isListenerRuleCreated = create443ListenerRule("${env.DEV_ALB_HTTPS_LISTENER_ARN}",
        //                         "${priority}", "${env.COM_SITE_HOST}", "${tgArn}", "${env.ICM_AWS_DEFAULT_REGION}")

        //                 if (isListenerRuleCreated != 0) {
        //                     currentBuild.result = 'FAILED'
        //                     error("Error while creating LB HTTPS listener.")
        //                 }

        //                 success("LB listener created successfully.")
        //                 info("Creating service now.")

        //                 def isServiceCreated = awsHelper.createECSService(service, "${env.CLUSTER_NAME}", taskFamily,
        //                         1, "${tgArn}", taskName, 443, "${env.ICM_AWS_DEFAULT_REGION}")

        //                 if (isServiceCreated != 0) {
        //                     currentBuild.result = 'FAILED'
        //                     error("Error while creating service. Marking the status of build FAILED.")
        //                 }
                                        
        //                 success("Service created successfully. Waiting for service to be stable.")
        //                 //ecs.wait("${env.CLUSTER_NAME}", service, "${env.ICM_AWS_DEFAULT_REGION}")
        //                 success("Deployment done.")
        //             }
        //             success("Deployment completed for Dev")
        //             success("Completed Stage 6: Deploy")
        //         }
        //     }
        // }

    }

    // Post actions
    post {
        aborted {
            script {
                info("###############################")
                info('Build process is aborted')
             //   helper.notifySlack("warning", "Job: ${env.JOB_NAME} with buildnumber ${env.BUILD_NUMBER} was aborted.")
                info("###############################")
            }
        }
        failure {
            script {
                error("#############################")
                error('Build process failed.')
              //  helper.notifySlack("danger", "Job: ${env.JOB_NAME} with buildnumber ${env.BUILD_NUMBER} was failed.")
                error("#############################")
            }
        }
        success {
            script {
                success("*************************************************")
                success("Endpoint: \n ${env.COM_SITE_HOST}")
                //helper.notifySlack("good", "Job: ${env.JOB_NAME} with buildnumber ${env.BUILD_NUMBER} was successful.\n URL:\n ${env.COM_SITE_HOST}")
                success("*************************************************")
                success('Build process completed successfully.')
            }
        }
    }
}