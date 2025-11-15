package com.borborema.agenda.business;

import com.borborema.agenda.configuration.security.TokenService;
import com.borborema.agenda.infrastructure.entitys.user.User;
import com.borborema.agenda.infrastructure.models.UserAuthenticationDTO;
import com.borborema.agenda.infrastructure.models.UserRegisterDTO;
import com.borborema.agenda.infrastructure.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class UserService {

    @Autowired
    AuthenticationManager authenticationManager;

    @Autowired
    UserRepository userRepository;

    @Autowired
    TokenService tokenService;

    @Autowired
    PasswordEncoder passwordEncoder;


    public void createUser (UserRegisterDTO userDto){

        String encryptedPassword = passwordEncoder.encode(userDto.password());
        User newUser = new User(userDto);
        newUser.setPassword(encryptedPassword);

        this.userRepository.save(newUser);
    }

    public String authenticate (UserAuthenticationDTO authenticationDTO) {
        var usernamePassword = new UsernamePasswordAuthenticationToken(authenticationDTO.email(), authenticationDTO.password());
        var auth = this.authenticationManager.authenticate(usernamePassword);

        var token = tokenService.generateToken((User) auth.getPrincipal());

        return token;
    }

    public List<User> listUsers() {
        List<User> users = this.userRepository.findAll();
        return users;
    }
}
