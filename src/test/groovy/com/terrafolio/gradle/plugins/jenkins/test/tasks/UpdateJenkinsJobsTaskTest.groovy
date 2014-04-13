package com.terrafolio.gradle.plugins.jenkins.test.tasks

import com.terrafolio.gradle.plugins.jenkins.JenkinsPlugin
import com.terrafolio.gradle.plugins.jenkins.service.JenkinsRESTServiceImpl
import com.terrafolio.gradle.plugins.jenkins.tasks.UpdateJenkinsJobsTask
import groovy.mock.interceptor.MockFor
import org.gradle.api.Project
import org.gradle.testfixtures.ProjectBuilder
import org.junit.Before
import org.junit.Test

class UpdateJenkinsJobsTaskTest {
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
            views {
                "test view" {
                    type "ListView"
                    server servers.test1
                    dsl { }
                }
            }
		}
		
		mockJenkinsRESTService = new MockFor(JenkinsRESTServiceImpl.class)
	}
	
	@Test
	def void execute_updatesOneJob() {
		mockJenkinsRESTService.demand.with {
			createConfiguration(0) { String jobName, String configXML -> }
			
			getConfiguration() { String jobName, Map overrides ->
				"<project><actions></actions><description>difference</description><keepDependencies>false</keepDependencies><properties></properties><scm class='hudson.scm.NullSCM'></scm><canRoam>true</canRoam><disabled>false</disabled><blockBuildWhenDownstreamBuilding>false</blockBuildWhenDownstreamBuilding><blockBuildWhenUpstreamBuilding>false</blockBuildWhenUpstreamBuilding><triggers class='vector'></triggers><concurrentBuild>false</concurrentBuild><builders></builders><publishers></publishers><buildWrappers></buildWrappers></project>"
			}
			
			updateConfiguration() { String jobName, String configXML, Map overrides ->
				assert jobName == project.jenkins.jobs.compile_master.definition.name
			}
			
		}
		
		project.task('updateOneJob', type: UpdateJenkinsJobsTask) {
			update(project.jenkins.jobs.compile_master)
		}
		
		mockJenkinsRESTService.use {
			project.tasks.updateOneJob.execute()
		}
	}
	
	@Test
	def void execute_skipsJobUpdateOnNoChange() {
		mockJenkinsRESTService.demand.with {
			createConfiguration(0) { String jobName, String configXML -> }
			
			getConfiguration() { String jobName, Map overrides ->
				"<project><actions></actions><description></description><keepDependencies>false</keepDependencies><properties></properties><scm class='hudson.scm.NullSCM'></scm><canRoam>true</canRoam><disabled>false</disabled><blockBuildWhenDownstreamBuilding>false</blockBuildWhenDownstreamBuilding><blockBuildWhenUpstreamBuilding>false</blockBuildWhenUpstreamBuilding><triggers class='vector'></triggers><concurrentBuild>false</concurrentBuild><builders></builders><publishers></publishers><buildWrappers></buildWrappers></project>"
			}
			
			updateConfiguration(0) { String jobName, String configXML, Map overrides -> }
			
		}
		
		project.task('updateOneJob', type: UpdateJenkinsJobsTask) {
			update(project.jenkins.jobs.compile_master)
		}
		
		mockJenkinsRESTService.use {
			project.tasks.updateOneJob.execute()
		}
	}

    @Test
    def void execute_skipsViewUpdateOnNoChange() {
        mockJenkinsRESTService.demand.with {
            createConfiguration(0) { String viewName, String configXML -> }

            getConfiguration() { String viewName, Map overrides ->
                """
                    <hudson.model.ListView>
                      <filterExecutors>false</filterExecutors>
                      <filterQueue>false</filterQueue>
                      <properties class="hudson.model.View\$PropertyList"/>
                      <jobNames class="tree-set">
                        <comparator class="hudson.util.CaseInsensitiveComparator"/>
                      </jobNames>
                      <jobFilters/>
                      <columns/>
                    </hudson.model.ListView>
                """
            }

            updateConfiguration(0) { String viewName, String configXML, Map overrides -> }

        }

        project.task('updateOneJob', type: UpdateJenkinsJobsTask) {
            update(project.jenkins.views."test view")
        }

        mockJenkinsRESTService.use {
            project.tasks.updateOneJob.execute()
        }
    }
	
	@Test
	def void execute_updatesOnForceUpdateString() {
		mockJenkinsRESTService.demand.with {
			createConfiguration(0) { String jobName, String configXML -> }
			
			getConfiguration() { String jobName, Map overrides ->
				"<project><actions></actions><description></description><keepDependencies>false</keepDependencies><properties></properties><scm class='hudson.scm.NullSCM'></scm><canRoam>true</canRoam><disabled>false</disabled><blockBuildWhenDownstreamBuilding>false</blockBuildWhenDownstreamBuilding><blockBuildWhenUpstreamBuilding>false</blockBuildWhenUpstreamBuilding><triggers class='vector'></triggers><concurrentBuild>false</concurrentBuild><builders></builders><publishers></publishers><buildWrappers></buildWrappers></project>"
			}
			
			updateConfiguration() { String jobName, String configXML, Map overrides ->
				assert jobName == project.jenkins.jobs.compile_master.definition.name
			}
			
		}
		
		project.ext.forceJenkinsJobsUpdate = 'true'
		project.task('updateOneJob', type: UpdateJenkinsJobsTask) {
			update(project.jenkins.jobs.compile_master)
		}
		
		mockJenkinsRESTService.use {
			project.tasks.updateOneJob.execute()
		}
	}
	
	@Test
	def void execute_updatesOnForceUpdateBoolean() {
		mockJenkinsRESTService.demand.with {
			createConfiguration(0) { String jobName, String configXML -> }
			
			getConfiguration() { String jobName, Map overrides ->
				"<project><actions></actions><description></description><keepDependencies>false</keepDependencies><properties></properties><scm class='hudson.scm.NullSCM'></scm><canRoam>true</canRoam><disabled>false</disabled><blockBuildWhenDownstreamBuilding>false</blockBuildWhenDownstreamBuilding><blockBuildWhenUpstreamBuilding>false</blockBuildWhenUpstreamBuilding><triggers class='vector'></triggers><concurrentBuild>false</concurrentBuild><builders></builders><publishers></publishers><buildWrappers></buildWrappers></project>"
			}
			
			updateConfiguration() { String jobName, String configXML, Map overrides ->
				assert jobName == project.jenkins.jobs.compile_master.definition.name
			}
			
		}
		
		project.ext.forceJenkinsJobsUpdate = true
		project.task('updateOneJob', type: UpdateJenkinsJobsTask) {
			update(project.jenkins.jobs.compile_master)
		}
		
		mockJenkinsRESTService.use {
			project.tasks.updateOneJob.execute()
		}
	}
	
	@Test
	def void execute_callsCreateOnMissingJob() {
		mockJenkinsRESTService.demand.with {
			updateConfiguration(0) { String jobName, String configXML -> }
			
			getConfiguration() { String jobName, Map overrides ->
				return null
			}
			
			createConfiguration() { String jobName, String configXML, Map overrides ->
				assert jobName == project.jenkins.jobs.compile_master.definition.name
			}
			
		}
		
		project.task('updateOneJob', type: UpdateJenkinsJobsTask) {
			update(project.jenkins.jobs.compile_master)
		}
		
		mockJenkinsRESTService.use {
			project.tasks.updateOneJob.execute()
		}
	}

    @Test
    def void execute_callsCreateOnMissingView() {
        mockJenkinsRESTService.demand.with {
            updateConfiguration(0) { String viewName, String configXML -> }

            getConfiguration() { String viewName, Map overrides ->
                return null
            }

            createConfiguration() { String viewName, String configXML, Map overrides ->
                assert viewName == project.jenkins.views."test view".name
                assert overrides.params.name == "test view"
            }

        }

        project.task('updateOneView', type: UpdateJenkinsJobsTask) {
            update(project.jenkins.views."test view")
        }

        mockJenkinsRESTService.use {
            project.tasks.updateOneView.execute()
        }
    }
	
	@Test
	def void execute_callsCreateOnMissingJobWithOverrides() {
		mockJenkinsRESTService.demand.with {
			updateConfiguration(0) { String jobName, String configXML -> }
			
			getConfiguration() { String jobName, Map overrides ->
				return null
			}
			
			createConfiguration() { String jobName, String configXML, Map overrides ->
				assert jobName == project.jenkins.jobs.compile_master.definition.name
				assert overrides.uri == "testUri"
			}
			
		}
		
		project.jenkins.jobs.compile_master {
			serviceOverrides {
				create = [ uri: "testUri" ]
			}
		}
		
		project.task('updateOneJob', type: UpdateJenkinsJobsTask) {
			update(project.jenkins.jobs.compile_master)
		}
		
		mockJenkinsRESTService.use {
			project.tasks.updateOneJob.execute()
		}
	}
	
	@Test
	def void execute_updatesMultipleJobs() {
		mockJenkinsRESTService.demand.with {
			createConfiguration(0) { String jobName, String configXML -> }
			
			getConfiguration() { String jobName, Map overrides ->
				"<project><actions></actions><description>difference</description><keepDependencies>false</keepDependencies><properties></properties><scm class='hudson.scm.NullSCM'></scm><canRoam>true</canRoam><disabled>false</disabled><blockBuildWhenDownstreamBuilding>false</blockBuildWhenDownstreamBuilding><blockBuildWhenUpstreamBuilding>false</blockBuildWhenUpstreamBuilding><triggers class='vector'></triggers><concurrentBuild>false</concurrentBuild><builders></builders><publishers></publishers><buildWrappers></buildWrappers></project>"
			}
			
			updateConfiguration() { String jobName, String configXML, Map overrides ->
				assert jobName == project.jenkins.jobs.compile_master.definition.name
			}
			
			getConfiguration() { String jobName, Map Overrides ->
				"<project><actions></actions><description>difference</description><keepDependencies>false</keepDependencies><properties></properties><scm class='hudson.scm.NullSCM'></scm><canRoam>true</canRoam><disabled>false</disabled><blockBuildWhenDownstreamBuilding>false</blockBuildWhenDownstreamBuilding><blockBuildWhenUpstreamBuilding>false</blockBuildWhenUpstreamBuilding><triggers class='vector'></triggers><concurrentBuild>false</concurrentBuild><builders></builders><publishers></publishers><buildWrappers></buildWrappers></project>"
			}
			
			updateConfiguration() { String jobName, String configXML, Map overrides ->
				assert jobName == project.jenkins.jobs.compile_releaseX.definition.name
			}
		}
		
		
		project.task('updateMultipleJobs', type: UpdateJenkinsJobsTask) {
			update(project.jenkins.jobs.compile_master)
			update(project.jenkins.jobs.compile_releaseX)
		}
		
		mockJenkinsRESTService.use {
			project.tasks.updateMultipleJobs.execute()
		}
	}

    @Test
    def void execute_updatesJobWithDefaultOverridesForUpdate() {
        mockJenkinsRESTService.demand.with {
            createConfiguration(0) { String jobName, String configXML -> }

            getConfiguration() { String jobName, Map overrides ->
                assert overrides.uri == "/job/test compile (master)/config.xml"
                "<project><actions></actions><description>difference</description><keepDependencies>false</keepDependencies><properties></properties><scm class='hudson.scm.NullSCM'></scm><canRoam>true</canRoam><disabled>false</disabled><blockBuildWhenDownstreamBuilding>false</blockBuildWhenDownstreamBuilding><blockBuildWhenUpstreamBuilding>false</blockBuildWhenUpstreamBuilding><triggers class='vector'></triggers><concurrentBuild>false</concurrentBuild><builders></builders><publishers></publishers><buildWrappers></buildWrappers></project>"
            }

            updateConfiguration() { String jobName, String configXML, Map overrides ->
                assert jobName == project.jenkins.jobs.compile_master.definition.name
                assert overrides.uri == "/job/test compile (master)/config.xml"
            }

        }

        project.task('updateOneJob', type: UpdateJenkinsJobsTask) {
            update(project.jenkins.jobs.compile_master)
        }

        mockJenkinsRESTService.use {
            project.tasks.updateOneJob.execute()
        }
    }

    @Test
    def void execute_updatesJobWithDefaultOverridesForCreate() {
        mockJenkinsRESTService.demand.with {
            createConfiguration(0) { String jobName, String configXML -> }

            getConfiguration() { String jobName, Map overrides ->
                assert overrides.uri == "/job/test compile (master)/config.xml"
                return null
            }

            createConfiguration() { String jobName, String configXML, Map overrides ->
                assert jobName == project.jenkins.jobs.compile_master.definition.name
                assert overrides.uri == "/createItem"
                assert overrides.params.name == "test compile (master)"
            }

        }

        project.task('updateOneJob', type: UpdateJenkinsJobsTask) {
            update(project.jenkins.jobs.compile_master)
        }

        mockJenkinsRESTService.use {
            project.tasks.updateOneJob.execute()
        }
    }

    @Test
    def void execute_updatesViewWithDefaultOverridesForUpdate() {
        mockJenkinsRESTService.demand.with {
            createConfiguration(0) { String viewName, String configXML -> }

            getConfiguration() { String viewName, Map overrides ->
                assert overrides.uri == "/view/test view/config.xml"
                """
                    <hudson.model.ListView>
                      <filterExecutors>true</filterExecutors>
                      <filterQueue>false</filterQueue>
                      <properties class="hudson.model.View\$PropertyList"/>
                      <jobNames class="tree-set">
                        <comparator class="hudson.util.CaseInsensitiveComparator"/>
                      </jobNames>
                      <jobFilters/>
                      <columns/>
                    </hudson.model.ListView>
                """
            }

            updateConfiguration() { String viewName, String configXML, Map overrides ->
                assert viewName == project.jenkins.views."test view".name
                assert overrides.uri == "/view/test view/config.xml"
            }

        }

        project.task('updateOneView', type: UpdateJenkinsJobsTask) {
            update(project.jenkins.views."test view")
        }

        mockJenkinsRESTService.use {
            project.tasks.updateOneView.execute()
        }
    }

    @Test
    def void execute_updatesViewWithDefaultOverridesForCreate() {
        mockJenkinsRESTService.demand.with {
            createConfiguration(0) { String viewName, String configXML -> }

            getConfiguration() { String viewName, Map overrides ->
                assert overrides.uri == "/view/test view/config.xml"
                return null
            }

            createConfiguration() { String viewName, String configXML, Map overrides ->
                assert viewName == project.jenkins.views."test view".name
                assert overrides.uri == "/createView"
                assert overrides.params.name == "test view"
            }

        }

        project.task('updateOneView', type: UpdateJenkinsJobsTask) {
            update(project.jenkins.views."test view")
        }

        mockJenkinsRESTService.use {
            project.tasks.updateOneView.execute()
        }
    }


    @Test
	def void execute_updatesWithOverride() {
		mockJenkinsRESTService.demand.with {
			createConfiguration(0) { String jobName, String configXML -> }
			
			getConfiguration() { String jobName, Map overrides ->
				assert overrides.uri == "anotherUri"
				"<project><actions></actions><description>difference</description><keepDependencies>false</keepDependencies><properties></properties><scm class='hudson.scm.NullSCM'></scm><canRoam>true</canRoam><disabled>false</disabled><blockBuildWhenDownstreamBuilding>false</blockBuildWhenDownstreamBuilding><blockBuildWhenUpstreamBuilding>false</blockBuildWhenUpstreamBuilding><triggers class='vector'></triggers><concurrentBuild>false</concurrentBuild><builders></builders><publishers></publishers><buildWrappers></buildWrappers></project>"
			}
			
			updateConfiguration() { String jobName, String configXML, Map overrides ->
				assert jobName == project.jenkins.jobs.compile_master.definition.name
				assert overrides.uri == "testUri"
				assert overrides.params.name == "test"
			}
			
		}
		
		project.jenkins.jobs.compile_master {
			serviceOverrides {
				update = [ uri: "testUri", params: [ name: "test" ] ]
				get = [ uri: "anotherUri" ]
			}
		}
		
		project.task('updateOneJob', type: UpdateJenkinsJobsTask) {
			update(project.jenkins.jobs.compile_master)
		}
		
		mockJenkinsRESTService.use {
			project.tasks.updateOneJob.execute()
		}
	}
	 
}
