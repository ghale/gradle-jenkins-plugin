package com.terrafolio.gradle.plugins.jenkins.test.tasks

import groovy.mock.interceptor.MockFor;

import org.gradle.api.Project
import org.junit.Test
import org.junit.Before
import org.gradle.testfixtures.ProjectBuilder
import org.gradle.api.tasks.TaskExecutionException

import com.terrafolio.gradle.plugins.jenkins.JenkinsPlugin
import com.terrafolio.gradle.plugins.jenkins.ConsoleFactory
import com.terrafolio.gradle.plugins.jenkins.dsl.JenkinsConfigurationException;
import com.terrafolio.gradle.plugins.jenkins.service.JenkinsRESTServiceImpl;
import com.terrafolio.gradle.plugins.jenkins.service.JenkinsServiceException;
import com.terrafolio.gradle.plugins.jenkins.tasks.JenkinsValidationException;
import com.terrafolio.gradle.plugins.jenkins.tasks.ValidateJenkinsJobsTask;

class ValidateJenkinsJobsTaskTest {
	def private final Project project = ProjectBuilder.builder().withProjectDir(new File('build/tmp/test')).build()
	def private final JenkinsPlugin plugin = new JenkinsPlugin()
	def MockFor mockJenkinsRESTService
	
	@Before
	def void setupProject() {
		plugin.apply(project)
		
		project.ext.branches = [
			master: [ parents: [ ] ],
			develop: [ parents: [ 'master' ] ],
			releaseX: []
		]
		
		project.jenkins {
			servers {
				test1 {
					url 'test1'
					username 'test1'
					password 'test1'
				}
				test2 {
					url 'test2'
					username 'test2'
					password 'test2'
				}
			}
			templates {
				compile {
					xml "<project><actions></actions><description></description><keepDependencies>false</keepDependencies><properties></properties><scm class='hudson.scm.NullSCM'></scm><canRoam>true</canRoam><disabled>false</disabled><blockBuildWhenDownstreamBuilding>false</blockBuildWhenDownstreamBuilding><blockBuildWhenUpstreamBuilding>false</blockBuildWhenUpstreamBuilding><triggers class='vector'></triggers><concurrentBuild>false</concurrentBuild><builders></builders><publishers></publishers><buildWrappers></buildWrappers></project>"
				}
			}
			jobs {
				project.branches.eachWithIndex { branchName, map, index ->
					"compile_${branchName}" {
						server servers.test1
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
	def void execute_SucceedsOnNoDifference() {
		mockJenkinsRESTService.demand.with {
			getJobConfiguration(3) { String jobName, Map overrides ->
				"<project><actions></actions><description></description><keepDependencies>false</keepDependencies><properties></properties><scm class='hudson.scm.NullSCM'></scm><canRoam>true</canRoam><disabled>false</disabled><blockBuildWhenDownstreamBuilding>false</blockBuildWhenDownstreamBuilding><blockBuildWhenUpstreamBuilding>false</blockBuildWhenUpstreamBuilding><triggers class='vector'></triggers><concurrentBuild>false</concurrentBuild><builders></builders><publishers></publishers><buildWrappers></buildWrappers></project>"
			}
		}
		
		mockJenkinsRESTService.use {
			project.tasks.validateJenkinsJobs.execute()
		}
	}
	
	@Test (expected = JenkinsValidationException)
	def void execute_FailsOnDifference() {
		mockJenkinsRESTService.demand.with {
			getJobConfiguration(3) { String jobName, Map overrides ->
				"<project><actions></actions><description></description><keepDependencies>true</keepDependencies><properties></properties><scm class='hudson.scm.NullSCM'></scm><canRoam>true</canRoam><disabled>false</disabled><blockBuildWhenDownstreamBuilding>false</blockBuildWhenDownstreamBuilding><blockBuildWhenUpstreamBuilding>false</blockBuildWhenUpstreamBuilding><triggers class='vector'></triggers><concurrentBuild>false</concurrentBuild><builders></builders><publishers></publishers><buildWrappers></buildWrappers></project>"
			}
		}
		
		mockJenkinsRESTService.use {
			try {
				project.tasks.validateJenkinsJobs.execute()
			} catch (TaskExecutionException e) {
				throw e.cause
			}
		}
	}
	
	@Test 
	def void execute_SucceedsWhenFailOnDifferenceFalse() {
		mockJenkinsRESTService.demand.with {
			getJobConfiguration(3) { String jobName, Map overrides ->
				"<project><actions></actions><description>difference</description><keepDependencies>true</keepDependencies><properties></properties><scm class='hudson.scm.NullSCM'></scm><canRoam>true</canRoam><disabled>false</disabled><blockBuildWhenDownstreamBuilding>false</blockBuildWhenDownstreamBuilding><blockBuildWhenUpstreamBuilding>false</blockBuildWhenUpstreamBuilding><triggers class='vector'></triggers><concurrentBuild>false</concurrentBuild><builders></builders><publishers></publishers><buildWrappers></buildWrappers></project>"
			}
		}
		
		project.tasks.validateJenkinsJobs.failOnDifference = false
		mockJenkinsRESTService.use {
			try {
				project.tasks.validateJenkinsJobs.execute()
			} catch (TaskExecutionException e) {
				throw e.cause
			}
		}
	}
}
