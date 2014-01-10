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

import java.util.Collection;
import java.util.Iterator;

public final class StringUtils
{
	private static final char[] hexChar = { '0', '1', '2', '3', '4', '5', '6', '7', '8', '9', 'A', 'B', 'C', 'D', 'E', 'F' };

	public static <T> String[] createArray(final Collection<T> c)
	{
		final String[] a = new String[c.size()];
		final Iterator<T> it = c.iterator();
		int i = 0;
		while(it.hasNext())
			a[i++] = it.next().toString();
		return a;
	}

	public static String[] createStringArray(final Long[] src)
	{
		final String[] a = new String[src.length];
		for(int i = 0; i < a.length; ++i)
			a[i] = "" + src[i];
		return null;
	}

	public static String generateQuestionTokens(final int length)
	{
		if(length == 0) return null;
		final StringBuilder sb = new StringBuilder(" IN ( ?");
		for(int i = 1; i < length; ++i)
			sb.append(",?");
		sb.append(")");
		return sb.toString();
	}

	public static String join(final int[] array, final String delimeter)
	{
		if(array.length == 0) return null;
		final StringBuilder sb = new StringBuilder("" + array[0]);
		for(int i = 1; i < array.length; ++i)
		{
			sb.append(delimeter).append("" + array[i]);
		}
		return sb.toString();
	}

	public static String join(final String[] string)
	{
		return join(string, ",");
	}

	public static String join(final String[] string, final String delimeter)
	{
		if(string.length == 0) return null;
		final StringBuilder sb = new StringBuilder(string[0]);
		for(int i = 1; i < string.length; ++i)
		{
			sb.append(delimeter).append(string[i]);
		}
		return sb.toString();
	}

	public static <T> int linearSearch(final T[] array, final T search)
	{
		for(int i = 0; i < array.length; ++i)
		{
			if(array[i].equals(search)) return i;
		}
		return -1;
	}

	public static int[] splitInts(final String string, final String delimeter)
	{
		try
		{
			final String[] array = string.split(delimeter);
			final int[] iarray = new int[array.length];
			for(int i = 0; i < array.length; ++i)
			{
				iarray[i] = Integer.parseInt(array[i]);
			}
			return iarray;
		}
		catch(final NumberFormatException e)
		{
			return null;
		}
	}

	// from http://www.xinotes.org/notes/note/812/
	public static String unicodeEscape(final String s)
	{
		final StringBuilder sb = new StringBuilder();
		for(int i = 0; i < s.length(); i++)
		{
			final char c = s.charAt(i);
			if((c >> 7) > 0)
			{
				sb.append("\\u");
				sb.append(hexChar[(c >> 12) & 0xF]); // append the hex character for the left-most 4-bits
				sb.append(hexChar[(c >> 8) & 0xF]); // hex for the second group of 4-bits from the left
				sb.append(hexChar[(c >> 4) & 0xF]); // hex for the third group
				sb.append(hexChar[c & 0xF]); // hex for the last group, e.g., the right most 4-bits
			}
			else
			{
				sb.append(c);
			}
		}
		return sb.toString();
	}

}
