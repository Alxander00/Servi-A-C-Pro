package com.climatizacion.sistema_clima.security;

import com.climatizacion.sistema_clima.entities.UsuarioEntity;
import com.climatizacion.sistema_clima.repository.UsuarioRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.Collections;

@Service
public class UserDetailsServiceImpl implements UserDetailsService {

    @Autowired
    private UsuarioRepository usuarioRepository;

    @Override
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        UsuarioEntity usuario = usuarioRepository.findByEmail(email)
                .orElseThrow(() -> new UsernameNotFoundException("Usuario no encontrado: " + email));

        SimpleGrantedAuthority authority = new SimpleGrantedAuthority(usuario.getRol().name());

        return new User(
                usuario.getEmail(),
                usuario.getPassword(),
                usuario.isActivo(),
                true,
                true,
                true,
                Collections.singletonList(authority)
        );
    }
}