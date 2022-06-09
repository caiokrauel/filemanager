package br.com.mdconsultoria.wsfile.filemanager.controller;

import java.io.IOException;
import java.security.NoSuchAlgorithmException;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import javax.servlet.http.HttpServletRequest;

import org.apache.commons.io.FileUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import br.com.mdconsultoria.wsfile.filemanager.models.FileStorage;
import br.com.mdconsultoria.wsfile.filemanager.service.FileStorageService;

@RestController
@RequestMapping(path = "")
public class FileController {
	private static final Logger LOGGER = LogManager.getLogger(FileController.class.getSimpleName());
	
	@Autowired
    private FileStorageService fileStorageService;
	
	@PostMapping("/uploadFile")
    public FileStorage uploadFile(@RequestParam("file") MultipartFile file) {
        String fileName = fileStorageService.storeFile(file);

        String fileDownloadUri = ServletUriComponentsBuilder.fromCurrentContextPath()
                .path("/downloadFile/")
                .path(fileName)
                .toUriString();
                
        return new FileStorage(fileName, fileDownloadUri, file.getContentType(), file.getSize(), FileStorageService.MD5(file));
    }
	
	@PostMapping("/uploadMultipleFiles")
    public List<FileStorage> uploadMultipleFiles(@RequestParam("files") MultipartFile[] files) {
        return Arrays.asList(files)
                .stream()
                .map(file -> uploadFile(file))
                .collect(Collectors.toList());
    }
	
	@GetMapping("/downloadFile/{fileName:.+}")
    public ResponseEntity<Resource> downloadFile(@PathVariable String fileName, HttpServletRequest request) throws NoSuchAlgorithmException, IOException {
        // Load file as Resource
        Resource resource = fileStorageService.loadFileAsResource(fileName);

        // Try to determine file's content type
        String contentType = null;
        try {
            contentType = request.getServletContext().getMimeType(resource.getFile().getAbsolutePath());
        } catch (IOException ex) {	
        	LOGGER.info("Could not determine file type.");
        }

        // Fallback to the default content type if type could not be determined
        if (contentType == null) 
            contentType = "application/octet-stream";

        return ResponseEntity.ok()
                .contentType(MediaType.parseMediaType(contentType))
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + resource.getFilename() + "\"")
                .header("MD5", FileStorageService.MD5(FileUtils.readFileToByteArray(resource.getFile())))
                .body(resource);
    }
	
	@GetMapping("/findbyname")
    public FileStorage findByName(@RequestParam("filename") String fileName) {                
        return fileStorageService.findByName(fileName);
    }
	
	@GetMapping("/findall")
    public List<FileStorage> findall() {                
        return fileStorageService.findAll();
    }
	
	@PostMapping("/delete")
    public ResponseEntity<String> delete(@RequestParam("filename") String fileName) {
        fileStorageService.delete(fileName);
        return ResponseEntity.ok().body("");
    }
}
