package jpkg.run;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Arrays;
import java.util.Scanner;

import jpkg.config.Config;
import jpkg.io.SimpleIO;
import jpkg.build.BuildMain;
import static jpkg.sys.ExecCmd.executeCommand;

public class RunMain {
	public static void run(String[] args) {
		String s = null;

		
		String[] prargs;
		
		String buildpath;

		// Where to actually do the building
		if(args.length == 0) {
			buildpath = ".";
			prargs = args;
		} else {
			buildpath = args[0];
			prargs = Arrays.copyOfRange(args, 1, args.length);
		}
			
		File f = new File(buildpath + "/bin/DEPS");
		
		// Get jarlist
		if(!f.exists() || args.length > 1 && args[1].equals("--force-build"))
			s = BuildMain.run(new String[] {args[0]});
		
		if(s == null) {
			s = SimpleIO.readSwallowed(f, null, true);
			if(s == null)
				return;
		}
		
		File buildfile = new File(buildpath + "/build.jpk");
		
		Scanner sc = null;
		try {
			sc = new Scanner(buildfile);
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}
		Config build = new Config();
		build.populate(sc);
		
		String maintype = build.getConfigFor("maintype");
		
		if(maintype == null) {
			System.err.println("No maintype in selection!");
			return;
		}
		
		executeCommand("java -cp \"" + s + "\" " + maintype + " " + String.join(
				" s", prargs), new File(buildpath));
	}
}
