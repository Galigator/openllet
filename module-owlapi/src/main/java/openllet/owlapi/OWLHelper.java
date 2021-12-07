package openllet.owlapi;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Optional;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Stream;

import org.semanticweb.owlapi.apibinding.OWLManager;
import org.semanticweb.owlapi.dlsyntax.renderer.DLSyntaxObjectRenderer;
import org.semanticweb.owlapi.formats.FunctionalSyntaxDocumentFormatFactory;
import org.semanticweb.owlapi.formats.PrefixDocumentFormat;
import org.semanticweb.owlapi.formats.RDFXMLDocumentFormatFactory;
import org.semanticweb.owlapi.io.XMLUtils;
import org.semanticweb.owlapi.model.IRI;
import org.semanticweb.owlapi.model.OWLAnonymousIndividual;
import org.semanticweb.owlapi.model.OWLAxiom;
import org.semanticweb.owlapi.model.OWLClass;
import org.semanticweb.owlapi.model.OWLDataFactory;
import org.semanticweb.owlapi.model.OWLDocumentFormat;
import org.semanticweb.owlapi.model.OWLIndividual;
import org.semanticweb.owlapi.model.OWLNamedIndividual;
import org.semanticweb.owlapi.model.OWLOntology;
import org.semanticweb.owlapi.model.OWLOntologyAlreadyExistsException;
import org.semanticweb.owlapi.model.OWLOntologyCreationException;
import org.semanticweb.owlapi.model.OWLOntologyID;
import org.semanticweb.owlapi.model.OWLOntologyManager;
import org.semanticweb.owlapi.model.OWLOntologyStorageException;
import org.semanticweb.owlapi.reasoner.NodeSet;
import org.semanticweb.owlapi.util.OWLDocumentFormatFactoryImpl;

import openllet.atom.OpenError;
import openllet.shared.tools.Logging;

/**
 * Functions that help management of OWL related matters. NB: This interface should replace every occurrence of OWLTools every where it is possible.
 *
 * @since 2.5.1
 */
public interface OWLHelper extends Logging, OWLManagementObject
{
	boolean _debug = false;

	String _protocol = "http://";
	String _secureProtocol = "https://";
	String _localProtocol = "file:/";
	String _webSeparator = "/";
	String _prefixSeparator = ":";
	String _entitySeparator = "#";
	String _innerSeparator = "_";
	String _caseSeparator = "-";
	String _fileExtention = ".owl";
	String _fileExtentionPart = ".part";
	String _delta = "owl.delta";
	OWLDocumentFormatFactoryImpl _formatFactory = new FunctionalSyntaxDocumentFormatFactory(); // new RDFXMLDocumentFormatFactory(); see https://github.com/owlcs/owlapi/issues/706
	OWLDocumentFormat _format = _formatFactory.get();

	/**
	 * @return true if this ontology isn't persistent.
	 * @since 2.5.1
	 */
	boolean isVolatile();

	/**
	 * Add the export format in the configuration of the provided ontology
	 *
	 * @param ontology you consider.
	 * @since 2.6.1
	 */
	static void setFormat(final OWLOntology ontology)
	{
		ontology.getOWLOntologyManager().setOntologyFormat(ontology, _format);
	}

	/**
	 * @param ontologyIRI is the id of the ontology without version. The ontology name.
	 * @param version is the short representation you want for this ontology.
	 * @return the complete representation of the version for the given identifier of ontology.
	 * @since 2.5.1
	 */
	static IRI buildVersion(final IRI ontologyIRI, final double version)
	{
		return IRI.create(ontologyIRI + _innerSeparator + version);
	}

	/**
	 * @param ontologyIRI is the id of the ontology without version. The ontology name.
	 * @param version is the short representation you want for this ontology.
	 * @return the complete ontologyID
	 * @since 2.6.0
	 */
	static OWLOntologyID getVersion(final IRI ontologyIRI, final double version)
	{
		return new OWLOntologyID(ontologyIRI, buildVersion(ontologyIRI, version));
	}

