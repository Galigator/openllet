// Copyright (c) 2006 - 2008, Clark & Parsia, LLC. <http://www.clarkparsia.com>
// This source code is available under the terms of the Affero General Public
// License v3.
//
// Please see LICENSE.txt for full license terms, including the availability of
// proprietary exceptions.
// Questions, comments, or requests for clarification: licensing@clarkparsia.com

package openllet;

import static openllet.OpenlletCmdOptionArg.NONE;
import static openllet.OpenlletCmdOptionArg.REQUIRED;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.StringWriter;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.StringTokenizer;
import java.util.logging.Logger;

import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;
import org.apache.jena.rdf.model.RDFReaderF;
import org.apache.jena.shared.NoReaderForLangException;

import openllet.core.KBLoader;
import openllet.core.KRSSLoader;
import openllet.core.KnowledgeBase;
import openllet.core.OpenlletOptions;
import openllet.core.utils.Timer;
import openllet.core.utils.Timers;
import openllet.jena.JenaLoader;
import openllet.owlapi.OWLAPILoader;
import openllet.shared.tools.Log;
import openllet.shared.tools.Logging;

/**
 * <p>
 * Description: Provides some functionality for Openllet command line applications
 * </p>
 * <p>
 * Copyright: Copyright (c) 2008
 * </p>
 * <p>
 * Company: Clark & Parsia, LLC. <http://www.clarkparsia.com>
 * </p>
 *
 * @author Markus Stocker
 * @author Evren Sirin
 */
public abstract class OpenlletCmdApp implements Logging
{
	public static final Logger _logger = Log.getLogger(OpenlletCmdApp.class);
	private final static String LINE_BREAK = System.getProperty("line.separator");
	private final static RDFReaderF READER_FACTORY = ModelFactory.createDefaultModel();

	protected String _appId;
	protected String _appCmd;
	protected String _help;
	protected OpenlletCmdOptions _options;
	private final List<String> _inputFiles;
	protected KBLoader _loader;
	protected boolean _verbose;
	protected Timers _timers;
	protected List<String> _tasks;

	public OpenlletCmdApp()
	{
		_options = getOptions();
		_appId = getAppId();
		_appCmd = getAppCmd();
		_inputFiles = new ArrayList<>();
		_timers = new Timers();

		buildHelp();
	}

	@Override
	public Logger getLogger()
	{
		return _logger;
	}

	public boolean requiresInputFiles()
	{
		return true;
	}

	protected void verbose(final String msg)
	{
		if (_verbose)
			System.err.println(msg);
	}

	protected void output(final String msg)
	{
		System.out.println(msg);
	}

	protected void output(final Model model)
	{
		model.write(System.out);
	}

	public abstract String getAppId();

	public abstract String getAppCmd();

	public abstract OpenlletCmdOptions getOptions();

	public abstract void run();

	public void finish()
	{
		if (_verbose)
		{
			final StringWriter sw = new StringWriter();
			_timers.print(sw, true, null);

			verbose("");
			verbose("Timer summary:");
			verbose(sw.toString());
		}
	}

	protected String getMandatoryOptions()
	{
		final StringBuffer ret = new StringBuffer();
		final Set<OpenlletCmdOption> mandatory = _options.getMandatoryOptions();

		for (final OpenlletCmdOption option : mandatory)
			ret.append("-" + option.getShortOption() + " arg ");

		return ret.toString();
	}

	public OpenlletCmdOption getIgnoreImportsOption()
	{
		final OpenlletCmdOption option = new OpenlletCmdOption("ignore-imports");
		//option.setShortOption("I");
		option.setDescription("Ignore imported ontologies");
		option.setDefaultValue(false);
		option.setIsMandatory(false);
		option.setArg(NONE);

		return option;
	}

