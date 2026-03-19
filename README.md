[![Build Status](https://github.com/fiji/fiji-links/actions/workflows/build.yml/badge.svg)](https://github.com/fiji/fiji-links/actions/workflows/build.yml)

This package implements support for fiji:// links,
built on [scijava-desktop](https://github.com/scijava/scijava-desktop).
Support can be extended via `LinkHandler` plugins.

## Supported link commands

### `fiji://open` — open data

| Link | Description |
|------|-------------|
| `fiji://open/file?p=<path>` | Open a local file (e.g. `/path/to/image.tif`) |
| `fiji://open/url?p=<url>` | Open a remote URL (e.g. `https://example.com/image.tif`) |
| `fiji://open/source?p=<source>` | Open a data source, auto-detecting its type |

### `fiji://hello` — diagnostic/testing

| Link | Description |
|------|-------------|
| `fiji://hello/print?greeting=<text>` | Print a message to stdout |
| `fiji://hello/log?greeting=<text>&level=<level>` | Log a message at the given log level (e.g. `info`, `warn`, `error`) |
| `fiji://hello/dialog?greeting=<text>` | Show a message in a dialog box |
