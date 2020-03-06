package io.github.cosmicsilence.scalafix

import org.gradle.testkit.runner.BuildResult
import org.gradle.testkit.runner.UnexpectedBuildFailure
import org.gradle.testkit.runner.internal.DefaultGradleRunner
import org.junit.Rule
import org.junit.rules.TemporaryFolder

import spock.lang.Specification

class ScalafixPluginGrettyTest extends Specification {

    @Rule
    public final TemporaryFolder testProjectDir = new TemporaryFolder()
    private File settingsFile
    private File buildFile
    private File scalafixConfFile

    def setup() {
        settingsFile = testProjectDir.newFile('settings.gradle')
        settingsFile.write "rootProject.name = 'hello-world'"

        buildFile = testProjectDir.newFile('build.gradle')
        buildFile.write '''
plugins {
    id 'scala'
     id 'org.scoverage' version "3.2.0"
    id 'io.github.cosmicsilence.scalafix'
    id 'org.gretty' version "2.3.1"
}

repositories {
    mavenCentral()
}

dependencies {
    implementation "org.scala-lang:scala-library:2.12.10"
}

tasks.withType(ScalaCompile) {
    // needed for RemoveUnused rule
    scalaCompileOptions.additionalParameters = [ "-Ywarn-unused" ]
}
'''

        scalafixConfFile = testProjectDir.newFile('.scalafix.conf')
    }

    def 'scalafixMain task should run compileScala by default'() {
        when:
        BuildResult buildResult = runGradle('tasks')

        then:
        println(buildResult.output)
        buildResult.output.contains('BUILD SUCCESSFUL')
    }


    private BuildResult runGradle(String... arguments) {
        return new DefaultGradleRunner()
                .withJvmArguments('-XX:MaxMetaspaceSize=500m')
                .withProjectDir(testProjectDir.getRoot())
                .withArguments(arguments.toList())
                .withGradleVersion(System.getProperty('compat.gradle.version'))
                .withPluginClasspath()
                .forwardOutput()
                .build()
    }

    private File createSourceFile(String content, String sourceSet = 'main') {
        def pkgFolder = new File(testProjectDir.root, "src/$sourceSet/scala/dummy")

        if (!pkgFolder.exists()) pkgFolder.mkdirs()

        def scalaSrcFile = new File(pkgFolder, "source_${new Random().nextInt(1000)}.scala")
        scalaSrcFile.write content
        scalaSrcFile
    }
}
