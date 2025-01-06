<!--suppress HtmlDeprecatedAttribute -->
<p align="center">
<img width="256" alt="oewntk" height="256" src="images/oewntk.png">
</p>
<!--suppress HtmlDeprecatedAttribute -->
<p align="center">
<img width="150" alt="mavencentral" src="images/mavencentral.png">
</p>

# JWI - Java WordNet Interface

Extended MIT's Java WordNet Interface [upstream](https://projects.csail.mit.edu/jwi/).

Ported to **Java 8** with lambda expressions.

Global configuration capability.

Global LexID check can be disabled.

Set global hints capability.

Set dictionary resource matcher (e.g. can use one index amongst many).

Set dictionary comparator (e.g. use a different comparator for index file).

Factored out IContentType key functionality into ContentType key enum.

Extensive JUnit testing (see test classes).

Added Maven support.

GroupID and ArtifactID on Maven Central:

	<groupId>io.github.oewntk</groupId>
	<artifactId>jwix</artifactId>
	<packaging>jar</packaging>
	<version>2.4.1.1</version>