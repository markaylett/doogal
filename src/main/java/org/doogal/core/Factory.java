package org.doogal.core;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.lang.reflect.InvocationTargetException;

import javax.mail.internet.ParseException;

import org.apache.commons.logging.Log;

public final class Factory {
    private Factory() {
    }

    private static final Doogal newDoogal(PrintWriter out, PrintWriter err,
            Log log, Environment env, Repo repo) throws EvalException,
            IllegalAccessException, InvocationTargetException, IOException {
        final Model model = new Model(out, err, log, env, repo);
        Doogal doogal = null;
        try {
            doogal = new Doogal(model);
        } finally {
            if (null == doogal)
                model.close();
        }
        // Now owns the session.
        return doogal;
    }

    public static Doogal newDoogal(PrintWriter out, PrintWriter err, Log log,
            Environment env) throws EvalException, IllegalAccessException,
            InvocationTargetException, IOException, ParseException {
        final Repo repo = new Repo(env.getRepo());
        repo.init();
        final Doogal doogal = newDoogal(out, err, log, env, repo);
        boolean done = false;
        try {
            final File conf = new File(repo.getEtc(), "doogal.conf");
            if (conf.canRead()) {
                final FileReader reader = new FileReader(conf);
                try {
                    Shellwords.parse(reader, doogal);
                } finally {
                    reader.close();
                }
            }
            done = true;
        } finally {
            if (!done)
                doogal.close();
        }
        return doogal;
    }
}
