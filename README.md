fuse-archetype-pax-exam
===============

JBoss Fuse archetype collection demonstrating the use of [Pax Exam](https://ops4j1.jira.com/wiki/display/paxexam/Pax+Exam) for integration testing


## Usage 

1. Clone repository
2. Build archetype using Maven to install archetypes into local repository

	```
	mvn clean install
	```
3. Generate new project from one of the archetypes

	```
	mvn archetype:generate -DarchetypeGroupId=com.redhat.fuse -DarchetypeArtifactId=camel-archetype-blueprint-pax-exam -Dversion=1.0.0 -DgroupID=<your-group-id> -DartifactId=<your-artifact-id> -Dversion=<your-version> -Dpackage=<your-package>
	```
4. Each generated project will example its usage on how to run and test