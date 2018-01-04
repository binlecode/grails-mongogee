/*******************************************************************************
 *  Copyright 2017 Bin Le
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 ******************************************************************************/

package grails.plugin.mongogee;

import grails.plugin.mongogee.exception.MongogeeChangeSetException;
import grails.util.Environment;
import org.reflections.Reflections;

import java.lang.reflect.AnnotatedElement;
import java.lang.reflect.Method;
import java.util.*;
import java.util.stream.Collectors;

import static java.util.Arrays.asList;

/**
 * This is a utility class to deal with reflections and annotations.
 *
 * @author lstolowski
 * @author binle
 * @since 27/07/2014
 */
public class ChangeAgent {
    private final Set<String> changeLogsBasePackageSet;

    /**
     * @param changeLogsBasePackage  single package path string or CSV string of multiple package paths
     */
    public ChangeAgent(String changeLogsBasePackage) {
        if (changeLogsBasePackage != null) {
            this.changeLogsBasePackageSet = Arrays.stream(changeLogsBasePackage.split(","))
                    .map(String::trim)
                    .collect(Collectors.toSet());
        } else {
            this.changeLogsBasePackageSet = null;
        }
    }

    /**
     * @param changeLogsBasePackages  collection of one or many package path strings
     */
    public ChangeAgent(Collection<String> changeLogsBasePackages) {
        if (changeLogsBasePackages != null) {
            this.changeLogsBasePackageSet = changeLogsBasePackages.stream()
                    .map(String::trim)
                    .collect(Collectors.toSet());
        } else {
            this.changeLogsBasePackageSet = null;
        }
    }

    public List<Class<?>> fetchChangeLogs() {
        List<Class<?>> changeLogs = new ArrayList<>();
        if (this.changeLogsBasePackageSet != null) {
            for (String basePackage : this.changeLogsBasePackageSet) {
                changeLogs.addAll(fetchChangeLogsByPackage(basePackage));
            }
        }
        Collections.sort(changeLogs, new ChangeLogComparator());
        return changeLogs;
    }

    protected List<Class<?>> fetchChangeLogsByPackage(String changeLogsBasePackage) {
        Reflections reflections = new Reflections(changeLogsBasePackage);
        Set<Class<?>> changeLogs = reflections.getTypesAnnotatedWith(ChangeLog.class); // TODO remove dependency, do own method
        return (List<Class<?>>) filterByActiveGrailsEnvironment(changeLogs);
    }

    public List<Method> fetchChangeSets(final Class<?> type) throws MongogeeChangeSetException {
        final List<Method> changeSets = filterChangeSetAnnotation(asList(type.getDeclaredMethods()));
        final List<Method> filteredChangeSets = (List<Method>) filterByActiveGrailsEnvironment(changeSets);

        Collections.sort(filteredChangeSets, new ChangeSetComparator());

        return filteredChangeSets;
    }

    public boolean isRunAlwaysChangeSet(Method changesetMethod) {
        if (changesetMethod.isAnnotationPresent(ChangeSet.class)) {
            ChangeSet annotation = changesetMethod.getAnnotation(ChangeSet.class);
            return annotation.runAlways();
        } else {
            return false;
        }
    }

    public boolean isContinueWithError(Method changesetMethod) {
        if (changesetMethod.isAnnotationPresent(ChangeSet.class)) {
            ChangeSet annotation = changesetMethod.getAnnotation(ChangeSet.class);
            return annotation.continueWithError();
        } else {
            return false;
        }
    }

    private boolean matchesActiveGrailsEnvironment(AnnotatedElement element) {
        if (element.isAnnotationPresent(ChangeEnv.class)) {
            List<String> environments = asList(element.getAnnotation(ChangeEnv.class).value());
            if (environments.contains(Environment.getCurrentEnvironment().name().toLowerCase())) {
                return true;
            } else {
                return false;
            }
        } else {
            return true; // not annotated change sets always match
        }
    }

    private List<?> filterByActiveGrailsEnvironment(Collection<? extends AnnotatedElement> annotated) {
        List<AnnotatedElement> filtered = new ArrayList<>();
        for (AnnotatedElement element : annotated) {
            if (matchesActiveGrailsEnvironment(element)) {
                filtered.add(element);
            }
        }
        return filtered;
    }

    private List<Method> filterChangeSetAnnotation(List<Method> allMethods) throws MongogeeChangeSetException {
        final Set<String> changeSetIds = new HashSet<>();
        final List<Method> changeSetMethods = new ArrayList<>();
        for (final Method method : allMethods) {
            if (method.isAnnotationPresent(ChangeSet.class)) {
                String id = method.getAnnotation(ChangeSet.class).id();
                if (changeSetIds.contains(id)) {
                    throw new MongogeeChangeSetException(String.format("Duplicated changeSet id found: '%s'", id));
                }
                changeSetIds.add(id);
                changeSetMethods.add(method);
            }
        }
        return changeSetMethods;
    }

}
