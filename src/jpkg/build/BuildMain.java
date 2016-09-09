package jpkg.build;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
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
		
		// It's not actually a module!
		if(!buildfile.exists()) {
			try {
				System.err.println("No build.jpk found in " + new File(buildpath).getCanonicalPath() + "! Skipping..");
			} catch (IOException e) {
				e.printStackTrace();
			}
			
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
		
		String[] deps = build.getConfigFor("dependencies").split(";");
		for(String s : deps)
			FetchMain.run(new String[] {s});
		
		// Give the jar path back
		try {
			return new File(build.getConfigFor("jar-output")).getCanonicalPath();
		} catch (IOException e) {
			e.printStackTrace();
			return null;
		}
	}
}