	public OpenlletCmdOption getLoaderOption()
	{
		final OpenlletCmdOption option = new OpenlletCmdOption("loader");
		option.setShortOption("l");
		option.setDescription("Use Jena, OWLAPI, OWLAPI or KRSS to load the ontology");
		option.setType("Jena | OWLAPI | KRSS");
		option.setDefaultValue("OWLAPI");
		option.setIsMandatory(false);
		option.setArg(REQUIRED);

		return option;
	}

	public OpenlletCmdOptions getGlobalOptions()
	{
		final OpenlletCmdOptions options = new OpenlletCmdOptions();

		final OpenlletCmdOption helpOption = new OpenlletCmdOption("help");
		helpOption.setShortOption("h");
		helpOption.setDescription("Print this message");
		helpOption.setDefaultValue(false);
		helpOption.setIsMandatory(false);
		helpOption.setArg(NONE);
		options.add(helpOption);

		final OpenlletCmdOption verboseOption = new OpenlletCmdOption("verbose");
		verboseOption.setShortOption("v");
		verboseOption.setDescription("Print full stack trace for errors.");
		verboseOption.setDefaultValue(false);
		verboseOption.setIsMandatory(false);
		verboseOption.setArg(NONE);
		options.add(verboseOption);

		final OpenlletCmdOption configOption = new OpenlletCmdOption("config");
		configOption.setShortOption("C");
		configOption.setDescription("Use the selected configuration file");
		configOption.setIsMandatory(false);
		configOption.setType("configuration file");
		configOption.setArg(REQUIRED);
		options.add(configOption);

		return options;
	}

	public OpenlletCmdOption getInputFormatOption()
	{
		final OpenlletCmdOption option = new OpenlletCmdOption("input-format");
		option.setDefaultValue(null);
		option.setDescription("Format of the input file (valid only for the " + "Jena _loader). Default behaviour is to guess " + "the input format based on the file extension.");
		option.setType("RDF/XML | Turtle | N-Triples");
		option.setIsMandatory(false);
		option.setArg(REQUIRED);

		return option;
	}

	protected KnowledgeBase getKB()
	{
		return getKB(getLoader());
	}

	protected KnowledgeBase getKB(final KBLoader loader)
	{
		try
		{
			final String[] inputFiles = getInputFiles();

			verbose("There are " + inputFiles.length + " input files:");
			for (final String inputFile : inputFiles)
				verbose(inputFile);

			startTask("loading");
			final KnowledgeBase kb = loader.createKB(inputFiles);
			finishTask("loading");

			if (_verbose)
			{
				final StringBuilder sb = new StringBuilder();
				sb.append("Classes = " + kb.getAllClasses().size() + ", ");
				sb.append("Properties = " + kb.getProperties().size() + ", ");
				sb.append("Individuals = " + kb.getIndividuals().size());
				verbose("Input size: " + sb);

				verbose("Expressivity: " + kb.getExpressivity());
			}

			return kb;
		}
		catch (final RuntimeException e)
		{
			throw new OpenlletCmdException(e);
		}
	}

	protected KBLoader getLoader()
	{
		if (_loader != null)
			return _loader;

		final String loaderName = _options.getOption("loader").getValueAsString();

		return getLoader(loaderName);
	}

	protected KBLoader getLoader(final String loaderName)
	{
		if (loaderName.equalsIgnoreCase("Jena"))
			_loader = new JenaLoader();
		else
			if (loaderName.equalsIgnoreCase("OWLAPI"))
				_loader = new OWLAPILoader();
			else
				if (loaderName.equalsIgnoreCase("KRSS"))
					_loader = new KRSSLoader();
				else
					throw new OpenlletCmdException("Unknown _loader: " + loaderName);

		_loader.setIgnoreImports(_options.getOption("ignore-imports").getValueAsBoolean());
		final OpenlletCmdOption option = _options.getOption("input-format");
		if (option != null && option.getValueAsString() != null)
			if (_loader instanceof JenaLoader)
			{
				final String inputFormat = option.getValueAsString().toUpperCase();

				try
				{
					if (inputFormat != null)
					{
						READER_FACTORY.getReader(inputFormat.toUpperCase());

						((JenaLoader) _loader).setInputFormat(inputFormat);
					}
				}
				catch (final NoReaderForLangException e)
				{
					throw new OpenlletCmdException("Unrecognized input format: " + inputFormat, e);
				}
			}
			else
			{
				// silently ignore
			}

		return _loader;
	}

