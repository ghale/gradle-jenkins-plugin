package com.terrafolio.gradle.plugins.jenkins.test.tasks

import com.terrafolio.gradle.plugins.jenkins.tasks.AbstractJenkinsTask
import com.terrafolio.gradle.plugins.jenkins.tasks.UpdateJenkinsItemsTask

class UpdateJenkinsItemsTaskTest extends JenkinsPluginTaskSpec {
    @Override
    AbstractJenkinsTask createTaskUnderTest() {
        return project.task('taskUnderTest', type: UpdateJenkinsItemsTask)
    }

    def "execute updates one job" () {
        setup:
        def jobName = project.jenkins.jobs.compile_master.definition.name
        project.tasks.taskUnderTest {
            update(project.jenkins.jobs.compile_master)
        }

        when:
        taskUnderTest.execute()

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
        project.tasks.taskUnderTest {
            update(project.jenkins.jobs.compile_master)
        }

        when:
        taskUnderTest.execute()

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
        project.tasks.taskUnderTest {
            update(project.jenkins.views."test view")
        }

        when:
        taskUnderTest.execute()

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
        project.tasks.taskUnderTest {
            update(project.jenkins.jobs.compile_master)
        }

        when:
        taskUnderTest.execute()

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
        project.tasks.taskUnderTest {
            update(project.jenkins.jobs.compile_master)
        }

        when:
        taskUnderTest.execute()

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
        project.tasks.taskUnderTest {
            update(project.jenkins.jobs.compile_master)
        }

        when:
        taskUnderTest.execute()

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
        project.tasks.taskUnderTest {
            update(project.jenkins.views."test view")
        }

        when:
        taskUnderTest.execute()

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
        project.tasks.taskUnderTest {
            update(project.jenkins.jobs.compile_master)
        }

        when:
        taskUnderTest.execute()

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
        project.tasks.taskUnderTest {
            update(project.jenkins.jobs.compile_master)
        }

        when:
        taskUnderTest.execute()

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
        def jobName2 = project.jenkins.jobs.compile_develop.definition.name
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
        project.tasks.taskUnderTest {
            update(project.jenkins.jobs.compile_master)
            update(project.jenkins.jobs.compile_develop)
        }

        when:
        taskUnderTest.execute()

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

        project.tasks.taskUnderTest {
            update(project.jenkins.jobs.compile_master)
        }

        when:
        taskUnderTest.execute()

        then:
        with(mockJenkinsRESTService) {
            0 * createConfiguration(*_)

            1 * getConfiguration(jobName, { it.uri == "job/${jobName}/config.xml" }) >> {
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

            1 * updateConfiguration(jobName, _, { it.uri == "job/${jobName}/config.xml" })
        }
    }

    def "execute creates job with default URI overrides" () {
        setup:
        def jobName = project.jenkins.jobs.compile_master.definition.name
        project.tasks.taskUnderTest {
            update(project.jenkins.jobs.compile_master)
        }

        when:
        taskUnderTest.execute()

        then:
        with(mockJenkinsRESTService) {
            0 * updateConfiguration(* _)

            1 * getConfiguration(jobName, { it.uri == "job/${jobName}/config.xml" })

            1 * createConfiguration(jobName, _, { it.uri == "createItem" && it.params.name == jobName })
        }
    }

    def "execute updates view with default URI overrides" () {
        setup:
        def viewName = project.jenkins.views."test view".name
        project.tasks.taskUnderTest {
            update(project.jenkins.views."test view")
        }

        when:
        taskUnderTest.execute()

        then:
        with(mockJenkinsRESTService) {
            0 * createConfiguration(*_)

            1 * getConfiguration(viewName, { it.uri == "view/${viewName}/config.xml" }) >> {
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

            1 * updateConfiguration(viewName, _, { it.uri == "view/${viewName}/config.xml" })
        }
    }

    def "execute creates view with default URI overrides" () {
        setup:
        def viewName = project.jenkins.views."test view".name
        project.tasks.taskUnderTest {
            update(project.jenkins.views."test view")
        }

        when:
        taskUnderTest.execute()

        then:
        with(mockJenkinsRESTService) {
            0 * updateConfiguration(* _)

            1 * getConfiguration(viewName, { it.uri == "view/${viewName}/config.xml" })

            1 * createConfiguration(viewName, _, { it.uri == "createView" && it.params.name == viewName })
        }
    }

    def "update adds lazy closure with one item" () {
        setup:
        def jobName = project.jenkins.jobs.compile_master.definition.name
        project.tasks.taskUnderTest {
            update { project.jenkins.jobs.compile_master }
        }

        expect:
        taskUnderTest.items == []

        when:
        taskUnderTest.execute()

        then:
        with(mockJenkinsRESTService) {
            0 * updateConfiguration(*_)
            1 * createConfiguration(jobName, _, _)
            1 * getConfiguration(jobName, _)
        }
    }

    def "update adds lazy closure with multiple items" () {
        setup:
        def jobName1 = project.jenkins.jobs.compile_master.definition.name
        def jobName2 = project.jenkins.jobs.compile_develop.definition.name

        project.tasks.taskUnderTest {
            update { [ project.jenkins.jobs.compile_master, project.jenkins.jobs.compile_develop ] }
        }

        expect:
        taskUnderTest.items == []

        when:
        taskUnderTest.execute()

        then:
        with(mockJenkinsRESTService) {
            0 * updateConfiguration(*_)

            1 * getConfiguration(jobName1,_)
            1 * createConfiguration(jobName1,_,_)

            1 * getConfiguration(jobName2,_)
            1 * createConfiguration(jobName2,_,_)
        }
    }
}
