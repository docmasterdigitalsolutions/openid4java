/*
 * Copyright 2006-2007 Sxip Identity Corporation
 */

package org.openid4java;

/**
 * @author Marius Scurtescu, Johnny Bufu
 */
public class OpenIDException extends Exception
{
    public OpenIDException(String message)
    {
        super(message);
    }

    public OpenIDException(String message, Throwable cause)
    {
        super(message, cause);
    }

    public OpenIDException(Throwable cause)
    {
        super(cause);
    }
}