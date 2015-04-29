performance-tests
=================

The performance tests for OSIAM

Chapters:
- [Technology](#technology)
- [How to run the tests](#how-to-run-the-tests)
  - [Running the performance tests in the console] (#running-the-performance-tests-in-the-console)
  - [Running the performance tests in your IDE](#running-the-performance-tests-in-your-ide)
  - [Choosing the target database](#choosing-the-target-database)
- [Retrieving test results](#retrieving-test-results)
- [How to modify the test data] (#how-to-modify-the-test-data)
- [How to add or extend tests](#how-to-add-or-extend-tests)
  - [Adding new tests](#adding-new-tests)

## Technology

The OSIAM Performance Tests are written in Java and are using Maven and JMeter for the setup and the test plans.

## How to run the tests

### Running the performance tests in the console

After completing the [Installation](../INSTALLATION.md), you can run the performance tests by simply running Maven with the `verify` goal:

    $ mvn verify

See [Retrieving test results](#retrieving-test-results) where to find the generated data.

### Running the performance tests in your IDE

To run the performance tests in your IDE you can choose between a H2 or a postgreSQL database. For both databases are maven profiles in the pom.xml configured.

If you choose a **H2** database, you have to start the database by the following command:

    $ mvn h2:spawn

If you choose a **postgreSQL** database make sure it's already running.

After that start the Jetty server:

**H2**:

    $ mvn jetty:run -P ide-h2

**postgreSQL**:

    $ mvn jetty:run -P ide-pg

You can now run the tests in your IDE but there will be no performance measurement executed.
To actually run the JMeter performance measurement issue the following command:

    $ mvn package jmeter:gui

In the JMeter GUI open the file `src/test/jmeter/OSIAM Performance Tests.jmx` and run the test plan. You can see the tests running in the console where you started the server.

To generate the JMeter test run report issue the following command:

    $ mvn xml:transform

The JMeter test results and the generated test run report can be found in the usual folders.

### Choosing the target database

The performance tests can be run against a H2 or postgreSQL database.
There are two Maven profiles that can be activated to choose the respective database:

* local-h2 (activated by default)
* local-postgres

Both profiles will start a local Jetty server with the OSIAM server webapps deployed and configured to use the respective database.
The profile `local-h2` will also start a local H2 database server listening on the default port and using an in-memory database.

#### Examples

Run against a local H2 database:

    $ mvn verify [-P local-h2]

Run against a local postgreSQL database

    $ mvn verify -P local-postgres

Running the performance tests against remote targets will be supported soon.

## Retrieving test results

The performance tests generate two files containing the test results:

* raw JMeter results (*.jtl) under `./performance-tests/target/jmeter/results`
* An HTML report under `./performance-tests/target/jmeter-reports`

## How to modify the test data

The performance tests fill the database for each test with the dataseed stored in the file src/main/resources/database_seed.xml. To change the amount of Users and Groups you can simply run again the data creation. To do this change the following two variables in the class [org.osiam.tests.performance.tools.TestDataCreation.java](https://github.com/osiam/test-suites/blob/master/performance-tests/src/main/java/org/osiam/tests/performance/tools/TestDataCreation.java):

```
private static final int NUMBER_USER = 1000;
private static final int NUMBER_GROUPS = 50;
```
to the amount you need.

Now issue the following command:

    $ mvn verify -P tools-generate-test-data

After the profile succeeded the file database_seed.xml will be overridden. (Attention: for more than 1000 Users it will be difficult to open the seed in the most editors because of the filesize).

## How to add or extend tests

To extend or change the tests edit the corresponding Java source file and re-run the performance tests. To change the test plan issue the following command:

    $ mvn package jmeter:gui

Then change the test plan as needed. See [Running the performance tests in your IDE](#running-the-performance-tests-in-your-ide) if you want to test your changes right from the JMeter GUI.

### Adding new tests

To add a new test create a new Java source file named after your new test that extends `AbstractPerformanceTest` and add a `run()` method annotated with `@org.junit.Test`:

```
public class MyShinyNewPerformanceTest extends AbstractPerformanceTest {

    @org.junit.Test
    public void run(){
        [...]
    }
}
```

Then extend the JMeter test plan. Issue the command:

    $ mvn package jmeter:gui

Now open the JMeter test plan (test-suites/performance-tests/src/test/jmeter/OSIAM Performance Test.jmx).


**Info:** To make sure that cache doesn't modify the test results we always run a test a few time before the real time measuring starts. For this we add for every Thread a dry run thread which runs 3 times.

In the JMeter GUI add a new Thread Group named after your test class. 

    Edit -> Add -> Threads(Users) -> Thread Group

In the Thread Group you can add a JUnit Request

right click on the Thread Group -> Add -> Sampler -> JUnit Request

Set the following Options:

Dry Run Thread: Loop Count = 3

Thread: Loop Count = 100

JUnit Requests: 
* activate "Search for JUnit 4 annotations"
* In the Box classname: select/write the full name of your testclass
* activate: Append runtime exceptions

Make sure that the Threads setUp and tearDown are always the first and the last Thread in the Testplan.

Now you can run your Test right from the JMeter GUI like in [Running the performance tests in your IDE](#running-the-performance-tests-in-your-ide) described.
