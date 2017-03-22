package openllet.owlapi;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintStream;
import java.io.Reader;
import java.util.ArrayList;
import java.util.Base64;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
import java.util.logging.Level;
import java.util.logging.Logger;
import openllet.atom.OpenError;
import openllet.core.utils.SetUtils;
import openllet.owlapi.parser.OWLFunctionalSyntaxParser;
import openllet.shared.tools.Log;
import org.semanticweb.owlapi.io.ToStringRenderer;
import org.semanticweb.owlapi.model.AddAxiom;
import org.semanticweb.owlapi.model.AddImport;
import org.semanticweb.owlapi.model.AddOntologyAnnotation;
import org.semanticweb.owlapi.model.AnnotationChange;
import org.semanticweb.owlapi.model.IRI;
import org.semanticweb.owlapi.model.ImportChange;
import org.semanticweb.owlapi.model.OWLAnnotation;
import org.semanticweb.owlapi.model.OWLAnnotationAxiom;
import org.semanticweb.owlapi.model.OWLAxiom;
import org.semanticweb.owlapi.model.OWLAxiomChange;
import org.semanticweb.owlapi.model.OWLOntology;
import org.semanticweb.owlapi.model.OWLOntologyChange;
import org.semanticweb.owlapi.model.OWLOntologyChangeListener;
import org.semanticweb.owlapi.model.OWLOntologyCreationException;
import org.semanticweb.owlapi.model.OWLOntologyID;
import org.semanticweb.owlapi.model.OWLOntologyLoaderConfiguration;
import org.semanticweb.owlapi.model.OWLOntologyManager;
import org.semanticweb.owlapi.model.RemoveAxiom;
import org.semanticweb.owlapi.model.RemoveImport;
import org.semanticweb.owlapi.model.RemoveOntologyAnnotation;
import org.semanticweb.owlapi.model.SetOntologyID;
import org.semanticweb.owlapi.util.SimpleRenderer;
import uk.ac.manchester.cs.owl.owlapi.OWLImportsDeclarationImpl;

/**
 * A listner that enable incremental storage of ontologies.
 *
 * @since 2.5.1
 */
public class OWLIncrementalFlatFileStorageManagerListener implements OWLOntologyChangeListener
{
	private static final Logger _logger = Log.getLogger(OWLIncrementalFlatFileStorageManagerListener.class);

	public static final int _flushTimeInMinute = 1;
	public static final byte[] _lineSeparator = "\n".getBytes();

	private final File _delta;
	private final File _directory;
	private final ScheduledThreadPoolExecutor _timer = new ScheduledThreadPoolExecutor(1);
	private final Set<OWLOntologyID> _changed = SetUtils.create();
	private final Map<OWLOntologyID, OWLFunctionalSyntaxParser> _parsers = new ConcurrentHashMap<>();
	private final OWLManagerGroup _owlManagerGroup;
	private final Lock _sequential = new ReentrantLock();
	private volatile Optional<OutputStream> _deltaStream = Optional.empty();

	private final Runnable _task = () ->
	{
		try
		{
			flush();
		}
		catch (final Exception e)
		{
			Log.error(_logger, e);
		}
	};

	public OWLIncrementalFlatFileStorageManagerListener(final File directory, final File log, final OWLManagerGroup owlManagerGroup) throws OWLOntologyCreationException
	{
		_delta = log;
		_directory = directory;
		_owlManagerGroup = owlManagerGroup;

		_owlManagerGroup.loadDirectory(_directory, _owlManagerGroup.getPersistentManager(), (m, f) ->
		{
			final List<OWLAxiom> axioms = new ArrayList<>();
			final OWLOntology ontology;

			try (final BufferedReader in = new BufferedReader(new FileReader(f)))
			{
				ontology = m.createOntology(parseOntologyId(in.readLine()));
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
				Log.error(_logger, "File " + f + " lead to corrupted ontology.", exception);
				return Optional.empty();
			}
			ontology.addAxioms(axioms);
			return Optional.of(ontology);
		});

		// Rebuild the ontology that still doesn't exists.
		rebuild();

		sync();
		// TODO : add a filter so after having sync/flush the ontologies, we can unload the ontologies we don't want to use ( just to free memory ).

		flush();

		// We flush the log file into ontologies file on fix schedule to avoid to much problem with the log file.
		_timer.scheduleAtFixedRate(_task, 0, _flushTimeInMinute, TimeUnit.MINUTES);
	}

