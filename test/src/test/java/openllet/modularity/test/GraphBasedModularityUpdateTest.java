package openllet.modularity.test;

import openllet.modularity.GraphBasedModuleExtractor;
import openllet.modularity.ModuleExtractor;

/**
 * @author Evren Sirin
 */
public class GraphBasedModularityUpdateTest extends ModularityUpdateTest
{
	/**
	 * {@inheritDoc}
	 */
	@Override
	public ModuleExtractor createModuleExtractor()
	{
		return new GraphBasedModuleExtractor();
	}
}
