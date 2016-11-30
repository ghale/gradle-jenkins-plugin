package com.terrafolio.gradle.plugins.jenkins.integTest

import org.gradle.testkit.runner.BuildResult
import org.gradle.testkit.runner.TaskOutcome

class TaskResults {
    final BuildResult result

    TaskResults(BuildResult result) {
        this.result = result
    }

    public boolean isAllSucceeded() {
        return result.tasks.every { task -> task.outcome == TaskOutcome.SUCCESS }
    }

    public boolean isAllFailed() {
        return result.tasks.every { task -> task.outcome == TaskOutcome.FAILED }
    }

    public getOutput() {
        return result.output
    }
}
