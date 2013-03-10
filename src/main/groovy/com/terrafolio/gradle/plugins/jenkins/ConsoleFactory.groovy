package com.terrafolio.gradle.plugins.jenkins

class ConsoleFactory {
	def static Console getConsole() {
		return System.console()
	}
}
