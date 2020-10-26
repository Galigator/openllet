// Copyright (c) 2006 - 2008, Clark & Parsia, LLC. <http://www.clarkparsia.com>
// This source code is available under the terms of the Affero General Public
// License v3.
//
// Please see LICENSE.txt for full license terms, including the availability of
// proprietary exceptions.
// Questions, comments, or requests for clarification: licensing@clarkparsia.com

package openllet;

import static openllet.OpenlletCmdOptionArg.REQUIRED;

import java.util.HashSet;
import java.util.Set;

import org.semanticweb.owlapi.formats.RDFXMLDocumentFormat;
import org.semanticweb.owlapi.io.SystemOutDocumentTarget;
import org.semanticweb.owlapi.model.OWLAxiom;
import org.semanticweb.owlapi.model.OWLEntity;
import org.semanticweb.owlapi.model.OWLException;
import org.semanticweb.owlapi.model.OWLOntology;

import openllet.atom.OpenError;
import openllet.modularity.ModularityUtils;
import openllet.owlapi.OWLAPILoader;
import openllet.owlapi.OntologyUtils;
import uk.ac.manchester.cs.owlapi.modularity.ModuleType;

/**
 * <p>
 * Copyright: Copyright (c) 2008
 * </p>
 * <p>
 * Company: Clark & Parsia, LLC. <http://www.clarkparsia.com>
 * </p>
 *
 * @author Markus Stocker
 */
public class OpenlletModularity extends OpenlletCmdApp
{

	private OWLAPILoader loader;
	private ModuleType moduleType;
	private String[] entityNames;

	public OpenlletModularity()
	{
	}

	@Override
	public String getAppCmd()
	{
		return "openllet modularity " + getMandatoryOptions() + "[options] <file URI>...";
	}

	@Override
	public String getAppId()
	{
		return "OpenlletModularity: Extract ontology modules for classes and write it to the STDOUT";
	}

	@Override
	public OpenlletCmdOptions getOptions()
	{
		final OpenlletCmdOptions options = getGlobalOptions();

		options.add(getIgnoreImportsOption());

		OpenlletCmdOption option = new OpenlletCmdOption("signature");
		option.setShortOption("s");
		option.setType("Space separated list");
		option.setDescription("One or more entity URI(s) or local name(s) to be extracted as a module. Example: \"Animal Wildlife Rainforest\"");
		option.setIsMandatory(true);
		option.setArg(REQUIRED);
		options.add(option);

		option = new OpenlletCmdOption("type");
		option.setShortOption("t");
		option.setType("lower| upper | upper-of-lower | lower-of-upper");
		option.setDefaultValue("lower");
		option.setDescription("The type of the module that will be extracted. See http://bit.ly/ontology-module-types for an explanation of the module types.");
		option.setIsMandatory(false);
		option.setArg(REQUIRED);
		options.add(option);

		return options;
	}

	@Override
	public void run()
	{
		loadEntityNames();
		loadModuleType();
		loadOntology();
		extractModule();
	}

	private void loadOntology()
	{
		loader = (OWLAPILoader) getLoader("OWLAPI");
		getKB();
	}

	private void loadEntityNames()
	{
		final String signature = _options.getOption("signature").getValueAsString();

		if (signature == null)
			throw new OpenlletCmdException("No signature provided");

		entityNames = signature.split(" ");

		if (entityNames.length == 0)
			throw new OpenlletCmdException("No signature provided");
	}

	private void loadModuleType()
	{
		final String type = _options.getOption("type").getValueAsString();

		if (type.equalsIgnoreCase("lower"))
			moduleType = ModuleType.TOP;
		else
			if (type.equalsIgnoreCase("upper"))
				moduleType = ModuleType.BOT;
			else
				if (type.equalsIgnoreCase("upper-of-lower"))
					moduleType = ModuleType.STAR;
				else
					if (type.equalsIgnoreCase("lower-of-upper"))
						moduleType = ModuleType.STAR;
					else
						throw new OpenlletCmdException("Unknown module type: " + type);
	}

	private void extractModule()
	{
		final Set<OWLEntity> entities = new HashSet<>();
		for (final String entityName : entityNames)
		{
			final OWLEntity entity = OntologyUtils.findEntity(entityName, loader.allOntologies());

			if (entity == null)
				throw new OpenlletCmdException("Entity not found in ontology: " + entityName);

			entities.add(entity);
		}

		final Set<OWLAxiom> module = ModularityUtils.extractModule(loader.getOntology(), entities, moduleType);

		try
		{
			final OWLOntology moduleOnt = loader.getManager().createOntology(module);
			loader.getManager().saveOntology(moduleOnt, new RDFXMLDocumentFormat(), new SystemOutDocumentTarget());
		}
		catch (final OWLException e)
		{
			throw new OpenError(e);
		}
	}
}
