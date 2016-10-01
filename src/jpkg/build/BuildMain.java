package jpkg.build;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Scanner;

import jpkg.config.Config;
import jpkg.fetch.FetchMain;
import jpkg.io.SimpleIO;
import static jpkg.sys.ExecCmd.executeCommand;

public class BuildMain {
	public static HashSet<String> modules = new HashSet<>();

	public static String run(String[] args) {
		// Someone didn't read the man pages
		if(args.length > 1) {
			System.err.print("Expected path as only argument (optional)!");
			return null;
		}

		String buildpath;

		// Where to actually do the building
		if(args.length == 0)
			buildpath = ".";
		else
			buildpath = args[0];

		if(!modules.contains(args[0]))
			modules.add(args[0]);
		else {
			System.err.println("Module " + args[0] + " already included! Skipping...");

			return "";
		}
		File buildfile = new File(buildpath + "/build.jpk");
		String bdir = null;
		try{
			bdir = new File(buildpath).getCanonicalPath();
		} catch (IOException e) {
			e.printStackTrace();
		}

		System.out.println("Building in " + bdir);

		// It's not actually a module!
		if(!buildfile.exists()) {
			System.err.println("No build.jpk found in " + bdir + "! Skipping..");

			return null;
		}

		// Read build file using config
		Scanner sc = null;
		try {
			sc = new Scanner(buildfile);
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}
		Config build = new Config();
		build.populate(sc);

		String ws = SimpleIO.readSwallowed(new File(buildpath + "/bin/BRANCH"), "build", false);

		// Get dependencies
		String[] deps = build.getConfigFor("dependencies").split(";");

		// Get output jar
		String outputjar = build.getConfigFor("output-jar").replace("${branch}", ws);

		HashSet<String>depjars = new HashSet<>();

		for(String s : deps) {
			if(s.isEmpty())
				continue;
			depjars.addAll(Arrays.asList(FetchMain.run(new String[] {s}).split(";")));
		}

		String buildcommand = build.getConfigFor("build-command");

		// Build the stuff
		if(buildcommand == null)
			compile(buildpath, depjars, outputjar);
		else if (System.getProperty("os.name").startsWith("Windows")) {
			// includes: Windows 2000,  Windows 95, Windows 98, Windows NT, Windows Vista, Windows XP
			executeCommand("cmd /c " + buildcommand
					.replace("${depjars}", String.join(File.pathSeparator, depjars)), 
					new File(buildpath));
		} else {
			// everything else (REAL computers)
			executeCommand("sh -c " + buildcommand
					.replace("${depjars}", String.join(File.pathSeparator, depjars)), 
					new File(buildpath));
		} 



		// Give the jar path back
		try {
			StringBuilder path = new StringBuilder(new File(buildpath + "/" + outputjar).getCanonicalPath());
			System.out.println("Adding jar to classpath: " + path);
			// System.out.println("Building with jars: " + depjars);
			for(String s : depjars) {
				path.append(File.pathSeparatorChar);
				path.append(s);
			}

			File f = new File(buildpath + "/bin/DEPS");
			new File(buildpath + "/bin").mkdir();
			FileWriter fw = new FileWriter(f.getCanonicalFile());
			BufferedWriter bw = new BufferedWriter(fw);

			bw.write(path.toString());
			bw.close();

			return path.toString();
		} catch (IOException e) {
			e.printStackTrace();
			return null;
		}
	}

	public static void compile(String path, HashSet<String> depjars, String outputjar) {
		try {
			ArrayList<String> sb = new ArrayList<>();
			compileRec(sb, path);

			String classeslist = String.join(" ", sb);
			String jarslist = String.join(File.pathSeparator, depjars);

			System.out.println("Building " + sb.size() + " classes with " + depjars.size() + " libraries");
			File r = new File(path + "/bin/build");
			r.mkdirs();

			executeCommand("javac -g:source -d " + r.getCanonicalPath() + " " + ((depjars.size() != 0) ? "-cp \"" + jarslist + "\" " : " ") + classeslist, new File(path));

			// System.out.println("Copying classes from /src to /bin/build");
			// copyClasses(path + "/src", path + "/bin/build");

			executeCommand("jar cf " + path + "/" + outputjar + " .", new File(path + "/bin/build"));
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	private static void compileRec(ArrayList<String> sb, String path) {
		File dir = new File(path);
		for(File f : dir.listFiles()) {
			if(f.isDirectory())
				compileRec(sb, f.getAbsolutePath());
			else if(f.isFile()) {
				String[]w = f.getName().split("\\.");
				if(w.length == 0)
					continue;
				String ext = w[w.length - 1];
				if(ext.equals("java")) {
					sb.add(f.getAbsolutePath());
				}
			}

		}
	}
}
