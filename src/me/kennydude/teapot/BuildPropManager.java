package me.kennydude.teapot;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Reader;
import java.util.Properties;

import android.os.Environment;

public class BuildPropManager extends Properties {
	public static interface Logger{
		public void log(String message);
	}
	
	private static final long serialVersionUID = 8978406638559359591L;

	Logger logger;
	String propReplaceFile;
	
	public BuildPropManager(Logger l){
		this.logger = l;
		propReplaceFile = Environment.getExternalStorageDirectory().getAbsolutePath() + "/propreplace.txt";
		
		log("Starting. Making copy of build.prop");
		backup();
		
		log("Making tempoary copy");
		makeTemp();
		
		File file = new File(tempFile);
    	try {
    		load(new FileInputStream(file));
    		log("loaded prop with "+ this.size()+ " elements");
		} catch (IOException e) {
			log("Error opening file: " + e);
		}
    	
    	log("Ready for changes");
	}
	
	public void log(String message){
		logger.log(message);
	}
	
	public void save(){
		log("Saving to tempoary output");
		try {
    		FileOutputStream out = new FileOutputStream(new File(tempFile));
    		store(out, null);
    		out.close();

    		replaceInFile(new File(tempFile));
    		
    		log("Saving to system");
    		transferFileToSystem();
		} catch (IOException e) {
			log("Error saving file: " + e);
		}
	}
	
	String tempFile;
	
	public void backup(){
		Process process = null;
        DataOutputStream os = null;
        
		try {
            process = Runtime.getRuntime().exec("su");
	        os = new DataOutputStream(process.getOutputStream());
	        os.writeBytes("mount -o remount,rw /system\n");
	        os.writeBytes("cp -f /system/build.prop " + Environment.getExternalStorageDirectory().getAbsolutePath() + "/build.prop.bak\n");
	        os.writeBytes("mount -o remount,ro /system\n");
	        os.writeBytes("exit\n");
	        os.flush();
	        process.waitFor();
        } catch (Exception e) {
            log("Error in backup: " + e.getMessage());
        } finally {
            try {
                if (os != null) {
                    os.close();
                }
                process.destroy();
            } catch (Exception e) {
            	log("Error in closing backup process: " + e.getMessage());
            }
        }
        
    	log("build.prop Backup at " + Environment.getExternalStorageDirectory().getAbsolutePath() + "/build.prop.bak");
	}
	
	public void makeTemp(){
		Process process = null;
        DataOutputStream os = null;
        
		try {
            process = Runtime.getRuntime().exec("su");
	        os = new DataOutputStream(process.getOutputStream());
	        os.writeBytes("mount -o remount,rw /system\n");
	        os.writeBytes("cp -f /system/build.prop " + Environment.getExternalStorageDirectory().getAbsolutePath() + "/buildprop.tmp\n");
	        os.writeBytes("chmod 777 " + Environment.getExternalStorageDirectory().getAbsolutePath() + "/buildprop.tmp\n");
	        os.writeBytes("mount -o remount,ro /system\n");
	        os.writeBytes("exit\n");
	        os.flush();
	        process.waitFor();
        } catch (Exception e) {
            log("Error in making temp file: " + e.getMessage());
        } finally {
            try {
                if (os != null) {
                    os.close();
                }
                process.destroy();
            } catch (Exception e) {
            	log("Error in closing temp process: " + e.getMessage());
            }
        }
        
        tempFile = Environment.getExternalStorageDirectory().getAbsolutePath() + "/buildprop.tmp";
	}
	
	private void transferFileToSystem() {
    	Process process = null;
        DataOutputStream os = null;
        
        try {
            process = Runtime.getRuntime().exec("su");
	        os = new DataOutputStream(process.getOutputStream());
	        os.writeBytes("mount -o remount,rw -t yaffs2 /dev/block/mtdblock4 /system\n");
	        os.writeBytes("mv -f /system/build.prop /system/build.prop.bak\n");
	        os.writeBytes("cp -f " + propReplaceFile + " /system/build.prop\n");
	        os.writeBytes("chmod 644 /system/build.prop\n");
	        os.writeBytes("mount -o remount,rw /system\n");
	        //os.writeBytes("mount -o remount,ro -t yaffs2 /dev/block/mtdblock4 /system\n");
	        //os.writeBytes("rm " + propReplaceFile);
	        //os.writeBytes("rm " + tempFile);
	        os.writeBytes("exit\n");
	        os.flush();
	        process.waitFor();
        } catch (Exception e) {
            log("Error: " + e.getMessage());
        } finally {
            try {
                if (os != null) {
                    os.close();
                }
                process.destroy();
            } catch (Exception e) {
            	log( "Error: " + e.getMessage() );
            }
        }
        
    	log( "Edit saved and a backup was made at " + Environment.getExternalStorageDirectory().getAbsolutePath() + "/build.prop.bak" );
    }
	
	private void replaceInFile(File file) throws IOException {
		File tmpFile = new File(propReplaceFile);
		FileWriter fw = new FileWriter(tmpFile);
		Reader fr = new FileReader(file);
		BufferedReader br = new BufferedReader(fr);

		while (br.ready()) {
			fw.write(br.readLine().replaceAll("\\\\", "") + "\n");
		}

		fw.close();
		br.close();
		fr.close();
	}
	
}
