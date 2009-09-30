package org.doogal.console;

import static org.doogal.core.Constants.PROMPT;
import static org.doogal.core.Utility.printResource;

import java.io.PrintWriter;
import org.apache.commons.logging.Log;
import org.doogal.core.Doogal;
import org.doogal.core.Environment;
import org.doogal.core.EvalException;
import org.doogal.core.ExitException;
import org.doogal.core.Interpreter;
import org.doogal.core.Shellwords;
import org.doogal.core.StandardLog;
import org.doogal.core.SyncDoogal;

public final class Main {

    public static void main(String[] args) throws Exception {

        final PrintWriter out = new PrintWriter(System.out, true);
        final PrintWriter err = new PrintWriter(System.err, true);
        final Log log = new StandardLog(out, err);
        final Environment env = new Environment();
        final Doogal doogal = SyncDoogal.newInstance(out, log, env);
        try {
            printResource("motd.txt", out);
            doogal.readConfig();
            doogal.setDefault("next");
            out.print(PROMPT);
            out.flush();
            Shellwords.parse(System.in, new Interpreter() {
                public final void eval(String cmd, Object... args)
                        throws EvalException {
                    doogal.eval(cmd, args);
                    out.print(PROMPT);
                    out.flush();
                }

                public final void eval() throws EvalException {
                    doogal.eval();
                    out.print(PROMPT);
                    out.flush();
                }
            });
        } catch (final ExitException e) {
        } finally {
            doogal.close();
        }
    }
}
