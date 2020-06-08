// Copyright (c) 2006 - 2008, Clark & Parsia, LLC. <http://www.clarkparsia.com>
// This source code is available under the terms of the Affero General Public License v3.
//
// Please see LICENSE.txt for full license terms, including the availability of proprietary exceptions.
// Questions, comments, or requests for clarification: licensing@clarkparsia.com

package openllet.core.utils.progress;

/**
 * <p>
 * Copyright: Copyright (c) 2006
 * </p>
 * <p>
 * Company: Clark & Parsia, LLC. <http://www.clarkparsia.com>
 * </p>
 *
 * @author Evren Sirin
 */
public interface ProgressMonitor
{
	default int getEchoInterval()
	{
		return 10;
	}

	int getLastEcho();

	int getProgress();

	int getProgressPercent();

	void incrementProgress();

	boolean isCanceled();

	void setProgress(int value);

	void setProgressLength(int length);

	void setProgressMessage(String message);

	void setProgressTitle(String title);

	void taskFinished();

	void taskStarted();
}
