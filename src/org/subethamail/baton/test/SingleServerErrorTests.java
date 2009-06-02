/*
 * $Id: BeanMixin.java 1075 2009-05-07 06:41:19Z lhoriman $
 * $URL: https://subetha.googlecode.com/svn/branches/resin/rtest/src/org/subethamail/rtest/util/BeanMixin.java $
 */

package org.subethamail.baton.test;

import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.subethamail.baton.Baton;
import org.subethamail.baton.Matcher;
import org.subethamail.client.SMTPException;
import org.subethamail.client.SmartClient;
import org.testng.annotations.Test;

/**
 * @author Jeff Schnitzer
 */
public class SingleServerErrorTests
{
	/** */
	@SuppressWarnings("unused")
	private static Logger log = LoggerFactory.getLogger(SingleServerErrorTests.class);
	
	/** */
	public static final int BATON_PORT = 2200;
	
	/** */
	public static final int WISER_PORT = 2201;
	public static final String WISER_HOSTPORT = "localhost:" + WISER_PORT;
	
	/** */
	public static final String MSG_BODY = "This is a random message body\nReally\nIt is";

	/** */
	@Test(expectedExceptions={SMTPException.class})
	public void rejectsTo() throws Exception
	{
		EvenWiser wiser1 = new EvenWiser(WISER_PORT, "bob@example.com", false);
		wiser1.start();
		
		List<Matcher> matchers = new ArrayList<Matcher>();
		matchers.add(new Matcher(WISER_HOSTPORT));
		
		Baton bat = new Baton(matchers, BATON_PORT, null);
		bat.start();

		try
		{
			SmartClient client = new SmartClient("localhost", BATON_PORT, "localhost");
			client.from("testing@somewhere.com");
			client.to("notbob@example.com");
		}
		finally
		{
			bat.stop();
			wiser1.stop();
		}
	}
	
	/** */
	@Test(expectedExceptions={SMTPException.class})
	public void rejectsData() throws Exception
	{
		EvenWiser wiser1 = new EvenWiser(WISER_PORT, null, true);
		wiser1.start();
		
		List<Matcher> matchers = new ArrayList<Matcher>();
		matchers.add(new Matcher(WISER_HOSTPORT));
		
		Baton bat = new Baton(matchers, BATON_PORT, null);
		bat.start();

		try
		{
			SmartClient client = new SmartClient("localhost", BATON_PORT, "localhost");
			client.from("testing@somewhere.com");
			client.to("notbob@example.com");
			client.dataStart();
			client.dataWrite(MSG_BODY.getBytes(), MSG_BODY.length());
			client.dataWrite(MSG_BODY.getBytes(), MSG_BODY.length());
			client.dataEnd();
		}
		finally
		{
			bat.stop();
			wiser1.stop();
		}
	}
}