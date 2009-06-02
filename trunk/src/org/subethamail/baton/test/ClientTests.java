/*
 * $Id: BeanMixin.java 1075 2009-05-07 06:41:19Z lhoriman $
 * $URL: https://subetha.googlecode.com/svn/branches/resin/rtest/src/org/subethamail/rtest/util/BeanMixin.java $
 */

package org.subethamail.baton.test;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.subethamail.client.SmartClient;
import org.subethamail.wiser.Wiser;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

/**
 * @author Jeff Schnitzer
 */
public class ClientTests
{
	/** */
	@SuppressWarnings("unused")
	private static Logger log = LoggerFactory.getLogger(ClientTests.class);
	
	/** */
	public static final int WISER_1_PORT = 2201;
	public static final String WISER_1_HOSTPORT = "localhost:" + WISER_1_PORT;
	
	/** */
	public static final String MSG_BODY = "This is a random message body\nReally\nIt is";
	
	/** */
	Wiser wiser1;
	
	/** */
	@BeforeMethod
	public void setUp()
	{
		this.wiser1 = new Wiser(WISER_1_PORT);
		this.wiser1.start();
	}
	
	/** */
	@AfterMethod
	public void tearDown()
	{
		this.wiser1.stop();
		this.wiser1 = null;
	}

	/** */
	@Test(groups={"client"})
	public void clientAgainstWiser() throws Exception
	{
		SmartClient client = new SmartClient("localhost", WISER_1_PORT, "localhost");
		client.from("testing@somewhere.com");
		client.to("one@example.com");
		client.dataStart();
		client.dataWrite(MSG_BODY.getBytes(), MSG_BODY.length());
		client.dataWrite(MSG_BODY.getBytes(), MSG_BODY.length());
		client.dataEnd();
		client.quit();
		
		assert 1 == this.wiser1.getMessages().size();
	}
}