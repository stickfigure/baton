/*
 * $Id: BeanMixin.java 1075 2009-05-07 06:41:19Z lhoriman $
 * $URL: https://subetha.googlecode.com/svn/branches/resin/rtest/src/org/subethamail/rtest/util/BeanMixin.java $
 */

package org.subethamail.baton;

import org.w3c.dom.Element;


/**
 * @author Jeff Schnitzer
 */
public class Matcher
{
	/** */
	public static final String ELEMENT_NAME = "default";
	public static final String ATTR_TARGET = "target";
	
	/** */
	String host;
	public String getHost() { return this.host; }

	/** */
	int port = 25;
	public int getPort() { return this.port; }
	
	/** 
	 * Initialize from a DOM entry like this:
	 * 
	 * <default route="localhost:2500"/>
	 */
	public Matcher(String hostAndPort)
	{
		String[] parts = hostAndPort.split(":");
		
		host = parts[0];
		
		if (parts.length > 1)
			port = Integer.parseInt(parts[1]);
	}
	
	/** Construct it from any match line*/
	public Matcher(Element route)
	{
		this(route.getAttribute(ATTR_TARGET));
	}

	/** True of this matcher matches the condition */
	public boolean matches(String from, String to)
	{
		return true;
	}
	
	/**
	 * @return a consistent, normalized view of target suitable for using
	 *  as a hashmap key.
	 */
	public String getTargetKey()
	{
		return this.host.toLowerCase() + ":" + this.port;
	}
}