package com.kyhsgeekcode.fixzip;

import android.Manifest;
import android.content.pm.PermissionInfo;
import android.util.*;
import java.io.*;
import java.lang.reflect.*;
import java.security.Permissions;
import java.util.*;
import java.util.zip.*;

public class FixZip
{
	public static void Run(MainActivity a)
	{
		String path = "";
		File file=new File("");
		File logfile=new File("/sdcard/fixzip.log");
		String yn="";
		boolean archive=false;
		boolean hasPermission=false;
		a.requestPermissions(new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE,Manifest.permission.READ_EXTERNAL_STORAGE},2000);
		synchronized (a)
		{
			try {
				a.wait();
			} catch (InterruptedException e) {
				a.print(Log.getStackTraceString(e));
			}
		}
		do{
			a.print(a.getString(R.string.rezipq));
			try
			{
				yn = a.readLine();
				if("y".equalsIgnoreCase(yn))
					archive=true;
				else if("n".equalsIgnoreCase(yn))
					archive=false;
				else
					continue;
				break;
			}
			catch (InterruptedException e)
			{}
		}while(true);
		do{	
			a.print(a.getString(R.string.enterpath));
			try
			{
				path = a.readLine();
			}
			catch (InterruptedException e)
			{
				a.print(Log.getStackTraceString(e));
			}
			file = new File(path);
		}while(!file.exists());
		final int BUFFER_SIZE = 1024;
		BufferedOutputStream dest = null;
		FileOutputStream logfos = null;
		try
		{
			 logfos=new FileOutputStream(logfile);
		}
		catch (FileNotFoundException e)
		{
			Log.e("FixZip","log error",e);
			a.print(a.getString(R.string.logfileerr));
		}
		try
		{
			FileInputStream fis = new FileInputStream(file);
			ZipInputStream zis = new ZipInputStream(new BufferedInputStream(fis));
			ZipEntry entry;
			File outDir=new File(file.getParentFile(),file.getName()+"out/");
			File outZip=new File(file.getParentFile(),file.getName()+".zip");
			ZipOutputStream zos=null;
			if(archive)
				zos=new ZipOutputStream(new FileOutputStream(outZip));
			File destFile;
			entry=zis.getNextEntry();
			ZipEntry lastEntry=entry;
			while (entry != null) {
				a.print( entry.getName());
				destFile=new File(outDir,entry.getName());
				if(destFile.exists())
				{
					/*
					Field namesField = ZipOutputStream.getDeclaredField("names");
					namesField.setAccessible(true);
					HashSet<String> names = (HashSet<String>) namesField.get(out);
					*/
					a.print(a.getString(R.string.pass,destFile.getPath()));
					if(archive) {
						zos.putNextEntry(entry);
						if(destFile.isFile()) {
							FileInputStream fisf = new FileInputStream(destFile);
							byte[] copybuf = new byte[4096];
							int readf = 0;
							while ((readf = fisf.read(copybuf, 0, 4096)) > 0)
								zos.write(copybuf, 0, readf);
							fisf.close();
						}
						zos.closeEntry();
					}
					entry = zis.getNextEntry();
					continue;

				}
				try
				{
					if (entry.isDirectory())
					{
						destFile.mkdirs();
						continue;
					}
					else
					{
						int count;
						byte data[] = new byte[BUFFER_SIZE];
						destFile.getParentFile().mkdirs();
						FileOutputStream fos = new FileOutputStream(destFile);
						if(archive)
							zos.putNextEntry(entry);
						dest = new BufferedOutputStream(fos, BUFFER_SIZE);
						while ((count = zis.read(data, 0, BUFFER_SIZE)) != -1)
						{
							dest.write(data, 0, count);
							if(archive)
								zos.write(data,0,count);
						}
						dest.flush();
						dest.close();
						if(archive)
						{
							zos.flush();;
							zos.closeEntry();;
						}
						fos.close();
					}
					entry = zis.getNextEntry();
					if(entry!=null)
					{
						if(entry.equals(lastEntry))
						{
							zis.read();
							entry=zis.getNextEntry();
						}
					}
					lastEntry=entry;
				}
				catch (Exception e)
				{
					a.print(Log.getStackTraceString(e));
					Log.e("FixZip","entry="+entry.getName(),e);
					if(logfos!=null)
					{
						logfos.write(("entry="+entry.getName()+ Log.getStackTraceString(e)+System.lineSeparator()).getBytes());
					}
				}
			}
			zis.close();
			fis.close();
			if(archive) {
				zos.flush();
				zos.close();
			}
		}
		catch (IOException e)
		{
			a.print(Log.getStackTraceString(e));
		}
		try
		{
			logfos.close();
		}
		catch (IOException e)
		{}
		/*doesn't eork on corrupt files
		try
		{
			ZipFile zfile=new ZipFile(file);
			File outFolder=new File(file.getParentFile(),file.getName()+"out/");
			outFolder.mkdirs();
			Enumeration zipFileEntries = zfile.entries();
			// Process each entry
			while (zipFileEntries.hasMoreElements())
			{
				ZipEntry entry= (ZipEntry) zipFileEntries.nextElement();
				a.print(entry.getName());
			}
		}
		catch (IOException e)
		{
			a.print(Log.getStackTraceString(e));
		}*/
	}
	/*
	public void unzipFileIntoDirectory(File archive, File destinationDir) 
    throws Exception {
		final int BUFFER_SIZE = 1024;
		BufferedOutputStream dest = null;
		FileInputStream fis = new FileInputStream(archive);
		ZipInputStream zis = new ZipInputStream(new BufferedInputStream(fis));
		ZipEntry entry;
		File destFile;
		while ((entry = zis.getNextEntry()) != null) {
			destFile = FilesystemUtils.combineFileNames(destinationDir, entry.getName());
			if (entry.isDirectory()) {
				destFile.mkdirs();
				continue;
			} else {
				int count;
				byte data[] = new byte[BUFFER_SIZE];
				destFile.getParentFile().mkdirs();
				FileOutputStream fos = new FileOutputStream(destFile);
				dest = new BufferedOutputStream(fos, BUFFER_SIZE);
				while ((count = zis.read(data, 0, BUFFER_SIZE)) != -1) {
					dest.write(data, 0, count);
				}
				dest.flush();
				dest.close();
				fos.close();
			}
		}
		zis.close();
		fis.close();
	}
	*/
	/*
	private void extractFolder(String zipFile, String extractFolder) 
	{
		try
		{
			int BUFFER = 2048;
			File file = new File(zipFile);

			ZipFile zip = new ZipFile(file);
			String newPath = extractFolder;

			new File(newPath).mkdir();
			Enumeration zipFileEntries = zip.entries();

			// Process each entry
			while (zipFileEntries.hasMoreElements())
			{
				// grab a zip file entry
				ZipEntry entry = (ZipEntry) zipFileEntries.nextElement();
				String currentEntry = entry.getName();

				File destFile = new File(newPath, currentEntry);
				//destFile = new File(newPath, destFile.getName());
				File destinationParent = destFile.getParentFile();

				// create the parent directory structure if needed
				destinationParent.mkdirs();

				if (!entry.isDirectory())
				{
					BufferedInputStream is = new BufferedInputStream(zip
																	 .getInputStream(entry));
					int currentByte;
					// establish buffer for writing file
					byte data[] = new byte[BUFFER];

					// write the current file to disk
					FileOutputStream fos = new FileOutputStream(destFile);
					BufferedOutputStream dest = new BufferedOutputStream(fos,
																		 BUFFER);

					// read and write until last byte is encountered
					while ((currentByte = is.read(data, 0, BUFFER)) != -1)
					{
						dest.write(data, 0, currentByte);
					}
					dest.flush();
					dest.close();
					is.close();
				}


			}
		}
		catch (Exception e) 
		{
			Log("ERROR: " + e.getMessage());
		}
		*/
		/*
	private void addFolderToZip(File folder, ZipOutputStream zip, String baseName) throws IOException
	{
		File[] files = folder.listFiles();
		for (File file : files)
		{
			if (file.isDirectory())
			{
				addFolderToZip(file, zip, baseName);
			}
			else
			{
				String name = file.getAbsolutePath().substring(baseName.length());
				ZipEntry zipEntry = new ZipEntry(name);
				zip.putNextEntry(zipEntry);
				IOUtils.copy(new FileInputStream(file), zip);
				zip.closeEntry();
			}
		}
	}*/
}
