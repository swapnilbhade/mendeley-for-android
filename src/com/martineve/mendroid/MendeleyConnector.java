/*
 *  Original version at: http://code.google.com/p/droidbiblio/source/detail?r=3
 *  Copyright 2011 Clemens Lombriser <clemens@lom.ch>
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 * 
 *  Modifications for Mendroid
 *  
 *  Mendroid: a Mendeley Android client
 *  Copyright 2011 Martin Paul Eve <martin@martineve.com>
 *
 *  This file is part of Mendroid.
 *
 *  Mendroid is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 *   
 *  Mendroid is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with Mendroid.  If not, see <http://www.gnu.org/licenses/>.
 *  
 */

package com.martineve.mendroid;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

import oauth.signpost.OAuthConsumer;
import oauth.signpost.OAuthProvider;
import oauth.signpost.commonshttp.CommonsHttpOAuthConsumer;
import oauth.signpost.commonshttp.CommonsHttpOAuthProvider;
import oauth.signpost.basic.DefaultOAuthConsumer;
import oauth.signpost.basic.DefaultOAuthProvider;
import oauth.signpost.exception.OAuthCommunicationException;
import oauth.signpost.exception.OAuthExpectationFailedException;
import oauth.signpost.exception.OAuthMessageSignerException;
import oauth.signpost.exception.OAuthNotAuthorizedException;

import org.json.JSONArray;

import android.app.Application;
import android.content.SharedPreferences;

public class MendeleyConnector {

	private boolean m_bAuthenticated = false;
	private Application a_app;
	private SharedPreferences s_settings;

	// import the OAuth keys from a class with just two static strings
	private String m_consumerkey = OAuth.CONSUMERKEY;
	private String m_consumersecret = OAuth.CONSUMERSECRET;

	private String m_authURL;
	

	private OAuthConsumer m_consumer = new DefaultOAuthConsumer(
			m_consumerkey,m_consumersecret);
	

	private OAuthProvider m_provider;

	public MendeleyConnector(Application androidApp) {
		m_provider = new DefaultOAuthProvider(
						MendeleyURLs.getURL(MendeleyURLs.OAUTH_REQUEST),
						MendeleyURLs.getURL(MendeleyURLs.OAUTH_ACCESS),
						MendeleyURLs.getURL(MendeleyURLs.OAUTH_AUTHORIZE));
		
		a_app = androidApp;
		
		s_settings = a_app.getSharedPreferences(Settings.PREFS_NAME, 0);
	}
	
	public MendeleyConnector(Application androidApp, String token, String tokenSecret) {
		m_provider = new DefaultOAuthProvider(
						MendeleyURLs.getURL(MendeleyURLs.OAUTH_REQUEST),
						MendeleyURLs.getURL(MendeleyURLs.OAUTH_ACCESS),
						MendeleyURLs.getURL(MendeleyURLs.OAUTH_AUTHORIZE));
		
		a_app = androidApp;

		m_consumer.setTokenWithSecret(token, tokenSecret);
		
		m_bAuthenticated = true;
		
		s_settings = a_app.getSharedPreferences(Settings.PREFS_NAME, 0);
	}
	
	public String getMendeleyResponse(String strURL) throws Exception{
		URL url = new URL(strURL + "consumer_key=" + m_consumerkey);
		HttpURLConnection request = (HttpURLConnection) url.openConnection();
		m_consumer.sign(request);

		request.connect();

		if (request.getResponseCode() == 200) {
			String strResponse = "";
			InputStreamReader in = new InputStreamReader((InputStream)request.getContent());
			BufferedReader buff = new BufferedReader(in);

			for (String strLine = ""; strLine != null; strLine = buff.readLine()) {
				strResponse += strLine + "\n";
			}
			
			in.close();

			return strResponse;
		} else {
			throw new Exception("Mendeley Server returned " + request.getResponseCode() + ": " + request.getResponseMessage());
		}

	}

	public String getAuthenticationURL() throws Exception {
		m_authURL = m_provider.retrieveRequestToken(m_consumer, OAuth.CALLBACKURL);
		return m_authURL;
	}

	public boolean isConnected() {
		return m_bAuthenticated;
	}

	public boolean setVerificationCode(String code) {
		try {
			m_provider.setOAuth10a(true);
			m_provider.retrieveAccessToken(m_consumer, code);
			
			// persist the token and secret
			SharedPreferences.Editor editor = s_settings.edit();
		    editor.putString("Token", m_consumer.getToken());
		    editor.putString("TokenSecret", m_consumer.getTokenSecret());

		    editor.commit();

		} catch (Exception e) {
			return false;
		}
		m_bAuthenticated = true;
		return true;
	}

}
