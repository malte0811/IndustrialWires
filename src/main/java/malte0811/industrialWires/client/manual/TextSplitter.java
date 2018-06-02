/*
 * This file is part of Industrial Wires.
 * Copyright (C) 2016-2018 malte0811
 * Industrial Wires is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * Industrial Wires is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * You should have received a copy of the GNU General Public License
 * along with Industrial Wires.  If not, see <http://www.gnu.org/licenses/>.
 */

package malte0811.industrialWires.client.manual;

import blusunrize.lib.manual.IManualPage;
import blusunrize.lib.manual.ManualInstance;
import blusunrize.lib.manual.ManualPages;
import gnu.trove.map.TIntIntMap;
import gnu.trove.map.TIntObjectMap;
import gnu.trove.map.hash.TIntIntHashMap;
import gnu.trove.map.hash.TIntObjectHashMap;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

public class TextSplitter {
	private final Function<String, Integer> width;
	private final TIntObjectMap<Map<Integer, Page>> specialByAnchor = new TIntObjectHashMap<>();
	private final TIntObjectMap<Page> specialByPage = new TIntObjectHashMap<>();
	private final List<List<String>> entry = new ArrayList<>();
	private final Function<String, String> tokenTransform;
	private final int lineWidth;
	private final int linesPerPage;
	private TIntIntMap pageByAnchor = new TIntIntHashMap();
	private final Function<String, IManualPage> defaultPage;

	public TextSplitter(Function<String, Integer> w, int lineWidthPixel, int linesPerPage,
						Function<String, IManualPage> defPage, Function<String, String> tokenTransform) {
		width = w;
		this.lineWidth = lineWidthPixel;
		this.linesPerPage = linesPerPage;
		this.tokenTransform = tokenTransform;
		this.defaultPage = defPage;
	}

	public TextSplitter(ManualInstance m) {
		this(m.fontRenderer::getStringWidth, 120, 16, s-> new ManualPages.Text(m, s), (s) -> s);
	}

	public TextSplitter(ManualInstance m, Function<String, String> tokenTransform) {
		this(m.fontRenderer::getStringWidth, 120, 16,s-> new ManualPages.Text(m, s), tokenTransform);
	}

	public void clearSpecialByPage() {
		specialByPage.clear();
	}

	public void clearSpecialByAnchor() {
		specialByAnchor.clear();
	}

	public void addSpecialPage(int ref, int offset, int lines, Function<String, IManualPage> factory) {
		if (offset < 0 || (ref != -1 && ref < 0)) {
			throw new IllegalArgumentException();
		}
		if (!specialByAnchor.containsKey(ref)) {
			specialByAnchor.put(ref, new HashMap<>());
		}
		specialByAnchor.get(ref).put(offset, new Page(lines, factory));
	}

	// I added labels to all break statements to make it more readable
	@SuppressWarnings({"UnnecessaryLabelOnBreakStatement", "UnusedLabel"})
	public void split(String in) {
		clearSpecialByPage();
		entry.clear();
		String[] wordsAndSpaces = splitWhitespace(in);
		int pos = 0;
		List<String> overflow = new ArrayList<>();
		updateSpecials(-1, 0, 0);
		entry:
		while (pos < wordsAndSpaces.length) {
			List<String> page = new ArrayList<>(overflow);
			overflow.clear();
			page:
			while (page.size() < getLinesOnPage(entry.size()) && pos < wordsAndSpaces.length) {
				String line = "";
				int currWidth = 0;
				line:
				while (pos < wordsAndSpaces.length && currWidth < lineWidth) {
					String token = tokenTransform.apply(wordsAndSpaces[pos]);
					int textWidth = getWidth(token);
					if (currWidth + textWidth < lineWidth || line.length() == 0) {
						pos++;
						if (token.equals("<np>")) {
							page.add(line);
							break page;
						} else if (token.equals("<br>")) {
							break line;
						} else if (token.startsWith("<&") && token.endsWith(">")) {
							int id = Integer.parseInt(token.substring(2, token.length() - 1));
							int pageForId = entry.size();
							Map<Integer, Page> specialForId = specialByAnchor.get(id);
							if (specialForId != null && specialForId.containsKey(0)) {
								if (page.size() > getLinesOnPage(pageForId)) {
									pageForId++;
								}
							}
							//New page if there is already a special element on this page
							if (updateSpecials(id, pageForId, page.size())) {
								page.add(line);
								pos--;
								break page;
							}
						} else if (!Character.isWhitespace(token.charAt(0)) || line.length() != 0) {//Don't add whitespace at the start of a line
							line += token;
							currWidth += textWidth;
						}
					} else {
						break line;
					}
				}
				line = line.trim();
				if (!line.isEmpty())
					page.add(line);
			}
			if (!page.stream().allMatch(String::isEmpty)) {
				int linesMax = getLinesOnPage(entry.size());
				if (page.size() > linesMax) {
					overflow.addAll(page.subList(linesMax, page.size()));
					page = page.subList(0, linesMax);
				}
				entry.add(page);
			}
		}
	}

