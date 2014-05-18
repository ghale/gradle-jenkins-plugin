package com.terrafolio.gradle.plugins.jenkins.test.tasks

import com.terrafolio.gradle.plugins.jenkins.service.JenkinsRESTServiceImpl
import com.terrafolio.gradle.plugins.jenkins.service.JenkinsService
import com.terrafolio.gradle.plugins.jenkins.service.JenkinsServiceFactory
import com.terrafolio.gradle.plugins.jenkins.tasks.UpdateJenkinsJobsTask
import nebula.test.ProjectSpec
import org.junit.Ignore

class UpdateJenkinsJobsTaskTest extends ProjectSpec {
    def mockJenkinsRESTService

    def void setup() {
        project.apply plugin: 'jenkins'

        project.ext.branches = [
                master  : [ parents: [ ] ],
                develop : [ parents: [ 'master' ] ],
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
                    xml """
                            <project>
                                <actions></actions>
                                <description></description>
                                <keepDependencies>false</keepDependencies>
                                <properties></properties>
                                <scm class='hudson.scm.NullSCM'></scm>
                                <canRoam>true</canRoam>
                                <disabled>false</disabled>
                                <blockBuildWhenDownstreamBuilding>false</blockBuildWhenDownstreamBuilding>
                                <blockBuildWhenUpstreamBuilding>false</blockBuildWhenUpstreamBuilding>
                                <triggers class='vector'></triggers>
                                <concurrentBuild>false</concurrentBuild>
                                <builders></builders>
                                <publishers></publishers>
                                <buildWrappers></buildWrappers>
                                </project>
                    """
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

        mockJenkinsRESTService = Mock(JenkinsRESTServiceImpl)
    }

    @Ignore
    def injectFactory(UpdateJenkinsJobsTask task) {
        task.serviceFactory = new JenkinsServiceFactory() {
            @Override
            JenkinsService getService(String url) {
                return mockJenkinsRESTService
            }

            @Override
            JenkinsService getService(String url, String username, String password) {
                return mockJenkinsRESTService
            }
        }
    }

    def "execute updates one job" () {
        setup:
        def jobName = project.jenkins.jobs.compile_master.definition.name
        project.task('updateOneJob', type: UpdateJenkinsJobsTask) {
            update(project.jenkins.jobs.compile_master)
        }
        injectFactory(project.tasks.updateOneJob)

        when:
        project.tasks.updateOneJob.execute()

        then:
        with(mockJenkinsRESTService) {
            0 * createConfiguration(*_)

            1 * getConfiguration(jobName,_) >> {
                """
                    <project>
                        <actions></actions>
                        <description>difference</description>
                        <keepDependencies>false</keepDependencies>
                        <properties></properties>
                        <scm class='hudson.scm.NullSCM'></scm>
                        <canRoam>true</canRoam>
                        <disabled>false</disabled>
                        <blockBuildWhenDownstreamBuilding>false</blockBuildWhenDownstreamBuilding>
                        <blockBuildWhenUpstreamBuilding>false</blockBuildWhenUpstreamBuilding>
                        <triggers class='vector'></triggers>
                        <concurrentBuild>false</concurrentBuild>
                        <builders></builders>
                        <publishers></publishers>
                        <buildWrappers></buildWrappers>
                    </project>
                """
            }

            1 * updateConfiguration(jobName,_,_)
        }
    }

    def "execute skips job update when there are no changes" () {
        setup:
        def jobName = project.jenkins.jobs.compile_master.definition.name
        project.task('updateOneJob', type: UpdateJenkinsJobsTask) {
            update(project.jenkins.jobs.compile_master)
        }
        injectFactory(project.tasks.updateOneJob)

        when:
        project.tasks.updateOneJob.execute()

        then:
        with(mockJenkinsRESTService) {
            0 * createConfiguration(*_)
            0 * updateConfiguration(*_)
            1 * getConfiguration(jobName, _) >> {
                """
                    <project>
                        <actions></actions>
                        <description></description>
                        <keepDependencies>false</keepDependencies>
                        <properties></properties>
                        <scm class='hudson.scm.NullSCM'></scm>
                        <canRoam>true</canRoam>
                        <disabled>false</disabled>
                        <blockBuildWhenDownstreamBuilding>false</blockBuildWhenDownstreamBuilding>
                        <blockBuildWhenUpstreamBuilding>false</blockBuildWhenUpstreamBuilding>
                        <triggers class='vector'></triggers>
                        <concurrentBuild>false</concurrentBuild>
                        <builders></builders>
                        <publishers></publishers>
                        <buildWrappers></buildWrappers>
                    </project>
                """
            }
        }
    }

    def "execute skips view update when there are no changes" () {
        setup:
        def viewName = project.jenkins.views."test view".name
        project.task('updateOneView', type: UpdateJenkinsJobsTask) {
            update(project.jenkins.views."test view")
        }
        injectFactory(project.tasks.updateOneView)

        when:
        project.tasks.updateOneView.execute()

        then:
        with(mockJenkinsRESTService) {
            0 * createConfiguration(*_)
            0 * updateConfiguration(*_)
            1 * getConfiguration(viewName, _) >> {
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
        }

    }

    def "execute updates on forceUpdate with String" () {
        setup:
        def jobName = project.jenkins.jobs.compile_master.definition.name
        project.ext.forceJenkinsJobsUpdate = 'true'
        project.task('updateOneJob', type: UpdateJenkinsJobsTask) {
            update(project.jenkins.jobs.compile_master)
        }
        injectFactory(project.tasks.updateOneJob)

        when:
        project.tasks.updateOneJob.execute()

        then:
        with(mockJenkinsRESTService) {
            0 * createConfiguration(*_)
            1 * updateConfiguration(jobName,_,_)
            1 * getConfiguration(jobName, _) >> {
                """
                    <project>
                        <actions></actions>
                        <description></description>
                        <keepDependencies>false</keepDependencies>
                        <properties></properties>
                        <scm class='hudson.scm.NullSCM'></scm>
                        <canRoam>true</canRoam>
                        <disabled>false</disabled>
                        <blockBuildWhenDownstreamBuilding>false</blockBuildWhenDownstreamBuilding>
                        <blockBuildWhenUpstreamBuilding>false</blockBuildWhenUpstreamBuilding>
                        <triggers class='vector'></triggers>
                        <concurrentBuild>false</concurrentBuild>
                        <builders></builders>
                        <publishers></publishers>
                        <buildWrappers></buildWrappers>
                    </project>
                """
            }
        }
    }

    def "execute updates on forceUpdate with Boolean" () {
        setup:
        def jobName = project.jenkins.jobs.compile_master.definition.name
        project.ext.forceJenkinsJobsUpdate = true
        project.task('updateOneJob', type: UpdateJenkinsJobsTask) {
            update(project.jenkins.jobs.compile_master)
        }
        injectFactory(project.tasks.updateOneJob)

        when:
        project.tasks.updateOneJob.execute()

        then:
        with(mockJenkinsRESTService) {
            0 * createConfiguration(*_)
            1 * updateConfiguration(jobName,_,_)
            1 * getConfiguration(jobName, _) >> {
                """
                    <project>
                        <actions></actions>
                        <description></description>
                        <keepDependencies>false</keepDependencies>
                        <properties></properties>
                        <scm class='hudson.scm.NullSCM'></scm>
                        <canRoam>true</canRoam>
                        <disabled>false</disabled>
                        <blockBuildWhenDownstreamBuilding>false</blockBuildWhenDownstreamBuilding>
                        <blockBuildWhenUpstreamBuilding>false</blockBuildWhenUpstreamBuilding>
                        <triggers class='vector'></triggers>
                        <concurrentBuild>false</concurrentBuild>
                        <builders></builders>
                        <publishers></publishers>
                        <buildWrappers></buildWrappers>
                    </project>
                """
            }
        }
    }

    def "execute calls create on missing job" () {
        setup:
        def jobName = project.jenkins.jobs.compile_master.definition.name
        project.task('updateOneJob', type: UpdateJenkinsJobsTask) {
            update(project.jenkins.jobs.compile_master)
        }
        injectFactory(project.tasks.updateOneJob)

        when:
        project.tasks.updateOneJob.execute()

        then:
        with(mockJenkinsRESTService) {
            0 * updateConfiguration(*_)
            1 * createConfiguration(jobName,_,_)
            1 * getConfiguration(jobName, _)
        }
    }

    def "execute calls create on missing view" () {
        setup:
        def viewName = project.jenkins.views."test view".name
        project.task('updateOneView', type: UpdateJenkinsJobsTask) {
            update(project.jenkins.views."test view")
        }
        injectFactory(project.tasks.updateOneView)

        when:
        project.tasks.updateOneView.execute()

        then:
        with(mockJenkinsRESTService) {
            0 * updateConfiguration(*_)
            1 * createConfiguration(viewName, _, { it.params.name == viewName})
            1 * getConfiguration(viewName, _)
        }
    }

    def "execute calls create on missing job with overrides" () {
        setup:
        def jobName = project.jenkins.jobs.compile_master.definition.name
        project.jenkins.jobs.compile_master {
            serviceOverrides {
                create = [ uri: "testUri" ]
            }
        }
        project.task('updateOneJob', type: UpdateJenkinsJobsTask) {
            update(project.jenkins.jobs.compile_master)
        }
        injectFactory(project.tasks.updateOneJob)

        when:
        project.tasks.updateOneJob.execute()

        then:
        with(mockJenkinsRESTService) {
            0 * updateConfiguration(*_)
            1 * createConfiguration(jobName, _, { it.uri == "testUri" })
            1 * getConfiguration(jobName, _)
        }
    }

    def "execute calls update with overrides" () {
        setup:
        def jobName = project.jenkins.jobs.compile_master.definition.name
        project.jenkins.jobs.compile_master {
            serviceOverrides {
                update = [ uri: "testUri", params: [ name: "test" ] ]
                get = [ uri: "anotherUri" ]
            }
        }
        project.task('updateOneJob', type: UpdateJenkinsJobsTask) {
            update(project.jenkins.jobs.compile_master)
        }
        injectFactory(project.tasks.updateOneJob)

        when:
        project.tasks.updateOneJob.execute()

        then:
        with(mockJenkinsRESTService) {
            0 * createConfiguration(*_)
            1 * updateConfiguration(jobName, _, { it.uri == "testUri" && it.params.name == "test" })
            1 * getConfiguration(jobName, { it.uri == "anotherUri" }) >> {
                """
                    <project>
                        <actions></actions>
                        <description>difference</description>
                        <keepDependencies>false</keepDependencies>
                        <properties></properties>
                        <scm class='hudson.scm.NullSCM'></scm>
                        <canRoam>true</canRoam>
                        <disabled>false</disabled>
                        <blockBuildWhenDownstreamBuilding>false</blockBuildWhenDownstreamBuilding>
                        <blockBuildWhenUpstreamBuilding>false</blockBuildWhenUpstreamBuilding>
                        <triggers class='vector'></triggers>
                        <concurrentBuild>false</concurrentBuild>
                        <builders></builders>
                        <publishers></publishers>
                        <buildWrappers></buildWrappers>
                    </project>
                """
            }
        }
    }

    def "execute updates multiple jobs" () {
        setup:
        def jobName1 = project.jenkins.jobs.compile_master.definition.name
        def jobName2 = project.jenkins.jobs.compile_releaseX.definition.name
        def differenceXml = """
            <project>
                <actions></actions>
                <description>difference</description>
                <keepDependencies>false</keepDependencies>
                <properties></properties>
                <scm class='hudson.scm.NullSCM'></scm>
                <canRoam>true</canRoam>
                <disabled>false</disabled>
                <blockBuildWhenDownstreamBuilding>false</blockBuildWhenDownstreamBuilding>
                <blockBuildWhenUpstreamBuilding>false</blockBuildWhenUpstreamBuilding>
                <triggers class='vector'></triggers>
                <concurrentBuild>false</concurrentBuild>
                <builders></builders>
                <publishers></publishers>
                <buildWrappers></buildWrappers>
            </project>
        """
        project.task('updateMultipleJobs', type: UpdateJenkinsJobsTask) {
            update(project.jenkins.jobs.compile_master)
            update(project.jenkins.jobs.compile_releaseX)
        }
        injectFactory(project.tasks.updateMultipleJobs)

        when:
        project.tasks.updateMultipleJobs.execute()

        then:
        with(mockJenkinsRESTService) {
            0 * createConfiguration(*_)

            1 * getConfiguration(jobName1,_) >> { String jobName, Map overrides ->
                differenceXml
            }

            1 * updateConfiguration(jobName1,_,_)

            1 * getConfiguration(jobName2,_) >> { String jobName, Map overrides ->
                differenceXml
            }

            1 * updateConfiguration(jobName2,_,_)
        }
    }

    def "execute updates job with default URI overrides" () {
        setup:
        def jobName = project.jenkins.jobs.compile_master.definition.name

        project.task('updateOneJob', type: UpdateJenkinsJobsTask) {
            update(project.jenkins.jobs.compile_master)
        }
        injectFactory(project.tasks.updateOneJob)

        when:
        project.tasks.updateOneJob.execute()

        then:
        with(mockJenkinsRESTService) {
            0 * createConfiguration(*_)

            1 * getConfiguration(jobName, { it.uri == "/job/${jobName}/config.xml" }) >> {
                """
                    <project>
                        <actions></actions>
                        <description>difference</description>
                        <keepDependencies>false</keepDependencies>
                        <properties></properties>
                        <scm class='hudson.scm.NullSCM'></scm>
                        <canRoam>true</canRoam>
                        <disabled>false</disabled>
                        <blockBuildWhenDownstreamBuilding>false</blockBuildWhenDownstreamBuilding>
                        <blockBuildWhenUpstreamBuilding>false</blockBuildWhenUpstreamBuilding>
                        <triggers class='vector'></triggers>
                        <concurrentBuild>false</concurrentBuild>
                        <builders></builders>
                        <publishers></publishers>
                        <buildWrappers></buildWrappers>
                    </project>
                """
            }

            1 * updateConfiguration(jobName, _, { it.uri == "/job/${jobName}/config.xml" })
        }
    }

    def "execute creates job with default URI overrides" () {
        setup:
        def jobName = project.jenkins.jobs.compile_master.definition.name
        project.task('updateOneJob', type: UpdateJenkinsJobsTask) {
            update(project.jenkins.jobs.compile_master)
        }
        injectFactory(project.tasks.updateOneJob)

        when:
        project.tasks.updateOneJob.execute()

        then:
        with(mockJenkinsRESTService) {
            0 * updateConfiguration(* _)

            1 * getConfiguration(jobName, { it.uri == "/job/${jobName}/config.xml" })

            1 * createConfiguration(jobName, _, { it.uri == "/createItem" && it.params.name == jobName })
        }
    }

    def "execute updates view with default URI overrides" () {
        setup:
        def viewName = project.jenkins.views."test view".name
        project.task('updateOneView', type: UpdateJenkinsJobsTask) {
            update(project.jenkins.views."test view")
        }
        injectFactory(project.tasks.updateOneView)

        when:
        project.tasks.updateOneView.execute()

        then:
        with(mockJenkinsRESTService) {
            0 * createConfiguration(*_)

            1 * getConfiguration(viewName, { it.uri == "/view/${viewName}/config.xml" }) >> {
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

            1 * updateConfiguration(viewName, _, { it.uri == "/view/${viewName}/config.xml" })
        }
    }

    def "execute creates view with default URI overrides" () {
        setup:
        def viewName = project.jenkins.views."test view".name
        project.task('updateOneView', type: UpdateJenkinsJobsTask) {
            update(project.jenkins.views."test view")
        }
        injectFactory(project.tasks.updateOneView)

        when:
        project.tasks.updateOneView.execute()

        then:
        with(mockJenkinsRESTService) {
            0 * updateConfiguration(* _)

            1 * getConfiguration(viewName, { it.uri == "/view/${viewName}/config.xml" })

            1 * createConfiguration(viewName, _, { it.uri == "/createView" && it.params.name == viewName })
        }
    }
}
