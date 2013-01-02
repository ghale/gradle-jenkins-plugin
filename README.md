gradle-jenkins-plugin
=====================

Gradle plugin to programmatically configure Jenkins jobs.  This plugin allows you to maintain jenkins job configurations in source control and apply them to the server via gradle.  Jobs can be stored as straight xml files, xml strings, or as markup builder closures.  Job templates can be defined that can then be manipulated in Groovy XmlSlurper fashion such that multiple jobs can be generated off of a single template definition.

Examples
========
This first example shows a very simple case where the jenkins job is stored as an xml file and applied to the server unchanged.

	apply plugin: 'jenkins'
	
	jenkins {
	        servers {
	                testing {
	                        url 'http://jenkins.somewhere.com:8080'
	                        secure true         // optional
	                        username "testuser" // optional
	                        password "testpass" // optional
	                }
	        }
	        
	        defaultServer servers.testing // optional
	        jobs {
	        		test {
	        				server servers.testing
	        				definition {
	        					name "Build ${project.name}"
	        					xml file('config.xml')
	        				}
	        		}
	        }
	}
	        			
	
The following gradle file demonstrates the use of templates.  It uses a single template xml file (downloaded straight from <JOB_URL>/api/config.xml) and then generates two new jobs - one for the master branch of a git repository and one for the develop branch.  For each, it uses a closure to override the template to disable the job, set a custom workspace, set the repository url, and set the branch name. 

	apply plugin: 'jenkins'
	
	jenkins {
	        servers {
	                testing {
	                        url 'http://jenkins.somewhere.com:8080'
	                        secure true         // optional
	                        username "testuser" // optional
	                        password "testpass" // optional
	                }
	        }
	
	        templates {
	                build {
	                        xml file('build-template.xml')
	                }
	        } 
	
	        defaultServer servers.testing // optional 
	        jobs {
	                [ 'master', 'develop' ].each { branchName ->
	                        "build_${branchName}" {
	                        		server servers.testing // optional
	                                definition {
	                                        name "Build ${project.name} (${branchName})"
	                                        xml templates.build.override { projectXml ->
	                                                projectXml.disabled = 'true'
	                                                projectXml.customWorkspace = "/build/${branchName}/${project.name}"
	                                                projectXml.scm.userRemoteConfigs.'hudson.plugins.git.UserRemoteConfig'.url = "git@gitserver:${project.name}.git"
	                                                projectXml.scm.branches.replaceNode { node ->
	                                                        branches() {
	                                                                'hudson.plugins.git.BranchSpec'() {
	                                                                        name([:], "*/${branchName}")
	                                                                }
	                                                        }
	                                                }
	                                        }
	                                }
	                        }
	                }
	        }
	}


Configuration
=============

- *jenkins* - The main configuration closure contains servers, templates, jobs and (optionally) a defaultServer definition.  If a defaultServer is not defined, then each job must specify the server it should be applied to.

- *servers* - Definitions of jenkins servers where jobs can be applied.  Each named server can define several fields: 
	- *url* - The url to the jenkins instance. 
	- *username* - The username to use (must have admin privileges). (Optional)
	- *password* - The password to use. (Optional
	- *secure* - Whether or not the server is secured (requiring username and password). (Optional, default = true)
	If Username and password are not defined, and secure is set to true, they will be prompted for on the console.  If no console is available, an exception will be thrown.  In the event that the secure field is set to false, empty values for username and password are allowed and no prompting will occur.

- *templates* - Definitions of jobs that can be used as templates for concrete jobs.  Each named template defines one field:
	- *xml* - The config.xml to use as a template for defining jobs.  This field can accept a String, a File, or a Groovy MarkupBuilder closure.

- *jobs* - Definitions of concrete jobs.  Each named job defines several fields:
	- *server* - The server the job should be applied to and the job definition.  If the server is not configured it will use the server defined by defaultServer.  
	- *definition* - The definition of the actual job.  This closure defines the following fields:
		- *name* - The name of the job on the Jenkins server. 
		- *xml* - The config.xml that defines the job.  This field accepts a String, a File, or a Groovy MarkupBuilder closure.  Additionally, if a template is defined, you can use the override method on the template which accepts a closure to manipulate the content.  The GPathResult from an XMLSlurper is passed to the closure for manipulation.  See http://groovy.codehaus.org/Updating+XML+with+XmlSlurper for info on using XMLSlurper GPathResults to manipulate xml.


Tasks
=====
The plugin applies two tasks to the project.  Both tasks operate on all defined jobs. 
- *updateJenkinsJobs* - Creates or updates an existing job on the server.  
- *deleteJenkinsJobs* - Deletes the jobs from the server.  


              
