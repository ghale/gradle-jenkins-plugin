package com.terrafolio.gradle.plugins.jenkins

class DumpJenkinsJobsTask extends AbstractJenkinsTask {
	def prettyPrint = true

	public DumpJenkinsJobsTask() {
		super();
		needsCredentials = false
	}

	@Override
	public void doExecute() {
		project.jenkins.jobs.each { job ->
			def jobDir = new File(project.buildDir, "jobs")
			if (! jobDir.exists()) {
				jobDir.mkdirs()
			}
			getServerDefinitions(job).each { server ->
				if (prettyPrint) {
					new File(jobDir, "${job.name}-config-${server.name}.xml").withWriter { fileWriter ->
						def node = new XmlParser().parseText(job.getServerSpecificDefinition(server).xml);
						new XmlNodePrinter(new PrintWriter(fileWriter)).print(node)
					}
				} else {
					def xml = job.getServerSpecificDefinition(server).xml
					new File(jobDir, "${job.name}-config-${server.name}.xml")
						.write(xml)
				}
			}
		}
	}

}
