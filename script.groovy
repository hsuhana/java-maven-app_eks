def buildJar() {
    echo 'building the application...'
    sh 'mvn package'
}

def buildImage() {
    echo "building the docker image..."
    withCredentials([usernamePassword(credentialsId: 'dockerhub-credential', passwordVariable: 'PASS', usernameVariable: 'USER')]) {
        sh 'docker build -t tracyhsu57/my-app:jma-2.0 .'
        sh 'echo $PASS | docker login -u $USER --password-stdin'
        sh 'docker push tracyhsu57/my-app:jma-2.0'
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
