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
