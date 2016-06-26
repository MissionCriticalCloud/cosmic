/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.apache.cloudstack.spring.module.locator;

import org.apache.cloudstack.spring.module.model.ModuleDefinition;

import java.io.IOException;
import java.util.Collection;

/**
 * Responsible for locating the ModuleDefinition for a given context.  The implementation
 * of this class should take extra care to set the ClassLoader of the ModuleDefinition
 * properly.
 */
public interface ModuleDefinitionLocator {

    Collection<ModuleDefinition> locateModules(String context) throws IOException;
}