	protected String[] getInputFiles()
	{
		return _inputFiles.toArray(new String[] {});
	}

	private void buildHelp()
	{
		final StringBuffer u = new StringBuffer();

		final HelpTable table = new HelpTable(_options);

		u.append(_appId + LINE_BREAK + LINE_BREAK);
		u.append("Usage: " + _appCmd + LINE_BREAK + LINE_BREAK);
		u.append(table.print() + LINE_BREAK);

		_help = u.toString();
	}

	public void parseArgs(final String[] args)
	{
		final HashSet<String> seenOptions = new HashSet<>();

		// skip first arg which is the name of the subcommand
		int i = 1;
		for (; i < args.length; i++)
		{
			String arg = args[i];

			if ("--".equals(arg))
				return;

			if (arg.charAt(0) == '-')
			{
				if (arg.charAt(1) == '-')
					arg = arg.substring(2);
				else
					arg = arg.substring(1);
			}
			else
				// no more options to parse
				break;

			final OpenlletCmdOption option = _options.getOption(arg);

			if (option == null)
				throw new OpenlletCmdException("Unrecognized option: " + arg);
			else
				if (option.getLongOption().equals("help"))
					help();
				else
					if (option.getLongOption().equals("verbose"))
						Openllet.exceptionFormatter.setVerbose(true);

			if (seenOptions.contains(option.getLongOption()))
				throw new OpenlletCmdException("Repeated use of option: " + arg);

			seenOptions.add(option.getLongOption());

			final OpenlletCmdOptionArg optionArg = option.getArg();
			final boolean nextIsArg = args.length > i + 1 && args[i + 1].charAt(0) != '-';
			switch (optionArg)
			{
				case NONE:
					option.setValue(true);
					break;
				case REQUIRED:
					if (!nextIsArg)
						throw new OpenlletCmdException("Option <" + option.getLongOption() + "> requires an argument");
					else
						option.setValue(args[++i]);
					break;
				case OPTIONAL:
					if (nextIsArg)
						option.setValue(args[++i]);
					else
						option.setExists(true);
					break;

				default:
					throw new OpenlletCmdException("Unrecognized option argument: " + optionArg);
			}
		}

		// Check if all mandatory options are set
		for (final OpenlletCmdOption option : _options.getOptions())
			if (option.isMandatory())
				if (option.getValue() == null)
					throw new OpenlletCmdException("Option <" + option.getLongOption() + "> is mandatory");

		loadConfig();

		// Input files are given as a list of file URIs at the end
		for (; i < args.length; i++)
			_inputFiles.add(args[i]);

		if (_options.getOption("verbose").getValueAsBoolean())
			_verbose = true;

		if (requiresInputFiles())
		{
			if (_inputFiles.isEmpty())
				throw new OpenlletCmdException("No input file given");
		}
		else
			if (!_inputFiles.isEmpty())
				throw new OpenlletCmdException("Unexpected argument(s): " + _inputFiles);
	}

	private void loadConfig()
	{
		final String configFile = _options.getOption("config").getValueAsString();

		if (configFile != null)
			try
			{
				final URL url = new URL("file:" + configFile);

				OpenlletOptions.load(url);
			}
			catch (final MalformedURLException e)
			{
				throw new OpenlletCmdException("Invalid URL given for the config file: " + configFile, e);
			}
			catch (final FileNotFoundException e)
			{
				throw new OpenlletCmdException("The specified configuration file cannot be found: " + configFile, e);
			}
			catch (final IOException e)
			{
				throw new OpenlletCmdException("I/O error while reading the configuration file : " + configFile, e);
			}
	}

