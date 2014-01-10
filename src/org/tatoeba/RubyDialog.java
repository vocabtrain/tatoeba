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

package org.tatoeba;

import android.app.Activity;
import android.app.Dialog;
import android.view.Window;
import android.webkit.WebSettings;
import android.webkit.WebView;

public class RubyDialog extends Dialog
{

	@SuppressWarnings("deprecation")
	protected RubyDialog(final Activity activity, final String html)
	{
		super(activity, true, null);
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		setContentView(R.layout.ruby_dialog);
		final WebView webview = (WebView) this.findViewById(R.id.rubydialog_view);
		final WebSettings settings = webview.getSettings();
		settings.setSupportZoom(true);
		settings.setBuiltInZoomControls(true);
		settings.setTextSize(WebSettings.TextSize.LARGEST);
		webview.loadDataWithBaseURL(null, html, "text/html", "utf-8", null);
	}

}
