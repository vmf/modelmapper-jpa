package com.vmf.modelmapper.jpa.autoconfiguration;

import java.util.Properties;

import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.util.StringUtils;

import com.vmf.modelmapper.jpa.JpaModelMapper;

import jakarta.persistence.EntityManager;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Configuration
@ConditionalOnClass(EntityManager.class)
public class JpaModelMapperAutoConfiguration {
    
    /**
     * JPA entity manager.
     */
    private EntityManager em;

    /**
     * Autowired default constructor.
     * @param em JPA entity manager.
     */
    public JpaModelMapperAutoConfiguration(EntityManager em) {
	super();
	this.em = em;
    }

    @Bean
    @ConditionalOnMissingBean
    JpaModelMapper modelMapper() {
	JpaModelMapper modelMapper = new JpaModelMapper(em);
	log.info("JpaModelMapper loaded.");
	return modelMapper;
    }
    
    /**
     * Return the starter version.
     * @return the starter version.
     */
    public String getVersion() {
	try {
	    Properties prop = new Properties();
	    prop.load(getClass().getResourceAsStream("/META-INF/maven/com.vmf/jpa-modelmapper-spring-boot-starter/pom.properties"));
	    String version = prop.getProperty("version");
	    return StringUtils.hasText(version) ? "[" + version + "] " : "";
	} catch (Exception e) {
	    return "unknown";
	}
    }

}
