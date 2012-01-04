/*
 *    Copyright 2011 Talis Systems Ltd
 * 
 *    Licensed under the Apache License, Version 2.0 (the "License");
 *    you may not use this file except in compliance with the License.
 *    You may obtain a copy of the License at
 * 
 *        http://www.apache.org/licenses/LICENSE-2.0
 * 
 *    Unless required by applicable law or agreed to in writing, software
 *    distributed under the License is distributed on an "AS IS" BASIS,
 *    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *    See the License for the specific language governing permissions and
 *    limitations under the License.
 */
package com.talis.jersey.auth;

import java.util.UUID;

import org.junit.Test;

public class NoopAuthenticatorTest {

	@Test
	public void alwaysAuthenticateWithoutError() throws InvalidCredentialsException{
		NoopAuthenticator authenticator = new NoopAuthenticator();
		int numAttempts = 10000;
		for (int i=0; i<numAttempts; i++){
			authenticator.authenticate(UUID.randomUUID().toString(), UUID.randomUUID().toString());
		}
	}
	
	@Test
	public void nullUserAndPasswordAuthenticateWithoutError() throws InvalidCredentialsException{
		NoopAuthenticator authenticator = new NoopAuthenticator();
		authenticator.authenticate(null, null);
	}
	
}
