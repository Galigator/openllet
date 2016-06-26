package openllet.modularity.test;

import openllet.modularity.GraphBasedModuleExtractor;
import openllet.modularity.ModuleExtractor;

/**
 * @author Mike Smith
 */
public class GraphBasedRandomizedModularityTest extends RandomizedModularityTest
{
	public GraphBasedRandomizedModularityTest()
	{
		super("test/data/modularity/");
	}

	@Override
	public ModuleExtractor createModuleExtractor()
	{
		return new GraphBasedModuleExtractor();
	}

}
