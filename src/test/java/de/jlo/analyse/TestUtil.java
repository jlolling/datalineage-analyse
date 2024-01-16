package de.jlo.analyse;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.nio.file.Path;
import java.nio.file.Paths;

import de.jlo.analyse.tableau.Utils;

public class TestUtil {

	public static void saveResourceAsFile(String resourceName, String targetFilePath) throws Exception {
		InputStream is = Utils.class.getResourceAsStream(resourceName);
		try (BufferedOutputStream os = new BufferedOutputStream(new FileOutputStream(new File(targetFilePath)))) {
			byte[] buffer = new byte[1024];
			int len = 0;
			while ((len = is.read(buffer)) != -1) {
				os.write(buffer, 0, len);
				os.flush();
			}
		} catch (Exception e) {
			throw new Exception("Save resource: " + resourceName + " as file: " + targetFilePath + " failed: " + e.getMessage(), e);
		} finally {
			if (is != null) {
				is.close();
			}
		}
	}
	
	public static String getWorkDir() {
		Path currentRelativePath = Paths.get("");
		return currentRelativePath.toAbsolutePath().toString();
	}

}
