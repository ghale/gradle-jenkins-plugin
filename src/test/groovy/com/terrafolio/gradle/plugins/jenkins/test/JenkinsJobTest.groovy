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
}
