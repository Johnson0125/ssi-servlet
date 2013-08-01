package com.github.ssi_servlet;

import java.io.IOException;
import java.util.Locale;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.catalina.Globals;
import org.apache.catalina.ssi.SSIServlet_JBossWeb;

import com.googlecode.htmlcompressor.compressor.HtmlCompressor;

public class SsiServlet extends SSIServlet_JBossWeb {

	public static final String INIT_PARAM_HTML_COMPRESSOR = "com.github.ssi_servlet.HTML_COMPRESSOR";
	public static final String INIT_PARAM_COMPRESS_CSS = "com.github.ssi_servlet.COMPRESS_CSS";
	public static final String INIT_PARAM_COMPRESS_JS = "com.github.ssi_servlet.COMPRESS_JAVASCRIPT";

	public static final String REQ_ATTR_INITIAL_REQUEST_STRING = "com.github.ssi_servlet.INITIAL_REQUEST_STRING";
	public static final String REQ_ATTR_HTML_LANG = "HTML_LANG";
	public static final String REQ_ATTR_HTML_DIR = "HTML_DIR";

	public static final String HTML_COMMENT_BYPASS_COMPRESSING_HTML = "<!-- BYPASS_COMPRESSING_HTML -->";
	public static final String HTML_COMMENT_BYPASS_COMPRESSING_CSS = "<!-- BYPASS_COMPRESSING_CSS -->";
	public static final String HTML_COMMENT_BYPASS_COMPRESSING_JAVASCRIPT = "<!-- BYPASS_COMPRESSING_JAVASCRIPT -->";

	private static final long serialVersionUID = 7557205809972862863L;

	private boolean htmlCompressor = false;
	private boolean compressCSS = false;
	private boolean compressJS = false;

	@Override
	public void init() throws ServletException {
		super.init();
		htmlCompressor = Boolean
				.parseBoolean(getInitParameter(INIT_PARAM_HTML_COMPRESSOR));
		compressCSS = Boolean
				.parseBoolean(getInitParameter(INIT_PARAM_COMPRESS_CSS));
		compressJS = Boolean
				.parseBoolean(getInitParameter(INIT_PARAM_COMPRESS_JS));

		if (htmlCompressor) {
			// For the HTML compressor, force buffered to be true
			buffered = true;
		}

		if (debug > 0) {
			log("SsiRestServlet.init() the HTML Compressor feature: "
					+ ((htmlCompressor) ? "Has been enabled"
							: "Was not enabled"));

			if (htmlCompressor) {
				log("SsiRestServlet.init() the CSS compressing with the HTML Compressor: "
						+ ((compressCSS) ? "Has been enabled"
								: "Was not enabled"));
				log("SsiRestServlet.init() the JavaScript compressing with the HTML Compressor: "
						+ ((compressJS) ? "Has been enabled"
								: "Was not enabled"));
			}
		}
	}

	@Override
	protected String processBuffered(HttpServletRequest req, String text) {
		String html = super.processBuffered(req, text);
		Object initialReqStr = req
				.getAttribute(REQ_ATTR_INITIAL_REQUEST_STRING);

		if (!req.toString().equals(initialReqStr)) {
			return html;
		}

		req.removeAttribute(REQ_ATTR_INITIAL_REQUEST_STRING);
		String preCompHtml = processBufferedBeforeCompress(req, html);

		if (!htmlCompressor) {
			return preCompHtml;
		}

		int bypassHTML = preCompHtml
				.indexOf(HTML_COMMENT_BYPASS_COMPRESSING_HTML);

		if (bypassHTML > -1) {
			return preCompHtml;
		}

		return compressHtml(req, preCompHtml);
	}

	@Override
	protected void requestHandler(HttpServletRequest req,
			HttpServletResponse res) throws IOException {
		boolean alreadyInProcessSSI = false;
		Object ssi_flag = req.getAttribute(Globals.SSI_FLAG_ATTR);

		if (ssi_flag != null) {
			alreadyInProcessSSI = Boolean.parseBoolean(ssi_flag.toString());
		}

		if (!alreadyInProcessSSI) {
			requestHandlerInProcessSSI(req, res);
		}

		super.requestHandler(req, res);
	}

	protected String processBufferedBeforeCompress(HttpServletRequest req,
			String html) {
		return html;
	}

	protected Locale getUserLocale(HttpServletRequest req) {
		return req.getLocale();
	}

	protected void requestHandlerInProcessSSI(HttpServletRequest req,
			HttpServletResponse res) throws IOException {
		req.setAttribute(REQ_ATTR_INITIAL_REQUEST_STRING, req.toString());
		Locale userLocale = getUserLocale(req);

		if (userLocale == null) {
			userLocale = SsiServletUtils.DEFAULT_LOCALE;
		}

		req.setAttribute(REQ_ATTR_HTML_LANG,
				SsiServletUtils.getCultureHTMLLangAttributeValue(userLocale));
		req.setAttribute(REQ_ATTR_HTML_DIR,
				SsiServletUtils.getLanguageHTMLDirAttributeValue(userLocale));
	}

	protected String compressHtml(final HttpServletRequest req,
			final String html) {
		if (debug > 0) {
			log("HTML compressing the resource '" + req.getServletPath() + "'");
		}

		HtmlCompressor compressor = new HtmlCompressor();
		int bypassCSS = html.indexOf(HTML_COMMENT_BYPASS_COMPRESSING_CSS);

		if (compressCSS && (bypassCSS == -1)) {
			if (debug > 0) {
				log("CSS minification for resource '" + req.getServletPath()
						+ "'");
			}

			compressor.setCompressCss(true);
			// --line-break param for Yahoo YUI Compressor
			compressor.setYuiCssLineBreak(-1);
		}

		int bypassJS = html.indexOf(HTML_COMMENT_BYPASS_COMPRESSING_JAVASCRIPT);

		if (compressJS && (bypassJS == -1)) {
			if (debug > 0) {
				log("JavaScript minification for resource '"
						+ req.getServletPath() + "'");
			}

			compressor.setCompressJavaScript(true);
			// --disable-optimizations param for Yahoo YUI Compressor
			compressor.setYuiJsDisableOptimizations(true);
			// --line-break param for Yahoo YUI Compressor
			compressor.setYuiJsLineBreak(-1);
			// --nomunge param for Yahoo YUI Compressor
			compressor.setYuiJsNoMunge(true);
			// --preserve-semi param for Yahoo YUI Compressor
			compressor.setYuiJsPreserveAllSemiColons(true);
		}

		long beginTime = System.currentTimeMillis();
		int sizeBefore = html.length();
		String compHtml = compressor.compress(html);

		if (debug > 0) {
			log("Compression statics for resource '" + req.getServletPath()
					+ "' - size before: " + sizeBefore + ", size after: "
					+ compHtml.length() + ", and total time: "
					+ (System.currentTimeMillis() - beginTime) + "(ms)");
		}

		return compHtml;
	}

}
