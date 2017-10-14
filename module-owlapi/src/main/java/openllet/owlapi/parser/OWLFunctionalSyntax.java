package openllet.owlapi.parser;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.logging.Logger;
import openllet.shared.tools.Log;
import org.semanticweb.owlapi.model.OWLAxiom;
import org.semanticweb.owlapi.model.OWLOntology;
import org.semanticweb.owlapi.model.OWLOntologyID;
import org.semanticweb.owlapi.model.OWLOntologyLoaderConfiguration;
import org.semanticweb.owlapi.model.OWLOntologyManager;

/**
 * Load and ontology in functional syntax.
 * 
 * @since 2.6.3
 */
public class OWLFunctionalSyntax
{
	private static final Logger _logger = Log.getLogger(OWLFunctionalSyntax.class);

	public static Optional<OWLOntology> load(final File file, final OWLOntologyManager manager, final Optional<OWLOntologyID> ontologyId)
	{
		final List<OWLAxiom> axioms = new ArrayList<>();
		final OWLOntology ontology;

		try (final BufferedReader in = new BufferedReader(new FileReader(file)))
		{
			ontology = (ontologyId.isPresent()) ? manager.createOntology(ontologyId.get()) : manager.createOntology();

			final OWLFunctionalSyntaxParser parser = new OWLFunctionalSyntaxParser(in);
			parser.setUp(ontology, new OWLOntologyLoaderConfiguration());
			try
			{
				while (true)
					axioms.add(parser.Axiom());
			}
			catch (@SuppressWarnings("unused") final Exception e)
			{
				// The exception is the way we detected the end.
			}
		}
		catch (final Exception exception)
		{
			Log.error(_logger, "Can't load the ontology from file " + file + ".", exception);
			return Optional.empty();
		}
		ontology.addAxioms(axioms);
		return Optional.of(ontology);
	}
}
