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

    // Jenkins Plugin
    withKubeConfig([credentialsId: 'digitalocean-credentials', serverUrl: 'https://70850354-96b1-475a-8b17-18b0b6b7dbe5.k8s.ondigitalocean.com']) {
        // Now deploy with kubectl
        sh 'kubectl create deployment nginx-deployment --image=nginx'
    }

    
}

return this