	public String ontology2filename(final OWLOntology ontology)
	{
		return OWLHelper.ontology2filename(_directory, ontology);
	}

	/**
	 * Save the ontology with there current state, so the log can be empty at that moment.
	 *
	 * @throws OWLOntologyStorageException when something bad append when storing an ontology.
	 * @throws IOException when something bad append when storing an ontology.
	 * @throws FileNotFoundException when something bad append when storing an ontology.
	 */
	public void flush()
	{
		{
			final List<OWLOntologyID> changed;
			synchronized (_changed) // We don't took the synchronized over _changed directly to avoid a general stop of the application.
			{
				changed = new ArrayList<>(_changed);
				_changed.clear();
			}

			_owlManagerGroup.getPersistentManager().ontologies()//
					.parallel()// Yes we can !
					.filter(ontology -> ontology.getOntologyID().getOntologyIRI().isPresent())//
					.filter(ontology -> changed.contains(ontology.getOntologyID())) //
					.filter(ontology -> !ontology.isAnonymous())//
					// Don't filter ontology empty. There is no other way to mark an ontology as empty, so we save empty ontology.
					.forEach(ontology ->
					{
						try (final OutputStream stream = new FileOutputStream(ontology2filename(ontology)))
						{
							final PrintStream out = new PrintStream(stream);
							writeOntologyId(stream, ontology.getOntologyID());
							stream.write(_lineSeparator);
							ontology.axioms().map(OWLAxiom::toString).forEach(out::println);
						} // All exceptions must be fatal to avoid loosing 'log' file. Re-apply a log isn't an issue.
						catch (final Exception e)
						{ // Do not make other ontologies crash at save time.
							Log.error(_logger, "Crash when saving " + ontology.getOntologyID(), e);
						}
					});

			_logger.fine("flush done");

			// Make sure to note catch the saveOntology exception.
			// Make sure everything goes correctly before removing 'log' file.
			releaseDeltaStream();
		}
	}

	private void releaseDeltaStream()
	{
		try
		{
			_sequential.lock();
			_delta.delete();
			if (_deltaStream.isPresent())
			{
				_deltaStream.get().close();
				_deltaStream = Optional.empty();
			}
		}
		catch (final Exception e)
		{
			Log.error(_logger, e);
		}
		finally
		{
			_sequential.unlock();
		}
	}

	private static byte[] bytesOfOntologyId(final OWLOntologyID ontologyID)
	{
		if (ontologyID.getVersionIRI().isPresent())
			return (ontologyID.getOntologyIRI().get() + " " + ontologyID.getVersionIRI().get()).getBytes();
		else
			return ontologyID.getOntologyIRI().get().toString().getBytes();
	}

	private static void writeOntologyId(final OutputStream stream, final OWLOntologyID ontologyID) throws IOException
	{
		stream.write(bytesOfOntologyId(ontologyID));
	}

	private static OWLOntologyID parseOntologyId(final String ontologyId)
	{
		final String[] parts = ontologyId.split(" ");

		if (parts.length == 2)
			return new OWLOntologyID(IRI.create(parts[0]), IRI.create(parts[1]));
		else
			if (parts.length == 1)
				return new OWLOntologyID(IRI.create(ontologyId));
			else
			{
				_logger.log(Level.SEVERE, "Malformed OntologyID " + ontologyId);
				return null;
			}
	}

	private static byte[] bytesOfChange(final OWLOntologyChange change)
	{
		// There 4 basic cases, do them all. The other cases that does exists cover ontologies that already have a storage backend.
		if (change instanceof OWLAxiomChange)
		{
			ToStringRenderer.setRenderer(SimpleRenderer::new);
			// ToStringRenderer.getRendering(object)

			return ToStringRenderer.getInstance().render(change.getAxiom()).getBytes();
		}
		else
			if (change instanceof AnnotationChange)
				return ToStringRenderer.getInstance().render(((AnnotationChange) change).getAnnotation()).getBytes();
			else
				if (change instanceof ImportChange)
					return (((ImportChange) change).getImportDeclaration()).getIRI().toString().getBytes();
				else
					if (change instanceof SetOntologyID)
						return bytesOfOntologyId(((SetOntologyID) change).getNewOntologyID());
					else
					{
						_logger.severe("No bytes available for " + change);
						return null;
					}
	}

