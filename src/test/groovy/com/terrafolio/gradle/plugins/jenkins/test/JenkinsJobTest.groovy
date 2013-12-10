package com.terrafolio.gradle.plugins.jenkins.test

import static org.junit.Assert.*

import org.gradle.api.Project
import org.gradle.testfixtures.ProjectBuilder

import org.junit.Test

import com.terrafolio.gradle.plugins.jenkins.JenkinsPlugin

class JenkinsJobTest {
	def private final Project project = ProjectBuilder.builder().withProjectDir(new File('build/tmp/test')).build()
	def private final JenkinsPlugin plugin = new JenkinsPlugin()
	
	@Test
	def void configure_closureAddsToDefinition() {
		def testXml = '<test>test</test>'
		plugin.apply(project)
		project.jenkins {
			jobs {
				test {
					definition {
						xml testXml 
					}
				}
			}
		}
		project.jenkins.jobs.test.definition {
			name "test name"
		}
		assert project.jenkins.jobs.test.definition.name == "test name"
		assert project.jenkins.jobs.test.definition.xml == testXml
	}
	
	
	
	@Test
	def void configure_configuresServiceOverrides() {
		def testXml = '<test>test</test>'
		plugin.apply(project)
		project.jenkins {
			jobs {
				test {
					definition {
						xml testXml
					}
					serviceOverrides {
						get([ uri: "getTest", params: [ test: "testGetParam"] ])
						create([ uri: "createTest", params: [ test: "testCreateParam"] ])
						update([ uri: "updateTest", params: [ test: "testUpdateParam"] ])
						delete([ uri: "deleteTest", params: [ test: "testDeleteParam"] ])
					}
				}
			}
		}

		assert project.jenkins.jobs.test.serviceOverrides.get.uri == "getTest"
		assert project.jenkins.jobs.test.serviceOverrides.get.params.test == "testGetParam"
		assert project.jenkins.jobs.test.serviceOverrides.create.uri == "createTest"
		assert project.jenkins.jobs.test.serviceOverrides.create.params.test == "testCreateParam"
		assert project.jenkins.jobs.test.serviceOverrides.update.uri == "updateTest"
		assert project.jenkins.jobs.test.serviceOverrides.update.params.test == "testUpdateParam"
		assert project.jenkins.jobs.test.serviceOverrides.delete.uri == "deleteTest"
		assert project.jenkins.jobs.test.serviceOverrides.delete.params.test == "testDeleteParam"
	}
	
}
