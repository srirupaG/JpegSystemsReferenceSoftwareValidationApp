package com.example.jpegSystemsValidation.util;

import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

public class XacmlParsing {
	
	
	public static boolean checkPermissionByRuleToViewOrEdit(Element rootElement, String userGroup, String ruleIdToCheck) {
//		System.out.println("userGroup"+userGroup);
	    NodeList ruleNodes = rootElement.getElementsByTagName("Rule");
	    for (int i = 0; i < ruleNodes.getLength(); i++) {
	        Element ruleElement = (Element) ruleNodes.item(i);
	        String ruleId = ruleElement.getAttribute("RuleId");
	        if (ruleId.equals(ruleIdToCheck)) {
	            NodeList allOfNodes = ruleElement.getElementsByTagName("AllOf");
	            for (int j = 0; j < allOfNodes.getLength(); j++) {
	                Element allOfElement = (Element) allOfNodes.item(j);
	                NodeList matchNodes = allOfElement.getElementsByTagName("Match");
	                boolean allMatch = false; 
	                for (int k = 0; k < matchNodes.getLength(); k++) {
	                    Element matchElement = (Element) matchNodes.item(k);
	                    Element attributeValueElement = (Element) matchElement.getElementsByTagName("AttributeValue").item(0);
	                    if (attributeValueElement == null) continue;
	                    String attributeValue = attributeValueElement.getTextContent();
	                    // Check if the userGroup matches any of the specified groups
	                    if (attributeValue.equals(userGroup)) {
	                        allMatch = true;
	                     // If match found, set allMatch to true and exit the loop
	                        break; 
	                    }
	                }
	                if (allMatch) {
	                	// If any match found within <AllOf>, return true
	                    return true; 
	                }
	            }
	        }
	    }
	    // If no matching rule found, return false
	    return false; 
	}

	
	public static boolean checkPermission(Element rootElement, String userRole, String action) {
	    boolean permit;
	    if (action.equals("view")) {
	        permit = checkPermissionByRule(rootElement, userRole, "urn:example:rule:permit-image-viewing") 
	        		|| checkPermissionByCreation(rootElement, userRole)
	        		;
	    } else if (action.equals("edit")) {
	        permit = checkPermissionByRule(rootElement, userRole, "urn:example:rule:permit-image-editing");
	    } else {
	        permit = false; 
	    }
	    return permit;
	}

	private static boolean checkPermissionByRule(Element rootElement, String userRole, String ruleIdToCheck) {
	    NodeList ruleNodes = rootElement.getElementsByTagName("Rule");
	    for (int i = 0; i < ruleNodes.getLength(); i++) {
	        Element ruleElement = (Element) ruleNodes.item(i);
	        String ruleId = ruleElement.getAttribute("RuleId");
	        if (ruleId.equals(ruleIdToCheck)) {
	            NodeList allOfNodes = ruleElement.getElementsByTagName("AllOf");
	            for (int j = 0; j < allOfNodes.getLength(); j++) {
	                Element allOfElement = (Element) allOfNodes.item(j);
	                NodeList matchNodes = allOfElement.getElementsByTagName("Match");
	                for (int k = 0; k < matchNodes.getLength(); k++) {
	                    Element matchElement = (Element) matchNodes.item(k);
	                    Element attributeValueElement = (Element) matchElement.getElementsByTagName("AttributeValue").item(0);
	                    if (attributeValueElement == null) continue;
	                    String attributeValue = attributeValueElement.getTextContent();
	                    if (attributeValue.equals(userRole)) {
	                        return true;
	                    }
	                }
	            }
	        }
	    }
	    return false;
	}

	private static boolean checkPermissionByCreation(Element rootElement, String userRole) {
	    return checkPermissionByRule(rootElement, userRole, "urn:example:rule:permit-image-creation");
	}

}
