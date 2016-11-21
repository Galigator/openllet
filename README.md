Openllet: An Open Source OWL DL reasoner for Java
-----------------------------------------------

[![Build Status](https://api.travis-ci.org/Galigator/openllet.svg?branch=integration)](https://travis-ci.org/Galigator/openllet)
[![Gitter](https://badges.gitter.im/Join%20Chat.svg)](https://gitter.im/Galigator/pelletEvolution?utm_source=badge&utm_medium=badge&utm_campaign=pr-badge&utm_content=badge)
[![Twitter](https://img.shields.io/badge/twitter-openllet-blue.svg)](https://twitter.com/openllet)
[![Codacy Badge](https://api.codacy.com/project/badge/Grade/d1acfdbe2c194252a311e223cd94e64e)](https://www.codacy.com/app/sejourne_kevin/openllet?utm_source=github.com&amp;utm_medium=referral&amp;utm_content=Galigator/openllet&amp;utm_campaign=Badge_Grade)
<a href="https://www.versioneye.com/user/projects/5832fff3e7cea00029198b38"><img src="https://www.versioneye.com/user/projects/5832fff3e7cea00029198b38/badge.svg?style=flat"/></a>

<!--
[![codecov](https://codecov.io/gh/Galigator/openllet/branch/integration/graph/badge.svg)](https://codecov.io/gh/Galigator/openllet)
-->

Openllet is the OWL 2 DL reasoner: 
--------------------------------


Openllet can be used with Jena or OWL-API libraries. Openllet provides functionality to check consistency of ontologies, compute the classification hierarchy, 
explain inferences, and answer SPARQL queries.

Feel free to fork this repository and submit pull requests if you want to see changes, new features, etc. in Pellet.
We need a lot more tests, send your samples if you can.

There are some  code samples in the examples/ directory.
Issues are on [Github](http://github.com/galigator/openllet/issues).

Openllet 2.6.X:
-----------

* Refactor modules dependencies.
* Enforce interface usage in the core system.
* Lighter hash functions and less conflict when use in multi-thread environnement.

Migration :
* lots of com.clarkparsia.* / com.mindswap.* are refactor into openllet.* to avoid conflict has typing change a lot.
* dependencies on modern libs.

		<dependency>
			<groupId>com.github.galigator.openllet</groupId>
			<artifactId>openllet-owlapi</artifactId>
			<version>2.6.0</version>
		</dependency>
		<dependency>
			<groupId>com.github.galigator.openllet</groupId>
			<artifactId>openllet-jena</artifactId>
			<version>2.6.0</version>
		</dependency>

Roadmap :
* Fullify strong typing in openllet core (2.7.X).
* Add support for sesame/rdf4j reasoning (2.8.X).

Openllet 2.5.X:
-----------

* full java 8 support, java 8 is a requierement.
* speed and stability improvement

Changes :
* Update versions of libs : owlapi 5, jena3 and lots more. Some old libs have been integrated and cleaned, strongly typed into openllet.
* Corrections : all tests works, no more warnings with high level of reports in Eclipse.

Migration :
* pellet/owlapi/src/main/java/com/clarkparsia/owlapiv3/ is now  pellet/owlapi/src/main/java/com/clarkparsia/owlapi/
* groupId   com.clarkparsia.pellet   is now   com.github.galigator.openllet


Pellet 1..2.3] Licences and supports: 
-------------------------------------
 
* [open source](https://github.com/complexible/pellet/blob/master/LICENSE.txt) (AGPL) or commercial license
* pure Java
* developed and [commercially supported](http://complexible.com/) by Complexible Inc. 

Commercial support for Pellet is [available](http://complexible.com/). 
The [Pellet FAQ](http://clarkparsia.com/pellet/faq) answers some frequently asked questions.

There is a [pellet-users mailing list](https://groups.google.com/forum/?fromgroups#!forum/pellet-users) for questions and feedback.
You can search [pellet-users archives](http://news.gmane.org/gmane.comp.web.pellet.user).
Bug reports and enhancement requests should be sent to the mailing list. 

Thanks for using Pellet.
