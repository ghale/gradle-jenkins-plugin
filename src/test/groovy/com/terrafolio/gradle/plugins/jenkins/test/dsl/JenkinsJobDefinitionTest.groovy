package com.terrafolio.gradle.plugins.jenkins.test.dsl

import nebula.test.ProjectSpec

class JenkinsJobDefinitionTest extends ProjectSpec {
    def setup() {
        project.apply plugin: 'jenkins'
    }

    def "configure sets name" () {
        when:
        project.jenkins {
            jobs {
                test {
                    definition {
                        name 'Test Job'
                    }
                }
            }
        }

        then:
        project.jenkins.jobs.findByName('test').definition.name == 'Test Job'
    }

    def "configure sets xml as string" () {
        when:
        project.jenkins {
            jobs {
                test {
                    definition {
                        name 'Test Job'
                        xml '<test><test2>value</test2></test>'
                    }
                }
            }
        }

        then:
        project.jenkins.jobs.findByName('test').definition.xml == '<test><test2>value</test2></test>'
    }

    def "configure sets xml as file" () {
        setup:
        def xmlFile = project.file('test.xml')
        xmlFile.write('<test><test2>value</test2></test>')

        when:
        project.jenkins {
            jobs {
                test {
                    definition {
                        name 'Test Job'
                        xml xmlFile
                    }
                }
            }
        }

        then:
        project.jenkins.jobs.findByName('test').definition.xml == '<test><test2>value</test2></test>'
    }

    def "configure sets xml as closure" () {
        when:
        project.jenkins {
            jobs {
                test {
                    definition {
                        name 'Test Job'
                        xml {
                            test1() {
                                test2('value')
                            }
                        }
                    }
                }
            }
        }

        then:
        project.jenkins.jobs.findByName('test').definition.xml == '<test1><test2>value</test2></test1>'
    }

    def "configure overrides xml" () {
        when:
        project.jenkins {
            templates {
                test {
                    xml "<test><test2>value</test2></test>"
                }
            }
            jobs {
                test {
                    definition {
                        name 'Test Job'
                        xml templates.test.override { test ->
                            test.test2 = 'myvalue'
                        }
                    }
                }
            }
        }

        then:
        project.jenkins.jobs.findByName('test').definition.xml == '<test><test2>myvalue</test2></test>'
    }

    def "configure sets default definition name as job name" () {
        when:
        project.jenkins {
            templates {
                test {
                    xml "<test><test2>value</test2></test>"
                }
            }
            jobs {
                test2 {
                    definition {
                        xml templates.test.xml
                    }
                }
            }
        }

        then:
        project.jenkins.jobs.test2.definition.name == 'test2'
    }

}
