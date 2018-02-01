/*
 * This file is part of Industrial Wires.
 * Copyright (C) 2016-2017 malte0811
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

import blusunrize.lib.manual.ManualPages;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

public class TextSplitter {
	private final Function<String, Integer> width;
	private final int lineWidth;
	private Map<Integer, Map<Integer, Page>> linesOnSpecialPages = new HashMap<>();
	private Map<Integer, Page> pageToSpecial = new HashMap<>();
	private List<List<String>> entry = new ArrayList<>();
	private Page defPage;
	public TextSplitter(Function<String, Integer> w, int lP, int lW, Function<String, ManualPages> defaultPage) {
		width = w;
		lineWidth = lW;
		defPage = new Page(lP, defaultPage);
	}

	public void clearSpecial() {
		linesOnSpecialPages.clear();
	}

	public void addSpecialPage(int ref, int offset, int linesOnPage, Function<String, ManualPages> factory) {
		if (offset<0||(ref!=-1&&ref<0)) {
			throw new IllegalArgumentException();
		}
		if (!linesOnSpecialPages.containsKey(ref)) {
			linesOnSpecialPages.put(ref, new HashMap<>());
		}
		linesOnSpecialPages.get(ref).put(offset, new Page(linesOnPage, factory));
	}

	// I added labels to all break statements to make it more readable
	@SuppressWarnings({"UnnecessaryLabelOnBreakStatement", "UnusedLabel"})
	public void split(String in) {
		String[] wordsAndSpaces = splitWhitespace(in);
		int pos = 0;
		List<String> overflow = new ArrayList<>();
		updateSpecials(-1, 0);
		entry:while (pos<wordsAndSpaces.length) {
			List<String> page = new ArrayList<>();
			page.addAll(overflow);
			overflow.clear();
			page:while (page.size()<getLinesOnPage(entry.size())&&pos<wordsAndSpaces.length) {
				String line = "";
				int currWidth = 0;
				line:while (pos<wordsAndSpaces.length&&currWidth<lineWidth) {
					String text = wordsAndSpaces[pos];
					if (pos<wordsAndSpaces.length) {
						int textWidth = getWidth(text);
						if (currWidth + textWidth < lineWidth||line.length()==0) {
							pos++;
							if (text.equals("<np>")) {
								page.add(line);
								break page;
							} else if (text.equals("<br>")) {
								break line;
							} else if (text.startsWith("<&")&&text.endsWith(">")) {
								int id = Integer.parseInt(text.substring(2, text.length()-1));
								int pageForId = entry.size();
								Map<Integer, Page> specialForId = linesOnSpecialPages.get(id);
								if (specialForId!=null&&specialForId.containsKey(0)) {
									if (page.size()>getLinesOnPage(pageForId)) {
										pageForId++;
									}
								}
								updateSpecials(id, pageForId);
							} else if (!Character.isWhitespace(text.charAt(0))||line.length()!=0) {//Don't add whitespace at the start of a line
								line += text;
								currWidth += textWidth;
							}
						} else {
							break line;
						}
					}
				}
				page.add(line);
			}
			if (!page.stream().allMatch(String::isEmpty)) {
				int linesMax = getLinesOnPage(entry.size());
				if (page.size()>linesMax) {
					overflow.addAll(page.subList(linesMax, page.size()));
					page = page.subList(0, linesMax-1);
				}
				entry.add(page);
			}
		}
	}

	public List<ManualPages> toManualEntry() {
		List<ManualPages> ret = new ArrayList<>(entry.size());
		for (int i = 0; i < entry.size(); i++) {
			String s = entry.get(i).stream().collect(Collectors.joining("\n"));
			ret.add(pageToSpecial.getOrDefault(i, defPage).factory.apply(s));
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
		if (pageToSpecial.containsKey(id)) {
			return pageToSpecial.get(id).lines;
		}
		return defPage.lines;
	}

	private void updateSpecials(int ref, int page) {
		if (linesOnSpecialPages.containsKey(ref)) {
			for (Map.Entry<Integer, Page> entry :linesOnSpecialPages.get(ref).entrySet()) {
				int specialPage = page+entry.getKey();
				if (pageToSpecial.containsKey(specialPage)) {
					throw new IllegalStateException("Page "+specialPage+" was registered already");
				}
				pageToSpecial.put(specialPage, entry.getValue());
			}
		} else if (ref!=-1) {//Default reference for page 0
			System.out.println("WARNING: Reference "+ref+" was found, but no special pages were registered for it");
		}
	}

	private String[] splitWhitespace(String in) {
		List<String> parts = new ArrayList<>();
		for (int i = 0;i<in.length();) {
			StringBuilder here = new StringBuilder();
			char first = in.charAt(i);
			here.append(first);
			i++;
			for (;i<in.length();) {
				char hereC = in.charAt(i);
				byte action = shouldSplit(first, hereC);
				if ((action&1)!=0) {
					here.append(in.charAt(i));
					i++;
				}
				if ((action&2)!=0||(action&1)==0) {
					break;
				}
			}
			parts.add(here.toString());
		}
		return parts.toArray(new String[parts.size()]);
	}

	/**
	 * @return
	 * &1: add
	 * &2: end here
	 */
	private byte shouldSplit(char start, char here) {
		byte ret = 0b01;
		if (Character.isWhitespace(start)^Character.isWhitespace(here)) {
			ret = 0b10;
		}
		if (here=='<') {
			ret = 0b10;
		}
		if (start=='<') {
			ret = 0b01;
			if (here=='>') {
				ret |= 0b10;
			}
		}
		return ret;
	}

	public List<List<String>> getEntry() {
		return entry;
	}

	private class Page {
		final int lines;
		final Function<String, ManualPages> factory;
		public Page(int l, Function<String, ManualPages> f) {
			factory = f;
			lines = l;
		}
	}
}
