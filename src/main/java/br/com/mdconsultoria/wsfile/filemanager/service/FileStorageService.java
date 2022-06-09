package br.com.mdconsultoria.wsfile.filemanager.service;

import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import br.com.mdconsultoria.wsfile.filemanager.Utils.UtilsFile;
import br.com.mdconsultoria.wsfile.filemanager.exception.FileStorageException;
import br.com.mdconsultoria.wsfile.filemanager.exception.MyFileNotFoundException;
import br.com.mdconsultoria.wsfile.filemanager.exception.ResourceNotFoundException;
import br.com.mdconsultoria.wsfile.filemanager.models.FileStorage;


@Service
public class FileStorageService {
	private final Path fileStorageLocation;

	@Autowired
	public FileStorageService() {
		//this.fileStorageLocation = Paths.get("/uploads").toAbsolutePath().normalize();
		this.fileStorageLocation = Paths.get(new File("src/main/resources/files").toURI()).toAbsolutePath().normalize();
		try {
			Files.createDirectories(this.fileStorageLocation);
		} catch (Exception ex) {
			throw new FileStorageException("Could not create the directory where the uploaded files will be stored.", ex);
		}
	}

	public String storeFile(MultipartFile file) {
		// Normalize file name
		String fileName = StringUtils.cleanPath(file.getOriginalFilename());

		try {
			// Check if the file's name contains invalid characters
			if (fileName.contains("..")) 
				throw new FileStorageException("Sorry! Filename contains invalid path sequence " + fileName);
			
			// Copy file to the target location (Replacing existing file with the same name)
			Path targetLocation = fileStorageLocation.resolve(fileName);
			Files.copy(file.getInputStream(), targetLocation, StandardCopyOption.REPLACE_EXISTING);

			return fileName;
		} catch (IOException ex) {
			throw new FileStorageException("Could not store file " + fileName + ". Please try again!", ex);
		}
	}

	public Resource loadFileAsResource(String fileName) {
		try {
			Path filePath = this.fileStorageLocation.resolve(fileName).normalize();
			Resource resource = new UrlResource(filePath.toUri());
			if (resource.exists()) {
				return resource;
			} else {
				throw new MyFileNotFoundException("File not found " + fileName);
			}
		} catch (MalformedURLException ex) {
			throw new MyFileNotFoundException("File not found " + fileName, ex);
		}
	}
	
	public static String MD5(byte[] byteFile) {
		try {
		    return UtilsFile.MD5(byteFile);
	    } catch (Exception e) {
	    	throw new FileStorageException("Generate MD5 error", e);
		}
	}
	
	public static String MD5(MultipartFile file) {
		try {
			return UtilsFile.MD5(file.getBytes());
	    } catch (Exception e) {
	    	throw new FileStorageException("Generate MD5 error", e);
		}
	}
	
	public FileStorage findByName(String fileName) {
		try {
			File[] files = new File("src/main/resources/files").listFiles();
			
			for (File file : files) {
				if (file.getName().equals(fileName)) {
					return new FileStorage(file.getName(), 
							ServletUriComponentsBuilder.fromCurrentContextPath().path("/downloadFile/").path(file.getName()).toUriString(), 
							FilenameUtils.getExtension(file.getName()), 
							file.length(), 
							MD5(FileUtils.readFileToByteArray(file)));
				}
			}
			
			throw new Exception("");
		} catch (Exception e) {
			throw new ResourceNotFoundException("Resources not found");
		}
	}
	
	public List<FileStorage> findAll() {
		try {
			File[] files = new File("src/main/resources/files").listFiles();
			
			List<FileStorage> fileStorageLista = new ArrayList<>();
			for (File file : files)
				fileStorageLista.add(new FileStorage(file.getName(), 
						ServletUriComponentsBuilder.fromCurrentContextPath().path("/downloadFile/").path(file.getName()).toUriString(), 
						FilenameUtils.getExtension(file.getName()), 
						file.length(), 
						MD5(FileUtils.readFileToByteArray(file))));
			
			return fileStorageLista;
		} catch (Exception e) {
			throw new ResourceNotFoundException("Resources not found");
		}
	}
	
	public boolean delete(String fileName) {
		try {
			File[] files = new File("src/main/resources/files").listFiles();
			
			for (File file : files) {
				if (file.getName().equals(fileName)) {
					file.delete();
					return true;
				}
			}
			throw new Exception("File not find");
		} catch (Exception e) {
			throw new ResourceNotFoundException("Error on delete file - " + e.getMessage());
		}
	}
}
