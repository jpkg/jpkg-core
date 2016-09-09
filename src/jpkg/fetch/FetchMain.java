package jpkg.fetch;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;

import jpkg.Main;
import jpkg.build.BuildMain;

public class FetchMain {
	
	public static void run(String[] args) {
		// Someone didn't read the man pages
		if(args.length == 0) {
			System.err.print("Expected module to fetch as next argument!");
			return;
		}
		
		String hosts = Main.mainconfig.getConfigFor("hosts");
		
		String[] branchsplit = args[0].split("#");
		
		String branch = "master";
		
		// Did user specify a branch? If so, then actually listen to them
		if(branchsplit.length == 2)
			branch = branchsplit[1];
		
		String[] hostlist = getHosts(branchsplit[0], hosts);
		String[] w = branchsplit[0].split("/");
		
		// Directory of ${repo-dir}/${user}
		File repodir = new File(Main.mainconfig.getConfigFor("repository-directory")
				+ ((w.length == 1) ? "jpkg/" : w[0]));
		
		// If repository doesn't exist, make it
		if(!repodir.exists())
			repodir.mkdirs();
		
		File k;
		
		// Directory of repo if it already exists (the directory ${repo-dir}/${user}/${repo})
		String repo = repodir.getAbsolutePath() + "/" + w[w.length - 1];
		
		
		if((k = new File(repo)).exists()) {		// If repository already exists, just refresh it
			executeCommand("git pull --all", k);
			
		} else for(String host : hostlist) 		// Otherwise, clone it
			if(executeCommand("git clone " + host, repodir)) break;
		
		executeCommand("git checkout " + branch, k);	// Checkout right branch because it's probably needed
		
		// Finally, build the repo
		BuildMain.run(new String[] {repo});
	}
	
	public static String[] getHosts(String arg, String hosts) {
		String[] branchsplit = arg.split("#");
		String[] tofetch = branchsplit[0].split("/");
		

		
		// Someone didn't read the URI specifications...
		if(tofetch.length > 2) {
			System.err.println("Invalid fetch target " + arg + "!");
			throw new RuntimeException();
		}
		
		String user = null;
		String proj = null;
		
		if(tofetch.length == 1)
			user = "jpkg";
		else
			user = tofetch[0];
		
		proj = tofetch[tofetch.length - 1];
		
		hosts = hosts.replace("${user}", user)
				.replace("${proj}", proj);
		
		
		return hosts.split(";");
	}
	
	private static boolean executeCommand(String command, File dirIn) {
		try {
			System.out.println("Running `" + command + "` in " + dirIn.getCanonicalPath());
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
					System.out.println(reader.readLine());
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
}
