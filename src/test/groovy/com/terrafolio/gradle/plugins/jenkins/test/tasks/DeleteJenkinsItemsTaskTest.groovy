package com.terrafolio.gradle.plugins.jenkins.test.tasks

import com.terrafolio.gradle.plugins.jenkins.JenkinsPlugin
import com.terrafolio.gradle.plugins.jenkins.service.JenkinsRESTServiceImpl
import com.terrafolio.gradle.plugins.jenkins.tasks.DeleteJenkinsItemsTask
import groovy.mock.interceptor.MockFor
import org.gradle.api.Project
import org.gradle.testfixtures.ProjectBuilder
import org.junit.Before
import org.junit.Test

class DeleteJenkinsItemsTaskTest {
    def private final Project project = ProjectBuilder.builder().withProjectDir(new File('build/tmp/test')).build()
    def private final JenkinsPlugin plugin = new JenkinsPlugin()
    def MockFor mockJenkinsRESTService

    @Before
    def void setupProject() {
        plugin.apply(project)

        project.ext.branches = [
                master : [parents: []],
                develop: [parents: ['master']]
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
                    dsl {}
                }
                "another view" {
                    type "ListView"
                    server servers.test1
                    dsl {}
                }
            }
        }

        mockJenkinsRESTService = new MockFor(JenkinsRESTServiceImpl.class)
    }

    @Test
    def void execute_deletesOneJob() {
        mockJenkinsRESTService.demand.with {
            getConfiguration() { String jobName, Map overrides -> "<project><actions></actions><description></description><keepDependencies>false</keepDependencies><properties></properties><scm class='hudson.scm.NullSCM'></scm><canRoam>true</canRoam><disabled>false</disabled><blockBuildWhenDownstreamBuilding>false</blockBuildWhenDownstreamBuilding><blockBuildWhenUpstreamBuilding>false</blockBuildWhenUpstreamBuilding><triggers class='vector'></triggers><concurrentBuild>false</concurrentBuild><builders></builders><publishers></publishers><buildWrappers></buildWrappers></project>" }
            deleteConfiguration() { String jobName, Map overrides ->
                if (!project.jenkins.jobs.collect { it.definition.name }.contains(jobName)) {
                    throw new Exception('deleteConfiguration received: ' + jobName + ' but there\'s no job definition with that name!')
                }
            }
        }

        project.task('deleteOneJob', type: DeleteJenkinsItemsTask) {
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
            getConfiguration() { String jobName, Map overrides -> "<project><actions></actions><description></description><keepDependencies>false</keepDependencies><properties></properties><scm class='hudson.scm.NullSCM'></scm><canRoam>true</canRoam><disabled>false</disabled><blockBuildWhenDownstreamBuilding>false</blockBuildWhenDownstreamBuilding><blockBuildWhenUpstreamBuilding>false</blockBuildWhenUpstreamBuilding><triggers class='vector'></triggers><concurrentBuild>false</concurrentBuild><builders></builders><publishers></publishers><buildWrappers></buildWrappers></project>" }
            deleteConfiguration() { String jobName, Map overrides ->
                assert jobName == jobToDelete
            }
        }

        project.task('deleteOneJob', type: DeleteJenkinsItemsTask) {
            delete(project.jenkins.servers.test1, jobToDelete)
        }

        mockJenkinsRESTService.use {
            project.tasks.deleteOneJob.execute()
        }
    }

    @Test
    def void execute_deletesOneViewTuple() {
        def viewToDelete = "test view"
        mockJenkinsRESTService.demand.with {
            getConfiguration() { String viewName, Map overrides ->
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
            deleteConfiguration() { String viewName, Map overrides ->
                assert viewName == viewToDelete
            }
        }

        project.task('deleteOneView', type: DeleteJenkinsItemsTask) {
            deleteView(project.jenkins.servers.test1, viewToDelete)
        }

        mockJenkinsRESTService.use {
            project.tasks.deleteOneView.execute()
        }
    }

    def void execute_deletesMultipleJobsWithTuple() {
        def jobToDelete = "${project.name} compile (master)"
        mockJenkinsRESTService.demand.with {
            2.times {
                getConfiguration() { String jobName -> "<project><actions></actions><description></description><keepDependencies>false</keepDependencies><properties></properties><scm class='hudson.scm.NullSCM'></scm><canRoam>true</canRoam><disabled>false</disabled><blockBuildWhenDownstreamBuilding>false</blockBuildWhenDownstreamBuilding><blockBuildWhenUpstreamBuilding>false</blockBuildWhenUpstreamBuilding><triggers class='vector'></triggers><concurrentBuild>false</concurrentBuild><builders></builders><publishers></publishers><buildWrappers></buildWrappers></project>" }
                deleteConfiguration() { String jobName ->
                    assert jobName == jobToDelete
                }
            }
        }

        project.task('deleteMultipleJobs', type: DeleteJenkinsItemsTask) {
            delete(project.jenkins.servers.test1, jobToDelete)
            delete(project.jenkins.servers.test2, jobToDelete)
        }

        mockJenkinsRESTService.use {
            project.tasks.deleteMultipleJobs.execute()
        }
    }

    def void execute_deletesMultipleJobs() {
        def jobsToDelete = ["${project.name} compile (master)", "${project.name} compile (develop)"]
        mockJenkinsRESTService.demand.with {
            2.times {
                getConfiguration() { String jobName -> "<project><actions></actions><description></description><keepDependencies>false</keepDependencies><properties></properties><scm class='hudson.scm.NullSCM'></scm><canRoam>true</canRoam><disabled>false</disabled><blockBuildWhenDownstreamBuilding>false</blockBuildWhenDownstreamBuilding><blockBuildWhenUpstreamBuilding>false</blockBuildWhenUpstreamBuilding><triggers class='vector'></triggers><concurrentBuild>false</concurrentBuild><builders></builders><publishers></publishers><buildWrappers></buildWrappers></project>" }
                deleteConfiguration() { String jobName ->
                    assert jobsToDelete.contains(jobName)
                    jobsToDelete.remove(jobName)
                }
            }
        }

        project.task('deleteMultipleJobs', type: DeleteJenkinsItemsTask) {
            delete(project.jenkins.jobs.compile_master)
            delete(project.jenkins.jobs.compile_develop)
        }

        mockJenkinsRESTService.use {
            project.tasks.deleteMultipleJobs.execute()
        }
    }

    @Test
    def void execute_deletesMultipleViewsWithTuple() {
        def viewsToDelete = ["test view", "another view"]
        mockJenkinsRESTService.demand.with {
            2.times {
                getConfiguration() { String viewName, Map overrides ->
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

                deleteConfiguration() { String viewName, Map overrides ->
                    assert viewsToDelete.contains(viewName)
                    viewsToDelete.remove(viewName)
                }
            }
        }

        project.task('deleteMultipleViews', type: DeleteJenkinsItemsTask) {
            deleteView(project.jenkins.servers.test1, "test view")
            deleteView(project.jenkins.servers.test1, "another view")
        }

        mockJenkinsRESTService.use {
            project.tasks.deleteMultipleViews.execute()
        }
    }

    @Test
    def void execute_deletesMultipleViews() {
        def viewsToDelete = ["test view", "another view"]
        mockJenkinsRESTService.demand.with {
            2.times {
                getConfiguration() { String viewName, Map overrides ->
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

                deleteConfiguration() { String viewName, Map overrides ->
                    assert viewsToDelete.contains(viewName)
                    viewsToDelete.remove(viewName)
                }
            }
        }

        project.task('deleteMultipleViews', type: DeleteJenkinsItemsTask) {
            delete(project.jenkins.views."test view")
            delete(project.jenkins.views."another view")
        }

        mockJenkinsRESTService.use {
            project.tasks.deleteMultipleViews.execute()
        }
    }

    @Test
    def void execute_deletesJobWithOverrides() {
        mockJenkinsRESTService.demand.with {
            getConfiguration() { String jobName, Map overrides -> "<project><actions></actions><description></description><keepDependencies>false</keepDependencies><properties></properties><scm class='hudson.scm.NullSCM'></scm><canRoam>true</canRoam><disabled>false</disabled><blockBuildWhenDownstreamBuilding>false</blockBuildWhenDownstreamBuilding><blockBuildWhenUpstreamBuilding>false</blockBuildWhenUpstreamBuilding><triggers class='vector'></triggers><concurrentBuild>false</concurrentBuild><builders></builders><publishers></publishers><buildWrappers></buildWrappers></project>" }
            deleteConfiguration() { String jobName, Map overrides ->
                if (!project.jenkins.jobs.collect { it.definition.name }.contains(jobName)) {
                    throw new Exception('deleteConfiguration received: ' + jobName + ' but there\'s no job definition with that name!')
                }
                assert overrides.uri == "testUri"
            }
        }

        project.jenkins.jobs.compile_master {
            serviceOverrides {
                delete = [uri: "testUri"]
            }
        }

        project.task('deleteOneJob', type: DeleteJenkinsItemsTask) {
            delete(project.jenkins.jobs.compile_master)
        }

        mockJenkinsRESTService.use {
            project.tasks.deleteOneJob.execute()
        }
    }

    @Test
    def void execute_deletesJobWithDefaultOverrides() {
        mockJenkinsRESTService.demand.with {
            getConfiguration() { String jobName, Map overrides -> "<project><actions></actions><description></description><keepDependencies>false</keepDependencies><properties></properties><scm class='hudson.scm.NullSCM'></scm><canRoam>true</canRoam><disabled>false</disabled><blockBuildWhenDownstreamBuilding>false</blockBuildWhenDownstreamBuilding><blockBuildWhenUpstreamBuilding>false</blockBuildWhenUpstreamBuilding><triggers class='vector'></triggers><concurrentBuild>false</concurrentBuild><builders></builders><publishers></publishers><buildWrappers></buildWrappers></project>" }
            deleteConfiguration() { String jobName, Map overrides ->
                if (!project.jenkins.jobs.collect { it.definition.name }.contains(jobName)) {
                    throw new Exception('deleteConfiguration received: ' + jobName + ' but there\'s no job definition with that name!')
                }
                assert overrides.uri == "/job/test compile (master)/doDelete"
            }
        }

        project.task('deleteOneJob', type: DeleteJenkinsItemsTask) {
            delete(project.jenkins.jobs.compile_master)
        }

        mockJenkinsRESTService.use {
            project.tasks.deleteOneJob.execute()
        }
    }

    @Test
    def void execute_deletesViewWithDefaultOverrides() {
        mockJenkinsRESTService.demand.with {
            getConfiguration() { String viewName, Map overrides -> "<project><actions></actions><description></description><keepDependencies>false</keepDependencies><properties></properties><scm class='hudson.scm.NullSCM'></scm><canRoam>true</canRoam><disabled>false</disabled><blockBuildWhenDownstreamBuilding>false</blockBuildWhenDownstreamBuilding><blockBuildWhenUpstreamBuilding>false</blockBuildWhenUpstreamBuilding><triggers class='vector'></triggers><concurrentBuild>false</concurrentBuild><builders></builders><publishers></publishers><buildWrappers></buildWrappers></project>" }
            deleteConfiguration() { String viewName, Map overrides ->
                if (!project.jenkins.views.collect { it.name }.contains(viewName)) {
                    throw new Exception('deleteConfiguration received: ' + viewName + ' but there\'s no view definition with that name!')
                }
                assert overrides.uri == "/view/test view/doDelete"
            }
        }

        project.task('deleteOneView', type: DeleteJenkinsItemsTask) {
            delete(project.jenkins.views."test view")
        }

        mockJenkinsRESTService.use {
            project.tasks.deleteOneView.execute()
        }
    }

}
