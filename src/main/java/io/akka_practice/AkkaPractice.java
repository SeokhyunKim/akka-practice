package io.akka_practice;

import akka.actor.ActorSystem;
import ch.qos.logback.classic.LoggerContext;
import ch.qos.logback.core.util.StatusPrinter;
import com.typesafe.config.Config;
import com.typesafe.config.ConfigFactory;
import io.akka_practice.cluster.SimpleClusterListener;
import java.net.URL;
import java.net.URLClassLoader;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.DefaultParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.slf4j.LoggerFactory;

@Slf4j
public class AkkaPractice {

    private final static String SHELL_DIR = "shell.dir";

    private String shellDir;

    public void run(String[] args) {
        // assume SLF4J is bound to logback in the current environment
        LoggerContext lc = (LoggerContext) LoggerFactory.getILoggerFactory();
        // print logback's internal status
        StatusPrinter.print(lc);

        shellDir = System.getProperty(SHELL_DIR);
        if (shellDir == null) {
            log.info("Cannot get current shell directory. You should always use absolute directory.");
        }
        final CommandLine commandLine = parseCommandLine(args);
        if (commandLine == null) {
            printHelpMessage();
            return;
        }

        final String port = commandLine.getOptionValue("p", "0");
        log.info("port: {}", port);
        final Config config =
                ConfigFactory.parseString("akka.remote.netty.tcp.port=" + port + "\n" +
                                                  "akka.remote.artery.canonical.port=" + port)
                        .withFallback(ConfigFactory.load());

        final ActorSystem system = ActorSystem.create(AkkaPractice.class.getSimpleName(), config);
        system.actorOf(SimpleClusterListener.props(), "clusterListener");
    }

    private CommandLine parseCommandLine(final String[] args) {
        final CommandLineParser parser = new DefaultParser();
        try {
            return parser.parse(buildOptions(), args);
        } catch (ParseException e) {
            log.debug("{}", ExceptionUtils.getStackTrace(e));
            log.info("Exception was thrown while parsing command line arguments. For details, please see error logs.");
            return null;
        }
    }

    private void printHelpMessage() {
        final Options options = buildOptions();
        final HelpFormatter formatter = new HelpFormatter();
        formatter.printHelp("akka-practice", options);
    }

    private Options buildOptions() {
        final Options options = new Options();
        options.addOption(Option.builder("p")
                                  .longOpt("port")
                                  .desc("port number")
                                  .hasArg()
                                  .argName("PORT")
                                  .build());
        return options;
    }

    public static void main(String[] args) {
        ClassLoader cl = ClassLoader.getSystemClassLoader();

        URL[] urls = ((URLClassLoader)cl).getURLs();

        for(URL url: urls){
            System.out.println(url.getFile());
        }

        final AkkaPractice akkaPractice = new AkkaPractice();
        akkaPractice.run(args);
    }
}
