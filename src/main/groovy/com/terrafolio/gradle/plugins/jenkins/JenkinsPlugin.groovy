package com.terrafolio.gradle.plugins.jenkins

import com.terrafolio.gradle.plugins.jenkins.dsl.*
import com.terrafolio.gradle.plugins.jenkins.jobdsl.MapJobManagement
import com.terrafolio.gradle.plugins.jenkins.tasks.*
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.plugins.BasePlugin
import org.gradle.util.DeprecationLogger

class JenkinsPlugin implements Plugin<Project> {

    @Override
    public void apply(Project project) {
        project.plugins.apply(BasePlugin.class)
        applyTasks(project)
        applyConventions(project)
        applyServerConventionRules(project)
        project.gradle.taskGraph.whenReady { checkAllServerValues(it) }
    }

    def applyTasks(Project project) {
        // Legacy Tasks
        def allItems = { project.jenkins.jobs + project.jenkins.views }
        def legacyTasks = []
        legacyTasks += project.task('updateJenkinsJobs', type: UpdateJenkinsItemsTask) { update allItems }
        legacyTasks += project.task('deleteJenkinsJobs', type: DeleteJenkinsItemsTask) { delete allItems }
        legacyTasks += project.task('dumpJenkinsJobs', type: DumpJenkinsItemsTask) { dump allItems }
        legacyTasks += project.task('retireJenkinsJobs', type: DeleteJenkinsItemsTask)
        legacyTasks += project.task('validateJenkinsJobs', type: ValidateJenkinsItemsTask) { validate allItems }
        legacyTasks.each { task ->
            task.configure {
                description = "(Deprecated) " + description
                doFirst {
                    DeprecationLogger.nagUserWith("The ${task.name} task has been deprecated and will be removed in a future " +
                            "release of the jenkins plugin.  Use the appropriate <action>JenkinsItems task instead " +
                            "(e.g. updateJenkinsJobs => updateJenkinsItems)")
                }
            }
        }

        project.task('dumpRemoteJenkinsItems', type: DumpRemoteJenkinsItemsTask) { dump allItems }
        project.task('updateJenkinsItems', type: UpdateJenkinsItemsTask) { update allItems }
        project.task('deleteJenkinsItems', type: DeleteJenkinsItemsTask) { delete allItems }
        project.task('dumpJenkinsItems', type: DumpJenkinsItemsTask) { dump allItems }
        project.task('validateJenkinsItems', type: ValidateJenkinsItemsTask) { validate allItems }
        project.task('retireJenkinsItems', type: DeleteJenkinsItemsTask)
    }

    def applyConventions(Project project) {
        def MapJobManagement jm = new MapJobManagement(new HashMap<String, String>())

        def jobs = project.container(JenkinsJob) { name ->
            new JenkinsJob(name, jm)
        }

        def templates = project.container(JenkinsJobTemplate) { name ->
            new JenkinsJobTemplate(name, jm)
        }

        def servers = project.container(JenkinsServerDefinition) { name ->
            new JenkinsServerDefinition(name)
        }

        def views = project.container(JenkinsView) { name ->
            new JenkinsView(name, jm)
        }

        def configuration = new JenkinsConfiguration(jobs, templates, servers, views, jm)
        project.convention.plugins.jenkins = new JenkinsConfigurationConvention(configuration)
    }

    def applyServerConventionRules(Project project) {
        project.tasks.addRule("Pattern: updateJenkinsItems<ServerName>: Updates the items on a server.") { String taskName ->
            if (taskName.startsWith("updateJenkinsItems")) {
                def serverName = taskName - 'updateJenkinsItems'
                def server = project.jenkins.servers.find { it.name.capitalize() == serverName.capitalize() }
                if (server != null) {
                    project.task("updateJenkinsItems${serverName}", type: UpdateJenkinsItemsTask) {
                        servers = [server]
                        update {
                            (project.jenkins.jobs + project.jenkins.views).findAll {
                                getServerDefinitions(it).contains(server)
                            }
                        }
                    }
                }
            }
        }

        project.tasks.addRule("Pattern: deleteJenkinsItems<ServerName>: Deletes the items on a server.") { String taskName ->
            if (taskName.startsWith("deleteJenkinsItems")) {
                def serverName = taskName - 'deleteJenkinsItems'
                def server = project.jenkins.servers.find { it.name.capitalize() == serverName.capitalize() }
                if (server != null) {
                    project.task("deleteJenkinsItems${serverName}", type: DeleteJenkinsItemsTask) {
                        servers = [server]
                        delete {
                            (project.jenkins.jobs + project.jenkins.views).findAll {
                                getServerDefinitions(it).contains(server)
                            }
                        }
                    }
                }
            }
        }

        project.tasks.addRule("Pattern: validateJenkinsItems<ServerName>: Validates the items on a server.") { String taskName ->
            if (taskName.startsWith("validateJenkinsItems")) {
                def serverName = taskName - 'validateJenkinsItems'
                def server = project.jenkins.servers.find { it.name.capitalize() == serverName.capitalize() }
                if (server != null) {
                    project.task("validateJenkinsItems${serverName}", type: ValidateJenkinsItemsTask) {
                        servers = [server]
                        validate {
                            (project.jenkins.jobs + project.jenkins.views).findAll {
                                getServerDefinitions(it).contains(server)
                            }
                        }
                    }
                }
            }
        }

        project.tasks.addRule("Pattern: dumpJenkinsItems<ServerName>: Dumps the items for a server.") { String taskName ->
            if (taskName.startsWith("dumpJenkinsItems")) {
                def serverName = taskName - 'dumpJenkinsItems'
                def server = project.jenkins.servers.find { it.name.capitalize() == serverName.capitalize() }
                if (server != null) {
                    project.task("dumpJenkinsItems${serverName}", type: DumpJenkinsItemsTask) {
                        servers = [server]
                        dump {
                            (project.jenkins.jobs + project.jenkins.views).findAll {
                                getServerDefinitions(it).contains(server)
                            }
                        }
                    }
                }
            }
        }

        project.tasks.addRule("Pattern: dumpRemoteJenkinsItems<ServerName>: Dumps the remote items on a server.") { String taskName ->
            if (taskName.startsWith("dumpRemoteJenkinsItems")) {
                def serverName = taskName - 'dumpRemoteJenkinsItems'
                def server = project.jenkins.servers.find { it.name.capitalize() == serverName.capitalize() }
                if (server != null) {
                    project.task("dumpRemoteJenkinsItems${serverName}", type: DumpRemoteJenkinsItemsTask) {
                        servers = [server]
                        dump {
                            (project.jenkins.jobs + project.jenkins.views).findAll {
                                getServerDefinitions(it).contains(server)
                            }
                        }
                    }
                }
            }
        }
    }

    def checkAllServerValues(taskGraph) {
        taskGraph.getAllTasks().findAll { it instanceof AbstractJenkinsTask }.collect { AbstractJenkinsTask task ->
            if (task.needsCredentials) {
                if (task.servers != null) {
                    return task.servers
                } else {
                    return task.getAllItems().collect { item ->
                        task.getServerDefinitions(item)
                    }.flatten()
                }
            } else {
                return []
            }
        }.flatten().unique().each { JenkinsServerDefinition server ->
            server.checkDefinitionValues()
        }
    }
}
