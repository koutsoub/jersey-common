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
package com.talis.jersey.guice;

import com.google.inject.AbstractModule;
import com.talis.jersey.filters.ServerInfo;

public class GenericServerInfoModule extends AbstractModule{

	public static final String SERVER_IDENTIFIER_DEFAULT = "Jetty/Jersey Web Server";
	public static final String SERVER_IDENTIFIER_PROPERTY = "com.talis.jersey.guice.serverid";
	
	@Override
	protected void configure() {
		bind(ServerInfo.class).toInstance(new ServerInfo() {
			@Override
			public String getServerIdentifier() {
				return System.getProperty(SERVER_IDENTIFIER_PROPERTY, SERVER_IDENTIFIER_DEFAULT);
			}
		});
	}
}
