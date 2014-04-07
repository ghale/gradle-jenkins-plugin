package com.terrafolio.gradle.plugins.jenkins.test.tasks

import com.terrafolio.gradle.plugins.jenkins.JenkinsPlugin
import com.terrafolio.gradle.plugins.jenkins.service.JenkinsRESTServiceImpl
import com.terrafolio.gradle.plugins.jenkins.tasks.DeleteJenkinsJobsTask
import groovy.mock.interceptor.MockFor
import org.gradle.api.Project
import org.gradle.testfixtures.ProjectBuilder
import org.junit.Before
import org.junit.Test

class DeleteJenkinsJobTaskTest {
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
	def void execute_deletesOneJob() {
		mockJenkinsRESTService.demand.with {
			getJobConfiguration() { String jobName, Map overrides -> "<project><actions></actions><description></description><keepDependencies>false</keepDependencies><properties></properties><scm class='hudson.scm.NullSCM'></scm><canRoam>true</canRoam><disabled>false</disabled><blockBuildWhenDownstreamBuilding>false</blockBuildWhenDownstreamBuilding><blockBuildWhenUpstreamBuilding>false</blockBuildWhenUpstreamBuilding><triggers class='vector'></triggers><concurrentBuild>false</concurrentBuild><builders></builders><publishers></publishers><buildWrappers></buildWrappers></project>"}
			deleteJob() { String jobName, Map overrides -> 
				if (! project.jenkins.jobs.collect { it.definition.name }.contains(jobName)) {
					throw new Exception('deleteJob received: ' + jobName + ' but there\'s no job definition with that name!')
				}
			}
		}
		
		project.task('deleteOneJob', type: DeleteJenkinsJobsTask) {
			delete(project.jenkins.jobs.compile_master)
		}
		
		mockJenkinsRESTService.use {
			project.tasks.deleteOneJob.execute()
		}
	}
	
	@Test
	def void execute_deletesOneJobTuple() {
		def jobToDelete = "${project.name} compile (master)"
		mockJenkinsRESTService.demand.with {
			getJobConfiguration() { String jobName, Map overrides -> "<project><actions></actions><description></description><keepDependencies>false</keepDependencies><properties></properties><scm class='hudson.scm.NullSCM'></scm><canRoam>true</canRoam><disabled>false</disabled><blockBuildWhenDownstreamBuilding>false</blockBuildWhenDownstreamBuilding><blockBuildWhenUpstreamBuilding>false</blockBuildWhenUpstreamBuilding><triggers class='vector'></triggers><concurrentBuild>false</concurrentBuild><builders></builders><publishers></publishers><buildWrappers></buildWrappers></project>"}
			deleteJob() { String jobName, Map overrides ->
				assert jobName == jobToDelete
			}
		}
		
		project.task('deleteOneJob', type: DeleteJenkinsJobsTask) {
			delete(project.jenkins.servers.test1, jobToDelete)
		}
		
		mockJenkinsRESTService.use {
			project.tasks.deleteOneJob.execute()
		}
	}
	
	def void execute_deletesMultipleJobs() {
		def jobToDelete = "${project.name} compile (master)"
		mockJenkinsRESTService.demand.with {
			2.times {
				getJobConfiguration() { String jobName -> "<project><actions></actions><description></description><keepDependencies>false</keepDependencies><properties></properties><scm class='hudson.scm.NullSCM'></scm><canRoam>true</canRoam><disabled>false</disabled><blockBuildWhenDownstreamBuilding>false</blockBuildWhenDownstreamBuilding><blockBuildWhenUpstreamBuilding>false</blockBuildWhenUpstreamBuilding><triggers class='vector'></triggers><concurrentBuild>false</concurrentBuild><builders></builders><publishers></publishers><buildWrappers></buildWrappers></project>"}
				deleteJob() { String jobName ->
					assert jobName == jobToDelete
				}
			}
		}
		
		project.task('deleteMultipleJobs', type: DeleteJenkinsJobsTask) {
			delete(project.jenkins.servers.test1, jobToDelete)
			delete(project.jenkins.servers.test2, jobToDelete)
		}
		
		mockJenkinsRESTService.use {
			project.tasks.deleteMultipleJobs.execute()
		}
	}
	
	@Test
	def void execute_deletesJobWithOverrides() {
		mockJenkinsRESTService.demand.with {
			getJobConfiguration() { String jobName, Map overrides -> "<project><actions></actions><description></description><keepDependencies>false</keepDependencies><properties></properties><scm class='hudson.scm.NullSCM'></scm><canRoam>true</canRoam><disabled>false</disabled><blockBuildWhenDownstreamBuilding>false</blockBuildWhenDownstreamBuilding><blockBuildWhenUpstreamBuilding>false</blockBuildWhenUpstreamBuilding><triggers class='vector'></triggers><concurrentBuild>false</concurrentBuild><builders></builders><publishers></publishers><buildWrappers></buildWrappers></project>"}
			deleteJob() { String jobName, Map overrides ->
				if (! project.jenkins.jobs.collect { it.definition.name }.contains(jobName)) {
					throw new Exception('deleteJob received: ' + jobName + ' but there\'s no job definition with that name!')
				}
				assert overrides.uri == "testUri"
			}
		}
		
		project.jenkins.jobs.compile_master {
			serviceOverrides {
				delete = [ uri: "testUri" ]
			}
		}
		
		project.task('deleteOneJob', type: DeleteJenkinsJobsTask) {
			delete(project.jenkins.jobs.compile_master)
		}
		
		mockJenkinsRESTService.use {
			project.tasks.deleteOneJob.execute()
		}
	}
	
}
