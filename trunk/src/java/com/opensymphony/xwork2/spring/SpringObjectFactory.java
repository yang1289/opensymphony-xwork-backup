/*
 * Copyright (c) 2002-2006 by OpenSymphony
 * All rights reserved.
 */
package com.opensymphony.xwork2.spring;

import java.util.HashMap;
import java.util.Map;

import org.springframework.beans.BeansException;
import org.springframework.beans.factory.NoSuchBeanDefinitionException;
import org.springframework.beans.factory.UnsatisfiedDependencyException;
import org.springframework.beans.factory.config.AutowireCapableBeanFactory;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;

import com.opensymphony.xwork2.ObjectFactory;
import com.opensymphony.xwork2.inject.Inject;
import com.opensymphony.xwork2.util.logging.Logger;
import com.opensymphony.xwork2.util.logging.LoggerFactory;

/**
 * Simple implementation of the ObjectFactory that makes use of Spring's application context if one has been configured,
 * before falling back on the default mechanism of instantiating a new class using the class name. <p/> In order to use
 * this class in your application, you will need to instantiate a copy of this class and set it as XWork's ObjectFactory
 * before the xwork.xml file is parsed. In a servlet environment, this could be done using a ServletContextListener.
 *
 * @author Simon Stewart (sms@lateral.net)
 */
public class SpringObjectFactory extends ObjectFactory implements ApplicationContextAware {
    private static final Logger LOG = LoggerFactory.getLogger(SpringObjectFactory.class);

    protected ApplicationContext appContext;
    protected AutowireCapableBeanFactory autoWiringFactory;
    protected int autowireStrategy = AutowireCapableBeanFactory.AUTOWIRE_BY_NAME;
    private Map classes = new HashMap();
    private boolean useClassCache = true;
    private boolean alwaysRespectAutowireStrategy = false;

    @Inject(value="applicationContextPath",required=false)
    public void setApplicationContextPath(String ctx) {
        if (ctx != null) {
            setApplicationContext(new ClassPathXmlApplicationContext(ctx));
        }
    }
    
    /**
     * Set the Spring ApplicationContext that should be used to look beans up with.
     *
     * @param appContext The Spring ApplicationContext that should be used to look beans up with.
     */
    public void setApplicationContext(ApplicationContext appContext)
            throws BeansException {
        this.appContext = appContext;
        autoWiringFactory = findAutoWiringBeanFactory(this.appContext);
    }

    /**
     * Sets the autowiring strategy
     *
     * @param autowireStrategy
     */
    public void setAutowireStrategy(int autowireStrategy) {
        switch (autowireStrategy) {
            case AutowireCapableBeanFactory.AUTOWIRE_AUTODETECT:
                LOG.info("Setting autowire strategy to autodetect");
                this.autowireStrategy = autowireStrategy;
                break;
            case AutowireCapableBeanFactory.AUTOWIRE_BY_NAME:
                LOG.info("Setting autowire strategy to name");
                this.autowireStrategy = autowireStrategy;
                break;
            case AutowireCapableBeanFactory.AUTOWIRE_BY_TYPE:
                LOG.info("Setting autowire strategy to type");
                this.autowireStrategy = autowireStrategy;
                break;
            case AutowireCapableBeanFactory.AUTOWIRE_CONSTRUCTOR:
                LOG.info("Setting autowire strategy to constructor");
                this.autowireStrategy = autowireStrategy;
                break;
            default:
                throw new IllegalStateException("Invalid autowire type set");
        }
    }

    public int getAutowireStrategy() {
        return autowireStrategy;
    }


    /**
     * If the given context is assignable to AutowireCapbleBeanFactory or contains a parent or a factory that is, then
     * set the autoWiringFactory appropriately.
     *
     * @param context
     */
    protected AutowireCapableBeanFactory findAutoWiringBeanFactory(ApplicationContext context) {
        if (context instanceof AutowireCapableBeanFactory) {
            // Check the context
            return (AutowireCapableBeanFactory) context;
        } else if (context instanceof ConfigurableApplicationContext) {
            // Try and grab the beanFactory
            return ((ConfigurableApplicationContext) context).getBeanFactory();
        } else if (context.getParent() != null) {
            // And if all else fails, try again with the parent context
            return findAutoWiringBeanFactory(context.getParent());
        }
        return null;
    }

