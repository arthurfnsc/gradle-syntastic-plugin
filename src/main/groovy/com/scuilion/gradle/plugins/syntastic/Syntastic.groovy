package com.scuilion.gradle.plugins.syntastic

import org.gradle.api.DefaultTask
import org.gradle.api.file.FileCollection
import org.gradle.api.tasks.InputFiles
import org.gradle.api.tasks.OutputFile
import org.gradle.api.tasks.SkipWhenEmpty
import org.gradle.api.tasks.TaskAction
import org.gradle.api.tasks.TaskExecutionException

class Syntastic extends DefaultTask {

    private Closure resolver

    @InputFiles
    @SkipWhenEmpty
    FileCollection getSyntasticPath() {
        resolver.call()
    }

    void setResolver(Closure closure) {
        resolver = closure
    }

    @OutputFile
    File output = project.ext.syntasticConfigFile

    String fileSeperator = File.pathSeparator

    String getValue() {
        def value = syntasticPath
        if (isCygwinEnv(project)) {
            value = cygpath()
        }
        value.join fileSeperator
    }

    @TaskAction
    void generate() {
        output.withWriter {
            it.write "let g:syntastic_java_javac_classpath = \"${value}\"\n"
        }
    }

    def isCygwinEnv(def project) {
        Utils.isCygwin && !project.hasProperty('disableCygwin')
    }

    def cygpath() {
        fileSeperator = ':'
        def cygpathOut = ''
        def results = project.exec {
            executable = 'cygpath'
            args = ['-u'] + syntasticPath
            standardOutput = new ByteArrayOutputStream()
            cygpathOut = {
                return standardOutput.toString()
            }
        }
        if (results.exitValue) {
            throw new TaskExecutionException('Failed to call cygdrive. Error: ' + results)
        }
        def cygPath = []
        cygpathOut().eachLine {
            cygPath << it
        }
        cygPath
    }

}
