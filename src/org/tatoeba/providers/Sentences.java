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
import java.util.LinkedList;
import java.util.List;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.queryparser.classic.ParseException;
import org.apache.lucene.queryparser.classic.QueryParser;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.ScoreDoc;
import org.apache.lucene.search.TopScoreDocCollector;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;
import org.tatoeba.providers.ProviderInterface.LinkTable;

public class Sentences
{
	private final String lucene_sentences_dir;

	Sentences(final String directory)
	{
		lucene_sentences_dir = directory + "/sentences/";
	}

	List<Long> findExamples(final String querystring, final String language, final int limit) throws ParseException, IOException
	{
		final Directory index = FSDirectory.open(new File(lucene_sentences_dir + language));
		final Analyzer analyzer = LuceneFunctions.getLanguageAnalyzer(language);
		final Query q = new QueryParser(LuceneFunctions.LUCENE_VERSION, LinkTable.TEXT, analyzer).parse(querystring);
		final IndexReader reader = DirectoryReader.open(index);
		final IndexSearcher searcher = new IndexSearcher(reader);
		final TopScoreDocCollector collector = TopScoreDocCollector.create(limit, true);
		searcher.search(q, collector);
		final ScoreDoc[] hits = collector.topDocs().scoreDocs;
		final List<Long> answers = new LinkedList<Long>();

		for(int i = 0; i < hits.length; ++i)
		{
			final int docId = hits[i].doc;
			answers.add(Long.parseLong(searcher.doc(docId).get(ProviderInterface.SearchTable._ID)));
		}
		reader.close();
		return answers;
	}
}