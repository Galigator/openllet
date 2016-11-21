Openllet: An Open Source OWL DL reasoner for Java
-----------------------------------------------

[![Build Status](https://api.travis-ci.org/Galigator/openllet.svg?branch=integration)](https://travis-ci.org/Galigator/openllet)
[![Gitter](https://badges.gitter.im/Join%20Chat.svg)](https://gitter.im/Galigator/pelletEvolution?utm_source=badge&utm_medium=badge&utm_campaign=pr-badge&utm_content=badge)
[![Twitter](https://img.shields.io/badge/twitter-openllet-blue.svg)](https://twitter.com/openllet)
[![Codacy Badge](https://api.codacy.com/project/badge/Grade/d1acfdbe2c194252a311e223cd94e64e)](https://www.codacy.com/app/sejourne_kevin/openllet?utm_source=github.com&amp;utm_medium=referral&amp;utm_content=Galigator/openllet&amp;utm_campaign=Badge_Grade)
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

Migration :
* lots of com.clarkparsia.* / com.mindswap.* are refactor into openllet.* to avoid conflict has typing change a lot.

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
* pellet/owlapi/src/main/java/com/clarkparsia/owlapiv3/ is now  pellet/owlapi/src/main/java/com/clarkparsia/owlapiv/
* groupId   com.clarkparsia.pellet   is now   com.github.galigator.openllet

		<dependency>
			<groupId>com.github.galigator.openllet</groupId>
			<artifactId>openllet-owlapi</artifactId>
			<version>2.5.1</version>
		</dependency>
		<dependency>
			<groupId>com.github.galigator.openllet</groupId>
			<artifactId>openllet-jena</artifactId>
			<version>2.5.1</version>
		</dependency>

Dependencies and security of modules of openllet : 
--------------------------------------------------

<table>
<tr><td>Modules</td><td></td></tr>
<tr><td>Functions</td><td>
    <a href="https://www.versioneye.com/user/projects/577054ea67189400364490f2"><img src="https://www.versioneye.com/user/projects/577054ea67189400364490f2/badge.svg?style=flat"/></a>
</td></tr>

<tr><td>Core</td><td>
<a href="https://www.versioneye.com/user/projects/577054e06718940052ba8db8"><img src="https://www.versioneye.com/user/projects/577054e06718940052ba8db8/badge.svg?style=flat"/></a>
</td></tr>

<tr><td>Jena</td><td>
<a href="https://www.versioneye.com/user/projects/577054e467189400364490e3"><img src="https://www.versioneye.com/user/projects/577054e467189400364490e3/badge.svg?style=flat"/></a>
</td></tr>

<tr><td>OwlApi</td><td>
<a href="https://www.versioneye.com/user/projects/577054e86718940052ba8dbf"><img src="https://www.versioneye.com/user/projects/577054e86718940052ba8dbf/badge.svg?style=flat"/></a>
</td></tr>

<tr><td>Query</td><td>
<a href="https://www.versioneye.com/user/projects/577054e9671894004e1a91f3"><img src="https://www.versioneye.com/user/projects/577054e9671894004e1a91f3/badge.svg?style=flat"/></a>
</td></tr>

<tr><td>Explanation</td><td>
<a href="https://www.versioneye.com/user/projects/577054e3671894004e1a91ee"><img src="https://www.versioneye.com/user/projects/577054e3671894004e1a91ee/badge.svg?style=flat"/></a>
</td></tr>

<tr><td>Modularity</td><td>
<a href="https://www.versioneye.com/user/projects/577054e6671894004fedd441"><img src="https://www.versioneye.com/user/projects/577054e6671894004fedd441/badge.svg?style=flat"/></a>
</td></tr>

<tr><td>Tools</td><td></td></tr>
<tr><td>Cli</td><td>
<a href="https://www.versioneye.com/user/projects/577054ee671894004e1a91f9"><img src="https://www.versioneye.com/user/projects/577054ee671894004e1a91f9/badge.svg?style=flat"/></a>
</td></tr>

<tr><td>Profiler</td><td>
<a href="https://www.versioneye.com/user/projects/577054f1671894004fedd493"><img src="https://www.versioneye.com/user/projects/577054f1671894004fedd493/badge.svg?style=flat"/></a>
</td></tr>

<tr><td>Pellint</td><td>
<a href="https://www.versioneye.com/user/projects/577054f167189400364490fb"><img src="https://www.versioneye.com/user/projects/577054f167189400364490fb/badge.svg?style=flat"/></a>
</td></tr>

<tr><td>Plugins</td><td></td></tr>
<tr><td>Protege</td><td>
<a href="https://www.versioneye.com/user/projects/577055026718940052ba8dd9"><img src="https://www.versioneye.com/user/projects/577055026718940052ba8dd9/badge.svg?style=flat"/></a>
</td></tr>

<tr><td>Others</td><td></td></tr>
<tr><td>Tests</td><td>
<a href="https://www.versioneye.com/user/projects/577054ed6718940052ba8dc7"><img src="https://www.versioneye.com/user/projects/577054ed6718940052ba8dc7/badge.svg?style=flat"/></a>
</td></tr>

<tr><td>Parent</td><td>
<a href="https://www.versioneye.com/user/projects/577054ec671894004fedd445"><img src="https://www.versioneye.com/user/projects/577054ec671894004fedd445/badge.svg?style=flat"/></a>
</td></tr>

<tr><td>Distribution</td><td>
<a href="https://www.versioneye.com/user/projects/57705509671894004e1a9204"><img src="https://www.versioneye.com/user/projects/57705509671894004e1a9204/badge.svg?style=flat"/></a>
</td></tr>

<tr><td>Examples</td><td>
<a href="https://www.versioneye.com/user/projects/577054d1671894004fedd438"><img src="https://www.versioneye.com/user/projects/577054d1671894004fedd438/badge.svg?style=flat"/></a>
</td></tr>



</table>

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
