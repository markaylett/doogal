package org.doogal.core;

import java.io.File;
import java.io.IOException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import javax.mail.internet.ParseException;

import org.apache.commons.logging.Log;

public final class AsyncDoogal implements Doogal {
    private final Log log;
    private final Doogal doogal;
    private final Controller controller;
    private final ExecutorService executor;

    public AsyncDoogal(Log log, Doogal doogal, Controller controller) {
        this.log = log;
        this.doogal = doogal;
        this.controller = controller;
        this.executor = Executors.newSingleThreadExecutor();
    }

    public final void close() {
        executor.execute(new Runnable() {
            public final void run() {
                try {
                    doogal.close();
                } catch (IOException e) {
                    log.error(e.getLocalizedMessage());
                }
            }
        });
        executor.shutdown();
        try {
            executor.awaitTermination(5, TimeUnit.SECONDS);
        } catch (final InterruptedException e) {
            log.error(e.getLocalizedMessage());
        }
        if (!executor.isShutdown())
            executor.shutdownNow();
    }

    public final void eval(final String cmd, final Object... args) throws EvalException {
        executor.execute(new Runnable() {
            public final void run() {
                try {
                    doogal.eval(cmd, args);
                } catch (ExitException e) {
                    try {
                        controller.close();
                    } catch (IOException e1) {
                        log.error(e.getLocalizedMessage());
                    }
                } catch (EvalException e) {
                    log.error(e.getLocalizedMessage());
                } finally {
                    controller.ready();
                }
            }
        });
    }

    public final void eval() throws EvalException {
        executor.execute(new Runnable() {
            public final void run() {
                try {
                    doogal.eval();
                } catch (ExitException e) {
                    try {
                        controller.close();
                    } catch (IOException e1) {
                        log.error(e.getLocalizedMessage());
                    }
                } catch (EvalException e) {
                    log.error(e.getLocalizedMessage());
                } finally {
                    controller.ready();
                }
            }
        });
    }

    public final void readConfig(final File config) throws EvalException, IOException,
            ParseException {
        executor.execute(new Runnable() {
            public final void run() {
                try {
                    doogal.readConfig(config);
                } catch (ExitException e) {
                    try {
                        controller.close();
                    } catch (IOException e1) {
                        log.error(e.getLocalizedMessage());
                    }
                } catch (EvalException e) {
                    log.error(e.getLocalizedMessage());
                } catch (IOException e) {
                    log.error(e.getLocalizedMessage());
                } catch (ParseException e) {
                    log.error(e.getLocalizedMessage());
                } finally {
                    controller.ready();
                }
            }
        });
    }

    public final void readConfig() throws EvalException, IOException,
            ParseException {
        executor.execute(new Runnable() {
            public final void run() {
                try {
                    doogal.readConfig();
                } catch (ExitException e) {
                    try {
                        controller.close();
                    } catch (IOException e1) {
                        log.error(e.getLocalizedMessage());
                    }
                } catch (EvalException e) {
                    log.error(e.getLocalizedMessage());
                } catch (IOException e) {
                    log.error(e.getLocalizedMessage());
                } catch (ParseException e) {
                    log.error(e.getLocalizedMessage());
                } finally {
                    controller.ready();
                }
            }
        });
    }

    public final void setDefault(final String def) {
        executor.execute(new Runnable() {
            public final void run() {
                doogal.setDefault(def);
            }
        });
    }
}