import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import java.util.zip.ZipOutputStream;
import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.DefaultParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;

public class JarVersionChanger {

    private File mJarFile;
    private File mOutputFile;
    private int mMajorVersion;
    private boolean mVerbose = false;

    public JarVersionChanger(File jarFile, File outputFile, int majorVersion, boolean verbose) {
        mJarFile = jarFile;
        mOutputFile = outputFile;
        mMajorVersion = majorVersion;
        mVerbose = verbose;
    }

    public void start() {
        try (ZipInputStream zis = new ZipInputStream(new FileInputStream(mJarFile));
                ZipOutputStream zos = new ZipOutputStream(new FileOutputStream(mOutputFile))) {
            ZipEntry entry;
            while ((entry = zis.getNextEntry()) != null) {
				String name = entry.getName();
                zos.putNextEntry(new ZipEntry(name));
                System.out.println("Skipping " + entry.getName() + " as its is not a class file.");
                if (entry.getName().endsWith(".class")) {
                    /*
                    8th byte carries information about major version
                    so basically this change that byte to new version.
                    */
                    for (int i = 0; i < 7 && zis.available() > 0; i++) {
                        zos.write(zis.read());
                    }
                    int oldVersion = zis.read();
                    zos.write(mMajorVersion);
					System.out.println("Changing version of "+ name+ " from "+ oldVersion+ " to "+ mMajorVersion);
                }

                byte[] buffer = new byte[1024];
                int len;
                while ((len = zis.read(buffer)) != -1) {
                    zos.write(buffer, 0, len);
                }
            }
        } catch (IOException e) {
            System.err.println(e.toString());
            System.exit(1);
        }
    }

    public static void main(String... args) throws Throwable {
        Options options = new Options();

        Option optionVerbose = new Option("v", "verbose", false, "If this ");
        optionVerbose.setRequired(false);
        options.addOption(optionVerbose);

        Option optionOutput = new Option("o", "output", false, "Output path of the new jar file .");
        optionOutput.setRequired(false);
        options.addOption(optionOutput);

        Option optionMajorVersion = new Option(null, "major-version", true, "Specify the major ");
        optionMajorVersion.setRequired(true);
        options.addOption(optionMajorVersion);

        HelpFormatter formatter = new HelpFormatter();
        CommandLineParser parser = new DefaultParser();
        CommandLine cmd;

        File jarFile = new File(args[0]);
        if (!(jarFile.exists() && jarFile.getAbsolutePath().endsWith(".jar"))) {
            System.err.println("File does not exist or is not a jar file");
            System.exit(1);
        }

        try {
            cmd = parser.parse(options, args, false);
            File outputPath;
            int majorVersion = 0;
            boolean verbose = false;
            try {
                majorVersion = Integer.parseInt(cmd.getOptionValue("major-version"));
            } catch (NumberFormatException e) {
                System.err.println("major-version only accepts integer values ");
                System.exit(1);
            }

            if (cmd.hasOption("o")) {
                outputPath = new File(cmd.getOptionValue(optionOutput));
            } else {
                String name = jarFile.getName();
                outputPath =
                        new File(
                                name.substring(0, name.length() - 4)
                                        + "-ver-"
                                        + majorVersion
                                        + ".jar");
            }
            if (cmd.hasOption("v")) {
                verbose = true;
            }

            new JarVersionChanger(jarFile, outputPath, majorVersion, verbose).start();
            System.out.println("Modified jar file stored in  " + outputPath.getAbsolutePath());
        } catch (ParseException e) {
            System.out.println(e.getMessage());
            formatter.printHelp("", options);
            System.exit(1);
        }
    }
}
