package org.subethamail.baton.util;

import java.io.IOException;

import org.subethamail.baton.util.SMTPClient.Response;

@SuppressWarnings("serial")
public class SMTPException extends IOException
{
	Response response;

	public SMTPException(Response resp)
	{
		super(resp.toString());
		
		this.response = resp;
	}

	public Response getResponse() { return this.response; }
	
}
