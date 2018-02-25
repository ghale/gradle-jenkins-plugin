package com.terrafolio.gradle.plugins.jenkins.test.service

import com.terrafolio.gradle.plugins.jenkins.service.Crumb
import com.terrafolio.gradle.plugins.jenkins.service.JenkinsRESTServiceImpl
import groovy.json.JsonParserType
import groovy.json.JsonSlurper
import groovyx.net.http.AuthConfig
import groovyx.net.http.HttpResponseDecorator
import groovyx.net.http.HttpResponseException
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
                //TODO : Make another test with thrown exception
                // groovyx.net.http.HttpResponseException: Not Found
                HttpResponse baseResponse = new BasicHttpResponse(new BasicStatusLine(HttpVersion.HTTP_1_1, 404, "OK"))
                HttpResponseDecorator response = new HttpResponseDecorator(baseResponse,[:])
                return response
            }
            Crumb actualCrumb = service.getCrumb(mockRESTClient)

        expect:
            actualCrumb.crumbRequestField == ""
            actualCrumb.crumb == ""
    }

    def "Empty crumb produces correct map" () {
        setup:
            Crumb crumb = new Crumb()

        expect:
            crumb.crumbRequestField == ""
            crumb.crumb == ""

            crumb.toMap().size() == 0
    }
}