	/**
	 * @param iri that should be use to generate a filename
	 * @return a relative path filename that reflect the iri.
	 * @since 2.5.1
	 */
	static String iri2filename(final IRI iri)
	{
		if (null == iri)
			throw new OWLException("iri2filename(null)");

		return iri.toString()// iri.getNamespace() + iri.getFragment();
				.replace(':', '_')//
				.replace('/', '_')//
				.replace('&', '_');
	}

	/**
	 * @param directory where the ontology will be put
	 * @param ontId is the id the ontology to convert.
	 * @return a full path filename that reflect the name of this ontology.
	 * @since 2.5.1
	 */
	static String ontology2filename(final File directory, final OWLOntologyID ontId)
	{
		final String id = iri2filename(ontId.getOntologyIRI().get());
		final IRI versionIRI = ontId.getVersionIRI().get();
		final String version = versionIRI != null ? iri2filename(versionIRI) : "0";
		return directory + _webSeparator + id + _caseSeparator + version + _fileExtention;
	}

	/**
	 * @param directory where the ontology will be put
	 * @param ontology is the ontology from witch we want a name
	 * @return a full path filename that reflect the name of this ontology.
	 * @since 2.5.1
	 */
	static String ontology2filename(final File directory, final OWLOntology ontology)
	{
		return ontology2filename(directory, ontology.getOntologyID());
	}

	//////////////////////////////// defaults methods //////////////////////////////////////

	/**
	 * @return the namespace utils that can resolve prefix.
	 * @since 2.5.1
	 */
	default Optional<PrefixDocumentFormat> getNamespaces()
	{
		final OWLDocumentFormat format = getManager().getOntologyFormat(getOntology());
		return format.isPrefixOWLDocumentFormat() ? Optional.of((PrefixDocumentFormat) format) : Optional.empty();
	}

	/**
	 * @return the root of the default object insert in the ontology without namespace.
	 * @since 2.5.1
	 */
	default IRI getRootIri()
	{
		return IRI.create(getOntology().getOntologyID().getOntologyIRI().get().getNamespace());
	}

	/**
	 * This function exist because the one in IRI is deprecated and will be remove : We want the memory of calling it 'fragment' preserved.
	 *
	 * @param iri to convert to XML from NCNameSuffix.
	 * @return the NCNameSuffix
	 * @since 2.5.1
	 */
	default String getFragment(final IRI iri)
	{
		return XMLUtils.getNCNameSuffix(iri);
	}

	/**
	 * The standard 'getOntology' from the OWLManager don't really take care of versionning. This function is here to enforce the notion of version
	 *
	 * @param ontologyID with version information
	 * @return the ontology if already load into the given manager.
	 * @since 2.5.1
	 */
	default Optional<OWLOntology> getOntology(final OWLOntologyID ontologyID)
	{
		return OWLGroup.getOntology(getManager(), ontologyID);
	}

	/**
	 * @return the shortest representation of the version of an ontology. Defaulting on 'zero' if no-version information.
	 * @since 2.5.1
	 */
	default double getVersion()
	{
		final IRI version = getOntology().getOntologyID().getVersionIRI().get();
		if (null == version)
			return 0;
		try
		{
			final String fragment = getFragment(version);
			final int index = fragment.lastIndexOf(_innerSeparator.charAt(0));
			return Double.parseDouble(fragment.substring(index + 1));
		}
		catch (final Exception e)
		{
			getLogger().log(Level.SEVERE, version.toString() + " isn't a standard version format", e);
			throw new OWLException("Plz use " + OWLHelper.class.getSimpleName() + " to manage your versions.", e);
		}
	}

	@SuppressWarnings("resource") // ressource is manage by the helper.
	default Optional<OWLHelper> look(final IRI ontology, final double version)
	{
		return getGroup().getOntology(ontology, version, isVolatile());
	}

	@SuppressWarnings("resource") // ressource is manage by the helper.
	default Optional<OWLHelper> look(final IRI ontology)
	{
		return getGroup().getOntology(new OWLOntologyID(ontology), isVolatile());
	}

