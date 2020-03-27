package network.palace.dashboard.log;

import jline.console.ConsoleReader;
import org.fusesource.jansi.Ansi;

import java.io.IOException;
import java.io.OutputStream;
import java.util.logging.Level;
import java.util.logging.Logger;

public class TerminalConsoleWriterThread extends Thread {
    private final OutputStream output;
    private final ConsoleReader reader;

    public TerminalConsoleWriterThread(OutputStream output, ConsoleReader reader) {
        super("TerminalConsoleWriter");
        this.output = output;
        this.reader = reader;

        this.setDaemon(true);
    }

    @Override
    public void run() {
        String message;

        //noinspection InfiniteLoopStatement
        while (true) {
            message = QueueLogAppender.getNextLogEvent("TerminalConsole");
            if (message == null) continue;

            try {
                reader.print(Ansi.ansi().eraseLine(Ansi.Erase.ALL).toString() + ConsoleReader.RESET_LINE);
                reader.flush();
                output.write(message.getBytes());
                output.flush();

                try {
                    reader.drawLine();
                } catch (Throwable ex) {
                    reader.getCursorBuffer().clear();
                }
                reader.flush();
            } catch (IOException ex) {
                Logger.getLogger(TerminalConsoleWriterThread.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
    }
}
