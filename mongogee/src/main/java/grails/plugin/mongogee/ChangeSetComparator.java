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

import java.io.Serializable;
import java.lang.reflect.Method;
import java.util.Comparator;

/**
 * Sort changesets by 'order' value
 *
 * @author lstolowski
 * @since 2014-09-17
 */
public class ChangeSetComparator implements Comparator<Method>, Serializable {
    @Override
    public int compare(Method o1, Method o2) {
        ChangeSet c1 = o1.getAnnotation(ChangeSet.class);
        ChangeSet c2 = o2.getAnnotation(ChangeSet.class);
        return c1.order().compareTo(c2.order());
    }
}
