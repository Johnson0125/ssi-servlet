package com.github.ssi_servlet.ant.taskdefs;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.Reader;
import java.io.UnsupportedEncodingException;
import java.util.Iterator;
import java.util.Vector;

import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.Project;
import org.apache.tools.ant.filters.util.ChainReaderHelper;
import org.apache.tools.ant.taskdefs.FixCRLF;
import org.apache.tools.ant.taskdefs.LogOutputStream;
import org.apache.tools.ant.taskdefs.MatchingTask;
import org.apache.tools.ant.types.FilterChain;
import org.apache.tools.ant.types.Resource;
import org.apache.tools.ant.types.ResourceCollection;
import org.apache.tools.ant.types.resources.FileResource;
import org.apache.tools.ant.types.resources.Resources;
import org.apache.tools.ant.types.resources.Restrict;
import org.apache.tools.ant.types.resources.selectors.Exists;
import org.apache.tools.ant.types.resources.selectors.Not;
import org.apache.tools.ant.types.resources.selectors.ResourceSelector;
import org.apache.tools.ant.util.FileUtils;
import org.apache.tools.ant.util.StringUtils;

import com.googlecode.htmlcompressor.compressor.YuiCssCompressor;
import com.googlecode.htmlcompressor.compressor.YuiJavaScriptCompressor;

public class ConcatMinimize extends MatchingTask {

	private static final int BUFFER_SIZE = 8192;
	private static final FileUtils FILE_UTILS = FileUtils.getFileUtils();
	private static final ResourceSelector EXISTS = new Exists();
	private static final ResourceSelector NOT_EXISTS = new Not(EXISTS);

	private static final String EOL_CR = "cr";
	private static final String EOL_MAC = "mac";
	private static final String EOL_LF = "lf";
	private static final String EOL_UNIX = "unix";
	private static final String EOL_CRLF = "crlf";
	private static final String EOL_DOS = "dos";
	private static final String CR = "\r";
	private static final String LF = "\n";
	private static final String CRLF = "\r\n";

	private static final String TYPE_CSS = ".css";
	private static final String TYPE_JS = ".js";
	private static final YuiCssCompressor cssCompressor = new YuiCssCompressor();
	private static final YuiJavaScriptCompressor jsCompressor = new YuiJavaScriptCompressor();

	private File destfile;
	private boolean append;
	private boolean overwrite = true;
	private String encoding;
	private String outputEncoding;
	private boolean fixLastLine = true;
	private String eolString;
	private ResourceCollection rc;
	private Vector<FilterChain> filterChains;
	private String typeCss = TYPE_CSS;
	private String typeJs = TYPE_JS;

	public ConcatMinimize() {
		reset();
	}

	public void setDestfile(File destfile) {
		this.destfile = destfile;
	}

	public void setAppend(boolean append) {
		this.append = append;
	}

	public void setOverwrite(boolean overwrite) {
		this.overwrite = overwrite;
	}

	public void setEncoding(String encoding) {
		this.encoding = encoding;

		if (outputEncoding == null) {
			outputEncoding = encoding;
		}
	}

	public void setOutputEncoding(String outputEncoding) {
		this.outputEncoding = outputEncoding;
	}

	public void setFixLastLine(boolean fixLastLine) {
		this.fixLastLine = fixLastLine;
	}

	public void setEol(FixCRLF.CrLf crlf) {
		String s = crlf.getValue();

		if (s.equalsIgnoreCase(EOL_CR) || s.equalsIgnoreCase(EOL_MAC)) {
			eolString = CR;
		} else if (s.equalsIgnoreCase(EOL_LF) || s.equalsIgnoreCase(EOL_UNIX)) {
			eolString = LF;
		} else if (s.equalsIgnoreCase(EOL_CRLF) || s.equalsIgnoreCase(EOL_DOS)) {
			eolString = CRLF;
		}
	}

	public void setTypeCss(String typeCss) {
		this.typeCss = typeCss;
	}

	public void setTypeJs(String typeJs) {
		this.typeJs = typeJs;
	}

	public synchronized void add(ResourceCollection c) {
		if (rc == null) {
			rc = c;
			return;
		}

		if (!(rc instanceof Resources)) {
			Resources newRc = new Resources();
			newRc.setProject(getProject());
			newRc.add(rc);
			rc = newRc;
		}

		((Resources) rc).add(c);
	}

	public void addFilterChain(FilterChain filterChain) {
		if (filterChains == null) {
			filterChains = new Vector<FilterChain>();
		}

		filterChains.addElement(filterChain);
	}

	public void reset() {
		destfile = null;
		append = false;
		overwrite = true;
		encoding = null;
		outputEncoding = null;
		fixLastLine = true;
		eolString = StringUtils.LINE_SEP;
		rc = null;
		filterChains = null;
		typeCss = TYPE_CSS;
		typeJs = TYPE_JS;
		cssCompressor.setLineBreak(-1);
		jsCompressor.setLineBreak(-1);
		jsCompressor.setNoMunge(true);
		jsCompressor.setPreserveAllSemiColons(true);
		jsCompressor.setDisableOptimizations(true);
	}

