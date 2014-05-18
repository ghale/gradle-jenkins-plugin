package com.terrafolio.gradle.plugins.jenkins.tasks

import com.terrafolio.gradle.plugins.jenkins.dsl.JenkinsConfigurable
import com.terrafolio.gradle.plugins.jenkins.dsl.JenkinsJob
import com.terrafolio.gradle.plugins.jenkins.dsl.JenkinsServerDefinition
import com.terrafolio.gradle.plugins.jenkins.dsl.JenkinsView
import com.terrafolio.gradle.plugins.jenkins.service.JenkinsService

class DeleteJenkinsItemsTask extends AbstractJenkinsTask {
    DeleteJenkinsItemsTask() {
        super()
        description = "Deletes items from the server(s)."
    }

    def void doExecute() {
        getAllItems().each { JenkinsConfigurable item ->
            eachServer(item) { JenkinsServerDefinition server, JenkinsService service ->
                def existing = service.getConfiguration(item.configurableName, item.serviceOverrides.get)
                if (existing != null) {
                    logger.warn('Deleting item ' + item.configurableName + ' on ' + server.url)
                    service.deleteConfiguration(item.configurableName, item.serviceOverrides.delete)
                } else {
                    logger.warn('Jenkins item ' + item.configurableName + ' does not exist on ' + server.url)
                }
            }
        }
    }

    def void delete(JenkinsConfigurable item) {
        items += item
    }

    def void delete(Closure closure) {
        itemClosures += closure
    }

    def void delete(JenkinsServerDefinition server, String jobName) {
        def job = new JenkinsJob(jobName, null)
        job.server server
        job.definition {
            name jobName
        }
        delete(job)
    }

    def void deleteView(JenkinsServerDefinition server, String viewName) {
        def view = new JenkinsView(viewName, null)
        view.server server
        delete(view)
    }
}
