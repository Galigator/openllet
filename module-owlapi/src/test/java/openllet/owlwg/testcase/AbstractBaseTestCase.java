package openllet.owlwg.testcase;

import static java.lang.String.format;
import static java.util.Collections.unmodifiableSet;
import static openllet.owlwg.testcase.TestVocabulary.DatatypeProperty.IDENTIFIER;
import static openllet.owlwg.testcase.TestVocabulary.Individual.FULL;
import static openllet.owlwg.testcase.TestVocabulary.ObjectProperty.IMPORTED_ONTOLOGY;
import static openllet.owlwg.testcase.TestVocabulary.ObjectProperty.PROFILE;
import static openllet.owlwg.testcase.TestVocabulary.ObjectProperty.SEMANTICS;
import static openllet.owlwg.testcase.TestVocabulary.ObjectProperty.SPECIES;
import static openllet.owlwg.testcase.TestVocabulary.ObjectProperty.STATUS;

import java.util.Collection;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import openllet.owlwg.testcase.TestVocabulary.Individual;
import org.semanticweb.owlapi.model.IRI;
import org.semanticweb.owlapi.model.OWLDataPropertyExpression;
import org.semanticweb.owlapi.model.OWLIndividual;
import org.semanticweb.owlapi.model.OWLLiteral;
import org.semanticweb.owlapi.model.OWLNamedIndividual;
import org.semanticweb.owlapi.model.OWLObjectPropertyExpression;
import org.semanticweb.owlapi.model.OWLOntology;
import org.semanticweb.owlapi.search.EntitySearcher;

/**
 * <p>
 * Title: Abstract Base Test Case
 * </p>
 * <p>
 * Description: Common base implementation openllet.shared.hash by all test case
 * implementations.
 * </p>
 * <p>
 * Copyright: Copyright &copy; 2009
 * </p>
 * <p>
 * Company: Clark & Parsia, LLC. <a
 * href="http://clarkparsia.com/"/>http://clarkparsia.com/</a>
 * </p>
 *
 * @author     Mike Smith &lt;msmith@clarkparsia.com&gt;
 * @param  <O>
 */
public abstract class AbstractBaseTestCase<O> implements TestCase<O>
{
	private final String						_identifier;
	private final Map<IRI, ImportedOntology>	_imports;
	private final EnumSet<SyntaxConstraint>		_notsatisfied;
	private final EnumSet<Semantics>			_notsemantics;
	private final EnumSet<SyntaxConstraint>		_satisfied;
	private final EnumSet<Semantics>			_semantics;
	private final Status						_status;
	private final IRI							_iri;

