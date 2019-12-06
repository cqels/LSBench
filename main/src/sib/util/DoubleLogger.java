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
package sib.util;

import java.io.*;

/**
 * prints  both into the System.err and the log file, if any.
 * @author ak
 *
 */
public class DoubleLogger {
	private PrintStream out = System.out;
    private PrintStream out2;

    public DoubleLogger(PrintStream out)   {
    	this.out=out;
    }

    public DoubleLogger(PrintStream out, File file) throws IOException   {
    	this.out=out;
        out2 = new PrintStream(new FileOutputStream(file));
    }

    public synchronized DoubleLogger print(char c)  {
		out.print(c);
		if (out2==null) return this;
        out2.print(c);
        return this;
    }

    public synchronized DoubleLogger print(Object... text)   {
        for(int k = 0; k < text.length; k++)    {
            Object str = text[k];
    		out.print(str);
    		if (out2!=null)	out2.print(str);
        }
        return this;
    }

    public synchronized DoubleLogger println(Object... text)   {
        for(int k = 0; k < text.length; k++)    {
            Object str = text[k];
    		out.print(str);
    		if (out2!=null) out2.print(str);
        }
		out.println();
		if (out2==null) return this;
		out2.println();
        return this;
    }

    public void printStackTrace(Throwable e) {
    	e.printStackTrace();
		if (out2==null) return;
    	e.printStackTrace(out2);
    }
    
    private static InheritableThreadLocal<DoubleLogger> thrLocalErr = new InheritableThreadLocal<DoubleLogger>() {

        public DoubleLogger initialValue()  {
            return new DoubleLogger(System.err);
        }

    };

    public static DoubleLogger getErr()    {
        return thrLocalErr.get();
    }

    /**
     * @throws IOException 
     */
    public static void setErrFile(File file) throws IOException   {
    	thrLocalErr.set(new DoubleLogger(System.err, file));
    }

    private static InheritableThreadLocal<DoubleLogger> thrLocalOut = new InheritableThreadLocal<DoubleLogger>() {

        public DoubleLogger initialValue()  {
            return new DoubleLogger(System.out);
        }

    };

    public static DoubleLogger getOut()    {
        return thrLocalOut.get();
    }

    /**
     * @throws IOException 
     */
    public static void setOutFile(File file) throws IOException   {
    	thrLocalErr.set(new DoubleLogger(System.out, file));
    }

}
