def notifySlack(String color, message) {
    slackSend channel: "${env.ICM_SLACK_CHANNEL}",
            color: "${color}",
            message: "${message}",
            tokenCredentialId: "${env.ICM_SLACK_CRED_ID}"
}