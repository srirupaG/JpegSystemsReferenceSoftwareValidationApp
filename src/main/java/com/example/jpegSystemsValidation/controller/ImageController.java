package com.example.jpegSystemsValidation.controller;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.Principal;
import java.security.spec.InvalidKeySpecException;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import javax.crypto.Cipher;
import javax.crypto.CipherInputStream;
import javax.crypto.NoSuchPaddingException;

import org.mipams.jumbf.entities.BinaryDataBox;
import org.mipams.jumbf.entities.JumbfBox;
import org.mipams.jumbf.entities.JumbfBoxBuilder;
import org.mipams.jumbf.services.CoreGeneratorService;
import org.mipams.jumbf.services.CoreParserService;
import org.mipams.jumbf.util.MipamsException;
import org.mipams.privsec.entities.ProtectionDescriptionBox;
import org.mipams.privsec.services.content_types.ProtectionContentType;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;

import com.example.jpegSystemsValidation.util.SecretKeyUtil;

@Controller
public class ImageController {

	@Autowired
	protected CoreGeneratorService coreGeneratorService;

	@Autowired
	protected CoreParserService coreParserService;


	public static final String ENCRYPT_OBJ = "_encrypt.obj";

	public static final String ENCRYPT_JUMBF = "_encrypt.jumbf";

	public static final String DECRYPT_JPEG = "_decrypt.jpeg";
	
	public static final String SUFFIX_JPEG = ".jpeg";
	
	/** Display the list of images which are fetched from a local directory */
	@Value("${file.directory.path}")
	private String filesDirectory;
	
	
	@Value("${upload.directory}")
	private String uploadDirectory;
	

	/** 
	 * Display the list of files from the "filesDirectory" 
	 * */
	@GetMapping("/images")
	public String displayListOfFiles(Model model, Principal principal) {

		File folder = new File(filesDirectory);
		File[] listOfFiles = folder.listFiles();
		if (listOfFiles != null) {
	        // Filter files with ".jumbf" and ".jpeg" extension but not ROI images
			List<String> fileNames = Arrays.stream(listOfFiles)
                    .filter(file -> {
                        String fileName = file.getName().toLowerCase();
                        boolean hasValidExtension = fileName.endsWith(".jumbf");
                        boolean containsROI = fileName.contains("roi");
                        return hasValidExtension && !containsROI;
                    })
                    .map(File::getName)
                    .collect(Collectors.toList());
	        

	        model.addAttribute("fileNames", fileNames);
		}
		
		return "dispayImageList";
	}
	

	/**
	 * After clicking on the hyperlink of the image, one can view the image
	 */
	@GetMapping("/files/{fileName}")
	public String viewImageIndividual(@PathVariable String fileName, Model model, Principal principal)
			throws NoSuchAlgorithmException, NoSuchPaddingException, MipamsException, InvalidKeyException, IOException,
			InvalidKeySpecException {

		Path filePath = Paths.get(filesDirectory, fileName);
		try {

			Resource resource = new UrlResource(filePath.toUri());

			if (resource.exists() && resource.isReadable()) {
				model.addAttribute("fileName", fileName);
				return "viewImage";
			}
		} catch (IOException e) {
			e.printStackTrace();
		}

		model.addAttribute("error", "Image not found or couldn't be read.");
		return "error";
	}

	/**
	 * This method is to check if the File is of Image type otherwise the file is
	 * considered as a .txt file it is handled in thyemeleaf as
	 * "@{/files/image/{fileName}(fileName=${fileName})}"
	 */
	@GetMapping("/files/image/{fileName}")
	public ResponseEntity<Resource> serveImage(@PathVariable String fileName) {

		Path filePath = Paths.get(filesDirectory, fileName);
		try {
			Resource resource = new UrlResource(filePath.toUri());

			if (resource.exists() && resource.isReadable()) {
				return ResponseEntity.ok().header(HttpHeaders.CONTENT_TYPE, MediaType.IMAGE_JPEG_VALUE).body(resource);
			}
		} catch (IOException e) {
			e.printStackTrace();
		}

		return ResponseEntity.notFound().build();
	}

	/************* Upload Image ******************/

	/** 
	 * Go to Upload Images Without Access Rules
	 */
	@GetMapping("/upload")
	public String displayUploadForm() {

		return "uploadImage";
	}


