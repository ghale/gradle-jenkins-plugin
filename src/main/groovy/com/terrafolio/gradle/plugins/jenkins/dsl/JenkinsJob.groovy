package com.terrafolio.gradle.plugins.jenkins.dsl

import javaposse.jobdsl.dsl.*
import org.gradle.util.ConfigureUtil

class JenkinsJob {
	def name
	def serverDefinitions = []
	def serverSpecificConfiguration = [:]
	def definition
	def serviceOverrides = new JenkinsOverrides()
    protected JobManagement jm
	
	JenkinsJob(String name, JobManagement jm) {
		this.name = name
        this.jm = jm
	}
	
	def definition(JenkinsJobDefinition definition) {
		setDefinition(definition)
	}

    def setDefinition(JenkinsJobDefinition definition) {
        this.definition = definition
        if (definition.xml != null) {
            jm.createOrUpdateConfig(name, definition.xml, true)
        }
    }
	
	def definition(Closure closure) {
		if (this.definition == null) {
			this.definition = new JenkinsJobDefinition(name)
		}
		ConfigureUtil.configure(closure, definition)
        if (definition.xml != null) {
            jm.createOrUpdateConfig(name, definition.xml, true)
        }
	}

    def dsl(File dslFile) {
        jm.getParameters().put("GRADLE_JOB_NAME", name)

        ScriptRequest request = new ScriptRequest(dslFile.name, null, dslFile.parentFile.toURI().toURL(), false)
        GeneratedItems generatedItems = DslScriptLoader.runDslEngine(request, jm)

        if (generatedItems.jobs.size() != 1) {
            throw new JenkinsConfigurationException("The DSL script ${dslFile.path} did not generate exactly one job (${generatedItems.jobs.size()})!  Use the jobs dsl form to generate multiple jobs from dsl.")
        } else {
            GeneratedJob generatedJob = generatedItems.getJobs().iterator().next()
            this.definition = new JenkinsJobDefinition(generatedJob.jobName==null?name:generatedJob.jobName)
            this.definition.xml jm.getConfig(generatedJob.jobName)
        }
    }

    def dsl(Closure closure) {
        jm.getParameters().put("GRADLE_JOB_NAME", name)

        def Job job = new Job(jm)
        // Load the existing xml as a template if it exists
        if (jm.getConfig(name) && job.templateName == null) {
            job.using(name)
        }
        job.with(closure)
        jm.createOrUpdateConfig(name, job.xml, true)
        this.definition = new JenkinsJobDefinition(job.name==null?name:job.name)
        this.definition.xml job.xml
    }
	
	def serviceOverrides(JenkinsOverrides overrides) {
		this.serviceOverrides = overrides
	}
	
	def serviceOverrides(Closure closure) {
		if (this.serviceOverrides == null) {
			this.serviceOverrides = new JenkinsOverrides()
		}
		ConfigureUtil.configure(closure, serviceOverrides)
	}
	
	def server(JenkinsServerDefinition server) {
		if (! serverDefinitions.contains(server)) {
			serverDefinitions += server
		}
	}
	
	def server(JenkinsServerDefinition server, Closure closure) {
		this.server(server)
		if (! serverSpecificConfiguration.containsKey(server)) {
			serverSpecificConfiguration[server] = []
		}
		serverSpecificConfiguration[server] += closure
	}
	
	def getServerSpecificDefinition(JenkinsServerDefinition server) {
		if (serverSpecificConfiguration.containsKey(server)) {
			def newDefinition = new JenkinsJobDefinition(definition.name, definition.xml)
			
			serverSpecificConfiguration[server].each { closure -> 
				ConfigureUtil.configure(closure, newDefinition)
			}
			
			return newDefinition
		} else {
			return definition
		}
	}
}
