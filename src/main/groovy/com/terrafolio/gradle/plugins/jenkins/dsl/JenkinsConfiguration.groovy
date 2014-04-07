package com.terrafolio.gradle.plugins.jenkins.dsl

import com.terrafolio.gradle.plugins.jenkins.jobdsl.MapJobManagement
import javaposse.jobdsl.dsl.DslScriptLoader
import javaposse.jobdsl.dsl.GeneratedItems
import javaposse.jobdsl.dsl.GeneratedJob
import javaposse.jobdsl.dsl.JobManagement
import javaposse.jobdsl.dsl.JobParent
import javaposse.jobdsl.dsl.ScriptRequest
import org.gradle.api.NamedDomainObjectContainer
import org.gradle.api.file.FileCollection
import org.gradle.util.Configurable


class JenkinsConfiguration {
	private final NamedDomainObjectContainer<JenkinsJob> jobs
	private final NamedDomainObjectContainer<JenkinsServerDefinition> servers
	private final NamedDomainObjectContainer<JenkinsJobDefinition> templates
	
	def defaultServer
	
	public JenkinsConfiguration(NamedDomainObjectContainer<JenkinsJob> jobs, NamedDomainObjectContainer<JenkinsJobDefinition> templates, NamedDomainObjectContainer<JenkinsServerDefinition> servers) {
		this.jobs = jobs
		this.servers = servers
		this.templates = templates
	}
	
	def jobs(Closure closure) {
		jobs.configure(closure)
	}
	
	def templates(Closure closure) {
		templates.configure(closure)
	}
	
	def servers(Closure closure) {
		servers.configure(closure)
	}
	
	def defaultServer(JenkinsServerDefinition server) {
		this.defaultServer = server
	}

    def dsl(FileCollection files) {
        Map configMap = new HashMap<String, String>()
        jobs.each { job ->
            if (job.definition != null && job.definition.xml != null) {
                configMap.put(job.definition.name, job.definition.xml)
            }
        }
        JobManagement jm = new MapJobManagement(configMap)

        files.each { dslFile ->
            ScriptRequest request = new ScriptRequest(dslFile.name, null, dslFile.parentFile.toURI().toURL(), false)
            GeneratedItems generatedItems = DslScriptLoader.runDslEngine(request, jm)

            generatedItems.getJobs().each { generatedJob ->
                def JenkinsJob job = jobs.findByName(generatedJob.jobName)
                if (job == null) {
                    job = new JenkinsJob()
                }
                job.name = generatedJob.jobName
                job.definition = new JenkinsJobDefinition(generatedJob.jobName)
                job.definition.xml jm.getConfig(generatedJob.jobName)
                jobs.add(job)
            }
        }
    }

    def dsl(Closure closure) {
        Map configMap = new HashMap<String, String>()
        jobs.each { job ->
            if (job.definition != null && job.definition.xml != null) {
                configMap.put(job.definition.name, job.definition.xml)
            }
        }
        JobManagement jm = new MapJobManagement(configMap)

        def JobParent jobParent = new JobParent() { def run() { } }
        jobParent.setJm(jm)
        jobParent.with(closure)

        jobParent.getReferencedJobs().each { referencedJob ->
            def JenkinsJob job = jobs.findByName(referencedJob.name)
            if (job == null) {
                job = new JenkinsJob()
            }
            job.name = referencedJob.name
            job.definition = new JenkinsJobDefinition(referencedJob.name)
            job.definition.xml referencedJob.xml
            jobs.add(job)
        }
    }
}
