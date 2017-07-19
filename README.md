# mongogee
MongoDB data migration Grails Plugin.

TravisCI status on master branch: https://travis-ci.org/ikaliZpet/mongogee.svg?branch=master
 
## INTRODUCTION 

Mongogee Grails plugin is a simple, secure service for mongodb data migration management.
This plugin is inspired by Mongobee (https://github.com/mongobee/mongobee) MongoDB data migration toolset.

**All the credits of the annotation based migration change management logic go to the Mongobee authors.**

This repository contains source code of Mongogee, and a testing sample host Grails application.

## INSTALL

In host Grails application's build.gradle file:

	plugins {
    	compile ':mongogee:$version' // current: 1.0.0
	}


## PREREQUISITES

Hosting Grails application version 3.0+.


## CONFIGURATION


In host Grails application grails-app/conf/application.yml

	mongogee
		changeEnabled: true 		# default is true
		continueWithError: false 	# default is false
		changeLogsScanPackage: <migration-class-package-path>  # required, no default value
	 	
       


## WRITE MIGRATION AND RUN


Adopting and extending Mongobee (https://github.com/mongobee/mongobee) annotations. There are two level of migration change units: change-logs (class level) and change-sets (method level). 
	
Some examples are below:

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
    


## CONTRIBUTORS

Bin Le (bin.le.code@gmail.com)


## LICENSE

Apache License Version 2.0. (http://www.apache.org/licenses/)


