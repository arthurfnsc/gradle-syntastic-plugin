package com.scuilion.gradle.plugins.syntastic

import org.junit.Test
import org.junit.BeforeClass
import static org.junit.Assume.assumeTrue
import static org.junit.Assume.assumeFalse

import org.gradle.tooling.GradleConnectionException
import org.gradle.tooling.GradleConnector
import org.apache.commons.lang3.SystemUtils

class SyntasticTest {

    private static final File INTEGRATION_BUILD_LOCATION =
        new File(System.getProperty('integrationTest.location'), 'syntasticTest')
    private static final File SYNTASTIC_CONFIG = new File(INTEGRATION_BUILD_LOCATION, ".syntastic_javac_config")
    private static listOfFiles

    @BeforeClass
    static void checkGeneate() {
        def connection =
            GradleConnector.newConnector().forProjectDirectory(INTEGRATION_BUILD_LOCATION).connect()
        def output = new ByteArrayOutputStream()
        def error = new ByteArrayOutputStream()
        try {
            def build = connection.newBuild()
            build.setStandardOutput(output)
            build.setStandardError(error)
            build.forTasks('syntastic')
            build.run()
            listOfFiles = matcher[0][1].tokenize(File.pathSeparator)
        } catch (Exception e) {
            def x = 'x' * 30
            println x + ' Standard Output ' + x
            println output
            println x + ' Standard Error  ' + x
            println error
            println x + ' Standard End    ' + x
            throw new GradleConnectionException("Build execution failed.", e)
        } finally {
            connection.close()
        }
    }

    @Test
    void syntatsicConfigGotCreated() {
        assert SYNTASTIC_CONFIG.exists()
        assert SYNTASTIC_CONFIG.isFile()
    }

    @Test
    void nixSpecificCheck() {
        assumeFalse(SystemUtils.IS_OS_WINDOWS)
        assert !listOfFiles.empty
        hasGuavaFile()
        assert !SYNTASTIC_CONFIG.text.contains("\\")
        assert !SYNTASTIC_CONFIG.text.contains(';')
    }

    @Test
    void windowsSpecificCheck() {
        assumeTrue(SystemUtils.IS_OS_WINDOWS)
        assert !listOfFiles.empty
        hasGuavaFile()
        assert !SYNTASTIC_CONFIG.text.contains('/')
        assert SYNTASTIC_CONFIG.text.contains(';')
    }

    @Test
    void foundSomethingInSyntasticConfig() {
        assert matcher.find() == true
    }

    private void hasGuavaFile() {
        listOfFiles.each {
            if (it.contains('guava')) {
                assert new File(it).exists()
                assert new File(it).isFile()
            }
        }
    }

    private static getMatcher() {
        return SYNTASTIC_CONFIG.text =~ /"([^"]*)"/
    }
}
