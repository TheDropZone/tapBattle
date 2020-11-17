package com.tapBattle.server.resources;

import java.util.List;
import java.util.Optional;

import javax.persistence.EntityManager;
import javax.persistence.QueryHint;
import javax.persistence.TypedQuery;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;

import io.dropwizard.hibernate.UnitOfWork;
import org.hibernate.CacheMode;
import org.hibernate.Session;
import org.hibernate.jpa.QueryHints;
import org.hibernate.query.Query;

import com.codahale.metrics.annotation.Timed;
import com.tapBattle.server.UserPrincipal;
import com.tapBattle.server.entities.Battle;
import com.tapBattle.server.entities.User;

import io.dropwizard.auth.Auth;
import io.swagger.annotations.Api;

@Api
@Path("/user")
@Produces(MediaType.APPLICATION_JSON)
public class UserResource {

	EntityManager manager;
	
	public UserResource(EntityManager manager) {
		this.manager = manager;
	}
	
	@GET
	@Timed
	public User getUser(@Auth UserPrincipal user, @QueryParam("username") Optional<String> username) {
		try{
			User returnUser = manager.find(User.class, user.getId());
			manager.refresh(returnUser);
			return returnUser;
		}catch(Exception e){
			if(username.isPresent()) {
				safelyStartTransaction();
				User newUser = new User(user.getId());
				newUser.setUsername(username.get());
				User returnUser = manager.merge(newUser);
				manager.getTransaction().commit();
				return returnUser;
			}else {
				return null;
			}
		}
	}
	
	@GET
	@Path("/battles")
	public List<Battle> getUserBattles(@Auth UserPrincipal user){
		((Session)manager.unwrap(Session.class)).setCacheMode(CacheMode.IGNORE);
		safelyStartTransaction();
		TypedQuery<Battle> query = manager.createQuery("from Battle as b where b.player1.id = :id or " +
				"b.player2.id = :id order by b.timestamp ASC", Battle.class).setMaxResults(30);
		query.setParameter("id", user.getId());
		query.setHint(QueryHints.HINT_CACHE_MODE, CacheMode.IGNORE);
		try {
			List<Battle> battles = query.getResultList();
			manager.getTransaction().commit();
			return battles;
		}catch(javax.persistence.NoResultException e) {
			manager.getTransaction().commit();
		}
		return null;
	}
	
	@GET
	@Path("/ping")
	public String pingTest(){
		return "Pong";
	}


	private void safelyStartTransaction(){
		while(manager.getTransaction().isActive()) {
			try {
				Thread.sleep(5);
			} catch (InterruptedException e1) {
				e1.printStackTrace();
			}
		}
		manager.getTransaction().begin();
	}
}
