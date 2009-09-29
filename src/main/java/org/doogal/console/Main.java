package org.doogal.console;

import static org.doogal.core.Constants.PROMPT;
import static org.doogal.core.Utility.printResource;

import java.io.PrintWriter;

import org.apache.commons.logging.Log;
import org.doogal.core.Environment;
import org.doogal.core.EvalException;
import org.doogal.core.ExitException;
import org.doogal.core.Factory;
import org.doogal.core.Interpreter;
import org.doogal.core.Shellwords;
import org.doogal.core.StandardLog;

public final class Main {

    public static void main(String[] args) throws Exception {

        final PrintWriter out = new PrintWriter(System.out, true);
        final PrintWriter err = new PrintWriter(System.err, true);
        final Log log = new StandardLog(out, err);
        final Environment env = new Environment();

        printResource("motd.txt", out);
        final Interpreter doogal = Factory.newDoogal(out, err, log, env);
        try {
            out.print(PROMPT);
            out.flush();
            Shellwords.parse(System.in, new Interpreter() {
                public final void close() {

                }

                public final void eval(String cmd, Object... args)
                        throws EvalException {
                    doogal.eval(cmd, args);
                    out.print(PROMPT);
                    out.flush();
                }

                public final void eval() throws EvalException {
                    doogal.eval("next");
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
