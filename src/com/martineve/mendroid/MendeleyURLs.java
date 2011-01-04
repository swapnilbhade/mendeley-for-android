/*
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

public class MendeleyURLs {
	public static String BASE = "http://www.mendeley.com";
	//public static String COLLECTIONS = "/oapi/library/";
	public static String COLLECTIONS = "/oapi/stats/authors/";
	public static String CONTACTS = "/oapi/profiles";
	public static String OAUTH_REQUEST = "/oauth/request_token/";
	public static String OAUTH_ACCESS = "/oauth/access_token/";
	public static String OAUTH_AUTHORIZE = "/oauth/authorize/";
	
	public static String getURL(String URLWithoutBase)
	{
		return BASE + URLWithoutBase;
	}
}
