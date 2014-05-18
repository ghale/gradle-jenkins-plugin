package com.terrafolio.gradle.plugins.jenkins.test

import com.terrafolio.gradle.plugins.jenkins.DefaultConsoleFactory
import com.terrafolio.gradle.plugins.jenkins.JenkinsPlugin
import com.terrafolio.gradle.plugins.jenkins.dsl.*
import com.terrafolio.gradle.plugins.jenkins.tasks.*
import nebula.test.PluginProjectSpec
import org.gradle.api.NamedDomainObjectCollection
import org.gradle.api.execution.TaskExecutionGraph
import org.gradle.api.plugins.BasePlugin

class JenkinsPluginTest extends PluginProjectSpec {
    @Override
    String getPluginName() {
        return 'jenkins'
    }

    def setup() {
        project.apply plugin: pluginName
    }

    def "apply applies JenkinsConfigurationConvention" () {
        expect: project.convention.plugins.jenkins instanceof JenkinsConfigurationConvention
    }

    def "apply applies BasePlugin" () {
        expect: project.plugins.hasPlugin(BasePlugin.class)
    }

    def "apply adds JenkinsConfiguration" () {
        expect: project.convention.plugins.jenkins.jenkins instanceof JenkinsConfiguration
    }

    def "apply creates Jenkins Jobs Collection" () {
        expect: project.convention.plugins.jenkins.jenkins.jobs instanceof NamedDomainObjectCollection<JenkinsJob>
    }

    def "apply creates Jenkins Server Definition Collection" () {
        expect: project.convention.plugins.jenkins.jenkins.servers instanceof NamedDomainObjectCollection<JenkinsServerDefinition>
    }

    def "apply creates Jenkins Templates Collection" () {
        expect: project.convention.plugins.jenkins.jenkins.templates instanceof NamedDomainObjectCollection<JenkinsJobDefinition>
    }

    def "apply creates Jenkins Views Collection" () {
        expect: project.convention.plugins.jenkins.jenkins.views instanceof NamedDomainObjectCollection<JenkinsView>
    }

    def "apply creates updateJenkinsJobs task" () {
        expect: project.tasks.findByName('updateJenkinsJobs') instanceof UpdateJenkinsItemsTask
    }

    def "apply creates deleteJenkinsJobs task" () {
        expect: project.tasks.findByName('deleteJenkinsJobs') instanceof DeleteJenkinsItemsTask
    }

    def "apply creates dumpJenkinsJobs task" () {
        expect: project.tasks.findByName('dumpJenkinsJobs') instanceof DumpJenkinsItemsTask
    }

    def "apply creates retireJenkinsJobs task" () {
        expect: project.tasks.findByName('retireJenkinsJobs') instanceof DeleteJenkinsItemsTask
    }

    def "apply creates validateJenkinsJobs task" () {
        expect: project.tasks.findByName('validateJenkinsJobs') instanceof ValidateJenkinsItemsTask
    }

    def "apply creates updateJenkinsItems task" () {
        expect: project.tasks.findByName('updateJenkinsItems') instanceof UpdateJenkinsItemsTask
    }

    def "apply creates deleteJenkinsItems task" () {
        expect: project.tasks.findByName('deleteJenkinsItems') instanceof DeleteJenkinsItemsTask
    }

    def "apply creates dumpJenkinsItems task" () {
        expect: project.tasks.findByName('dumpJenkinsItems') instanceof DumpJenkinsItemsTask
    }

    def "apply creates retireJenkinsItems task" () {
        expect: project.tasks.findByName('retireJenkinsItems') instanceof DeleteJenkinsItemsTask
    }

    def "apply creates validateJenkinsItems task" () {
        expect: project.tasks.findByName('validateJenkinsItems') instanceof ValidateJenkinsItemsTask
    }

    def "apply creates retireJenkinsItemsTask" () {
        expect: project.tasks.findByName('retireJenkinsItems') instanceof DeleteJenkinsItemsTask
    }

    def "apply creates server convention rules" () {
        setup:
        project.jenkins.servers { test { } }

        expect:
        project.tasks.findByName('updateJenkinsItemsTest') instanceof UpdateJenkinsItemsTask
        project.tasks.findByName('deleteJenkinsItemsTest') instanceof DeleteJenkinsItemsTask
        project.tasks.findByName('validateJenkinsItemsTest') instanceof ValidateJenkinsItemsTask
        project.tasks.findByName('dumpRemoteJenkinsItemsTest') instanceof DumpRemoteJenkinsItemsTask
        project.tasks.findByName('dumpJenkinsItemsTest') instanceof DumpJenkinsItemsTask
    }

    def "apply creates server convention rules that ignore case" () {
        setup:
        project.jenkins.servers { test { } }

        expect:
        project.tasks.findByName('updateJenkinsItemstest') instanceof UpdateJenkinsItemsTask
        project.tasks.findByName('deleteJenkinsItemstest') instanceof DeleteJenkinsItemsTask
        project.tasks.findByName('validateJenkinsItemstest') instanceof ValidateJenkinsItemsTask
        project.tasks.findByName('dumpRemoteJenkinsItemstest') instanceof DumpRemoteJenkinsItemsTask
        project.tasks.findByName('dumpJenkinsItemstest') instanceof DumpJenkinsItemsTask
    }

    def "apply creates server convention rules that do not accept bad server" () {
        expect:
        project.tasks.findByName('updateJenkinsItemsBadServer') == null
    }

