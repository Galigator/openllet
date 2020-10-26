package openllet.owlapi;

import java.io.File;
import java.io.InputStream;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;

import org.semanticweb.owlapi.io.StringDocumentSource;
import org.semanticweb.owlapi.model.IRI;
import org.semanticweb.owlapi.model.OWLAxiom;
import org.semanticweb.owlapi.model.OWLDataFactory;
import org.semanticweb.owlapi.model.OWLOntology;
import org.semanticweb.owlapi.model.OWLOntologyCreationException;
import org.semanticweb.owlapi.model.OWLOntologyID;
import org.semanticweb.owlapi.model.OWLOntologyManager;
import org.semanticweb.owlapi.reasoner.OWLReasoner;

import openllet.atom.OpenError;
import openllet.shared.tools.Log;

/**
 * The main difference between OWLTools and OWLGenericTools is the usage of static resources by OWLTools.
 *
 * @since 2.5.1
 */
public class OWLGenericTools implements OWLHelper
{
	private static final Logger _logger = Log.getLogger(OWLGenericTools.class);

	/**
	 * Ontology denote the current ontology. So it can change in version of environment.
	 *
	 * @since 2.5.1
	 */
	protected volatile OWLOntology _ontology;

	//	protected final OWLOntologyManager _manager;

	protected final OWLGroup _group;
	private volatile Function<OWLOntology, OWLReasoner> _reasonerFactory = OpenlletReasonerFactory.getInstance()::createReasoner;

	protected boolean _isVolatile = true;

	private Optional<OWLReasoner> _reasoner = Optional.empty();

	@Override
	public Logger getLogger()
	{
		return _logger;
	}

	@Override
	public OWLOntology getOntology()
	{
		return _ontology;
	}

	@Override
	public OWLOntologyManager getManager()
	{
		return _ontology.getOWLOntologyManager();
	}

	@Override
	public OWLGroup getGroup()
	{
		return _group;
	}

	@Override
	public boolean isVolatile()
	{
		return _isVolatile;
	}

	@Override
	public OWLDataFactory getFactory()
	{
		return getManager().getOWLDataFactory();
	}

	@Override
	public OWLReasoner getReasoner()
	{
		if (!_reasoner.isPresent())
			try
			{

				final OWLReasoner reasoner = _reasonerFactory.apply(getOntology());
				reasoner.isConsistent();
				_reasoner = Optional.of(reasoner);
			}
			catch (final Exception e)
			{
				Log.error(_logger, e);
				throw new OpenError(e);
			}

		_reasoner.get().flush();
		return _reasoner.get();
	}

	public OWLGenericTools setReasonerFactory(final Function<OWLOntology, OWLReasoner> reasonerFactory)
	{
		_reasonerFactory = reasonerFactory;
		return this;
	}

	/**
	 * Load the ontology ontologyId into the good manager or create it if it doesn't previously exist.
	 *
	 * @param group of managers.
	 * @param ontologyID is the reference to the ontology
	 * @throws OWLOntologyCreationException is raise when things goes baddly wrong.
	 * @since 2.5.1
	 */
	public OWLGenericTools(final OWLGroup group, final OWLOntologyID ontologyID) throws OWLOntologyCreationException
	{
		if (ontologyID.getOntologyIRI().toString().indexOf(' ') != -1)
			throw new OWLOntologyCreationException("Illegal character ' ' in name on [" + ontologyID.getOntologyIRI() + "]");
		else
			if (ontologyID.getVersionIRI() != null && ontologyID.getVersionIRI().toString().indexOf(' ') != -1)
				throw new OWLOntologyCreationException("Illegal character ' ' in version on [" + ontologyID.getVersionIRI() + "]");
		_group = group;

		// Maybe already in a manager.
		if (group.getOntologiesDirectory().isPresent())
			if (group.havePersistentManager())
				_ontology = OWLGroup.getOntology(group.getPersistentManager(), ontologyID).orElse(null);

		OWLOntologyManager manager;
		if (_ontology != null)
			manager = _ontology.getOWLOntologyManager();
		else
		{ // Not in storage manager
			_ontology = OWLGroup.getOntology(group.getVolatileManager(), ontologyID).orElse(null);
			if (_ontology != null)
				manager = _ontology.getOWLOntologyManager();
			else
			{ // Not in volatile manager.
				// Maybe we should load it.
				manager = group.getOntologiesDirectory().isPresent() ? group.getPersistentManager() : group.getVolatileManager();
				final File file = new File(group.ontology2filename(ontologyID));
				if (file.exists())
					try
					{
						_ontology = manager.loadOntologyFromOntologyDocument(file);
					}
					catch (final Exception e)
					{
						_logger.log(Level.INFO, "Ontology " + ontologyID + " couldn't be load", e);
					}
				if (_ontology == null) // Maybe we should create it.
				{
					_logger.log(Level.FINE, "Ontology " + ontologyID + " will be create now");
					_ontology = manager.createOntology(ontologyID);
				}
			}
		}

		if (group.haveVolatileManager() && !group.havePersistentManager())
			_isVolatile = true;
		else
			if (!group.haveVolatileManager() && group.havePersistentManager())
				_isVolatile = false;
			else
				_isVolatile = manager == group.getVolatileManager();
		OWLHelper.setFormat(_ontology);
	}

