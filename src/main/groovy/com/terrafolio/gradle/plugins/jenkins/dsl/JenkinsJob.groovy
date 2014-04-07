package com.terrafolio.gradle.plugins.jenkins.dsl

import com.terrafolio.gradle.plugins.jenkins.jobdsl.MapJobManagement
import javaposse.jobdsl.dsl.DslScriptLoader
import javaposse.jobdsl.dsl.GeneratedItems
import javaposse.jobdsl.dsl.GeneratedJob
import javaposse.jobdsl.dsl.JobManagement
import javaposse.jobdsl.dsl.Job
import javaposse.jobdsl.dsl.ScriptRequest
import org.gradle.util.ConfigureUtil


class JenkinsJob {
	def name
	def serverDefinitions = []
	def serverSpecificConfiguration = [:]
	def definition
	def serviceOverrides = new JenkinsOverrides()
	
	JenkinsJob(String name) {
		this.name = name
	}
	
	def definition(JenkinsJobDefinition definition) {
		this.definition = definition
	}
	
	def definition(Closure closure) {
		if (this.definition == null) {
			this.definition = new JenkinsJobDefinition(name)
		}
		ConfigureUtil.configure(closure, definition)
	}

    def dsl(File dslFile) {
        Map configMap = new HashMap<String, String>()
        if (definition != null && definition.xml != null) {
            configMap.put(name, definition.xml)
        }
        JobManagement jm = new MapJobManagement(configMap)
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
        Map configMap = new HashMap<String, String>()
        // Load the existing xml into the configMap
        if (definition != null && definition.xml != null) {
            configMap.put(name, definition.xml)
        }
        JobManagement jm = new MapJobManagement(configMap)
        jm.getParameters().put("GRADLE_JOB_NAME", name)

        def Job job = new Job(jm)
        // Load the existing xml as a template if it exists
        if (configMap.containsKey(name) && job.templateName == null) {
            job.using(name)
        }
        job.with(closure)
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
