
package com.electrodata.minecore360.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;

import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.provisioning.InMemoryUserDetailsManager;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
public class SecurityConfig {

    // ======================================
    // CODIFICAR CONTRASEÑAS CON BCRYPT
    // ======================================
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    // ======================================
    // 3 USUARIOS DE PRUEBA
    // ======================================
    @Bean
    public UserDetailsService userDetailsService(
            PasswordEncoder encoder,

            @Value("${MINECORE_USUARIO_PASSWORD}")
            String usuarioPassword,

            @Value("${MINECORE_SUPERVISOR_PASSWORD}")
            String supervisorPassword,

            @Value("${MINECORE_ADMIN_PASSWORD}")
            String adminPassword) {

        UserDetails usuario = User.builder()
                .username("usuario")
                .password(encoder.encode(usuarioPassword))
                .roles("USUARIO")
                .build();

        UserDetails supervisor = User.builder()
                .username("supervisor")
                .password(encoder.encode(supervisorPassword))
                .roles("SUPERVISOR")
                .build();

        UserDetails admin = User.builder()
                .username("admin")
                .password(encoder.encode(adminPassword))
                .roles("ADMIN")
                .build();

        return new InMemoryUserDetailsManager(
                usuario,
                supervisor,
                admin
        );
    }

    // ======================================
    // SEGURIDAD DE RUTAS
    // ======================================
    @Bean
    public SecurityFilterChain securityFilterChain(
            HttpSecurity http) throws Exception {

        http.authorizeHttpRequests(auth -> auth

            // PORTADA, LOGIN Y RECURSOS
            .requestMatchers(
                    "/",
                    "/login",
                    "/css/**",
                    "/js/**",
                    "/img/**",
                    "/images/**",
                    "/favicon.ico",
                    "/error"
            ).permitAll()

            // ELIMINACIÓN: SOLO ADMIN
            .requestMatchers(
                    HttpMethod.POST,
                    "/inspecciones/eliminar/**"
            ).hasRole("ADMIN")

            // EXCEL: TODOS LOS PERFILES
            // LA RESTRICCIÓN DE REGISTROS PROPIOS
            // SE APLICA EN InspeccionController.
            .requestMatchers(
                    HttpMethod.GET,
                    "/inspecciones/exportar/excel"
            ).hasAnyRole(
                    "USUARIO",
                    "SUPERVISOR",
                    "ADMIN"
            )

            // GESTIÓN DE CAMIONES
            .requestMatchers("/camiones/**")
            .hasAnyRole("SUPERVISOR", "ADMIN")

            // INSPECCIONES
            .requestMatchers("/inspecciones/**")
            .hasAnyRole(
                    "USUARIO",
                    "SUPERVISOR",
                    "ADMIN"
            )

            // DASHBOARD
            .requestMatchers("/dashboard")
            .hasAnyRole(
                    "USUARIO",
                    "SUPERVISOR",
                    "ADMIN"
            )

            // DEMÁS FUNCIONES
            .anyRequest().authenticated()
        );

        // LOGIN PERSONALIZADO
        http.formLogin(form -> form
                .loginPage("/login")
                .loginProcessingUrl("/login")
                .defaultSuccessUrl("/dashboard", true)
                .failureUrl("/login?error")
                .permitAll()
        );

        // CERRAR SESIÓN
        http.logout(logout -> logout
                .logoutUrl("/logout")
                .logoutSuccessUrl("/login?logout")
                .permitAll()
        );

        return http.build();
    }
}
