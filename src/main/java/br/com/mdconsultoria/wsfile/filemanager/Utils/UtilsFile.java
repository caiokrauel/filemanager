package br.com.mdconsultoria.wsfile.filemanager.Utils;

import java.io.File;
import java.security.MessageDigest;

import org.apache.commons.io.FileUtils;

public class UtilsFile {
	
	public static File findFileByName(String pathSource, String fileName) throws Exception {
		try {
			File[] fileList = new File(pathSource).listFiles();
            for (File file : fileList) 
            	if (file.getName().equals(fileName)) 
            		return file;
       
            return null;
            
		} catch (Exception e) {
			throw new Exception("UtilsFile - findFileByName() - Error: " + e.getMessage());
		}
	}
	
	public static String MD5(byte[] fileBytes) throws Exception {
		try {
		    byte[] digest = MessageDigest.getInstance("MD5").digest(fileBytes);
		    StringBuffer sb = new StringBuffer();
	        for (byte b : digest)
	            sb.append(String.format("%02x", b & 0xff));
	        return sb.toString();
	    } catch (Exception e) {
	    	throw new Exception("UtilsFile - MD5() - Error: " + e.getMessage());
		}
	}
	
	public static String MD5(File file) throws Exception {
		try {
			return MD5(FileUtils.readFileToByteArray(file));
		} catch (Exception e) {
	    	throw new Exception(e.getMessage());
		}
	}
	
	public static String MD5(String pathSource, String fileName) throws Exception {
		try {
			File file = findFileByName(pathSource, fileName);
			return MD5(file);
		} catch (Exception e) {
	    	throw new Exception(e.getMessage());
		}
	}

}
