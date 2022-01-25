CONFIG = "mct-zone1-cs1-kvm1-kvm2"
TESTS = [
    "tests/test_delete_account.py",
    "tests/test_ip_exclusion_list.py",
    "tests/test_loadbalance.py",
    "tests/test_password_and_dnsmasq_service.py",
    "tests/test_privategw_acl.py",
    "tests/test_public_ip_acl.py",
    "tests/test_release_ip.py",
    "tests/test_router_ip_tables_policies.py",
    "tests/test_vpc_ip_tables_policies.py",
    "tests/test_ssvm.py",
    "tests/test_vpc_redundant.py",
    "tests/test_vpc_router_nics.py",
    "tests/test_vpc_vpn.py"
]
TESTS_DEFAULTS = TESTS.join("\n")

pipeline {
    agent {
        // label "executor"
        label "hv02"
    }
    options {
        timestamps()
        ansiColor("xterm")
    }

    parameters {
        string(
            name: 'RELEASEVERSION',
            defaultValue: '',
            description: 'Release version'
        )
        text(
            name: 'TESTS',
            description: 'Set of integration tests to execute',
            defaultValue: TESTS_DEFAULTS
        )
        string(
            name: 'CONFIG',
            description: 'Terraform config to use',
            defaultValue: CONFIG
        )
    }

    environment {
        def VERSION = "${params.RELEASEVERSION}"
    }

    stages {
        stage('Checkout') {
            steps {
                // checkout scm
                git url: "https://github.com/sanderv32/cosmic.git", branch: "jenkinsfile"
            }
        }

        stage('Build infrastructure') {
            steps {
                sh script: "cp $WORKSPACE/pom.xml /tmp/pom.xml"
                sh script: "/data/shared/build-run-deploy.sh -c configs/${params.CONFIG} -W $WORKSPACE"
            }
        }

        stage('Run integration tests') {
            steps {
                executeTests(params.TESTS)
            }
        }

        stage('Collect logs') {
            steps {
                sh script: "sshpass -p password scp root@cs1:/var/log/cosmic/management/management.log $WORKSPACE/cs1_management.log"
                sh script: "sshpass -p password scp root@cs1:/var/log/cosmic/management/apiagent.log $WORKSPACE/cs1_apiagent.log"
                sh script: "sshpass -p password scp root@cs1:/var/log/cosmic/management/api.log $WORKSPACE/cs1_api.log"
                sh script: "sshpass -p password scp root@kvm1:/var/log/cosmic/agent/agent.log $WORKSPACE/kvm1_agent.log"
                sh script: "sshpass -p password scp root@kvm2:/var/log/cosmic/agent/agent.log $WORKSPACE/kvm2_agent.log"
            }
        }
    }

    post {
        success {
            archiveArtifacts artifacts: "cosmic-client/target/cloud-client-ui-*.war", fingerprint: true
            archiveArtifacts artifacts: "cosmic-agent/target/cloud-agent-*.jar", fingerprint: true
        }

        always {
            archiveArtifacts artifacts: "cs1_management.log", fingerprint: true
            archiveArtifacts artifacts: "cs1_apiagent.log", fingerprint: true
            archiveArtifacts artifacts: "cs1_api.log", fingerprint: true
            archiveArtifacts artifacts: "kvm1_agent.log", fingerprint: true
            archiveArtifacts artifacts: "kvm2_agent.log", fingerprint: true
            archiveArtifacts artifacts: "failed_plus_exceptions.txt", fingerprint: true
            archiveArtifacts artifacts: "runinfo.txt", fingerprint: true
            sh script: "find $WORKSPACE -path */surefire-reports/*.xml -exec touch {} \\;"
            junit "**/target/surefire-reports/*.xml"
        }

        cleanup {
            // Cleanup VM's
            sh script: "ansible-playbook -i /data/shared/configs/$CONFIG/inventory --extra-vars TERRAFORM_PLAN=/data/shared/terraform-plans/$CONFIG /data/shared/ci-cleanup.yml"
        //     // Cleanup workspace
        //     sh "test -d $WORKSPACE && rm -rf $WORKSPACE"
        }
    }
}

def executeTests(tests) {
    for (item in tests.split('\n')) {
        if (item?.trim()) {
            sh(script: "python -u -m unittest $WORKSPACE/cosmic-core/test/integration/${item}", returnStdout: true)
        }
    }
}