	@SuppressWarnings("resource") // resources are close when calling the 'close()' method, that why this class is auto-closable too
	@Override
	public void ontologiesChanged(final List<? extends OWLOntologyChange> changes)
	{
		final List<? extends OWLOntologyChange> copyOfChanges = new ArrayList<>(changes);
		{
			try
			{
				try
				{
					_sequential.lock();
					if (!_deltaStream.isPresent())
						_deltaStream = Optional.of(new FileOutputStream(_delta, true));
				}
				finally
				{
					_sequential.unlock();
				}

				final OutputStream stream = _deltaStream.orElseThrow(() -> new OpenError("Can't open write stream to " + _delta));

				for (final OWLOntologyChange change : copyOfChanges)
				{
					final byte[] bytes = bytesOfChange(change);
					if (bytes != null)
					{
						final OWLOntologyID ontologyId = change.getOntology().getOntologyID();

						stream.write(change.getClass().getSimpleName().getBytes());
						stream.write(_lineSeparator);

						writeOntologyId(stream, ontologyId);
						stream.write(_lineSeparator);

						stream.write(Base64.getEncoder().encode(bytes));
						stream.write(_lineSeparator);

						synchronized (_changed)
						{
							_changed.add(ontologyId);
						}
					}
				}
				stream.flush();
			}
			catch (final Exception e1)
			{
				Log.error(_logger, e1);
				releaseDeltaStream();
			}

		}
	}

	private class DeltaReader extends Reader implements Iterator<OWLOntologyChange>
	{
		private final OWLOntologyManager _manager = _owlManagerGroup.getPersistentManager();
		private final BufferedReader _in;

		private volatile char[] _data;
		private volatile int _localOffset = 0;
		private volatile int _line = 0;

		public DeltaReader(final BufferedReader in)
		{
			_in = in;
		}

		public OWLFunctionalSyntaxParser getParser(final OWLOntologyID ontId)
		{
			OWLFunctionalSyntaxParser parser = _parsers.get(ontId);
			if (parser == null)
			{
				parser = new OWLFunctionalSyntaxParser(this);
				OWLOntology ontology = _manager.getOntology(ontId);
				if (ontology == null)
					try
					{
						_logger.info("Creation of " + ontId.getOntologyIRI());
						ontology = new OWLGenericTools(_owlManagerGroup, ontId, false).getOntology();
					}
					catch (final OWLOntologyCreationException exception)
					{
						throw new OWLException("Ontology id lead to non existant ontology : " + ontId + ". And we can't create it.", exception);
					}

				parser.setUp(ontology, new OWLOntologyLoaderConfiguration());
				_parsers.put(ontId, parser);
			}
			return parser;
		}

		@Override
		public int read(final char[] cbuf, final int off, final int len) throws IOException
		{
			if (_data == null)
				return 0;

			int i = _localOffset;
			int j = off;
			int wrote = 0;
			for (; (j < cbuf.length) && wrote < len && i < _data.length; i++, j++, wrote++)
				cbuf[j] = _data[i];

			_localOffset = i;
			return wrote;
		}

		@Override
		public void close() throws IOException
		{
			// We don't need to close it. This is delegated to the one that manage the BufferedReader
		}

		@Override
		public boolean hasNext()
		{
			try
			{
				return _in.ready();
			}
			catch (final IOException e)
			{
				Log.error(_logger, e);
				return false;
			}
		}

		private OWLAxiom parseAxiom(final OWLOntologyID ontId, final String axiomStr)
		{
			_data = axiomStr.toCharArray();
			_localOffset = 0;

			try
			{
				return getParser(ontId).Axiom();
			}
			catch (final OWLException exception)
			{
				throw exception;
			}
			catch (final Exception exception)
			{
				throw new OWLException("Malformed File near " + (_line * 3) + " on " + axiomStr, exception);
			}
		}

		private OWLAnnotation getAnnotation(final OWLOntologyID ontId, final String data)
		{
			final OWLAxiom axiom = parseAxiom(ontId, data);
			if (axiom instanceof OWLAnnotationAxiom)
			{
				final OWLAnnotationAxiom aa = (OWLAnnotationAxiom) axiom;
				return aa.annotations().findAny().orElseThrow(() -> new OWLException("Invalid annotation axiom : " + data));
			}
			else
				throw new OWLException("Invalid annotation near " + (_line * 3) + "(line " + _line + ")" + " on axiom : " + data);
		}

