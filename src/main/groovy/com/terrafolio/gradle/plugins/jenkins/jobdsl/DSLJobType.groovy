package com.terrafolio.gradle.plugins.jenkins.jobdsl

import javaposse.jobdsl.dsl.Job
import javaposse.jobdsl.dsl.jobs.*


enum DSLJobType {
    Freeform(FreeStyleJob),
    Maven(MavenJob),
    Multijob(MultiJob),
    BuildFlow(BuildFlowJob),
    Workflow(WorkflowJob),
    Matrix(MatrixJob)

    final Class<? extends Job> jobClass

    DSLJobType(Class<? extends Job> jobClass) {
        this.jobClass = jobClass
    }

    static find(String enumName) {
        values().find { it.name().toLowerCase() == enumName.toLowerCase() }
    }
}