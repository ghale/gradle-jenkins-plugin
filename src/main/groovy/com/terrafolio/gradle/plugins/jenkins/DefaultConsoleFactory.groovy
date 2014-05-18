package com.terrafolio.gradle.plugins.jenkins

/**
 * Created by ghale on 5/18/14.
 */
class DefaultConsoleFactory implements ConsoleFactory {
    def Object getConsole() {
        return System.console()
    }
}
