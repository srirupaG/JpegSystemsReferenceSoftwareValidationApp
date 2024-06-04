package com.example.jpegSystemsValidation.controller;

import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.StringReader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.Principal;
import java.security.spec.InvalidKeySpecException;
import java.util.ArrayList;
import java.util.Base64;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import javax.crypto.Cipher;
import javax.crypto.CipherInputStream;
import javax.crypto.NoSuchPaddingException;
import javax.imageio.ImageIO;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.mipams.jumbf.entities.BinaryDataBox;
import org.mipams.jumbf.entities.ContiguousCodestreamBox;
import org.mipams.jumbf.entities.JumbfBox;
import org.mipams.jumbf.entities.JumbfBoxBuilder;
import org.mipams.jumbf.entities.XmlBox;
import org.mipams.jumbf.services.CoreGeneratorService;
import org.mipams.jumbf.services.CoreParserService;
import org.mipams.jumbf.services.content_types.XmlContentType;
import org.mipams.jumbf.util.MipamsException;
import org.mipams.privsec.entities.ProtectionDescriptionBox;
import org.mipams.privsec.entities.ReplacementDescriptionBox;
import org.mipams.privsec.entities.replacement.ReplacementType;
import org.mipams.privsec.entities.replacement.RoiParamHandler;
import org.mipams.privsec.services.content_types.ProtectionContentType;
import org.mipams.privsec.services.content_types.ReplacementContentType;
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
import org.springframework.web.bind.annotation.RequestMapping;
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
public class ROIController {
	
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

    public ROIController(UserRepository userRepository, RoleRepository roleRepository) {
        this.userRepository = userRepository;
        this.roleRepository = roleRepository;
    }
    
    
	/** Display the list of images which are fetched from a local directory */
	@Value("${file.directory.path}")
	private String filesDirectory;
	
	@Value("${upload.directory}")
	private String uploadDirectory;

	// Resize the emoji/overlay image to fit within the dimensions of the ROI
		private BufferedImage resizeImage(BufferedImage originalImage, int targetWidth, int targetHeight) {
		    BufferedImage resizedImage = new BufferedImage(targetWidth, targetHeight, originalImage.getType());
		    Graphics2D g = resizedImage.createGraphics();
		    g.drawImage(originalImage, 0, 0, targetWidth, targetHeight, null);
		    g.dispose();
		    return resizedImage;
		}

		// Overlay the emoji/overlay image on the cropped region
		private BufferedImage addEmojiOverlay(BufferedImage croppedImage, BufferedImage emojiImage) {
		    Graphics2D g = croppedImage.createGraphics();
		    g.drawImage(emojiImage, 0, 0, null);
		    g.dispose();
		    return croppedImage;
		}


		// Method to save the image to a file and return the file path
		private String saveImage(BufferedImage image, String outputPath) throws IOException {
		    File outputFile = new File(outputPath);
		    ImageIO.write(image, "jpg", outputFile);
		    return outputFile.getAbsolutePath();
		}
		
		
		// Method to merge the overlay image into the original image at the ROI position
		private BufferedImage mergeImages(BufferedImage originalImage, BufferedImage overlayImage, int x, int y) {
		    Graphics2D g = originalImage.createGraphics();
		    g.drawImage(overlayImage, x, y, null);
		    g.dispose();
		    return originalImage;
		}
		
		private BufferedImage cropImage(BufferedImage originalImage, int x, int y, int width, int height) {
		    return originalImage.getSubimage(x, y, width, height);
		}
	
