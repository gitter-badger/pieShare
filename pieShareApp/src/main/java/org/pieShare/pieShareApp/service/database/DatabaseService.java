/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.pieShare.pieShareApp.service.database;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import javax.annotation.PostConstruct;
import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.Persistence;
import javax.persistence.Query;
import org.pieShare.pieShareApp.model.PieUser;
import org.pieShare.pieShareApp.model.entities.FilterEntity;
import org.pieShare.pieShareApp.model.entities.PieUserEntity;
import org.pieShare.pieShareApp.service.configurationService.PieShareAppConfiguration;
import org.pieShare.pieShareApp.service.database.api.IDatabaseService;
import org.pieShare.pieShareApp.service.fileFilterService.FileFilter;
import org.pieShare.pieShareApp.service.fileFilterService.api.IFilter;
import org.pieShare.pieTools.pieUtilities.model.EncryptedPassword;
import org.pieShare.pieTools.pieUtilities.service.base64Service.api.IBase64Service;
import org.pieShare.pieTools.pieUtilities.service.beanService.IBeanService;

/**
 *
 * @author Richard
 */
public class DatabaseService implements IDatabaseService {

	private PieShareAppConfiguration appConfiguration;
	private EntityManagerFactory emf;
	private IBase64Service base64Service;
	private IBeanService beanService;

	public void setBase64Service(IBase64Service base64Service) {
		this.base64Service = base64Service;
	}

	public void setPieShareAppConfiguration(PieShareAppConfiguration config) {
		this.appConfiguration = config;
	}

	public void setBeanService(IBeanService beanService) {
		this.beanService = beanService;
	}

	@PostConstruct
	public void init() {
		emf = Persistence.createEntityManagerFactory(appConfiguration.getBaseConfigPath() + "/objectdb/db/points.odb");
	}

	@Override
	public synchronized void persistPieUser(PieUser service) {
		EntityManager em = emf.createEntityManager();
		PieUserEntity entity = new PieUserEntity();
		byte[] pwd = base64Service.encode(service.getPassword().getPassword());
		entity.setPassword(pwd);
		entity.setUserName(service.getUserName());
		em.getTransaction().begin();
		em.persist(entity);
		em.getTransaction().commit();
		em.close();
	}

	@Override
	public synchronized PieUser getPieUser(String name) {
		EntityManager em = emf.createEntityManager();
		PieUser user = null;
		try {
			PieUserEntity entity = em.find(PieUserEntity.class, name);
			if (entity == null) {
				return null;
			}
			user = new PieUser();
			user.setIsLoggedIn(false);
			EncryptedPassword paswd = new EncryptedPassword();
			paswd.setPassword(base64Service.decode(entity.getPassword()));
			user.setPassword(paswd);
			user.setUserName(entity.getUserName());
			em.close();
		} catch (IllegalArgumentException ex) {
			return null;
		}
		return user;
	}

	@Override
	public ArrayList<PieUser> findAllPieUsers() {
		EntityManager em = emf.createEntityManager();
		Query query = em.createQuery(String.format("SELECT e FROM %s e", PieUserEntity.class.getSimpleName()));

		ArrayList<PieUser> list = new ArrayList<>();
		for (PieUserEntity entity : ((Collection<PieUserEntity>) query.getResultList())) {
			PieUser user = new PieUser();
			user.setIsLoggedIn(false);
			EncryptedPassword paswd = new EncryptedPassword();
			paswd.setPassword(base64Service.decode(entity.getPassword()));
			user.setPassword(paswd);
			user.setUserName(entity.getUserName());
			list.add(user);
		}
		em.close();
		return list;
	}

	@Override
	public synchronized void persistFileFilter(IFilter filter) {
		EntityManager em = emf.createEntityManager();
		FilterEntity en = new FilterEntity();
		en.setPattern(filter.getPattern());
		em.getTransaction().begin();
		em.persist(en);
		em.getTransaction().commit();
		em.close();
		filter.setEntity(en);
	}

	@Override
	public synchronized void removeFileFilter(IFilter filter) {
		EntityManager em = emf.createEntityManager();
		em.getTransaction().begin();

		FilterEntity f = em.find(FilterEntity.class, filter.getEntity().getId());
		em.remove(f);

		em.getTransaction().commit();
		em.close();
	}

	@Override
	public ArrayList<IFilter> findAllFilters() {
		EntityManager em = emf.createEntityManager();
		Query query = em.createQuery(String.format("SELECT e FROM %s e", FilterEntity.class.getSimpleName()));
		ArrayList<IFilter> list = new ArrayList<>();

		List resultList;

		try {
			resultList = query.getResultList();
		} catch (Exception ex) {
			return list;
		}

		if (!resultList.isEmpty()) {
			for (FilterEntity entity : (Collection<FilterEntity>) resultList) {
				IFilter filter = beanService.getBean(FileFilter.class);
				filter.setEntity(entity);
				filter.setPattern(entity.getPattern());
				list.add(filter);
			}
		}
		em.close();
		return list;
	}
}
