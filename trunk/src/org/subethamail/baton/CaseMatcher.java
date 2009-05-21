/*
 * $Id: BeanMixin.java 1075 2009-05-07 06:41:19Z lhoriman $
 * $URL: https://subetha.googlecode.com/svn/branches/resin/rtest/src/org/subethamail/rtest/util/BeanMixin.java $
 */

package org.subethamail.baton;

import org.w3c.dom.Element;


/**
 * This matcher checks the "from" and/or the "to" fields (envelope sender and
 * envelope recipient) against static regexes.  If both "to" and "from" are
 * specified, the conditions are ANDed.
 * 
 * @author Jeff Schnitzer
 */
public class CaseMatcher extends Matcher
{
	/** */
	public static final String ELEMENT_NAME = "case";
	public static final String ATTR_FROM = "from";
	public static final String ATTR_TO = "to";
	
	/** */
	public String matchFrom;
	public String matchTo;
	
	/** 
	 * Initialize from a DOM entry like this:
	 * 
	 * <case to="somebody.*@example\.com" route="localhost:2500"/>
	 * 
	 * Can specify attributes "to" and/or "from".
	 */
	public CaseMatcher(Element matchNode)
	{
		super(matchNode);
		
		String from = matchNode.getAttribute(ATTR_FROM);
		if (from != null && from.trim().length() == 0)
			from = null;
		
		String to = matchNode.getAttribute(ATTR_TO);
		if (to != null && to.trim().length() == 0)
			to = null;
		
		this.setMatchFrom(from);
		this.setMatchTo(to);
	}
	
	/** One or both of "from" or "to" must be non-null.  If both, condition is AND. */
	public CaseMatcher(String hostAndPort, String from, String to)
	{
		super(hostAndPort);
		
		this.setMatchFrom(from);
		this.setMatchTo(to);
	}
	
	/** Add implicit begin/end to the regex */
	protected void setMatchFrom(String value)
	{
		this.matchFrom = "^" + value + "$";
	}
	
	/** Add implicit begin/end to the regex */
	protected void setMatchTo(String value)
	{
		this.matchTo = "^" + value + "$";
	}
	
	/** Checks against the "from" and "to" regexes */
	@Override
	public boolean matches(String from, String to)
	{
		if (this.matchFrom != null && !from.matches(this.matchFrom))
			return false;
		
		if (this.matchTo != null && !to.matches(this.matchTo))
			return false;
		
		return true;
	}
}