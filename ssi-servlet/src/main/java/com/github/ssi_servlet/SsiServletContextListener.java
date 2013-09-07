package com.github.ssi_servlet;

import java.util.concurrent.ConcurrentHashMap;

import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;

import org.apache.catalina.ssi.SSIServlet_JBossWeb;

public class SsiServletContextListener implements ServletContextListener {

	private ConcurrentHashMap<String, String> fileTextCache;

	@Override
	public void contextDestroyed(ServletContextEvent event) {
		Object attrbObj = event.getServletContext().getAttribute(
				SSIServlet_JBossWeb.INIT_PARAM_FILE_TEXT_CACHING);
		event.getServletContext().removeAttribute(
				SSIServlet_JBossWeb.INIT_PARAM_FILE_TEXT_CACHING);

		if (attrbObj instanceof ConcurrentHashMap) {
			((ConcurrentHashMap<?, ?>) attrbObj).clear();
			attrbObj = null;
		}

		if (fileTextCache != null) {
			fileTextCache.clear();
			fileTextCache = null;
		}
	}

	@Override
	public void contextInitialized(ServletContextEvent event) {
		boolean fileTextCaching = Boolean.parseBoolean(event
				.getServletContext().getInitParameter(
						SSIServlet_JBossWeb.INIT_PARAM_FILE_TEXT_CACHING));

		if (fileTextCaching) {
			fileTextCache = new ConcurrentHashMap<String, String>(50, 0.75f, 25);
			event.getServletContext().setAttribute(
					SSIServlet_JBossWeb.INIT_PARAM_FILE_TEXT_CACHING,
					fileTextCache);
		}
	}

}
