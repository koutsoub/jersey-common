package com.talis.jersey.auth;

public class NullAuthenticator implements Authenticator{

	@Override
	public void authenticate(String username, String password)
			throws InvalidCredentialsException {
		throw new InvalidCredentialsException("No authentication mechanism is configured");
	}

}