	/**
	 * Clone into another ontology with the same axioms and same manager. NB : In a future version this function may return an ontology that share axiom with
	 * previous for memory saving.
	 *
	 * @param version that will have the new ontology.
	 * @return a new ontology with the axioms of the given one.
	 * @throws OWLOntologyCreationException if we can't create the ontology.
	 * @since 2.5.1
	 */
	default OWLHelper derivate(final double version) throws OWLOntologyCreationException
	{
		final Optional<OWLHelper> result = look(this.getOntology().getOntologyID().getOntologyIRI().get(), version);
		if (!result.isPresent())
			throw new OWLOntologyCreationException("Can't derivate to version " + version);

		if (result.get().getOntology().getAxiomCount() != 0)
			getLogger().warning(() -> "The ontology you try to derivate from " + getVersion() + " to version " + version + " already exist.");

		result.get().addAxioms(getOntology().axioms());
		return result.get();
	}

	/**
	 * Same as derivate but with a version number based on EPOCH time.
	 *
	 * @return a new ontology with the axioms of the given one.
	 * @throws OWLOntologyCreationException if we can't create the ontology.
	 * @since 2.5.1
	 * @Deprecated because we want this function to return an OWLHelper
	 */
	default OWLHelper derivate() throws OWLOntologyCreationException
	{
		return derivate(System.currentTimeMillis());
	}

	/**
	 * @param quoteExpression an expression with " at begin and end.
	 * @return the same expression without the first and last char.
	 * @since 1.1
	 */
	default String removeFirstLast(final String quoteExpression)
	{
		String expression = quoteExpression;
		if (quoteExpression != null)
			expression = quoteExpression.substring(1, quoteExpression.length() - 1); // Remove the " at begin and end of literal.

		return expression;
	}

	/**
	 * @param parts of an uri
	 * @return a join of all parts separated by an entitySeparator.
	 * @since 2.5.1
	 */
	default String path(final String[] parts)
	{
		final StringBuffer buff = new StringBuffer();
		buff.append(parts[1]);

		for (int i = 2; i < parts.length; i++)
			buff.append(_entitySeparator).append(parts[i]);

		return buff.toString();
	}

	/**
	 * @param identifier to resolve
	 * @return parts of the identifiers using optionnaly prefix resolution.
	 * @since 2.5.1
	 */
	default String[] resolvPrefix(final String identifier)
	{
		final String[] parts = identifier.split(":");

		if (parts.length == 0)
			return new String[] { getRootIri().toString(), "" };

		final Optional<PrefixDocumentFormat> space = getNamespaces();
		if (space.isPresent())
		{
			if (parts.length == 1)
				return new String[] { space.get().getPrefix(""), parts[0] };
			else
				return new String[] { space.get().getPrefix(parts[0]), path(parts) };
		}
		else
			return new String[] { identifier };
	}

	/**
	 * @param identifier of the data.
	 * @return an array of size 2, with the first element that contain the namespace and the second that contain the fragment.
	 */
	default String[] getNameSpace(final String identifier)
	{
		if (identifier.startsWith(_protocol) || identifier.startsWith(_secureProtocol))
		{
			final String[] parts = identifier.split(_entitySeparator); // XXX see also this : XMLUtils.getNCNamePrefix(identifier);
			switch (parts.length)
			{
				case 0:
					throw new OpenError("Error processing : " + identifier);
				case 1:
					return new String[] { identifier, "" };
				default:
				{
					return new String[] { parts[0], path(parts) };
				}
			}
		}
		else
			return resolvPrefix(identifier);
	}

