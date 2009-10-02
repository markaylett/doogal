package org.doogal.console;

import static org.doogal.core.Constants.PROMPT;
import static org.doogal.core.Utility.printResource;

import java.io.PrintWriter;
import org.apache.commons.logging.Log;
import org.doogal.core.Controller;
import org.doogal.core.Doogal;
import org.doogal.core.Environment;
import org.doogal.core.ExitException;
import org.doogal.core.PrintView;
import org.doogal.core.Shellwords;
import org.doogal.core.StandardLog;
import org.doogal.core.SyncDoogal;
import org.doogal.core.View;

public final class Main {

    private static final class PrintController implements Controller {

        private final PrintWriter out;
        private final Log log;

        PrintController(PrintWriter out, Log log) {
            this.out = out;
            this.log = log;
        }

        public final void exit(boolean interact) throws ExitException {
            if (interact)
                log.info("exiting...");
            throw new ExitException();
        }

        public final void ready() {
            out.print(PROMPT);
            out.flush();
        }
    }

    public static void main(String[] args) throws Exception {

        final Environment env = new Environment();
        final PrintWriter out = new PrintWriter(System.out, true);
        final PrintWriter err = new PrintWriter(System.err, true);
        final Log log = new StandardLog(out, err);
        final View view = new PrintView(out, log);
        final Controller controller = new PrintController(out, log);
        final Doogal doogal = SyncDoogal.newInstance(env, view, controller);
        try {
            printResource("motd.txt", out);
            try {
                doogal.config();
            } catch (ExitException e) {
            }
            Shellwords.parse(System.in, doogal);
        } catch (ExitException e) {
        } finally {
            doogal.close();
        }
    }
}
