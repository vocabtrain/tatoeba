/*
Tatoeba - Collection of example sentences for android 
Copyright (C) 2012 Dominik Köppl

Licensed under the Apache License, Version 2.0 (the "License");
you may not use this file except in compliance with the License.
You may obtain a copy of the License at

    http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License.
 */

package org.tatoeba.providers;

import android.net.Uri;
import android.provider.BaseColumns;

public interface ProviderInterface
{
	public static final class LinkTable implements BaseColumns
	{
		public static final Uri CONTENT_URI = Uri.parse("content://" + ProviderInterface.AUTHORITY + "/" + LINK_TABLE);
		public static final String CONTENT_TYPE = "vnd.android.cursor.dir/vnd.org.tatoeba.link";
		public final static String LANGUAGE = "lang";
		public final static String TEXT = "text";
	}

	public static final class RubyTable implements BaseColumns
	{
		public static final Uri CONTENT_URI = Uri.parse("content://" + ProviderInterface.AUTHORITY + "/" + RUBY_TABLE);
		public static final String CONTENT_TYPE = "vnd.android.cursor.dir/vnd.org.tatoeba.ruby";
		public final static String TEXT = "text";
	}

	public static final class SearchTable implements BaseColumns
	{
		public static final Uri CONTENT_URI = Uri.parse("content://" + ProviderInterface.AUTHORITY + "/" + SEARCH_TABLE);
		public static final String CONTENT_TYPE = "vnd.android.cursor.dir/vnd.org.tatoeba.search";
		public final static String LANGUAGE = "lang";
	}

	public static final String AUTHORITY = "org.tatoeba.providers.TranslationProvider";

	public static final String SEARCH_TABLE = "search";

	public static final String LINK_TABLE = "links";

	public static final String RUBY_TABLE = "ruby";
}
