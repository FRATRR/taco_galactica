package com.codeoftheweb.salvo;

import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;

import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.web.servlet.support.SpringBootServletInitializer;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.authentication.configuration.GlobalAuthenticationConfigurerAdapter;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.core.authority.AuthorityUtils;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.factory.PasswordEncoderFactories;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.WebAttributes;
import org.springframework.security.web.authentication.logout.HttpStatusReturningLogoutSuccessHandler;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.util.*;


@SpringBootApplication
public class SalvoApplication {

    public static void main(String[] args) {
        SpringApplication.run(SalvoApplication.class, args);
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return PasswordEncoderFactories.createDelegatingPasswordEncoder();

    }

    @Bean
    public CommandLineRunner initData(PlayerRepository playerRepo, GameRepository gameRepo, GamePlayerRepository gamePlayerRepo, ShipRepository shipRepo, SalvoRepository salvoRepo, ScoreRepository scoreRepo) {

        return (args) -> {


            //Add players----------------------------------------------------------------------------------------------------------------


            Player p1 = new Player("player_one@salvo.com", passwordEncoder().encode("password1"));
            System.out.println("p1" + p1.getPassword());
            Player p2 = new Player("player_two@salvo.com", passwordEncoder().encode("password2"));
            System.out.println("p2" + p2.getPassword());
            Player p3 = new Player("player_three@salvo.com", passwordEncoder().encode("password3"));
            System.out.println("p3" + p3.getPassword());
            Player p4 = new Player("player_four@salvo.com", passwordEncoder().encode("password4"));

            playerRepo.save(p1);
            playerRepo.save(p2);
            playerRepo.save(p3);
            playerRepo.save(p4);


            //Add games----------------------------------------------------------------------------------------------------------------
            Game g1 = new Game();
            gameRepo.save(g1);
            Game g2 = new Game();
            gameRepo.save(g2);


            //Add game players----------------------------------------------------------------------------------------------------------------
            GamePlayer gp1 = new GamePlayer(p1, g1);
            GamePlayer gp2 = new GamePlayer(p2, g1);
            GamePlayer gp3 = new GamePlayer(p3, g2);
            GamePlayer gp4 = new GamePlayer(p4, g2);

            gamePlayerRepo.save(gp1);
            gamePlayerRepo.save(gp2);
            gamePlayerRepo.save(gp3);
            gamePlayerRepo.save(gp4);

            //Add ship ----------------------------------------------------------------------------------------------------------------

            Ship s1 = new Ship("player one boat", gp1, new ArrayList<>(Arrays.asList("H1", "H2", "H3")));
            Ship s2 = new Ship("player two boat ", gp2, new ArrayList<>(Arrays.asList("A1", "A2", "A3")));
            Ship s3 = new Ship("destroyer", gp3, new ArrayList<>(Arrays.asList("B6", "B7", "B8")));
            Ship s4 = new Ship("destroyer", gp4, new ArrayList<>(Arrays.asList("D4", "E4", "F4")));

            shipRepo.save(s1);
            shipRepo.save(s2);
            shipRepo.save(s3);
            shipRepo.save(s4);

            //Add salvo -------------------------------------------------------------------------------------------
            Salvo salvo1 = new Salvo(1, new ArrayList<>(Arrays.asList("B9", "A4", "C2", "F9", "B10")), gp1);
            Salvo salvo2 = new Salvo(1, new ArrayList<>(Arrays.asList("H1", "A2", "C7", "C1", "E5")), gp2);
            Salvo salvo3 = new Salvo(1, new ArrayList<>(Arrays.asList("B9", "A1", "C2", "E4", "D6")), gp3);
            Salvo salvo4 = new Salvo(1, new ArrayList<>(Arrays.asList("A10", "A4", "C5", "B8", "E7")), gp4);

            salvoRepo.save(salvo1);
            salvoRepo.save(salvo2);
            salvoRepo.save(salvo3);
            salvoRepo.save(salvo4);

            //Add score -------------------------------------------------------------------------------------------
           /* Score score1 = new Score(1, new Date(), p1, g1);
            Score score2 = new Score(0, new Date(), p2, g1);
            Score score3 = new Score(.5, new Date(), p3, g2);
            Score score4 = new Score(.5, new Date(), p4, g2);

            scoreRepo.save(score1);
            scoreRepo.save(score2);
            scoreRepo.save(score3);
            scoreRepo.save(score4);*/

        };

    }

    @SpringBootApplication
    public class Application extends SpringBootServletInitializer {


    }

    @Configuration
    class WebSecurityConfiguration extends GlobalAuthenticationConfigurerAdapter {

        @Autowired
        PlayerRepository playerRepository;

        @Override
        public void init(AuthenticationManagerBuilder auth) throws Exception {
            auth.userDetailsService(inputName -> {
                Player player = playerRepository.findByUserName(inputName);
                if (player != null) {
                    return new User(player.getUserName(), player.getPassword(),
                            AuthorityUtils.createAuthorityList("USER"));
                } else {
                    throw new UsernameNotFoundException("Unknown user: " + inputName);
                }
            });
        }
    }


    @EnableWebSecurity
    @Configuration
    class WebSecurityConfig extends WebSecurityConfigurerAdapter {

        @Override
        protected void configure(HttpSecurity http) throws Exception {
            http.authorizeRequests()
                    .antMatchers("/web/game.html").hasAuthority("USER")
                    .antMatchers("/web/scripts/game.js").hasAuthority("USER")
                    .antMatchers("/web/games.html").hasAuthority("USER")
                    .antMatchers("/web/login.html").permitAll()
                    .antMatchers("/web/scripts/games.js").permitAll()
                    .antMatchers("/web/styles/games_style.css").permitAll()
                    .antMatchers("/web/assets/*").permitAll()
                    .antMatchers("/web/fonts/*").permitAll()
                    .antMatchers("/api/games").permitAll()
                    .antMatchers("/api/players").permitAll();


            http.authorizeRequests()
                    .anyRequest().fullyAuthenticated()
                    .and()
                    .formLogin()
                    .usernameParameter("userName")
                    .passwordParameter("password")
                    .loginPage("/api/login");
            http.logout().logoutUrl("/api/logout");

            http.headers().frameOptions().disable();
            // turn off checking for CSRF tokens
            http.csrf().disable();

            // if user is not authenticated, just send an authentication failure response
            http.exceptionHandling().authenticationEntryPoint((req, res, exc) -> res.sendError(HttpServletResponse.SC_UNAUTHORIZED));

            // if login is successful, just clear the flags asking for authentication
            http.formLogin().successHandler((req, res, auth) -> clearAuthenticationAttributes(req));

            // if login fails, just send an authentication failure response
            http.formLogin().failureHandler((req, res, exc) -> res.sendError(HttpServletResponse.SC_UNAUTHORIZED));

            // if logout is successful, just send a success response
            http.logout().logoutSuccessHandler(new HttpStatusReturningLogoutSuccessHandler());

        }

        private void clearAuthenticationAttributes(HttpServletRequest request) {
            HttpSession session = request.getSession(false);
            if (session != null) {
                session.removeAttribute(WebAttributes.AUTHENTICATION_EXCEPTION);
            }

        }
    }
}



