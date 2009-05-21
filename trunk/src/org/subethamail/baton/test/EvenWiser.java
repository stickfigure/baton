/*
 * $Id: BeanMixin.java 1075 2009-05-07 06:41:19Z lhoriman $
 * $URL: https://subetha.googlecode.com/svn/branches/resin/rtest/src/org/subethamail/rtest/util/BeanMixin.java $
 */

package org.subethamail.baton.test;

import java.io.IOException;
import java.io.InputStream;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.subethamail.smtp.TooMuchDataException;
import org.subethamail.wiser.Wiser;

/**
 * Extends Wiser to allow us to reject recipients and simulate rejection
 * of the data stream.
 * 
 * @author Jeff Schnitzer
 */
public class EvenWiser extends Wiser
{
	/** */
	@SuppressWarnings("unused")
	private static Logger log = LoggerFactory.getLogger(EvenWiser.class);
	
	/** */
	public boolean breakData;
	public String onlyAllowedRecipient;

	/**
	 * @param onlyAllowedRecipient if null, any recipients are allowed
	 * @param breakData if true, data transfer will error rather than complete OK
	 */
	public EvenWiser(int port, String onlyAllowedRecipient, boolean breakData)
	{
		super(port);
		
		this.onlyAllowedRecipient = onlyAllowedRecipient;
		this.breakData = breakData;
	}

	@Override
	public boolean accept(String from, String recipient)
	{
		if (this.onlyAllowedRecipient == null)
			return true;
		else
			return this.onlyAllowedRecipient.equals(recipient);
	}

	@Override
	public void deliver(String from, String recipient, InputStream data)
			throws TooMuchDataException, IOException
	{
		if (this.breakData)
		{
			throw new TooMuchDataException();
		}
		else
		{
			super.deliver(from, recipient, data);
		}
	}

}