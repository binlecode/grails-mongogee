/* Copyright 2006-2015 the original author or authors.
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
package grails.plugin.mongogee

import grails.core.GrailsApplication
import grails.util.Environment
import grails.util.Holders
import groovy.transform.CompileStatic
import groovy.util.logging.Slf4j

/**
 * Plugin config Helper utilities.
 */
@CompileStatic
@Slf4j
class MongogeeConfigUtils {
    private static GrailsApplication application
    private static ConfigObject _mongogeeConfig

    // Constructor. Static methods only
    private MongogeeConfigUtils() {}

    /**
     * Parse and load the mongogee configuration.
     * @return the configuration
     */
    static synchronized ConfigObject getMongogeeConfig() {
        if (_mongogeeConfig == null) {
            log.trace 'Building mongogee config since there is no cached config'
            reloadMongogeeConfig()
        }
        _mongogeeConfig
    }

    /**
     * For testing only.
     * @param config the config object
     */
    static void setMongogeeConfig(ConfigObject config) {
        _mongogeeConfig = config
    }

    /**
     * Force a reload of the mongogee configuration.
     */
    static void reloadMongogeeConfig() {
        mergeConfig getApplicationMongoeeConfig(), 'DefaultMongogeeConfig'
        log.trace 'reloaded mongogee config'
    }

    /**
     * Merge in a secondary config (such as provided by plugin as defaults) into the main config.
     * @param currentConfig the current configuration
     * @param className the name of the config class to load
     */
    private static void mergeConfig(ConfigObject currentConfig, String className) {
        log.trace("Merging currentConfig with $className")
        GroovyClassLoader classLoader = new GroovyClassLoader(Thread.currentThread().contextClassLoader)
        ConfigObject secondary = new ConfigSlurper(Environment.current.name).parse(classLoader.loadClass(className))
        secondary = secondary.defaultMongogee as ConfigObject

        _mongogeeConfig = mergeConfig(currentConfig, secondary)
    }

    /**
     * Merge two configs together. The order is important: if <code>secondary</code> is not null then
     * start with that and merge the main config on top of that. This lets the <code>secondary</code>
     * config act as default values but let user-supplied values in the main config override them.
     *
     * @param currentConfig the main config, starting from Config.groovy
     * @param secondary default config values
     * @return the merged configs
     */
    private static ConfigObject mergeConfig(ConfigObject currentConfig, ConfigObject secondary) {
        log.trace("Merging secondary config on top of currentConfig")
        (secondary ?: new ConfigObject()).merge(currentConfig ?: new ConfigObject()) as ConfigObject
    }

    private static ConfigObject getApplicationMongoeeConfig() {
        getApplication().config.mongogee as ConfigObject
    }

    private static GrailsApplication getApplication() {
        if (!application) {
            application = Holders.grailsApplication
        }
        application
    }


}
