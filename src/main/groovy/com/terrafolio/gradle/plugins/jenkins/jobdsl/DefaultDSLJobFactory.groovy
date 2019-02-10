package com.terrafolio.gradle.plugins.jenkins.jobdsl

import javaposse.jobdsl.dsl.Job
import javaposse.jobdsl.dsl.JobManagement

class DefaultDSLJobFactory implements DSLJobFactory {
    @Override
    Job create(JobManagement management, String type, String jobName) {
        Class<? extends Job> jobClass
        if (type == null) {
            jobClass = DSLJobType.Freeform.jobClass
        } else {
            jobClass = DSLJobType.find(type).jobClass
        }
        
        return jobClass.newInstance(management, jobName)
    }
}
