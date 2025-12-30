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
    echo "building multi-arch docker image..."

    withCredentials([usernamePassword(credentialsId: 'ecr-credentials',
                                      passwordVariable: 'PASS',
                                      usernameVariable: 'USER')]) {

        // Login first
        sh 'echo $PASS | docker login -u $USER --password-stdin ${DOCKER_REPO_SERVER}'

        // Enable buildx (safe to run multiple times)
        sh 'docker buildx create --use --name multiarch-builder || true'
        sh 'docker buildx inspect --bootstrap'

        // Build & push multi-arch image
        sh """
        docker buildx build \
          --platform linux/amd64,linux/arm64 \
          -t ${DOCKER_REPO}:${IMAGE_NAME} \
          --push .
        """
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
