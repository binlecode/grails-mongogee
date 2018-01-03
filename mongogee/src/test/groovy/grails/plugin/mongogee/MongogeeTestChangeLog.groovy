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
/**
 * Created by binle on 7/7/17.
 */
@ChangeLog(order = '001')
class MongogeeTestChangeLog {

    @ChangeSet(author = "testuser", id = "test1", order = "01")
    void testChangeSet1() {
        System.out.println("invoked 1")
    }

    @ChangeSet(author = "testuser", id = "test2", order = "02")
    void testChangeSet2() {
        System.out.println("invoked 2")
    }

    @ChangeSet(author = 'testuser', id = 'test3', order = '03', runAlways = true)
    void testChangeSetRunAlways() {
        println 'invoke runAlways'
    }

    @ChangeSet(author = 'testuser', id = 'test4', order = '04')
    @ChangeEnv('development')
    void testChangeSetEnvDevelopment() {
        println 'invoke test for env development'
    }

    @ChangeSet(author = 'testuser', id = 'test5', order = '05')
    @ChangeEnv('test')
    void testChangeSetEnvTest() {
        println 'invoke test for env test'
    }

}
