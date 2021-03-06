/*
 * Copyright 2009 the original author or authors.
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
package org.gradle.api.internal.notations;

import org.gradle.api.artifacts.ClientModule;
import org.gradle.internal.reflect.Instantiator;
import org.gradle.api.internal.artifacts.dependencies.DefaultClientModule;
import org.gradle.api.internal.notations.api.NotationParser;
import org.gradle.api.internal.notations.api.TopLevelNotationParser;

import java.util.Collection;

/**
 * @author Hans Dockter
 */
public class ClientModuleNotationParser implements TopLevelNotationParser, NotationParser<ClientModule> {

    private final NotationParser<ClientModule> delegate;

    public ClientModuleNotationParser(Instantiator instantiator) {
        delegate = new NotationParserBuilder<ClientModule>()
                .resultingType(ClientModule.class)
                .parser(new DependencyStringNotationParser<DefaultClientModule>(instantiator, DefaultClientModule.class))
                .parser(new DependencyMapNotationParser<DefaultClientModule>(instantiator, DefaultClientModule.class))
                .toComposite();
    }

    public void describe(Collection<String> candidateFormats) {
        delegate.describe(candidateFormats);
    }

    public ClientModule parseNotation(Object notation) {
        return delegate.parseNotation(notation);
    }
}
