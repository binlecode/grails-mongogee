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

package grails.plugin.mongogee

import groovy.transform.ToString

/**
 * Entry in the changes collection log
 * Type: entity class.
 *
 * @author lstolowski
 * @author binle
 * @since 27/07/2014
 */
@ToString(includeNames = true, includePackage = false)
class ChangeEntry {

    Date dateCreated
    Date lastUpdated
    String id
    String changeLogClassName
    String changeSetMethodName
    String changeSetId
    String author
    /** total count of runs for this change entry */
    Long runCount = 1L

    static constraints = {
        changeSetId blank: false, unique: true
        author nullable: true
    }

    static mapWith = 'mongo'
    static mapping = {
        collection 'mongogeeChangeEntry'
        version true
        changeSetId index: true
        changeLogClassName index: true
        changeSetMethodName index: true
    }

    // disable delete
    def beforeDelete() {
        throw new UnsupportedOperationException('Delete not allowed for this domain')
    }

}
