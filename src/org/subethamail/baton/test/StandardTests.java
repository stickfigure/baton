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
import org.subethamail.client.SmartClient;
import org.subethamail.wiser.Wiser;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

/**
 * @author Jeff Schnitzer
 */
public class StandardTests
{
	/** */
	@SuppressWarnings("unused")
	private static Logger log = LoggerFactory.getLogger(StandardTests.class);
	
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
	
	/** */
	Wiser wiser1;
	Wiser wiser2;
	
	/** */
	@BeforeMethod
	public void setUp()
	{
		this.wiser1 = new Wiser(WISER_1_PORT);
		this.wiser1.start();
		
		this.wiser2 = new Wiser(WISER_2_PORT);
		this.wiser2.start();
	}
	
	/** */
	@AfterMethod
	public void tearDown()
	{
		this.wiser1.stop();
		this.wiser1 = null;
		
		this.wiser2.stop();
		this.wiser2 = null;
	}

	/** */
	@Test(groups={"easy"})
	public void oneServerOneRecipient() throws Exception
	{
		List<Matcher> matchers = new ArrayList<Matcher>();
		matchers.add(new Matcher(WISER_1_HOSTPORT));
		
		Baton bat = new Baton(matchers, BATON_PORT, null);
		bat.start();
		
		SmartClient client = new SmartClient("localhost", BATON_PORT, "localhost");
		client.from("testing@somewhere.com");
		client.to("one@example.com");
		client.dataStart();
		client.dataWrite(MSG_BODY.getBytes(), MSG_BODY.length());
		client.dataWrite(MSG_BODY.getBytes(), MSG_BODY.length());
		client.dataEnd();
		client.quit();
		
		bat.stop();
		
		assert 1 == this.wiser1.getMessages().size();
	}
	
	/** */
	@Test
	public void twoServersSplitRecipients() throws Exception
	{
		List<Matcher> matchers = new ArrayList<Matcher>();
		matchers.add(new CaseMatcher(WISER_1_HOSTPORT, null, "one.*@example\\.com"));
		matchers.add(new Matcher(WISER_2_HOSTPORT));
		
		Baton bat = new Baton(matchers, BATON_PORT, null);
		bat.start();
		
		SmartClient client = new SmartClient("localhost", BATON_PORT, "localhost");
		client.from("testing@somewhere.com");
		client.to("one@example.com");
		client.to("two@example.com");
		client.dataStart();
		client.dataWrite(MSG_BODY.getBytes(), MSG_BODY.length());
		client.dataWrite(MSG_BODY.getBytes(), MSG_BODY.length());
		client.dataEnd();
		client.quit();
		
		bat.stop();
		
		assert 1 == this.wiser1.getMessages().size();
		assert 1 == this.wiser2.getMessages().size();
	}

	/** */
	@Test
	public void oneServerTwoRecipients() throws Exception
	{
		List<Matcher> matchers = new ArrayList<Matcher>();
		matchers.add(new Matcher(WISER_1_HOSTPORT));
		
		Baton bat = new Baton(matchers, BATON_PORT, null);
		bat.start();
		
		SmartClient client = new SmartClient("localhost", BATON_PORT, "localhost");
		client.from("testing@somewhere.com");
		client.to("one@example.com");
		client.to("two@example.com");
		client.dataStart();
		client.dataWrite(MSG_BODY.getBytes(), MSG_BODY.length());
		client.dataWrite(MSG_BODY.getBytes(), MSG_BODY.length());
		client.dataEnd();
		client.quit();
		
		bat.stop();
		
		assert 2 == this.wiser1.getMessages().size();
	}

	/** */
	@Test
	public void multipleCasesSameTarget() throws Exception
	{
		List<Matcher> matchers = new ArrayList<Matcher>();
		matchers.add(new CaseMatcher(WISER_1_HOSTPORT, null, "one@example.com"));
		matchers.add(new Matcher(WISER_1_HOSTPORT));
		
		Baton bat = new Baton(matchers, BATON_PORT, null);
		bat.start();
		
		SmartClient client = new SmartClient("localhost", BATON_PORT, "localhost");
		client.from("testing@somewhere.com");
		client.to("one@example.com");
		client.to("two@example.com");
		client.dataStart();
		client.dataWrite(MSG_BODY.getBytes(), MSG_BODY.length());
		client.dataWrite(MSG_BODY.getBytes(), MSG_BODY.length());
		client.dataEnd();
		client.quit();
		
		bat.stop();
		
		assert 2 == this.wiser1.getMessages().size();
	}
}