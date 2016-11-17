package openllet.owlapi;

import java.io.File;
import java.util.Optional;
import java.util.function.BiFunction;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Stream;
import openllet.shared.tools.Log;
import org.semanticweb.owlapi.apibinding.OWLManager;
import org.semanticweb.owlapi.model.IRI;
import org.semanticweb.owlapi.model.OWLOntology;
import org.semanticweb.owlapi.model.OWLOntologyID;
import org.semanticweb.owlapi.model.OWLOntologyManager;

/**
 * Group of Owl Manager, volatile and persistent.
 *
 * @since 2.5.1
 */
public class OWLManagerGroup implements AutoCloseable
{
	private static final Logger _logger = Log.getLogger(OWLManagerGroup.class);

	public volatile Optional<File> _ontologiesDirectory = Optional.empty();
	public volatile OWLOntologyManager _volatileManager = null;
	public volatile OWLOntologyManager _storageManager = null;
	private volatile OWLIncrementalFlatFileStorageManagerListener _storageListener;

	public OWLManagerGroup()
	{
		/**/
	}

	public OWLManagerGroup(final File ontologiesDirectory)
	{
		setOntologiesDirectory(ontologiesDirectory);
	}

	public OWLManagerGroup(final Optional<OWLOntologyManager> volatileManager, final Optional<OWLOntologyManager> storageManager)
	{
		volatileManager.ifPresent(m -> _volatileManager = m);
		storageManager.ifPresent(m -> _storageManager = m);
	}

	public boolean setOntologiesDirectory(final File directory)
	{
		_ontologiesDirectory = Optional.ofNullable(directory);
		return _ontologiesDirectory.isPresent();
	}

	public Optional<File> getOntologiesDirectory()
	{
		return _ontologiesDirectory;
	}

	public OWLOntologyManager getVolatileManager()
	{
		if (null == _volatileManager)
			_volatileManager = OWLManager.createConcurrentOWLOntologyManager();

		return _volatileManager;
	}

	/**
	 * @return The storage manager if you have call setOntologiesDirectory() before; else it throw a RuntimeException.
	 * @since 2.5.1
	 */
	public synchronized OWLOntologyManager getStorageManager()
	{
		if (null == _storageManager)
		{
			_storageManager = OWLManager.createConcurrentOWLOntologyManager();

			if (!getOntologiesDirectory().isPresent())
			{
				final String msg = "You should define a directory for stored ontologies before using stored ontologies.";
				_logger.log(Level.SEVERE, msg, new OWLException(msg));
				throw new OWLException(msg);
			}

			try
			{
				_storageListener = new OWLIncrementalFlatFileStorageManagerListener(getOntologiesDirectory().get(), new File(getOntologiesDirectory().get().getPath() + File.separator + OWLHelper._delta), this);
				getStorageManager().addOntologyChangeListener(_storageListener);
			}
			catch (final Exception e)
			{
				throw new OWLException(e);
			}
		}

		return _storageManager;
	}

	public void loadDirectory(final File directory, final OWLOntologyManager manager, final BiFunction<OWLOntologyManager, File, Optional<OWLOntology>> loader)
	{
		if (!directory.exists())
			if (!directory.mkdir())
				throw new OWLException("Can't create the directory " + directory + " .");

		if (!directory.isDirectory())
			throw new OWLException("The directory parameter must be a true existing directory. " + directory + " isn't.");

		for (final File file : directory.listFiles())
			if (file.isFile() && file.canRead() && file.getName().endsWith(OWLHelper._fileExtention))
				try
				{
					_logger.info("loading from " + file);
					// We just want the ontology to be put into the manager and configuration set to our standard. We don't care of the tools for now.
					loader.apply(manager, file)// The side effect is wanted.
							.ifPresent(ontology ->
							{
								OWLHelper.setFormat(manager, ontology);
								_logger.info(ontology.getOntologyID() + "loaded from " + file);
							});//
				}
				catch (final Exception e)
				{
					_logger.log(Level.SEVERE, "Can't load ontology of file " + file, e);
				}
			else
				_logger.info(() -> file + " will not be load.");
	}

	public void loadDirectory(final File directory)
	{
		loadDirectory(directory, getStorageManager(), (m, f) ->
		{
			try
			{
				return Optional.of(m.loadOntologyFromOntologyDocument(f));
			}
			catch (final Exception e)
			{
				_logger.log(Level.SEVERE, "Can't load ontology of file " + f, e);
				return Optional.empty();
			}
		});
	}

	/**
	 * Seek the asked ontology. First in the volatile ontologies, then in the stored ontologies that are already stored.
	 *
	 * @param ontology the iri of the ontology you are looking for.
	 * @param version of the ontology
	 * @param isVolatile
	 * @return an ontology if found.
	 * @since 2.6.0
	 */
	public Optional<OWLHelper> getOntology(final IRI ontology, final double version, final boolean isVolatile)
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
	public Optional<OWLHelper> getOntology(final OWLOntologyID ontologyID, final boolean isVolatile)
	{
		try
		{
			return Optional.of(new OWLGenericTools(this, ontologyID));
		}
		catch (final Exception e)
		{
			_logger.log(Level.WARNING, "Can't load " + ontologyID + " in volatile=" + isVolatile + " mode", e);
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
	public Optional<OWLOntology> getOntology(final OWLOntologyID ontologyID)
	{
		final Optional<OWLOntology> ontology = getOntology(getVolatileManager(), ontologyID);
		return ontology.isPresent() ? ontology : getOntology(getStorageManager(), ontologyID);
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

	public String ontology2filename(final OWLOntologyID ontId)
	{
		if (_ontologiesDirectory.isPresent())
			return OWLHelper.ontology2filename(_ontologiesDirectory.get(), ontId);
		throw new OWLException("Storage directory should be define to enable loading of ontology by iri.");
	}

	public String ontology2filename(final OWLOntology ontology)
	{
		return ontology2filename(ontology.getOntologyID());
	}

	public void check(final OWLOntologyManager manager)
	{
		if (manager == _volatileManager || manager == _storageManager)
			return;
		throw new OWLException("The given manager isn't know from in the OWLManagerGroup. Check your manager usage.");
	}

	/**
	 * Make sure that no temporary file remain and all delta are in the storage. This function as debugging/testing purpose only.
	 * 
	 * @since 2.6.0
	 */
	public void flushIncrementalStorage()
	{
		_storageListener.flush();
	}

	/**
	 * Free all in memory resource. The 'in memory' space taken by the persistent data is also free, but the persistent is maintain for future usage. The
	 * storage system is disable.
	 *
	 * @since 2.5.1
	 */
	@Override
	public void close()
	{
		if (this == OWLUtils.getOwlManagerGroup())
		{
			_logger.log(Level.WARNING, "You try to close a static resource that should never be closed.");
			return;
		}

		if (_volatileManager != null)
		{
			_volatileManager.clearOntologies();
			_volatileManager.getIRIMappers().clear();
			_volatileManager = null; // Mark for GC.
		}
		if (_storageManager != null)
		{
			_storageManager.removeOntologyChangeListener(_storageListener);
			_storageListener.close();
			_storageManager.clearOntologies();
			_storageManager.getIRIMappers().clear();
			_storageManager = null; // Mark for GC.
		}
	}
}
