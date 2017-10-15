// Copyright (c) 2006 - 2008, Clark & Parsia, LLC. <http://www.clarkparsia.com>
// This source code is available under the terms of the Affero General Public
// License v3.
//
// Please see LICENSE.txt for full license terms, including the availability of
// proprietary exceptions.
// Questions, comments, or requests for clarification: licensing@clarkparsia.com

package openllet.owlapi;

import static org.semanticweb.owlapi.util.OWLAPIStreamUtils.asSet;

import java.util.Set;
import java.util.stream.Stream;
import openllet.atom.OpenError;
import openllet.core.KBLoader;
import openllet.core.KnowledgeBase;
import openllet.core.OpenlletOptions;
import org.semanticweb.owlapi.apibinding.OWLManager;
import org.semanticweb.owlapi.model.AddImport;
import org.semanticweb.owlapi.model.IRI;
import org.semanticweb.owlapi.model.MissingImportEvent;
import org.semanticweb.owlapi.model.MissingImportHandlingStrategy;
import org.semanticweb.owlapi.model.MissingImportListener;
import org.semanticweb.owlapi.model.OWLDataFactory;
import org.semanticweb.owlapi.model.OWLImportsDeclaration;
import org.semanticweb.owlapi.model.OWLOntology;
import org.semanticweb.owlapi.model.OWLOntologyChangeException;
import org.semanticweb.owlapi.model.OWLOntologyCreationException;
import org.semanticweb.owlapi.model.OWLOntologyManager;

/**
 * <p>
 * Copyright: Copyright (c) 2007
 * </p>
 * <p>
 * Company: Clark & Parsia, LLC. <http://www.clarkparsia.com>
 * </p>
 *
 * @author Evren Sirin
 */
public class OWLAPILoader extends KBLoader implements OWLHelper
{
	private final OWLOntologyManager _manager = OWLManager.createOWLOntologyManager();

	private final LimitedMapIRIMapper _iriMapper = new LimitedMapIRIMapper();

	private volatile OpenlletReasoner _reasoner;

	private volatile OWLOntology _ontology;

	private boolean _ignoreImports;

	/**
	 * A workaround for OWLAPI bug that does not let us import a loaded ontology so that we can minimize the warnings printed when
	 * OWLOntologyManager.makeLoadImportRequest is called
	 */
	private boolean _loadSingleFile;

	@Override
	public OWLOntologyManager getManager()
	{
		return _manager;
	}

	@Override
	public OWLDataFactory getFactory()
	{
		return _manager.getOWLDataFactory();
	}

	/**
	 * The ontology is load from a file but is not persist into a file after change, so it is a volatile ontology.
	 */
	@Override
	public boolean isVolatile()
	{
		return true;
	}

	@Override
	public OWLGroup getGroup()
	{
		return OWLGroup.fromVolatileManager(_manager);
	}

	/**
	 * Returns the reasoner created by this loader. A <code>null</code> value is returned until {@link #load()} function is called (explicitly or implicitly).
	 *
	 * @return the reasoner created by this loader
	 */
	@Override
	public OpenlletReasoner getReasoner()
	{
		return _reasoner;
	}

	@Override
	public OWLOntology getOntology()
	{
		return _ontology;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public KnowledgeBase getKB()
	{
		return _reasoner.getKB();
	}

	public OWLAPILoader()
	{
		_manager.setOntologyLoaderConfiguration(_manager.getOntologyLoaderConfiguration().setMissingImportHandlingStrategy(MissingImportHandlingStrategy.SILENT));
		_manager.addMissingImportListener(new MissingImportListener()
		{
			/**
			 * TODO
			 *
			 * @since
			 */
			private static final long serialVersionUID = -1580704502184270618L;

			@Override
			public void importMissing(final MissingImportEvent event)
			{
				if (!_ignoreImports)
				{
					final IRI importURI = event.getImportedOntologyURI();
					System.err.println("WARNING: Cannot import " + importURI);
					event.getCreationException().printStackTrace();
				}
			}
		});

		clear();
	}

	/**
	 * @Deprecated 2.5.1 use the stream version
	 */
	@Deprecated
	public Set<OWLOntology> getAllOntologies()
	{
		return asSet(_manager.ontologies());
	}

	public Stream<OWLOntology> allOntologies()
	{
		return _manager.ontologies();
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void load()
	{
		_reasoner = new OpenlletReasonerFactory().createReasoner(_ontology);
		_reasoner.getKB().setTaxonomyBuilderProgressMonitor(OpenlletOptions.USE_CLASSIFICATION_MONITOR.create());
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void parse(final String... fileNames)
	{
		// note if we will load a single file
		_loadSingleFile = fileNames.length == 1;

		super.parse(fileNames);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	protected void parseFile(final String file)
	{
		try
		{
			final IRI fileIRI = IRI.create(file);
			_iriMapper.addAllowedIRI(fileIRI);

			if (_loadSingleFile) // we are loading a single file so we can load it directly
				_ontology = _manager.loadOntologyFromOntologyDocument(fileIRI);
			else
			{
				// loading multiple files so each input file should be added as
				// an import to the base ontology we created
				final OWLOntology importOnt = _manager.loadOntologyFromOntologyDocument(fileIRI);
				final OWLImportsDeclaration declaration = _manager.getOWLDataFactory().getOWLImportsDeclaration(importOnt.getOntologyID().getOntologyIRI().get());
				_manager.applyChange(new AddImport(_ontology, declaration));
			}
		}
		catch (final IllegalArgumentException e)
		{
			throw new OpenError(file, e);
		}
		catch (final OWLOntologyCreationException e)
		{
			throw new OpenError(file, e);
		}
		catch (final OWLOntologyChangeException e)
		{
			throw new OpenError(file, e);
		}
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void setIgnoreImports(final boolean ignoreImports)
	{
		_ignoreImports = ignoreImports;
		_manager.getIRIMappers().clear();
		if (ignoreImports)
			_manager.getIRIMappers().add(_iriMapper);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void clear()
	{
		_iriMapper.clear();
		_manager.clearOntologies();

		try
		{
			_ontology = _manager.createOntology();
		}
		catch (final OWLOntologyCreationException e)
		{
			throw new OpenError(e);
		}
	}

	@Override
	public void dispose()
	{
		_reasoner.dispose();
	}
}
