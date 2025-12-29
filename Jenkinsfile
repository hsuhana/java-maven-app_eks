def gv

pipeline {   
    agent any
    tools {
        maven 'maven-3.9'
    }
    stages {
        stage("init") {
            steps {
                script {
                    gv = load "script.groovy"
                }
            }
        }

        stage("increment version") {
            steps {
                script {
                    gv.incrementVer()
                }
            }
        }

        stage("build jar") {
            steps {
                script {
                    gv.buildJar()

                }
            }
        }

        stage("build image") {
            steps {
                script {
                    gv.buildImage()
                }
            }
        }

        stage("deploy") {
            environment {
                AWS_ACCESS_KEY_ID = credentials('jenkins_aws_access_key_id')
                AWS_SECRET_ACCESS_KEY = credentials('jenkins_aws_secret_access_key')
                AWS_DEFAULT_REGION = 'ca-central-1'
                APP_NAME = 'java-maven-app'
            }
            steps {
                script {
                    echo 'deploying the application...'

                    // Make sure kubeconfig exists for kubectl
                    sh 'aws eks update-kubeconfig --name eks-cluster-test --region $AWS_DEFAULT_REGION'

                    sh 'envsubst < kubernetes/deployment.yaml | kubectl apply -f -'
                    sh 'envsubst < kubernetes/service.yaml | kubectl apply -f -'
                }
            }
        }

        stage('commit version update'){
            steps {
                script {
                    withCredentials([usernamePassword(credentialsId: 'github-credential', passwordVariable: 'PASS', usernameVariable: 'USER')]){
                        sh "git remote set-url origin https://${USER}:${PASS}@github.com/hsuhana/java-maven-app_eks.git"
                        sh 'git add .'
                        sh 'git commit -m "ci: version bump"'
                        sh 'git push origin HEAD:dockerhub-aws-k8s-integrate-to-pipeline'
                    }
                }
            }
        }
              
    }
} 
