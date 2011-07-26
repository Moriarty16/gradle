/*
 * Copyright 2011 the original author or authors.
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
package org.gradle.api.internal;

import groovy.lang.Closure;
import org.gradle.api.Namer;
import org.gradle.api.Named;
import org.gradle.api.NamedDomainObjectFactory;

public class FactoryNamedDomainObjectContainer<T> extends AbstractNamedDomainObjectContainer<T> {

    private final NamedDomainObjectFactory<T> factory;

    /**
     * <p>Creates a container that instantiates reflectively, expecting a 1 arg constructor taking the name.<p>
     *
     * <p>The type must implement the {@link Named} interface as a {@link Namer} will be created based on this type.</p>
     *
     * @param type The concrete type of element in the container (must implement {@link Named})
     * @param classGenerator The class generator to use to create any other collections based on this one
     */
    public FactoryNamedDomainObjectContainer(Class<T> type, ClassGenerator classGenerator) {
        this(type, classGenerator, (Namer<T>)createNamerForNamed(type));
    }

    /**
     * <p>Creates a container that instantiates reflectively, expecting a 1 arg constructor taking the name.<p>
     *
     * @param type The concrete type of element in the container (must implement {@link Named})
     * @param classGenerator The class generator to use to create any other collections based on this one
     * @param namer The naming strategy to use
     */
    public FactoryNamedDomainObjectContainer(Class<T> type, ClassGenerator classGenerator, Namer<? super T> namer) {
        this(type, classGenerator, namer, new ReflectiveNamedDomainObjectFactory<T>(type));
    }

    /**
     * <p>Creates a container that instantiates using the given factory.<p>
     *
     * @param type The concrete type of element in the container (must implement {@link Named})
     * @param classGenerator The class generator to use to create any other collections based on this one
     * @param factory The factory responsible for creating new instances on demand
     */
    public FactoryNamedDomainObjectContainer(Class<T> type, ClassGenerator classGenerator, NamedDomainObjectFactory<T> factory) {
        this(type, classGenerator, createNamerForNamed(type), factory);
    }

    /**
     * <p>Creates a container that instantiates using the given factory.<p>
     *
     * @param type The concrete type of element in the container
     * @param classGenerator The class generator to use to create any other collections based on this one
     * @param namer The naming strategy to use
     * @param factory The factory responsible for creating new instances on demand
     */
    public FactoryNamedDomainObjectContainer(Class<T> type, ClassGenerator classGenerator, Namer<? super T> namer, NamedDomainObjectFactory<T> factory) {
        super(type, classGenerator, namer);
        this.factory = factory;
    }

    /**
     * <p>Creates a container that instantiates using the given factory.<p>
     *
     * @param type The concrete type of element in the container (must implement {@link Named})
     * @param classGenerator The class generator to use to create any other collections based on this one
     * @param factoryClosure The closure responsible for creating new instances on demand
     */
    public FactoryNamedDomainObjectContainer(Class<T> type, ClassGenerator classGenerator, final Closure factoryClosure) {
        this(type, classGenerator, createNamerForNamed(type), factoryClosure);
    }

    /**
     * <p>Creates a container that instantiates using the given factory.<p>
     *
     * @param type The concrete type of element in the container
     * @param classGenerator The class generator to use to create any other collections based on this one
     * @param namer The naming strategy to use
     * @param factory The factory responsible for creating new instances on demand
     */
    public FactoryNamedDomainObjectContainer(Class<T> type, ClassGenerator classGenerator, Namer<? super T> namer, final Closure factoryClosure) {
        this(type, classGenerator, namer, new ClosureObjectFactory<T>(type, factoryClosure));
    }

    static private <T> Namer<T> createNamerForNamed(Class<T> type) {
        if (Named.class.isAssignableFrom(type)) {
            return (Namer<T>)new org.gradle.api.Named.Namer();
        } else {
            throw new IllegalArgumentException(String.format("The '%s' cannot be used with FactoryNamedDomainObjectContainer without specifying a Namer as it does not implement the Named interface.", type));
        }
    }

    @Override
    protected T doCreate(String name) {
        return factory.create(name);
    }

    private static class ClosureObjectFactory<T> implements NamedDomainObjectFactory<T> {
        private final Class<T> type;
        private final Closure factoryClosure;

        public ClosureObjectFactory(Class<T> type, Closure factoryClosure) {
            this.type = type;
            this.factoryClosure = factoryClosure;
        }

        public T create(String name) {
            return type.cast(factoryClosure.call(name));
        }
    }
}
