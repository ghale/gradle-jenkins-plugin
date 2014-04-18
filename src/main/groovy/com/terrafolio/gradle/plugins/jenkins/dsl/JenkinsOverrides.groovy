package com.terrafolio.gradle.plugins.jenkins.dsl

class JenkinsOverrides {
    def create = [:]
    def update = [:]
    def delete = [:]
    def get = [:]

    def create(Map map) {
        this.create = map
    }

    def update(Map map) {
        this.update = map
    }

    def delete(Map map) {
        this.delete = map
    }

    def get(Map map) {
        this.get = map
    }
}
