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
package sib.bibm.qualification;

import sib.bibm.Exceptions.BadSetupException;
import sib.util.json.JsonList;
import sib.util.json.ParserException;
import sib.util.json.impl.SimpleJsonObject;


public abstract class ResultDescription extends SimpleJsonObject {
    public static ResultDescription stringResult;
    static {
        SimpleJsonObject descr=new SimpleJsonObject();
        descr.put("type", "str");
        stringResult=new StringResult(descr);
    }

    public static ResultDescription newResultDescription(SimpleJsonObject descr) {
        String type= ((String) descr.get("type"));
        if (type==null || "str".equalsIgnoreCase(type) || "date".equalsIgnoreCase(type)) {
            return new StringResult(descr);
        } else if ("real".equalsIgnoreCase(type)) {
            return new RealResult( descr);
        } else if ("int".equalsIgnoreCase(type)) {
            return new IntResult(descr);
        } else if ("object".equalsIgnoreCase(type)) {// only for OUT parameters of a function call
            return new ObjectResult(descr);
        }

        type=type.toLowerCase();
        String decimalType = "decimal.";
        if (type.startsWith(decimalType)) {
            String precision = type.substring(decimalType.length());
            return new DecimalResult(descr, precision); 
        }
        
        throw new ParserException("unknown type:"+type);
    }
    
    /** results which may contain spaces and other problematic characters,
     *   must be enquoted while printinq and dequoted while parsing.
     */
    protected  boolean quoted=false;
    protected Check[] checks;
        
    @SuppressWarnings("unchecked")
    public ResultDescription(SimpleJsonObject descr, boolean quoted) {
        super(descr);
        this.quoted = quoted;
        if (descr == null) {
            checks = new Check[0];
            return;
        }
        Object checksO = descr.get("check");
        if (checksO == null) {
            checks = new Check[0];
            return;
        }
        if (checksO instanceof String) {
            checks = new Check[1];
            checks[0] = makeCheck((String) checksO);
            return;
        } else if (checksO instanceof JsonList) {
            JsonList<String> checksS = (JsonList<String>) checksO;
            checks = new Check[checksS.size()];
            for (int k = 0; k < checks.length; k++) {
                String checkS = checksS.get(k);
                checks[k] = makeCheck(checkS);
            }
        } else {
            throw new BadSetupException(" wrong check type: " + checksO.getClass().getName());
        }

    }
    
    public String canonicalString(String value) {
        return value;
    }

    protected Object nullNumber(Object v1) {
        if (v1 instanceof String) {
            String sv1 = (String) v1;
            if ("null".equalsIgnoreCase(sv1)) {
                v1 = null;
            }
        }
        return v1;
    }

    private Check makeCheck(String checkS) {
        if (checkS.startsWith("$")) {
            long delta=Long.parseLong(checkS.substring(1));
            return new DeltaCheck(delta);
        } else  if (checkS.endsWith("%")) {
            int percent=Integer.parseInt(checkS.substring(0, checkS.length()-1));
            return new PercentCheck(percent);
        } else {
            throw new BadSetupException("uncknown check:"+checkS);
        }
    }

    public String fromUnquoted(Object value) {
        // all values come unquoted
        if (value==null) {
            return "null";
        } else if (quoted) {
            StringBuilder sb=new StringBuilder();
            sb.append(QUOTE).append(value).append(QUOTE);
            return sb.toString(); 
        } else {
            return value.toString();
        }
    }
    
    public String fromQuoted(String value) {
        // all values come quoted
        if (value==null) {
            return "null";
        } else if (quoted) {
            return value;
        } else {
            return value.substring(1, value.length()-1);
        }
    }

    public abstract boolean compare(Object v1, Object v2);
   
    public Check[] getChecks() {
        return checks;
    }

    static final char QUOTE='\'';
    
    static class StringResult extends ResultDescription {
        public StringResult(SimpleJsonObject descr) {
            super(descr, true);
        }

        @Override
        public boolean compare(Object v1, Object v2) {
            if (v1==null) {
                return v2==null;
            }
            if (v2==null) {
                return false;
            }
            String s1=(String)v1; 
            String s2=(String)v2; 
            int l1=s1.length();
            if (l1!=s2.length()) {
                return false;
            }
            for (int i = 1; i < l1-1; i++) {
               if (s1.charAt(i)!=s2.charAt(i)) {
                   return false;
               }
            }
            return true;
        }
    }
    
    static class IntResult extends ResultDescription {
        public IntResult(SimpleJsonObject descr2) {
            super(descr2, false);
        }

        @Override
        public boolean compare(Object v1, Object v2) {
            v1 = nullNumber(v1);
            v2 = nullNumber(v2);
            if (v1==null) {
                return v2==null;
            }
            if (v2==null) {
                return false;
            }
            long v1l=toLong(v1);
            long v2l=toLong(v2);
            if (checks.length==0) {
                return v1l==v2l;
            }
            for (Check check: checks) {
                if (!check.compare(v1l, v2l)) {
                    return false;
                }
            }
            return true;
        }