	public void help()
	{
		output(_help);
		System.exit(0);
	}

	private static class HelpTable
	{
		private final OpenlletCmdOptions _helpOptions;
		private final int _maxLineWidth = 80;
		private final int _indent = 5;

		public HelpTable(final OpenlletCmdOptions options)
		{
			_helpOptions = options;
		}

		public String print()
		{
			final StringBuffer ret = new StringBuffer();

			ret.append("Argument description:" + LINE_BREAK + LINE_BREAK);

			int i = 0;
			boolean last = false;

			for (final OpenlletCmdOption option : _helpOptions.getOptions())
			{
				i++;

				if (i == _helpOptions.getOptions().size())
					last = true;

				final String longOption = option.getLongOption();
				final String shortOption = option.getShortOption();
				final String type = option.getType();
				final OpenlletCmdOptionArg arg = option.getArg();
				final String description = option.getDescription();

				String defaultValue = "";

				if (option.getDefaultValue() != null)
					defaultValue = option.getDefaultValue().toString();

				final String firstLine = firstLine(shortOption, longOption, type, arg);
				final String secondLine = secondLine(description, defaultValue);

				ret.append(firstLine);
				ret.append(LINE_BREAK);
				ret.append(secondLine);

				if (!last)
					ret.append(LINE_BREAK + LINE_BREAK);
			}

			return ret.toString();
		}

		private static String fill(final int n)
		{
			return draw(" ", n);
		}

		private static String draw(final String c, final int n)
		{
			final StringBuffer ret = new StringBuffer();

			for (int i = 0; i < n; i++)
				ret.append(c);

			return ret.toString();
		}

		private static String firstLine(final String shortOption, final String longOption, final String type, final OpenlletCmdOptionArg arg)
		{
			final StringBuffer ret = new StringBuffer();

			ret.append("--" + longOption);

			if (shortOption != null)
				ret.append(", -" + shortOption);

			ret.append(" ");

			if (type != null)
				if (arg.equals(OpenlletCmdOptionArg.OPTIONAL) && !(type.startsWith("[") || type.startsWith("(")))
					ret.append("[" + type + "] ");
				else
					if (arg.equals(OpenlletCmdOptionArg.REQUIRED) && !(type.startsWith("[") || type.startsWith("(")))
						ret.append("(" + type + ") ");

			return ret.toString();
		}

		private String secondLine(final String description, final String defaultValue)
		{
			final int colStart = _indent;
			int colLength = colStart;

			final StringBuffer ret = new StringBuffer();

			if (description == null && defaultValue == null)
				return ret.toString();

			String tokens;

			if (defaultValue != null && defaultValue.length() != 0 && !(defaultValue.equals("true") || defaultValue.equals("false")))
				tokens = description + " (Default: " + defaultValue + ")";
			else
				tokens = description;

			ret.append(fill(colStart));

			final StringTokenizer tokenizer = new StringTokenizer(tokens);

			while (tokenizer.hasMoreTokens())
			{
				final String token = tokenizer.nextToken();

				colLength = colLength + token.length() + 1;

				if (colLength > _maxLineWidth)
				{
					ret.append(LINE_BREAK + fill(colStart));
					colLength = colStart + token.length() + 1;
				}

				ret.append(token + " ");
			}

			return ret.toString();
		}
	}

	protected void startTask(final String task)
	{
		verbose("Start " + task);
		_timers.startTimer(task);
	}

	protected void finishTask(final String task)
	{
		final Optional<Timer> timer = _timers.getTimer(task);
		timer.ifPresent(t ->
		{
			t.stop();
			verbose("Finished " + task + " in " + t.format());
		});
	}
}
