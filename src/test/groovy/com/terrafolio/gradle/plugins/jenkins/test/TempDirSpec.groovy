package com.terrafolio.gradle.plugins.jenkins.test

import org.junit.Rule
import org.junit.rules.TemporaryFolder
import spock.lang.Specification

/**
 * Created on 6/27/14.
 */
class TempDirSpec extends Specification {
    @Rule TemporaryFolder tempDir = new TemporaryFolder()

    def File file(String path, String contents=null) {
        File file = tempDir.newFile(path)
        if (contents) {
            file << contents
        }
        return file
    }

    def File dir(String path) {
        File file = tempDir.newFolder(path)
        file.mkdirs()
        return file
    }
}
