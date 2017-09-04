package openllet.examples;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import openllet.jena.PelletReasonerFactory;
import org.apache.jena.ontology.OntModel;
import org.apache.jena.ontology.OntModelSpec;
import org.apache.jena.ontology.OntProperty;
import org.apache.jena.rdf.model.InfModel;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;
import org.apache.jena.reasoner.Reasoner;
import org.apache.jena.util.iterator.ExtendedIterator;

public class JenaOwl2_Example
{
	public static void main(final String[] args)
	{
		final String ontostring = //
				"@prefix : <http://purl.bdrc.io/ontology/> .\n" + //
						"@prefix bdo: <http://bdo/> .\n" + //
						"@prefix owl: <http://www.w3.org/2002/07/owl#> .\n" + //
						"@prefix rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#> .\n" + //
						"@base <http://purl.bdrc.io/ontology/> .\n" + //
						"<http://purl.bdrc.io/ontology/> rdf:type owl:Ontology .\n" + //
						"bdo:x rdf:type owl:ObjectProperty , owl:FunctionalProperty .\n"//
		;

		final OntModel m = ModelFactory.createOntologyModel(OntModelSpec.OWL_DL_MEM);

		final InputStream stream = new ByteArrayInputStream(ontostring.getBytes());
		m.read(stream, null, "TURTLE");
		ExtendedIterator<OntProperty> ppi = m.listAllOntProperties();

		System.out.println("iter or ppi");
		while (ppi.hasNext())
		{
			final OntProperty p = ppi.next();
			System.out.println(p);
		}
		final OntModel ontoModelInferred = ModelFactory.createOntologyModel(PelletReasonerFactory.THE_SPEC, m);

		// ------------------------------------------------------------------------------------------
		ontoModelInferred.setStrictMode(false); // <--------------- HERE THE TRICK FOR OWL2. --------
		// ------------------------------------------------------------------------------------------

		ppi = ontoModelInferred.listAllOntProperties();

		System.out.println("iter or ppi");
		while (ppi.hasNext())
		{
			final OntProperty p = ppi.next();
			System.out.println(p);
		}

		final Reasoner reasoner = PelletReasonerFactory.theInstance().create();
		final Model emptyModel = ModelFactory.createDefaultModel();
		final InfModel model = ModelFactory.createInfModel(reasoner, emptyModel);
		model.read(stream, null, "TURTLE");

		System.out.println("iter or ppi");
		while (ppi.hasNext())
		{
			final OntProperty p = ppi.next();
			System.out.println(p);
		}

	}
}
