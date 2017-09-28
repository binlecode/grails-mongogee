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

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Set of changes to be added to the DB. Many changesets are included in one changelog.
 *
 * @author lstolowski
 * @see ChangeLog
 * @since 27/07/2014
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface ChangeSet {

    /**
     * Author of the changeset.
     * Obligatory
     *
     * @return author
     */
    public String author();  // must be set

    /**
     * Unique ID of the changeset.
     * Obligatory
     *
     * @return unique id
     */
    public String id();      // must be set

    /**
     * Sequence that provide correct order for changesets. Sorted alphabetically, ascending.
     * Obligatory.
     *
     * @return ordering
     */
    public String order();   // must be set

    /**
     * Executes the change set on every mongobee's execution, even if it has been run before.
     * Optional (default is false)
     *
     * @return should run always?
     */
    public boolean runAlways() default false;
//
//  /**
//   * Executes the change the first time it is seen and each time the change set has been changed. <br/>
//   * Optional (default is false)
//   */
//  public boolean runOnChange() default false;
}
