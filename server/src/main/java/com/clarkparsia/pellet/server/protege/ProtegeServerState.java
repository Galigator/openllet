package com.clarkparsia.pellet.server.protege;

import java.util.Collection;
import java.util.List;
import java.util.Stack;
import java.util.concurrent.atomic.AtomicReference;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.clarkparsia.pellet.server.model.OntologyState;
import com.clarkparsia.pellet.server.model.ServerState;
import com.clarkparsia.pellet.server.model.impl.OntologyStateImpl;
import com.clarkparsia.pellet.server.model.impl.ServerStateImpl;
import com.google.common.base.Throwables;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Lists;
import org.protege.owl.server.api.client.Client;
import org.protege.owl.server.api.client.RemoteOntologyDocument;
import org.protege.owl.server.api.client.RemoteServerDirectory;
import org.protege.owl.server.api.client.RemoteServerDocument;
import org.protege.owl.server.api.client.VersionedOntologyDocument;
import org.protege.owl.server.api.exception.OWLServerException;
import org.protege.owl.server.util.ClientUtilities;
import org.semanticweb.owlapi.apibinding.OWLManager;
import org.semanticweb.owlapi.model.IRI;
import org.semanticweb.owlapi.model.OWLOntology;
import org.semanticweb.owlapi.model.OWLOntologyCreationException;

/**
 * @author Edgar Rodriguez-Diaz
 */
public final class ProtegeServerState implements ServerState {

	private static final Logger LOGGER = Logger.getLogger(ProtegeServerState.class.getName());

	private final Client mClient;

	private final AtomicReference<ServerState> mServerState = new AtomicReference<ServerState>();

	private final IRI serverRoot;

	public ProtegeServerState(final Client theProtegeClient) {
		mClient = theProtegeClient;
		serverRoot = IRI.create(mClient.getScheme() + "://" + mClient.getAuthority());

		mServerState.set(snapshot());
	}

	private ServerState snapshot() {
		ImmutableSet.Builder<OntologyState> ontologies = ImmutableSet.builder();

		try {
			// scan the protege server to get all the ontologies.
			RemoteServerDocument rootDir = mClient.getServerDocument(serverRoot);
			Collection<RemoteOntologyDocument> docs = list(mClient, (RemoteServerDirectory) rootDir);

			for (RemoteOntologyDocument ontoDoc : docs) {
				try {
					VersionedOntologyDocument vont = ClientUtilities.loadOntology(mClient,
					                                                              OWLManager.createOWLOntologyManager(),
					                                                              ontoDoc);
					OWLOntology ontology = vont.getOntology();
					ontologies.add(new ProtegeOntologyState(ontoDoc.getServerLocation(),
					                                        new OntologyStateImpl(ontology)));
				}
				catch (OWLOntologyCreationException e) {
					LOGGER.log(Level.FINER, "Could not load one or more ontologies from Protege server", e);
				}
			}
		}
		catch (OWLServerException e) {
			LOGGER.log(Level.FINER, "Could not capture snapshot of ontologies from Protege server", e);
			Throwables.propagate(e);
		}

		return ServerStateImpl.create(ontologies.build());
	}

	public static Collection<RemoteOntologyDocument> list(final Client client,
	                                                      final RemoteServerDirectory theDir) throws OWLServerException {
		List<RemoteOntologyDocument> docs = Lists.newLinkedList();
		Stack<RemoteServerDirectory> dirsToProc = new Stack<RemoteServerDirectory>();
		dirsToProc.push(theDir);

		while (!dirsToProc.empty()) {
			for (final RemoteServerDocument doc : client.list(dirsToProc.pop())) {
				if (doc != null) {
					if (doc instanceof RemoteOntologyDocument) {
						docs.add((RemoteOntologyDocument) doc);
					}
					else {
						dirsToProc.push((RemoteServerDirectory) doc);
					}
				}
			}
		}

		return docs;
	}

	@Override
	public OntologyState getOntology(final IRI ontology) {
		return mServerState.get().getOntology(ontology);
	}

	@Override
	public void refresh() {
		// free resources from previous server state and update with new snapshot from server
		try {
			mServerState.getAndSet(snapshot())
			            .close();
		}
		catch (Exception e) {
			LOGGER.log(Level.SEVERE, "Could not refresh Server State from Protege", e);
		}
	}

	@Override
	public void close() throws Exception {
		// close current server state
		mServerState.get().close();

		mClient.shutdown();
	}
}