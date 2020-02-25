node('master') {
    try {
        currentBuild.displayName = "${env.BUILD_NUMBER}-BDSO-Cognito-CD-API(CI/CD)"
        wrap([$class: 'BuildUser']) {
            def gitUrl = 'https://github.com/AceInfoSolutions/CoETechTraining-Cognito'
            def gitCredentialId = 'githubCredentialId'
            
            def nexusRegistryURL = 'nexus.aceinfosolutions.com:8441'
            def nexusRepoUser	=	'aceinfocoe'
            def nexusCredentialId = 'AceInfoCoECreds'
            def nexusEmail = '0c9bce5f-0d20-46ab-a8a3-5fcf17af84d0'

            def dockerImageVersion = '1.0.0'
            
            def sonarQubeURL = 'http://sonar.aceinfosolutions.com:9000'
            def sonarQubeToken = 'sonarQubeToken'
            def sonarQubeSCMDisabled = 'true'
            def sonarQubeForceAuthentication = "true"
            
            def anchoreEngineURL = 'http://anchore.aceinfosolutions.com:8228/v1'
            
            def filepath = "/Source/"
            def artifactFilePath = "Source/"

            def projectTagName = "bdso-cognito"   

            def oc = 'oc'
            def openShiftHost = "https://api.rhopenshift-dev.aceinfocoe.com:6443"
            def openShiftCredential = "openshift-dev-token"
            def osConfigMapName = "${projectTagName}-config-map"


            def testProjectName = "bdso-test"
            def testConfigMapKeyValues = "bdsoCognitoConfigMap"
            def testProjectDisplayName = "bdso-test"
            def testTemplateName = "${artifactFilePath}templates/openshift/testing-template.yaml"

			def stageProjectName = "bdso-stag"
			def stageConfigMapKeyValues = "bdsoCognitoConfigMap"
			def stageProjectDisplayName = "bdso-stag"
			def stageTemplateName = "${artifactFilePath}templates/openshift/staging-template.yaml"

			def prodProjectName = "bdso-prod"
			def prodConfigMapKeyValues = "bdsoCognitoConfigMap"
			def prodProjectDisplayName = "bdso-prod"
			def prodTemplateName = "${artifactFilePath}templates/openshift/production-template.yaml"


            def route = '/healthcheck'
            def osurl = ""  

	    def green = "a"
            def blue = "b"
  	    def userInput
  	    def blueWeight
  	    def greenWeight
  	    def canaryDeployment = false
            
			stage ('Initializing') {
            	notifyStarted()
				checkout([$class: 'GitSCM',
                		branches: [[name: 'develop']],
                		doGenerateSubmoduleConfigurations: false,
                		extensions: [],
                		submoduleCfg: [],
                		userRemoteConfigs: [[credentialsId: gitCredentialId, url: gitUrl]],  poll: true
                	])
				withCredentials([
		        string(credentialsId: openShiftCredential, variable: 'OSTOKEN'),
		        [$class: 'UsernamePasswordMultiBinding', credentialsId: nexusCredentialId, usernameVariable: 'NEXUS_USERNAME', passwordVariable: 'NEXUS_PASSWORD']
		      	]) {
			        sh """
			        	${oc} login --server=${openShiftHost} --token=${OSTOKEN} --insecure-skip-tls-verify
			        """
					try {
						sh """
						    ${oc} project ${stageProjectName}
						"""         
					} catch (Exception e) {
						sh """
							${oc} new-project ${stageProjectName} --display-name="Staging"
							${oc} secrets new-dockercfg "nexus-${stageProjectName}" --docker-server=${nexusRegistryURL}/${nexusRepoUser} --docker-username=${env.NEXUS_USERNAME} --docker-password=${env.NEXUS_PASSWORD} --docker-email=${nexusEmail}
							${oc} secrets link default "nexus-${stageProjectName}" --for=pull
							${oc} secrets link builder "nexus-${stageProjectName}" --for=pull
							${oc} secrets link deployer "nexus-${stageProjectName}" --for=pull
						"""                
					}
		        
					try {
						sh """
						    ${oc} project ${productionProject}
						"""         
					} catch (Exception e) {
						sh """
							${oc} new-project ${prodProjectName} --display-name="Production"
							${oc} secrets new-dockercfg "nexus-${prodProjectName}" --docker-server=${nexusRegistryURL}/${nexusRepoUser} --docker-username=${env.NEXUS_USERNAME} --docker-password=${env.NEXUS_PASSWORD} --docker-email=${nexusEmail}
							${oc} secrets link default "nexus-${prodProjectName}" --for=pull
							${oc} secrets link builder "nexus-${prodProjectName}" --for=pull
							${oc} secrets link deployer "nexus-${prodProjectName}" --for=pull
						"""                
					}
		      	}
		    }
			
			stage ('Staging Deployment') {
				withCredentials([
					string(credentialsId: openShiftCredential, variable: 'OSTOKEN'),
				]) { 
	                sh """
	                	${oc} login ${osHost} --username=${env.OS_USERNAME} --password=${env.OS_PASSWORD} --insecure-skip-tls-verify
	                	${oc} project ${stageProjectName}
		                ${oc} process -f ${stageTemplateName} | ${oc} apply -f - -n ${stageProjectName}
                        ${oc} tag --source=docker ${nexusRegistryURL}/${nexusRepoUser}/${projectTagName}:${dockerImageVersion} ${stageProjectName}/${projectTagName}-is:${dockerImageVersion} --insecure
	                    sleep 5
		                ${oc} import-image ${projectTagName}-is:${dockerImageVersion} --confirm --insecure | grep -i "imported"
	                """
				  }
			}
			
			
         	 stage ('Zero Downtime Deployment (ZDD)') {
                withCredentials([
                     [$class: 'UsernamePasswordMultiBinding', credentialsId: nexusCredentialId, usernameVariable: 'NEXUS_USERNAME', passwordVariable: 'NEXUS_PASSWORD'],
                    string(credentialsId: openShiftCredential, variable: 'OSTOKEN'),
                    string(credentialsId: testConfigMapKeyValues, variable: 'PROD_CONFIG')
                    ]) {
                        sh """
                            ${oc} login --server=${openShiftHost} --token=${OSTOKEN}
                        """
                        try {
                            sh """
                                ${oc} project ${prodProjectName}
                            """         
                        } catch (Exception e) {
                            sh """
                                ${oc} new-project ${prodProjectName} --display-name="${prodProjectDisplayName}"
                                ${oc} secrets new-dockercfg "nexus-${prodProjectName}" --docker-server=${nexusRegistryURL}/${nexusRepoUser} --docker-username=${env.NEXUS_USERNAME} --docker-password=${env.NEXUS_PASSWORD} --docker-email=${nexusEmail}
                                ${oc} secrets link default "nexus-${prodProjectName}" --for=pull
                                ${oc} secrets link builder "nexus-${prodProjectName}" --for=pull
                                ${oc} secrets link deployer "nexus-${prodProjectName}" --for=pull
                            """                
                        }
                        try {
                            sh """
                                ${oc} create configmap ${osConfigMapName} $PROD_CONFIG
                            """         
                        } catch (Exception e) {
                        }
                        sh """
                            ${oc} process -f ${prodTemplateName} | ${oc} apply -f - -n ${prodProjectName}
                            ${oc} tag --source=docker ${nexusRegistryURL}/${nexusRepoUser}/${projectTagName}:${dockerImageVersion} ${prodProjectName}/${projectTagName}-is:${dockerImageVersion} --insecure
                            sleep 5
                            ${oc} import-image ${projectTagName}-is:${dockerImageVersion} --confirm --insecure | grep -i "imported"
                            sleep 30
                        """
                    }
         	
         	
				userInput = input(
		        id: 'userInput', message: 'Canary or Rolling deployment?', parameters: [
		        	[$class: 'ChoiceParameterDefinition', choices: 'ZDD Canary deployment\nZDD Rolling Deployment', description: 'ZDD Canary Deployment or ZDD Rolling Deployment', name: 'DEPLOYMENT_TYPE'],
		        ])
					withCredentials([
        			 string(credentialsId: openShiftCredential, variable: 'OSTOKEN')
      			])  {
	         		def exists = fileExists 'active_service.txt'
	         		if (exists) {
						echo '##### Not a First Time Deployment #####'
						sh """
							${oc} get route ${projectTagName} -n ${prodProjectName} -o jsonpath='{ .spec.to.name }' > active_service.txt
							cat active_service.txt
						"""
					} else {
    					echo '##### First Time Deployment #####'
    					sh """
    						${oc} tag --source=docker ${nexusRegistryURL}/${nexusRepoUser}/${projectTagName}:${dockerImageVersion} ${prodProjectName}/${green}-${projectTagName}-is:${dockerImageVersion} --insecure
							${oc} import-image ${green}-${projectTagName}-is:${dockerImageVersion} --confirm --insecure -n ${prodProjectName} | grep -i "imported"
							    						
    						${oc} tag --source=docker ${nexusRegistryURL}/${nexusRepoUser}/${projectTagName}:${dockerImageVersion} ${prodProjectName}/${blue}-${projectTagName}-is:${dockerImageVersion} --insecure
							${oc} import-image ${blue}-${projectTagName}-is:${dockerImageVersion} --confirm --insecure -n ${prodProjectName} | grep -i "imported"
							    						
							${oc} get route ${projectTagName} -n ${prodProjectName} -o jsonpath='{ .spec.to.name }' > active_service.txt
							cat active_service.txt
    						sleep 15
    						${oc} set -n ${prodProjectName} route-backends ${projectTagName} ${blue}-${projectTagName}-opr-svc=50 ${green}-${projectTagName}-opr-svc=50
    						sleep 15
						"""
					}
	      			activeService = readFile('active_service.txt').trim()
	       			if (activeService == "a-${projectTagName}-opr-svc") {
	       				blue = "a"
	       				green = "b"
	       			}
	       			
	       			if (userInput != "ZDD Rolling Deployment") {
						canaryDeployment = "true"
        			}
      			}
			}

			if (canaryDeployment) {
	      		stage ('ZDD Canary Deployment') {
	        		userInput = input(
	         			id: 'userInput', message: 'ZDD Canary Deployment?', parameters: [
	            		[$class: 'StringParameterDefinition', defaultValue: '25', description: 'New deployment weight', name: 'NEW_DEPLOYMENT_WEIGHT'],
	            		[$class: 'StringParameterDefinition', defaultValue: '75', description: 'Existing deployment weight', name: 'EXISTING_DEPLOYMENT_WEIGHT'],
	           		])
	        		blueWeight = userInput['EXISTING_DEPLOYMENT_WEIGHT']
	        		greenWeight = userInput['NEW_DEPLOYMENT_WEIGHT']
	        		withCredentials([
	          			string(credentialsId: openShiftCredential, variable: 'OSTOKEN')
	        		]) {
	          			sh """
           					${oc} tag --source=docker ${nexusRegistryURL}/${nexusRepoUser}/${projectTagName}:${dockerImageVersion} ${prodProjectName}/${green}-${projectTagName}-is:${dockerImageVersion} --insecure
          					sleep 5
           					${oc} import-image ${green}-${projectTagName}-is:${dockerImageVersion} --confirm --insecure -n ${prodProjectName} | grep -i "imported"
           					sleep 5          			
							${oc} login --server=${openShiftHost} -n ${prodProjectName} --token=${OSTOKEN}
	         				${oc} set -n ${prodProjectName} route-backends ${projectTagName} ${green}-${projectTagName}-opr-svc=${greenWeight} ${blue}-${projectTagName}-opr-svc=${blueWeight}
	          			"""
	        		}
	      		}
	      		
				stage('Healthcheck'){
      					 def retstat = 1
                timeout (time: 5, unit: 'MINUTES') {
                  for (;retstat != 0;) {
                    retstat = sh(
                    script: """
                    curl -I -k "https://`${oc} get route ${projectTagName} -n ${prodProjectName} -o jsonpath='{ .spec.host }'`${route}" | grep "HTTP/1.1 200" """, returnStatus: true)
                    if (retstat != 0) {
                      sleep 30
                    }
                  }
                }
                if (retstat != 0) {
                  echo "Health check to https://`${oc} get route ${projectTagName} -n ${stageProjectName} -o jsonpath='{ .spec.host }'`${route} failed."
                  sh exit ${retstat}
                }  
      		}
	      		
	      		stage ('Production ZDD Go Live or Rollback') {
			        userInput = input(
			           id: 'userInput', message: 'ZDD Go Live or ZDD Rollback?', parameters: [
			            [$class: 'ChoiceParameterDefinition', choices: 'ZDD Go Live\nZDD Rollback', description: 'ZDD Go Live to new deployment or ZDD Rollback to existing deployment', name: 'GO_LIVE_OR_ROLLBACK'],
			           ])
					withCredentials([
					string(credentialsId: openShiftCredential, variable: 'OSTOKEN') ])
					{
			        	sh """
			         		${oc} login --server=${openShiftHost} -n ${prodProjectName} --token=${OSTOKEN}
			         	"""
			          	if (userInput == "ZDD Rollback") {
				            sh """
				           		${oc} set -n ${prodProjectName} route-backends ${projectTagName} ${blue}-${projectTagName}-opr-svc=100 ${green}-${projectTagName}-opr-svc=0
 							"""              
			          	} else {
			            	sh """
			            		${oc} tag --source=docker ${nexusRegistryURL}/${nexusRepoUser}/${projectTagName}:${dockerImageVersion} ${prodProjectName}/${blue}-${projectTagName}-is:${dockerImageVersion} --insecure
          						sleep 5
           						${oc} import-image ${blue}-${projectTagName}-is:${dockerImageVersion} --confirm --insecure -n ${prodProjectName} | grep -i "imported"
           						sleep 5
			            		${oc} set -n ${prodProjectName} route-backends ${projectTagName} ${green}-${projectTagName}-opr-svc=50 ${blue}-${projectTagName}-opr-svc=50			             
				            """                            
			          }
			        }
		      }
      		}
      		else {
      			stage ('ZDD Rolling Deployment') {
		         	sh """
						${oc} tag --source=docker ${nexusRegistryURL}/${nexusRepoUser}/${projectTagName}:${dockerImageVersion} ${prodProjectName}/${blue}-${projectTagName}-is:${dockerImageVersion} --insecure
						sleep 5         					
						${oc} import-image ${blue}-${projectTagName}-is:${dockerImageVersion} --confirm --insecure -n ${prodProjectName} | grep -i "imported"
						sleep 5
						${oc} tag --source=docker ${nexusRegistryURL}/${nexusRepoUser}/${projectTagName}:${dockerImageVersion} ${prodProjectName}/${green}-${projectTagName}-is:${dockerImageVersion} --insecure
						sleep 5         					
						${oc} import-image ${green}-${projectTagName}-is:${dockerImageVersion} --confirm --insecure -n ${prodProjectName} | grep -i "imported"
						sleep 5
						${oc} set -n ${prodProjectName} route-backends ${projectTagName} ${blue}-${projectTagName}-opr-svc=50 ${green}-${projectTagName}-opr-svc=50    
					"""
				}
      		}
			
			stage('Healthcheck'){
      					 def retstat = 1
                timeout (time: 5, unit: 'MINUTES') {
                  for (;retstat != 0;) {
                    retstat = sh(
                    script: """
                    curl -I -k "https://`${oc} get route ${projectTagName} -n ${prodProjectName} -o jsonpath='{ .spec.host }'`${route}" | grep "HTTP/1.1 200" """, returnStatus: true)
                    if (retstat != 0) {
                      sleep 30
                    }
                  }
                }
                if (retstat != 0) {
                  echo "Health check to https://`${oc} get route ${projectTagName} -n ${prodProjectName} -o jsonpath='{ .spec.host }'`${route} failed."
                  sh exit ${retstat}
                }  
      		}
                      

        }
        notifySuccessful()
    } catch (e) {
        currentBuild.result = "FAILED"
        notifyFailed()
        throw e
    }
}

def notifyStarted() {
    //slackSend (color: '#FFFF00', message: "STARTED: Job '${env.JOB_NAME} [${env.BUILD_NUMBER}]' (${env.BUILD_URL})")
}
def notifySuccessful() {
    //slackSend (color: '#00FF00', message: "SUCCESSFUL: Job '${env.JOB_NAME} [${env.BUILD_NUMBER}]' (${env.BUILD_URL})")
}
def notifyFailed() {
    //slackSend (color: '#FF0000', message: "FAILED: Job '${env.JOB_NAME} [${env.BUILD_NUMBER}]' (${env.BUILD_URL})")
}
