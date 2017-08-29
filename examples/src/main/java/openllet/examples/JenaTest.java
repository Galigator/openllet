package openllet.examples;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import openllet.jena.PelletReasonerFactory;
import org.apache.jena.ontology.OntModel;
import org.apache.jena.ontology.OntModelSpec;
import org.apache.jena.ontology.OntProperty;
import org.apache.jena.rdf.model.ModelFactory;
import org.apache.jena.util.iterator.ExtendedIterator;

/**
 * TODO
 * 
 * @since
 */
public class JenaTest
{
	public static void main(final String[] args)
	{
		final String ontostring = //
				"@prefix : <http://purl.bdrc.io/ontology/> .\n" + //
						"@prefix bdo: <http://purl.bdrc.io/ontology/> .\n" + //
						"@prefix owl: <http://www.w3.org/2002/07/owl#> .\n" + //
						"@prefix rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#> .\n" + //
						"@prefix xsd: <http://www.w3.org/2001/XMLSchema#> .\n" + //
						"@prefix rdfs: <http://www.w3.org/2000/01/rdf-schema#> .\n" + //
						"@base <http://purl.bdrc.io/ontology/> .\n" + //
						"\n" + //
						"<http://purl.bdrc.io/ontology/> rdf:type owl:Ontology ;\n" + //
						"                                 rdfs:label \"BDRC Ontology\"^^xsd:string ;\n" + //
						"                                 owl:versionInfo \"0.1\"^^xsd:string .\n" + //
						"\n" + //
						"bdo:personKinWho rdf:type owl:ObjectProperty ,\n" + //
						"                          owl:FunctionalProperty ;\n" + //
						"                 rdfs:domain bdo:Kin ;\n" + //
						"                 rdfs:range bdo:Person .\n" + //
						"\n" + //
						"\n" + //
						"bdo:Kin rdf:type owl:Class .\n" + //
						"\n" + //
						"bdo:Person rdf:type owl:Class .";
		final OntModel m = ModelFactory.createOntologyModel(OntModelSpec.OWL_DL_MEM);

		final InputStream stream = new ByteArrayInputStream(ontostring.getBytes());
		m.read(stream, null, "TURTLE");
		ExtendedIterator<OntProperty> ppi = m.listAllOntProperties();

		System.out.println("iter or ppi");
		int i = 0;
		while (ppi.hasNext())
		{
			System.out.print(i++ + " ");
			final OntProperty p = ppi.next();
			System.out.println(p);
		}
		final OntModel ontoModelInferred = ModelFactory.createOntologyModel(PelletReasonerFactory.THE_SPEC, m);
		ontoModelInferred.setStrictMode(false);
		ppi = ontoModelInferred.listAllOntProperties();

		System.out.println("iter or ppi");
		i = 0;
		while (ppi.hasNext())
		{
			System.out.print(i++ + " ");
			final OntProperty p = ppi.next();
			System.out.println(p);
		}
	}
}
