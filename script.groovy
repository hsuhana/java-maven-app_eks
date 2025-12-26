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
    sh '''
        aws sts get-caller-identity
        kubectl get nodes
    '''
    sh 'kubectl create deployment nginx-deployment --image=nginx'
}

return this
