package org.doogal.core;

import java.io.File;
import java.io.IOException;
import java.io.Reader;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import javax.mail.internet.ParseException;

import org.apache.commons.logging.Log;

public final class AsyncDoogal implements Doogal {
    private final Log log;
    private final Doogal doogal;
    private final ExecutorService executor;

    public AsyncDoogal(Log log, Doogal doogal) {
        this.log = log;
        this.doogal = doogal;
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

    public final void eval(final String cmd, final Object... args)
            throws EvalException {
        executor.execute(new Runnable() {
            public final void run() {
                try {
                    doogal.eval(cmd, args);
                } catch (EvalException e) {
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
                } catch (EvalException e) {
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
                } catch (EvalException e) {
                    log.error(e.getLocalizedMessage());
                } catch (IOException e) {
                    log.error(e.getLocalizedMessage());
                } catch (ParseException e) {
                    log.error(e.getLocalizedMessage());
                }
            }
        });
    }

    public final void batch(final File file) throws EvalException,
            IOException, ParseException {
        executor.execute(new Runnable() {
            public final void run() {
                try {
                    doogal.batch(file);
                } catch (EvalException e) {
                    log.error(e.getLocalizedMessage());
                } catch (IOException e) {
                    log.error(e.getLocalizedMessage());
                } catch (ParseException e) {
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
                } catch (EvalException e) {
                    log.error(e.getLocalizedMessage());
                } catch (IOException e) {
                    log.error(e.getLocalizedMessage());
                } catch (ParseException e) {
                    log.error(e.getLocalizedMessage());
                }
            }
        });
    }
}