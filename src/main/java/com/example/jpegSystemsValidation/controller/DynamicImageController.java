package com.example.jpegSystemsValidation.controller;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.StringReader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.Principal;
import java.security.spec.InvalidKeySpecException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import javax.crypto.Cipher;
import javax.crypto.CipherInputStream;
import javax.crypto.NoSuchPaddingException;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.mipams.jumbf.entities.BinaryDataBox;
import org.mipams.jumbf.entities.JumbfBox;
import org.mipams.jumbf.entities.JumbfBoxBuilder;
import org.mipams.jumbf.entities.XmlBox;
import org.mipams.jumbf.services.CoreGeneratorService;
import org.mipams.jumbf.services.CoreParserService;
import org.mipams.jumbf.services.content_types.XmlContentType;
import org.mipams.jumbf.util.MipamsException;
import org.mipams.privsec.entities.ProtectionDescriptionBox;
import org.mipams.privsec.services.content_types.ProtectionContentType;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

import com.example.jpegSystemsValidation.model.Image;
import com.example.jpegSystemsValidation.model.Role;
import com.example.jpegSystemsValidation.model.User;
import com.example.jpegSystemsValidation.repo.RoleRepository;
import com.example.jpegSystemsValidation.repo.UserRepository;
import com.example.jpegSystemsValidation.service.ImageGroupViewService;
import com.example.jpegSystemsValidation.service.ImageService;
import com.example.jpegSystemsValidation.service.ImageViewService;
import com.example.jpegSystemsValidation.util.SecretKeyUtil;
import com.example.jpegSystemsValidation.util.XacmlParsing;
import com.example.jpegSystemsValidation.util.XacmlTemplate;

@Controller
public class DynamicImageController {
	
	@Autowired
	protected CoreGeneratorService coreGeneratorService;

	@Autowired
	protected CoreParserService coreParserService;
	
	@Autowired
	private ImageService imageService;
	
	@Autowired
	private ImageViewService imageViewService;
	
	@Autowired
	private ImageGroupViewService imageGroupViewService;
	
	
	public static final String ENCRYPT_OBJ = "_encrypt.obj";
	
	public static final String ENCRYPT_JUMBF = "_encrypt.jumbf";
	
	public static final String DECRYPT_JPEG = "_decrypt.jpeg";
	
	public final UserRepository userRepository;
	
	public final RoleRepository roleRepository;

    public DynamicImageController(UserRepository userRepository, RoleRepository roleRepository) {
        this.userRepository = userRepository;
        this.roleRepository = roleRepository;
    }
    
    
	/** 
	 * Display the list of images which are fetched from a local directory
	 */
	@Value("${file.directory.path}")
	private String filesDirectory;
	
	@Value("${upload.directory}")
	private String uploadDirectory;

	
	/************* Image upload based on user group ******************/
	
