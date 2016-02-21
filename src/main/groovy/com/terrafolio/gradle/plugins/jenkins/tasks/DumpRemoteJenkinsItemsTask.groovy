package com.terrafolio.gradle.plugins.jenkins.tasks

import com.terrafolio.gradle.plugins.jenkins.dsl.JenkinsConfigurable
import com.terrafolio.gradle.plugins.jenkins.dsl.JenkinsServerDefinition
import com.terrafolio.gradle.plugins.jenkins.service.BuildDirService
import com.terrafolio.gradle.plugins.jenkins.service.JenkinsService

class DumpRemoteJenkinsItemsTask extends AbstractDumpJenkinsItemsTask {
    def prettyPrint = false
    def prettyPrintPreserveWhitespace = true;

    DumpRemoteJenkinsItemsTask() {
        description = "Dumps remote item configurations from server(s) to files."
    }

    @Override
    public void writeXmlConfigurations(JenkinsConfigurable item, BuildDirService buildDir, String itemTypeDir) {
        eachServer(item) { JenkinsServerDefinition server, JenkinsService service ->
            String serverStrItem = service.getConfiguration(item.configurableName, item.serviceOverrides.get)

            if (serverStrItem == null) {
                return
            }

            def file = new File(buildDir.makeAndGetDir("remotes/${server.name}/$itemTypeDir"), "${item.name}.xml")

            if (prettyPrint) {
                file.withWriter { fileWriter ->
                    def node = new XmlParser().parseText(serverStrItem);
                    def nodePrinter = new XmlNodePrinter(new PrintWriter(fileWriter))
                    nodePrinter.preserveWhitespace = prettyPrintPreserveWhitespace;
                    nodePrinter.print(node)
                }
            } else {
                file.write(serverStrItem)
            }
        }
    }
}
