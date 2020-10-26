// Portions Copyright (c) 2006 - 2008, Clark & Parsia, LLC. <http://www.clarkparsia.com>
// Clark & Parsia, LLC parts of this source code are available under the terms of the Affero General Public License v3.
//
// Please see LICENSE.txt for full license terms, including the availability of proprietary exceptions.
// Questions, comments, or requests for clarification: licensing@clarkparsia.com
//
// ---
// Portions Copyright (c) 2003 Ron Alford, Mike Grove, Bijan Parsia, Evren Sirin
// Alford, Grove, Parsia, Sirin parts of this source code are available under the terms of the MIT License.
//
// The MIT License
//
// Permission is hereby granted, free of charge, to any person obtaining a copy
// of this software and associated documentation files (the "Software"), to
// deal in the Software without restriction, including without limitation the
// rights to use, copy, modify, merge, publish, distribute, sublicense, and/or
// sell copies of the Software, and to permit persons to whom the Software is
// furnished to do so, subject to the following conditions:
//
// The above copyright notice and this permission notice shall be included in
// all copies or substantial portions of the Software.
//
// THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
// IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
// FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
// AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
// LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING
// FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS
// IN THE SOFTWARE.

package openllet.owlapi;

import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Logger;
import java.util.stream.Collectors;

import org.semanticweb.owlapi.model.AddAxiom;
import org.semanticweb.owlapi.model.AddImport;
import org.semanticweb.owlapi.model.AddOntologyAnnotation;
import org.semanticweb.owlapi.model.OWLAxiom;
import org.semanticweb.owlapi.model.OWLObject;
import org.semanticweb.owlapi.model.OWLOntology;
import org.semanticweb.owlapi.model.OWLOntologyChange;
import org.semanticweb.owlapi.model.OWLOntologyChangeVisitor;
import org.semanticweb.owlapi.model.OWLOntologyManager;
import org.semanticweb.owlapi.model.RemoveAxiom;
import org.semanticweb.owlapi.model.RemoveImport;
import org.semanticweb.owlapi.model.RemoveOntologyAnnotation;
import org.semanticweb.owlapi.model.SetOntologyID;

import openllet.aterm.ATermAppl;
import openllet.core.KnowledgeBase;
import openllet.core.exceptions.InternalReasonerException;
import openllet.core.utils.SetUtils;
import openllet.core.utils.Timer;
import openllet.owlapi.facet.FacetManagerOWL;
import openllet.shared.tools.Log;

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
public class PelletLoader implements FacetManagerOWL
{
	public static Logger _logger = Log.getLogger(PelletLoader.class);

	private volatile KnowledgeBase _kb;

	private volatile OWLOntologyManager _manager;

	private final Set<OWLOntology> _ontologies = SetUtils.create();

	/**
	 * Flag to check if imports will be automatically loaded/unloaded
	 */
	private volatile boolean _processImports;

	/**
	 * Ontologies that are loaded due to imports but they have not been included in an explicit load statement by the user
	 */
	private final Set<OWLOntology> _notImported = SetUtils.create();

	/**
	 * This is the reverse mapping of imports. The key is an ontology and the value is a set of ontology that imports the ontology used as the key
	 */
	private final Map<OWLOntology, Set<OWLOntology>> _importDependencies = new ConcurrentHashMap<>();

	private final PelletVisitor _visitor;

	private final ChangeVisitor _changeVisitor = new ChangeVisitor();

	@Override
	public OWLOntologyManager getManager()
	{
		return _manager;
	}

	public void setManager(final OWLOntologyManager manager)
	{
		_manager = manager;
	}

	@Override
	public OWLGroup getGroup()
	{
		return OWLGroup.fromVolatileManager(_manager);
	}

	private class ChangeVisitor implements OWLOntologyChangeVisitor
	{
		private boolean _reloadRequired;

		public boolean isReloadRequired()
		{
			return _reloadRequired;
		}

		/**
		 * Process a change, providing a single call for common reset,accept,isReloadRequired pattern.
		 *
		 * @param change the {@link OWLOntologyChange} to process
		 * @return <code>true</code> if change is handled, <code>false</code> if a reload is required
		 */
		public boolean process(final OWLOntologyChange change)
		{
			reset();
			change.accept(this);
			return !isReloadRequired();
		}

		public void reset()
		{
			_visitor.reset();
			_reloadRequired = false;
		}

		@Override
		public void visit(final AddAxiom change)
		{
			_visitor.setAddAxiom(true);
			change.getAxiom().accept(_visitor);
			_reloadRequired = _visitor.isReloadRequired();
		}

		@Override
		public void visit(final RemoveAxiom change)
		{
			_visitor.setAddAxiom(false);
			change.getAxiom().accept(_visitor);
			_reloadRequired = _visitor.isReloadRequired();
		}

		@Override
		public void visit(final AddImport change)
		{
			_reloadRequired = getProcessImports();
		}

		@Override
		public void visit(final AddOntologyAnnotation change)
		{
			// Core reasoner don't process annotations
		}

		@Override
		public void visit(final RemoveImport change)
		{
			_reloadRequired = getProcessImports();
		}

		@Override
		public void visit(final RemoveOntologyAnnotation change)
		{
			// Core reasoner don't process annotations
		}

