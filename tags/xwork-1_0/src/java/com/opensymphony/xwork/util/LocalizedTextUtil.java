/*
 * Copyright (c) 2002-2003 by OpenSymphony
 * All rights reserved.
 */
package com.opensymphony.xwork.util;

import com.opensymphony.xwork.ActionContext;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.text.MessageFormat;
import java.util.*;


/**
 * LocalizedTextUtil
 *
 * @author Jason Carreira
 *         Created Apr 19, 2003 11:02:26 PM
 */
public class LocalizedTextUtil {
    //~ Static fields/initializers /////////////////////////////////////////////

    private static final List DEFAULT_RESOURCE_BUNDLES = Collections.synchronizedList(new ArrayList());
    private static final Log LOG = LogFactory.getLog(LocalizedTextUtil.class);

    static {
        DEFAULT_RESOURCE_BUNDLES.add("com/opensymphony/xwork/xwork-messages");
    }

    //~ Methods ////////////////////////////////////////////////////////////////

    public static void addDefaultResourceBundle(String resourceBundleName) {
        //make sure this doesn't get added more than once
        DEFAULT_RESOURCE_BUNDLES.remove(resourceBundleName);
        DEFAULT_RESOURCE_BUNDLES.add(0, resourceBundleName);

        if (LOG.isDebugEnabled()) {
            LOG.debug("Added default resource bundle " + resourceBundleName + ", default resource bundles = " + DEFAULT_RESOURCE_BUNDLES);
        }
    }

    public static String findDefaultText(String aTextName, Locale locale) throws MissingResourceException {
        MissingResourceException e = null;
        List localList = new ArrayList(DEFAULT_RESOURCE_BUNDLES);

        for (Iterator iterator = localList.iterator(); iterator.hasNext();) {
            String bundleName = (String) iterator.next();

            try {
                ResourceBundle bundle = findResourceBundle(bundleName, locale);

                return bundle.getString(aTextName);
            } catch (MissingResourceException ex) {
                e = ex;
            }
        }

        if (e == null) {
            e = new MissingResourceException("Unable to find text for key " + aTextName, LocalizedTextUtil.class.getName(), aTextName);
        }

        throw e;
    }

    /**
     * Returns a localized message for the specified text key, aTextName, substituting variables from the
     * array of params into the message.
     *
     * @param aTextName The message key
     * @param params    An array of objects to be substituted into the message text
     * @return A formatted message based on the key specified
     * @throws MissingResourceException
     */
    public static String findDefaultText(String aTextName, Locale locale, Object[] params) throws MissingResourceException {
        return MessageFormat.format(findDefaultText(aTextName, locale), params);
    }

    public static ResourceBundle findResourceBundle(String aBundleName, Locale locale) {
        return ResourceBundle.getBundle(aBundleName, locale, Thread.currentThread().getContextClassLoader());
    }

    /**
     * Calls {@link #findText(Class aClass, String aTextName, Locale locale, String defaultMessage, Object[] args)} with
     * aTextName as the default message.
     *
     * @see #findText(Class aClass, String aTextName, Locale locale, String defaultMessage, Object[] args)
     */
    public static String findText(Class aClass, String aTextName, Locale locale) {
        return findText(aClass, aTextName, locale, aTextName, new Object[0]);
    }

    public static String findText(ResourceBundle bundle, String aTextName, Locale locale) {
        return findText(bundle, aTextName, locale, aTextName, new Object[0]);
    }

    /**
     * Finds a localized text message for the given text key, aTextName. findText uses a system of
     * defaults for finding resource bundle property files for searching for the message text.
     * <ol>
     * <li>The class name is used with the call <br>
     * ResourceBundle.getBundle(aBundleName, locale, Thread.currentThread().getContextClassLoader())<br>
     * </li>
     * <li>If the message text is not found, each parent class of the action is used as above until
     * java.lang.Object is found.<li>
     * <li>If the message text has still not been found, findDefaultText(aTextName, locale) is called to
     * search the default message bundles</li>
     * <li>If the text has still not been found, the provided defaultMessage is returned.<li>
     * </ol>
     *
     * @param aClass         the class whose name to use as the start point for the search
     * @param aTextName      the text key to find the text message for
     * @param locale         the locale to use to find the correct ResourceBundle
     * @param defaultMessage the message to be returned if no text message can be found in a resource bundle
     * @return
     */
    public static String findText(Class aClass, String aTextName, Locale locale, String defaultMessage, Object[] args) {
        OgnlValueStack valueStack = ActionContext.getContext().getValueStack();

        do {
            try {
                ResourceBundle bundle = findResourceBundle(aClass.getName(), locale);

                String message = TextParseUtil.translateVariables(bundle.getString(aTextName), valueStack);

                return MessageFormat.format(message, args);
            } catch (MissingResourceException ex) {
                aClass = aClass.getSuperclass();
            }
        } while (!aClass.equals(Object.class));

        return getDefaultText(aTextName, locale, valueStack, args, defaultMessage);
    }

    public static String findText(ResourceBundle bundle, String aTextName, Locale locale, String defaultMessage, Object[] args) {
        OgnlValueStack valueStack = ActionContext.getContext().getValueStack();

        try {
            String message = TextParseUtil.translateVariables(bundle.getString(aTextName), valueStack);

            return MessageFormat.format(message, args);
        } catch (MissingResourceException ex) {
        }

        return getDefaultText(aTextName, locale, valueStack, args, defaultMessage);
    }

    private static String getDefaultText(String aTextName, Locale locale, OgnlValueStack valueStack, Object[] args, String defaultMessage) {
        try {
            String message = TextParseUtil.translateVariables(findDefaultText(aTextName, locale), valueStack);

            return MessageFormat.format(message, args);
        } catch (MissingResourceException ex) {
            //ignore
        }

        if (LOG.isDebugEnabled()) {
            LOG.debug("Unable to find text for key " + aTextName);
        }

        return MessageFormat.format(TextParseUtil.translateVariables(defaultMessage, valueStack), args);
    }
}
