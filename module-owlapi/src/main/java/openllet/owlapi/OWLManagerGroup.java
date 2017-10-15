package openllet.owlapi;

import java.io.File;
import java.util.Optional;
import java.util.function.BiFunction;
import java.util.logging.Level;
import java.util.logging.Logger;
import openllet.shared.tools.Log;
import org.semanticweb.owlapi.apibinding.OWLManager;
import org.semanticweb.owlapi.model.OWLOntology;
import org.semanticweb.owlapi.model.OWLOntologyID;
import org.semanticweb.owlapi.model.OWLOntologyManager;

/**
 * Group of Owl Manager, volatile and persistent.
 *
 * @since 2.5.1
 */
public class OWLManagerGroup implements OWLGroup
{
	private static final Logger _logger = Log.getLogger(OWLManagerGroup.class);

	public volatile Optional<File> _ontologiesDirectory = Optional.empty();
	public volatile OWLOntologyManager _volatileManager = null;
	public volatile OWLOntologyManager _persistentManager = null;
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
		storageManager.ifPresent(m -> _persistentManager = m);
	}

	public OWLManagerGroup(final OWLOntology ontology)
	{
		_volatileManager = ontology.getOWLOntologyManager();
	}

	@Override
	public Logger getLogger()
	{
		return _logger;
	}

	@Override
	public boolean setOntologiesDirectory(final File directory)
	{
		_ontologiesDirectory = Optional.ofNullable(directory);
		return _ontologiesDirectory.isPresent();
	}

	@Override
	public Optional<File> getOntologiesDirectory()
	{
		return _ontologiesDirectory;
	}

	@Override
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
	@Override
	public synchronized OWLOntologyManager getPersistentManager()
	{
		if (null == _persistentManager)
		{
			_persistentManager = OWLManager.createConcurrentOWLOntologyManager();

			if (!getOntologiesDirectory().isPresent())
			{
				final String msg = "You should define a directory for stored ontologies before using stored ontologies.";
				final OWLException ex = new OWLException(msg);
				Log.error(getLogger(), msg, ex);
				throw ex;
			}

			try
			{
				_storageListener = new OWLIncrementalFlatFileStorageManagerListener(getOntologiesDirectory().get(), new File(getOntologiesDirectory().get().getPath() + File.separator + OWLHelper._delta), this);
				getPersistentManager().addOntologyChangeListener(_storageListener);
			}
			catch (final Exception e)
			{
				throw new OWLException(e);
			}
		}

		return _persistentManager;
	}

	@Override
	public boolean havePersistentManager()
	{
		return null != _persistentManager;
	}

	@Override
	public boolean haveVolatileManager()
	{
		return null != _volatileManager;
	}

	@Override
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
								OWLHelper.setFormat(ontology);
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

	@Override
	public void loadDirectory(final File directory)
	{
		loadDirectory(directory, getPersistentManager(), (m, f) ->
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

	@Override
	public String ontology2filename(final OWLOntologyID ontId)
	{
		if (_ontologiesDirectory.isPresent())
			return OWLHelper.ontology2filename(_ontologiesDirectory.get(), ontId);
		throw new OWLException("Storage directory should be define to enable loading of ontology by iri.");
	}

	@Override
	public String ontology2filename(final OWLOntology ontology)
	{
		return ontology2filename(ontology.getOntologyID());
	}

	@Override
	public void check(final OWLOntologyManager manager)
	{
		if (manager == _volatileManager || manager == _persistentManager)
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
		if (this == OWL._managerGroup)
		{
			_logger.log(Level.WARNING, "You try to close a static resource that should never be closed : ignore");
			return;
		}

		if (_volatileManager != null)
		{
			_volatileManager.clearOntologies();
			_volatileManager.getIRIMappers().clear();
			_volatileManager = null; // Mark for GC.
		}
		if (_persistentManager != null)
		{
			_persistentManager.removeOntologyChangeListener(_storageListener);
			_storageListener.close();
			_persistentManager.clearOntologies();
			_persistentManager.getIRIMappers().clear();
			_persistentManager = null; // Mark for GC.
		}
	}

}
