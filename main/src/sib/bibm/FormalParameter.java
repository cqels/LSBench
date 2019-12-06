/*
 *  Big Database Semantic Metric Tools
 *
 * Copyright (C) 2011 OpenLink Software <bdsmt@openlinksw.com>
 * All Rights Reserved.
 *
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation;  only Version 2 of the License dated
 * June 1991.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301, USA.
 */
package sib.bibm;

public class FormalParameter {
	public byte parameterType;

	public FormalParameter(byte classByte) {
		this.parameterType = classByte;
	}

    public  String getDefaultValue() {
        throw new UnsupportedOperationException(getClass().getName()+'.'+getDefaultValue());
    }
    
	/**
	 * to be overriden for Strings, to enquote
	 * @param param
	 * @return
	 */
    public String toString(Object param) {
        return param.toString();
    }
}
