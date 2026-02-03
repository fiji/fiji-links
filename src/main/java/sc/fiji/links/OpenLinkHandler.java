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

import org.scijava.io.IOService;
import org.scijava.io.location.FileLocation;
import org.scijava.io.location.Location;
import org.scijava.io.location.LocationService;
import org.scijava.io.location.URLLocation;
import org.scijava.links.AbstractLinkHandler;
import org.scijava.links.LinkHandler;
import org.scijava.links.Links;
import org.scijava.log.LogService;
import org.scijava.plugin.Parameter;
import org.scijava.plugin.Plugin;
import org.scijava.ui.UIService;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.Arrays;

/** Handles {@code fiji://open} links. */
@Plugin(type = LinkHandler.class)
public class OpenLinkHandler extends AbstractLinkHandler {

	@Parameter(required = false)
	private LogService log;

	@Parameter(required = false)
	private LocationService loc;

	@Parameter
	private IOService io;

	@Parameter(required = false)
	private UIService ui;

	@Override
	public boolean supports(final URI uri) {
		var operations = Arrays.asList("file", "url", "source");
		return "fiji".equals(uri.getScheme()) &&
			uri.getHost().equals("open") &&
			operations.contains(Links.operation(uri));
	}

	@Override
	public void handle(final URI uri) {
		if (!supports(uri)) throw new UnsupportedOperationException("" + uri);

		var operation = Links.operation(uri);
		var query = Links.query(uri);
		var p = query.get("p");
		if (p == null) {
			if (log != null) log.error("No path given for URI: " + uri);
			return;
		}

		try {
			if ("file".equals(operation)) {
				// fiji://open/file?p=/path/to/local-image.tif
				handleOpen(new FileLocation(p));
			}
			else if ("url".equals(operation)) {
				// fiji://open/url?p=https://example.com/path/to/remote-image.tif
				var url = new URL(p); // Validate the URL syntax.
				// Note: Instead of hardcoding URLLocation here, we lean on the
				// location resolver to convert the URL string into a Location
				// of the proper type. We do this because there is another
				// Location implementation, org.scijava.io.http.HTTPLocation,
				// which offers better random-access behavior than URLLocation,
				// and generally speaking, there may be other URL-compatible
				// location plugins available on the classpath.
				final Location location;
				if (loc == null) {
					if (log != null) {
						log.warn("No location service; falling back " +
							"to hardcoded URLLocation for URI: " + uri);
					}
					location = new URLLocation(url);
				}
				else location = loc.resolve(url.toString());
				handleOpen(location);
			}
			else if ("source".equals(operation)) {
				// Auto-detect data source type from string content.
				if (loc == null) {
					if (log != null) {
						log.error("No location service; " +
							"cannot resolve source string from URI: " + uri);
					}
					return;
				}
				handleOpen(loc.resolve(p));
			}
			else {
				throw new UnsupportedOperationException(
					"Unknown open operation: " + operation);
			}
		}
		catch (IOException | URISyntaxException e) {
			if (log != null) log.error(e);
		}
	}

	private void handleOpen(Location location) throws IOException {
		Object result = io.open(location);
		if (ui != null) ui.show(result);
	}
}
