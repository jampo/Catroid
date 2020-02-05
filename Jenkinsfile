#!groovy

class DockerParameters {
    def fileName = 'Dockerfile.jenkins'

    // 'docker build' would normally copy the whole build-dir to the container, changing the
    // docker build directory avoids that overhead
    def dir = 'docker'

    // Pass the uid and the gid of the current user (jenkins-user) to the Dockerfile, so a
    // corresponding user can be added. This is needed to provide the jenkins user inside
    // the container for the ssh-agent to work.
    // Another way would be to simply map the passwd file, but would spoil additional information
    // Also hand in the group id of kvm to allow using /dev/kvm.
    def buildArgs = '--build-arg USER_ID=$(id -u) --build-arg GROUP_ID=$(id -g) --build-arg KVM_GROUP_ID=$(getent group kvm | cut -d: -f3)'

    def args = '--device /dev/kvm:/dev/kvm -v /var/local/container_shared/gradle_cache/$EXECUTOR_NUMBER:/home/user/.gradle -m=14G'
    def label = 'LimitedEmulator'
}

def d = new DockerParameters()

def junitAndCoverage(String jacocoReportDir, String jacocoReportXml, String coverageName) {
    // Consume all test xml files. Otherwise tests would be tracked multiple
    // times if this function was called again.
    String testPattern = '**/*TEST*.xml'
    junit testResults: testPattern, allowEmptyResults: true
    cleanWs patterns: [[pattern: testPattern, type: 'INCLUDE']]

    publishJacocoHtml jacocoReportDir, jacocoReportXml, coverageName
}

def postEmulator(String coverageNameAndLogcatPrefix) {
    sh './gradlew stopEmulator'

    def jacocoReportDir = 'catroid/build/reports/coverage/catroid/debug'
    junitAndCoverage jacocoReportDir, 'report.xml', coverageNameAndLogcatPrefix

    archiveArtifacts "${coverageNameAndLogcatPrefix}_logcat.txt"
}

def useWebTestParameter() {
    return env.USE_WEB_TEST?.toBoolean() ? '-PuseWebTest' : ''
}

def allFlavoursParameters() {
    return env.BUILD_ALL_FLAVOURS?.toBoolean() ? 'assembleCreateAtSchoolDebug ' +
            'assembleLunaAndCatDebug assemblePhiroDebug assembleArduinoDebug' : ''
}

def useDebugLabelParameter(defaultLabel){
    return env.DEBUG_LABEL?.trim() ? env.DEBUG_LABEL : defaultLabel
}

pipeline {
    agent none

    parameters {
        booleanParam name: 'USE_WEB_TEST', defaultValue: false, description: 'When selected all the archived APKs will point to the test Catrobat web server, useful for testing web changes.'
        booleanParam name: 'BUILD_ALL_FLAVOURS', defaultValue: false, description: 'When selected all flavours are built and archived as artifacts that can be installed alongside other versions of the same APK.'
        string name: 'DEBUG_LABEL', defaultValue: '', description: 'For debugging when entered will be used as label to decide on which slaves the jobs will run.'
    }

    options {
        timeout(time: 2, unit: 'HOURS')
        timestamps()
        buildDiscarder(logRotator(numToKeepStr: '30'))
    }

    triggers {
        cron(env.BRANCH_NAME == 'develop' ? '@midnight' : '')
        issueCommentTrigger('.*test this please.*')
    }

    stages {
        stage('All') {
            parallel {
                stage('1') {
                    agent {
                        dockerfile {
                            filename d.fileName
                            dir d.dir
                            additionalBuildArgs d.buildArgs
                            args d.args
                            label useDebugLabelParameter(d.label)
                        }
                    }

                    stages {
						stage('fastlane version') {
							steps {
								echo 'fastlane version with sourcing'
								sh "#!/bin/bash \n" +
								"source ~/.bashrc && fastlane --version"
								echo 'fastlane versio without sourcing'
								sh "#!/bin/bash \n" +
								"fastlane --version"
								sh "fastlane --version"
								sh "fastlane actions"
								echo 'ruby version'
								sh "ruby --version"
								echo 'rbenv version'
								sh "rbenv --version"
								echo 'rmv version'
								sh "rmv --version"
							}
						}
                    }
                    post {
                        always {
                            stash name: 'logParserRules', includes: 'buildScripts/log_parser_rules'
                        }
                    }
                }
            }
        }
    }

    post {
        always {
            node('master') {
                unstash 'logParserRules'
                step([$class: 'LogParserPublisher', failBuildOnError: true, projectRulePath: 'buildScripts/log_parser_rules', unstableOnWarning: true, useProjectRule: true])
            }
        }
        changed {
            node('master') {
                notifyChat()
            }
        }
    }
}
