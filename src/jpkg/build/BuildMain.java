package jpkg.build;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Scanner;

import jpkg.Config;
import jpkg.fetch.FetchMain;

public class BuildMain {
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


		// Get dependencies
		String[] deps = build.getConfigFor("dependencies").split(";");

		// Get output jar
		String outputjar = build.getConfigFor("output-jar");
		
		ArrayList<String>depjars = new ArrayList<>();
		
		for(String s : deps) {
			if(s.isEmpty())
				continue;
			depjars.add(FetchMain.run(new String[] {s}));
		}

		// Build the stuff
		compile(buildpath, depjars, outputjar);

		
		
		// Give the jar path back
		try {
			StringBuilder path = new StringBuilder(new File(buildpath + "/bin").getCanonicalPath() + "/" + outputjar);
			
			for(String s : depjars) {
				path.append(' ');
				path.append(s);
			}
			
			return path.toString();
		} catch (IOException e) {
			e.printStackTrace();
			return null;
		}
	}

	public static void compile(String path, ArrayList<String> libjars, String outputjar) {
		try {
			ArrayList<String> sb = new ArrayList<>();
			compileRec(sb, path);

			StringBuilder classeslist = new StringBuilder();
			StringBuilder jarslist = new StringBuilder();
			for(String s : sb) {
				classeslist.append(s);
				classeslist.append(' ');
			}

			for(String s : libjars) {
				jarslist.append(s);
				jarslist.append(File.pathSeparatorChar);
			}
			
			System.out.println("Building " + sb.size() + " classes with " + libjars.size() + " libraries");
			
			executeCommand("javac " + ((libjars.size() != 0) ? "-cp \"" + jarslist + "\" " : " ") + classeslist, new File(path));
			
			System.out.println("Copying classes from /src to /bin/build");
			copyClasses(path + "/src", path + "/bin/build");
			
			executeCommand("jar cf ../" + outputjar + " .", new File(path + "/bin/build"));
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

	private static boolean executeCommand(String command, File dirIn) {
		try {
			
			System.out.println("Running `" + command.substring(0, command.length() > 100 ? 100 : command.length()) + (command.length() > 100 ? "..." : "") + "` in " + dirIn.getCanonicalPath());
		} catch (IOException e1) {
			e1.printStackTrace();
		}

		StringBuffer output = new StringBuffer();

		Process p = null;
		try {

			p = Runtime.getRuntime().exec(command, null, dirIn);

			BufferedReader reader =
					new BufferedReader(new InputStreamReader(p.getErrorStream()));
			BufferedReader reader2 =
					new BufferedReader(new InputStreamReader(p.getInputStream()));

			String line = "";
			while (p.isAlive()) {
				if(reader.ready())
					System.err.println(reader.readLine());
				else if(reader2.ready())
					System.out.println(reader2.readLine());
				else
					continue;
			}

		} catch (Exception e) {
			e.printStackTrace();
		}

		return p.exitValue() == 0;

	}
	
	public static void copyClasses(String from, String to) {
		File fromfile = new File(from);
		File tofile = new File(to);
		
		if(!tofile.exists())
			tofile.mkdirs();
		
		for(File f : fromfile.listFiles()) {
			if(f.isDirectory())
				copyClasses(from + "/" + f.getName(), to + "/" + f.getName());
			else if(f.isFile() && f.getName().endsWith(".class")) {
				f.renameTo(new File(to + "/" + f.getName()));
			}
		}
	}
}
