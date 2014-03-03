/*
 * Copyright 2008-2014, David Karnok 
 * The file is part of the Open Imperium Galactica project.
 * 
 * The code should be distributed under the LGPL license.
 * See http://www.gnu.org/licenses/lgpl.html for details.
 */
/* -*-mode:java; c-basic-offset:2; indent-tabs-mode:nil -*- */
/* JOrbis
 * Copyright (C) 2000 ymnk, JCraft,Inc.
 *  
 * Written by: 2000 ymnk<ymnk@jcraft.com>
 *   
 * Many thanks to 
 *   Monty <monty@xiph.org> and 
 *   The XIPHOPHORUS Company http://www.xiph.org/ .
 * JOrbis has been based on their awesome works, Vorbis codec.
 *   
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Library General Public License
 * as published by the Free Software Foundation; either version 2 of
 * the License, or (at your option) any later version.

 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Library General Public License for more details.
 * 
 * You should have received a copy of the GNU Library General Public
 * License along with this program; if not, write to the Free Software
 * Foundation, Inc., 675 Mass Ave, Cambridge, MA 02139, USA.
 */

package com.jcraft.jorbis;
/**
 * Exception class to indicate problems in the JOrbis library.
 * Comments and style correction by karnokd.
 * @author ymnk
 * @version 1.1
 */
public class JOrbisException extends Exception {
	/** The serial version. */
	private static final long serialVersionUID = -2429372581713032065L;
	/** Constructor without message or cause. */
	public JOrbisException() {
		super();
	}
	/**
	 * Constructor with custom error message.
	 * @param message the error message
	 */
	public JOrbisException(String message) {
		super("JOrbis: " + message);
	}
	/**
	 * Constructor with root cause throwable.
	 * @param cause the root cause
	 * @since 1.1 karnokd
	 */
	public JOrbisException(Throwable cause) {
		super(cause);
	}
	/**
	 * Constructor with message and root cause.
	 * @param message the message
	 * @param cause the root cause
	 * @since 1.1 karnokd
	 */
	public JOrbisException(String message, Throwable cause) {
		super("JOrbis: " + message, cause);
	}
}
