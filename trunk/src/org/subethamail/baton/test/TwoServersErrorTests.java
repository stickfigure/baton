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
import org.subethamail.baton.CaseMatcher;
import org.subethamail.baton.Matcher;
import org.subethamail.baton.util.SmartClient;
import org.testng.annotations.Test;

/**
 * @author Jeff Schnitzer
 */
public class TwoServersErrorTests
{
	/** */
	@SuppressWarnings("unused")
	private static Logger log = LoggerFactory.getLogger(TwoServersErrorTests.class);
	
	/** */
	public static final int BATON_PORT = 2200;
	
	/** */
	public static final int WISER_1_PORT = 2201;
	public static final String WISER_1_HOSTPORT = "localhost:" + WISER_1_PORT;
	
	/** */
	public static final int WISER_2_PORT = 2202;
	public static final String WISER_2_HOSTPORT = "localhost:" + WISER_2_PORT;
	
	/** */
	public static final String MSG_BODY = "This is a random message body\nReally\nIt is";
	
	/**
	 * This should complete cleanly because we log but ignore errors when
	 * one server fails data but the other succeeds.
	 */
	@Test
	public void oneRejectsData() throws Exception
	{
		EvenWiser normalWiser = new EvenWiser(WISER_1_PORT, null, false);
		normalWiser.start();
		
		EvenWiser brokenWiser = new EvenWiser(WISER_2_PORT, null, true);
		brokenWiser.start();
		
		List<Matcher> matchers = new ArrayList<Matcher>();
		matchers.add(new CaseMatcher(WISER_1_HOSTPORT, null, "^bob.*@example.com$"));
		matchers.add(new Matcher(WISER_2_HOSTPORT));
		
		Baton bat = new Baton(matchers, BATON_PORT, null);
		bat.start();

		try
		{
			SmartClient client = new SmartClient("localhost", BATON_PORT, "localhost");
			client.from("testing@somewhere.com");
			client.to("bob@example.com");
			client.to("notbob@example.com");
			client.dataStart();
			client.dataWrite(MSG_BODY.getBytes(), MSG_BODY.length());
			client.dataWrite(MSG_BODY.getBytes(), MSG_BODY.length());
			client.dataEnd();
			
			assert normalWiser.getMessages().size() == 1;
			assert brokenWiser.getMessages().size() == 0;
		}
		finally
		{
			bat.stop();
			normalWiser.stop();
			brokenWiser.stop();
		}
	}
}