		@Override
		public OWLOntologyChange next()
		{
			String kind;
			String ontologyId;
			String data;
			try
			{
				kind = _in.readLine();
				ontologyId = _in.readLine();
				data = _in.readLine();
			} // If the triple readLine fail we are in a malformed file.
			catch (final Exception e)
			{
				_logger.log(Level.SEVERE, "Malformed File near " + (_line * 3), e);
				return null;
			}

			data = new String(Base64.getDecoder().decode(data));

			final OWLOntologyID ontId = parseOntologyId(ontologyId);
			final OWLOntology ontology = _manager.getOntology(ontId);

			final OWLOntologyChange change;

			switch (kind)
			{
				case "SetOntologyID":
				{
					change = new SetOntologyID(ontology, parseOntologyId(data));
					break;
				}
				case "AddOntologyAnnotation":
				{
					change = new AddOntologyAnnotation(ontology, getAnnotation(ontId, data));
					break;
				}
				case "RemoveOntologyAnnotation":
				{
					change = new RemoveOntologyAnnotation(ontology, getAnnotation(ontId, data));
					break;
				}
				case "AddImport":
				{
					change = new AddImport(ontology, new OWLImportsDeclarationImpl(IRI.create(new String(data))));
					break;
				}
				case "RemoveImport":
				{
					change = new RemoveImport(ontology, new OWLImportsDeclarationImpl(IRI.create(new String(data))));
					break;
				}
				case "AddAxiom":
				{
					change = new AddAxiom(ontology, parseAxiom(ontId, data));
					break;
				}
				case "RemoveAxiom":
				{
					change = new RemoveAxiom(ontology, parseAxiom(ontId, data));
					break;
				}
				default:
				{
					throw new OWLException("Don't know what to do with change kind " + kind);
				}
			}

			_line++;
			return change;
		}
	}

	private class Builder
	{
		public Set<String> scan()
		{
			final Set<String> result = new HashSet<>();
			int line = 0;

			try (final BufferedReader in = new BufferedReader(new FileReader(_delta)))
			{
				while (in.ready())
					try
					{
						in.readLine(); // kind
						result.add(in.readLine()); // ontologyId
						in.readLine(); // data
						line++;
					} // If the triple readLine fail we are in a malformed file.
					catch (final Exception e)
					{
						Log.error(_logger, "Malformed File near " + (line * 3), e);
						return null;
					}
			}
			catch (final Exception e)
			{
				Log.error(_logger, e);
			}

			return result;
		}
	}

	private void rebuild() throws OWLOntologyCreationException
	{
		if (_delta.exists())
			for (final String sIri : (new Builder()).scan())
				if (sIri != null && !sIri.equals(""))
				{
					final String[] parts = sIri.split(" ");
					// Aim at reload ontology before access.
					final OWLOntologyID ontId;
					if (parts.length == 2)
						ontId = new OWLOntologyID(IRI.create(parts[0]), IRI.create(parts[1]));
					else
						ontId = new OWLOntologyID(IRI.create(sIri));

					// Build or load the ontology
					final OWLHelper tools = new OWLGenericTools(_owlManagerGroup, ontId, false); // Do not delete this line. It effectively load the ontology.
					_logger.info(tools.getOntology().getOntologyID() + " have been load.");

					// Add it to save list if just build.
					_changed.add(ontId);
				}
	}

	private void sync()
	{
		final List<OWLOntologyChange> changes = new ArrayList<>();

		if (_delta.exists())
			try (final BufferedReader br = new BufferedReader(new FileReader(_delta)))
			{
				try (final DeltaReader reader = new DeltaReader(br))
				{
					while (reader.hasNext())
						changes.add(reader.next());
				}
			}
			catch (final Exception e)
			{
				Log.error(_logger, e);
			}
	}

	/**
	 * Put an end to the storage manager.
	 *
	 * @since 2.5.1
	 */
	public void close()
	{
		try
		{
			flush();
		}
		catch (final Exception exception)
		{
			Log.error(_logger, exception);
		}
		try
		{
			_timer.remove(_task);
			_logger.log(Level.INFO, "The task that save ontologies have been removed.");
		}
		catch (final Exception exception)
		{
			Log.warning(_logger, exception);
		}
		try
		{
			_timer.purge();
		}
		catch (final Exception exception)
		{
			Log.warning(_logger, exception);
		}
	}
}
