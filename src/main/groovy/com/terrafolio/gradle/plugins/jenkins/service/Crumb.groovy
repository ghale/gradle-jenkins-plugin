package com.terrafolio.gradle.plugins.jenkins.service

class Crumb {

    String crumbRequestField = "";
    String crumb = "";

    Map<String, String> toMap() {
        Map<String, String> map = new HashMap<>()
        if (crumb != "") {
            map.put(crumbRequestField, crumb)
        }
        return map
    }


}
