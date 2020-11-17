package com.tapBattle.server;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.EnumSet;

import javax.persistence.EntityManager;
import javax.servlet.DispatcherType;
import javax.servlet.FilterRegistration;
import javax.servlet.ServletRegistration;

import io.dropwizard.hibernate.UnitOfWorkAwareProxyFactory;
import org.atmosphere.cpr.ApplicationConfig;
import org.atmosphere.cpr.AtmosphereServlet;
import org.atmosphere.interceptor.CorsInterceptor;
import org.eclipse.jetty.servlets.CrossOriginFilter;

import com.tapBattle.server.entities.Battle;
import com.tapBattle.server.entities.User;
import com.tapBattle.server.resources.BattleResource;
import com.tapBattle.server.resources.UserResource;

import io.dropwizard.Application;
import io.dropwizard.auth.AuthDynamicFeature;
import io.dropwizard.auth.AuthValueFactoryProvider;
import io.dropwizard.auth.oauth.OAuthCredentialAuthFilter;
import io.dropwizard.db.DataSourceFactory;
import io.dropwizard.hibernate.HibernateBundle;
import io.dropwizard.setup.Bootstrap;
import io.dropwizard.setup.Environment;
import io.federecio.dropwizard.swagger.SwaggerBundle;
import io.federecio.dropwizard.swagger.SwaggerBundleConfiguration;
import io.dropwizard.configuration.EnvironmentVariableSubstitutor;
import io.dropwizard.configuration.SubstitutingSourceProvider;

public class tapBattleServerApplication extends Application<tapBattleServerConfiguration> {
	
    public static void main(final String[] args) throws Exception {
        new tapBattleServerApplication().run(args);
    }

    @Override
    public String getName() {
        return "tapBattleServer";
    }

    @Override
    public void initialize(final Bootstrap<tapBattleServerConfiguration> bootstrap) {
    	bootstrap.setConfigurationSourceProvider(
                new SubstitutingSourceProvider(bootstrap.getConfigurationSourceProvider(),
                                                   new EnvironmentVariableSubstitutor(false)
                )
    		);
        bootstrap.addBundle(hibernate);
        bootstrap.addBundle(new SwaggerBundle<tapBattleServerConfiguration>() {
            @Override
            protected SwaggerBundleConfiguration getSwaggerBundleConfiguration(tapBattleServerConfiguration configuration) {
                return configuration.swaggerBundleConfiguration;
            }
        });
        Thread.setDefaultUncaughtExceptionHandler(new Thread.UncaughtExceptionHandler() {
			@Override
			public void uncaughtException(Thread t, Throwable e) {
				StringWriter sw = new StringWriter();
				PrintWriter pw = new PrintWriter(sw);
				e.printStackTrace(pw);
				System.exit(1);
			}
		});
    }

    @Override
    public void run(final tapBattleServerConfiguration configuration,
                    final Environment environment) {
		final UserResource userResource = new UnitOfWorkAwareProxyFactory(hibernate)
				.create(UserResource.class, EntityManager.class, hibernate.getSessionFactory().createEntityManager());
    	BattleServer.initialize(hibernate.getSessionFactory().createEntityManager());
    	//final BattleResource battleResource = new BattleResource();
    	//environment.jersey().register(battleResource);
    	environment.jersey().register(userResource);
    	configureAuth(configuration, environment);
    	configureCors(configuration, environment);
    	configureAtmosphere(configuration, environment);
    	
    }

    private final HibernateBundle<tapBattleServerConfiguration> hibernate = new HibernateBundle<tapBattleServerConfiguration>(User.class, Battle.class) {
    	@Override
    	public DataSourceFactory getDataSourceFactory(tapBattleServerConfiguration configuration) {
    		return configuration.getDataSourceFactory();
    	}
    };
    
    private void configureAuth(final tapBattleServerConfiguration config, final Environment environment) {
    	try {
    		GoogleOAuth.initialize();
    		environment.jersey().register(new AuthDynamicFeature(
    			new	OAuthCredentialAuthFilter.Builder<UserPrincipal>()
    				.setAuthenticator(new UserAuthenticator())
    				.setPrefix("Bearer")
    				.buildAuthFilter()
    				));
    		environment.jersey().register(new AuthValueFactoryProvider.Binder<>(UserPrincipal.class));
    	}catch(Exception e) {
    		throw new IllegalStateException("Failed to configure OAuth");
    	}
    }
    
    private void configureCors(final tapBattleServerConfiguration config, final Environment environment) {
    	final FilterRegistration.Dynamic cors =
    	        environment.servlets().addFilter("CORS", CrossOriginFilter.class);
    	cors.setInitParameter(CrossOriginFilter.ACCESS_CONTROL_ALLOW_ORIGIN_HEADER, "*");
    	cors.setInitParameter(CrossOriginFilter.ACCESS_CONTROL_ALLOW_CREDENTIALS_HEADER, Boolean.TRUE.toString());
        cors.setInitParameter(CrossOriginFilter.ALLOWED_HEADERS_PARAM, "Content-Type,Authorization,X-Requested-With,Content-Length,Accept,Origin");
        cors.setInitParameter("allowedMethods", "OPTIONS,GET,PUT,POST,DELETE,HEAD");
        cors.addMappingForUrlPatterns(EnumSet.allOf(DispatcherType.class), true, "/*");
        cors.setInitParameter(CrossOriginFilter.CHAIN_PREFLIGHT_PARAM, Boolean.FALSE.toString());
    }
    
    private void configureAtmosphere(final tapBattleServerConfiguration config, final Environment environment) {
    	AtmosphereServlet servlet = new AtmosphereServlet();
    	servlet.framework().addInitParameter("com.tapBattle.server.api","com.tapBattle.server.resources");
    	servlet.framework().addInitParameter(ApplicationConfig.WEBSOCKET_CONTENT_TYPE, "application/json");
    	servlet.framework().addInitParameter(ApplicationConfig.WEBSOCKET_SUPPORT, "true");
    	 
    	ServletRegistration.Dynamic servletHolder = environment.servlets().addServlet("battle", servlet);
    	servletHolder.addMapping("/battle/*");
    }
}
