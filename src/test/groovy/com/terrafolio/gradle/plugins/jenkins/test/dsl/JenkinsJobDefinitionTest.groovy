package com.terrafolio.gradle.plugins.jenkins.test.dsl

import com.terrafolio.gradle.plugins.jenkins.JenkinsPlugin
import org.gradle.api.Project
import org.gradle.testfixtures.ProjectBuilder
import org.junit.Before
import org.junit.Test

class JenkinsJobDefinitionTest {
	def private final Project project = ProjectBuilder.builder().withProjectDir(new File('build/tmp/test')).build()
	def private final JenkinsPlugin plugin = new JenkinsPlugin()
	
	@Before
	def void setupProject() {
		plugin.apply(project)
	}
	
	@Test
	def void configure_setsName() {
		project.jenkins {
			jobs {
				test {
					definition {
						name 'Test Job'
					}
				}
			}
		}
		
		assert project.jenkins.jobs.findByName('test').definition.name == 'Test Job'
	}
	
	@Test
	def void configure_xmlAsString() {
		project.jenkins {
			jobs {
				test {
					definition {
						name 'Test Job'
						xml '<test><test2>value</test2></test>'
					}
				}
			}
		}
		
		assert project.jenkins.jobs.findByName('test').definition.xml == '<test><test2>value</test2></test>'
	}
	
	@Test
	def void configure_xmlAsFile() {
		def xmlFile = project.file('test.xml')
		xmlFile.write('<test><test2>value</test2></test>')
	
		project.jenkins {
			jobs {
				test {
					definition {
						name 'Test Job'
						xml xmlFile
					}
				}
			}
		}
		
		assert project.jenkins.jobs.findByName('test').definition.xml == '<test><test2>value</test2></test>'
	}
	
	@Test
	def void configure_xmlAsClosure() {
		project.jenkins {
			jobs {
				test {
					definition {
						name 'Test Job'
						xml {
							test1() {
								test2('value')
							}
						}
					}
				}
			}
		}
		
		assert project.jenkins.jobs.findByName('test').definition.xml == '<test1><test2>value</test2></test1>'
	}
	
	@Test
	def void configure_overridesXml() {
		project.jenkins {
			templates {
				test {
					xml "<test><test2>value</test2></test>"
				}
			}
			jobs {
				test {
					definition {
						name 'Test Job'
						xml templates.test.override { test ->
							test.test2 = 'myvalue'
						}
					}
				}
			}
		}
		
		assert project.jenkins.jobs.findByName('test').definition.xml == '<test><test2>myvalue</test2></test>'
	}

	@Test
	def void configure_defaultsJobDefinitionNameToJobName() {
		project.jenkins {
			templates {
				test {
					xml "<test><test2>value</test2></test>"
				}
			}
			jobs {
				test2 {
					definition {
						xml templates.test.xml
					}
				}
			}
		}
		
		assert project.jenkins.jobs.test2.definition.name == 'test2'
	}

}
