package jpkg.test;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.Arrays;
import java.util.Scanner;

import jpkg.config.Config;
import jpkg.build.BuildMain;

import static jpkg.sys.ExecCmd.executeCommand;

public class TestMain {

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
			try {
				
				Scanner sc = new Scanner(f);
				StringBuilder sb = new StringBuilder();
				while(sc.hasNext())
					sb.append(sc.nextLine());
				s = sb.toString();
				sc.close();
				
			} catch (FileNotFoundException e) {
				e.printStackTrace();
				return;
			}
		}
		
		File buildfile = new File(buildpath + "/build.jpk");
		
		Config build = new Config();
		build.populate(buildfile);
		
		String maintype = build.getConfigFor("maintype-test");
		
		if(maintype == null) {
			System.err.println("No maintype in selection!");
			return;
		}
		
		executeCommand("java -cp \"" + s + "\" " + maintype + " " + String.join(
				" s", prargs), new File(buildpath));
	}

}