	public AbstractBaseTestCase(final OWLOntology ontology, final OWLNamedIndividual i)
	{

		_iri = i.getIRI();

		final Map<OWLDataPropertyExpression, Collection<OWLLiteral>> dpValues = EntitySearcher.getDataPropertyValues(i, ontology).asMap();
		final Collection<OWLLiteral> identifiers = dpValues.get(IDENTIFIER.getOWLDataProperty());
		if (identifiers == null) throw new NullPointerException();
		if (identifiers.size() != 1) throw new IllegalArgumentException();

		_identifier = identifiers.iterator().next().getLiteral();

		final Map<OWLObjectPropertyExpression, Collection<OWLIndividual>> opValues = EntitySearcher.getObjectPropertyValues(i, ontology).asMap();

		_imports = new HashMap<>();
		final Collection<OWLIndividual> importedOntologies = opValues.get(IMPORTED_ONTOLOGY.getOWLObjectProperty());
		if (importedOntologies != null) for (final OWLIndividual ind : importedOntologies)
		{
			final ImportedOntology io = new ImportedOntologyImpl(ontology, ind.asOWLNamedIndividual());
			_imports.put(io.getIRI(), io);
		}

		final Collection<OWLIndividual> statuses = opValues.get(STATUS.getOWLObjectProperty());
		if (statuses == null || statuses.isEmpty())
			_status = null;
		else if (statuses.size() > 1)
			throw new IllegalArgumentException();
		else
		{
			final OWLNamedIndividual s = statuses.iterator().next().asOWLNamedIndividual();
			_status = Status.get(s);
			if (_status == null) throw new NullPointerException(format("Unexpected _status ( %s ) for test case %s", s.getIRI().toURI().toASCIIString(), i.getIRI()));
		}

		_satisfied = EnumSet.noneOf(SyntaxConstraint.class);
		final Collection<OWLIndividual> profiles = opValues.get(PROFILE.getOWLObjectProperty());
		if (profiles != null) for (final OWLIndividual p : profiles)
		{
			final SyntaxConstraint c = SyntaxConstraint.get(p);
			if (c == null) throw new NullPointerException(format("Unexpected profile ( %s ) for test case %s", p.asOWLNamedIndividual().getIRI().toURI().toASCIIString(), i.getIRI()));
			_satisfied.add(c);
		}

		final Collection<OWLIndividual> species = opValues.get(SPECIES.getOWLObjectProperty());
		if (species != null) for (final OWLIndividual s : species)
		{
			if (FULL.getOWLIndividual().equals(s)) continue;
			if (Individual.DL.getOWLIndividual().equals(s))
				_satisfied.add(SyntaxConstraint.DL);
			else
				throw new IllegalArgumentException(format("Unexpected species ( %s ) for test case %s", s.asOWLNamedIndividual().getIRI().toURI().toASCIIString(), i.getIRI()));
		}

		_semantics = EnumSet.noneOf(Semantics.class);
		final Collection<OWLIndividual> sems = opValues.get(SEMANTICS.getOWLObjectProperty());
		if (sems != null) for (final OWLIndividual sem : sems)
		{
			final Semantics s = Semantics.get(sem);
			if (s == null) throw new NullPointerException(format("Unexpected _semantics ( %s ) for test case %s ", sem.asOWLNamedIndividual().getIRI().toURI().toASCIIString(), i.getIRI()));
			_semantics.add(s);
		}

		final Map<OWLObjectPropertyExpression, Collection<OWLIndividual>> nopValues = EntitySearcher.getNegativeObjectPropertyValues(i, ontology).asMap();

		_notsatisfied = EnumSet.noneOf(SyntaxConstraint.class);

		final Collection<OWLIndividual> notprofiles = nopValues.get(PROFILE.getOWLObjectProperty());
		if (notprofiles != null) for (final OWLIndividual p : notprofiles)
		{
			final SyntaxConstraint c = SyntaxConstraint.get(p);
			if (c == null) throw new NullPointerException(format("Unexpected profile ( %s ) for test case %s", p.asOWLNamedIndividual().getIRI().toURI().toASCIIString(), i.getIRI()));
			_notsatisfied.add(c);
		}

		final Collection<OWLIndividual> notspecies = nopValues.get(SPECIES.getOWLObjectProperty());
		if (notspecies != null) for (final OWLIndividual s : notspecies)
			if (Individual.DL.getOWLIndividual().equals(s))
				_notsatisfied.add(SyntaxConstraint.DL);
			else
				throw new IllegalArgumentException(format("Unexpected species ( %s ) for test case %s", s.asOWLNamedIndividual().getIRI().toURI().toASCIIString(), i.getIRI()));

		_notsemantics = EnumSet.noneOf(Semantics.class);
		final Collection<OWLIndividual> notsems = nopValues.get(SEMANTICS.getOWLObjectProperty());
		if (notsems != null) for (final OWLIndividual sem : notsems)
		{
			final Semantics s = Semantics.get(sem);
			if (s == null) throw new NullPointerException(format("Unexpected _semantics ( %s ) for test case %s", sem.asOWLNamedIndividual().getIRI().toURI().toASCIIString(), i.getIRI()));
			_notsemantics.add(s);
		}
	}

	@Override
	public void dispose()
	{
		_imports.clear();
		_notsatisfied.clear();
		_semantics.clear();
	}

	@Override
	public Set<Semantics> getApplicableSemantics()
	{
		return unmodifiableSet(_semantics);
	}

	@Override
	public String getIdentifier()
	{
		return _identifier;
	}

	@Override
	public String getImportedOntology(final IRI iri, final SerializationFormat format)
	{
		final ImportedOntology io = _imports.get(iri);
		if (io == null)
			return null;
		else
			return io.getOntology(format);
	}

	@Override
	public Set<IRI> getImportedOntologies()
	{
		return unmodifiableSet(_imports.keySet());
	}

	@Override
	public Set<SerializationFormat> getImportedOntologyFormats(final IRI iri)
	{
		final ImportedOntology io = _imports.get(iri);
		if (io == null)
			return EnumSet.noneOf(SerializationFormat.class);
		else
			return io.getFormats();
	}

	@Override
	public Set<Semantics> getNotApplicableSemantics()
	{
		return unmodifiableSet(_notsemantics);
	}

	@Override
	public Set<SyntaxConstraint> getSatisfiedConstraints()
	{
		return unmodifiableSet(_satisfied);
	}

	@Override
	public Status getStatus()
	{
		return _status;
	}

	@Override
	public Set<SyntaxConstraint> getUnsatisfiedConstraints()
	{
		return unmodifiableSet(_notsatisfied);
	}

	@Override
	public IRI getIRI()
	{
		return _iri;
	}
}
