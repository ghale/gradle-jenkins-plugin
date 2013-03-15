package com.terrafolio.gradle.plugins.jenkins.test

import static org.junit.Assert.*
import groovy.lang.GroovyObject;
import groovy.mock.interceptor.MockFor
import groovy.xml.StreamingMarkupBuilder

import org.custommonkey.xmlunit.Diff
import org.custommonkey.xmlunit.XMLUnit
import org.gradle.api.Project
import org.gradle.testfixtures.ProjectBuilder
import org.junit.Before
import org.junit.Test

import com.terrafolio.gradle.plugins.jenkins.JenkinsPlugin
import com.terrafolio.gradle.plugins.jenkins.ConsoleFactory

class DumpJenkinsJobsTaskTest {
	def private final Project project = ProjectBuilder.builder().withProjectDir(new File('build/tmp/test')).build()
	def private final JenkinsPlugin plugin = new JenkinsPlugin()

	@Before
	def void setupProject() {
		plugin.apply(project)

		project.jenkins {
			servers {
				test1 {
					url 'test1'
					username 'test1'
					password 'test1'
				}
			}
			
			templates {
				compile { xml "<?xml version='1.0' encoding='UTF-8'?><project><actions></actions><description></description><keepDependencies>false</keepDependencies><properties></properties><scm class='hudson.scm.NullSCM'></scm><canRoam>true</canRoam><disabled>false</disabled><blockBuildWhenDownstreamBuilding>false</blockBuildWhenDownstreamBuilding><blockBuildWhenUpstreamBuilding>false</blockBuildWhenUpstreamBuilding><triggers class='vector'></triggers><concurrentBuild>false</concurrentBuild><builders></builders><publishers></publishers><buildWrappers></buildWrappers></project>" }
			}
			
			jobs {
				job1 {
					server servers.test1
					definition {
						name "job1"
						xml templates.compile.xml
					}
				}

				job2 {
					server servers.test1
					definition {
						name "job2"
						xml templates.compile.xml
					}
				}
			}
		}
	}

	@Test
	def void execute_dumpsJobsToFiles() {
		project.tasks.dumpJenkinsJobs.execute()
		
		def dumpDir = new File('build/tmp/test/build/jobs')
		project.jenkins.jobs.each { job ->
			def jobFile = new File(dumpDir, "${job.name}-config.xml")
			assert jobFile.exists()
			
			XMLUnit.setIgnoreWhitespace(true)
			def xmlDiff = new Diff(job.definition.xml, jobFile.getText())
			assert xmlDiff.similar()
		}
	}
	
	@Test
	def void execute_doesNotPromptForCredentials() {
		def mockConsoleFactory = new MockFor(ConsoleFactory.class)
		mockConsoleFactory.demand.with {
			getConsole(0)
		}
		
		project.jenkins.servers.each { server ->
			server.username = null
			server.password = null
		}
		
		mockConsoleFactory.use {
			project.tasks.dumpJenkinsJobs.execute()
		}
	}
	
	@Test
	def void execute_dumpsRawJobsToFile() {
		project.tasks.dumpJenkinsJobs.prettyPrint = false
		project.tasks.dumpJenkinsJobs.execute()
		
		def dumpDir = new File('build/tmp/test/build/jobs')
		project.jenkins.jobs.each { job ->
			def jobFile = new File(dumpDir, "${job.name}-config.xml")
			assert jobFile.exists()
			assert jobFile.getText() == job.definition.xml
		}
	}
}
