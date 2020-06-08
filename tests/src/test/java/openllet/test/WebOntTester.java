// Copyright (c) 2006 - 2008, Clark & Parsia, LLC. <http://www.clarkparsia.com>
// This source code is available under the terms of the Affero General Public License v3.
//
// Please see LICENSE.txt for full license terms, including the availability of proprietary exceptions.
// Questions, comments, or requests for clarification: licensing@clarkparsia.com

package openllet.test;

public interface WebOntTester
{
	void setInputOntology(String inputFileURI);

	void setTimeout(long timeout);

	boolean isConsistent();

	void classify();

	void testEntailment(String entailmentFileURI, boolean positiveEntailment);

	void registerURIMapping(String fromURI, String toURI);
}
