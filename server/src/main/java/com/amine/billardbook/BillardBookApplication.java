package com.amine.billardbook;

import com.amine.billardbook.dao.ReservationDao;
import com.amine.billardbook.dao.UserDao;
import com.amine.billardbook.connection.ConnectionManager;
import com.amine.billardbook.connection.JwtConnectionManager;
import com.amine.billardbook.util.BillardBookJwtTokenProvider;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.web.context.annotation.ApplicationScope;

/**
 * Classe de démarrage de l'application.
 */
@SpringBootApplication
public class BillardBookApplication {

	public static void main(String[] args) {
		SpringApplication.run(BillardBookApplication.class, args);
	}

	/**
	 * Instanciation d'un DAO pour l'ensemble de l'application.
	 * @return un DAO d'utilisateurs
	 */
	@Bean
	@ApplicationScope
	public UserDao userDao() {
		return new UserDao();
	}

	/**
	 * Instanciation d'un DAO pour l'ensemble de l'application.
	 * @return un DAO d'utilisateurs
	 */
	@Bean
	@ApplicationScope
	public ReservationDao reservationDao() {
		return new ReservationDao();
	}

	/**
	 * Instanciation d'un ConnectionManager pour l'ensemble de l'application.
	 * @return une classe implémentant l'interface <code>ConnectionManager</code>
	 */
	@Bean
	@ApplicationScope
	public ConnectionManager connectionManager() {
		//return new HttpSessionConnectionManager();
		return new JwtConnectionManager();
	}

	@Bean
	@ApplicationScope
	public BillardBookJwtTokenProvider jwtTokenProvider() {
		return new BillardBookJwtTokenProvider();
	}
}
