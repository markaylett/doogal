package org.doogal.notes.console;

import static org.doogal.notes.core.Utility.printResource;
import static org.doogal.notes.domain.Constants.PROMPT;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.PrintWriter;
import java.lang.reflect.InvocationTargetException;

import javax.mail.internet.ParseException;

import org.apache.commons.logging.Log;
import org.doogal.notes.core.Controller;
import org.doogal.notes.core.Doogal;
import org.doogal.notes.core.Environment;
import org.doogal.notes.core.ExitException;
import org.doogal.notes.core.PromptDoogal;
import org.doogal.notes.core.Repo;
import org.doogal.notes.core.SyncDoogal;
import org.doogal.notes.util.Shellwords;
import org.doogal.notes.util.StandardLog;
import org.doogal.notes.view.PrintView;
import org.doogal.notes.view.View;

public final class Main {

    private static final class PrintController implements Controller {

        private final PrintWriter out;
        private final Log log;

        PrintController(PrintWriter out, Log log) {
            this.out = out;
            this.log = log;
        }

        public final void exit(boolean interact) {
            if (interact)
                log.info("exiting...");
            throw new ExitException();
        }

        public final void ready() {
            out.print(PROMPT);
            out.flush();
        }
    }

    public static void main(String[] args) throws IllegalAccessException,
            InvocationTargetException, IOException, ParseException {

        System.setProperty("line.separator", "\n");
        final Environment env = new Environment();
        final PrintWriter out = new PrintWriter(System.out, true);
        final PrintWriter err = new PrintWriter(System.err, true);
        final Log log = new StandardLog(out, err);
        final View view = new PrintView(out, log);
        final Controller controller = new PrintController(out, log);

        final Repo repo = new Repo(env.getRepo());
        repo.init();
        final Doogal doogal = new PromptDoogal(controller, new SyncDoogal(env,
                view, controller, repo));
        try {
            printResource("motd.txt", out);
            try {
                doogal.config();
            } catch (final ExitException e) {
            }
            Shellwords.parse(System.in, doogal);
        } catch (final ExitException e) {
        } finally {
            doogal.destroy();
        }
    }
}
