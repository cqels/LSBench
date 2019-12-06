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

import java.io.IOException;

import sib.util.json.JsonList;
import sib.util.json.JsonObject;
import sib.util.json.ParserException;
import sib.util.json.impl.AutoJsonObject;
import sib.util.json.impl.IgnoreCase;
import sib.util.json.impl.IndexedJsonList;
import sib.util.json.impl.Sequence;
import sib.util.json.impl.SimpleJsonList;


@IgnoreCase
@Sequence(value={"header", "default_tag", "tables", "common_fields"})
public class DBSchema extends AutoJsonObject {

    JsonList<String> header;
	String default_tag; // for AutoJsonObject, name should be the same as in json schema
	Tables tables;
	CsvFields common_fields;

    public DBSchema(String filename) throws IOException {
        super(true);
        super.loadFrom(filename);
        for (CsvTableSchema tableScema: tables) {
            tableScema.fixFields(common_fields);
        }
    }

    @Override
    public JsonList<?> newJsonList(String key) {
       if ("tables".equalsIgnoreCase(key)) {
            return new Tables();
       } else if ("header".equalsIgnoreCase(key)) {
           return  new SimpleJsonList<String>();
       } else if ("common_fields".equalsIgnoreCase(key)) {
           return  new CsvFields();
        } else {
            throw new ParserException("Not a List field: '"+key+'\'');
        }
    }

    public JsonList<String> getHeader() {
        return header;
    }

    public void setHeader(JsonList<String> header) {
        this.header = header;
    }

    public String getDefault_tag() {
        return default_tag;
    }

    public void setDefault_tag(String defaultTag) {
        default_tag = defaultTag;
    }

    public Tables getTables() {
        return tables;
    }

    public void setTables(Tables tables) {
        this.tables = tables;
    }

    public CsvFields getCommon_fields() {
        return common_fields;
    }

    public void setCommon_fields(CsvFields commonFields) {
        common_fields = commonFields;
    }

    public static class Tables extends IndexedJsonList<CsvTableSchema> {

        @Override
        public JsonObject newJsonObject(boolean ignoreCase) {
            return new CsvTableSchema();
        }
    }

    static class CsvFields extends IndexedJsonList<CsvField> {

        @Override
        public JsonObject newJsonObject(boolean ignoreCase) {
            return new CsvField();
        }
    }

    static class Row extends SimpleJsonList<String> {
        public Object getTyped(int k) {
            String value=(String)super.get(k);
            if (value.startsWith("\"") || value.startsWith("\'")) {
                return value.substring(1, value.length()-1);
            } else {
                try {
                    return Long.parseLong(value);
                } catch (NumberFormatException e) {
                    return Double.parseDouble(value);
                }
            }
        }
    }
}