		@Override
		public void visit(final SetOntologyID change)
		{
			// nothing to do here
		}

	}

	public PelletLoader(final KnowledgeBase kb)
	{
		_kb = kb;

		_visitor = new PelletVisitor(kb);

		_processImports = true;
	}

	/**
	 * @deprecated Use {@link #getProcessImports()} instead
	 */
	@SuppressWarnings("javadoc")
	@Deprecated
	public boolean loadImports()
	{
		return getProcessImports();
	}

	/**
	 * @deprecated Use {@link #setProcessImports(boolean)} instead
	 */
	@Deprecated
	public void setLoadImports(@SuppressWarnings("javadoc") final boolean loadImports)
	{
		setProcessImports(loadImports);
	}

	public boolean getProcessImports()
	{
		return _processImports;
	}

	public void setProcessImports(final boolean processImports)
	{
		_processImports = processImports;
	}

	public void clear()
	{
		_visitor.clear();
		_kb.clear();
		_ontologies.clear();
		_notImported.clear();
		_importDependencies.clear();
	}

	public KnowledgeBase getKB()
	{
		return _kb;
	}

	public void setKB(final KnowledgeBase kb)
	{
		_kb = kb;
	}

	public ATermAppl term(final OWLObject d)
	{
		_visitor.reset();
		_visitor.setAddAxiom(false);
		d.accept(_visitor);

		final ATermAppl a = _visitor.result();

		if (a == null)
			throw new InternalReasonerException("Cannot create ATerm from description " + d);

		return a;
	}

	public void reload()
	{
		_logger.fine("Reloading the ontologies");

		// copy the loaded ontologies
		final Set<OWLOntology> notImportedOnts = new HashSet<>(_notImported);

		clear(); // clear everything

		load(notImportedOnts); // load ontologies again
	}

	public void load(final Set<OWLOntology> ontologies)
	{
		final Optional<Timer> timer = _kb.getTimers().startTimer("load");

		final Collection<OWLOntology> toBeLoaded = new LinkedHashSet<>();
		for (final OWLOntology ontology : ontologies)
			load(ontology, false, toBeLoaded);

		_visitor.reset();
		_visitor.setAddAxiom(true);

		for (final OWLOntology ontology : toBeLoaded)
			ontology.accept(_visitor);

		_visitor.verify();

		timer.ifPresent(Timer::stop);
	}

	private int load(final OWLOntology ontology, final boolean imported, final Collection<OWLOntology> toBeLoaded)
	{
		// if not imported add it to notImported set
		if (!imported)
			_notImported.add(ontology);

		// add to the loaded _ontologies
		final boolean added = _ontologies.add(ontology);

		// if it was already there, nothing more to do
		if (!added)
			return 0;

		int axiomCount = ontology.getAxiomCount();
		toBeLoaded.add(ontology);

		// if processing imports load the imported ontologies
		if (_processImports)
			for (final OWLOntology importedOnt : ontology.imports().collect(Collectors.toList()))
			{
				// load the importedOnt
				axiomCount += load(importedOnt, true, toBeLoaded);

				// update the import dependencies
				Set<OWLOntology> importees = _importDependencies.get(importedOnt);
				if (importees == null)
				{
					importees = new HashSet<>();
					_importDependencies.put(importedOnt, importees);
				}
				importees.add(ontology);
			}

		return axiomCount;
	}

	public Set<OWLAxiom> getUnsupportedAxioms()
	{
		return _visitor.getUnsupportedAxioms();
	}

	public void unload(final Set<OWLOntology> ontologies)
	{
		ontologies.forEach(this::unload);
	}

	private void unload(final OWLOntology ontology)
	{
		// remove the ontology from the set
		final boolean removed = _ontologies.remove(ontology);

		// if it is not there silently return
		if (!removed)
			return;

		// remove it from notImported set, too
		_notImported.remove(ontology);

		// if we are processing imports we might need to unload the imported ontologies
		if (_processImports)
			ontology.imports().forEach(importOnt ->
			{
				// see if the importedOnt is imported by any other ontology
				final Set<OWLOntology> importees = _importDependencies.get(importOnt);
				if (importees != null)
				{
					importees.remove(ontology); // remove the unloaded ontology from the dependencies
					if (importees.isEmpty()) // if nothing is left
					{
						_importDependencies.remove(importOnt); // remove the empty set from dependencies
						if (!_notImported.contains(importOnt)) // only unload if this ontology was not loaded by the user explicitly
							unload(importOnt);
					}
				}
			});
	}

	/**
	 * @return Returns the loaded _ontologies.
	 */
	public Set<OWLOntology> getOntologies()
	{
		return Collections.unmodifiableSet(_ontologies);
	}

	/**
	 * Apply the given changes to the Pellet KB.
	 *
	 * @param changes List of ontology changes to be applied
	 * @return <code>true</code> if changes applied successfully, <code>false</code> otherwise indicating a reload is required
	 * @throws OWLException
	 */
	public boolean applyChanges(final List<? extends OWLOntologyChange> changes)
	{

		for (final OWLOntologyChange change : changes)
		{
			if (!_ontologies.contains(change.getOntology()))
				continue;

			if (!_changeVisitor.process(change))
			{
				_logger.fine(() -> "Reload required by ontology change " + change);

				return false;
			}
		}

		return true;
	}

}
