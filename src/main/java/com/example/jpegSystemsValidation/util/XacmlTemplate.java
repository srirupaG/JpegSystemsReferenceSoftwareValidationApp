package com.example.jpegSystemsValidation.util;

import java.util.List;

public final class XacmlTemplate {
	
	
	 public static String generatePolicyXmlForImageAccessBasedOnGroup(String imageName, List<String> viewGroupNames, List<String> editGroupNames) {
        StringBuilder xmlBuilder = new StringBuilder();
        
        xmlBuilder.append("<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n");
        xmlBuilder.append("<Policy xmlns=\"urn:oasis:names:tc:xacml:3.0:core:schema:wd-17\" ");
        xmlBuilder.append("xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\" ");
        xmlBuilder.append("xsi:schemaLocation=\"urn:oasis:names:tc:xacml:3.0:core:schema:wd-17 http://docs.oasis-open.org/xacml/3.0/xacml-core-v3-schema-wd-17.xsd\" ");
        xmlBuilder.append("PolicyId=\"urn:isdcm:policyid:1\" ");
        xmlBuilder.append("RuleCombiningAlgId=\"urn:oasis:names:tc:xacml:1.0:rule-combining-algorithm:first-applicable\" ");
        xmlBuilder.append("Version=\"1.0\">\n");
        xmlBuilder.append("    <Description>Policy</Description>\n");
        xmlBuilder.append("    <Target>\n");
        xmlBuilder.append("        <AnyOf>\n");
        xmlBuilder.append("            <AllOf>\n");
        xmlBuilder.append("                <Match MatchId=\"urn:oasis:names:tc:xacml:1.0:function:string-equal\">\n");
        xmlBuilder.append("                    <AttributeValue DataType=\"http://www.w3.org/2001/XMLSchema#string\">").append(imageName).append("</AttributeValue>\n");       
        xmlBuilder.append("                    <AttributeDesignator MustBePresent=\"false\" ");
        xmlBuilder.append("                        AttributeId=\"urn:oasis:names:tc:xacml:1.0:resource:image-name\" ");
        xmlBuilder.append("                        Category=\"urn:oasis:names:tc:xacml:3.0:attribute-category:resource\" ");
        xmlBuilder.append("                        DataType=\"http://www.w3.org/2001/XMLSchema#string\"/>\n");
        xmlBuilder.append("                </Match>\n");
        xmlBuilder.append("            </AllOf>\n");
        xmlBuilder.append("        </AnyOf>\n");
        xmlBuilder.append("    </Target>\n");

        // Rule for allowing specific Group to view the image
        xmlBuilder.append("    <Rule RuleId=\"urn:oasis:names:tc:xacml:3.0:permit-image-view\" Effect=\"Permit\">\n");
        xmlBuilder.append("        <Target>\n");
        xmlBuilder.append("            <AnyOf>\n");
        xmlBuilder.append("                <AllOf>\n");
        xmlBuilder.append("                    <!-- Which group -->\n");
        for (String viewGroup : viewGroupNames) {
            xmlBuilder.append("                    <Match MatchId=\"urn:oasis:names:tc:xacml:1.0:function:string-equal\">\n");
            xmlBuilder.append("                        <AttributeValue DataType=\"http://www.w3.org/2001/XMLSchema#string\">").append(viewGroup).append("</AttributeValue>\n");
            xmlBuilder.append("                        <AttributeDesignator MustBePresent=\"false\" ");
            xmlBuilder.append("                            AttributeId=\"urn:oasis:names:tc:xacml:1.0:subject:subject-id\" ");
            xmlBuilder.append("                            Category=\"urn:oasis:names:tc:xacml:1.0:subject-category:access-subject\" ");
            xmlBuilder.append("                            DataType=\"http://www.w3.org/2001/XMLSchema#string\"/>\n");
            xmlBuilder.append("                    </Match>\n");
        }
        xmlBuilder.append("                    <!-- Which action  -->\n");
        xmlBuilder.append("                    <Match MatchId=\"urn:oasis:names:tc:xacml:1.0:function:string-equal\">\n");
        xmlBuilder.append("                        <AttributeValue DataType=\"http://www.w3.org/2001/XMLSchema#string\">view</AttributeValue>\n");
        xmlBuilder.append("                        <AttributeDesignator MustBePresent=\"false\" ");
        xmlBuilder.append("                            Category=\"urn:oasis:names:tc:xacml:3.0:attribute-category:action\" ");
        xmlBuilder.append("                            AttributeId=\"urn:oasis:names:tc:xacml:1.0:action:action-id\" ");
        xmlBuilder.append("                            DataType=\"http://www.w3.org/2001/XMLSchema#string\"/>\n");
        xmlBuilder.append("                    </Match>\n");
        xmlBuilder.append("                </AllOf>\n");
        xmlBuilder.append("            </AnyOf>\n");
        xmlBuilder.append("        </Target>\n");
        xmlBuilder.append("    </Rule>\n");
        // Rule for allowing image editing
        xmlBuilder.append("    <Rule RuleId=\"urn:oasis:names:tc:xacml:3.0:permit-image-editing\" Effect=\"Permit\">\n");
        xmlBuilder.append("        <Target>\n");
        xmlBuilder.append("            <AnyOf>\n");
        xmlBuilder.append("                <AllOf>\n");
        xmlBuilder.append("                    <!-- Which group -->\n");
        for (String editGroup : editGroupNames) {
            xmlBuilder.append("                    <Match MatchId=\"urn:oasis:names:tc:xacml:1.0:function:string-equal\">\n");
            xmlBuilder.append("                        <AttributeValue DataType=\"http://www.w3.org/2001/XMLSchema#string\">").append(editGroup).append("</AttributeValue>\n");
            xmlBuilder.append("                        <AttributeDesignator MustBePresent=\"false\" ");
            xmlBuilder.append("                            AttributeId=\"urn:oasis:names:tc:xacml:1.0:subject:subject-id\" ");
            xmlBuilder.append("                            Category=\"urn:oasis:names:tc:xacml:1.0:subject-category:access-subject\" ");
            xmlBuilder.append("                            DataType=\"http://www.w3.org/2001/XMLSchema#string\"/>\n");
            xmlBuilder.append("                    </Match>\n");
        }
        xmlBuilder.append("                    <!-- Which action  -->\n");
        xmlBuilder.append("                    <Match MatchId=\"urn:oasis:names:tc:xacml:1.0:function:string-equal\">\n");
        xmlBuilder.append("                        <AttributeValue DataType=\"http://www.w3.org/2001/XMLSchema#string\">edit</AttributeValue>\n");
        xmlBuilder.append("                        <AttributeDesignator MustBePresent=\"false\" ");
        xmlBuilder.append("                            Category=\"urn:oasis:names:tc:xacml:3.0:attribute-category:action\" ");
        xmlBuilder.append("                            AttributeId=\"urn:oasis:names:tc:xacml:1.0:action:action-id\" ");
        xmlBuilder.append("                            DataType=\"http://www.w3.org/2001/XMLSchema#string\"/>\n");
        xmlBuilder.append("                    </Match>\n");
        xmlBuilder.append("                </AllOf>\n");
        xmlBuilder.append("            </AnyOf>\n");
        xmlBuilder.append("        </Target>\n");
        xmlBuilder.append("    </Rule>\n");

        xmlBuilder.append("</Policy>");
        return xmlBuilder.toString();
	 }
	 
	 
	 public static String generateXmlPolicyToViewRoiImages(String imageName, List<String> viewGroupNames) {
	        StringBuilder xmlBuilder = new StringBuilder();
	        
	        xmlBuilder.append("<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n");
	        xmlBuilder.append("<Policy xmlns=\"urn:oasis:names:tc:xacml:3.0:core:schema:wd-17\" ");
	        xmlBuilder.append("xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\" ");
	        xmlBuilder.append("xsi:schemaLocation=\"urn:oasis:names:tc:xacml:3.0:core:schema:wd-17 http://docs.oasis-open.org/xacml/3.0/xacml-core-v3-schema-wd-17.xsd\" ");
	        xmlBuilder.append("PolicyId=\"urn:isdcm:policyid:1\" ");
	        xmlBuilder.append("RuleCombiningAlgId=\"urn:oasis:names:tc:xacml:1.0:rule-combining-algorithm:first-applicable\" ");
	        xmlBuilder.append("Version=\"1.0\">\n");
	        xmlBuilder.append("    <Description>Policy</Description>\n");
	        xmlBuilder.append("    <Target>\n");
	        xmlBuilder.append("        <AnyOf>\n");
	        xmlBuilder.append("            <AllOf>\n");
	        xmlBuilder.append("                <Match MatchId=\"urn:oasis:names:tc:xacml:1.0:function:string-equal\">\n");
	        xmlBuilder.append("                    <AttributeValue DataType=\"http://www.w3.org/2001/XMLSchema#string\">").append(imageName).append("</AttributeValue>\n");       
	        xmlBuilder.append("                    <AttributeDesignator MustBePresent=\"false\" ");
	        xmlBuilder.append("                        AttributeId=\"urn:oasis:names:tc:xacml:1.0:resource:image-name\" ");
	        xmlBuilder.append("                        Category=\"urn:oasis:names:tc:xacml:3.0:attribute-category:resource\" ");
	        xmlBuilder.append("                        DataType=\"http://www.w3.org/2001/XMLSchema#string\"/>\n");
	        xmlBuilder.append("                </Match>\n");
	        xmlBuilder.append("            </AllOf>\n");
	        xmlBuilder.append("        </AnyOf>\n");
	        xmlBuilder.append("    </Target>\n");

	        // Rule for allowing specific Group to view the image
	        xmlBuilder.append("    <Rule RuleId=\"urn:oasis:names:tc:xacml:3.0:permit-image-view\" Effect=\"Permit\">\n");
	        xmlBuilder.append("        <Target>\n");
	        xmlBuilder.append("            <AnyOf>\n");
	        xmlBuilder.append("                <AllOf>\n");
	        xmlBuilder.append("                    <!-- Which group -->\n");
	        for (String viewGroup : viewGroupNames) {
	            xmlBuilder.append("                    <Match MatchId=\"urn:oasis:names:tc:xacml:1.0:function:string-equal\">\n");
	            xmlBuilder.append("                        <AttributeValue DataType=\"http://www.w3.org/2001/XMLSchema#string\">").append(viewGroup).append("</AttributeValue>\n");
	            xmlBuilder.append("                        <AttributeDesignator MustBePresent=\"false\" ");
	            xmlBuilder.append("                            AttributeId=\"urn:oasis:names:tc:xacml:1.0:subject:subject-id\" ");
	            xmlBuilder.append("                            Category=\"urn:oasis:names:tc:xacml:1.0:subject-category:access-subject\" ");
	            xmlBuilder.append("                            DataType=\"http://www.w3.org/2001/XMLSchema#string\"/>\n");
	            xmlBuilder.append("                    </Match>\n");
	        }
	        xmlBuilder.append("                    <!-- Which action  -->\n");
	        xmlBuilder.append("                    <Match MatchId=\"urn:oasis:names:tc:xacml:1.0:function:string-equal\">\n");
	        xmlBuilder.append("                        <AttributeValue DataType=\"http://www.w3.org/2001/XMLSchema#string\">view</AttributeValue>\n");
	        xmlBuilder.append("                        <AttributeDesignator MustBePresent=\"false\" ");
	        xmlBuilder.append("                            Category=\"urn:oasis:names:tc:xacml:3.0:attribute-category:action\" ");
	        xmlBuilder.append("                            AttributeId=\"urn:oasis:names:tc:xacml:1.0:action:action-id\" ");
	        xmlBuilder.append("                            DataType=\"http://www.w3.org/2001/XMLSchema#string\"/>\n");
	        xmlBuilder.append("                    </Match>\n");
	        xmlBuilder.append("                </AllOf>\n");
	        xmlBuilder.append("            </AnyOf>\n");
	        xmlBuilder.append("        </Target>\n");
	        xmlBuilder.append("    </Rule>\n");

	        xmlBuilder.append("</Policy>");
	        return xmlBuilder.toString();
		 }

}
