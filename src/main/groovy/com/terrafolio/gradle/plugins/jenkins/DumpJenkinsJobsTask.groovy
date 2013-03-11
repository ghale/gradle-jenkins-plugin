package com.terrafolio.gradle.plugins.jenkins

class DumpJenkinsJobsTask extends AbstractJenkinsTask {

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
                new File(jobDir, "${job.name}-config.xml").withWriter { fileWriter ->
                        def node = new XmlParser().parseText(job.definition.xml);
                        new XmlNodePrinter(new PrintWriter(fileWriter)).print(node)
                }
        }
		
	}

}
