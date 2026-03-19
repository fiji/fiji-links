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

import io.scif.SCIFIO;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.scijava.Named;
import org.scijava.Priority;
import org.scijava.desktop.links.LinkService;
import org.scijava.event.EventHandler;
import org.scijava.event.EventService;
import org.scijava.object.event.ObjectEvent;
import org.scijava.plugin.Plugin;
import org.scijava.ui.UIService;
import org.scijava.ui.UserInterface;
import org.scijava.ui.headless.HeadlessUI;

import java.io.File;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests {@link OpenLinkHandler}.
 *
 * @author Curtis Rueden
 */
public class OpenLinkHandlerTest {

	private static final String IMAGE_URL =
		"https://imagej.net/images/colors.gif";

	private static SCIFIO scifio;

	@Plugin(type = UserInterface.class, name = "mock", priority = Priority.HIGH)
	public static class MockUI extends HeadlessUI {
		public final List<Object> shown = new ArrayList<>();

		@Override
		public void show(final String name, final Object o) {
			shown.add(o);
		}
	}

	private static class EventTracker {
		public final List<Object> objects = new ArrayList<>();

		@EventHandler
		public void onEvent(ObjectEvent evt) {
			objects.add(evt.getObject());
		}
	}

	private static final EventTracker events = new EventTracker();

	@BeforeAll
	public static void setUp() {
		scifio = new SCIFIO();
		var eventService = scifio.get(EventService.class);
		eventService.subscribe(events);
	}

	@AfterAll
	public static void tearDown() {
		scifio.dispose();
	}

	@Test
	public void testOpenFile() throws URISyntaxException {
		var linkService = scifio.get(LinkService.class);
		var tableURL = getClass().getResource("molitor.csv");
		assertNotNull(tableURL);
		var tableFile = new File(tableURL.toURI());
		assertEquals("molitor.csv", tableFile.getName());
		var uri = new URI("fiji://open/file?p=" +
			URLEncoder.encode(tableFile.getAbsolutePath(), StandardCharsets.UTF_8));
		var ui = (MockUI) scifio.get(UIService.class).getDefaultUI();
		ui.shown.clear();
		linkService.handle(uri);
		assertEquals(1, ui.shown.size());
        //noinspection unchecked
        var table = (List<List<Double>>) ui.shown.get(0);
		assertEquals(3, table.size()); // 3 columns
		var years = table.get(0);
		var ages = table.get(1);
		var avgs = table.get(2);
		assertEquals(Arrays.asList(
			1978., 1979., 1980., 1981., 1982., 1983., 1984.,
			1985., 1986., 1987., 1988., 1989., 1990., 1991.,
			1992., 1993., 1994., 1995., 1996., 1997., 1998.
		), years);
		assertEquals(Arrays.asList(
			21., 22., 23., 24., 25., 26., 27., 28., 29., 30., 31.,
			32., 33., 34., 35., 36., 37., 38., 39., 40., 41.
		), ages);
		assertEquals(Arrays.asList(
			.273, .322, .304, .267, .302, .270, .217, .297, .281, .353, .312,
			.315, .285, .325, .320, .332, .341, .270, .341, .305, .281
		), avgs);
	}

	@Test
	public void testOpenURL() throws URISyntaxException {
		// Note: DefaultTableIOPlugin only opens content from files, not URLs.
		// So we cannot use the CSV for this test. But scifio+scijava-io-http
		// can open certain image formats from HTTPS, so we use an image here.
		var linkService = scifio.get(LinkService.class);
		var uri = new URI("fiji://open/url?p=" +
			URLEncoder.encode(IMAGE_URL, StandardCharsets.UTF_8));
		events.objects.clear();
		linkService.handle(uri);
		assertEquals(1, events.objects.size());
		assertObject("net.imagej.DefaultDataset",
			"colors.gif", events.objects.get(0));
	}

	@Test
	public void testOpenSource() throws URISyntaxException {
		var linkService = scifio.get(LinkService.class);
		var uri = new URI("fiji://open/source?p=" +
			URLEncoder.encode(IMAGE_URL, StandardCharsets.UTF_8));
		events.objects.clear();
		linkService.handle(uri);
		assertEquals(1, events.objects.size());
		assertObject("net.imagej.DefaultDataset",
				"colors.gif", events.objects.get(0));
	}

	private void assertObject(String className, String objName, Object o) {
		assertEquals(className, o.getClass().getName());
		assertInstanceOf(Named.class, o);
		assertEquals(objName, ((Named) o).getName());
	}
}