	public OWLGenericTools(final OWLGroup group, final OWLOntologyID ontologyID, final boolean isVolatile) throws OWLOntologyCreationException
	{
		if (ontologyID.getOntologyIRI().toString().indexOf(' ') != -1)
			throw new OWLOntologyCreationException("Illegal character ' ' in name on [" + ontologyID.getOntologyIRI() + "]");
		else
			if (ontologyID.getVersionIRI() != null && ontologyID.getVersionIRI().toString().indexOf(' ') != -1)
				throw new OWLOntologyCreationException("Illegal character ' ' in version on [" + ontologyID.getVersionIRI() + "]");

		_group = group;
		_isVolatile = isVolatile;
		final OWLOntologyManager manager = _isVolatile ? group.getVolatileManager() : group.getPersistentManager();
		_ontology = OWLGroup.getOntology(manager, ontologyID).orElse(null); // Maybe already in the manager.

		// Maybe we should load it.
		if (_ontology == null && !_isVolatile)
		{
			final File file = new File(group.ontology2filename(ontologyID));
			if (file.exists())
				try
				{
					_ontology = manager.loadOntologyFromOntologyDocument(file);
				}
				catch (final Exception e)
				{
					_logger.log(Level.INFO, "Ontology " + ontologyID + " couldn't be load", e);
				}
		}

		if (_ontology == null)
		{
			_logger.log(Level.INFO, () -> "Ontology " + ontologyID + " will be create now");
			_ontology = manager.createOntology(ontologyID);// Maybe we should create it.
		}

		OWLHelper.setFormat(_ontology);
	}

	public OWLGenericTools(final OWLGroup group, final IRI ontologyIRI, final double version) throws OWLOntologyCreationException
	{
		this(group, new OWLOntologyID(ontologyIRI, OWLHelper.buildVersion(ontologyIRI, version)));
	}

	public OWLGenericTools(final OWLGroup group, final IRI ontologyIRI, final double version, final boolean isVolatile) throws OWLOntologyCreationException
	{
		this(group, new OWLOntologyID(ontologyIRI, OWLHelper.buildVersion(ontologyIRI, version)), isVolatile);
	}

	public OWLGenericTools(final OWLGroup group, final IRI ontologyIRI, final boolean isVolatile) throws OWLOntologyCreationException
	{
		this(group, ontologyIRI, 0, isVolatile);
	}

	public OWLGenericTools(final OWLGroup group, final InputStream is) throws OWLOntologyCreationException
	{
		_group = group;
		_ontology = group.getVolatileManager().loadOntologyFromOntologyDocument(is);
		_isVolatile = true;
		OWLHelper.setFormat(_ontology);
	}

	public OWLGenericTools(final OWLGroup group, final OWLOntology ontology, final boolean isVolatile)
	{
		_group = group;
		_ontology = ontology;
		_isVolatile = isVolatile;
		OWLHelper.setFormat(_ontology);
		group.check(getManager());
	}

	public OWLGenericTools(final OWLGroup group, final OWLOntologyManager manager, final OWLOntology ontology)
	{
		_group = group;
		_ontology = ontology;
		_isVolatile = manager == group.getVolatileManager();
		OWLHelper.setFormat(_ontology);
		group.check(getManager());
	}

	public OWLGenericTools(final OWLGroup group, final OWLOntologyManager manager, final File file) throws OWLOntologyCreationException
	{
		_group = group;
		_ontology = manager.loadOntologyFromOntologyDocument(file);
		_isVolatile = manager == group.getVolatileManager();
		OWLHelper.setFormat(_ontology);
		group.check(getManager());
	}

	// Raw create
	public OWLGenericTools(final OWLGroup group, final OWLOntology ontology, final OWLOntologyManager manager, final Map<String, String> namespaces)
	{
		_group = group;
		_ontology = ontology;
		if (!namespaces.isEmpty())
			getNamespaces().ifPresent(space -> namespaces.forEach(space::setPrefix));
		_isVolatile = manager == group.getVolatileManager();
		OWLHelper.setFormat(_ontology);
		group.check(getManager());
	}

	public OWLGenericTools(final OWLGroup group, final File file) throws Exception
	{
		this(group, group.getPersistentManager(), file);
	}

	public OWLGenericTools(final OWLGroup group, final String ontology, final boolean isVolatile) throws Exception
	{
		_group = group;
		_ontology = (isVolatile ? _group.getVolatileManager() : _group.getPersistentManager()).loadOntologyFromOntologyDocument(new StringDocumentSource(ontology));
		_isVolatile = isVolatile;
		OWLHelper.setFormat(_ontology);
	}

	@Override
	public String toString()
	{
		return getOntology().axioms().map(OWLAxiom::toString).sorted().collect(Collectors.joining("\n"));
	}

	@Override
	public void dispose()
	{
		_reasoner.ifPresent(OWLReasoner::dispose);
	}
}
