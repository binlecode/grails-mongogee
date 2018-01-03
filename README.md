# mongogee
MongoDB data migration Grails Plugin.

TravisCI [![Build Status](https://travis-ci.org/binlecode/mongogee.svg?branch=master)](https://travis-ci.org/binlecode/mongogee)
 
## INTRODUCTION 

Mongogee Grails plugin is a simple, secure service for mongodb data migration management.
This plugin is inspired by Mongobee (https://github.com/mongobee/mongobee) MongoDB data migration toolset.

**All the credits of the annotation based migration change management logic go to the Mongobee authors.**

This repository contains source code of Mongogee, and a testing sample host Grails application.

## INSTALL

In host Grails application's build.gradle file:

	plugins {
    	compile ':mongogee:$version'
	}


## PREREQUISITES

Hosting Grails application version 3.0+.


## CONFIGURATION

In host Grails application grails-app/conf/application.yml

```yaml
mongogee:
    changeEnabled: true 		          # default is true
    continueWithError: false 	          # default is false
    changeLogsScanPackage: 'some.package' # required, no default value
    lockingRetryEnabled: false            # default to true
    lockingRetryIntervalMillis: 3000      # default to 5s
    lockingRetryMax: 60                   # default to 120, aka 10min
```
	 	
## WRITE MIGRATION CHANGES

Adopting and extending Mongobee (https://github.com/mongobee/mongobee) annotations. There are two level of migration change units: change-logs (class level) and change-sets (method level).
Change-logs can be written in either Java or Groovy. Some groovy examples are below:

```groovy
@ChangeLog(order = '001')
	class MongogeeTestChangeLog {
	
	    @ChangeSet(author = "testuserA", id = "test1", order = "01")
	    void testChangeSet1() {
	        System.out.println("invoked 1")
	    }
	
	    @ChangeSet(author = "testuserB", id = "test2", order = "02")
	    void testChangeSet2(DB db) {
	        System.out.println("invoked 2 with mongodb DB argument: $db")
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
```
	
    
## RUN MIGRATION

**Version 0.9 and up:** The manual line adding below is no longer needed. Mongogee migration service will be executed automatically if `changeEnabled` is set to `true` (which is also default).

**Version 0.8 and below:** Add following to init/BootStrap.groovy

```groovy
class BootStrap {

    MongogeeService mongogeeService

    def init = { servletContext ->
        // ...

        mongogeeService.execute()

    }

    // ...
}
```

## CHANGE LOG

#### v 0.9.2
- issue-18: add an ```continueWithError``` optional attribute to changeSet, so that:
    - when a changeSet causes an error, the execution logic can decide whether to continue to next changeSet or halt
    - default should be false, as this is the general case that the successive migration changeSets should be stopped
    - this attribute should be used with caution and mainly for those changeSets not causing contextual impacts

#### v 0.9.1
- issue-11: change entry log can intercept and save exception error information during change set invocation, and then bubble up back to main execution flow  

#### v 0.9
- issue-9: simplify hosting app to save the manual line adding in host app bootstrap.groovy

#### v 0.8
- issue-4: add run-count support in changeEntry persistence for those repeatable changeSets

#### v 0.7 and under
- issue-3: add loop/lock-retry ability to application start-up



## CONTRIBUTORS

Bin Le (bin.le.code@gmail.com)


## LICENSE

Apache License Version 2.0. (http://www.apache.org/licenses/)


