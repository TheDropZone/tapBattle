package com.tapBattle.server;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;

import com.fasterxml.jackson.annotation.JsonProperty;

import io.dropwizard.Configuration;
import io.dropwizard.db.DataSourceFactory;
import io.federecio.dropwizard.swagger.SwaggerBundleConfiguration;

public class tapBattleServerConfiguration extends Configuration {

	@Valid
	@NotNull
	@JsonProperty("database")
	DataSourceFactory database = new DataSourceFactory();
	
	
	@JsonProperty("swagger")
    public SwaggerBundleConfiguration swaggerBundleConfiguration;
	
	
	public DataSourceFactory getDataSourceFactory() {
		return database;
	}
	
	public void setDatabase(DataSourceFactory database) {
	    this.database = database;
	}
}
