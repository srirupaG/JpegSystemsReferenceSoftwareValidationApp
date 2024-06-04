package com.example.jpegSystemsValidation.util;

import org.mipams.jumbf.BmffBoxServiceDiscoveryManager;
import org.mipams.jumbf.ContentTypeDiscoveryManager;
import org.mipams.jumbf.entities.BinaryDataBox;
import org.mipams.jumbf.entities.ContiguousCodestreamBox;
import org.mipams.jumbf.services.CoreGeneratorService;
import org.mipams.jumbf.services.CoreParserService;
import org.mipams.jumbf.services.boxes.BinaryDataBoxService;
import org.mipams.jumbf.services.boxes.ContiguousCodestreamBoxService;
import org.mipams.jumbf.services.boxes.DescriptionBoxService;
import org.mipams.jumbf.services.boxes.JumbfBoxService;
import org.mipams.jumbf.services.boxes.PaddingBoxService;
import org.mipams.jumbf.services.boxes.PrivateBoxService;
import org.mipams.jumbf.services.boxes.XmlBoxService;
import org.mipams.jumbf.services.content_types.XmlContentType;
import org.mipams.privsec.entities.ProtectionDescriptionBox;
import org.mipams.privsec.entities.ReplacementDescriptionBox;
import org.mipams.privsec.entities.replacement.RoiParamHandler;
import org.mipams.privsec.services.boxes.ProtectionDescriptionBoxService;
import org.mipams.privsec.services.boxes.ReplacementDescriptionBoxService;
import org.mipams.privsec.services.boxes.replacement.AppReplacementHandler;
import org.mipams.privsec.services.boxes.replacement.BoxReplacementHandler;
import org.mipams.privsec.services.boxes.replacement.DataBoxHandlerFactory;
import org.mipams.privsec.services.boxes.replacement.ParamHandlerFactory;
import org.mipams.privsec.services.boxes.replacement.RoiReplacementHandler;
import org.mipams.privsec.services.content_types.ProtectionContentType;
import org.mipams.privsec.services.content_types.ReplacementContentType;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class CoreServiceBean {

	
	@Bean
	public CoreParserService coreParserService() {
		return new CoreParserService();
	}

	@Bean
	public CoreGeneratorService coreGeneratorService() {
		return new CoreGeneratorService();
	}

	@Bean
	public ProtectionDescriptionBox protectionDescriptionBox() {
		return new ProtectionDescriptionBox();

	}

	@Bean
	public BinaryDataBox binaryDataBox() {
		return new BinaryDataBox();

	}

	@Bean
	public ProtectionContentType protectionContentType() {
		return new ProtectionContentType();

	}

	@Bean
	public JumbfBoxService jumbfBoxService() {
		return new JumbfBoxService();
	}

	@Bean
	public DescriptionBoxService descriptionBoxService() {
		return new DescriptionBoxService();
	}

	@Bean
	public BmffBoxServiceDiscoveryManager bmffBoxServiceDiscoveryManager() {
		return new BmffBoxServiceDiscoveryManager();
	}

	@Bean
	public PrivateBoxService privateBoxService() {
		return new PrivateBoxService();
	}

	@Bean
	public PaddingBoxService paddingBoxService() {
		return new PaddingBoxService();
	}

	@Bean
	public ContentTypeDiscoveryManager contentBoxDiscoveryManager() {
		return new ContentTypeDiscoveryManager();
	}

	@Bean
	public ProtectionDescriptionBoxService protectionDescriptionBoxService() {
		return new ProtectionDescriptionBoxService();
	}

	@Bean
	public BinaryDataBoxService binaryDataBoxService() {
		return new BinaryDataBoxService();
	}
	
    @Bean
    public XmlContentType xmlContentType() {
        return new XmlContentType();
    }
    
    @Bean
    public XmlBoxService xmlBoxService() {
        return new XmlBoxService();
    }
    
	@Bean
	public ReplacementDescriptionBox replacementDescriptionBox() {
		return new ReplacementDescriptionBox();

	}
	
	@Bean
	public RoiParamHandler roiParamHandler() {
		return new RoiParamHandler();

	}
	
	@Bean
	public ContiguousCodestreamBox contiguousCodestreamBox() {
		return new ContiguousCodestreamBox();

	}
	
	@Bean
	public ReplacementContentType replacementContentType() {
		return new ReplacementContentType();

	}
	
	@Bean
	public ReplacementDescriptionBoxService replacementDescriptionBoxService() {
		return new ReplacementDescriptionBoxService();

	}
	
	@Bean
	public ParamHandlerFactory paramHandlerFactory() {
		return new ParamHandlerFactory();

	}
	
	@Bean
	public DataBoxHandlerFactory dataBoxHandlerFactory() {
		return new DataBoxHandlerFactory();

	}
	
	@Bean
	public BoxReplacementHandler boxReplacementHandler() {
		return new BoxReplacementHandler();

	}
	
	@Bean
	public AppReplacementHandler appReplacementHandler() {
		return new AppReplacementHandler();

	}
	
	@Bean
	public RoiReplacementHandler roiReplacementHandler() {
		return new RoiReplacementHandler();

	}
	
	@Bean
	public ContiguousCodestreamBoxService contiguousCodestreamBoxService() {
		return new ContiguousCodestreamBoxService();

	}

}
