package com.terrafolio.gradle.plugins.jenkins.test;

import static org.junit.Assert.*

import org.gradle.api.Project
import org.junit.Test
import org.junit.Before
import org.gradle.testfixtures.ProjectBuilder
import org.gradle.api.tasks.TaskExecutionException

import com.terrafolio.gradle.plugins.jenkins.JenkinsPlugin
import com.terrafolio.gradle.plugins.jenkins.JenkinsRESTServiceImpl
import com.terrafolio.gradle.plugins.jenkins.JenkinsServiceException
import com.terrafolio.gradle.plugins.jenkins.JenkinsConfigurationException

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
	def void execute_updatesExistingJob() {
		mockJenkinsRESTService.demand.with {
			createJob(0) { String jobName, String configXML -> }
			
			2.times {
				getJobConfiguration() { String jobName ->
					"<project><actions></actions><description></description><keepDependencies>false</keepDependencies><properties></properties><scm class='hudson.scm.NullSCM'></scm><canRoam>true</canRoam><disabled>false</disabled><blockBuildWhenDownstreamBuilding>false</blockBuildWhenDownstreamBuilding><blockBuildWhenUpstreamBuilding>false</blockBuildWhenUpstreamBuilding><triggers class='vector'></triggers><concurrentBuild>false</concurrentBuild><builders></builders><publishers></publishers><buildWrappers></buildWrappers></project>"
				}
				
				updateJobConfiguration() { String jobName, String configXML -> 
					if (! project.jenkins.jobs.collect { it.definition.name }.contains(jobName)) {
						throw new Exception('updateJobConfiguration called with: ' + jobName + ' but no job definition exists with that name!')
					}
				}
			}
		}
		
		mockJenkinsRESTService.use {
			project.tasks.updateJenkinsJobs.execute()
		}
	}
	
	@Test
	def void execute_createsNewJob() {
		mockJenkinsRESTService.demand.with {
			updateJobConfiguration(0) { String jobName, String configXML -> }
			
			2.times {
				getJobConfiguration() { String jobName ->
					null
				}
				
				createJob() { String jobName, String configXML -> 
					if (! project.jenkins.jobs.collect { it.definition.name }.contains(jobName)) {
						throw new Exception('createJob called with: ' + jobName + ' but no job definition exists with that name!')
					}
				}
			}
		}
		
		mockJenkinsRESTService.use {
			project.tasks.updateJenkinsJobs.execute()
		}
	}
	
	@Test
	def void execute_runsOnAllServers() {
		mockJenkinsRESTService.demand.with {
			updateJobConfiguration(0) { String jobName, String configXML -> }
			
			4.times {
				getJobConfiguration() { String jobName ->
					null
				}
				
				createJob() { String jobName, String configXML -> 
					if (! project.jenkins.jobs.collect { it.definition.name }.contains(jobName)) {
						throw new Exception('createJob called with: ' + jobName + ' but no job definition exists with that name!')
					}
				}
			
			}
		}
		
		project.jenkins.jobs.each { job ->
			job.server project.jenkins.servers.test2
		}
		
		mockJenkinsRESTService.use {
			project.tasks.updateJenkinsJobs.execute()
		}
	}
	
	@Test
	def void execute_usesDefaultServer() {
		mockJenkinsRESTService.demand.with {
			updateJobConfiguration(0) { String jobName, String configXML -> }
			
			2.times {
				getJobConfiguration() { String jobName ->
					null
				}
				
				createJob() { String jobName, String configXML ->
					if (! project.jenkins.jobs.collect { it.definition.name }.contains(jobName)) {
						throw new Exception('createJob called with: ' + jobName + ' but no job definition exists with that name!')
					}
				}
			
			}
		}
		
		project.jenkins {
			defaultServer project.jenkins.servers.test1
		}
		
		project.jenkins.jobs.each { job ->
			job.serverDefinitions = []
		}
		
		mockJenkinsRESTService.use {
			project.tasks.updateJenkinsJobs.execute()
		}
	}
	
	@Test(expected = JenkinsConfigurationException)
	def void execute_throwsExceptionOnMissingServer() {
		project.jenkins.jobs.each { job ->
			job.serverDefinitions = []
		}
		
		mockJenkinsRESTService.use {
			try {
				project.tasks.updateJenkinsJobs.execute()
			} catch (TaskExecutionException e) {
				throw e.cause
			} 
		}
	}
	
	@Test(expected = JenkinsConfigurationException)
	def void execute_throwsExceptionForMissingURL() {
		mockJenkinsRESTService.demand.with {
			updateJobConfiguration(0) { String jobName, String configXML -> }
			
			2.times {
				getJobConfiguration() { String jobName ->
					null
				}
				
				createJob() { String jobName, String configXML ->
					if (! project.jenkins.jobs.collect { it.definition.name }.contains(jobName)) {
						throw new Exception('createJob called with: ' + jobName + ' but no job definition exists with that name!')
					}
				}
			
			}
		}
		
		project.jenkins.servers.each { server ->
			server.url = null
		}
		
		mockJenkinsRESTService.use {
			try {
				project.tasks.updateJenkinsJobs.execute()
			} catch (TaskExecutionException e) {
				throw e.cause
			} 
		}
	}
	
	@Test(expected = JenkinsConfigurationException)
	def void execute_throwsExceptionForMissingUsername() {
		mockJenkinsRESTService.demand.with {
			updateJobConfiguration(0) { String jobName, String configXML -> }
			
			2.times {
				getJobConfiguration() { String jobName ->
					null
				}
				
				createJob() { String jobName, String configXML ->
					if (! project.jenkins.jobs.collect { it.definition.name }.contains(jobName)) {
						throw new Exception('createJob called with: ' + jobName + ' but no job definition exists with that name!')
					}
				}
			
			}
		}
		
		project.jenkins.servers.each { server ->
			server.username = null
		}
		
		mockJenkinsRESTService.use {
			try {
				project.tasks.updateJenkinsJobs.execute()
			} catch (TaskExecutionException e) {
				throw e.cause
			}
		}
	}
	
	@Test(expected = JenkinsConfigurationException)
	def void execute_throwsExceptionForMissingPassword() {
		mockJenkinsRESTService.demand.with {
			updateJobConfiguration(0) { String jobName, String configXML -> }
			
			2.times {
				getJobConfiguration() { String jobName ->
					null
				}
				
				createJob() { String jobName, String configXML ->
					if (! project.jenkins.jobs.collect { it.definition.name }.contains(jobName)) {
						throw new Exception('createJob called with: ' + jobName + ' but no job definition exists with that name!')
					}
				}
			
			}
		}
		
		project.jenkins.servers.each { server ->
			server.password = null
		}
		
		mockJenkinsRESTService.use {
			try {
				project.tasks.updateJenkinsJobs.execute()
			} catch (TaskExecutionException e) {
				throw e.cause
			}
		}
	}
	
	@Test
	def void execute_allowsMissingUsernameForInsecureServer() {
		mockJenkinsRESTService.demand.with {
			updateJobConfiguration(0) { String jobName, String configXML -> }
			
			2.times {
				getJobConfiguration() { String jobName ->
					null
				}
				
				createJob() { String jobName, String configXML ->
					if (! project.jenkins.jobs.collect { it.definition.name }.contains(jobName)) {
						throw new Exception('createJob called with: ' + jobName + ' but no job definition exists with that name!')
					}
				}
			
			}
		}
		
		project.jenkins.servers.each { server ->
			server.secure = false
			server.username = null
		}
		
		mockJenkinsRESTService.use {
			try {
				project.tasks.updateJenkinsJobs.execute()
			} catch (TaskExecutionException e) {
				throw e.cause
			}
		}
	}
	
	@Test
	def void execute_allowsMissingPasswordForInsecureServer() {
		mockJenkinsRESTService.demand.with {
			updateJobConfiguration(0) { String jobName, String configXML -> }
			
			2.times {
				getJobConfiguration() { String jobName ->
					null
				}
				
				createJob() { String jobName, String configXML ->
					if (! project.jenkins.jobs.collect { it.definition.name }.contains(jobName)) {
						throw new Exception('createJob called with: ' + jobName + ' but no job definition exists with that name!')
					}
				}
			
			}
		}
		
		project.jenkins.servers.each { server ->
			server.secure = false
			server.password = null
		}
		
		mockJenkinsRESTService.use {
			try {
				project.tasks.updateJenkinsJobs.execute()
			} catch (TaskExecutionException e) {
				throw e.cause
			}
		}
	}
}
