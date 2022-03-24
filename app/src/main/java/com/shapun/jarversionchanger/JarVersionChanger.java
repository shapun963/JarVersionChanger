package com.shapun.jarversionchanger;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Arrays;
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

    private  static final String HELP_TEXT = "JarVersionChanger [jarfile] [options] \n where jarfile is path of jar file whose version needs to be changed and options are : ";
    private final File mJarFile;
    private final File mOutputFile;
    private final int mMajorVersion;
    private final boolean mVerbose;

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
                if (entry.getName().endsWith(".class")) {
                    /*
                    8th byte carries information about major version
                    so basically this changes that byte to new version.
                    */
                    for (int i = 0; i < 7 && zis.available() > 0; i++) {
                        zos.write(zis.read());
                    }
                    int oldVersion = zis.read();
                    zos.write(mMajorVersion);
                    if (mVerbose) {
                        System.out.println("Changing version of "+ name+ " from "+ oldVersion + " to "+ mMajorVersion);
                    }
                }

                byte[] buffer = new byte[1024];
                int len;
                while ((len = zis.read(buffer)) != -1) {
                    zos.write(buffer, 0, len);
                }
            }
        } catch (IOException e) {
            System.err.println(e.getMessage());
            System.exit(1);
        }
    }

    public static void main(String... args) {
        Options options = new Options();
        Option optionHelp = new Option("h", "help", false, "Print this message.");
        options.addOption(optionHelp);

        Option optionVerbose = new Option("v", "verbose", false, "Enable verbose logging");
        options.addOption(optionVerbose);

        Option optionOutput =new Option("o","output",false,"Output path of the new modified jar file.");
        options.addOption(optionOutput);

        Option optionMajorVersion = new Option("major", "major-version", true, "New major version for the jar file. Eg : 51 for Java 7, 52 for Java 8, 53 for Java 9 .");
        optionMajorVersion.setType(Integer.class);
        options.addOption(optionMajorVersion);

        HelpFormatter formatter = new HelpFormatter();
        CommandLineParser parser = new DefaultParser();

        CommandLine cmd;

        try {
            cmd = parser.parse(options,args);
            File outputPath;
            int majorVersion = 0;
            boolean verbose = false;
            if(cmd.hasOption("h")){
                formatter.printHelp(HELP_TEXT, options);
                System.exit(0);
            }
            //Check jar file
            File jarFile = new File(args[0]);
            if (!(jarFile.exists() && jarFile.getAbsolutePath().endsWith(".jar"))) {
                System.err.println("File does not exist or is not a jar file");
                System.exit(1);
            }
            if(cmd.hasOption("major")) {
                    majorVersion = Integer.parseInt(cmd.getOptionValue("major-version"));
            }else{
                System.err.println("major-version is mandatory. Type --help for more info.");
                System.exit(1);
            }
            System.out.println(Arrays.toString(cmd.getArgs()));
            //Check if output is specified other wise create a new output
            if (cmd.hasOption("o")) {
                outputPath = new File(cmd.getOptionValue(optionOutput));
            } else {
                String name = jarFile.getName();
                String newName = name.substring(0, name.length() - 4) + "-ver-" + majorVersion+".jar";
                outputPath = new File(newName);
            }
            if (cmd.hasOption("v")) {
                verbose = true;
            }

            new JarVersionChanger(jarFile, outputPath, majorVersion, verbose).start();
            System.out.println("Modified jar file stored in  " + outputPath.getAbsolutePath());
        } catch (ParseException e) {
            System.err.println(e.getMessage());
            formatter.printHelp(HELP_TEXT, options);
            System.exit(1);
        }
    }
}
