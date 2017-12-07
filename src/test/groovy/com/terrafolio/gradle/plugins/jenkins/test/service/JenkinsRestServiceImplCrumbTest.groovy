package com.terrafolio.gradle.plugins.jenkins.test.service

import com.terrafolio.gradle.plugins.jenkins.service.Crumb
import com.terrafolio.gradle.plugins.jenkins.service.JenkinsRESTServiceImpl
import groovy.json.JsonParserType
import groovy.json.JsonSlurper
import groovyx.net.http.AuthConfig
import groovyx.net.http.HttpResponseDecorator
import groovyx.net.http.RESTClient
import org.apache.http.HttpResponse
import org.apache.http.HttpVersion
import org.apache.http.message.BasicHttpResponse
import org.apache.http.message.BasicStatusLine
import spock.lang.Specification

class JenkinsRestServiceImplCrumbTest extends Specification {

    def RESTClient mockRESTClient
    def JenkinsRESTServiceImpl service
    def url = "http://testurl.availity.com"
    def username = "testuser"
    def password = "testpassword"

    def setup() {
        mockRESTClient = Mock(RESTClient)
        service = new JenkinsRESTServiceImpl(url, username, password)
        service.client = mockRESTClient
    }

    def "CSRF protection on jenkins enabled" () {
        setup:
            def mockAuth = Mock(AuthConfig)
            mockAuth.basic(username, password) >> {}
            mockRESTClient.getAuth() >> { mockAuth }
            mockRESTClient.get(_) >> { Map map ->
                HttpResponse baseResponse = new BasicHttpResponse(new BasicStatusLine(HttpVersion.HTTP_1_1, 200, "OK"))
                HttpResponseDecorator response = new HttpResponseDecorator(baseResponse,
                        ["crumbRequestField": "jenkins-crumb",
                        "crumb": "CRCRCR"])
                return response
            }
            Crumb actualCrumb = service.getCrumb(mockRESTClient)
        expect:
            actualCrumb.crumbRequestField == "jenkins-crumb"
            actualCrumb.crumb == "CRCRCR"


    }

    def "There is no CSRF protection on jenkins" () {
        setup:
            def mockAuth = Mock(AuthConfig)
            mockAuth.basic(username, password) >> {}
            mockRESTClient.getAuth() >> { mockAuth }
            mockRESTClient.get(_) >> { Map map ->
                HttpResponse baseResponse = new BasicHttpResponse(new BasicStatusLine(HttpVersion.HTTP_1_1, 404, "OK"))
                HttpResponseDecorator response = new HttpResponseDecorator(baseResponse,[:])
                return response
            }
            Crumb actualCrumb = service.getCrumb(mockRESTClient)

        expect:
            actualCrumb.crumbRequestField == ""
            actualCrumb.crumb == ""


    }

    def "empty crumb is parsed correctly" () {
        setup:
            def jsonSlurper = new JsonSlurper()
            def crumbJson = jsonSlurper.parseText("{\"_class\":\"hudson.security.csrf.DefaultCrumbIssuer\",\"crumb\":\"\",\"crumbRequestField\":\"\"}");
        expect:
            crumbJson.crumb == ""
            crumbJson.crumbRequestField == ""
    }

    def "crumb xml parsed correctly"() {
        setup:
            String xml = '''<defaultCrumbIssuer _class="hudson.security.csrf.DefaultCrumbIssuer"><crumb>d02a5f6e61f061279ef45cba5028f3e5</crumb><crumbRequestField>Jenkins-Crumb</crumbRequestField></defaultCrumbIssuer>'''
            Crumb crumb = new XmlParser().parse(xml);
            System.out.println("SB:" + xml)
        expect:
            crumbJson.crumb == "2e76182265083db06b744fb38dd42b5c"
            crumbJson.crumbRequestField == "Jenkins-Crumb"
    }
}
