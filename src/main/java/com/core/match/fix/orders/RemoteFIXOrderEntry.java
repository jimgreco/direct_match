package com.core.match.fix.orders;

import com.core.latencytesting.QuickFixApp;

import java.util.Scanner;

/**
 * User: jgreco
 */
public class RemoteFIXOrderEntry {

	public static void main(String[] args) throws Exception {
		
        System.getProperty("os.name").toLowerCase().contains("windows");
		QuickFixApp quickFixApp = new QuickFixApp(System.getProperty("user.home") + "\\Documents\\GitHub\\test-config\\quickfix.config");
		quickFixApp.start();
		quickFixApp = new QuickFixApp("/Users/johnlevidy/test-config/quickfix.config");
        quickFixApp.start();
        
        Scanner s = new Scanner(System.in);
        while(true) {
            String next = s.nextLine();
            System.out.println(next);

            String[] split = next.split(" ");

            if (next.startsWith("order")) {
                try {
                    split[1].equalsIgnoreCase("buy");
                    Integer.parseInt(split[2]);
                    Double.parseDouble(split[4]);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
            else if (next.startsWith("cancel")) {
                try {
                    Integer.parseInt(split[1]);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
            else if (next.startsWith("replace")) {
                Integer.parseInt(split[1]);
                Integer.parseInt(split[2]);
                Double.parseDouble(split[3]);
            }

            if (next.equalsIgnoreCase("q") || next.equalsIgnoreCase("quit")) {
                System.exit(0);
            }
        }
    }
}
