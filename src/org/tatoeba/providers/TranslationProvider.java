/*
Tatoeba - Collection of example sentences for android 
Copyright (C) 2012 Dominik Köppl

This program is free software: you can redistribute it and/or modify
it under the terms of the GNU General Public License as published by
the Free Software Foundation, either version 3 of the License, or
(at your option) any later version.

This program is distributed in the hope that it will be useful,
but WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
GNU General Public License for more details.

You should have received a copy of the GNU General Public License
along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package org.tatoeba.providers;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URLEncoder;
import java.util.List;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.lucene.queryparser.classic.ParseException;
import org.tatoeba.TatoebaApplication;
import org.tatoeba.providers.ProviderInterface.LinkTable;
import org.tatoeba.providers.ProviderInterface.RubyTable;
import org.tatoeba.providers.ProviderInterface.SearchTable;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import android.content.ContentProvider;
import android.content.ContentValues;
import android.content.SharedPreferences;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.MatrixCursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteException;
import android.database.sqlite.SQLiteQueryBuilder;
import android.net.Uri;
import android.preference.PreferenceManager;

public class TranslationProvider extends ContentProvider
{

	public static final String TAG = "TranslationProvider";
	private static final UriMatcher sUriMatcher;
	private static final int SEARCH = 1;
	private static final int LINK = 2;
	private static final int RUBY = 3;

	private SQLiteDatabase db;
	private SQLiteDatabase cachedb;

	static
	{
		sUriMatcher = new UriMatcher(UriMatcher.NO_MATCH);
		sUriMatcher.addURI(ProviderInterface.AUTHORITY, ProviderInterface.SEARCH_TABLE, SEARCH);
		sUriMatcher.addURI(ProviderInterface.AUTHORITY, ProviderInterface.LINK_TABLE + "/#", LINK);
		sUriMatcher.addURI(ProviderInterface.AUTHORITY, ProviderInterface.RUBY_TABLE + "/#", RUBY);
	}
	private Sentences sentenceSearcher;

	private static final String API_ROOT = TatoebaApplication.API_SERVER + "tatoeba/";
	private static final String API_FULL_SEARCH = API_ROOT + "query/%s/%s";
	private static final String API_RANDOM_SAMPLE_WITH_LANGUAGE = API_ROOT + "query/%s";
	private static final String API_RANDOM_SAMPLE = API_ROOT + "query";
	
	private final static String CACHE_FILE = "cache.db";

	@Override
	public int delete(final Uri arg0, final String arg1, final String[] arg2)
	{
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public String getType(final Uri uri)
	{
		switch(sUriMatcher.match(uri))
		{
			case SEARCH:
				return SearchTable.CONTENT_TYPE;
			case LINK:
				return LinkTable.CONTENT_TYPE;
			case RUBY:
				return RubyTable.CONTENT_TYPE;
			default:
				throw new IllegalArgumentException("Unknown URI " + uri);
		}
	}

	@Override
	public Uri insert(final Uri arg0, final ContentValues arg1)
	{
		// TODO Auto-generated method stub
		return null;
	}
	
	public static final String DATABASE_FILENAME = "tatoeba.sqlite";

	@Override
	public boolean onCreate()
	{
		final SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getContext());
		final boolean use_offline_dict = prefs.getBoolean("offline_dict", false);
		try
		{
			if(use_offline_dict)
			{

				final String directory = prefs.getString("directory", null);
				if(sentenceSearcher == null)
					sentenceSearcher = new Sentences(directory);
				if(db == null)
					db = SQLiteDatabase.openDatabase(directory + File.separator + DATABASE_FILENAME, null, SQLiteDatabase.OPEN_READONLY);
				if(cachedb != null)
				{
					cachedb.close();
					cachedb = null;
					// if(CACHE_FILE.exists()) CACHE_FILE.delete();
				}
			}
			else
			{

				if(db != null)
				{
					db.close();
					db = null;
				}
				if(sentenceSearcher != null)
					sentenceSearcher = null;
				final File cacheFile = getContext().getDatabasePath(CACHE_FILE);
				cacheFile.getParentFile().mkdirs();
				if(cacheFile.exists()) cacheFile.delete();
				if(cachedb == null)
					cachedb = SQLiteDatabase.openOrCreateDatabase(cacheFile.toString(), null);
				
				cachedb.execSQL("CREATE TABLE IF NOT EXISTS `" + ProviderInterface.LINK_TABLE
						+ "` (`sentence_id` INTEGER, `translation_id` INTEGER, UNIQUE(`sentence_id`, `translation_id`))");
				cachedb.execSQL("CREATE TABLE IF NOT EXISTS `sentences` (`_id` INTEGER NOT NULL,`lang` TEXT,`text` TEXT,PRIMARY KEY(`_id`))");

				cachedb.execSQL("CREATE TABLE IF NOT EXISTS `cache` (`search` TEXT NOT NULL, `sentence_id` INTEGER, UNIQUE(`search`, `sentence_id`))");

			}
			return true;
		}
		catch(final SQLiteException e)
		{
		}

		return true;
	}

	@Override
	public Cursor query(final Uri uri, final String[] projection, final String selection, final String[] selectionArgs, final String sortOrder)
	{
		final SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getContext());
		final boolean use_offline_dict = prefs.getBoolean("offline_dict", false);
		if(use_offline_dict)
		{
			if(db == null || sentenceSearcher == null) onCreate();
			return queryOffline(uri, projection, selection, selectionArgs, sortOrder);
		}
		else
		{
			if(cachedb == null) onCreate();
			return queryOnline(uri, projection, selection, selectionArgs, sortOrder);
		}

	}
	
	private String[] getRandomOffline()
	{
		String selectionArgs[] = new String[2];
		Cursor countCursor = db.query("sentences", new String[] { "COUNT(*)" }, null, null, null, null, null);
		if(!countCursor.moveToFirst()) return null;
		long count = countCursor.getLong(0);
		long id = (long) (Math.random() * count);
		countCursor.close();
		Cursor textCursor = db.query("sentences", new String[] { ProviderInterface.LinkTable.TEXT ,  ProviderInterface.LinkTable.LANGUAGE}, "_id >= ?", new String[] { "" + id }, null, null, null, "1");
		if(!textCursor.moveToFirst()) return null;
		String lang = textCursor.getString(textCursor.getColumnIndex(ProviderInterface.LinkTable.LANGUAGE));
		String text = textCursor.getString(textCursor.getColumnIndex(ProviderInterface.LinkTable.TEXT));
		String[] cols = text.split("[\n ,;\\.]");
		String search = (cols.length == 1) ? ("" + text.charAt((int) (Math.random() * text.length()))) : cols[(int) (Math.random() * cols.length)];
		selectionArgs[0] = search;
		selectionArgs[1] = lang;
		textCursor.close();
		return selectionArgs;
	}
	private String getRandomOffline(String language)
	{
		Cursor textCursor = db.query("sentences", new String[] { ProviderInterface.LinkTable.TEXT}, ProviderInterface.LinkTable.LANGUAGE + " = ?", new String[] { language }, null, null, "RANDOM()", "1");
		if(!textCursor.moveToFirst()) return null;
		String text = textCursor.getString(textCursor.getColumnIndex(ProviderInterface.LinkTable.TEXT));
		String[] cols = text.split("[\n ,;\\.]");
		String search = (cols.length == 1) ? ("" + text.charAt((int) (Math.random() * text.length()))) : cols[(int) (Math.random() * cols.length)];
		textCursor.close();
		return search;
	}

	private Cursor queryOffline(final Uri uri, final String[] projection, final String selection, String[] selectionArgs, final String sortOrder)
	{

		if(sentenceSearcher == null) return null;
		if(db == null) return null;
		try
		{

			switch(sUriMatcher.match(uri))
			{
				case SEARCH:
				{
					String querystring = getContext().getString(org.tatoeba.R.string.example_string);
					String language = getContext().getString(org.tatoeba.R.string.example_language);
					int limit = 10;
					if(selectionArgs == null) 
						selectionArgs = getRandomOffline();
					if(selectionArgs != null)
					{
						if(selectionArgs.length > 0) querystring = selectionArgs[0];
						if(selectionArgs.length > 1) language = selectionArgs[1];
						if(querystring.length() == 0 || querystring.equals(" ")) querystring = getRandomOffline(language);

					}
					if(sortOrder != null) limit = Integer.parseInt(sortOrder);
					final List<Long> examples = sentenceSearcher.findExamples(querystring, language, limit);
					if(examples == null || examples.isEmpty()) return null;

					final SQLiteQueryBuilder queryBuilder = new SQLiteQueryBuilder();
					queryBuilder.setTables("sentences");

					return queryBuilder.query(db, projection, ProviderInterface.SearchTable._ID + StringUtils.generateQuestionTokens(examples.size()),
							StringUtils.createArray(examples), null, null, null);

				}
				case LINK:
				{
					final long link_id = Long.parseLong(uri.getLastPathSegment());
					final SQLiteQueryBuilder queryBuilder = new SQLiteQueryBuilder();
					queryBuilder.setTables(ProviderInterface.LINK_TABLE + " join sentences on _id = translation_id");
					queryBuilder.appendWhere("sentence_id = " + link_id);
					final Cursor cursor = queryBuilder.query(db, projection, selection, selectionArgs, null, null, sortOrder);
					return cursor;

				}
				case RUBY:
				{
					final long link_id = Long.parseLong(uri.getLastPathSegment());
					final SQLiteQueryBuilder queryBuilder = new SQLiteQueryBuilder();
					queryBuilder.setTables(ProviderInterface.RUBY_TABLE);
					queryBuilder.appendWhere(ProviderInterface.RUBY_TABLE + "._id = " + link_id);
					final Cursor cursor = queryBuilder.query(db, projection, selection, selectionArgs, null, null, sortOrder);
					return cursor;

				}
			}
		}
		catch(final ParseException e)
		{
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		catch(final IOException e)
		{
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		return null;
	}

	private Cursor queryOnline(final Uri uri, final String[] projection, final String selection, final String[] selectionArgs, final String sortOrder)
	{
		if(cachedb == null) return null;

		try
		{
			switch(sUriMatcher.match(uri))
			{
				case SEARCH:
				{
					String querystring = null;
					String language = null;
					if(selectionArgs != null)
					{
						if(selectionArgs.length > 0) querystring = selectionArgs[0];
						if(selectionArgs.length > 1) language = selectionArgs[1];
						if(querystring.length() == 0) querystring = null;
						if(language.length() == 0) language = null;
					}
					final String url = querystring != null ? String.format(API_FULL_SEARCH, language == null ? "und" : language, URLEncoder.encode(querystring, "UTF-8"))
						: (language != null ? String.format(API_RANDOM_SAMPLE_WITH_LANGUAGE, language) : API_RANDOM_SAMPLE);
					
					if(querystring != null)
					{
						final SQLiteQueryBuilder queryBuilder = new SQLiteQueryBuilder();
						queryBuilder.setTables("sentences join cache on sentences._id = cache.sentence_id");
						if(querystring != null)
							queryBuilder.appendWhere("cache.search = \'" + querystring + "\'");
						final Cursor cursor = queryBuilder.query(cachedb, projection, null, null, null, null, null);
						if(cursor.getCount() != 0)
						return cursor;
						cursor.close();
					}
					
					final HttpClient client = new DefaultHttpClient();                                                                                                                          
                    client.getParams().setParameter("Accept", "text/xml,application/xml");
		            final HttpGet get = new HttpGet(url);
		            get.addHeader("Accept", "text/xml,application/xml");
		            final HttpResponse responseGet = client.execute(get);
		            final InputStream response = responseGet.getEntity().getContent();

					
					
					final DocumentBuilderFactory documentBuilderFactory = DocumentBuilderFactory.newInstance();
					final DocumentBuilder documentBuilder = documentBuilderFactory.newDocumentBuilder();
					final Document document = documentBuilder.parse(response);
					document.getDocumentElement().normalize();
					final MatrixCursor cursor = new MatrixCursor(projection);
					
					final NodeList relation = document.getElementsByTagName("relation");
					for(int i = 0; i < relation.getLength(); ++i)
					{
						final Element sentenceset = (Element) relation.item(i);
						final NodeList mainsentences = sentenceset.getElementsByTagName("mainsentence");
						if(mainsentences.getLength() == 0) continue;
						final Element mainsentence = (Element) mainsentences.item(0);
						final ContentValues main_content = new ContentValues();
						final String main_id = mainsentence.getAttribute("id");
						final String main_lang = mainsentence.getAttribute("language");
						main_content.put("_id", main_id);
						main_content.put("lang", main_lang);
						
						final Node main_text_first_node = mainsentence.getFirstChild();
						if(main_text_first_node == null) continue;
						final String main_text = main_text_first_node.getNodeValue().trim();
						main_content.put("text", main_text);
						
						/*final NodeList main_texts = sentenceset.getElementsByTagName("text");
						if(main_texts.getLength() == 0) continue;
						final Node main_text_node = main_texts.item(0);
						final Node main_text_first_node = main_text_node.getFirstChild();
						if(main_text_first_node == null) continue;
						final String main_text = main_text_first_node.getNodeValue().trim();
						main_content.put("text", main_text);
						*/
						cachedb.replace("sentences", null, main_content);
						if(querystring != null)
						{
							final ContentValues cache_content = new ContentValues();
							cache_content.put("search", querystring);
							cache_content.put("sentence_id", main_id);
							cachedb.replace("cache", null, cache_content);
						}
						MatrixCursor.RowBuilder rowbuilder = cursor.newRow();
						for(String column : projection)
						{
							if(column.equals(LinkTable._ID))
								rowbuilder.add(main_id);
							else if(column.equals(LinkTable.LANGUAGE))
								rowbuilder.add(main_lang);
							else if(column.equals(LinkTable.TEXT))
								rowbuilder.add(main_text);
						}

						final NodeList sentences = sentenceset.getElementsByTagName("sentence");
						for(int j = 0; j < sentences.getLength(); ++j)
						{
							final Element sentence = (Element) sentences.item(j);
							final ContentValues slave_content = new ContentValues();
							final String slave_id = sentence.getAttribute("id");
							final String slave_lang = sentence.getAttribute("language");
							slave_content.put("_id", slave_id);
							slave_content.put("lang", slave_lang);

							final Node sentence_text_first_node = sentence.getFirstChild();
							if(sentence_text_first_node == null) continue;
							final String slave_text = sentence_text_first_node.getNodeValue();
							slave_content.put("text", slave_text);
							/*
							final NodeList sentence_texts = sentence.getElementsByTagName("text");
							if(sentence_texts.getLength() == 0) continue;
							final Node sentence_text_node = sentence_texts.item(0);
							final Node sentence_text_first_node = sentence_text_node.getFirstChild();
							if(sentence_text_first_node == null) continue;
							final String slave_text = sentence_text_first_node.getNodeValue();
							slave_content.put("text", slave_text);
							*/

							cachedb.replace("sentences", null, slave_content);

							final ContentValues link_content = new ContentValues();
							link_content.put("sentence_id", main_id);
							link_content.put("translation_id", slave_id);
							cachedb.replace(ProviderInterface.LINK_TABLE, null, link_content);
						}

					}
					return cursor;

				}
				case LINK:
				{
					final long link_id = Long.parseLong(uri.getLastPathSegment());
					final SQLiteQueryBuilder queryBuilder = new SQLiteQueryBuilder();
					queryBuilder.setTables(ProviderInterface.LINK_TABLE + " join sentences on _id = translation_id");
					queryBuilder.appendWhere("sentence_id = " + link_id);
					final Cursor cursor = queryBuilder.query(cachedb, projection, selection, selectionArgs, null, null, sortOrder);
					return cursor;
				}
				case RUBY:
				{
					return new MatrixCursor(projection);

				}
			}
		}
		catch(final SAXException e)
		{
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		catch(final ParserConfigurationException e)
		{
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		catch(final MalformedURLException e)
		{
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		catch(final IOException e)
		{
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		return null;

	}

	@Override
	public int update(final Uri arg0, final ContentValues arg1, final String arg2, final String[] arg3)
	{
		// TODO Auto-generated method stub
		return 0;
	}
}
