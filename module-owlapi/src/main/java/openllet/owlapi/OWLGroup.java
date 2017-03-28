package openllet.owlapi;

import java.io.File;
import java.util.Optional;
import java.util.function.BiFunction;
import java.util.logging.Level;
import java.util.stream.Stream;
import openllet.shared.tools.Logging;
import org.semanticweb.owlapi.model.IRI;
import org.semanticweb.owlapi.model.OWLOntology;
import org.semanticweb.owlapi.model.OWLOntologyID;
import org.semanticweb.owlapi.model.OWLOntologyManager;

/**
 * @since 2.6.1
 */
public interface OWLGroup extends AutoCloseable, Logging
{
	public static OWLManagerGroup fromVolatileManager(final OWLOntologyManager manager)
	{
		return new OWLManagerGroup(Optional.of(manager), Optional.empty());
	}

	public static OWLManagerGroup fromPersistentManager(final OWLOntologyManager manager)
	{
		return new OWLManagerGroup(Optional.empty(), Optional.of(manager));
	}

	public boolean setOntologiesDirectory(final File directory);

	public Optional<File> getOntologiesDirectory();

	public OWLOntologyManager getVolatileManager();

	/**
	 * @return The storage manager if you have call setOntologiesDirectory() before; else it throw a RuntimeException.
	 * @since 2.5.1
	 */
	public OWLOntologyManager getPersistentManager();

	public boolean havePersistentManager();

	public boolean haveVolatileManager();

	public void loadDirectory(final File directory, final OWLOntologyManager manager, final BiFunction<OWLOntologyManager, File, Optional<OWLOntology>> loader);

	public void loadDirectory(final File directory);

	/**
	 * Seek the asked ontology. First in the volatile ontologies, then in the stored ontologies that are already stored.
	 *
	 * @param ontology the iri of the ontology you are looking for.
	 * @param version of the ontology
	 * @param isVolatile
	 * @return an ontology if found.
	 * @since 2.6.0
	 */
	default Optional<OWLHelper> getOntology(final IRI ontology, final double version, final boolean isVolatile)
	{
		return getOntology(OWLHelper.getVersion(ontology, version), isVolatile);
	}

	/**
	 * Seek the asked ontology. First in the volatile ontologies, then in the stored ontologies that are already stored.
	 *
	 * @param ontologyID the id of the ontology you are looking for.
	 * @param isVolatile
	 * @return an ontology if found.
	 * @since 2.6.0
	 */
	default Optional<OWLHelper> getOntology(final OWLOntologyID ontologyID, final boolean isVolatile)
	{
		try
		{
			return Optional.of(new OWLGenericTools(this, ontologyID));
		}
		catch (final Exception e)
		{
			getLogger().log(Level.WARNING, "Can't load " + ontologyID + " in volatile=" + isVolatile + " mode", e);
			return Optional.empty();
		}
	}

	/**
	 * Seek the asked ontology. First in the volatile ontologies, then in the stored ontologies that are already stored.
	 *
	 * @param ontologyID the id of the ontology you are looking for.
	 * @return an ontology if found.
	 * @since 2.5.1
	 */
	default Optional<OWLOntology> getOntology(final OWLOntologyID ontologyID)
	{
		final Optional<OWLOntology> ontology = getVolatileOntology(ontologyID);
		return ontology.isPresent() ? ontology : getPersistentOntology(ontologyID);
	}

	default Optional<OWLHelper> getHelper(final OWLOntologyID ontologyID)
	{
		Optional<OWLOntology> ontology = getVolatileOntology(ontologyID);
		if (ontology.isPresent())
			return Optional.of(new OWLGenericTools(this, getVolatileManager(), ontology.get()));

		ontology = getPersistentOntology(ontologyID);
		if (ontology.isPresent())
			return Optional.of(new OWLGenericTools(this, getPersistentManager(), ontology.get()));

		return Optional.empty();
	}

	default Stream<OWLHelper> getVolatilesHelper()
	{
		if (!haveVolatileManager())
			return Stream.empty();

		final OWLOntologyManager vm = getVolatileManager();
		return vm.ontologies().map(ontology -> new OWLGenericTools(this, vm, ontology));
	}

	default Stream<OWLHelper> getPersistentsHelper()
	{
		if (!havePersistentManager())
			return Stream.empty();

		final OWLOntologyManager vm = getPersistentManager();
		return vm.ontologies().map(ontology -> new OWLGenericTools(this, vm, ontology));
	}

	/**
	 * The standard 'getOntology' from the OWLManager don't really take care of versionning. This function is here to enforce the notion of version
	 *
	 * @param manager to look into (mainly storage or volatile)
	 * @param ontologyID with version information
	 * @return the ontology if already load into the given manager.
	 * @since 2.5.1
	 */
	public static Optional<OWLOntology> getOntology(final OWLOntologyManager manager, final OWLOntologyID ontologyID)
	{
		final Optional<IRI> ontIri = ontologyID.getOntologyIRI();
		if (!ontIri.isPresent())
			return Optional.empty();

		Stream<OWLOntology> ontologies = manager.versions(ontIri.get());

		final Optional<IRI> verIri = ontologyID.getVersionIRI();
		if (verIri.isPresent())
		{
			final IRI version = verIri.get();
			ontologies = ontologies.filter(//
					candidat -> candidat.getOntologyID()//
							.getVersionIRI()//
							.map(version::equals)//
							.orElse(false)//
			);
		}

		return ontologies.findAny();
	}

	default Optional<OWLOntology> getVolatileOntology(final OWLOntologyID ontologyID)
	{
		if (haveVolatileManager()) // If there is no volatile manager then the ontology isn't load.
			return getOntology(getVolatileManager(), ontologyID);
		else
			return Optional.empty();
	}

	default Optional<OWLOntology> getPersistentOntology(final OWLOntologyID ontologyID)
	{
		return getOntology(getPersistentManager(), ontologyID);
	}

	public String ontology2filename(final OWLOntologyID ontId);

	default String ontology2filename(final OWLOntology ontology)
	{
		return ontology2filename(ontology.getOntologyID());
	}

	public void check(final OWLOntologyManager manager);
}
