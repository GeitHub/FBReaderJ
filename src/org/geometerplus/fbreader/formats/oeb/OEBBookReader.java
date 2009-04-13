/*
 * Copyright (C) 2007-2009 Geometer Plus <contact@geometerplus.com>
 *
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA
 * 02110-1301, USA.
 */

package org.geometerplus.fbreader.formats.oeb;

import java.util.*;

import org.geometerplus.zlibrary.core.xml.*;
import org.geometerplus.zlibrary.core.filesystem.ZLFile;
import org.geometerplus.zlibrary.core.image.ZLFileImage;

import org.geometerplus.fbreader.bookmodel.*;
import org.geometerplus.fbreader.formats.xhtml.XHTMLReader;
import org.geometerplus.fbreader.formats.util.MiscUtil;
import org.geometerplus.fbreader.constants.XMLNamespace;

class Reference {
	public final String Title;
	public final String HRef;

	public Reference(String title, String href) {
		Title = title;
		HRef = href;
	}
}

class OEBBookReader extends ZLXMLReaderAdapter implements XMLNamespace {
	private static final char[] Dots = new char[] {'.', '.', '.'};

	private final BookReader myModelReader;
	private final HashMap<String,String> myIdToHref = new HashMap<String,String>();
	private final ArrayList<String> myHtmlFileNames = new ArrayList<String>();
	private final ArrayList<Reference> myTourTOC = new ArrayList<Reference>();
	private final ArrayList<Reference> myGuideTOC = new ArrayList<Reference>();

	private String myOPFSchemePrefix;
	private String myFilePrefix;
	private String myNCXTOCFileName;

	OEBBookReader(BookModel model) {
		myModelReader = new BookReader(model);
	}

	boolean readBook(String fileName) {
		myFilePrefix = MiscUtil.htmlDirectoryPrefix(fileName);

		myIdToHref.clear();
		myHtmlFileNames.clear();
		myNCXTOCFileName = null;
		myTourTOC.clear();
		myGuideTOC.clear();
		myState = READ_NONE;

		if (!read(fileName)) {
			return false;
		}

		myModelReader.setMainTextModel();
		myModelReader.pushKind(FBTextKind.REGULAR);

		for (String name : myHtmlFileNames) {
			new XHTMLReader(myModelReader).readFile(myFilePrefix + name, name);
		}

		generateTOC();

		return true;
	}

	private void generateTOC() {
		if (myNCXTOCFileName != null) {
			NCXReader ncxReader = new NCXReader(myModelReader);
			if (ncxReader.read(myFilePrefix + myNCXTOCFileName)) {
				final Map<Integer,NCXReader.NavPoint> navigationMap = ncxReader.navigationMap();
				if (!navigationMap.isEmpty()) {
					int level = 0;
					for (NCXReader.NavPoint point : navigationMap.values()) {
						final BookModel.Label label = myModelReader.Model.getLabel(point.ContentHRef);
						int index = (label != null) ? label.ParagraphIndex : -1;
						while (level > point.Level) {
							myModelReader.endContentsParagraph();
							--level;
						}
						while (++level <= point.Level) {
							myModelReader.beginContentsParagraph(-2);
							myModelReader.addContentsData(Dots);
						}
						myModelReader.beginContentsParagraph(index);
						myModelReader.addContentsData(point.Text.toCharArray());
					}
					while (level > 0) {
						myModelReader.endContentsParagraph();
						--level;
					}
					return;
				}
			}
		}

		for (Reference ref : myTourTOC.isEmpty() ? myGuideTOC : myTourTOC) {
			final BookModel.Label label = myModelReader.Model.getLabel(ref.HRef);
			if (label != null) {
				final int index = label.ParagraphIndex;
				if (index != -1) {
					myModelReader.beginContentsParagraph(index);
					myModelReader.addContentsData(ref.Title.toCharArray());
					myModelReader.endContentsParagraph();
				}
			}
		}
	}

	private static final String MANIFEST = "manifest";
	private static final String SPINE = "spine";
	private static final String GUIDE = "guide";
	private static final String TOUR = "tour";
	private static final String SITE = "site";
	private static final String REFERENCE = "reference";
	private static final String ITEMREF = "itemref";
	private static final String ITEM = "item";

	private static final String COVER_IMAGE = "other.ms-coverimage-standard";

	private static final int READ_NONE = 0;
	private static final int READ_MANIFEST = 1;
	private static final int READ_SPINE = 2;
	private static final int READ_GUIDE = 3;
	private static final int READ_TOUR = 4;
	
	private int myState;

	public boolean startElementHandler(String tag, ZLStringMap xmlattributes) {
		tag = tag.toLowerCase();
		if ((myOPFSchemePrefix != null) && tag.startsWith(myOPFSchemePrefix)) {
			tag = tag.substring(myOPFSchemePrefix.length());
		}
		tag = tag.intern();
		if (MANIFEST == tag) {
			myState = READ_MANIFEST;
		} else if (SPINE == tag) {
			myNCXTOCFileName = myIdToHref.get(xmlattributes.getValue("toc"));
			myState = READ_SPINE;
		} else if (GUIDE == tag) {
			myState = READ_GUIDE;
		} else if (TOUR == tag) {
			myState = READ_TOUR;
		} else if ((myState == READ_MANIFEST) && (ITEM == tag)) {
			final String id = xmlattributes.getValue("id");
			final String href = xmlattributes.getValue("href");
			if ((id != null) && (href != null)) {
				myIdToHref.put(id, href);
			}
		} else if ((myState == READ_SPINE) && (ITEMREF == tag)) {
			final String id = xmlattributes.getValue("idref");
			if (id != null) {
				final String fileName = myIdToHref.get(id);
				if (fileName != null) {
					myHtmlFileNames.add(fileName);
				}
			}
		} else if ((myState == READ_GUIDE) && (REFERENCE == tag)) {
			final String type = xmlattributes.getValue("type");
			final String title = xmlattributes.getValue("title");
			final String href = xmlattributes.getValue("href");
			if (href != null) {
				if (title != null) {
					myGuideTOC.add(new Reference(title, href));
				}
				if ((type != null) && (COVER_IMAGE.equals(type))) {
					myModelReader.setMainTextModel();
					myModelReader.addImageReference(href, (short)0);
					myModelReader.addImage(href, new ZLFileImage("image/auto", myFilePrefix + href));
				}
			}
		} else if ((myState == READ_TOUR) && (SITE == tag)) {
			final String title = xmlattributes.getValue("title");
			final String href = xmlattributes.getValue("href");
			if ((title != null) && (href != null)) {
				myTourTOC.add(new Reference(title, href));
			}
		}
		return false;
	}

	public boolean endElementHandler(String tag) {
		tag = tag.toLowerCase();
		if ((myOPFSchemePrefix != null) && tag.startsWith(myOPFSchemePrefix)) {
			tag = tag.substring(myOPFSchemePrefix.length());
		}
		tag = tag.intern();
		if ((MANIFEST == tag) || (SPINE == tag) || (GUIDE == tag) || (TOUR == tag)) {
			myState = READ_NONE;
		}
		return false;
	}

	public boolean processNamespaces() {
		return true;
	}

	public void namespaceListChangedHandler(HashMap namespaces) {
		myOPFSchemePrefix = null;
		for (Object o : namespaces.keySet()) {
			if (OpenPackagingFormat.equals(o)) {
				myOPFSchemePrefix = namespaces.get(o) + ":";
			}
		}
	}

	public boolean dontCacheAttributeValues() {
		return true;
	}
}