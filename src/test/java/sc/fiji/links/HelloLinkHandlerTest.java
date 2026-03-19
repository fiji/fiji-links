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

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.scijava.SciJava;
import org.scijava.console.ConsoleService;
import org.scijava.console.OutputEvent;
import org.scijava.desktop.links.LinkService;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests {@link HelloLinkHandler}.
 *
 * @author Curtis Rueden
 */
public class HelloLinkHandlerTest {

	private static SciJava gateway;

	@BeforeAll
	public static void setUp() {
		gateway = new SciJava();
	}

	@AfterAll
	public static void tearDown() {
		gateway.dispose();
	}

	@Test
	public void testHelloPrint() throws URISyntaxException {
		var linkService = gateway.get(LinkService.class);
		var consoleService = gateway.get(ConsoleService.class);
		var output = new ArrayList<OutputEvent>();
		consoleService.addOutputListener(output::add);

		var uri = new URI("fiji://hello/print?greeting=Howdy");
		linkService.handle(uri);

		assertFalse(output.isEmpty());
		assertTrue(output.get(0).isStdout());
		assertEquals("Howdy", output.get(0).getOutput());
	}

	@Test
	public void testHelloLog() throws URISyntaxException {
		var linkService = gateway.get(LinkService.class);
		var consoleService = gateway.get(ConsoleService.class);
		var output = new ArrayList<OutputEvent>();
		consoleService.addOutputListener(output::add);

		var uri = new URI("fiji://hello/log?level=w&greeting=Cuidado!");
		linkService.handle(uri);

		assertFalse(output.isEmpty());
		assertTrue(output.get(0).isStderr());
		assertEquals("[WARNING] Cuidado!", output.get(0).getOutput().trim());
	}
}