	@Override
	public void execute() throws BuildException {
		validateAttributes();
		ResourceCollection c = getResources();

		if (isUpToDate(c)) {
			log(destfile + " is up-to-date.", Project.MSG_VERBOSE);
			return;
		}

		if (c.size() == 0) {
			return;
		}

		OutputStream out;

		if (destfile == null) {
			// Log using WARN so it displays in 'quiet' mode.
			out = new LogOutputStream(this, Project.MSG_WARN);
		} else {
			try {
				// ensure that the parent dir of dest file exists
				File parent = destfile.getParentFile();

				if (!parent.exists()) {
					parent.mkdirs();
				}

				out = new FileOutputStream(destfile.getPath(), append);
			} catch (Throwable t) {
				throw new BuildException("Unable to open " + destfile
						+ " for writing", t);
			}
		}

		Iterator<?> rcIter = rc.iterator();

		while (rcIter.hasNext()) {
			Object inObj = rcIter.next();

			if (!(inObj instanceof Resource)) {
				continue;
			}

			Resource inRes = (Resource) inObj;

			if (!inRes.isExists()) {
				continue;
			}

			try {
				Reader inReader = getFilteredReader(new BufferedReader(
						encoding == null ? new InputStreamReader(
								inRes.getInputStream())
								: new InputStreamReader(inRes.getInputStream(),
										encoding)));
				StringBuffer inBuffer = new StringBuffer();
				char[] charBuffer = new char[BUFFER_SIZE];
				int len = inReader.read(charBuffer, 0, BUFFER_SIZE);

				while (len > -1) {
					if (len > 0) {
						for (int index = 0; index < len; index++) {
							inBuffer.append(charBuffer[index]);
						}
					}

					len = inReader.read(charBuffer, 0, BUFFER_SIZE);
				}

				FileUtils.close(inReader);
				String inString = fixInputLastLine(minimizeInput(
						inBuffer.toString(), inRes.getName()));
				out.write(inString.getBytes(), 0, inString.length());
			} catch (UnsupportedEncodingException e) {
			} catch (IOException e) {
			}
		}

		FileUtils.close(out);
	}

	protected void validateAttributes() throws BuildException {
		if ((rc == null) || (rc.size() == 0)) {
			throw new BuildException("At least one resource must be provided.");
		}
	}

	private String minimizeInput(String input, String resName) {
		if ((input == null) || (resName == null)
				|| (!resName.endsWith(typeCss) && !resName.endsWith(typeJs))) {
			return input;
		}

		boolean minimized = true;
		int crlfPos = input.indexOf(CRLF);
		int cnt = 0;

		while (minimized && (crlfPos > -1) && (crlfPos < input.length() - 1)) {
			cnt++;

			if (cnt > 3) {
				minimized = false;
				break;
			}

			crlfPos = input.indexOf(CRLF, crlfPos + 1);
		}

		if (minimized) {
			int crPos = input.indexOf(CR);
			cnt = 0;

			while (minimized && (crPos > -1) && (crPos < input.length() - 1)) {
				cnt++;

				if (cnt > 3) {
					minimized = false;
					break;
				}

				crPos = input.indexOf(CR, crPos + 1);
			}
		}

		if (minimized) {
			int lfPos = input.indexOf(LF);
			cnt = 0;

			while (minimized && (lfPos > -1) && (lfPos < input.length() - 1)) {
				cnt++;

				if (cnt > 3) {
					minimized = false;
					break;
				}

				lfPos = input.indexOf(LF, lfPos + 1);
			}
		}

		if (minimized) {
			return input;
		}

		if (resName.endsWith(typeCss)) {
			return cssCompressor.compress(input);
		}

		return jsCompressor.compress(input);
	}

	private String fixInputLastLine(String input) {
		if (!fixLastLine || (input == null)) {
			return input;
		}

		String inEOL = null;

		if (input.endsWith(CRLF)) {
			inEOL = CRLF;
		} else if (input.endsWith(CR)) {
			inEOL = CR;
		} else if (input.endsWith(LF)) {
			inEOL = LF;
		}

		if (inEOL == null) {
			return input + eolString;
		}

		if (inEOL.equals(eolString)) {
			return input;
		}

		if (inEOL.length() == 2) {
			if (input.length() <= 2) {
				return eolString;
			}

			return input.substring(0, input.length() - 2) + eolString;
		}

		if (input.length() <= 1) {
			return eolString;
		}

		return input.substring(0, input.length() - 1) + eolString;
	}

	private ResourceCollection getResources() {
		Restrict noexistRc = new Restrict();
		noexistRc.add(NOT_EXISTS);
		noexistRc.add(rc);

		for (Iterator<?> i = noexistRc.iterator(); i.hasNext();) {
			log(i.next() + " does not exist.", Project.MSG_ERR);
		}

		if (destfile != null) {
			for (Iterator<?> i = rc.iterator(); i.hasNext();) {
				Object o = i.next();

				if (o instanceof FileResource) {
					File f = ((FileResource) o).getFile();

					if (FILE_UTILS.fileNameEquals(f, destfile)) {
						throw new BuildException("Input file \"" + f
								+ "\" is the same as the output file.");
					}
				}
			}
		}

		Restrict result = new Restrict();
		result.add(EXISTS);
		result.add(rc);
		return result;
	}

	private boolean isUpToDate(ResourceCollection c) {
		if ((destfile == null) || overwrite) {
			return false;
		}

		for (Iterator<?> i = c.iterator(); i.hasNext();) {
			Resource r = (Resource) i.next();

			if ((r.getLastModified() == 0L)
					|| (r.getLastModified() > destfile.lastModified())) {
				return false;
			}
		}

		return true;
	}

	private Reader getFilteredReader(Reader r) {
		if (filterChains == null) {
			return r;
		}

		ChainReaderHelper helper = new ChainReaderHelper();
		helper.setBufferSize(BUFFER_SIZE);
		helper.setPrimaryReader(r);
		helper.setFilterChains(filterChains);
		helper.setProject(getProject());
		// used to be a BufferedReader here, but we should be buffering lower:
		return helper.getAssembledReader();
	}

}
