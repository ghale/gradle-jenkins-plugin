package com.terrafolio.gradle.plugins.jenkins.dsl

import javaposse.jobdsl.dsl.*
import org.gradle.api.NamedDomainObjectContainer
import org.gradle.api.file.FileCollection

class JenkinsConfiguration {
    private final NamedDomainObjectContainer<JenkinsJob> jobs
    private final NamedDomainObjectContainer<JenkinsServerDefinition> servers
    private final NamedDomainObjectContainer<JenkinsJobTemplate> templates
    private final NamedDomainObjectContainer<JenkinsView> views
    private final JobManagement jobManagement

    def defaultServer

    public JenkinsConfiguration(NamedDomainObjectContainer<JenkinsJob> jobs,
                                NamedDomainObjectContainer<JenkinsJobTemplate> templates,
                                NamedDomainObjectContainer<JenkinsServerDefinition> servers,
                                NamedDomainObjectContainer<JenkinsView> views,
                                JobManagement jobManagement) {
        this.jobs = jobs
        this.servers = servers
        this.templates = templates
        this.views = views
        this.jobManagement = jobManagement
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

    def views(Closure closure) {
        views.configure(closure)
    }

    def defaultServer(JenkinsServerDefinition server) {
        this.defaultServer = server
    }

    def dsl(FileCollection files) {
        jobs.each { job ->
            if (job.definition != null && job.definition.xml != null) {
                jobManagement.createOrUpdateConfig(job.definition.name, job.definition.xml, true)
            }
        }

        DslScriptLoader scriptLoader = new DslScriptLoader(jobManagement)
        Collection<ScriptRequest> scriptRequests = files.collect { dslFile -> new ScriptRequest(dslFile.text) }
        GeneratedItems generatedItems = scriptLoader.runScripts(scriptRequests)

        generatedItems.getJobs().each { generatedJob ->
            def JenkinsJob job = jobs.findByName(generatedJob.jobName)
            if (job == null) {
                job = new JenkinsJob(generatedJob.jobName, jobManagement)
            }
            job.definition = new JenkinsJobDefinition(generatedJob.jobName)
            job.definition.xml jobManagement.getConfig(generatedJob.jobName)
            jobs.add(job)
        }

        generatedItems.getViews().each { GeneratedView generatedView ->
            def JenkinsView view = views.findByName(generatedView.name)
            if (view == null) {
                view = new JenkinsView(generatedView.name, jobManagement)
            }
            view.xml = jobManagement.getConfig(generatedView.name)
            views.add(view)
        }

    }

    def dsl(Closure closure) {
        def JobParent jobParent = new JobParent() {
            def run() { }
        }
        jobParent.setJm(jobManagement)
        jobParent.with(closure)

        jobParent.getReferencedJobs().each { referencedJob ->
            jobManagement.createOrUpdateConfig(referencedJob.name, referencedJob.xml, true)
            def JenkinsJob job = jobs.findByName(referencedJob.name)
            if (job == null) {
                job = new JenkinsJob(referencedJob.name, jobManagement)
            }
            job.definition = new JenkinsJobDefinition(referencedJob.name)
            job.definition.xml referencedJob.xml
            jobs.add(job)
        }

        jobParent.getReferencedViews().each { referencedView ->
            jobManagement.createOrUpdateView(referencedView.name, referencedView.xml, true)
            def JenkinsView view = jobs.findByName(referencedView.name)
            if (view == null) {
                view = new JenkinsView(referencedView.name, jobManagement)
            }
            view.xml = referencedView.xml
            views.add(view)
        }
    }
}
