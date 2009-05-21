/*
 * $Id: BeanMixin.java 1075 2009-05-07 06:41:19Z lhoriman $
 * $URL: https://subetha.googlecode.com/svn/branches/resin/rtest/src/org/subethamail/rtest/util/BeanMixin.java $
 */

package org.subethamail.baton;

import java.io.File;
import java.io.IOException;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.List;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.subethamail.smtp.MessageContext;
import org.subethamail.smtp.MessageHandler;
import org.subethamail.smtp.MessageHandlerFactory;
import org.subethamail.smtp.server.SMTPServer;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

/**
 * @author Jeff Schnitzer
 */
public class Baton implements MessageHandlerFactory
{
	/** */
	public static final String ATTR_PORT = "port";
	public static final String ATTR_HELO = "helo";
	
	/** */
	@SuppressWarnings("unused")
	private static Logger log = LoggerFactory.getLogger(Baton.class);
	
	/** */
	protected List<Matcher> matchers;
	
	/** */
	protected SMTPServer server;
	
	/** The name we announce ourselves to be in HELO to other servers */
	protected String helo;
	
	/** */
	protected Baton()
	{
		this.server = new SMTPServer(this);
	}

	/** */
	public Baton(String file) throws IOException
	{
		this(new File(file));
	}	
	/** */
	public Baton(File config) throws IOException
	{
		this();
		
		DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
		try
		{
			DocumentBuilder db = dbf.newDocumentBuilder();
			Document doc = db.parse(config);
			
			this.configure(doc);
		}
		catch (ParserConfigurationException ex) { throw new RuntimeException(ex); }
		catch (SAXException ex) { throw new IOException(ex); }
	}
	
	/** */
	public Baton(Document doc)
	{
		this();
		
		this.configure(doc);
	}
	
	/** */
	public Baton(List<Matcher> matchers, int port, String helo)
	{
		this();
		
		this.server.setPort(port);
		this.setHelo(helo);
		this.matchers = matchers;
	}
	
	/**
	 * Try an automatic lookup if value is null or empty
	 */
	protected void setHelo(String value)
	{
		if (value != null && value.length() > 0)
		{
			this.helo = value;
		}
		else
		{
			try
			{
				this.helo = InetAddress.getLocalHost().getCanonicalHostName();
			}
			catch (UnknownHostException ex) { throw new RuntimeException(ex); }
		}
	}
	
	/**
	 * See sample-config.xml
	 */
	protected void configure(Document doc)
	{
		Element root = doc.getDocumentElement();
		
		String port = root.getAttribute(ATTR_PORT);
		if (port != null && port.trim().length() > 0)
			this.server.setPort(Integer.parseInt(port));
		
		String whoami = root.getAttribute(ATTR_HELO);
		this.setHelo(whoami);
		
		this.matchers = new ArrayList<Matcher>();
		
		NodeList nodes = root.getChildNodes();
		for (int i=0; i<nodes.getLength(); i++)
		{
			Element node = (Element)nodes.item(i);
			String name = node.getNodeName();
			
			if (name.equals(Matcher.ELEMENT_NAME))
				this.matchers.add(new Matcher(node));
			else if (name.equals(CaseMatcher.ELEMENT_NAME))
				this.matchers.add(new CaseMatcher(node));
			else
				throw new IllegalArgumentException("Unknown configuration element: " + name);
		}
	}
	
	/** */
	List<Matcher> getMatchers() { return this.matchers; }
	
	/** */
	String getHelo() { return this.helo; }
	
	/** */
	public void start()
	{
		this.server.start();
	}
	
	/** */
	public void stop()
	{
		this.server.stop();
	}

	/* */
	@Override
	public MessageHandler create(MessageContext ctx)
	{
		return new Session(this, ctx);
	}
}