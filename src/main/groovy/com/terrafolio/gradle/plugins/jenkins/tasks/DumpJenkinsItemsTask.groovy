package com.terrafolio.gradle.plugins.jenkins.tasks

import com.terrafolio.gradle.plugins.jenkins.dsl.JenkinsConfigurable
import com.terrafolio.gradle.plugins.jenkins.service.BuildDirService

class DumpJenkinsItemsTask extends AbstractDumpJenkinsItemsTask {
    def prettyPrint = true

    public DumpJenkinsItemsTask() {
        super();
        needsCredentials = false
        description = "Dumps item configurations from the local model to files."
    }

    @Override
    public void writeXmlConfigurations(JenkinsConfigurable item, BuildDirService buildDirService, String itemType) {
        getServerDefinitions(item).each { server ->
            def file = new File(buildDirService.makeAndGetDir("${server.name}/${itemType}"), "${item.name}.xml")
            if (prettyPrint) {
                file.withWriter { fileWriter ->
                    def node = new XmlParser().parseText(item.getServerSpecificXml(server));
                    new XmlNodePrinter(new PrintWriter(fileWriter)).print(node)
                }
            } else {
                def xml = item.getServerSpecificXml(server)
                file.write(xml)
            }
        }
    }
}