	public List<IManualPage> toManualEntry() {
		List<IManualPage> ret = new ArrayList<>(entry.size());
		for (int i = 0; i < entry.size(); i++) {
			String s = entry.get(i).stream().collect(Collectors.joining("\n"));
			if (specialByPage.containsKey(i)) {
				ret.add(specialByPage.get(i).factory.apply(s));
			} else {
				ret.add(defaultPage.apply(s));
			}
		}
		return ret;
	}

	private int getWidth(String text) {
		switch (text) {
			case "<br>":
			case "<np>":
				return 0;
			default:
				if (text.startsWith("<link;")) {
					text = text.substring(text.indexOf(';') + 1);
					text = text.substring(text.indexOf(';') + 1, text.lastIndexOf(';'));
				}
				return width.apply(text);
		}
	}

	private int getLinesOnPage(int id) {
		if (specialByPage.containsKey(id)) {
			return specialByPage.get(id).lines;
		}
		return linesPerPage;
	}

	private boolean updateSpecials(int ref, int page, int currLine) {
		if (specialByAnchor.containsKey(ref)) {
			TIntObjectMap<Page> specialByPageTmp = new TIntObjectHashMap<>();
			for (Map.Entry<Integer, Page> entry : specialByAnchor.get(ref).entrySet()) {
				int specialPage = page + entry.getKey();
				if (specialByPage.containsKey(specialPage)) {
					return true;
				}
				if (entry.getKey()==0&&entry.getValue().lines<=currLine) {
					return true;
				}
				specialByPageTmp.put(specialPage, entry.getValue());
			}
			specialByPage.putAll(specialByPageTmp);
		} else if (ref != -1) {//Default reference for page 0
			System.out.println("WARNING: Reference " + ref + " was found, but no special pages were registered for it");
		}
		pageByAnchor.put(ref, page);
		return false;
	}

	private String[] splitWhitespace(String in) {
		List<String> parts = new ArrayList<>();
		for (int i = 0; i < in.length(); ) {
			StringBuilder here = new StringBuilder();
			char first = in.charAt(i);
			here.append(first);
			i++;
			for (; i < in.length(); ) {
				char hereC = in.charAt(i);
				byte action = shouldSplit(first, hereC);
				if ((action & 1) != 0) {
					here.append(in.charAt(i));
					i++;
				}
				if ((action & 2) != 0 || (action & 1) == 0) {
					break;
				}
			}
			parts.add(here.toString());
		}
		return parts.toArray(new String[0]);
	}

	/**
	 * @return &1: add
	 * &2: end here
	 */
	private byte shouldSplit(char start, char here) {
		byte ret = 0b01;
		if (Character.isWhitespace(start) ^ Character.isWhitespace(here)) {
			ret = 0b10;
		}
		if (here == '<') {
			ret = 0b10;
		}
		if (start == '<') {
			ret = 0b01;
			if (here == '>') {
				ret |= 0b10;
			}
		}
		return ret;
	}

	private class Page {
		final int lines;
		final Function<String, IManualPage> factory;
		public Page(int l, Function<String, IManualPage> f) {
			factory = f;
			lines = l;
		}
	}
}