	/**
	 * Axiom are parsed from the stream then add into the ontology.
	 *
	 * @param input is a stream of axioms
	 * @throws OWLOntologyCreationException if we can't load the ontology.
	 * @throws IOException if there is an problem when reading.
	 * @since 2.5.1
	 */
	default void deserializeAxiomsInto(final String input) throws OWLOntologyCreationException, IOException
	{
		try (final InputStream stream = new ByteArrayInputStream(input.getBytes()))
		{
			addAxioms(getManager().loadOntologyFromOntologyDocument(stream).axioms());
		}
		catch (final OWLOntologyAlreadyExistsException e)
		{
			if (e.getOntologyID().equals(getOntology().getOntologyID()))
			{
				getLogger().severe("The ontology already exists with the name of the Tools : " + e.getOntologyID());
				throw e;
			}
			else
			{
				getManager().removeOntology(e.getOntologyID());
				deserializeAxiomsInto(input); // WARN : if the file define 2 ontologies and one is already define, then an infinite loop here can occur.
			}
		}
	}

	/**
	 * @param input is a stream of axioms
	 * @return the set of axiom contains in the input
	 * @throws OWLOntologyCreationException if we can't load the ontology.
	 * @throws IOException if there is an problem when reading.
	 * @since 2.5.1
	 */
	default Stream<OWLAxiom> deserializeAxioms(final String input) throws OWLOntologyCreationException, IOException
	{
		try (final InputStream stream = new ByteArrayInputStream(input.getBytes()))
		{
			final OWLOntologyManager manager = OWLManager.createOWLOntologyManager();
			final OWLOntology ontology = manager.loadOntologyFromOntologyDocument(stream);
			return ontology.axioms();
		}
	}

	/**
	 * @return the axioms as a single blob string.
	 * @throws OWLOntologyStorageException if we can't store the ontology.
	 * @throws IOException if there is an problem when reading. * @since 1.2
	 * @since 2.5.1
	 */
	default String serializeAxioms() throws OWLOntologyStorageException, IOException
	{
		try (final ByteArrayOutputStream stream = new ByteArrayOutputStream())
		{
			getManager().saveOntology(getOntology(), stream);
			return stream.toString();
		}
		catch (final OWLOntologyStorageException | IOException e)
		{
			getLogger().log(Level.SEVERE, "Problem at serialisation of axioms : " + getOntology().getOntologyID(), e);
			throw e;
		}
	}

	/**
	 * Compute the types of an individual. Use this function only if you mix Named and Anonymous individuals.
	 *
	 * @param ind the individual named _or_ anonymous
	 * @return the classes of the individual.
	 * @since 2.5.1
	 */
	default NodeSet<OWLClass> getTypes(final OWLIndividual ind)
	{
		if (ind instanceof OWLAnonymousIndividual)
		{
			// We create a temporary named Individual to allow the reasoner to work.
			final OWLNamedIndividual individual = getFactory().getOWLNamedIndividual(IRI.create(_protocol + OWLHelper.class.getPackage().getName() + _webSeparator + OWLHelper.class.getSimpleName() + _entitySeparator + IRIUtils.randId(OWLHelper.class.getSimpleName())));
			final Stream<OWLAxiom> axioms = Stream.of( //
					getFactory().getOWLDeclarationAxiom(individual), //
					getFactory().getOWLSameIndividualAxiom(individual, ind) // The temporary named is the same as the anonymous one.
			);
			getManager().addAxioms(getOntology(), axioms);
			final NodeSet<OWLClass> result = getReasoner().getTypes(individual, false);
			getManager().removeAxioms(getOntology(), axioms);
			return result;
		}
		else
			return getReasoner().getTypes((OWLNamedIndividual) ind, false);
	}

	/**
	 * @param buff is the target for the axioms rendered in DL syntax
	 * @param msg is insert before and after the axioms to detach it from its background (use "" if you don't know what to do with that).
	 * @return the given buffer
	 * @since 2.5.1
	 */
	default StringBuffer ontologyToString(final StringBuffer buff, final String msg)
	{
		final DLSyntaxObjectRenderer syntax = new DLSyntaxObjectRenderer();
		buff.append("====\\/==" + msg + "===\\/====\n");
		getOntology().axioms().map(syntax::render).filter(x -> x != null && x.length() > 0).sorted().map(x -> x + "\n").forEach(buff::append);
		buff.append("====/\\==" + msg + "===/\\====\n");
		return buff;
	}