	/** 
	 * Upload Images Without Access Rules
	 */
	@RequestMapping("/uploadImages")
	public String upload(Model model, @RequestParam("files") MultipartFile[] files, Principal principal)
			throws InvalidKeySpecException, NoSuchAlgorithmException, FileNotFoundException, NoSuchPaddingException,
			InvalidKeyException, IOException, MipamsException {

		StringBuilder fileNames = new StringBuilder();
		for (MultipartFile file : files) {
			Path fileNameAndPath = Paths.get(uploadDirectory, file.getOriginalFilename());
			fileNames.append(file.getOriginalFilename() + " ");
			
//			System.out.println("fileNames:::::"+fileNames.toString());
			
			int lastDotIndex = fileNames.lastIndexOf(".");
//			String newFileName = fileNames.substring(0, lastDotIndex);
//			System.out.println("newFileName:::::"+newFileName);
			
			try {

				// Check
//				Cipher cipher = Cipher.getInstance("AES/CBC/PKCS5Padding");
				Cipher cipher = Cipher.getInstance("AES");
				cipher.init(Cipher.ENCRYPT_MODE, SecretKeyUtil.getKeyFromPassword("abc", "xyz")); // get userId
				CipherInputStream cipt = new CipherInputStream(new FileInputStream(new File(uploadDirectory, file.getOriginalFilename())), cipher);
				FileOutputStream fileip = new FileOutputStream(uploadDirectory + "/" + file.getOriginalFilename().substring(0, lastDotIndex) + ENCRYPT_OBJ);
				int i;
				while ((i = cipt.read()) != -1) {
					fileip.write(i);
				}

				ProtectionDescriptionBox protectionDescriptionBox = new ProtectionDescriptionBox();
				protectionDescriptionBox.setAes256CbcProtection();

				BinaryDataBox binaryDataBox = new BinaryDataBox();
				binaryDataBox.setFileUrl(uploadDirectory + "/" + file.getOriginalFilename().substring(0, lastDotIndex) + ENCRYPT_OBJ);

				ProtectionContentType protectionContentType = new ProtectionContentType();
				JumbfBoxBuilder jumbfBoxBuilder = new JumbfBoxBuilder(protectionContentType);

				jumbfBoxBuilder.appendContentBox(protectionDescriptionBox);
				jumbfBoxBuilder.appendContentBox(binaryDataBox);

				JumbfBox jBox = jumbfBoxBuilder.getResult();

				coreGeneratorService.generateJumbfMetadataToFile(List.of(jBox),
						uploadDirectory + "/" + file.getOriginalFilename().substring(0, lastDotIndex) + ENCRYPT_JUMBF);

				Files.write(fileNameAndPath, file.getBytes());

			} catch (IOException e) {
				e.printStackTrace();
			}

		}

		model.addAttribute("msg", "Successfully uploaded Image: " + fileNames.toString());
		return "uploadImageStatus";
	}

	
	/** 
	 * Click on the Decrypt Images hyperlink to decrypt the image
	 */
	@GetMapping("/decryptImage")
	public String decyptImage(Model model, @RequestParam String fileName, Principal principal)
			throws MipamsException, NoSuchAlgorithmException, NoSuchPaddingException, InvalidKeyException,
			InvalidKeySpecException, IOException {

		int lastDotIndex = fileName.lastIndexOf(".");
		String baseFileName = fileName.substring(0, lastDotIndex);
		
		int lastUnderscoreIndex = fileName.lastIndexOf("_");
		String originalFileName = fileName.substring(0, lastUnderscoreIndex);
//		
		System.out.println("baseFileName:::"+baseFileName);
		
		System.out.println("originalFileName:::"+originalFileName);

		List<JumbfBox> bBoxes = coreParserService.parseMetadataFromFile(uploadDirectory + "/" + fileName);
		JumbfBox jBoxess = bBoxes.get(0);
		BinaryDataBox bBox = (BinaryDataBox) jBoxess.getContentBoxList().get(1);
		bBox.getFileUrl();

		Cipher cipher = Cipher.getInstance("AES");
		cipher.init(Cipher.DECRYPT_MODE, SecretKeyUtil.getKeyFromPassword("abc", "xyz"));
		CipherInputStream ciptt = new CipherInputStream(new FileInputStream(new File(bBox.getFileUrl())), cipher);

		FileOutputStream fileipo = new FileOutputStream(uploadDirectory + "/" + fileName + DECRYPT_JPEG);
		
		int j;
		while ((j = ciptt.read()) != -1) {
			fileipo.write(j);
		}

		model.addAttribute("msg", "Successfully decrypted .jumbf file " + fileName);
		model.addAttribute("imageName", originalFileName + SUFFIX_JPEG);
		model.addAttribute("fileName", fileName + DECRYPT_JPEG);
		
		return "viewImage";
	}


}
