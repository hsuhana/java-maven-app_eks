def incrementVer() {
    echo 'incrementing app version...'
    sh 'mvn build-helper:parse-version versions:set \
        -DnewVersion=\\\${parsedVersion.majorVersion}.\\\${parsedVersion.minorVersion}.\\\${parsedVersion.nextIncrementalVersion} \
        versions:commit'
    def matcher = readFile('pom.xml') =~ '<version>(.+)</version>'
    def version = matcher[0][1]
    env.IMAGE_NAME = "$version-$BUILD_NUMBER"
}

def buildJar() {
    echo 'building the application...'
    sh 'mvn clean package'
}

def buildImage() {
    echo "building the docker image..."
    withCredentials([usernamePassword(credentialsId: 'dockerhub-credential', passwordVariable: 'PASS', usernameVariable: 'USER')]) {
        sh "docker build -t ${DOCKER_REPO}:${IMAGE_NAME} ."
        sh 'echo $PASS | docker login -u $USER --password-stdin ${DOCKER_REPO_SERVER}'
        sh "docker push ${DOCKER_REPO}:${IMAGE_NAME}"
    }
}

def deployApp() {
    echo 'deploying the application...'

    // Make sure kubeconfig exists for kubectl
    sh 'aws eks update-kubeconfig --name eks-cluster-test --region $AWS_DEFAULT_REGION'

    // Now deploy with kubectl
    sh 'kubectl create deployment nginx-deployment --image=nginx'
}

return this
