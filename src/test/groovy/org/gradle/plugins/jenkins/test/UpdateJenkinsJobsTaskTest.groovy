package org.gradle.plugins.jenkins.test;

import static org.junit.Assert.*

import org.gradle.api.Project
import org.junit.Test
import org.junit.Before
import org.gradle.testfixtures.ProjectBuilder

import org.gradle.plugins.jenkins.JenkinsPlugin
import org.gradle.plugins.jenkins.JenkinsRESTServiceImpl
import org.gradle.plugins.jenkins.JenkinsServiceException

import groovy.mock.interceptor.MockFor

class UpdateJenkinsJobsTaskTest {
	def private final Project project = ProjectBuilder.builder().withProjectDir(new File('build/tmp/test')).build()
	def private final JenkinsPlugin plugin = new JenkinsPlugin()
	def MockFor mockJenkinsRESTService
	
	@Before
	def void setupProject() {
		plugin.apply(project)
		
		project.ext.branches = [
			master: [ parents: [ ] ],
			develop: [ parents: [ 'master' ] ]
		]
		
		project.jenkins {
			templates {
				compile {
					xml "<project><actions></actions><description></description><keepDependencies>false</keepDependencies><properties></properties><scm class='hudson.scm.NullSCM'></scm><canRoam>true</canRoam><disabled>false</disabled><blockBuildWhenDownstreamBuilding>false</blockBuildWhenDownstreamBuilding><blockBuildWhenUpstreamBuilding>false</blockBuildWhenUpstreamBuilding><triggers class='vector'></triggers><concurrentBuild>false</concurrentBuild><builders></builders><publishers></publishers><buildWrappers></buildWrappers></project>"
				}
			}
			jobs {
				project.branches.eachWithIndex { branchName, map, index ->
					"compile_${branchName}" {
						definition {
							name "${project.name} compile (${branchName})"
							xml templates.compile.xml
						}
					}
				}
			}
		}
		
		mockJenkinsRESTService = new MockFor(JenkinsRESTServiceImpl.class)
	}
	
	@Test
	def void execute_updatesExistingJob() {
		mockJenkinsRESTService.demand.with {
			createJob(0) { String jobName, String configXML -> }
			
			getJobConfiguration() { String jobName ->
				"<project><actions></actions><description></description><keepDependencies>false</keepDependencies><properties></properties><scm class='hudson.scm.NullSCM'></scm><canRoam>true</canRoam><disabled>false</disabled><blockBuildWhenDownstreamBuilding>false</blockBuildWhenDownstreamBuilding><blockBuildWhenUpstreamBuilding>false</blockBuildWhenUpstreamBuilding><triggers class='vector'></triggers><concurrentBuild>false</concurrentBuild><builders></builders><publishers></publishers><buildWrappers></buildWrappers></project>"
			}
			
			updateJobConfiguration() { String jobName, String configXML -> }
			
			getJobConfiguration() { String jobName ->
				"<project><actions></actions><description></description><keepDependencies>false</keepDependencies><properties></properties><scm class='hudson.scm.NullSCM'></scm><canRoam>true</canRoam><disabled>false</disabled><blockBuildWhenDownstreamBuilding>false</blockBuildWhenDownstreamBuilding><blockBuildWhenUpstreamBuilding>false</blockBuildWhenUpstreamBuilding><triggers class='vector'></triggers><concurrentBuild>false</concurrentBuild><builders></builders><publishers></publishers><buildWrappers></buildWrappers></project>"
			}
			
			updateJobConfiguration() { String jobName, String configXML -> }
		}
		
		mockJenkinsRESTService.use {
			project.tasks.updateJenkinsJobs.execute()
		}
	}
	
	@Test
	def void execute_createsNewJob() {
		mockJenkinsRESTService.demand.with {
			updateJobConfiguration(0) { String jobName, String configXML -> }
			
			getJobConfiguration() { String jobName ->
				null
			}
			
			createJob() { String jobName, String configXML -> }
			
			getJobConfiguration() { String jobName ->
				null
			}
			
			createJob() { String jobName, String configXML -> }
		}
		
		mockJenkinsRESTService.use {
			project.tasks.updateJenkinsJobs.execute()
		}
	}
}
