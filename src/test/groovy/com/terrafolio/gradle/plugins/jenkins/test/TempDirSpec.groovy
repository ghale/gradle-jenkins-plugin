package com.terrafolio.gradle.plugins.jenkins.test

import com.energizedwork.spock.extensions.TempDirectory
import spock.lang.Ignore
import spock.lang.Specification

/**
 * Created on 6/27/14.
 */
class TempDirSpec extends Specification {
    @TempDirectory(clean=false) File tempdir

    @Ignore
    def File file(String path, String contents=null) {
        File file = new File(tempdir, path)
        if (contents) {
            file << contents
        }
        return file
    }

    @Ignore
    def File dir(String path) {
        File file = new File(tempdir, path)
        file.mkdirs()
        return file
    }
}
