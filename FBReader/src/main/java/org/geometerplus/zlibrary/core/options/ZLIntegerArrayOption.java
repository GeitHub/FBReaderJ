/*
 * Copyright (C) 2007-2012 Geometer Plus <contact@geometerplus.com>
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

package org.geometerplus.zlibrary.core.options;

public final class ZLIntegerArrayOption extends ZLOption {
	public final int MinValue;
	public final int MaxValue;
	public final int[] Values;

	private final int myDefaultValue;
	private int myValue;

	public ZLIntegerArrayOption(String group, String optionName, int[] values, int defaultValue) {
		super(group, optionName);
		Values = values;
		MinValue = values[0];
		MaxValue = values[values.length - 1];
		if (defaultValue < MinValue) {
			defaultValue = MinValue;
		} else if (defaultValue > MaxValue) {
			defaultValue = MaxValue;
		}
		myDefaultValue = defaultValue;
		myValue = defaultValue;
	}

	/**
	 * Get the Values array index of a value (or closest value less than)
	 * 
	 * @param value
	 * @return
	 */
	public int getValueIndex(int value) {
		for (int i = 0; i < Values.length - 1; i++) {
			if (value >= Values[i] && value < Values[i + 1]) {
				return i;
			}
		}
		return -1;
	}
	
	

	public void zoom(int delta) {
		int index = getValueIndex(getValue());
		int newIndex = index + delta;
		if (newIndex < 0) {
			newIndex = 0;
		}
		if (newIndex > Values.length - 1) {
			newIndex = Values.length - 1;
		}
		setValue(Values[newIndex]);
	}

	public int getValue() {
		if (!myIsSynchronized) {
			String value = getConfigValue(null);
			if (value != null) {
				try {
					int intValue = Integer.parseInt(value);
					if (intValue < MinValue) {
						intValue = MinValue;
					} else if (intValue > MaxValue) {
						intValue = MaxValue;
					}
					myValue = intValue;
				} catch (NumberFormatException e) {
				}
			}
			myIsSynchronized = true;
		}
		return myValue;
	}

	public void setValue(int value) {
		if (value < MinValue) {
			value = MinValue;
		} else if (value > MaxValue) {
			value = MaxValue;
		}
		if (myIsSynchronized && (myValue == value)) {
			return;
		}
		myValue = value;
		myIsSynchronized = true;
		if (value == myDefaultValue) {
			unsetConfigValue();
		} else {
			setConfigValue("" + value);
		}
	}
}
