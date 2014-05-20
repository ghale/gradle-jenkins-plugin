package com.terrafolio.gradle.plugins.jenkins.dsl

import com.terrafolio.gradle.plugins.jenkins.ConsoleFactory
import com.terrafolio.gradle.plugins.jenkins.DefaultConsoleFactory;

class JenkinsServerDefinition {
    def name
    def url
    def username
    def password
    def secure = true
    def console
    def ConsoleFactory consoleFactory = new DefaultConsoleFactory()

    JenkinsServerDefinition(String name) {
        this.name = name
    }

    def void setUrl(String url) {
        if (url.endsWith("/")) {
            this.url = url
        } else {
            this.url = "${url}/"
        }
    }

    def url(String url) {
        setUrl(url)
    }

    def username(String username) {
        this.username = username
    }

    def password(String password) {
        this.password = password
    }

    def secure(Boolean secure) {
        this.secure = secure
    }

    def void checkDefinitionValues() {
        if (console == null) {
            console = consoleFactory.getConsole()
        }

        if (url == null) {
            if (console != null) {
                url = console.readLine("\nEnter the URL for server \"${name}\": ", null)
            } else {
                throw new JenkinsConfigurationException("No URL defined for server \"${name}\" and no console available for input.")
            }
        }

        if (secure) {
            if (username == null) {
                if (console != null) {
                    username = console.readLine("\nEnter the username for server \"${name}\": ", null)
                } else {
                    throw new JenkinsConfigurationException("No username defined for server \"${name}\" and no console available for input.")
                }
            }

            if (password == null) {
                if (console != null) {
                    password = new String(console.readPassword("\nEnter the password for server \"${name}\": ", null))
                } else {
                    throw new JenkinsConfigurationException("No password defined for server \"${name}\" and no console available for input.")
                }
            }
        }
    }
}
