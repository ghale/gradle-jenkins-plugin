package com.terrafolio.gradle.plugins.jenkins.tasks

import com.terrafolio.gradle.plugins.jenkins.dsl.JenkinsConfigurable
import com.terrafolio.gradle.plugins.jenkins.dsl.JenkinsServerDefinition
import com.terrafolio.gradle.plugins.jenkins.service.JenkinsService
import org.custommonkey.xmlunit.DetailedDiff
import org.custommonkey.xmlunit.Diff
import org.custommonkey.xmlunit.Difference
import org.custommonkey.xmlunit.XMLUnit

class ValidateJenkinsItemsTask extends AbstractJenkinsTask {
    def failOnDifference = true

    ValidateJenkinsItemsTask() {
        description = "Validates item configurations in the local model against those on the server(s)."
    }

    @Override
    public void doExecute() {
        def success = true
        getAllItems().each { JenkinsConfigurable item ->
            eachServer(item) { JenkinsServerDefinition server, JenkinsService service ->
                def serverJob = service.getConfiguration(item.configurableName, item.serviceOverrides.get)
                if (serverJob == null) {
                    logger.warn('Jenkins item ' + item.configurableName + ' does not exist on ' + server.url)
                    success = false
                } else {
                    XMLUnit.setIgnoreWhitespace(true)
                    def xmlDiff = new DetailedDiff(new Diff(item.getServerSpecificXml(server), serverJob))
                    if (xmlDiff.similar()) {
                        logger.info('Jenkins item ' + item.configurableName + ' matches the version on ' + server.url)
                    } else {
                        logger.warn('Jenkins item ' + item.configurableName + ' differs from the version on ' + server.url)
                        xmlDiff.getAllDifferences().each { Difference difference ->
                            logger.info(difference.toString())
                        }
                        success = false
                    }
                }
            }
        }

        if ((! success) && failOnDifference) {
            throw new JenkinsValidationException("Jenkins job validation failed!", null)
        }
    }

    def validate(JenkinsConfigurable item) {
        items += item
    }

    def validate(Closure closure) {
        itemClosures += closure
    }
}
