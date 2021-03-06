/*
 * Copyright 2018 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.gradle.buildinit.plugins

import org.gradle.buildinit.plugins.fixtures.ScriptDslFixture
import org.gradle.test.fixtures.file.LeaksFileHandles
import org.gradle.util.Requires
import org.gradle.util.TestPrecondition
import spock.lang.Unroll

import static org.gradle.buildinit.plugins.internal.modifiers.BuildInitDsl.KOTLIN

@Requires(TestPrecondition.JDK8_OR_LATER)
class KotlinLibraryInitIntegrationTest extends AbstractInitIntegrationSpec {

    public static final String SAMPLE_LIBRARY_CLASS = "some/thing/Library.kt"
    public static final String SAMPLE_LIBRARY_TEST_CLASS = "some/thing/LibraryTest.kt"

    def "defaults to kotlin build scripts"() {
        when:
        run ('init', '--type', 'kotlin-library')

        then:
        dslFixtureFor(KOTLIN).assertGradleFilesGenerated()
    }

    @LeaksFileHandles
    @Unroll
    def "creates sample source if no source present with #scriptDsl build scripts"() {
        when:
        run('init', '--type', 'kotlin-library', '--dsl', scriptDsl.id)

        then:
        targetDir.file("src/main/kotlin").assertHasDescendants(SAMPLE_LIBRARY_CLASS)
        targetDir.file("src/test/kotlin").assertHasDescendants(SAMPLE_LIBRARY_TEST_CLASS)

        and:
        commonFilesGenerated(scriptDsl)

        when:
        executer.expectDeprecationWarning()
        run("build")

        then:
        assertTestPassed("some.thing.LibraryTest", "testSomeLibraryMethod")

        where:
        scriptDsl << ScriptDslFixture.SCRIPT_DSLS
    }

    @Unroll
    def "creates sample source with package and #scriptDsl build scripts"() {
        when:
        run('init', '--type', 'kotlin-library', '--package', 'my.lib', '--dsl', scriptDsl.id)

        then:
        targetDir.file("src/main/kotlin").assertHasDescendants("my/lib/Library.kt")
        targetDir.file("src/test/kotlin").assertHasDescendants("my/lib/LibraryTest.kt")

        and:
        commonFilesGenerated(scriptDsl)

        when:
        executer.expectDeprecationWarning()
        run("build")

        then:
        assertTestPassed("my.lib.LibraryTest", "testSomeLibraryMethod")

        where:
        scriptDsl << ScriptDslFixture.SCRIPT_DSLS
    }

    @Unroll
    def "source generation is skipped when kotlin sources detected with #scriptDsl build scripts"() {
        setup:
        targetDir.file("src/main/kotlin/org/acme/SampleMain.kt") << """
            package org.acme

            class SampleMain {
            }
    """
        targetDir.file("src/test/kotlin/org/acme/SampleMainTest.kt") << """
                    package org.acme

                    class SampleMainTest {
                    }
            """
        when:
        run('init', '--type', 'kotlin-library', '--dsl', scriptDsl.id)

        then:
        targetDir.file("src/main/kotlin").assertHasDescendants("org/acme/SampleMain.kt")
        targetDir.file("src/test/kotlin").assertHasDescendants("org/acme/SampleMainTest.kt")
        dslFixtureFor(scriptDsl).assertGradleFilesGenerated()

        when:
        executer.expectDeprecationWarning()
        run("build")

        then:
        executed(":test")

        where:
        scriptDsl << ScriptDslFixture.SCRIPT_DSLS
    }
}