    def "checkAllServerValues checks only selected servers" () {
        setup:
        def JenkinsPlugin plugin = project.plugins.findPlugin('jenkins')
        def mockConsole = Mock(MockConsoleInterface)
        def testFactory = new DefaultConsoleFactory() {
            def Object getConsole() {
                return mockConsole
            }
        }
        project.jenkins.servers {
            test1 {
                url 'testUrl'
                consoleFactory = testFactory
            }
            test2 { consoleFactory = testFactory }
        }
        def graph = Stub(TaskExecutionGraph) {
            getAllTasks() >> {
                return [ project.tasks.updateJenkinsItemsTest1 ]
            }
        }

        when:
        plugin.checkAllServerValues(graph)

        then:
        with(mockConsole) {
            1 * readLine("\nEnter the username for server \"test1\": ",_) >> { return 'mockUser' }
            1 * readPassword("\nEnter the password for server \"test1\": ",_) >> { return 'mockPassword' }
            0 * _
        }
    }

    def "checkAllServerValues handles only insecure servers" () {
        setup:
        def JenkinsPlugin plugin = project.plugins.findPlugin('jenkins')
        def mockConsole = Mock(MockConsoleInterface)
        def testFactory = new DefaultConsoleFactory() {
            def Object getConsole() {
                return mockConsole
            }
        }
        project.jenkins.servers {
            test1 {
                url 'testUrl'
                secure false
                consoleFactory = testFactory
            }
            test2 { consoleFactory = testFactory }
        }
        def graph = Stub(TaskExecutionGraph) {
            getAllTasks() >> {
                return [ project.tasks.updateJenkinsItemsTest1 ]
            }
        }

        when:
        plugin.checkAllServerValues(graph)

        then:
        with(mockConsole) {
            0 * _
        }
    }

    def "checkAllServerValues handles task that does not need credentials" () {
        setup:
        def JenkinsPlugin plugin = project.plugins.findPlugin('jenkins')
        def mockConsole = Mock(MockConsoleInterface)
        def testFactory = new DefaultConsoleFactory() {
            def Object getConsole() {
                return mockConsole
            }
        }
        project.jenkins {
            servers {
                test1 {
                    url 'testUrl'
                    consoleFactory = testFactory
                }
            }
            jobs {
                job1 {
                    server servers.test1
                }
            }
        }
        def graph = Stub(TaskExecutionGraph) {
            getAllTasks() >> {
                return [ project.tasks.dumpJenkinsJobs ]
            }
        }

        when:
        plugin.checkAllServerValues(graph)

        then:
        with(mockConsole) {
            0 * _
        }
    }

    def "checkAllServerValues checks all secure servers" () {
        setup:
        def JenkinsPlugin plugin = project.plugins.findPlugin('jenkins')
        def mockConsole = Mock(MockConsoleInterface)
        def testFactory = new DefaultConsoleFactory() {
            def Object getConsole() {
                return mockConsole
            }
        }
        project.jenkins {
            servers {
                test1 {
                    url 'testUrl'
                    consoleFactory = testFactory
                }
                test2 {
                    url 'anotherUrl'
                    consoleFactory = testFactory
                }
                test3 {
                    url 'thirdUrl'
                    secure false
                    consoleFactory = testFactory
                }
            }
            jobs {
                job1 {
                    server servers.test1
                    server servers.test2
                }
                job2 {
                    server servers.test1
                    server servers.test3
                }
            }
        }
        def graph = Stub(TaskExecutionGraph) {
            getAllTasks() >> {
                return [ project.tasks.updateJenkinsItemsTest1, project.tasks.updateJenkinsItemsTest2, project.tasks.deleteJenkinsItemsTest1 ]
            }
        }

        when:
        plugin.checkAllServerValues(graph)

        then:
        with(mockConsole) {
            1 * readLine("\nEnter the username for server \"test1\": ",_) >> { return 'mockUser' }
            1 * readPassword("\nEnter the password for server \"test1\": ",_) >> { return 'mockPassword' }

            1 * readLine("\nEnter the username for server \"test2\": ",_) >> { return 'mockUser' }
            1 * readPassword("\nEnter the password for server \"test2\": ",_) >> { return 'mockPassword' }

            0 * _
        }
    }

    def "checkAllServerValues throws exception on missing username" () {
        setup:
        def JenkinsPlugin plugin = project.plugins.findPlugin('jenkins')
        project.jenkins.servers {
            test1 {
                url 'testUrl'
                password 'testpass'
            }
        }
        def graph = Stub(TaskExecutionGraph) {
            getAllTasks() >> {
                return [ project.tasks.updateJenkinsItemsTest1 ]
            }
        }

        when:
        plugin.checkAllServerValues(graph)

        then:
        thrown(JenkinsConfigurationException)
    }

    def "checkAllServerValues throws exception on missing password" () {
        setup:
        def JenkinsPlugin plugin = project.plugins.findPlugin('jenkins')
        project.jenkins.servers {
            test1 {
                url 'testUrl'
                username 'testuser'
            }
        }
        def graph = Stub(TaskExecutionGraph) {
            getAllTasks() >> {
                return [ project.tasks.updateJenkinsItemsTest1 ]
            }
        }

        when:
        plugin.checkAllServerValues(graph)

        then:
        thrown(JenkinsConfigurationException)
    }

    def "checkAllServerValues throws exception on missing url" () {
        setup:
        def JenkinsPlugin plugin = project.plugins.findPlugin('jenkins')
        project.jenkins.servers {
            test1 {
                username 'testuser'
                password 'testpass'
            }
        }
        def graph = Stub(TaskExecutionGraph) {
            getAllTasks() >> {
                return [ project.tasks.updateJenkinsItemsTest1 ]
            }
        }

        when:
        plugin.checkAllServerValues(graph)

        then:
        thrown(JenkinsConfigurationException)
    }
}

def interface MockConsoleInterface {
    String readLine(message, args)
    String readPassword(message, args)
}
