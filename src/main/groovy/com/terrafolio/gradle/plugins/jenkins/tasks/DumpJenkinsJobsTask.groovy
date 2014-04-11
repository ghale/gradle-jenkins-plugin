package com.terrafolio.gradle.plugins.jenkins.tasks

import com.terrafolio.gradle.plugins.jenkins.dsl.JenkinsConfigurable

class DumpJenkinsJobsTask extends AbstractJenkinsTask {
	def prettyPrint = true

	public DumpJenkinsJobsTask() {
		super();
		needsCredentials = false
	}

	@Override
	public void doExecute() {
		getJobs().each { job ->
			def jobDir = new File(project.buildDir, "jobs")
			if (! jobDir.exists()) {
				jobDir.mkdirs()
			}
			writeXmlConfigurations(job, jobDir)
		}

        getViews().each { view ->
            def viewDir = new File(project.buildDir, "views")
            if (! viewDir.exists()) {
                viewDir.mkdirs()
            }
            writeXmlConfigurations(view, viewDir)
        }
	}

    public void writeXmlConfigurations(JenkinsConfigurable item, File itemDir) {
        getServerDefinitions(item).each { server ->
            if (prettyPrint) {
                new File(itemDir, "${item.name}-config-${server.name}.xml").withWriter { fileWriter ->
                    def node = new XmlParser().parseText(item.getServerSpecificXml(server));
                    new XmlNodePrinter(new PrintWriter(fileWriter)).print(node)
                }
            } else {
                def xml = item.getServerSpecificXml(server)
                new File(itemDir, "${item.name}-config-${server.name}.xml")
                        .write(xml)
            }
        }
    }

}