        private long toLong(Object v1) {
            if (v1 instanceof Long) {
                return ((Long)v1).longValue();
            } else if (v1 instanceof Integer) {
                return ((Integer)v1).intValue();
            } else if (v1 instanceof String ) {
                return Long.parseLong((String)v1);
            } else {
                throw new IllegalArgumentException("cannot extract long value from "+v1.getClass().getName());
            }
        }
    }
    
    static class RealResult extends ResultDescription {
        
        RealResult(SimpleJsonObject descr) {
            super(descr, false);
        }
        
        @Override
        public boolean compare(Object v1, Object v2) {
            v1 = nullNumber(v1);
            v2 = nullNumber(v2);
            if (v1==null) {
                return v2==null;
            }
            if (v2==null) {
                return false;
            }
            double v1l=toDouble(v1);
            double v2l=toDouble(v2);
            if (checks.length==0) {
                return v1l==v2l;
            }
            for (Check check: checks) {
                if (!check.compare(v1l, v2l)) {
                    return false;
                }
            }
            return true;
        }

        protected double toDouble(Object v1) {
            if (v1 instanceof Double) {
                return ((Double)v1).doubleValue();
            } else  if (v1 instanceof Float) {
                return ((Float)v1).doubleValue();
            } else  if (v1 instanceof Long) {
                return ((Long)v1).doubleValue();
            } else if (v1 instanceof Integer) {
                return ((Integer)v1).doubleValue();
            } else if (v1 instanceof String ) {
                return Double.parseDouble((String)v1);
            } else {
                throw new IllegalArgumentException("cannot extract double value from "+v1.getClass().getName());
            }
        }

        @Override
        public String canonicalString(String value) {
            throw new UnsupportedOperationException("RealResult.canonicalString()");
        }
    }

    static class DecimalResult extends RealResult {
        int factor; // 10^precision 
        int precision;
        
        /**
         * 
         * @param descr
         * @param precision number of meaningful digits after point
         */
        DecimalResult(SimpleJsonObject descr, String precision) {
            super(descr);
            try {
                this.precision=Integer.parseInt(precision);
                factor=1;
                for (int k=0; k<this.precision; k++) {
                    factor=factor*10;
                }
            } catch (NumberFormatException e) {
                throw new ParserException("bad decimal precision");
            }
        }

        @Override
        public boolean compare(Object v1, Object v2) {
            if (v1==null) {
                return v2==null;
            }
            if (v2==null) {
                return false;
            }
            double v1d=toDouble(v1);
            double v2d=toDouble(v2);
            if (checks.length==0) {
                long v1l=Math.round(v1d*factor); 
                long v2l=Math.round(v2d*factor); 
                return v1l==v2l;
            }
            for (Check check: checks) {
                if (!check.compare(v1d, v2d)) {
                    return false;
                }
            }
            return true;
        }

        @Override
        public String canonicalString(String value) {
            int actualPrecision = value.length() - 1 - value.indexOf('.');
            int delta = precision - actualPrecision;
            if (delta == 0) {
                return value;
            } else if (delta > 0) {
                StringBuilder sb = new StringBuilder(value);
                for (int k = 0; k < delta; k++) {
                    sb.append('0');
                }
                return sb.toString();
            } else {  // delta<0
                int canonicalLength = value.length() + delta;
                for (int k = canonicalLength; k < value.length(); k++) {
                    if (value.charAt(k) != '0') {
                        throw new NumberFormatException(value + ": only " + precision + " digits allowed");
                    }
                }
                return value.substring(0, canonicalLength);
            }
        }

   }

    static class ObjectResult extends ResultDescription {
        public ObjectResult(SimpleJsonObject descr) {
            super(descr, true);
        }

        @Override
        public boolean compare(Object v1, Object v2) {
            throw new UnsupportedOperationException("ObjectResult.compare()");
        }
    }

    public static abstract class Check {

        public abstract boolean compare(long v1, long v2);
        public abstract boolean compare(double v1, double v2);

    }

    public static class DeltaCheck extends Check {
        long delta;
        
        public DeltaCheck(long delta) {
            this.delta = delta;
        }

        public boolean compare(long v1, long v2) {
            return compare((double)v1, (double)v2);
        }

        public boolean compare(double v1, double v2) {
            return Math.abs(v1-v2)<=delta;
        }

    }

    public class PercentCheck extends Check {
        long lo, hi;

        public PercentCheck(int percent) {
            this.lo = 100-percent;
            this.hi = 100+percent;
        }

        @Override
        public boolean compare(long v1, long v2) {
            return compare((double)v1, (double)v2);
        }

        public boolean compare(double v1, double v2) {
            if (v1==0.0 || v2==0.0) {
                return false;  // because of division by zero
            }
            double ratio=v1/v2;
            long rounded=Math.round(ratio*100);
            return lo<=rounded && rounded <=hi;
        }
    }
    
}
