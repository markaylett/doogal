package org.doogal.core;

import java.io.File;
import java.io.IOException;
import java.io.Reader;
import java.util.Collections;
import java.util.Map;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import javax.mail.internet.ParseException;

import org.apache.commons.logging.Log;
import org.doogal.core.command.Command;
import org.doogal.core.table.TableType;
import org.doogal.core.util.EvalException;

public final class AsyncDoogal implements Doogal {
    private final Log log;
    private final Doogal doogal;
    private final ExecutorService executor;

    public AsyncDoogal(Log log, Doogal doogal) {
        this.log = log;
        this.doogal = doogal;
        executor = Executors.newSingleThreadExecutor();
    }

    public final void close() {
        executor.execute(new Runnable() {
            public final void run() {
                try {
                    doogal.close();
                } catch (final IOException e) {
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

    public final void eval(final String cmd, final Object... args)
            throws EvalException {
        executor.execute(new Runnable() {
            public final void run() {
                try {
                    doogal.eval(cmd, args);
                } catch (final EvalException e) {
                    log.error(e.getLocalizedMessage());
                }
            }
        });
    }

    public final void eval() throws EvalException {
        executor.execute(new Runnable() {
            public final void run() {
                try {
                    doogal.eval();
                } catch (final EvalException e) {
                    log.error(e.getLocalizedMessage());
                }
            }
        });
    }

    public final void batch(final Reader reader) throws EvalException,
            IOException, ParseException {
        executor.execute(new Runnable() {
            public final void run() {
                try {
                    doogal.batch(reader);
                } catch (final ExitException e) {
                } catch (final EvalException e) {
                    log.error(e.getLocalizedMessage());
                } catch (final IOException e) {
                    log.error(e.getLocalizedMessage());
                } catch (final ParseException e) {
                    log.error(e.getLocalizedMessage());
                }
            }
        });
    }

    public final void batch(final File file) throws EvalException, IOException,
            ParseException {
        executor.execute(new Runnable() {
            public final void run() {
                try {
                    doogal.batch(file);
                } catch (final ExitException e) {
                } catch (final EvalException e) {
                    log.error(e.getLocalizedMessage());
                } catch (final IOException e) {
                    log.error(e.getLocalizedMessage());
                } catch (final ParseException e) {
                    log.error(e.getLocalizedMessage());
                }
            }
        });
    }

    public final void config() throws EvalException, IOException,
            ParseException {
        executor.execute(new Runnable() {
            public final void run() {
                try {
                    doogal.config();
                } catch (final ExitException e) {
                } catch (final EvalException e) {
                    log.error(e.getLocalizedMessage());
                } catch (final IOException e) {
                    log.error(e.getLocalizedMessage());
                } catch (final ParseException e) {
                    log.error(e.getLocalizedMessage());
                }
            }
        });
    }

    public final void setSelection(final TableType type, final Object... args) {
        executor.execute(new Runnable() {
            public final void run() {
                doogal.setSelection(type, args);
            }
        });
    }

    public final void clearSelection() {
        executor.execute(new Runnable() {
            public final void run() {
                doogal.clearSelection();
            }
        });
    }

    public final Map<String, Command> getBuiltins() {
        try {
            return executor.submit(new Callable<Map<String, Command>>() {
                public final Map<String, Command> call() throws Exception {
                    return doogal.getBuiltins();
                }
            }).get();
        } catch (final Exception e) {
            return Collections.<String, Command> emptyMap();
        }
    }
}