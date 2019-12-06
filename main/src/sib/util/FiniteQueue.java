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

import java.util.Deque;

public class FiniteQueue<E> {
    Deque<E> queue;
	boolean finish=false;
	
	public FiniteQueue(Deque<E> queue) {
		this.queue=queue;
	}
	
	public synchronized void setFinish () {
		this.finish=true;
		notifyAll();
	}

	public synchronized boolean offer (E e) {
		if (queue.offer(e)) {
		    notifyAll();
	        return true;
		} else {
		    return false;
		}
	}

	public synchronized void add (E e) throws InterruptedException {
		for (;;) {
	        if (queue.offer(e)) {
	            notifyAll();
	            return;
	        } else {    // buf is full
	            wait();
	        }
		}
	}

	public synchronized E take () throws InterruptedException {
		for (;;) {
		    E el = queue.poll();
			if (el != null) { // buf is not empty
				return el;
			}
			if (finish) { // check the flag only if buf is empty 
				return null;
			}
			wait();
		}
	}

}
