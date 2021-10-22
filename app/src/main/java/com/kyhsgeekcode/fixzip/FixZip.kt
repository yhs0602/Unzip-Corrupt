package com.kyhsgeekcode.fixzip

import android.util.Log
import timber.log.Timber
import java.io.*
import java.util.zip.ZipEntry
import java.util.zip.ZipInputStream

object FixZip {
    suspend fun Run(a: MainActivity) {
        throw RuntimeException("Test Crash") // Force a crash
        var path = ""
        var file = File("")
        val logfile = File("/sdcard/fixzip.log")
        var yn = ""
        var archive = false
        do {
            Timber.d("Asking")
            a.print("Archive again(y) or extract only(n)?")
            try {
                yn = a.readLine()
                archive =
                    if ("y".equals(yn, ignoreCase = true))
                        true
                    else if ("n".equals(yn, ignoreCase = true))
                        false
                    else continue
                break
            } catch (e: InterruptedException) {
            }
        } while (true)
        do {
            a.print("enter the path")
            path = a.readLine()
            file = File(path)
        } while (!file.exists())
        val BUFFER_SIZE = 1024
        var dest: BufferedOutputStream? = null
//        var logfos: FileOutputStream? = null
//        try {
//            logfos = FileOutputStream(logfile)
//        } catch (e: FileNotFoundException) {
//            Log.e("FixZip", "log error", e)
//            a.print("log failed filenotfound")
//        }
        try {
            val fis = FileInputStream(file)
            val zis = ZipInputStream(BufferedInputStream(fis))
            var entry: ZipEntry?
            val outDir = File(file.parentFile, file.name + "out/")
            var destFile: File
            entry = zis.nextEntry
            var lastEntry = entry
            while (entry != null) {
                a.print(entry.name)
                destFile = File(outDir, entry.name)
                if (destFile.exists()) {
                    /*
					Field namesField = ZipOutputStream.getDeclaredField("names");
					namesField.setAccessible(true);
					HashSet<String> names = (HashSet<String>) namesField.get(out);
					*/
                    entry = zis.nextEntry
                    continue
                }
                try {
                    if (entry.isDirectory) {
                        destFile.mkdirs()
                        continue
                    } else {
                        var count: Int
                        val data = ByteArray(BUFFER_SIZE)
                        destFile.parentFile.mkdirs()
                        val fos = FileOutputStream(destFile)
                        dest = BufferedOutputStream(fos, BUFFER_SIZE)
                        while (zis.read(data, 0, BUFFER_SIZE).also { count = it } != -1) {
                            dest.write(data, 0, count)
                        }
                        dest.flush()
                        dest.close()
                        fos.close()
                    }
                    entry = zis.nextEntry
                    if (entry != null) {
                        if (entry == lastEntry) {
                            zis.read()
                            entry = zis.nextEntry
                        }
                    }
                    lastEntry = entry
                } catch (e: Exception) {
                    a.print(Log.getStackTraceString(e))
                    Log.e("FixZip", "entry=" + entry.name, e)
//                    logfos?.write(("entry=" + entry.name + Log.getStackTraceString(e) + System.lineSeparator()).toByteArray())
                }
            }
            zis.close()
            fis.close()
        } catch (e: IOException) {
            a.print(Log.getStackTraceString(e))
        }
//        try {
//            logfos!!.close()
//        } catch (e: IOException) {
//        }
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
    } /*
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