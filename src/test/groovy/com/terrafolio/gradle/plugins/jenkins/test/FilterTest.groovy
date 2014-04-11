package com.terrafolio.gradle.plugins.jenkins.test

import com.terrafolio.gradle.plugins.jenkins.JenkinsPlugin
import com.terrafolio.gradle.plugins.jenkins.service.JenkinsRESTServiceImpl
import groovy.mock.interceptor.MockFor
import org.gradle.api.Project
import org.gradle.testfixtures.ProjectBuilder
import org.junit.Before
import org.junit.Test

class FilterTest {
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
	def void execute_filtersServers() {
		mockJenkinsRESTService.demand.with {
			updateConfiguration(0) { String jobName, String configXML -> }
			
			2.times {
				getConfiguration() { String jobName, Map overrides ->
					null
				}
				
				createConfiguration() { String jobName, String configXML, Map overrides ->
					if (! project.jenkins.jobs.collect { it.definition.name }.contains(jobName)) {
						throw new Exception('createConfiguration called with: ' + jobName + ' but no job definition exists with that name!')
					}
				}
			
			}
		}
		
		project.jenkinsServerFilter = 'test1'
		project.jenkins.jobs.each { job ->
			job.server project.jenkins.servers.test2
		}
		
		mockJenkinsRESTService.use {
			assert [ "test1" ] == project.tasks.updateJenkinsJobs.getServerDefinitions(project.jenkins.jobs."compile_master").collect { it.name }
			project.tasks.updateJenkinsJobs.execute()
		}
	}
	
	@Test
	def void execute_filtersJobs() {
		mockJenkinsRESTService.demand.with {
			updateConfiguration(0) { String jobName, String configXML, Map Overrides -> }
			
			2.times {
				getConfiguration() { String jobName, Map overrides ->
					null
				}
				
				createConfiguration() { String jobName, String configXML, Map overrides ->
					assert jobName =~ /master/
					if (! project.jenkins.jobs.collect { it.definition.name }.contains(jobName)) {
						throw new Exception('createConfiguration called with: ' + jobName + ' but no job definition exists with that name!')
					}
				}
			
			}
		}
		
		project.jenkinsJobFilter = '.*_master'
		project.jenkins.jobs.each { job ->
			job.server project.jenkins.servers.test2
		}
		
		mockJenkinsRESTService.use {
			project.tasks.updateJenkinsJobs.execute()
		}
	} 
}