		/** Go to upload images page for ROI */
		@GetMapping("/goToUploadImageRoi")
	    public String goToUploadImageRoi(Model model) {
			
			List<Role> roles = roleRepository.findAll();
	        model.addAttribute("roles", roles);
	        return "uploadImagesForRoi";
	    }
	
	
		@RequestMapping("/uploadImagesForROI")
		public String uploadImagesForROI(Model model, 
			@RequestParam("files") MultipartFile[] files, 
            @RequestParam(value = "viewGroups", required = false) List<String> viewGroupNames,
			Principal principal)
			throws InvalidKeySpecException, NoSuchAlgorithmException, FileNotFoundException, NoSuchPaddingException,
			InvalidKeyException, IOException, MipamsException {
		
			System.out.println("principal.getName()::::"+principal.getName());
			StringBuilder fileNames = new StringBuilder();
		
			for (MultipartFile file : files) {
				Path fileNameAndPath = Paths.get(uploadDirectory, file.getOriginalFilename());
		        fileNames.append(file.getOriginalFilename()).append(" ");
		        
	            String imagePath = fileNameAndPath.toString();
	            String originalFileName = file.getOriginalFilename();
	            String baseName = originalFileName.substring(0, originalFileName.lastIndexOf('.'));
	            String outputPath = uploadDirectory + "/" + baseName + "_with_ROI.jpg";
	            String encryptedOutputPath = uploadDirectory + "/" + baseName + ENCRYPT_OBJ;
	            String roiImagePath = uploadDirectory + "/" + baseName + "_cropped_R.jpg";
	            String emojiOverlayPath = uploadDirectory + "/" + baseName + "_emoji_R.jpg";
	            String finalFileName = baseName + "_with_ROI.jpg";

	            // Define the region of interest (ROI), the position determined by offsets x and y 
	            int roiX = 500;
	            int roiY = 500;
	            int roiWidth = 400;
	            int roiHeight = 400;
			
	            File imageFile = new File(imagePath);
	    	    if (!imageFile.exists()) {
	    	        System.err.println("The file " + imagePath + " does not exist");
	    	    }
	    	    
	    	        BufferedImage originalImage = ImageIO.read(imageFile);
	    	        if (originalImage == null) {
	    	            System.err.println("Could not read the image file");
	    	        }
	    	        
		            BufferedImage croppedImage = cropImage(originalImage, roiX, roiY, roiWidth, roiHeight);

		            String savedCroppedImagePath = saveImage(croppedImage, roiImagePath);
		            System.out.println("savedCroppedImagePath::::: " + savedCroppedImagePath);

		            BufferedImage emojiImage = ImageIO.read(new File(uploadDirectory + "/" + "Emoji.jpeg"));
		            BufferedImage resizedEmojiImage = resizeImage(emojiImage, roiWidth, roiHeight);

		            BufferedImage emojiOverlayImage = addEmojiOverlay(croppedImage, resizedEmojiImage);

		            String savedEmojiOverlayImagePath = saveImage(emojiOverlayImage, emojiOverlayPath);
		            System.out.println("savedEmojiOverlayImagePath::::: " + savedEmojiOverlayImagePath);

		            BufferedImage mergedImage = mergeImages(originalImage, emojiOverlayImage, roiX, roiY);

		            String savedMergedImagePath = saveImage(mergedImage, outputPath);
		            System.out.println("savedMergedImagePath::::: " + savedMergedImagePath);


				// Replacement box
					
			     // path of separate image stream (R)
                RoiParamHandler roiParamHandler = new RoiParamHandler();
                
                // To set the offsets
                roiParamHandler.setOffsetX(roiX);
                roiParamHandler.setOffsetY(roiY);
                
                ContiguousCodestreamBox jp2cBox = new ContiguousCodestreamBox();
                // set the URL of the cropped image R
                jp2cBox.setFileUrl(savedCroppedImagePath);
                
                ReplacementDescriptionBox replacementDescriptionBox = new ReplacementDescriptionBox();
                replacementDescriptionBox.setAutoApply(true); // if you want to replace the box automatically
                replacementDescriptionBox.setReplacementTypeId(ReplacementType.ROI.getId());
                replacementDescriptionBox.setParamHandler(roiParamHandler);
                
                ReplacementContentType replacementContentType = new ReplacementContentType();
                
                JumbfBoxBuilder jumbfBoxBuilder = new JumbfBoxBuilder(replacementContentType);
                jumbfBoxBuilder.appendContentBox(replacementDescriptionBox);
                jumbfBoxBuilder.appendContentBox(jp2cBox);
                JumbfBox jRBox = jumbfBoxBuilder.getResult();
                
                coreGeneratorService.generateJumbfMetadataToFile(List.of(jRBox), uploadDirectory + "/" + finalFileName + "ecryptReplacementBox.jumbf");
                
                
				try {
					Cipher cipher = Cipher.getInstance("AES");
					cipher.init(Cipher.ENCRYPT_MODE, SecretKeyUtil.getKeyFromPassword("abc", "xyz")); // get userId
					
					File fileToRead = new File(uploadDirectory + "/" + finalFileName + "ecryptReplacementBox.jumbf");
					
					if (!fileToRead.exists() || fileToRead.isDirectory()) {
					    System.err.println("File does not exist or is a directory: " + fileToRead.getAbsolutePath());
					} else {
						
					CipherInputStream cipt = new CipherInputStream(new FileInputStream(fileToRead), cipher);
					
					FileOutputStream fileip = new FileOutputStream(encryptedOutputPath); //.obj
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
					     
					     
				String xacmlContentString = XacmlTemplate.generateXmlPolicyToViewRoiImages(finalFileName, groupsToView);	     
                
                // Protection box
				
				ProtectionDescriptionBox protectionDescriptionBox = new ProtectionDescriptionBox();
				protectionDescriptionBox.setAes256CbcProtection();
				protectionDescriptionBox.setArLabel("roi-reference-group");
				protectionDescriptionBox.includeAccessRulesInToggle();

				BinaryDataBox binaryDataBox = new BinaryDataBox();
				
				// set the URL of the modified image
				binaryDataBox.setFileUrl(encryptedOutputPath);
				
				ProtectionContentType protectionContentType = new ProtectionContentType();
				jumbfBoxBuilder = new JumbfBoxBuilder(protectionContentType);

				jumbfBoxBuilder.appendContentBox(protectionDescriptionBox);
				jumbfBoxBuilder.appendContentBox(binaryDataBox);

				JumbfBox jBox = jumbfBoxBuilder.getResult();
				
				// XML Box
				
				// XmlContentType have inbuild DescriptionBox
				XmlContentType xmlContentType = new XmlContentType();

				jumbfBoxBuilder = new JumbfBoxBuilder(xmlContentType);

				// both the Labels should be same
				jumbfBoxBuilder.setLabel("roi-reference-group");

				XmlBox xmlBox = new XmlBox();
//				System.out.println(xacmlContentString);
				// setting StringBuilder content to String
				xmlBox.setContent(xacmlContentString.toString().getBytes());

				jumbfBoxBuilder.appendContentBox(xmlBox);
				JumbfBox jBoxForXmlBox = jumbfBoxBuilder.getResult();
				
				coreGeneratorService.generateJumbfMetadataToFile(List.of(jBox, jBoxForXmlBox), uploadDirectory + "/" + finalFileName + ENCRYPT_JUMBF);
				
	            Files.write(fileNameAndPath, file.getBytes());
	            
				//Save to DB
				User createdBy = userRepository.findByUsername(principal.getName());
				// save in images table
				Image image = imageService.saveImage(uploadDirectory, finalFileName, createdBy);
				
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
				
				model.addAttribute("msg", "Successfully uploaded files " + finalFileName);
		}
		
		return "uploadImageStatus";
	}

	
		/** Display the list of files from the "filesDirectory" */
		@GetMapping("/viewImageRoiPage")
		public String viewImageRoiPage(Model model, Principal principal) {
	
		    List<Image> listOfAllImages = imageService.findAllVisibleImages();
		    
		    List<Image> images = listOfAllImages.stream()
	                .filter(image -> image.getImageName().contains("ROI"))
	                .collect(Collectors.toList());
	
		    model.addAttribute("images", images);
			
			return "viewImagesForRoi";
		}
	
	
		/**  */
		@GetMapping("/viewIndividualROI")
		public String viewIndividualROI(Model model, 
				@RequestParam("imageId") Long imageId,
				Principal principal)
				throws MipamsException, NoSuchAlgorithmException, NoSuchPaddingException, InvalidKeyException,
				InvalidKeySpecException, IOException, SAXException, ParserConfigurationException {
	
			
			System.out.println("principal.getName()" + principal.getName());
	        System.out.println("imageId:::" + imageId);
	
	        Optional<Image> imageOptional = imageService.findByImageId(imageId);
	
	        if (imageOptional.isPresent()) {
	            Image image = imageOptional.get();
	            String imageName = image.getImageName();
	            System.out.println("images.getImagename():::" + imageName);
	            
	            File imageFile = new File(filesDirectory, imageName);
	
	            if (imageFile.exists() && imageFile.isFile()) {
	                System.out.println("Image found at: " + imageFile.getAbsolutePath());
	
	                byte[] imageBytes = Files.readAllBytes(imageFile.toPath());
	                String base64Image = Base64.getEncoder().encodeToString(imageBytes);
	
	                model.addAttribute("imageId", imageId);
	                model.addAttribute("base64Image", base64Image);
	            } else {
	                System.out.println("Image not found in the specified folder.");
	            }
	        } else {
	            System.out.println("Image not found in the database.");
	        }
	
	
	        return "viewToDecryptRoiImage"; 
	    }
					
	
	
		@PostMapping("/decryptAndViewImagesByROI")
		public String decryptAndViewImagesByROI(Model model, 
				@RequestParam("imageId") Long imageId,
				Principal principal)
				throws MipamsException, NoSuchAlgorithmException, NoSuchPaddingException, InvalidKeyException,
				InvalidKeySpecException, IOException, InvalidAlgorithmParameterException, ParserConfigurationException, SAXException {
			
				System.out.println("principal.getName()::::"+principal.getName());
				System.out.println("imageId::::"+imageId);
				
				Optional<Image> imageOptional = imageService.findByImageId(imageId);
			
				Image images = imageOptional.get();
				String imageName = images.getImageName();
				String filePath = images.getFilePath();
				String baseImagePath = filePath + "/" + imageName;
				System.out.println("imageName:::"+imageName);
				System.out.println("filePath:::"+filePath);
				System.out.println("baseImagePath:::"+baseImagePath);
				
				if(filePath.equalsIgnoreCase(uploadDirectory)) {
					
					String jumbfFileName = images.getImageName() + ENCRYPT_JUMBF;
					int lastDotIndex = jumbfFileName.lastIndexOf(".");
					String baseFileName = jumbfFileName.substring(0, lastDotIndex);
					String outputPath = uploadDirectory + "/" + baseFileName + DECRYPT_JPEG;
					System.out.println("baseFileName:::"+baseFileName);
					System.out.println("jumbfFileName:::::"+jumbfFileName);

					List<JumbfBox> bBoxes = coreParserService.parseMetadataFromFile(uploadDirectory + "/" + jumbfFileName);
					JumbfBox jBoxess = bBoxes.get(0);
					
					BinaryDataBox bBox = (BinaryDataBox) jBoxess.getContentBoxList().get(1);
					bBox.getFileUrl();
					
					JumbfBox jBoxessXml = bBoxes.get(1);
					XmlBox xmlBox = (XmlBox)jBoxessXml.getContentBoxList().get(0);
//					System.out.println("xmlBox:::::"+ new String(xmlBox.getContent()));
					
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
				        	 
				        	 Cipher cipher = Cipher.getInstance("AES");
								cipher.init(Cipher.DECRYPT_MODE, SecretKeyUtil.getKeyFromPassword("abc", "xyz"));
								CipherInputStream ciptt = new CipherInputStream(new FileInputStream(new File(bBox.getFileUrl())), cipher);
					
								FileOutputStream fileipo = new FileOutputStream(uploadDirectory + "/" + baseFileName + "DecryptIntermediate");
					
								int j;
								while ((j = ciptt.read()) != -1) {
					
									fileipo.write(j);
								}
								
							//ReplacementBox
							
							List<JumbfBox> rBoxes = coreParserService.parseMetadataFromFile(uploadDirectory + "/" + baseFileName + "DecryptIntermediate");
							JumbfBox jBoxess1 = rBoxes.get(0);
							
							ContiguousCodestreamBox contiguousCodestreamBox = (ContiguousCodestreamBox) jBoxess1.getContentBoxList().get(1);
							System.out.println("contiguousCodestreamBox:::::"+ contiguousCodestreamBox.getFileUrl());
							
							// getting the cropped image inside the temp folder
							ReplacementDescriptionBox replacementDescriptionBox = (ReplacementDescriptionBox) jBoxess1.getContentBoxList().get(0);
							System.out.println("replacementDescriptionBox:::::"+ replacementDescriptionBox.getParamHandler());
							
							RoiParamHandler roiParamHandler = new RoiParamHandler();
							roiParamHandler = (RoiParamHandler) replacementDescriptionBox.getParamHandler();
							
							try {
					            // Load the base image
					            BufferedImage baseImage = ImageIO.read(new File(baseImagePath));
					            if (baseImage == null) {
					                System.err.println("Could not read the base image file.");
					            }

				            // The image stream R’ is merged with the image stream I’
				            BufferedImage croppedImage = ImageIO.read(new File(contiguousCodestreamBox.getFileUrl()));
				            if (croppedImage == null) {
				                System.err.println("Could not read the cropped image file.");
				            }
			
				            // the position determined by offsets x and y. 
				            int offsetX = roiParamHandler.getOffsetX(); 
				            int offsetY = roiParamHandler.getOffsetY(); 
				            System.out.println("offsetX:::::"+ offsetX);
				            System.out.println("offsetY:::::"+ offsetY);
			
				            // Merge the cropped image back into the base image at the specified offsets
				            BufferedImage mergedImage = mergeImages(baseImage, croppedImage, offsetX, offsetY);
			
				            // Save the resulting image
				            String decryptImage = saveImage(baseImage, outputPath);
				            System.out.println("Image decoded and saved successfully at " + decryptImage);
				            
				        } catch (IOException e) {
				            System.err.println("Error processing the image: " + e.getMessage());
				            e.printStackTrace();
				        }
							model.addAttribute("msg", "Successfully decrypted files " + baseFileName + DECRYPT_JPEG + ", go back to main menu to view this image");
							model.addAttribute("fileName", baseFileName + DECRYPT_JPEG);
				        	 
				        	 
				         } else {
							model.addAttribute("error", "User not allowed to view image");
							return "error";
						}		
				        

			}
			return "viewImage";

	}	

}
