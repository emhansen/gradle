/*
 * Copyright 2012 the original author or authors.
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

package org.gradle.api.internal.artifacts.result

import spock.lang.Specification

import static org.gradle.api.internal.artifacts.DefaultModuleVersionSelector.newSelector
import static org.gradle.api.internal.artifacts.result.ResolutionResultDataBuilder.*

/**
 * by Szczepan Faber, created at: 9/20/12
 */
class DefaultResolutionResultTest extends Specification {

    def "provides all modules and dependencies including unresolved"() {
        given:
        def dep1 = newDependency('dep1')
        def dep2 = newDependency('dep2')

        def root = newModule('root').addDependency(dep1).addDependency(dep2)

        def dep3 = newDependency('dep3')
        def dep4 = newUnresolvedDependency('dep4')

        dep2.selected.addDependency(dep3).addDependency(dep4)

        when:
        def deps = new DefaultResolutionResult(root).allDependencies
        def modules = new DefaultResolutionResult(root).allModules

        then:
        deps.size() == 4
        deps.containsAll(dep1, dep2, dep3, dep4)

        and:
        modules.size() == 4
        //does not contain unresolved dep, contains root
        modules.containsAll(root, dep1.selected, dep2.selected, dep3.selected)
    }

    def "provides all modules and dependencies hook"() {
        given:
        def dep = newDependency('dep1')
        def root = newModule('root').addDependency(dep).addDependency(newDependency('dep2'))
        dep.selected.addDependency(newDependency('dep3'))

        def result = new DefaultResolutionResult(root)

        when:
        def deps = []
        def modules = []
        result.allDependencies { deps << it }
        result.allModules { modules << it }

        then:
        deps.size() == 3
        deps*.requested.group.containsAll(['dep1', 'dep2', 'dep3'])

        and:
        modules.size() == 4
        modules*.id.group.containsAll(['root', 'dep1', 'dep2', 'dep3'])
    }

    def "deals with dependency cycles"() {
        given:
        // a->b->a
        def root = newModule('a', 'a', '1')
        def dep1 = newDependency('b', 'b', '1')
        root.addDependency(dep1)
        dep1.selected.addDependency(new DefaultResolvedDependencyResult(newSelector('a', 'a', '1'), root, dep1.selected))

        when:
        def deps = new DefaultResolutionResult(root).allDependencies
        def modules = new DefaultResolutionResult(root).allModules

        then:
        deps.size() == 2
        modules.size() == 2
    }

    def "mutating all dependencies or modules is harmless"() {
        given:
        def dep1 = newDependency('dep1')
        def dep2 = newDependency('dep2')

        def root = newModule('root').addDependency(dep1).addDependency(dep2)

        when:
        def result = new DefaultResolutionResult(root)

        then:
        result.allDependencies == [dep1, dep2] as Set
        result.allModules == [root, dep1.selected, dep2.selected] as Set

        when:
        result.allDependencies << newDependency('dep3')
        result.allModules << newModule('foo')

        then:
        result.allDependencies == [dep1, dep2] as Set
        result.allModules == [root, dep1.selected, dep2.selected] as Set
    }
}
