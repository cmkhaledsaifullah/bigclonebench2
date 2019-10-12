package util;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Enumeration;
import java.util.Objects;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

/**
 * @author Jeff Svajlenko
 */
public class UnzipUtil {
	public static void unzipIntoFolder(Path zipfile, Path destination) throws FileNotFoundException, IOException {
		unzipIntoFolder(zipfile, destination, 2048);
	}
	
	/**
	 * Unzips specified zip file into the specified directory.
	 * If destination exists, the two are merged with the ziped contents taking precedence.
	 * @param zipfile
	 * @param destination
	 * @throws FileNotFoundException
	 * @throws IOException
	 */
	public static void unzipIntoFolder(Path zipfile, Path destination, int bufferSize) throws FileNotFoundException, IOException {
		Objects.requireNonNull(zipfile);
		Objects.requireNonNull(destination);
		if(!Files.exists(zipfile)) { throw new FileNotFoundException("zipfile does not exist.");}
		
		
		if(destination.toFile().isFile()) {Files.delete(destination);}
		
		Files.createDirectories(destination);
		
		
		ZipFile zipFile = new ZipFile(zipfile.toFile(), ZipFile.OPEN_READ);
		
		Enumeration<? extends ZipEntry> zipFileEntries = zipFile.entries();
		while(zipFileEntries.hasMoreElements()) {
			ZipEntry entry = (ZipEntry) zipFileEntries.nextElement();
			
			//Destination Path
			Path dest = destination.resolve(entry.getName());
			
			
			
			if(entry.isDirectory()) {
				//Create Directory
				Files.createDirectories(dest);
			} else {
				//Create Parent Directories
				Files.createDirectories(dest.getParent());
				
				byte buffer[] = new byte[bufferSize];
				int cursor;
				
				BufferedInputStream bis = new BufferedInputStream(zipFile.getInputStream(entry));
				BufferedOutputStream bos = new BufferedOutputStream(new FileOutputStream(dest.toFile()));
				
				cursor = bis.read(buffer, 0, bufferSize);
				while(cursor != -1) {
					bos.write(buffer, 0, cursor);
					cursor = bis.read(buffer, 0, bufferSize);
				}
				
				bos.flush();
				bos.close();
				bis.close();
			}
		}
		zipFile.close();
	}
}
