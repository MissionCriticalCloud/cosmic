package org.apache.cloudstack.managed.context.impl;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import org.apache.cloudstack.managed.context.ManagedContextListener;
import org.apache.cloudstack.managed.threadlocal.ManagedThreadLocal;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;

import org.junit.Before;
import org.junit.Test;

public class DefaultManagedContextTest {

    DefaultManagedContext context;

    @Before
    public void init() {
        ManagedThreadLocal.setValidateInContext(false);

        context = new DefaultManagedContext();
    }

    @Test
    public void testCallable() throws Exception {
        assertEquals(5, context.callWithContext(new Callable<Integer>() {
            @Override
            public Integer call() throws Exception {
                return 5;
            }
        }).intValue());
    }

    @Test
    public void testRunnable() throws Exception {
        final List<Object> touch = new ArrayList<>();

        context.runWithContext(new Runnable() {
            @Override
            public void run() {
                touch.add(new Object());
            }
        });

        assertEquals(1, touch.size());
    }

    @Test
    public void testGoodListeners() throws Exception {
        final List<Object> touch = new ArrayList<>();

        context.registerListener(new ManagedContextListener<Object>() {
            @Override
            public Object onEnterContext(final boolean reentry) {
                touch.add("enter");
                return "hi";
            }

            @Override
            public void onLeaveContext(final Object data, final boolean reentry) {
                touch.add("leave");
                assertEquals("hi", data);
            }
        });

        context.registerListener(new ManagedContextListener<Object>() {
            @Override
            public Object onEnterContext(final boolean reentry) {
                touch.add("enter1");
                return "hi";
            }

            @Override
            public void onLeaveContext(final Object data, final boolean reentry) {
                touch.add("leave1");
                assertEquals("hi", data);
            }
        });

        assertEquals(5, context.callWithContext(new Callable<Integer>() {
            @Override
            public Integer call() throws Exception {
                return 5;
            }
        }).intValue());

        assertEquals("enter", touch.get(0));
        assertEquals("enter1", touch.get(1));
        assertEquals("leave1", touch.get(2));
        assertEquals("leave", touch.get(3));
    }

    @Test
    public void testBadListeners() throws Exception {
        final List<Object> touch = new ArrayList<>();

        context.registerListener(new ManagedContextListener<Object>() {
            @Override
            public Object onEnterContext(final boolean reentry) {
                touch.add("enter");
                throw new RuntimeException("I'm a failure");
            }

            @Override
            public void onLeaveContext(final Object data, final boolean reentry) {
                touch.add("leave");
                assertNull(data);
            }
        });

        context.registerListener(new ManagedContextListener<Object>() {
            @Override
            public Object onEnterContext(final boolean reentry) {
                touch.add("enter1");
                return "hi";
            }

            @Override
            public void onLeaveContext(final Object data, final boolean reentry) {
                touch.add("leave1");
                assertEquals("hi", data);
            }
        });

        try {
            context.callWithContext(new Callable<Integer>() {
                @Override
                public Integer call() throws Exception {
                    return 5;
                }
            }).intValue();

            fail();
        } catch (final Throwable t) {
            assertTrue(t instanceof RuntimeException);
            assertEquals("I'm a failure", t.getMessage());
        }

        assertEquals("enter", touch.get(0));
        assertEquals("enter1", touch.get(1));
        assertEquals("leave1", touch.get(2));
        assertEquals("leave", touch.get(3));
    }

    @Test
    public void testBadInvocation() throws Exception {
        final List<Object> touch = new ArrayList<>();

        context.registerListener(new ManagedContextListener<Object>() {
            @Override
            public Object onEnterContext(final boolean reentry) {
                touch.add("enter");
                return "hi";
            }

            @Override
            public void onLeaveContext(final Object data, final boolean reentry) {
                touch.add("leave");
                assertEquals("hi", data);
            }
        });

        context.registerListener(new ManagedContextListener<Object>() {
            @Override
            public Object onEnterContext(final boolean reentry) {
                touch.add("enter1");
                return "hi1";
            }

            @Override
            public void onLeaveContext(final Object data, final boolean reentry) {
                touch.add("leave1");
                assertEquals("hi1", data);
            }
        });

        try {
            context.callWithContext(new Callable<Integer>() {
                @Override
                public Integer call() throws Exception {
                    throw new RuntimeException("I'm a failure");
                }
            }).intValue();

            fail();
        } catch (final Throwable t) {
            assertTrue(t.getMessage(), t instanceof RuntimeException);
            assertEquals("I'm a failure", t.getMessage());
        }

        assertEquals("enter", touch.get(0));
        assertEquals("enter1", touch.get(1));
        assertEquals("leave1", touch.get(2));
        assertEquals("leave", touch.get(3));
    }

    @Test
    public void testBadListernInExit() throws Exception {
        final List<Object> touch = new ArrayList<>();

        context.registerListener(new ManagedContextListener<Object>() {
            @Override
            public Object onEnterContext(final boolean reentry) {
                touch.add("enter");
                return "hi";
            }

            @Override
            public void onLeaveContext(final Object data, final boolean reentry) {
                touch.add("leave");
                assertEquals("hi", data);

                throw new RuntimeException("I'm a failure");
            }
        });

        context.registerListener(new ManagedContextListener<Object>() {
            @Override
            public Object onEnterContext(final boolean reentry) {
                touch.add("enter1");
                return "hi1";
            }

            @Override
            public void onLeaveContext(final Object data, final boolean reentry) {
                touch.add("leave1");
                assertEquals("hi1", data);
            }
        });

        try {
            context.callWithContext(new Callable<Integer>() {
                @Override
                public Integer call() throws Exception {
                    return 5;
                }
            }).intValue();

            fail();
        } catch (final Throwable t) {
            assertTrue(t.getMessage(), t instanceof RuntimeException);
            assertEquals("I'm a failure", t.getMessage());
        }

        assertEquals("enter", touch.get(0));
        assertEquals("enter1", touch.get(1));
        assertEquals("leave1", touch.get(2));
        assertEquals("leave", touch.get(3));
    }
}
