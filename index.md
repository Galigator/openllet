
## Openllet is an OWL 2 DL reasoner

Openllet can be used with [Jena](https://jena.apache.org/) or [OWL-API](http://owlcs.github.io/owlapi/) libraries. Openllet provides functionality to check consistency of ontologies, compute the classification hierarchy, 
explain inferences, and answer SPARQL queries.

Feel free to fork this repository and submit pull requests if you want to see changes, new features, etc. in Openllet.
We need a lot more tests, send your samples if you can.

There are some code samples in the [examples/](https://github.com/Galigator/openllet/tree/integration/examples) directory.
Issues are on [Github](http://github.com/galigator/openllet/issues).
Pellet community is on [pellet-users mailing list](https://groups.google.com/forum/?fromgroups#!forum/pellet-users).

## Openllet 2.6.X

* Refactor modules dependencies.
* Enforce interface usage in the core system.
* Lighter hash functions and less conflict when use in multi-thread environnement.

### Migration :

* lots of `com.clarkparsia.*` / `com.mindswap.*` are refactored into `openllet.*` to avoid conflicts and have typing changed a lot.
* dependencies on modern libs.

```xml
	<dependency>
		<groupId>com.github.galigator.openllet</groupId>
		<artifactId>openllet-owlapi</artifactId>
		<version>2.6.3</version>
	</dependency>
	<dependency>
		<groupId>com.github.galigator.openllet</groupId>
		<artifactId>openllet-jena</artifactId>
		<version>2.6.3</version>
	</dependency>
```

NB, the Protege plugin need a Protege that work with an 5.1.X version of the OWL-API, so the main branch of Protege isn't compatible with Openllet.

### Roadmap :

* Fullify strong typing in _openllet_ core (2.7.X).
* Add support for _sesame/rdf4j_ reasoning (2.8.X).

### Examples :

Play with the Owl-Api:
```java
try (final OWLManagerGroup group = new OWLManagerGroup())
{
	final OWLOntologyID ontId = OWLHelper.getVersion(IRI.create("http://myOnotology"), 1.0);
	final OWLHelper owl = new OWLGenericTools(group, ontId, true);

	final OWLNamedIndividual x1 = OWL.Individual("#I1");
	final OWLNamedIndividual x2 = OWL.Individual("#I2");

	owl.addAxiom(OWL.equivalentClasses(ClsA, OWL.some(propB, OWL.restrict(XSD.STRING, OWL.facetRestriction(OWLFacet.PATTERN, OWL.constant("A.A"))))));
	owl.addAxiom(OWL.propertyAssertion(x1, propB, OWL.constant("AAA")));
	owl.addAxiom(OWL.propertyAssertion(x2, propB, OWL.constant("BBB")));
	owl.addAxiom(OWL.differentFrom(x1, x2));

	final OpenlletReasoner r = owl.getReasoner();
	assertTrue(r.isEntailed(OWL.classAssertion(x1, ClsA)));
	assertFalse(r.isEntailed(OWL.classAssertion(x2, ClsA)));
}
```

Play with Jena:
```java
	final String ns = "http://www.example.org/test#";

	final OntModel model = ModelFactory.createOntologyModel(PelletReasonerFactory.THE_SPEC);
	model.read(_base + "uncle.owl");

	final Individual Bob = model.getIndividual(ns + "Bob");
	final Individual Sam = model.getIndividual(ns + "Sam");

	final Property uncleOf = model.getProperty(ns + "uncleOf");

	final Model uncleValues = ModelFactory.createDefaultModel();
	addStatements(uncleValues, Bob, uncleOf, Sam);
	assertPropertyValues(model, uncleOf, uncleValues);
```













[editor on GitHub](https://github.com/Galigator/openllet/edit/master/index.md)  [Link](url) and ![Image](src)
