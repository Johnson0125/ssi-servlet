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
	public static final String INIT_PARAM_UPDATE_HTML_LANG_DIR_PER_REQUEST = "com.github.ssi_servlet.UPDATE_HTML_LANG_DIR_PER_REQUEST";

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
	private boolean updateHtmlLangDir = false;

	@Override
	public void init() throws ServletException {
		super.init();
		htmlCompressor = Boolean
				.parseBoolean(getInitParameter(INIT_PARAM_HTML_COMPRESSOR));
		compressCSS = Boolean
				.parseBoolean(getInitParameter(INIT_PARAM_COMPRESS_CSS));
		compressJS = Boolean
				.parseBoolean(getInitParameter(INIT_PARAM_COMPRESS_JS));
		updateHtmlLangDir = Boolean
				.parseBoolean(getInitParameter(INIT_PARAM_UPDATE_HTML_LANG_DIR_PER_REQUEST));

		if (htmlCompressor || updateHtmlLangDir) {
			// For the HTML compressor or update the HTML lang & dir per
			// request, force buffered to be true
			buffered = true;
		}

		if (debug > 0) {
			log("SsiServlet.init() the HTML Compressor feature: "
					+ ((htmlCompressor) ? "Has been enabled"
							: "Was not enabled"));

			if (htmlCompressor) {
				log("SsiServlet.init() the CSS compressing with the HTML Compressor: "
						+ ((compressCSS) ? "Has been enabled"
								: "Was not enabled"));
				log("SsiServlet.init() the JavaScript compressing with the HTML Compressor: "
						+ ((compressJS) ? "Has been enabled"
								: "Was not enabled"));
			}

			log("SsiServlet.init() the update HTML lang and dir per Request feature: "
					+ ((updateHtmlLangDir) ? "Has been enabled"
							: "Was not enabled"));
		}
	}

	@Override
	protected String processBuffered(final HttpServletRequest req,
			final String text) {
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
	protected void requestHandler(final HttpServletRequest req,
			final HttpServletResponse res) throws IOException {
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

	protected String processBufferedBeforeCompress(
			final HttpServletRequest req, final String html) {
		if (!updateHtmlLangDir) {
			return html;
		}

		Locale userLocale = getUserLocale(req);

		if (userLocale == null) {
			userLocale = SsiServletUtils.DEFAULT_LOCALE;
		}

		return updateHtmlLangDirPerRequest(req, html, userLocale);
	}

	protected Locale getUserLocale(final HttpServletRequest req) {
		return req.getLocale();
	}

	protected void requestHandlerInProcessSSI(final HttpServletRequest req,
			final HttpServletResponse res) throws IOException {
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

	protected String updateHtmlLangDirPerRequest(final HttpServletRequest req,
			final String html, final Locale userLocale) {
		if (html == null) {
			return null;
		}

		String htmlLang = SsiServletUtils
				.getCultureHTMLLangAttributeValue(userLocale);
		String htmlDir = SsiServletUtils
				.getLanguageHTMLDirAttributeValue(userLocale);

		if (debug > 0) {
			log("Updating the HTML for the request '" + req.getServletPath()
					+ "' ; lang=\"" + htmlLang + "\" ; dir=\"" + htmlDir + "\"");
		}

		StringBuilder updtHtml = new StringBuilder(html);
		boolean langAttrInserted = false;
		String langAttr = "\"" + htmlLang + "\"";
		boolean dirAttrInserted = false;
		String dirAttr = "\"" + htmlDir + "\"";
		int htmlTagPos = updtHtml.indexOf("<html");
		int htmlTagClosePos = -1;

		if (htmlTagPos == -1) {
			htmlTagPos = updtHtml.indexOf("<HTML");
		}

		if (htmlTagPos > -1) {
			htmlTagClosePos = updtHtml.indexOf(">", htmlTagPos);
		}

		if ((htmlTagPos > -1) && (htmlTagClosePos > htmlTagPos)) {
			int langAttrPos = updtHtml.indexOf(" lang=", htmlTagPos);

			if (langAttrPos == -1) {
				langAttrPos = updtHtml.indexOf(" LANG=", htmlTagPos);
			}

			if ((langAttrPos > -1) && (langAttrPos < htmlTagClosePos)) {
				int spacePos = updtHtml.indexOf(" ", langAttrPos + 6);

				if ((spacePos > -1) && (spacePos < htmlTagClosePos)) {
					updtHtml.replace(langAttrPos + 6, spacePos, langAttr);
				} else {
					updtHtml.replace(langAttrPos + 6, htmlTagClosePos, langAttr);
				}

				htmlTagClosePos = updtHtml.indexOf(">", htmlTagPos);
				langAttrInserted = true;
			}

			int dirAttrPos = updtHtml.indexOf(" dir=", htmlTagPos);

			if (dirAttrPos == -1) {
				dirAttrPos = updtHtml.indexOf(" DIR=", htmlTagPos);
			}

			if ((dirAttrPos > -1) && (dirAttrPos < htmlTagClosePos)) {
				int spacePos = updtHtml.indexOf(" ", dirAttrPos + 5);

				if ((spacePos > -1) && (spacePos < htmlTagClosePos)) {
					updtHtml.replace(dirAttrPos + 5, spacePos, dirAttr);
				} else {
					updtHtml.replace(dirAttrPos + 5, htmlTagClosePos, dirAttr);
				}

				htmlTagClosePos = updtHtml.indexOf(">", htmlTagPos);
				dirAttrInserted = true;
			}
		}

		int bodyTagPos = updtHtml.indexOf("<body");
		int bodyTagClosePos = -1;

		if (bodyTagPos == -1) {
			bodyTagPos = updtHtml.indexOf("<BODY");
		}

		if (bodyTagPos > -1) {
			bodyTagClosePos = updtHtml.indexOf(">", bodyTagPos);
		}

		if ((bodyTagPos > -1) && (bodyTagClosePos > bodyTagPos)) {
			if (!langAttrInserted) {
				int langAttrPos = updtHtml.indexOf(" lang=", bodyTagPos);

				if (langAttrPos == -1) {
					langAttrPos = updtHtml.indexOf(" LANG=", bodyTagPos);
				}

				if ((langAttrPos > -1) && (langAttrPos < bodyTagClosePos)) {
					int spacePos = updtHtml.indexOf(" ", langAttrPos + 6);

					if ((spacePos > -1) && (spacePos < bodyTagClosePos)) {
						updtHtml.replace(langAttrPos + 6, spacePos, langAttr);
					} else {
						updtHtml.replace(langAttrPos + 6, bodyTagClosePos,
								langAttr);
					}

					bodyTagClosePos = updtHtml.indexOf(">", bodyTagPos);
					langAttrInserted = true;
				}
			}

			if (!dirAttrInserted) {
				int dirAttrPos = updtHtml.indexOf(" dir=", bodyTagPos);

				if (dirAttrPos == -1) {
					dirAttrPos = updtHtml.indexOf(" DIR=", bodyTagPos);
				}

				if ((dirAttrPos > -1) && (dirAttrPos < bodyTagClosePos)) {
					int spacePos = updtHtml.indexOf(" ", dirAttrPos + 5);

					if ((spacePos > -1) && (spacePos < bodyTagClosePos)) {
						updtHtml.replace(dirAttrPos + 5, spacePos, dirAttr);
					} else {
						updtHtml.replace(dirAttrPos + 5, bodyTagClosePos,
								dirAttr);
					}

					bodyTagClosePos = updtHtml.indexOf(">", bodyTagPos);
					dirAttrInserted = true;
				}
			}
		}

		if (!langAttrInserted) {
			if (htmlTagPos > -1) {
				updtHtml.insert(htmlTagPos + 5, " lang=" + langAttr);
			} else if (bodyTagPos > -1) {
				updtHtml.insert(bodyTagPos + 5, " lang=" + langAttr);
			}
		}

		if (!dirAttrInserted) {
			if (htmlTagPos > -1) {
				updtHtml.insert(htmlTagPos + 5, " dir=" + dirAttr);
			} else if (bodyTagPos > -1) {
				updtHtml.insert(bodyTagPos + 5, " dir=" + dirAttr);
			}
		}

		return updtHtml.toString();
	}

}
