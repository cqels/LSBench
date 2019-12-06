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
package sib.util.csv2ttl;

import sib.util.Nameable;
import sib.util.json.impl.AutoJsonObject;
import sib.util.json.impl.IgnoreCase;
import sib.util.json.impl.Sequence;


@IgnoreCase
@Sequence(value={"name", "type", "prop", "refto"})
public class CsvField extends AutoJsonObject implements Nameable {
    String name;
	DataType type;
	boolean prop=true;
	String refto=null;

	public CsvField() {
	}

    @Override
    public String getName() {
        return name;
    }

    public DataType getType() {
        return type;
    }

    public void setType(String value) {
        this.type = DataType.valueOf((value).toUpperCase());
    }

    public boolean isProp() {
        return prop;
    }

    public void setProp(String value) {
        this.prop = Boolean.valueOf(value);
    }

    public String getRefto() {
        return refto;
    }

    public void setRefto(String refto) {
        this.refto = refto;
    }

    public void setName(String name) {
        this.name = name;
    }
}
