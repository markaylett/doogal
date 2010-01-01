package org.doogal.core.actor.message;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.doogal.core.actor.annotation.MessageHandler;

/**
 * The AbstractIntrepreter provides a convenient mapping between messages and
 * late-bound method calls.
 * 
 * @author Mark Aylett
 * 
 */
public abstract class AbstractInterpreter implements Interpreter {
    private final Map<Integer, List<Method>> handlers;

    private static boolean equalParams(Class<?>[] lhs, Class<?>[] rhs) {
        if (null == lhs)
            return null == rhs || 0 == rhs.length;

        if (null == rhs)
            return 0 == lhs.length;

        if (lhs.length != rhs.length)
            return false;

        for (int i = 0; i < lhs.length; i++)
            if (lhs[i] != rhs[i])
                return false;

        return true;
    }

    private Method getMethod(int type, Class<?>... params)
            throws NoSuchMethodException {
        final List<Method> methods = handlers.get(type);
        if (null == methods)
            throw new NoSuchMethodException("no method for type: " + type);
        for (final Method method : methods)
            if (equalParams(params, method.getParameterTypes()))
                return method;
        throw new NoSuchMethodException("no overload for type: " + type);
    }

    private final Object invoke(int type, Object arg)
            throws IllegalAccessException, InvocationTargetException,
            NoSuchMethodException {
        if (!arg.getClass().isArray()) {
            final Method method = getMethod(type, arg.getClass());
            return method.invoke(this, arg);
        }

        final Object[] args = (Object[]) arg;
        final List<Class<?>> types = new ArrayList<Class<?>>();
        for (final Object obj : args)
            types.add(obj.getClass());

        try {
            final Method method = getMethod(type, types.toArray(new Class[types
                    .size()]));
            return method.invoke(this, args);
        } catch (final NoSuchMethodException e) {
            final Method method = getMethod(type, Object[].class);
            return method.invoke(this, (Object) args);
        }
    }

    public AbstractInterpreter() {
        handlers = new HashMap<Integer, List<Method>>();
        final Method[] all = getClass().getMethods();
        for (int i = 0; i < all.length; ++i) {
            final Method method = all[i];
            final MessageHandler handler = method
                    .getAnnotation(MessageHandler.class);
            if (null != handler) {
                List<Method> methods = handlers.get(handler.value());
                if (null == methods) {
                    methods = new ArrayList<Method>();
                    handlers.put(handler.value(), methods);
                }
                methods.add(method);
            }
        }
    }

    public final Object eval(int type, Object request)
            throws InterruptedException, InvocationTargetException,
            NoSuchMethodException {
        try {
            return invoke(type, request);
        } catch (final InvocationTargetException e) {
            final Throwable t = e.getCause();
            if (t instanceof InterruptedException)
                throw (InterruptedException) t;
            else
                throw e;
        } catch (final NoSuchMethodException e) {
            throw e;
        } catch (final Exception e) {
            throw new InvocationTargetException(e);
        }
    }

    public final void eval(Message message) throws InterruptedException {
        try {
            final Object response = eval(message.getType(), message
                    .getRequest());
            message.getResponse().set(response);
        } catch (final InterruptedException e) {
            message.getResponse().setException(e);
            throw e;
        } catch (final InvocationTargetException e) {
            message.getResponse().setException(e.getCause());
        } catch (final NoSuchMethodException e) {
            message.getResponse().setException(e);
        }
    }
}
