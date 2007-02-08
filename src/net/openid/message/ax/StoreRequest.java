/*
 * Copyright 2006-2007 Sxip Identity Corporation
 */

package net.openid.message.ax;

import net.openid.message.MessageException;
import net.openid.message.Parameter;
import net.openid.message.ParameterList;

import java.util.*;

/**
 * Implements the extension for Attribute Exchange store requests.
 *
 * @author Marius Scurtescu, Johnny Bufu
 */
public class StoreRequest extends AxMessage
{
    /**
     * Constructs a Store Request with an empty parameter list.
     */
    protected StoreRequest()
    {
        _parameters.set(new Parameter("mode", "store_request"));
    }

    /**
     * Constructs a Store Request with an empty parameter list.
     */
    public static StoreRequest createStoreRequest()
    {
        return new StoreRequest();
    }

    /**
     * Constructs a StoreRequest from a parameter list.
     * <p>
     * The parameter list can be extracted from a received message with the
     * getExtensionParams method of the Message class, and MUST NOT contain
     * the "openid.<extension_alias>." prefix.
     */
    protected StoreRequest(ParameterList params)
    {
        _parameters = params;
    }

    /**
     * Constructs a StoreRequest from a parameter list.
     * <p>
     * The parameter list can be extracted from a received message with the
     * getExtensionParams method of the Message class, and MUST NOT contain
     * the "openid.<extension_alias>." prefix.
     */
    public static StoreRequest createStoreRequest(ParameterList params)
            throws MessageException
    {
        StoreRequest req = new StoreRequest(params);

        if (! req.isValid())
            throw new MessageException("Invalid parameters for a store request");

        return req;
    }

    /**
     * Adds an attribute to the store request.
     *
     * @param       alias       The identifier that will be associated with the
     *                          attribute name URI
     * @param       typeUri     The attribute name URI
     * @param       value       The value of the attribute
     */
    public void addAttribute(String alias, String typeUri, String value)
    {
        int count = getCount(alias);

        String index = "";

        switch(count)
        {
            case 0:
                _parameters.set(new Parameter("type." + alias, typeUri));
                break;

            case 1:
                // rename the existing one
                _parameters.set(new Parameter("value." + alias + ".1",
                        getParameterValue("value." + alias)));
                _parameters.removeParameters("value." + alias);
                index = ".2";
                break;

            default:
                index = "." +Integer.toString(count + 1);
        }

        _parameters.set(new Parameter("value." + alias + index, value));
        setCount(alias, ++count);
    }

    /**
     * Returns a list with the attribute value(s) associated with the specified
     * alias.
     *
     * @param   alias       Attribute alias.
     * @return              List of attribute values.
     */
    public List getAttributeValues(String alias)
    {
        List values = new ArrayList();

        if (! _parameters.hasParameter("count." + alias))
            values.add(getParameterValue("value." + alias));
        else
            for (int i = 1; i <= getCount(alias); i++)
                values.add(getParameterValue("value." + alias + "." + Integer.toString(i)));

        return values;
    }

    /**
     * Gets a list of attribute aliases.
     */
    public List getAttributeAliases()
    {
        List aliases = new ArrayList();

        Iterator it = _parameters.getParameters().iterator();
        while (it.hasNext())
        {
            String paramName = ((Parameter) it.next()).getKey();

            if (paramName.startsWith("value."))
            {
                String alias;
                if (paramName.endsWith("."))
                    alias = paramName.substring(6, paramName.length() - 1);
                else
                    alias = paramName.substring(6);

                if ( ! aliases.contains(alias) )
                    aliases.add(alias);
            }
        }

        return aliases;
    }

    /**
     * Gets a map with attribute aliases -> list of values.
     */
    public Map getAttributes()
    {
        Map attributes = new HashMap();

        Iterator it = _parameters.getParameters().iterator();
        while (it.hasNext())
        {
            String paramName = ((Parameter) it.next()).getKey();

            if (paramName.startsWith("value."))
            {
                String alias;
                if (paramName.endsWith("."))
                    alias = paramName.substring(6, paramName.length() - 1);
                else
                    alias = paramName.substring(6);

                if ( ! attributes.containsKey(alias) )
                    attributes.put(alias, getAttributeValues(alias));
            }
        }

        return attributes;
    }

    /**
     * Gets the number of values provided in the fetch response for the
     * specified attribute alias.
     *
     * @param alias     The attribute alias.
     */
    public int getCount(String alias)
    {
        if (_parameters.hasParameter("count." + alias))
            return Integer.parseInt(_parameters.getParameterValue("count." + alias));

        else if (_parameters.hasParameter("value." + alias))
            return 1;

        else
            return 0;
    }

    /**
     * Sets the number of values provided in the fetch response for the
     * specified attribute alias.
     *
     * @param alias     The attribute alias.
     * @param count     The number of values.
     */
    private void setCount(String alias, int count)
    {
        // make sure that count.< alias >.1 is removed
        _parameters.removeParameters("count." + alias);

        if (count > 1)
            _parameters.set(new Parameter("count." + alias, Integer.toString(count)));
    }

    /**
     * Checks the validity of the extension.
     * <p>
     * Used when constructing a extension from a parameter list.
     *
     * @return      True if the extension is valid, false otherwise.
     */
    public boolean isValid()
    {
        Iterator it = _parameters.getParameters().iterator();
        while (it.hasNext())
        {
            String paramName = ((Parameter) it.next()).getKey();

            if (! paramName.equals("mode") &&
                    ! paramName.startsWith("type.") &&
                    ! paramName.startsWith("value.") &&
                    ! paramName.startsWith("count.") )
                return false;
        }

        return checkAttributes();
    }

    private boolean checkAttributes()
    {
        List aliases = getAttributeAliases();

        Iterator it = aliases.iterator();
        while (it.hasNext())
        {
            String alias = (String) it.next();

            if (! _parameters.hasParameter("type." + alias))
                return false;

            if ( ! _parameters.hasParameter("count." + alias) )
            {
                if ( ! _parameters.hasParameter("value." + alias) )
                    return false;
            }
            else // count.alias present
            {
                if (_parameters.hasParameter("value." + alias))
                    return false;

                int count = getCount(alias);

                for (int i = 1; i <= count; i++)
                    if (! _parameters.hasParameter("value." + alias + "." +
                            Integer.toString(i)))
                        return false;
            }
        }

        return true;
    }
}