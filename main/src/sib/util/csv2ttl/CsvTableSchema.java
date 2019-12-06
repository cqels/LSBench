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

import java.util.HashMap;

import sib.bibm.Exceptions.BadSetupException;
import sib.util.Nameable;
import sib.util.csv2ttl.DBSchema.CsvFields;
import sib.util.json.JsonList;
import sib.util.json.JsonObject;
import sib.util.json.ParserException;
import sib.util.json.impl.AutoJsonObject;
import sib.util.json.impl.IgnoreCase;
import sib.util.json.impl.Sequence;
import sib.util.json.impl.SimpleJsonList;


@IgnoreCase
@Sequence(value={"name", "fields", "keys"})
public class CsvTableSchema extends AutoJsonObject implements Nameable {
    
    private String name;
    private Fields fields;
    private JsonList<String> keys;
    private int[] keyIndexes; 
	
    public CsvTableSchema() {
    }

    void fixFields(CsvFields commonFields) {
        if (keys==null) {
            return;
        }
        keyIndexes=new int[keys.size()];
        for (int k=0; k<keyIndexes.length; k++) {
            keyIndexes[k]=-1;            
        }
        for (int k=0; k<fields.size(); k++) {
            Object fieldOrString=fields.get(k);
            String fieldName;
            if (fieldOrString instanceof String) {
                fieldName=(String)fieldOrString;
                CsvField field = commonFields.get(fieldName);
                if (field==null) {
                    throw new ParserException("commond fielf not declared:"+fieldName);
                }
                fields.set(k, field);
            } else {
                fieldName=((CsvField)fieldOrString).getName();
            }
            int keyIndex=keys.indexOf(fieldName);
            if (keyIndex!=-1) { // yes this is a key
                keyIndexes[keyIndex]=k;
            }
        }
        for (int k=0; k<keyIndexes.length; k++) {
            if (keyIndexes[k]==-1) {
                throw new BadSetupException("In table '"+name+"': key '"+keys.get(k)+"' not declared as field");            
            }
        }
    }
    
    @Override
    public JsonList<?> newJsonList(String key) {
        if ("fields".equalsIgnoreCase(key)) {
            return  new Fields();
        } else if ("keys".equalsIgnoreCase(key)) {
            return  new SimpleJsonList<String>();
         } else {
             throw new ParserException("Not a List field: '"+key+'\'');
         }
     }

    public JsonList<String> getKeys() {
        return keys;
    }

    public void setKeys(JsonList<String> keys) {
        this.keys = keys;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setFields(Fields fields) {
        this.fields = fields;
    }

    /** 
     *  List of mixed elements: CsvField and String, so cannot use IndexedJsonList
     * @author ak
     *
     */
    static class Fields extends SimpleJsonList<Object> {

        HashMap<String, Object> index=new HashMap<String, Object>(); 

        @Override
        public void add(Object element) {
            if (element instanceof String) {
                index.put((String)element, element);            
            } else if (element instanceof Nameable) {
                String name = ((Nameable)element).getName();
                index.put(name, element);
            }
            super.add(element);
        }
        
       public Object get(String name) {
           return index.get(name);
       }

        @Override
        public JsonObject newJsonObject(boolean ignoreCase) {
            return new CsvField();
        }
    }


    @Override
    public String getName() {
        return name;
    }

    @SuppressWarnings("unchecked")
    public JsonList<CsvField> getFields() {
        JsonList tmp=fields;
        return tmp;
    }

    public int[] getKeyIdndexes() {
        return keyIndexes;
    }

}