	/** 
	 * Go to upload images page for groups 
	*/
	@GetMapping("/uploadImageBasedOnGroups")
    public String uploadImageBasedOnGroups(Model model, Principal principal ) {
		
		List<Role> roles = roleRepository.findAll();
        model.addAttribute("roles", roles);
        return "uploadImageBasedOnGroups";
    }
	
	
	/** 
	 * Upload Images Based on User Groups 
	 */
	@PostMapping("uploadImagesWithAcessRulesOnGroups")
    public String uploadImagesWithAcessRulesOnGroups(Model model,
            @RequestParam(value = "viewGroups", required = false) List<String> viewGroupNames,
            @RequestParam(value = "editGroups", required = false) List<String> editGroupNames,
            @RequestParam("files") MultipartFile[] files,
            Principal principal) 
            throws InvalidKeySpecException, NoSuchAlgorithmException, FileNotFoundException, NoSuchPaddingException,InvalidKeyException, IOException, MipamsException {
		
        StringBuilder fileNames = new StringBuilder();
        for (MultipartFile file : files) {
			Path fileNameAndPath = Paths.get(uploadDirectory, file.getOriginalFilename());
			fileNames.append(file.getOriginalFilename() + " ");

			try {
				Cipher cipher = Cipher.getInstance("AES");
				cipher.init(Cipher.ENCRYPT_MODE, SecretKeyUtil.getKeyFromPassword("abc", "xyz")); // get userId
				
				File fileToRead = new File(uploadDirectory, file.getOriginalFilename());
				
				if (!fileToRead.exists() || fileToRead.isDirectory()) {
				    System.err.println("File does not exist or is a directory: " + fileToRead.getAbsolutePath());
				} else {
				CipherInputStream cipt = new CipherInputStream(new FileInputStream(fileToRead), cipher);
				
				FileOutputStream fileip = new FileOutputStream(uploadDirectory + "/" + file.getOriginalFilename() + ENCRYPT_OBJ);
				int i;
				while ((i = cipt.read()) != -1) {
					fileip.write(i);
				}
				}
				
				  List<String> groupsToView = new ArrayList<>();

				  // check if the user has given any access view
				     if (viewGroupNames != null && !viewGroupNames.isEmpty()) {
				         for (String groupName : viewGroupNames) {
				             if (!groupsToView.contains(groupName)) { 
				            	 groupsToView.add(groupName);
				             }
				         }
				     }
				     
				String xacmlContentString = XacmlTemplate.generatePolicyXmlForImageAccessBasedOnGroup(file.getOriginalFilename(), groupsToView, editGroupNames);
				
		        ProtectionDescriptionBox protectionDescriptionBox = new ProtectionDescriptionBox();
				protectionDescriptionBox.setAes256CbcProtection();

				protectionDescriptionBox.setArLabel("access-rules-reference-group");
				protectionDescriptionBox.includeAccessRulesInToggle();

				BinaryDataBox binaryDataBox = new BinaryDataBox();
				binaryDataBox
						.setFileUrl(uploadDirectory + "/" + file.getOriginalFilename() + ENCRYPT_OBJ);

				ProtectionContentType protectionContentType = new ProtectionContentType();
				JumbfBoxBuilder jumbfBoxBuilder = new JumbfBoxBuilder(protectionContentType);

				jumbfBoxBuilder.appendContentBox(protectionDescriptionBox);
				jumbfBoxBuilder.appendContentBox(binaryDataBox);

				JumbfBox jBox = jumbfBoxBuilder.getResult();
				
				// XmlContentType have inbuild DescriptionBox
				XmlContentType xmlContentType = new XmlContentType();

				jumbfBoxBuilder = new JumbfBoxBuilder(xmlContentType);

				// both the Labels should be same
				jumbfBoxBuilder.setLabel("access-rules-reference-group");

				XmlBox xmlBox = new XmlBox();
				System.out.println(xacmlContentString);
				// setting StringBuilder content to String
				xmlBox.setContent(xacmlContentString.toString().getBytes());

				jumbfBoxBuilder.appendContentBox(xmlBox);
				JumbfBox jBoxForXmlBox = jumbfBoxBuilder.getResult();
				coreGeneratorService.generateJumbfMetadataToFile(List.of(jBox, jBoxForXmlBox),
						uploadDirectory + "/" + file.getOriginalFilename() + ENCRYPT_JUMBF);

				Files.write(fileNameAndPath, file.getBytes());
				
				//Save to DB
				User createdBy = userRepository.findByUsername(principal.getName());
				// save in images table
				Image image = imageService.saveImage(uploadDirectory, file.getOriginalFilename(), createdBy);
				
				// save in image service table
				imageViewService.save(image, createdBy);
				
		        if (groupsToView != null && !groupsToView.isEmpty()) {
		        	
		        	for(String role : groupsToView) {
		        		
				        Role roleGiven = roleRepository.findByName(role);
				        // save in image group table
				        imageGroupViewService.save(image, roleGiven);
		        		
		        	}
		        	
		        }
				
			} catch (IOException e) {
				e.printStackTrace();
			}
		
        }
		model.addAttribute("msg", "Successfully uploaded files " + fileNames.toString());
        return "uploadImageStatus";
        
    }
	
