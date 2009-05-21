/*
 * $Id: BeanMixin.java 1075 2009-05-07 06:41:19Z lhoriman $
 * $URL: https://subetha.googlecode.com/svn/branches/resin/rtest/src/org/subethamail/rtest/util/BeanMixin.java $
 */

package org.subethamail.baton;

import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.subethamail.baton.util.SMTPException;
import org.subethamail.baton.util.SmartClient;
import org.subethamail.smtp.MessageContext;
import org.subethamail.smtp.MessageHandler;
import org.subethamail.smtp.RejectException;
import org.subethamail.smtp.TooMuchDataException;

/**
 * @author Jeff Schnitzer
 */
class Session implements MessageHandler
{
	/** */
	private static Logger log = LoggerFactory.getLogger(Session.class);
	
	/** */
	protected Baton app;
	
	/** */
	protected MessageContext msgContext;
	
	/** */
	protected String from;
	
	/** */
	protected Map<String, SmartClient> targets = new HashMap<String, SmartClient>();
	
	/** */
	Session(Baton app, MessageContext ctx)
	{
		this.app = app;
		this.msgContext = ctx;
	}

	/**
	 * We merely keep track of the from field, this doesn't trigger any tickling
	 * of the target servers.
	 */
	@Override
	public void from(String from) throws RejectException
	{
		this.from = from;
	}

	/**
	 * Each of these will trigger a search through the matchers and subsequent
	 * prodding of target servers.
	 */
	@Override
	public void recipient(String to) throws RejectException
	{
		Matcher match = this.getMatch(this.from, to);
		if (match == null)
			throw new RejectException(554, "No appropriate target server");
			
		try
		{
			SmartClient target = this.getTarget(match);
			
			try
			{
				target.from(this.from);
				target.to(to);
			}
			catch (SMTPException ex)
			{
				throw new RejectException(ex.getResponse().getCode(), ex.getResponse().getMessage());
			}
		}
		catch (IOException ex)
		{
			throw new RejectException(554, ex.getMessage());
		}
	}
	
	/**
	 * Here we deliver the data to all target servers.  Note that failures are logged
	 * but otherwise ignored.
	 */
	@Override
	public void data(InputStream data) throws RejectException, TooMuchDataException, IOException
	{
		this.dataStart();
		this.dataWrite(data);
		this.dataEnd();
	}
	
	/**
	 * Start the DATA command on all connected targets.  Shutdown and remove all
	 * targets that fail.
	 */
	protected void dataStart() throws IOException
	{
		// First everybody gets the DATA start command.
		Iterator<Map.Entry<String, SmartClient>> targetIt = this.targets.entrySet().iterator();
		while (targetIt.hasNext())
		{
			Map.Entry<String, SmartClient> targetEntry = targetIt.next();
			
			try
			{
				targetEntry.getValue().dataStart();
			}
			catch (IOException ex)
			{
				log.error("Failure starting DATA on server " + targetEntry.getKey(), ex);
				targetEntry.getValue().close();
				targetIt.remove();
			}
		}
		
		if (this.targets.isEmpty())
			throw new IOException("DATA error on all target servers");
	}
	
	/**
	 * Write data on all connected targets.  Shut down and remove targets that fail.
	 * 
	 * @throws IOException if we fail to read from the client.
	 */
	protected void dataWrite(InputStream data) throws IOException
	{
		// Here we actually write the data to each target.  Again, failures are logged
		// but otherwise ignored.
		byte[] buffer = new byte[8192];
		int numRead;
		while ((numRead = data.read(buffer)) > 0)
		{
			Iterator<Map.Entry<String, SmartClient>> targetIt = targets.entrySet().iterator();
			while (targetIt.hasNext())
			{
				Map.Entry<String, SmartClient> targetEntry = targetIt.next();
				
				try
				{
					targetEntry.getValue().dataWrite(buffer, numRead);
				}
				catch (IOException ex)
				{
					log.error("Failure writing DATA on server " + targetEntry.getKey(), ex);
					targetEntry.getValue().close();
					targetIt.remove();
				}
			}
		}

		if (this.targets.isEmpty())
			throw new IOException("DATA error on all target servers");
	}

	/**
	 * Complete the data session on all connected targets.
	 * Shut down and remove targets that fail.
	 */
	protected void dataEnd() throws IOException
	{
		// First everybody gets the DATA start command.
		Iterator<Map.Entry<String, SmartClient>> targetIt = targets.entrySet().iterator();
		while (targetIt.hasNext())
		{
			Map.Entry<String, SmartClient> targetEntry = targetIt.next();
			
			try
			{
				targetEntry.getValue().dataEnd();
			}
			catch (IOException ex)
			{
				log.error("Failure ending data stream on server " + targetEntry.getKey(), ex);
				targetEntry.getValue().close();
				targetIt.remove();
			}
		}

		if (this.targets.isEmpty())
			throw new IOException("DATA error on all target servers");
	}

	/**
	 * Checks them all, returns null if nothing found. 
	 */
	protected Matcher getMatch(String matchFrom, String matchTo)
	{
		for (Matcher match: this.app.getMatchers())
		{
			if (match.matches(matchFrom, matchTo))
			{
				return match;
			}
		}
		
		return null;
	}
	
	/** Gets or creates an appropriate client */
	protected SmartClient getTarget(Matcher match) throws IOException
	{
		String key = match.getTargetKey();
		
		SmartClient target = this.targets.get(key);
		if (target == null)
		{
			target = new SmartClient(match.getHost(), match.getPort(), app.getHelo());
			this.targets.put(key, target);
		}
		
		return target;
	}

	/**
	 * Cleans up any open sockets to backend targets.
	 */
	@Override
	public void done()
	{
		for (SmartClient target: this.targets.values())
			target.quit();

		this.targets.clear();
	}
}