    /**
     * Looks up beans using Spring's application context before falling back to the method defined in the {@link
     * ObjectFactory}.
     *
     * @param beanName     The name of the bean to look up in the application context
     * @param extraContext
     * @return A bean from Spring or the result of calling the overridden
     *         method.
     * @throws Exception
     */
    public Object buildBean(String beanName, Map extraContext, boolean injectInternal) throws Exception {
        Object o = null;
        try {
            o = appContext.getBean(beanName);
        } catch (NoSuchBeanDefinitionException e) {
            Class beanClazz = getClassInstance(beanName);
            o = buildBean(beanClazz, extraContext);
        }
        if (injectInternal) {
            injectInternalBeans(o);
        }
        return o;
    }

    /**
     * @param clazz
     * @param extraContext
     * @throws Exception
     */
    public Object buildBean(Class clazz, Map extraContext) throws Exception {
        Object bean;

        try {
            // Decide to follow autowire strategy or use the legacy approach which mixes injection strategies
            if (alwaysRespectAutowireStrategy) {
                // Leave the creation up to Spring
                bean = autoWiringFactory.createBean(clazz, autowireStrategy, false);
                injectApplicationContext(bean);
                return injectInternalBeans(bean);
            } else {
                bean = autoWiringFactory.autowire(clazz, AutowireCapableBeanFactory.AUTOWIRE_CONSTRUCTOR, false);
                bean = autoWiringFactory.applyBeanPostProcessorsBeforeInitialization(bean, bean.getClass().getName());
                // We don't need to call the init-method since one won't be registered.
                bean = autoWiringFactory.applyBeanPostProcessorsAfterInitialization(bean, bean.getClass().getName());
                return autoWireBean(bean, autoWiringFactory);
            }
        } catch (UnsatisfiedDependencyException e) {
            // Fall back
            return autoWireBean(super.buildBean(clazz, extraContext), autoWiringFactory);
        }
    }

    public Object autoWireBean(Object bean) {
        return autoWireBean(bean, autoWiringFactory);
    }

    /**
     * @param bean
     * @param autoWiringFactory
     */
    public Object autoWireBean(Object bean, AutowireCapableBeanFactory autoWiringFactory) {
        if (autoWiringFactory != null) {
            autoWiringFactory.autowireBeanProperties(bean,
                    autowireStrategy, false);
        }
        injectApplicationContext(bean);

        injectInternalBeans(bean);

        return bean;
    }

    private void injectApplicationContext(Object bean) {
        if (bean instanceof ApplicationContextAware) {
            ((ApplicationContextAware) bean).setApplicationContext(appContext);
        }
    }

    public Class getClassInstance(String className) throws ClassNotFoundException {
        Class clazz = null;
        if (useClassCache) {
            synchronized(classes) {
                // this cache of classes is needed because Spring sucks at dealing with situations where the
                // class instance changes 
                clazz = (Class) classes.get(className);
            }
        }

        if (clazz == null) {
            if (appContext.containsBean(className)) {
                clazz = appContext.getBean(className).getClass();
            } else {
                clazz = super.getClassInstance(className);
            }

            if (useClassCache) {
                synchronized(classes) {
                    classes.put(className, clazz);
                }
            }
        }

        return clazz;
    }

    /**
     * This method sets the ObjectFactory used by XWork to this object. It's best used as the "init-method" of a Spring
     * bean definition in order to hook Spring and XWork together properly (as an alternative to the
     * org.apache.struts2.spring.lifecycle.SpringObjectFactoryListener)
     * @deprecated Since 2.1 as it isn't necessary
     */
    public void initObjectFactory() {
        // not necessary anymore
    }

    /**
     * Allows for ObjectFactory implementations that support
     * Actions without no-arg constructors.
     *
     * @return false
     */
    public boolean isNoArgConstructorRequired() {
        return false;
    }

    /**
     *  Enable / disable caching of classes loaded by Spring.
     *  
     * @param useClassCache
     */
    public void setUseClassCache(boolean useClassCache) {
        this.useClassCache = useClassCache;
    }

    /**
     * Determines if the autowire strategy is always followed when creating beans
     *
     * @param alwaysRespectAutowireStrategy True if the strategy is always used
     */
    public void setAlwaysRespectAutowireStrategy(boolean alwaysRespectAutowireStrategy) {
        this.alwaysRespectAutowireStrategy = alwaysRespectAutowireStrategy;
    }
}