	/**
	 * @return the axioms rendered in DL syntax
	 * @param msg is insert before and after the axioms to detach it from its background (use "" if you don't know what to do with that).
	 * @since 2.5.1
	 */
	default String ontologyToString(final String msg)
	{
		final StringBuffer buff = new StringBuffer();
		ontologyToString(buff, msg);
		return buff.toString();
	}

	default void ontologyToFile(final String filename)
	{
		try (OutputStream outputStream = new FileOutputStream(filename))
		{
			getManager().saveOntology(getOntology(), outputStream);
		}
		catch (final Exception e)
		{
			e.printStackTrace();
		}
	}

	default void ontologyToFormatFile(final OWLDocumentFormat format, final String filename)
	{
		final var previousFormat = getManager().getOntologyFormat(getOntology());
		getManager().setOntologyFormat(getOntology(), format);
		ontologyToFile(filename);
		getManager().setOntologyFormat(getOntology(), previousFormat);
	}

	default void ontologyToRdfXmlFile(final String filename)
	{
		ontologyToFormatFile(new RDFXMLDocumentFormatFactory().get(), filename);
	}

	/**
	 * Dispose the reasoner attached to this helper. If the reasoner wasn't attached, it doesn't buildit.
	 *
	 * @since 2.6.3
	 */
	void dispose();

	/**
	 * When you have finish use this Helper, you must call {dispose() and eventually getGroup().close()}
	 *
	 * @param ontology an already build ontology.
	 * @return an helper
	 * @since 2.6.3
	 */
	@SuppressWarnings("resource")
	static OWLHelper createLightHelper(final OWLOntology ontology)
	{
		return new OWLGenericTools(new OWLManagerGroup(ontology), ontology, true)
		{
			@Override
			public void dispose()
			{
				super.dispose();
				try
				{
					getGroup().close();
				}
				catch (final Exception exception)
				{
					throw new OpenError(exception);
				}
			}
		};
	}

	/**
	 * When you have finish use this Helper, you must call {dispose() and eventually getGroup().close()}
	 *
	 * @param inputStream ontology an already build ontology.
	 * @return an helper
	 * @since 2.6.3
	 */
	static OWLHelper createLightHelper(final InputStream inputStream)
	{
		try
		{
			return createLightHelper(OWLManager.createOWLOntologyManager().loadOntologyFromOntologyDocument(inputStream));
		}
		catch (final OWLOntologyCreationException exception)
		{
			throw new OpenError(exception);
		}
	}

	/**
	 * When you have finish use this Helper, you must call {dispose() and eventually getGroup().close()}
	 *
	 * @param reasoner backed on an ontology already build.
	 * @return an helper
	 * @since 2.6.3
	 */
	static OWLHelper createLightHelper(final OpenlletReasoner reasoner)
	{
		return new OWLHelper()
		{
			private final OWLGroup _group = new OWLManagerGroup(Optional.of(getManager()), Optional.empty());

			@Override
			public Logger getLogger()
			{
				return PelletReasoner._logger;
			}

			@Override
			public OWLDataFactory getFactory()
			{
				return reasoner.getFactory();
			}

			@Override
			public OWLOntologyManager getManager()
			{
				return reasoner.getManager();
			}

			@Override
			public OWLGroup getGroup()
			{
				return _group; // This is leaking resources as well.
			}

			@Override
			public OWLOntology getOntology()
			{
				return reasoner.getOntology();
			}

			@Override
			public OpenlletReasoner getReasoner()
			{
				reasoner.flush();
				return reasoner;
			}

			@Override
			public boolean isVolatile()
			{
				return true;
			}

			@Override
			public void dispose()
			{
				reasoner.dispose();
				if (null != _group)
					try
					{
						_group.close();
					}
					catch (final Exception exception)
					{
						throw new OpenError(exception);
					}
			}
		};
	}
}
