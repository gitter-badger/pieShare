/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.pieShare.pieShareApp.springConfiguration;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.SynchronousQueue;
import java.util.concurrent.TimeUnit;
import org.pieShare.pieTools.pieUtilities.service.base64Service.Base64Service;
import org.pieShare.pieTools.pieUtilities.service.beanService.BeanService;
import org.pieShare.pieTools.pieUtilities.service.commandParser.Argparse4jService;
import org.pieShare.pieTools.pieUtilities.service.compressor.Compressor;
import org.pieShare.pieTools.pieUtilities.service.propertiesReader.PropertiesReader;
import org.pieShare.pieTools.pieUtilities.service.eventBase.EventBase;
import org.pieShare.pieTools.pieUtilities.service.idService.IDService;
import org.pieShare.pieTools.pieUtilities.service.networkService.UdpPortService;
import org.pieShare.pieTools.pieUtilities.service.pieExecutorService.PieExecutorService;
import org.pieShare.pieTools.pieUtilities.service.pieExecutorService.PieExecutorTaskFactory;
import org.pieShare.pieTools.pieUtilities.service.regexService.RegexService;
import org.pieShare.pieTools.pieUtilities.service.security.BouncyCastleProviderService;
import org.pieShare.pieTools.pieUtilities.service.security.encodeService.EncodeService;
import org.pieShare.pieTools.pieUtilities.service.security.hashService.MD5Service;
import org.pieShare.pieTools.pieUtilities.service.security.pbe.PasswordEncryptionService;
import org.pieShare.pieTools.pieUtilities.service.shutDownService.ShutdownService;
import org.pieShare.pieTools.pieUtilities.service.tempFolderService.TempFolderService;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Lazy;
import org.springframework.context.annotation.Scope;

/**
 *
 * @author Svetoslav
 */
@Configuration
public class PieUtilitiesConfiguration {

    @Bean
    @Lazy
    public BeanService beanService() {
        return new BeanService();
    }

    @Bean
    @Lazy
    @Scope(value = "prototype")
    public Map javaMap() {
        return new HashMap();
    }

    @Bean
    @Lazy
    public ExecutorService javaExecutorService() {
        return java.util.concurrent.Executors.newCachedThreadPool();
    }

    @Bean
    @Lazy
    public PieExecutorService pieExecutorService() {
        PieExecutorService service = PieExecutorService.newCachedPieExecutorService();
        //service.setExecutor(this.javaExecutorService());
        service.setExecutorFactory(this.pieExecutorTaskFactory());
        return service;
    }

    @Bean
    @Lazy
    public PieExecutorTaskFactory pieExecutorTaskFactory() {
        PieExecutorTaskFactory factory = new PieExecutorTaskFactory();
        factory.setBeanService(this.beanService());
        factory.setTasks(this.javaMap());
        return factory;
	}
	
	@Bean
	@Lazy
	public ShutdownService shutdownService() {
		ShutdownService service = new ShutdownService();
		service.setListener(this.pieExecutorService());
		return service;
    }

    @Bean
    @Lazy
    public Argparse4jService argparse4jService() {
        return new Argparse4jService();
    }

    @Bean
    @Lazy
    public Base64Service base64Service() {
        return new Base64Service();
    }

    @Bean
    @Lazy
    @Scope(value = "prototype")
    public Compressor compressor() {
        Compressor com = new Compressor();
        com.setBase64Service(base64Service());
        return com;
    }

    @Bean
    @Lazy
    public PropertiesReader configurationReader() {
        return new PropertiesReader();
    }

    @Bean
    @Lazy
    public BouncyCastleProviderService providerService() {
        return new BouncyCastleProviderService();
    }

    @Bean
    @Lazy
    public PasswordEncryptionService passwordEncryptionService() {
        PasswordEncryptionService service = new PasswordEncryptionService();
        service.setProviderService(this.providerService());
        return service;
    }

    @Bean
    @Lazy
    public TempFolderService tempFolderService() {
        return new TempFolderService();
    }

    @Bean
    @Lazy
    public MD5Service md5Service() {
        MD5Service service = new MD5Service();
        service.setProviderService(this.providerService());
        return service;
    }

    @Bean
    @Lazy
    @Scope(value = "prototype")
    public EventBase eventBase() {
        return new EventBase();
    }

    @Bean
    @Lazy
    @Scope(value = "prototype")
    public RegexService regexService() {
        return new RegexService();
    }

    @Bean
    @Lazy
    public EncodeService encodeService() {
        EncodeService service = new EncodeService();
        service.setPasswordEncryptionService(passwordEncryptionService());
        service.setProviderService(providerService());
        return service;
    }

    @Bean
    @Lazy
    public IDService idService() {
        IDService service = new IDService();
        return service;
    }

    @Bean
    @Lazy
    public UdpPortService udpPortService() {
        UdpPortService service = new UdpPortService();
        return service;
    }
}
