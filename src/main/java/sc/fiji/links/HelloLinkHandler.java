/*-
 * #%L
 * Standard fiji:// URL handlers.
 * %%
 * Copyright (C) 2025 - 2026 Fiji developers.
 * %%
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 * 
 * 1. Redistributions of source code must retain the above copyright notice,
 *    this list of conditions and the following disclaimer.
 * 2. Redistributions in binary form must reproduce the above copyright notice,
 *    this list of conditions and the following disclaimer in the documentation
 *    and/or other materials provided with the distribution.
 * 
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
 * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
 * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE
 * ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDERS OR CONTRIBUTORS BE
 * LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR
 * CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF
 * SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS
 * INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN
 * CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
 * ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE
 * POSSIBILITY OF SUCH DAMAGE.
 * #L%
 */

package sc.fiji.links;

import org.scijava.links.AbstractLinkHandler;
import org.scijava.links.LinkHandler;
import org.scijava.links.Links;
import org.scijava.log.LogLevel;
import org.scijava.log.LogService;
import org.scijava.plugin.Parameter;
import org.scijava.plugin.Plugin;
import org.scijava.ui.UIService;

import java.net.URI;
import java.util.Map;

/** Handles {@code fiji://hello} links. */
@Plugin(type = LinkHandler.class)
public class HelloLinkHandler extends AbstractLinkHandler {

	@Parameter(required = false)
	private LogService log;

	@Parameter(required = false)
	private UIService ui;

	@Override
	public boolean supports(final URI uri) {
		return "fiji".equals(uri.getScheme()) &&
			uri.getHost().equals("hello");
	}

	@Override
	public void handle(final URI uri) {
		if (!supports(uri)) throw new UnsupportedOperationException("" + uri);

		var operation = Links.operation(uri);
		var query = Links.query(uri);
		var greeting = query.getOrDefault("greeting", "Hello!");

		var levelStr = query.getOrDefault("level", "info");
		int level = LogLevel.value(levelStr);

		if ("print".equals(operation)) {
			System.out.println(greeting);
		}
		else if ("log".equals(operation)) {
			if (log != null) log.log(level, greeting);
		}
		else if ("dialog".equals(operation)) {
			if (ui != null) ui.showDialog(greeting);
		}
		else {
			throw new UnsupportedOperationException(
				"Unknown hello operation: " + operation);
		}
	}

	public static void main(String[] args) throws Exception {
		String s = "fiji://run/plugin?p=Bio-Formats&arg=open%3D%2Fhome%2Fcurtis%2Fdata%2Fimagej%2FFluorescentCells.jpg%20color_mode%3DDefault%20rois_import%3D%5BROI%20manager%5D%20view%3DHyperstack%20stack_order%3DXYCZT%20use_virtual_stack";
		URI uri = new URI(s);
		Map<String, String> query = Links.query(uri);
		System.out.println(query.get("arg"));
	}
}