	/** 
	 * View Images Uploaded By Users
	 */
	@GetMapping("/viewImageUploadedByUser")
	public String viewImageUploadedByUser(Model model, Principal principal) {
		
		String loggedInUser = principal.getName();
		User users = userRepository.findByUsername(loggedInUser);
		System.out.println("users::::::::"+ users.getId());
		
		List<Image> listOfAllImages = imageService.findImagesByUserId(users.getId());
		
		 List<Image> images = listOfAllImages.stream()
	                .filter(image -> !image.getImageName().contains("ROI"))
	                .collect(Collectors.toList());
		
		model.addAttribute("images", images);
		
	    return "viewImagesUploadedByUser";
	    
	}
	
	
	/** 
	 * User can view his own uploaded images. 
	 * Here the XACML rules will not be checked 
	*/
	@GetMapping("/viewIndividualImageUploadedByUser")
	public String viewIndividualImageUploadedByUser(Model model, 
			@RequestParam("imageId") Long imageId,
			Principal principal)
			throws MipamsException, NoSuchAlgorithmException, NoSuchPaddingException, InvalidKeyException,
			InvalidKeySpecException, IOException, SAXException, ParserConfigurationException {

		// Search the image from DB
		Optional<Image> imageOptional = imageService.findByImageId(imageId);
		
		System.out.println("image:::::::::"+imageOptional);
		
			Image images = imageOptional.get();
			String filePath = images.getFilePath();
			System.out.println("images.getImagename():::"+images.getImageName());

			if(filePath.equalsIgnoreCase(uploadDirectory)) {
				
				String jumbfFileName = images.getImageName() + ENCRYPT_JUMBF;
				System.out.println("jumbfFileName:::::"+jumbfFileName);
				
				int lastDotIndex = jumbfFileName.lastIndexOf(".");
				String baseFileName = jumbfFileName.substring(0, lastDotIndex);
				
				System.out.println("baseFileName:::"+baseFileName);

				List<JumbfBox> bBoxes = coreParserService.parseMetadataFromFile(uploadDirectory + "/" + jumbfFileName);
				JumbfBox jBoxess = bBoxes.get(0);
				
				BinaryDataBox bBox = (BinaryDataBox) jBoxess.getContentBoxList().get(1);
				bBox.getFileUrl();
				
		            Cipher cipher = Cipher.getInstance("AES");
					cipher.init(Cipher.DECRYPT_MODE, SecretKeyUtil.getKeyFromPassword("abc", "xyz"));
					CipherInputStream ciptt = new CipherInputStream(new FileInputStream(new File(bBox.getFileUrl())), cipher);

					FileOutputStream fileipo = new FileOutputStream(uploadDirectory + "/" + baseFileName + DECRYPT_JPEG);

					int j;
					while ((j = ciptt.read()) != -1) {

						fileipo.write(j);
					}
					
					model.addAttribute("msg", "Successfully decrypted files " + baseFileName + DECRYPT_JPEG + ", go back to main menu to view this image");
					model.addAttribute("fileName", baseFileName + DECRYPT_JPEG);
				
			}
			return "viewImage";
	}
	
	
	/** 
	 * View / Hide functionality from database 
	*/
	@PostMapping("/hideImageFromOthers")
    public String hideImageFromOthers(@RequestParam("imageIds") List<Long> imageIds, Model model, Principal principal) {
		
        for (Long imageId : imageIds) {
        	Optional<Image> imageOptional = imageService.findByImageId(imageId);
			Image imageToModify = imageOptional.get();
			Long id  = imageToModify.getId();
			String imageName = imageToModify.getImageName();
			String filePath = imageToModify.getFilePath();
			User createdBy = imageToModify.getCreatedBy();
			boolean isHidden = imageToModify.isHidden();
			System.out.println("isHidden::::::"+isHidden);
			
			imageService.saveOrUpdateImage(id, imageName, filePath, createdBy, isHidden);
        }
        
        String loggedInUser = principal.getName();
		User users = userRepository.findByUsername(loggedInUser);
		System.out.println("users::::::::"+ users.getId());
		
		List<Image> images = imageService.findImagesByUserId(users.getId());
		model.addAttribute("images", images);
        
        return "viewImagesUploadedByUser";
    }
	
	
	/** 
	 * Go to View All Images Page. 
	 * This page is populated by DB based on the isHidden field 
	 * */
	@GetMapping("/viewImageForUserGroup")
	public String viewImageForUserGroup(Model model, Principal principal) {

	    // returns the list of imaged who is visible (isHidden=false)
	    List<Image> listOfAllImages = imageService.findAllVisibleImages();
	    
	    List<Image> images = listOfAllImages.stream()
                .filter(image -> !image.getImageName().contains("ROI"))
                .collect(Collectors.toList());

	    model.addAttribute("images", images);
	    
	    return "viewEditImagesBasedOnUserGroup";
	}
	
	
	/** 
	 * Function to view the image if the user has access rights 
	 * */
	@GetMapping("/viewImagesByUserGroup")
	public String viewImagesByUserGroup(Model model, 
			@RequestParam("imageId") Long imageId,
			Principal principal)
			throws MipamsException, NoSuchAlgorithmException, NoSuchPaddingException, InvalidKeyException,
			InvalidKeySpecException, IOException, SAXException, ParserConfigurationException {
		
		// Search the image from DB
		Optional<Image> imageOptional = imageService.findByImageId(imageId);
		
			Image images = imageOptional.get();
			String filePath = images.getFilePath();
			System.out.println("images.getImagename():::"+images.getImageName());
			System.out.println("filePath:::"+filePath);
		
			if(filePath.equalsIgnoreCase(uploadDirectory)) {
				
				String jumbfFileName = images.getImageName() + ENCRYPT_JUMBF;
				System.out.println("jumbfFileName:::::"+jumbfFileName);
				
				int lastDotIndex = jumbfFileName.lastIndexOf(".");
				String baseFileName = jumbfFileName.substring(0, lastDotIndex);
				
				System.out.println("baseFileName:::"+baseFileName);
				
				List<JumbfBox> bBoxes = coreParserService.parseMetadataFromFile(uploadDirectory + "/" + jumbfFileName);
				JumbfBox jBoxess = bBoxes.get(0);
				
				BinaryDataBox bBox = (BinaryDataBox) jBoxess.getContentBoxList().get(1);
				bBox.getFileUrl();
				
				JumbfBox jBoxess1 = bBoxes.get(1);
				XmlBox xmlBox = (XmlBox)jBoxess1.getContentBoxList().get(0);
				System.out.println("xmlBox:::::"+ new String(xmlBox.getContent()));
				
				// XML content as a string
				String xmlContent = new String(xmlBox.getContent());
				
				
				String role = null;
				List<String> groupsToView = new ArrayList<>();
				// Need to check user in groups
				
				 // To get the roles(groups) assigned to the logged in user principal.getName()
			     Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
			     Object principalObject = authentication.getPrincipal();
			     if (principalObject instanceof UserDetails) {
			         UserDetails userDetails = (UserDetails) principalObject;
			         Collection<? extends GrantedAuthority> authorities = userDetails.getAuthorities();
			         for (GrantedAuthority authority : authorities) {
			             role = authority.getAuthority();
			             System.out.println("Role: " + role);
			             if (!groupsToView.contains(role)) { 
			            	 groupsToView.add(role);
			             }
			         }
			     }
			     
					// Parse the XML content
			        DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
			        DocumentBuilder db = dbf.newDocumentBuilder();
			        InputSource is = new InputSource(new StringReader(xmlContent));
			        Document document = db.parse(is);
			        document.getDocumentElement().normalize();

			        // Get the root element
			        Element rootElement = document.getDocumentElement();		     
			     
			          // Check if user can view the image
			         boolean canView = false;
			         for (String group : groupsToView) {
			        	 System.out.println("group::::::"+group);
			             if (XacmlParsing.checkPermissionByRuleToViewOrEdit(rootElement, group, "urn:oasis:names:tc:xacml:3.0:permit-image-view")) {
			                 canView = true;
			                 break; 
			             }
			         }

			         System.out.println("User can view the image: " + canView); 	
					
			        if(canView) {
						
						System.out.println("I am inside the canView check method");
			        	
			            Cipher cipher = Cipher.getInstance("AES");
						cipher.init(Cipher.DECRYPT_MODE, SecretKeyUtil.getKeyFromPassword("abc", "xyz"));
						CipherInputStream ciptt = new CipherInputStream(new FileInputStream(new File(bBox.getFileUrl())), cipher);

						FileOutputStream fileipo = new FileOutputStream(uploadDirectory + "/" + baseFileName + DECRYPT_JPEG);

						int j;
						while ((j = ciptt.read()) != -1) {

							fileipo.write(j);
						}
						
						model.addAttribute("msg", "Successfully decrypted files " + baseFileName + DECRYPT_JPEG + ", go back to main menu to view this image");
						
						model.addAttribute("fileName", baseFileName + DECRYPT_JPEG);
					}
					else {
						model.addAttribute("error", "User not allowed to view image");
						return "error";
					}		     
			     
			}
        
			return "viewImage";
        
	}
	
	
	/** 
	 * Function to edit the image if the user has access rights 
	 * */
	@GetMapping("/editImagesByUserGroup")
	public String editImagesByUserGroup(Model model, 
			@RequestParam("imageName") String imageName,
			Principal principal)
			throws MipamsException, NoSuchAlgorithmException, NoSuchPaddingException, InvalidKeyException,
			InvalidKeySpecException, IOException, SAXException, ParserConfigurationException {
		
		
		String fileName = imageName + ENCRYPT_JUMBF;
		int lastDotIndex = fileName.lastIndexOf(".");

		List<JumbfBox> bBoxes = coreParserService.parseMetadataFromFile(uploadDirectory + "/" + fileName);
		JumbfBox jBoxess = bBoxes.get(0);
		
		BinaryDataBox bBox = (BinaryDataBox) jBoxess.getContentBoxList().get(1);
		bBox.getFileUrl();
		
		JumbfBox jBoxess1 = bBoxes.get(1);
		XmlBox xmlBox = (XmlBox)jBoxess1.getContentBoxList().get(0);
		System.out.println("xmlBox:::::"+ new String(xmlBox.getContent()));
		
		// XML content as a string
		String xmlContent = new String(xmlBox.getContent());
		String role = null;
		List<String> groupsToView = new ArrayList<>();
		// Need to check user in groups
		
		 // To get the roles(groups) assigned to the logged in user principal.getName()
	     Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
	     Object principalObject = authentication.getPrincipal();
	     if (principalObject instanceof UserDetails) {
	         UserDetails userDetails = (UserDetails) principalObject;
	         Collection<? extends GrantedAuthority> authorities = userDetails.getAuthorities();
	         for (GrantedAuthority authority : authorities) {
	             role = authority.getAuthority();
	             System.out.println("Role: " + role);
	             if (!groupsToView.contains(role)) { 
	            	 groupsToView.add(role);
	             }
	         }
	     }	
			// Parse the XML content
	        DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
	        DocumentBuilder db = dbf.newDocumentBuilder();
	        InputSource is = new InputSource(new StringReader(xmlContent));
	        Document document = db.parse(is);
	        document.getDocumentElement().normalize();

	        // Get the root element
	        Element rootElement = document.getDocumentElement();

	          // Check if user can view the image
      boolean canEdit = false;
      for (String group : groupsToView) {
          if (XacmlParsing.checkPermissionByRuleToViewOrEdit(rootElement, group, "urn:oasis:names:tc:xacml:3.0:permit-image-editing")) {
        	  canEdit = true;
              break; 
          }
      }

      System.out.println("User can edit the image: " + canEdit); 
		
		if(canEdit) {
			
			System.out.println("User can edit the Jumbf file");
        	
			model.addAttribute("msg", "User is authorised to edit the Image (Jumbf file)");
			
			return "displayDecryptStatus";
		}
		else {
			model.addAttribute("error", "User not allowed to edit image");
			return "error";
		}
		
	}
	
}

