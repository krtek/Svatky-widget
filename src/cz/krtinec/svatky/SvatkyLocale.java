/*
 * This file is part of Svatky Widget.
 *
 * Svatky Widget is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Svatky Widget is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with Svatky Widget.  If not, see <http://www.gnu.org/licenses/>.
 *
 * Copyright (c) Lukas Marek, 2011.
 */

package cz.krtinec.svatky;

public enum SvatkyLocale {
	cs("cs"), sk("sk");
	
	String abbr; 
	
	private SvatkyLocale(String abbr) {
		this.abbr = abbr;
	}

	@Override
	public String toString() {
		return abbr;
	}
	
	
}
