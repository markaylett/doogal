package org.doogal.console;

import java.io.File;
import java.io.FileReader;
import java.io.PrintWriter;

import org.apache.commons.logging.Log;
import org.doogal.core.Doogal;
import org.doogal.core.Environment;
import org.doogal.core.EvalException;
import org.doogal.core.ExitException;
import org.doogal.core.Interpreter;
import org.doogal.core.Repo;
import org.doogal.core.ResetException;
import org.doogal.core.Session;
import org.doogal.core.Shellwords;
import org.doogal.core.StandardLog;

import static org.doogal.core.Constants.*;
import static org.doogal.core.Utility.*;

public final class Main {
    public static void main(String[] args) throws Exception {

        final PrintWriter out = new PrintWriter(System.out, true);
        final PrintWriter err = new PrintWriter(System.err, true);
        printResource("motd.txt", out);

        final Environment env = new Environment();
        final Log log = new StandardLog(out, err);
        for (;;) {
            final Repo repo = new Repo(env.getRepo());
            repo.init();
            final Session s = new Session(out, err, log, env, repo);
            try {
                final Doogal m = new Doogal(s);
                final File conf = new File(repo.getEtc(), "doogal.conf");
                if (conf.canRead()) {
                    final FileReader reader = new FileReader(conf);
                    try {
                        Shellwords.readLine(reader, m);
                    } finally {
                        reader.close();
                    }
                }
                out.print(PROMPT);
                out.flush();
                Shellwords.readLine(System.in, new Interpreter() {
                    public final void eval(String cmd, Object... args)
                            throws EvalException {
                        m.eval(cmd, args);
                        out.print(PROMPT);
                        out.flush();
                    }

                    public final void eval() throws EvalException {
                        m.eval("next");
                        out.print(PROMPT);
                        out.flush();
                    }
                });
            } catch (final ExitException e) {
                break;
            } catch (final ResetException e) {
                log.info("resetting...");
            } finally {
                s.close();
            }
        }
    }
}
